ARG UNITE_VERSION=latest
FROM unite-base:${UNITE_VERSION}


# install dependencies
RUN apt-get update && apt-get install unzip

# install its-tools
RUN mkdir its && \
    cd its && \
    wget https://lip6.github.io/ITS-commandline/fr.lip6.move.gal.itscl.product-linux.gtk.x86_64.zip && \
    unzip fr.lip6.move.gal.itscl.product-linux.gtk.x86_64.zip && \
    rm fr.lip6.move.gal.itscl.product-linux.gtk.x86_64.zip

## make the image smaller
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# copy over Unite configuration for cbmc
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf
# convert CRLF to LF in case of copying from Windows
RUN find ./unite/conf/ | xargs dos2unix