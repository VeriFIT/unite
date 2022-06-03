FROM unite-base:latest

# install Frama-C using opam
#! fails on installing libgtksourceview2.0-dev
# RUN apt-get install -y opam
# RUN opam init --disable-sandboxing
# RUN opam install -y depext
# RUN opam depext frama-c
# RUN opam install -y frama-c


# install Frama-C using apt-get
RUN apt-get update && \
    apt-get install -y frama-c-base

# make the image smaller
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# copy over Unite configuration for Valgrind
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf
# convert CRLF to LF in case of copying from Windows
RUN find ./unite/conf/ | xargs dos2unix