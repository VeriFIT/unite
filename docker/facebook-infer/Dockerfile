FROM unite:latest

# copy over Unite configuration for Infer
COPY ./unite-conf/* ./unite/conf/analysis_advanced/AnalysisTools/

# install Infer
RUN VERSION=1.0.0; curl -sSL "https://github.com/facebook/infer/releases/download/v$VERSION/infer-linux64-v$VERSION.tar.xz" | tar -C /opt -xJ && ln -s "/opt/infer-linux64-v$VERSION/bin/infer" /usr/local/bin/infer