FROM unite-base:latest

RUN apt-get update

# install Valgrind
RUN apt-get install -y valgrind

# install Infer
RUN VERSION=1.0.0; curl -sSL "https://github.com/facebook/infer/releases/download/v$VERSION/infer-linux64-v$VERSION.tar.xz" | tar -C /opt -xJ && ln -s "/opt/infer-linux64-v$VERSION/bin/infer" /usr/local/bin/infer

# install Theta
RUN apt-get install -y --no-install-recommends libgomp1 wget && \
    git clone --depth 1 --branch v4.0.0 https://github.com/ftsrg/theta.git; cd theta && \
    wget https://github.com/ftsrg/theta/releases/download/v4.0.0/theta-xcfa-cli.jar && \
    wget https://github.com/ftsrg/theta/releases/download/v4.0.0/theta-xsts-cli.jar && \
    cd ../

# install Frama-C using apt-get
RUN apt-get install -y frama-c-base
    
# install grep
RUN apt-get install -y grep

# install Perun
RUN apt-get install -y python3 pip libfreetype-dev libmagic1 && \
    ln -s /usr/include/freetype2/ft2build.h /usr/include/ && \
    git clone --depth=1 https://github.com/tfiedor/perun.git && \
    cd perun && \
    make init && \
    make install

# install cbmc
RUN apt-get install -y cbmc

# make the image smaller
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# z3 libraries need to be in path for Theta
ENV LD_LIBRARY_PATH="$LD_LIBRARY_PATH:/theta/lib/"

# copy over Unite configuration for Infer
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf
# convert CRLF to LF in case of copying from Windows
RUN find ./unite/conf/ | xargs dos2unix