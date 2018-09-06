transmartApp
============

tranSMART is a knowledge management platform that enables scientists to develop
and refine research hypotheses by investigating correlations between genetic and
phenotypic data, and assessing their analytical results in the context of
published literature and other work.

Installation
------------

Some pre-requisites are required in order to run tranSMART. For development,
a copy of [grails][1] is needed in order to run the application. For production
or evaluation purposes, it is sufficient to download a pre-built WAR file, for
instance a snapshot from the [i2b2-tranSMART Foundation's][2]
CI/build servers, for snapshots of the i2b2-tranSMART Foundation's GitHub
repository. In order to run the WAR, an application server is
required. The only supported one is [Tomcat][3], from the 7.x
line, though it will most likely work on others.

In addition, an Oracle 12 or PostgreSQL database installed with the
proper schema and data is required. This can be prepared with the
[transmart-data][4] project in the transmart-data directory in this
repository. This project also handles the required configuration,
running the Solr instances and, for development purposes, running an R
server and installing sample data.

For details on how to install the i2b2-tranSMART Foundation's releases, refer to
[their wiki][5].


  [1]: http://grails.org/
  [2]: https://ci.transmartfoundation.org/
  [3]: http://tomcat.apache.org/
  [4]: https://github.com/tranSMART-Foundation/transmart/transmart-data
  [5]: https://wiki.transmartfoundation.org/
