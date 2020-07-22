@echo off
cd %~dp0

echo Starting the Triplestore
cd sparql_triplestore/jetty-distribution
START "Universal VeriFIT OSLC Adapter - Triplestore" java -DFUSEKI_BASE="../triplestore" -jar start.jar

echo Starting the Compilation adapter
cd ../../compilation
START "Universal VeriFIT OSLC Adapter - Compilation" mvn jetty:run-exploded

echo Starting the Analysis adapter
cd ../analysis
START "Universal VeriFIT OSLC Adapter - Analysis" mvn jetty:run-exploded

echo.
echo Wait till startup finishes (see the 3 new opened consoles)
echo Press any key to exit...
echo (do not use ctrl+c otherwise youll need to kill subprocesses manualy)...
pause >nul

taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter - Triplestore*" /T /F >null
taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter - Compilation*" /T /F >null
taskkill /FI "WindowTitle eq Universal VeriFIT OSLC Adapter - Analysis*" /T /F  >null