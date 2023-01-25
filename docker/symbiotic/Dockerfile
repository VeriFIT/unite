ARG UNITE_VERSION=latest
FROM unite-base:${UNITE_VERSION}

# install symbiotic
# [inspired by https://github.com/staticafi/symbiotic/blob/master/Dockerfile]
RUN apt-get update && \
    apt-get install -y git cmake make llvm zlib1g-dev clang g++ python3 python curl wget rsync unzip gcc-multilib xz-utils libz3-dev libsqlite3-dev && \
    git clone --depth 1 --no-single-branch  https://github.com/staticafi/symbiotic && \
    git config --global user.email "anonymous@email.com" && \
    git config --global user.name "anonymous" && \
    cd symbiotic && \
    ./build.sh -j4 && \
    ln -s "/symbiotic/scripts/symbiotic" "/usr/local/bin/symbiotic"

## make the image smaller
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# copy over Unite configuration for symbiotic
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf
# convert CRLF to LF in case of copying from Windows
RUN find ./unite/conf/ | xargs dos2unix