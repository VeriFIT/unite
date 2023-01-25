ARG UNITE_VERSION=latest
FROM unite-base:${UNITE_VERSION}

# install Perun
RUN apt-get update && \
    apt-get install -y python3 pip libfreetype-dev libmagic1 && \
    ln -s /usr/include/freetype2/ft2build.h /usr/include/ && \
    git clone --depth=1 https://github.com/tfiedor/perun.git && \
    cd perun && \
    make init && \
    make install

# make the image smaller
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# copy over Unite configuration for Valgrind
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf
# convert CRLF to LF in case of copying from Windows
RUN find ./unite/conf/ | xargs dos2unix