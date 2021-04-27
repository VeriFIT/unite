# Universal OSLC Analysis Adapter

author: Ondrej Vasicek, xvasic25@stud.fit.vutbr.cz

This is a development repository of the Universal OSLC Analysis Adapter which aims to provide an easy way of adding an OSLC Automation interface to as many analysis tools as possible by leveraging their command-line similarities. The adapter consists of two main components - Analysis Adapter and Compilation Adapter. The Compilation Adapter manages SUT resources, and the Analysis Adapter executes analysis on SUT resources using any configured analysis tool. The repository also includes a distribution of Jetty with an Apache Fuseki WAR for user's convenience (to make the setup process easier) which allows the adapter to be connected to a SPARQL triplestore for resource persistence and query capabilities.

## Acknowledgement
This work was supported by the [AuFoVer](https://www.vutbr.cz/en/rad/projects/detail/29833) project and the [Arrowhead](https://www.fit.vut.cz/research/project/1299/.en) project.