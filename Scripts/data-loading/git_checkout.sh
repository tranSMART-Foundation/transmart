#!/bin/sh

#todo check database path. Surely ~transmart/transmart?
# copying a vars file from somewhere that surely does not exist yet

TRANSMART_RELEASE="release-16.2"
#TRANSMART_DATABASE="oracle"
TRANSMART_DATABASE="postgres"

cd /data/ETL

mkdir -p release

cd release

git clone https://github.com/tranSMART-Foundation/tranSMART-ETL.git
cd tranSMART-ETL/
git checkout $TRANSMART_RELEASE

# build the loader target
# needs a check on the ojdbc library for Oracle
mvn package

cd ..

git clone https://github.com/tranSMART-Foundation/transmart-data.git
cd transmart-data/
git checkout $TRANSMART_RELEASE

# set up the vars file
# using predefined file with standard settings and using these directories
cp /data/ETL/vars.$TRANSMART_DATABASE ./vars

. ./vars

# on Oracle:
# check DataSource.groovy especially for Oracle
# we do not want to reset another server

echo '        url = "jdbc:oracle:thin:@'$ORAHOST':'$ORAPORT':'$ORASID'"' > oraset.tmp
grep jdbc:oracle:thin:  ~tomcat7/.grails/transmartConfig/DataSource.groovy > oraconfig.tmp
diff -b oraset.tmp oraconfig.tmp > oradiff.tmp
cat oradiff.tmp
rm oraset.tmp oraconfig.tmp oradiff.tmp

# install groovy if not already available
# TODO need a test for groovy
make -C env groovy

# install Kettle (Pentaho data integration version 4.4)

make -C env data-integration

# consider installing other software
# tMDataloader for ETL - makes changes to database schemas

