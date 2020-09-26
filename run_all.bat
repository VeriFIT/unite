@echo off
cd %~dp0

setlocal EnableDelayedExpansion
set LF=^


set HELP= Launches the sparql triplestore, and then the analysis adapter and the compilation adapter.!LF!^
 The triplestore needs to finish startup before both adapters, which is controled by giving!LF!^
 the triplestore a headstart. Duration of the headstart in seconds is controlled using an!LF!^
 optional argument (default 3).!LF!^
 !LF!^
 Usage: run_all.sh [triplestore_sleep_seconds]!LF!

:: process arguments
set /A SLEEP=3
IF "%1" == "" ( 
  echo  Using default 3s sleep for triplestore to startup
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
                    echo  Usage: run_all.sh [triplestore_sleep_seconds]
                    exit 1
                ) ELSE (
                    set /A SLEEP=%1
                )
            )
        )
    ) ELSE ( 
        echo  Invalid arguments
        echo  Usage: run_all.sh [triplestore_sleep_seconds]
        exit 1
    )
)

:: start the triplestore
echo Starting the Triplestore
cd sparql_triplestore/jetty-distribution
START "Universal VeriFIT OSLC Adapter - Triplestore" java -DFUSEKI_BASE="../triplestore" -jar start.jar
:: wait a while to let the triplestore start
timeout /t %SLEEP% /nobreak > NUL

:: start the compilation adapter
echo Starting the Compilation adapter
cd ../../compilation
START "Universal VeriFIT OSLC Adapter - Compilation" mvn jetty:run-exploded

:: start the analysis adapter
echo Starting the Analysis adapter
cd ../analysis
START "Universal VeriFIT OSLC Adapter - Analysis" mvn jetty:run-exploded

echo.
echo Wait till startup finishes (see the 3 new opened consoles)
echo Press any key to exit...
echo (do not use ctrl+c otherwise youll need to kill subprocesses manualy)
:: sleep until a key is pressed
pause > NUL

:: kill all subprocesses based on window titles
taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter - Triplestore*" /T /F >null
taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter - Compilation*" /T /F >null
taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter - Analysis*" /T /F  >null