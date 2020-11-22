#!/bin/bash

SLEEP=1

HELP=" Launches the sparql triplestore, and then the analysis adapter and the compilation adapter.
 The triplestore needs to finish startup before both adapters, which is controled by polling the 
 triplestore using curl until it responds

 Usage: run_all.sh"

# process arguments
if [ "$#" -ne 0 ]; then
    echo -e " Invalid arguments\n Usage: run_all.sh"
    exit 1
fi


USRPATH=$PWD # get the call directory
cd "${BASH_SOURCE%/*}" # move to the script directory

# catch ctrl+c and kill all subprocesses
trap 'killall' INT
killall() {
    trap '' INT TERM     # ignore INT and TERM while shutting down
    echo "Shutting down..."
    kill -TERM 0
    wait
    echo "All done."
    exit
}

# polls an address using curl until the request returns zero (i.e. the address responds)
# patams: address - URL for curl to poll
curl_poll()
{
    curl_ret=42
    while [ $curl_ret != 0 ]
    do
        sleep 3
        echo -n "."
        curl $1 &> /dev/null
        curl_ret=$?
    done
    echo
}


# lookup triplestore config
triplestore_host=$(cat sparql_triplestore/jetty-distribution/start.ini | grep "^ *jetty.http.host=" | sed "s/^ *jetty.http.host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
triplestore_port=$(cat sparql_triplestore/jetty-distribution/start.ini | grep "^ *jetty.http.port=" | sed "s/^ *jetty.http.port=//")
triplestore_url="$triplestore_host:$triplestore_port/fuseki/"
# lookup compilation adapter config
compilation_host=$(cat compilation/VeriFitCompilation.properties | grep "^ *adapter_host=" | sed "s/^ *adapter_host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
compilation_port=$(cat compilation/VeriFitCompilation.properties | grep "^ *adapter_port=" | sed "s/^ *adapter_port=//")
compilation_url="$compilation_host:$compilation_port/compilation/"
# lookup analysis adapter config
analysis_host=$(cat analysis/VeriFitAnalysis.properties | grep "^ *adapter_host=" | sed "s/^ *adapter_host=//" | sed "s|/$||") # removes final slash in case there is one (http://host/ vs http://host)
analysis_port=$(cat analysis/VeriFitAnalysis.properties | grep "^ *adapter_port=" | sed "s/^ *adapter_port=//")
analysis_url="$analysis_host:$analysis_port/analysis/"


# create log files and append headings
mkdir $USRPATH/logs &> /dev/null
CURTIME=$(date +%F_%T)
echo -e "####################################################\n## Run started at: $CURTIME" > "$USRPATH/logs/triplestore_$CURTIME.log"
echo -e "####################################################\n## Run started at: $CURTIME" > "$USRPATH/logs/compilation_$CURTIME.log"
echo -e "####################################################\n## Run started at: $CURTIME" > "$USRPATH/logs/analysis_$CURTIME.log"

# start the triplestore
echo "Starting the Triplestore"
cd sparql_triplestore
./run.sh &> "$USRPATH/logs/triplestore_$CURTIME.log" &
echo "Waiting for the Triplestore to finish startup"
curl_poll "$triplestore_url"
echo -e "Triplestore running\n"

# start the compilation adapter
echo "Starting the Compilation adapter"
cd ../compilation
mvn jetty:run-exploded &> "$USRPATH/logs/compilation_$CURTIME.log" &
echo "Waiting for the Compilation adapter to finish startup"
curl_poll "$compilation_url"
echo -e "Compilation adapter running\n"

# start the analysis adapter
echo "Starting the Analysis adapter"
cd ../analysis
mvn jetty:run-exploded &> "$USRPATH/logs/analysis_$CURTIME.log" &
echo "Waiting for the Analysis adapter to finish startup"
curl_poll "$analysis_url"
echo -e "Analysis adapter running\n"

echo "Ready to go!"
echo "Use ctrl+c to exit..."
cat # wait forever
