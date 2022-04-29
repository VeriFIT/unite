FROM unite:latest

# copy over Unite configuration for anaconda
COPY ./unite-conf/* ./unite/conf/analysis_advanced/AnalysisTools/

# install anaconda
RUN apt-get install -y m4 wget && \
    git clone --depth 1 --recurse-submodules --shallow-submodules https://pajda.fit.vutbr.cz/anaconda/anaconda.git && \
    cd anaconda && \
    ./tools/build.sh --setup-environment && \
    ./tools/build.sh all && \
    cd ..