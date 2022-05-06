FROM openjdk:11-slim
WORKDIR /

# default ports
EXPOSE 8080
EXPOSE 8081

RUN apt-get update && \
    apt-get install -y maven && \
    apt-get install -y build-essential && \
    apt-get install -y curl && \
    apt-get install -y procps && \
    apt-get install -y git && \
    git clone --depth 1 https://pajda.fit.vutbr.cz/verifit/unite.git && \
    /bin/bash ./unite/build.sh

# copy over Unite configuration (by default there is no configuration)
# make sure to set the right ports in case you change then in the conf
COPY ./unite-conf ./unite/conf

CMD ["./unite/run_all.sh"]