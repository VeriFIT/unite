FROM unite:latest

# copy over Unite configuration for Theta
COPY ./unite-conf/* ./unite/conf/analysis_advanced/AnalysisTools/

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
