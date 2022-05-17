#!/bin/bash

#************************************************************************************
#  Script to load solR server
#  for tranSMART release 19.1
#************************************************************************************

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallSolr starting"

echo "++++++++++++++++++++++++++++++++"
echo "+  NN. Set up solr service files"
echo "++++++++++++++++++++++++++++++++"

sudo -v
cd $TMINSTALL_BASE/transmart-data
source ./vars

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Install solr"

make -C solr solr

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Create solr core targets"

make -C solr solr_home

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Create solr service"

sudo TABLESPACES=$TABLESPACES SOLR_USER="$USER" make -C solr install_solr_unit

cd $TMSCRIPTS_BASE/checks
./checkFilesSolr.sh
if [ "$( checkInstallError "solR install failed; redo install" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Finished installing solr and solr service"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Starting solr service"

echo "+++++++++++++++++++++++++++++++++"
echo "+  Load, configure and start SOLR"
echo "+++++++++++++++++++++++++++++++++"

cd $TMINSTALL_BASE/transmart-data
sudo -v
source ./vars

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Load the new service for solr"

sudo systemctl daemon-reload

sudo systemctl enable solr
sudo systemctl start solr

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Sleeping - waiting for SOLR to start (2 minutes)"

sleep 2m

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} import data into solr"

make -C solr rwg_full_import sample_full_import browse_full_import

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallSolr done. Finished loading, configuring and starting SOLR"



