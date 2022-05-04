FROM unite:latest

# copy over Unite configuration for Valgrind
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf

# install Infer
RUN apt-get install -y valgrind
