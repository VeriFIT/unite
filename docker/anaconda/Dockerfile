FROM unite:latest

# copy over Unite configuration for anaconda
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf

# install anaconda
RUN apt-get install -y m4 wget && \
    git clone --depth 1 --recurse-submodules --shallow-submodules https://pajda.fit.vutbr.cz/anaconda/anaconda.git && \
    cd anaconda && \
    ./tools/build.sh --setup-environment && \
    ./tools/build.sh all && \
    cd ..