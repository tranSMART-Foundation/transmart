XSLT TESTS
==========

Sample XML files for testing offline. Everything under the "ds" directory corresponds to
DAS commands on data sources, for example http://host/my-data-source/features?segment=blah

To allow the browser to download images, CSS and JavaScript:
# cd to src/test/webapp
cd ..
# Create soft link to src/main/webapp/xslt
ln -s ../../main/webapp/xslt

Now open a browser and go to for example:
file:///home/me/mydas/example_server_installation/src/test/webapp/xml/dsn-uniprot.xml
file:///home/me/mydas/example_server_installation/src/test/webapp/xml/ds/features-brca1.xml

$Id$