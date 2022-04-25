FROM unite:latest
EXPOSE 8080
EXPOSE 8081
RUN VERSION=1.0.0; curl -sSL "https://github.com/facebook/infer/releases/download/v$VERSION/infer-linux64-v$VERSION.tar.xz" | tar -C /opt -xJ && ln -s "/opt/infer-linux64-v$VERSION/bin/infer" /usr/local/bin/infer && \
    cp unite/analysis/tests/resources/AnalysisTools/infer.rdf unite/conf/analysis_advanced/AnalysisTools/ && \
    cp unite/analysis/tests/resources/AnalysisTools/infer.properties unite/conf/analysis_advanced/AnalysisTools/