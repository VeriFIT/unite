FROM unite-base:latest

# install anaconda
RUN apt-get install -y m4 wget && \
    git clone --depth 1 --recurse-submodules --shallow-submodules https://pajda.fit.vutbr.cz/anaconda/anaconda.git && \
    cd anaconda && \
    ./tools/build.sh --setup-environment && \
    ./tools/build.sh all && \
    cd ..

# make the image smaller
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# copy over Unite configuration for anaconda
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf
# convert CRLF to LF in case of copying from Windows
RUN find ./unite/conf/ | xargs dos2unix