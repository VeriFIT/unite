FROM unite:latest

# copy over Unite configuration for Infer
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf

# install Infer
RUN VERSION=1.0.0; curl -sSL "https://github.com/facebook/infer/releases/download/v$VERSION/infer-linux64-v$VERSION.tar.xz" | tar -C /opt -xJ && ln -s "/opt/infer-linux64-v$VERSION/bin/infer" /usr/local/bin/infer