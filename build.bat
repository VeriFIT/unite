@echo off

setlocal EnableDelayedExpansion
set LF=^

set USRPATH=%CD%
set ROOTDIR=%~dp0
cd %ROOTDIR%

echo #########################################################
echo #### Building and Installing shared resources
echo #########################################################
cd shared
call mvn clean install
echo #### Done

echo #########################################################
echo #### Building and Installing the Compilation adapter
echo #########################################################
cd ..\compilation
call mvn clean install
echo #### Done


echo #########################################################
echo #### Building and Installing the Analysis adapter
echo #########################################################
cd ..\analysis
call mvn clean install
echo #### Done

echo.
echo #### ALL DONE ####
echo.

echo     Make sure to create configuration files for both adapters
echo     by creating analysis/VeriFitAnalysis.properties and
echo     by creating compilation/VeriFitCompilation.properties.
echo     Both directories contian a *Example.properties file that
echo     can be used as a template.
echo.
