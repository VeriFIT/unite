# Docker Images for Unite
This directory contains a docker image for Unite (in ./unite-only/).
Then there is a number of analysis tools setup with Unite in separate docker
images. Each analysis tool image is based on the base Unite image. 

## HOW TO
Pick a directory based on the desired docker image and run:
```
$ cd facebook-infer        # or any other directory
$ make build
$ make run-new
# Unite is now listing on ports 8080 and 8081
```

To rerun an existing container try using:
```
$ make run-existing
```

See the Makefile options for other commands.
