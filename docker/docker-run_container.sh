#!/bin/bash

NAME=unite-docker

docker run -p 8080:8080 -p 8081:8081 --name $NAME unite || docker start -a $NAME