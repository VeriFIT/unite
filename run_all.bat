@echo off
cd %~dp0

:: start the triplestore
echo Starting the Triplestore
cd sparql_triplestore/jetty-distribution
START "Universal VeriFIT OSLC Adapter - Triplestore" java -DFUSEKI_BASE="../triplestore" -jar start.jar
:: wait a while to let the triplestore start
timeout /t 3 /nobreak > NUL

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
echo (do not use ctrl+c otherwise youll need to kill subprocesses manualy)...
:: sleep until a key is pressed
pause > NUL

:: kill all subprocesses based on window titles
taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter - Triplestore*" /T /F >null
taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter - Compilation*" /T /F >null
taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter - Analysis*" /T /F  >null