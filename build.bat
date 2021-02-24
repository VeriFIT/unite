@echo off

setlocal EnableDelayedExpansion
set LF=^

set USRPATH=%CD%
set ROOTDIR=%~dp0
cd %ROOTDIR%

echo.
echo ############################################################
echo     Checking configuration files
echo ############################################################
echo.
call :confFileCheckOrDefault .\analysis\VeriFitAnalysis.properties .\analysis\VeriFitAnalysisExample.properties
call :confFileCheckOrDefault .\compilation\VeriFitCompilation.properties .\compilation\VeriFitCompilationExample.properties
call :confFileCheckOrDefault .\sparql_triplestore\jetty-distribution\start.ini .\sparql_triplestore\jetty-distribution\startExample.ini

echo.
echo ############################################################
echo     Building and Installing shared resources
echo ############################################################
echo.
cd shared
call mvn clean install

echo.
echo ############################################################
echo     Building and Installing the Compilation adapter
echo ############################################################
echo.
cd ..\compilation
call mvn clean install


echo.
echo ############################################################
echo     Building and Installing the Analysis adapter
echo ############################################################
echo.
cd ..\analysis
call mvn clean install

echo.
echo ##### ALL DONE #############################################
echo.
exit 0



:: Checks if a conf file exists. If not, then a default one is created.
:: $1 = file to check
:: $2 = default file to copy if not found
:confFileCheckOrDefault
    echo |set /p="Checking %~1: "
    if not exist %~1 (
        echo Not found
        echo     Creating a default one
        copy %~2 %~1 >nul
    ) else (
        echo OK
    )
exit /B 0