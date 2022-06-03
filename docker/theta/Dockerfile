FROM unite-base:latest

# install Theta
RUN apt-get update && \
    apt-get install -y --no-install-recommends libgomp1 wget && \
    apt-get clean && rm -rf /var/lib/apt/lists/* && \
    git clone --depth 1 --branch v4.0.0 https://github.com/ftsrg/theta.git; cd theta && \
    wget https://github.com/ftsrg/theta/releases/download/v4.0.0/theta-xcfa-cli.jar && \
    wget https://github.com/ftsrg/theta/releases/download/v4.0.0/theta-xsts-cli.jar && \
    cd ../

# z3 libraries need to be in path
ENV LD_LIBRARY_PATH="$LD_LIBRARY_PATH:/theta/lib/"

# copy over Unite configuration for Theta
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf
# convert CRLF to LF in case of copying from Windows
RUN find ./unite/conf/ | xargs dos2unix