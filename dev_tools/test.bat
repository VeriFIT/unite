@echo off

setlocal EnableDelayedExpansion
set LF=^


set HELP=   Runs the run_all script and then Postman testsuites for the adapter using Newman.!LF!

set USAGE=   Usage: test.bat [-t^|-h]!LF!^
      -t ... "tools" - Runs an additional testsuite which requires some analysis!LF!^
                         tools to be installed (i.e. will not work on all machines)!LF!^
      -h ... help!LF!

:: process arguments
if "%1" == "-t" (
    rem :: nothing
) else if "%1" == "-h" (
    echo.
    echo !HELP!
    echo !USAGE!
    echo.
    exit 0
) else if not "%1" == "" ( 
    echo Invalid arguments
    echo.
    echo !USAGE!
    exit 1
) 

set USRPATH=%CD%
set ROOTDIR=%~dp0
cd %ROOTDIR%


:: make sure configuration files exist
if not exist "..\analysis\VeriFitAnalysis.properties" (
    echo ERROR: Configuration file "%ROOTDIR%analysis\VeriFitAnalysis.properties" not found.
    echo   The adapter needs to be configured to be able to run!
    echo   See the "VeriFitAnalysisExample.properties" file for instructions and use it as a template.
    exit 1
)
if not exist "..\compilation\VeriFitCompilation.properties" (
    echo ERROR: Configuration file "%ROOTDIR%compilation\VeriFitCompilation.properties" not found.
    echo   The adapter needs to be configured to be able to run!
    echo   See the "VeriFitCompilationExample.properties" file for instructions and use it as a template.
    exit 1
)

:: lookup analysis adapter config
FOR /F "tokens=*" %%g IN ('findstr adapter_host ..\analysis\VeriFitAnalysis.properties') do (SET ANALYSIS_HOST=%%g)
set ANALYSIS_HOST=%ANALYSIS_HOST:~13%
FOR /F "tokens=*" %%g IN ('findstr adapter_port ..\analysis\VeriFitAnalysis.properties') do (SET ANALYSIS_PORT=%%g)
set ANALYSIS_PORT=%ANALYSIS_PORT:~13%
set ANALYSIS_URL=%ANALYSIS_HOST%:%ANALYSIS_PORT%/analysis

:: start the analysis adapter
mkdir %ROOTDIR%\logs > nul 2>&1
echo Booting up the Universal Analysis Adapter
START /MIN "Universal VeriFIT OSLC Adapter" CMD /C "..\run_all.bat >> %ROOTDIR%\logs\run_all.log 2>&1"
call :poll_url %ANALYSIS_URL%
echo Adapter up and running
echo.

set compilationRes=0
set analysisRes=0
set analysisToolsRes=0

echo.
echo Running Compilation adapter test suite
call newman run ..\compilation\tests\TestSuite.postman_collection
set compilationRes=%errorlevel%

echo.
echo Running Analysis adapter test suite
call newman run ..\analysis\tests\TestSuite.postman_collection
set analysisRes=%errorlevel%

if "%1" == "-t" (
    echo.
    echo Running Analysis adapter Tested Tools test suite
    call newman run ..\analysis\tests\TestSuite_TestedTools.postman_collection
    set analysisToolsRes=%errorlevel%
)

echo.
echo Shutting down the adaters...
:: kill all subprocesses based on window titles
taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter" /T /F > nul 2>&1
echo All done.

:: return non-zero if there were failed tests
if not %compilationRes% == 0 (
    echo.
    echo.
    echo   TESTS FAILED
    echo.
    exit 1
)
if not %analysisRes% == 0 (
    echo.
    echo.
    echo   TESTS FAILED
    echo.
    exit 1
)
if not %compilationRes% == 0 (
    echo.
    echo.
    echo   TESTS FAILED
    echo.
    exit 1
)

echo.
echo.
echo   TESTS PASSED
echo.
exit 0






:: polls an address using curl until the request returns zero (i.e. the address responds)
:: patams: address - URL for curl to poll
:poll_url
:loop
timeout /t 3 /nobreak > NUL
echo |set /p="."
powershell curl -UseBasicParsing %~1 > nul 2>&1
if not %errorlevel% == 0 goto :loop
echo.
exit /B 0