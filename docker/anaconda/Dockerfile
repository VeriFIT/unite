FROM unite-base:latest

# install anaconda
RUN apt-get update && \
    apt-get install -y m4 wget
RUN git clone --depth 1 --recurse-submodules --shallow-submodules https://pajda.fit.vutbr.cz/anaconda/anaconda.git
#RUN cd anaconda
#RUN cd anaconda && ./tools/build.sh --setup-environment
#RUN cd anaconda && ./tools/build.sh all
#RUN echo 0 > /proc/sys/kernel/yama/ptrace_scope

# TODO currently broken

# make the image smaller
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# copy over Unite configuration for anaconda
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf
# convert CRLF to LF in case of copying from Windows
RUN find ./unite/conf/ | xargs dos2unix