FROM unite-base:latest

# install Infer
RUN apt-get install -y valgrind

# copy over Unite configuration for Valgrind
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf
# convert CRLF to LF in case of copying from Windows
RUN find ./unite/conf/ | xargs dos2unix