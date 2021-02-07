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

set /A SLEEP=1

setlocal EnableDelayedExpansion
set LF=^


set HELP= Launches the sparql triplestore, and then the analysis adapter and the compilation adapter.!LF!^
 The triplestore needs to finish startup before both adapters, which is controled by giving!LF!^
 the triplestore a headstart. Duration of the headstart in seconds is controlled using an!LF!^
 optional argument (default !SLEEP!).!LF!^
 !LF!^
 Usage: run_all.bat [triplestore_sleep_seconds]!LF!

:: process arguments
IF "%1" == "" ( 
  echo  Using default %SLEEP%s sleep for triplestore to startup
) ELSE (
    IF "%2" == "" ( 
        IF "%1" == "-h" (
            echo !HELP!
            exit 0
        ) ELSE (
            IF "%1" == "--help" (
                echo !HELP!
                exit 0
            ) ELSE (
                :: below - if not isnumber(%1)
                SET "notNumber="&for /f "delims=0123456789" %%i in ("%1") do set notNumber=%%i
                IF defined notNumber (
                    echo  Argument has to be a number
                    echo  Usage: run_all.bat [triplestore_sleep_seconds]
                    exit 1
                ) ELSE (
                    set /A SLEEP=%1
                )
            )
        )
    ) ELSE ( 
        echo  Invalid arguments
        echo  Usage: run_all.bat [triplestore_sleep_seconds]
        exit 1
    )
)


set USRPATH=%CD%
cd %~dp0

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
START "Universal VeriFIT OSLC Adapter - Triplestore" powershell "java -DFUSEKI_BASE=\""..\triplestore\"" -jar start.jar | tee -a %USRPATH%\logs\triplestore_%CURTIME%.txt"
:: wait a while to let the triplestore start
timeout /t %SLEEP% /nobreak > NUL

:: start the compilation adapter
echo Starting the Compilation adapter
cd ..\..\compilation
START "Universal VeriFIT OSLC Adapter - Compilation"  powershell "mvn jetty:run-exploded | tee -a %USRPATH%\logs\compilation_%CURTIME%.txt"

:: start the analysis adapter
echo Starting the Analysis adapter
cd ..\analysis
START "Universal VeriFIT OSLC Adapter - Analysis" powershell "mvn jetty:run-exploded | tee -a  %USRPATH%\logs\analysis_%CURTIME%.txt"

echo.
echo Wait till startup finishes (see the 3 new opened consoles)
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