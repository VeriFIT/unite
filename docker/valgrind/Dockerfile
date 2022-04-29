FROM unite:latest

# copy over Unite configuration for Theta
COPY ./unite-conf/* ./unite/conf/analysis_advanced/AnalysisTools/

# install Infer
RUN apt-get install -y valgrind
