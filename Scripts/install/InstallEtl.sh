#!/bin/bash

#******************************
#  Script to load ETL utilities
#  for tranSMART release 19.1
#******************************

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallEtl starting"

echo "+++++++++++++++++++++++++++++++++++++++++++"
echo "+  NN. Dependency: Install of transmart-etl"
echo "+++++++++++++++++++++++++++++++++++++++++++"

# make -C env ubuntu_deps_regular
#   In the makefile target, ubuntu_deps_regular, causes a
#   make of these four steps: 
# (1)
sudo -v
cd $TMINSTALL_BASE/transmart-data

if ! [ -d "$TMINSTALL_BASE/transmart-data/env/transmart-etl" ] ; then
    if [ "$TMSOURCE" == "beta" ]; then
	export TRANSMART_LIBRARY="http://library.transmartfoundation.org/beta"
	export TRANSMART_RELEASE_DIR="beta19_1_0"
    fi
    make -C env update_etl_git
fi
     
# verify ETL folder
cd $TMSCRIPTS_BASE/checks
./checkFilesETLFolder.sh
if [ "$( checkInstallError "The directory transmart-data/env/transmart-etl was not installed properly; redo install" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "make -C env update_etl - finished at ${now}"

echo "++++++++++++++++++++++++++++++++++++++++++++++"
echo "+  NN. Dependency: Install of data-integration"
echo "++++++++++++++++++++++++++++++++++++++++++++++"

# (2)
sudo -v
cd $TMINSTALL_BASE/transmart-data
make -C env data-integration 
# verify data-integration folder
cd $TMSCRIPTS_BASE/checks
./checkFilesDataIntegrationFolder.sh
if [ "$( checkInstallError "The directory transmart-data/env/data-integration was not installed properly; redo install" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "make -C env data-integration - finished at ${now}"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallEtl finished"
