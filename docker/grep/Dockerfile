ARG UNITE_VERSION=latest
FROM unite-base:${UNITE_VERSION}

# install grep
RUN apt-get update && \
    apt-get install -y grep

# make the image smaller
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# copy over Unite configuration for Valgrind
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf
# convert CRLF to LF in case of copying from Windows
RUN find ./unite/conf/ | xargs dos2unix