FROM openjdk:11-slim
WORKDIR /unite-docker
EXPOSE 8080
EXPOSE 8081
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get install -y build-essential && \
    apt-get install -y curl && \
    apt-get install -y procps && \
    apt-get install -y git && \
    git clone -b dev https://pajda.fit.vutbr.cz/verifit/unite.git && \
    /bin/bash ./unite/build.sh
CMD ["./unite/run_all.sh"]