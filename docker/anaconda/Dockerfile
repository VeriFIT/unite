FROM unite:latest
RUN apt-get install -y m4 && \
    git clone --recurse-submodules https://pajda.fit.vutbr.cz/anaconda/anaconda.git && \
    cd anaconda && \
    ./tools/build.sh --setup-environment && \
    ./tools/build.sh all && \
    cd .. && \
    cp unite/analysis/tests/resources/AnalysisTools/anaconda.rdf unite/conf/analysis_advanced/AnalysisTools/ && \
    cp unite/analysis/tests/resources/AnalysisTools/anaconda.properties unite/conf/analysis_advanced/AnalysisTools/