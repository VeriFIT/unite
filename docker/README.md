# Docker Images for Unite
This directory contains a docker image for Unite (in ./unite-base/).
Then there is a number of analysis tools setup with Unite in separate docker
images. Each analysis tool image is based on the base Unite image. 
There is also unite-all which contains all analysis tools in a single image.

## HOW TO all at once
To build and run all analysis tool dockers as separate dockers use:
```
$ make build-all
$ make up-all
...
# make down-all     # to shut down
```

To build and run all analysis tools in a single docker use:
```
$ build-all-as-one
$ up-one
...
# make down-one     # to shut down
```

## HOW TO manual one by one
Pick a directory based on the desired docker image and run:
```
$ cd facebook-infer        # or any other directory
$ make build
$ make run-new
# Unite is now listening on localhost on ports 8080 and 8081
```

To rerun an existing container try using:
```
$ make run-existing
```

Unite configuration can be changed by placing the contents of your unite "conf"
directory into the "unite-conf" directory of your desired image.
See also the Makefile options for further commands.

## HOW TO with docker compose
Alternatively, you can use docker-compose. Pick a directory based on the desired
docker image and then run:
```
$ cd facebook-infer        # or any other directory
$ make compose
# Unite is now listening on localhost on ports 8080 and 8081
# Or you can change the docker-compose.yml to customize ports and host
```