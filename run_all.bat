@echo off

::##########################
::# Copyright (C) 2020 Ondřej Vašíček <ondrej.vasicek.0@gmail.com>, <xvasic25@stud.fit.vutbr.cz>
::#
::# This program and the accompanying materials are made available under
::# the terms of the Eclipse Public License 2.0 which is available at
::# https://www.eclipse.org/legal/epl-2.0
::#
::# SPDX-License-Identifier: EPL-2.0
::##########################

setlocal EnableDelayedExpansion
set LF=^


set HELP= Launches the sparql triplestore, and then the analysis adapter and the compilation adapter.!LF!^
 The triplestore needs to finish startup before both adapters, which is controled by polling the!LF!^
 triplestore using curl until it responds!LF!^
 !LF!^
 Usage: run_all.bat!LF!

:: process arguments
IF not "%1" == "" ( 
    echo  Invalid arguments
    echo  Usage: run_all-nopowershell.bat
    exit 1
)

set USRPATH=%CD%
cd %~dp0


:: lookup triplestore config
FOR /F "tokens=*" %%g IN ('findstr jetty.http.host sparql_triplestore\jetty-distribution\start.ini') do (SET TRIPESTORE_HOST=%%g)
set TRIPESTORE_HOST=%TRIPESTORE_HOST:~16%
FOR /F "tokens=*" %%g IN ('findstr jetty.http.port sparql_triplestore\jetty-distribution\start.ini') do (SET TRIPESTORE_PORT=%%g)
set TRIPESTORE_PORT=%TRIPESTORE_PORT:~16%
:: check if the triplestore is running or https or not (checks whether the jetty config is commented out)
FOR /F "tokens=*" %%g IN ('findstr jetty.httpConfig.secureScheme sparql_triplestore\jetty-distribution\start.ini') do (SET TRIPESTORE_PROTOCOL=%%g)
set TRIPESTORE_PROTOCOL=%TRIPESTORE_PROTOCOL:~0,1%
if %TRIPESTORE_PROTOCOL% == # (
    set TRIPESTORE_PROTOCOL=http://
) else (
    set TRIPESTORE_PROTOCOL=https://
)
set TRIPESTORE_URL=%TRIPESTORE_PROTOCOL%%TRIPESTORE_HOST%:%TRIPESTORE_PORT%/fuseki

:: lookup compilation adapter config
FOR /F "tokens=*" %%g IN ('findstr adapter_host compilation\VeriFitCompilation.properties') do (SET COMPILATION_HOST=%%g)
set COMPILATION_HOST=%COMPILATION_HOST:~13%
FOR /F "tokens=*" %%g IN ('findstr adapter_port compilation\VeriFitCompilation.properties') do (SET COMPILATION_PORT=%%g)
set COMPILATION_PORT=%COMPILATION_PORT:~13%
set COMPILATION_URL=%COMPILATION_HOST%:%COMPILATION_PORT%/compilation

:: lookup analysis adapter config
FOR /F "tokens=*" %%g IN ('findstr adapter_host analysis\VeriFitAnalysis.properties') do (SET ANALYSIS_HOST=%%g)
set ANALYSIS_HOST=%ANALYSIS_HOST:~13%
FOR /F "tokens=*" %%g IN ('findstr adapter_port analysis\VeriFitAnalysis.properties') do (SET ANALYSIS_PORT=%%g)
set ANALYSIS_PORT=%ANALYSIS_PORT:~13%
set ANALYSIS_URL=%ANALYSIS_HOST%:%ANALYSIS_PORT%/analysis

:: create log files and append headings
mkdir %USRPATH%\logs > nul 2>&1
set CURTIME=%DATE%_%TIME:~0,2%.%TIME:~3,2%.%TIME:~6,2%
set CURTIME=%CURTIME: =0%
set CURTIME=%CURTIME:\=-%
set CURTIME=%CURTIME:/=-%
echo ####################################################!LF!## Run started at: %CURTIME% > %USRPATH%\logs\triplestore_%CURTIME%.txt
echo ####################################################!LF!## Run started at: %CURTIME% > %USRPATH%\logs\compilation_%CURTIME%.txt
echo ####################################################!LF!## Run started at: %CURTIME% > %USRPATH%\logs\analysis_%CURTIME%.txt

:: start the triplestore
echo Starting the Triplestore
cd sparql_triplestore\jetty-distribution
START /MIN "Universal VeriFIT OSLC Adapter - Compilation" CMD /C "java -DFUSEKI_BASE=..\triplestore -jar start.jar >> %USRPATH%\logs\triplestore_%CURTIME%.txt 2>&1"
echo Waiting for the Triplestore to finish startup
call :poll_url %TRIPESTORE_URL%
echo Triplestore running
echo.

:: start the compilation adapter
echo Starting the Compilation adapter
cd ..\..\compilation
START /MIN "Universal VeriFIT OSLC Adapter - Compilation" CMD /C "mvn jetty:run-exploded >> %USRPATH%\logs\compilation_%CURTIME%.txt 2>&1"  
echo Waiting for the Compilation adapter to finish startup
call :poll_url %COMPILATION_URL%
echo Compilation adapter running
echo.

:: start the analysis adapter
echo Starting the Analysis adapter
cd ..\analysis
START /MIN "Universal VeriFIT OSLC Adapter - Analysis" CMD /C "mvn jetty:run-exploded >> %USRPATH%\logs\analysis_%CURTIME%.txt 2>&1"  
echo Waiting for the Analysis adapter to finish startup
call :poll_url %ANALYSIS_URL%
echo Analysis adapter running
echo.

echo Ready to go
echo Press any key to exit...
echo (do not use ctrl+c otherwise youll need to kill subprocesses manualy)
:: sleep until a key is pressed
pause > NUL

echo Shutting down...
:: kill all subprocesses based on window titles
taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter - Triplestore*" /T /F > nul 2>&1
taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter - Compilation*" /T /F > nul 2>&1
taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter - Analysis*" /T /F  > nul 2>&1
echo All done.
exit 0



:: polls an address using curl until the request returns zero (i.e. the address responds)
:: patams: address - URL for curl to poll
:poll_url
:loop
timeout /t 3 /nobreak > NUL
echo |set /p="."
curl %~1 > nul 2>&1
if not %errorlevel% == 0 goto :loop
echo.
exit /B 0