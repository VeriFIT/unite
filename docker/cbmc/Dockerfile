FROM unite-base:latest

# install cbmc
RUN apt-get update && \
    apt-get install -y cbmc

## make the image smaller
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# copy over Unite configuration for symbiotic
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf