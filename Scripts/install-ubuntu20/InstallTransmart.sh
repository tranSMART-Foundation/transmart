#!/bin/bash

# Version: Mon Nov  13 05:30:00 EST 2017
# 
#************************************************************************************
#  Script to load all that is needed to run an example/demo
#  version of tranSMART 19.0
#************************************************************************************

# set up with 
#   sudo apt-get update
#   sudo apt-get install -y git
#   git clone https://github.com/tranSMART-Foundation/transmart.git
#
# to run the install scripts
#   cd $HOME
#   cd transmart
#   ./Scripts/install-ubuntu20/InstallTransmart.sh
#
# to run the checking scripts
#   ./Scripts/install-ubuntu20/checks/checkAll.sh

# on error; stop/exit
set -e

# Helper function: check and quit on error
function checkInstallError {
	returnValue=$?
	errorMessage=$1
	if (( $returnValue )); then
		echo "************"
		echo "** $errorMessage"
		echo "************"
	fi
	return $returnValue
}

if [ -z "$SCRIPTS_BASE" ] ; then SCRIPTS_BASE="$HOME/transmart" ; fi

echo "Starting at $(date)"
echo "+++++++++++++++++++++++++++++++++++++++++"
echo "+  Checking locations of Script Directory"
echo "+++++++++++++++++++++++++++++++++++++++++"
if ! [ -d "$SCRIPTS_BASE/Scripts" ] ; then
	echo "This script assumes that the Scripts directory is installed at $SCRIPTS_BASE/Scripts"
	echo "It does not appear to be there. Please fix that and restart this script."
	echo "  cd $SCRIPTS_BASE"
	echo "  sudo apt-get update"
	echo "  sudo apt-get install -y git"
	echo "  git clone https://github.com/tranSMART-Foundation/transmart.git"
	echo "  git checkout -b release-19.0"
	exit 1
else
	echo "Script directory found: $SCRIPTS_BASE/Scripts"
fi
echo "Finished checking locations of Script Directory at $(date)"

echo "++++++++++++++++++++++++++++++++++++++++++++++"
echo "+  set up working dir (tranSMART install base)"
echo "++++++++++++++++++++++++++++++++++++++++++++++"

if [ -z "$INSTALL_BASE" ] ; then INSTALL_BASE="$HOME/transmart" ; fi
export INSTALL_BASE

if ! [ -d "$INSTALL_BASE" ] ; then
	mkdir -p "$INSTALL_BASE"
fi
echo "tranSMART will be installed at this location: $INSTALL_BASE"

# give user option to suppress sudo timeout (see welcome.sh)
cd $SCRIPTS_BASE/Scripts/install-ubuntu20
source welcome.sh

# set up sudo early
sudo -k
sudo -v

echo "+++++++++++++++"
echo "+  install make"
echo "+++++++++++++++"
# gmake on ubuntu20 is simply called 'make'
sudo apt-get -q install -y make

cd $INSTALL_BASE
sudo -v
sudo apt-get -q install -y curl
sudo apt-get -q install -y unzip

echo "+++++++++++++++++++++++++++++"
echo " checking java 8 is installed"
echo "+++++++++++++++++++++++++++++"

set +e
cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./checkJava.sh
if [ "$( checkInstallError "java not installed correctly; install" )" ] ; then
    sudo apt-get -q install -y openjdk-8-jdk openjdk-8-jre
fi
set -e

echo "+++++++++++++++++++++++++++++++++++"
echo "+  set up the transmart-data folder"
echo "+++++++++++++++++++++++++++++++++++"

cd $INSTALL_BASE/
if ! [ -e transmart-data-release-19.0.zip ] ; then
    curl http://library.transmartfoundation.org/release/release19_0_0_artifacts/transmart-data-release-19.0.zip --output transmart-data-release-19.0.zip
fi
if ! [ -e transmart-data ] ; then
	unzip -q transmart-data-release-19.0.zip
	mv transmart-data-release-19.0 transmart-data
fi

echo "Finished setting up the transmart-data folder at $(date)"

echo "++++++++++++++++++++++++++++++++++++++++++"
echo "+  Install of basic tools and dependencies"
echo "++++++++++++++++++++++++++++++++++++++++++"

# sudo make -C env ubuntu_deps_root
#   In the makefile target, ubuntu_deps_root, causes a
#   make of these two steps: 
# (1)
sudo -v
cd $INSTALL_BASE/transmart-data
sudo make -C env install_ubuntu_packages20 
sudo make -C env install_ubuntu_packages
# verify these packages
cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./checkMakefilePackages.sh
if [ "$( checkInstallError "Some Basic Command-Line Tool from 'sudo make -C env install_ubuntu_packages' is missing; redo install" )" ] ; then exit -1; fi
echo "sudo make -C env install_ubuntu_packages - finished at $(date)"

# (2)
sudo -v
cd $INSTALL_BASE/transmart-data
sudo make -C env /var/lib/postgresql/tablespaces
# verify tablespaces
cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./checkFilesTablespace.sh
if [ "$( checkInstallError "TABLESPACE files (/var/lib/postgresql/tablespaces) not set up properly; redo install" )" ] ; then exit -1; fi
echo "sudo make -C env /var/lib/postgresql/tablespaces - finished at $(date)"
echo " "

echo "Finished setting ubuntu dependencies (with root) at $(date)"

echo "+++++++++++++++++++++++++++++++++++++++"
echo "+  Dependency: Install of transmart-etl"
echo "+++++++++++++++++++++++++++++++++++++++"

# make -C env ubuntu_deps_regular
#   In the makefile target, ubuntu_deps_regular, causes a
#   make of these four steps: 
# (1)
sudo -v
cd $INSTALL_BASE/transmart-data

if ! [ -d "$INSTALL_BASE/transmart-data/env/transmart-etl" ] ; then
    make -C env update_etl_git
fi
     
# verify ETL folder
cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./checkFilesETLFolder.sh
if [ "$( checkInstallError "The directory transmart-data/env/transmart-etl was not installed properly; redo install" )" ] ; then exit -1; fi

echo "make -C env update_etl - finished at $(date)"

echo "++++++++++++++++++++++++++++++++++++++++++"
echo "+  Dependency: Install of data-integration"
echo "++++++++++++++++++++++++++++++++++++++++++"

# (2)
sudo -v
cd $INSTALL_BASE/transmart-data
make -C env data-integration 
# verify data-integration folder
cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./checkFilesDataIntegrationFolder.sh
if [ "$( checkInstallError "The directory transmart-data/env/data-integration was not installed properly; redo install" )" ] ; then exit -1; fi

echo "make -C env data-integration - finished at $(date)"

echo "++++++++++++++++++++++++++++++"
echo "+  Dependency: Install of vars"
echo "++++++++++++++++++++++++++++++"

# (3)
sudo -v
cd $INSTALL_BASE/transmart-data
make -C env ../vars
# verify setup of vars file
cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./checkFilesVars.sh
if [ "$( checkInstallError "vars file (transmart-data/vars) not set up properly; redo install" )" ] ; then exit -1; fi

cd $INSTALL_BASE/transmart-data
# source the vars file to set the path for groovy below
. ./vars

echo "make -C env ../vars - finished at $(date)"

echo "++++++++++++++++++++++++++++++++++++++++++++"
echo "+  Dependency: Install of ant, maven, groovy"
echo "++++++++++++++++++++++++++++++++++++++++++++"

echo "Finished setting ubuntu dependencies (without root) at $(date)"
sudo -v
sudo apt-get -q install -y ant
sudo apt-get -q install -y maven
echo "Finished install of ant and maven at $(date)"

make -C env groovy
echo "make -C env groovy - finished at $(date)"

cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./checkGroovy.sh
if [ "$( checkInstallError "groovy not installed correctly; redo install" )" ] ; then exit -1; fi

echo "Finished install of groovy at $(date)"

echo "++++++++++++++++++++++++++++++++++++++++++++++"
echo "+  Checks on install of tools and dependencies"
echo "++++++++++++++++++++++++++++++++++++++++++++++"

# fix files for postgres
echo "Patch dir permits for TABLESPACES"
sudo -v
cd $INSTALL_BASE/transmart-data
sudo chmod 700 $TABLESPACES/*

echo "Checks on basic load"
cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./basics.sh
if [ "$( checkInstallError "Some Basic Command-Line Tool is missing; redo install" )" ] ; then exit -1; fi
./checkVersions.sh
if [ "$( checkInstallError "There is a Command-Line with an unsupportable version; redo install" )" ] ; then exit -1; fi
./checkFilesBasic.sh
if [ "$( checkInstallError "One of more basic files are missing; redo install" )" ] ; then exit -1; fi

echo "Check on loading and setup of postgres"
./checkPsqlInstall.sh 
if [ "$( checkInstallError "PostgreSQL is not installed; redo install" )" ] ; then exit -1; fi
./checkFilesPsql.sh
if [ "$( checkInstallError "Database table folders needs by transmart not correct; fix as indicated; then redo install" )" ] ; then exit -1; fi

echo "Finished installing basic tools and dependencies at $(date)"

echo "+++++++++++++++++++"
echo "+  Install Tomcat 9"
echo "+++++++++++++++++++"

sudo -v 
cd $HOME
sudo apt-get -q install -y tomcat9 
sudo service tomcat9 stop

# while tomcat9 is stopped:
# For tomcat9 on Ubuntu20 we need to define the tomcat user directory
# which defaults to root!

sudo usermod tomcat --home /home/tomcat
sudo mkdir -p /home/tomcat
sudo chown -R tomcat:tomcat /home/tomcat

$SCRIPTS_BASE/Scripts/install-ubuntu20/updateTomcatConfig.sh

# Fix tomcat user:
# for Ubuntu20 and tomcat9 the user has no hom edirectory

echo "+  Checks on tomcat install"
cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./checkTomcatInstall.sh
if [ "$( checkInstallError "Tomcat install failed; redo install" )" ] ; then exit -1; fi

echo "Finished installing tomcat at $(date)"

echo "+++++++++++++++++++++++++++++++++++++++"
echo "+  Install R, Rserve and other packages"
echo "+++++++++++++++++++++++++++++++++++++++"

base="$INSTALL_BASE/transmart-data"
baseR="$base/R"
filepath="$baseR/root/bin"
if [ -e "$filepath" ]; then
    echo "+  R is already installed"
else
	echo "+  installing R at $filepath"
    sudo -v
    cd $INSTALL_BASE/transmart-data
    source ./vars
    make -C R install_packages
fi
sudo -v
cd $HOME
if ! [ -e /etc/profile.d/Rpath.sh ] ; then
    echo "export PATH=${HOME}/transmart/transmart-data/R/root/bin:\$PATH" > Rpath.sh
    sudo mv Rpath.sh /etc/profile.d/
fi
source /etc/profile.d/Rpath.sh

sudo -v
cd $INSTALL_BASE/transmart-data
source ./vars
sudo TABLESPACES=$TABLESPACES RSERVE_USER="tomcat" make -C R install_rserve_unit

cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./checkFilesR.sh
if [ "$( checkInstallError "R install failed; redo install" )" ] ; then exit -1; fi
./checkR.sh
if [ "$( checkInstallError "R install failed; redo install" )" ] ; then exit -1; fi
echo "Finished installing R and R packages at $(date)"


echo "++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "+  Set up basic PostgreSQL; supports transmart login"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++"

# only load database if not already loaded
set +e
cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
sudo -u postgres psql -c "drop role if exists transmartadmin"
sudo -u postgres psql -c "create role transmartadmin superuser createdb createrole login password 'transmart'"
./checkPsqlDataLoad.sh quiet
returnCode=$?
set -e

if [ $returnCode -eq 0 ] ; then
	echo "Database is already loaded"
else
	echo "Setting up PostgreSQL database"
	cd $INSTALL_BASE/transmart-data
	source ./vars
	make -j4 postgres
	make update_datasets
fi
cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./checkPsqlDataLoad.sh
if [ "$( checkInstallError "Loading database failed; clear database and run install again" )" ] ; then exit -1; fi

echo "Finished setting up the PostgreSQL database at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Set up solr service files"
echo "++++++++++++++++++++++++++++"


sudo -v
cd $INSTALL_BASE/transmart-data
source ./vars

# install solr
make -C solr solr
# create solr core targets
make -C solr solr_home

# create solr service
sudo TABLESPACES=$TABLESPACES SOLR_USER="$USER" make -C solr install_solr_unit

cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./checkFilesSolr.sh
if [ "$( checkInstallError "solR install failed; redo install" )" ] ; then exit -1; fi

./checkR.sh
if [ "$( checkInstallError "solR install failed; redo install" )" ] ; then exit -1; fi
echo "Finished installing solr and solr service at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Set up configuration files"
echo "++++++++++++++++++++++++++++"

cd $INSTALL_BASE/transmart-data
sudo -v
source ./vars
make -C config install
sudo mkdir -p /home/tomcat/.grails/transmartConfig/
sudo cp $HOME/.grails/transmartConfig/*.groovy /home/tomcat/.grails/transmartConfig/
sudo chown -R tomcat:tomcat /home/tomcat/.grails

cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
./checkFilesConfig.sh
if [ "$( checkInstallError "configuration files not set up correctly, see install script and redo" )" ] ; then exit -1; fi

echo "Finished setting up the configuration files at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Install war files"
echo "++++++++++++++++++++++++++++"

cd $INSTALL_BASE
sudo -v
if ! [ -d war-files ]; then
	mkdir war-files
fi

cd war-files
if ! [ -e transmart.war ]; then
    curl http://library.transmartfoundation.org/release/release19_0_0_artifacts/transmartApp-release-19.0.war --output transmart.war
fi
if ! [ -e gwava.war ]; then
    curl http://library.transmartfoundation.org/release/release19_0_0_artifacts/gwava-release-19.0.war --output gwava.war
fi
sudo cp *.war /var/lib/tomcat9/webapps/
cd ..

rm -rf transmart-manual-release-19.0
curl http://library.transmartfoundation.org/release/release19_0_0_artifacts/transmart-manual-release-19.0.zip --output transmart-manual-release-19.0.zip
unzip -q transmart-manual-release-19.0.zip
sudo rm -rf /var/lib/tomcat9/webapps/transmartmanual
sudo mv transmart-manual-release-19.0 /var/lib/tomcat9/webapps/transmartmanual
sudo chown -R tomcat.tomcat /var/lib/tomcat9/webapps/transmartmanual

cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
sudo ./checkFilesTomcatWar.sh
if [ "$( checkInstallError "transmart war file not set up correctly, see install script and redo" )" ] ; then exit -1; fi

echo "Finished installing war files at $(date)"

echo "+++++++++++++++++++++++++++++++++"
echo "+  Load, configure and start SOLR"
echo "+++++++++++++++++++++++++++++++++"

cd $INSTALL_BASE/transmart-data
sudo -v
source ./vars

# load the new services for solr and rserve
sudo systemctl daemon-reload

sudo systemctl enable solr
sudo systemctl start solr
echo "Sleeping - waiting for SOLR to start (2 minutes)"
sleep 2m
make -C solr rwg_full_import sample_full_import browse_full_import
echo "Finished loading, configuring and starting SOLR at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  start Rserve"
echo "++++++++++++++++++++++++++++"

sudo -v
cd $SCRIPTS_BASE/Scripts/install-ubuntu20
# rserve runs as user tomcat9
# service started using /etc/systemd/system/rserve.service
# and writes to /var/log/tomcat9/rserve-transmart.log
sudo systemctl enable rserve
sudo systemctl start rserve
echo "Finished starting RServe at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  start Tomcat"
echo "++++++++++++++++++++++++++++"

#  (TODO)  Should check to see if it is already running
sudo systemctl restart tomcat9
echo "Finished starting Tomcat9 at $(date)"
echo "Sleeping - waiting for tomcat/transmart to start (3 minutes)"
sleep 3m

echo "++++++++++++++++++++++++++++"
echo "+ Done with install - making final checks - (may take a while)"
echo "++++++++++++++++++++++++++++"

set e+
cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks
sudo ./checkFilesTomcat.sh
./checkTools.sh
./checkWeb.sh

echo "++++++++++++++++++++++++++++"
echo "+ To redo all checks"
echo "cd $SCRIPTS_BASE/Scripts/install-ubuntu20/checks"
echo "./checkAll.sh"
echo "++++++++++++++++++++++++++++"
echo "+ Done with final checks"
echo "++++++++++++++++++++++++++++"


echo "Finished install of basic transmart system at $(date)"

echo "-----------------------------------------------------------------"
echo "To load datasets, use the these two files in"
echo "the Scripts/install-ubuntu20  directory: "
echo "    datasetsList.txt - the list of possible datasets to load, and"
echo "    load_datasets.sh - the script to load the datasets. "
echo ""
echo "First, in the file datasetsList.txt, un-comment the lines that "
echo "corresponding to the data sets you wish to load. "
echo ""
echo "Then run the file load_datasets.sh with:"
echo "    cd $SCRIPTS_BASE"
echo "    ./Scripts/install-ubuntu20/load_datasets.sh"
echo ""
echo "-- Note that loading the same dataset twice is not recommended" 
echo "   and may produce unpredictable results"
echo "-----------------------------------------------------------------"

