FROM openjdk:11-slim
WORKDIR /

RUN apt-get update && \
    apt-get install -y maven build-essential curl procps git vim tmux dos2unix
#RUN git clone --depth 1 --no-single-branch --branch dev https://pajda.fit.vutbr.cz/verifit/unite.git && \
RUN git clone --depth 1 --no-single-branch https://pajda.fit.vutbr.cz/verifit/unite.git && \
    cd unite && git config pull.rebase false && cd .. && \
    /bin/bash ./unite/build.sh

# default ports
EXPOSE 8080
EXPOSE 8081

# copy over Unite configuration (by default there is no configuration)
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf
# convert CRLF to LF in case of copying from Windows
RUN find ./unite/conf/ | xargs dos2unix

CMD ["./unite/run_all.sh"]