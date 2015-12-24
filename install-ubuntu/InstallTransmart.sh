#!/bin/bash

# Version: Thu Dec 24 11:06:04 EST 2015
# 
#**********************************************************************************
#  Script to load all that is needed to run an example/demo version of tranSMART 1.2.4
#**********************************************************************************

# set up with 
#   sudo apt-get update
#   sudo apt-get install -y git
#   git clone https://github.com/tranSMART-Foundation/Scripts.git
#
# to run the install scripts
#   cd ~
#   /Scripts/install-ubuntu/InstallTransmart.sh
#
# to run the checking scripts
#   Scripts/install-ubuntu/checks/checkAll.sh

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

if [ -z "$SCRIPTS_BASE" ] ; then SCRIPTS_BASE="$HOME" ; fi

echo "Starting at $(date)"
echo "++++++++++++++++++++++++++++"
echo "+  Checking locations of Script Directory"
echo "++++++++++++++++++++++++++++"
if ! [ -d "$SCRIPTS_BASE/Scripts" ] ; then
	echo "This script assumes that the Scripts directory is installed at $SCRIPTS_BASE/Scripts"
	echo "It does not appear to be there. Please fix that and restart this script."
	echo "  cd $SCRIPTS_BASE"
	echo "  sudo apt-get update"
	echo "  sudo apt-get install -y git"
	echo "  git clone https://github.com/tranSMART-Foundation/Scripts.git"
	exit 1
else
	echo "Script directory found: $SCRIPTS_BASE/Scripts"
fi
echo "Finished checking locations of Script Directory at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  set up working dir (tranSMART install base) "
echo "++++++++++++++++++++++++++++"

if [ -z "$INSTALL_BASE" ] ; then INSTALL_BASE="$HOME/transmart" ; fi
export INSTALL_BASE

if ! [ -d "$INSTALL_BASE" ] ; then
	mkdir -p "$INSTALL_BASE"
fi
echo "tranSMART will be installed at this location: $INSTALL_BASE"

# give user option to suppress sudo timeout (see welcome.sh)
cd $SCRIPTS_BASE/Scripts/install-ubuntu
source welcome.sh

# set up sudo early
sudo -k
sudo -v

echo "++++++++++++++++++++++++++++"
echo "+  install make "
echo "++++++++++++++++++++++++++++"
sudo apt-get -q install -y make

echo "++++++++++++++++++++++++++++"
echo "+  set up the transmart-data folder"
echo "++++++++++++++++++++++++++++"

cd $INSTALL_BASE
sudo -v
sudo apt-get -q install -y curl
sudo apt-get -q install -y unzip
if ! [ -e transmart-data-release-1.2.4.zip ] ; then
	curl https://codeload.github.com/tranSMART-Foundation/transmart-data/zip/release-1.2.4 -o transmart-data-release-1.2.4.zip
fi
if ! [ -e transmart-data ] ; then
	unzip transmart-data-release-1.2.4.zip
	mv transmart-data-release-1.2.4 transmart-data
fi

echo "Finished setting up the transmart-date folder at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Install of basic tools"
echo "++++++++++++++++++++++++++++"

# sudo make -C env ubuntu_deps_root
#   In the makefile target, ubuntu_deps_root, causes a
#   make of these two steps: 
# (1)
sudo -v
cd $INSTALL_BASE/transmart-data
sudo make -C env install_ubuntu_packages 
# verify these packages
cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
./checkMakefilePackages.sh
if [ "$( checkInstallError "Some Basic Command-Line Tool from 'sudo make -C env install_ubuntu_packages' is missing; redo install" )" ] ; then exit -1; fi
echo "sudo make -C env install_ubuntu_packages - finished at $(date)"

# (2)
sudo -v
cd $INSTALL_BASE/transmart-data
sudo make -C env /var/lib/postgresql/tablespaces
# verify tablespaces
cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
./checkFilesTablespace.sh
if [ "$( checkInstallError "TABLESPACE files (/var/lib/postgresql/tablespaces) not set up properly; redo install" )" ] ; then exit -1; fi
echo "sudo make -C env /var/lib/postgresql/tablespaces - finished at $(date)"
echo " "

echo "Finished setting ubuntu dependencies (with root) at $(date)"

# make -C env ubuntu_deps_regular
#   In the makefile target, ubuntu_deps_regular, causes a
#   make of these four steps: 
# (1)
sudo -v
cd $INSTALL_BASE/transmart-data
make -C env update_etl
# verify ETL folder
cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
./checkFilesETLFolder.sh
if [ "$( checkInstallError "The directory transmart-data/tranSMART-ETL was not installed properly; redo install" )" ] ; then exit -1; fi
echo "make -C env update_etl - finished at $(date)"

# (2)
sudo -v
cd $INSTALL_BASE/transmart-data
make -C env data-integration 
# verify data-integration folder
cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
./checkFilesDataIntegrationFolder.sh
if [ "$( checkInstallError "The directory transmart-data/data-integration was not installed properly; redo install" )" ] ; then exit -1; fi
echo "make -C env data-integration - finished at $(date)"

# (3)
sudo -v
cd $INSTALL_BASE/transmart-data
make -C env ../vars
# verify setup of vars file
cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
./checkFilesVars.sh
if [ "$( checkInstallError "vars file (transmart-data/vars) not set up properly; redo install" )" ] ; then exit -1; fi
echo "make -C env ../vars - finished at $(date)"

# (4) -- this last step is replaced by the sdk calls below
# make -C env groovy
echo " "

echo "Finished setting ubuntu dependencies (without root) at $(date)"
sudo -v
sudo apt-get -q install -y ant
sudo apt-get -q install -y maven
echo "Finished install of ant and maven at $(date)"

# No longer need to remove this as the make step that creates it is not skipped!
# rm $INSTALL_BASE/transmart-data/env/groovy

curl -s get.sdkman.io | bash
echo "Y" > AnswerYes.txt
source $HOME/.sdkman/bin/sdkman-init.sh
sdk install grails 2.3.11 < AnswerYes.txt
sdk install groovy 2.4.5 < AnswerYes.txt
cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
./checkSdkmanApps.sh
if [ "$( checkInstallError "groovy and/or grails not installed correctly; redo install" )" ] ; then exit -1; fi
echo "Finished install of groovy and grails at $(date)"

# fix files for postgres
echo "Patch dir permits for TABLESPACES"
sudo -v
cd $INSTALL_BASE/transmart-data
. ./vars
sudo chmod 700 $TABLESPACES/*

echo "Checks on basic load"
cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
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

echo "Finished installing basic tools at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Install Tomcat 7"
echo "++++++++++++++++++++++++++++"

sudo -v 
cd $HOME
sudo apt-get -q install -y tomcat7 
sudo service tomcat7 stop
$SCRIPTS_BASE/Scripts/install-ubuntu/updateTomcatConfig.sh

echo "+  Checks on tomcat install"
cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
./checkTomcatInstall.sh
if [ "$( checkInstallError "Tomcat install failed; redo install" )" ] ; then exit -1; fi

echo "Finished installing tomcat at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Install R, Rserve and other packages"
echo "++++++++++++++++++++++++++++"

# could be install from apt-get when version 3.1+ becomes available
# as of Dec 23 2015 - current install is 3.0.2 - >=3.1.0 required
# Specifically: https://cran.r-project.org/web/packages/plyr/plyr.pdf

#sudo -v
#sudo apt-get install -y r-base=3.0.2-1ubuntu1
#cd $INSTALL_BASE/transmart-data/R
#R_MIRROR="http://cran.utstat.utoronto.ca/"
#R_EXEC=$(which R)
#sudo CRAN_MIRROR=$(R_MIRROR) $(R_EXEC) -f cran_pkg.R
#sudo CRAN_MIRROR=$(R_MIRROR) $(R_EXEC) -f other_pkg.R
# also set up install_rserve_init (see below)

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
sudo TABLESPACES=$TABLESPACES TRANSMART_USER="tomcat7" make -C R install_rserve_init

cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
./checkFilesR.sh
if [ "$( checkInstallError "R install failed; redo install" )" ] ; then exit -1; fi
./checkR.sh
if [ "$( checkInstallError "R install failed; redo install" )" ] ; then exit -1; fi
echo "Finished installing R and R packages at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Load study GSE8581 in database"
echo "++++++++++++++++++++++++++++"

# only load database if not already loaded
cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
./checkPsqlDataLoad.sh quiet
returnCode=$?

if [ $returnCode -eq 0 ] ; then
	echo "Database is already loaded"
else
	echo "Setting up PostgreSQL database"
	cd $INSTALL_BASE/transmart-data
	source ./vars
	make -j4 postgres
	echo "Finished setting up the PostgreSQL database at $(date)"
	echo "Loading sample dataset GSE8581"
	make update_datasets
	make -C samples/postgres load_clinical_GSE8581
	make -C samples/postgres load_ref_annotation_GSE8581
	make -C samples/postgres load_expression_GSE8581
fi
cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
./checkPsqlDataLoad.sh
if [ "$( checkInstallError "Loading database failed; clear database and run install again" )" ] ; then exit -1; fi

echo "Finished loading data in the PostgreSQL database at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Set up configuration files"
echo "++++++++++++++++++++++++++++"

cd $INSTALL_BASE/transmart-data
sudo -v
source ./vars
make -C config install
sudo mkdir -p /usr/share/tomcat7/.grails/transmartConfig/
sudo cp $HOME/.grails/transmartConfig/*.groovy /usr/share/tomcat7/.grails/transmartConfig/

cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
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
if

cd war-files
if ! [ -e transmart.war]; then
	curl http://75.124.74.64/wars/transmart.V1.2.4.war --output transmart.war
fi
if ! [ -e gwava.war]; then
	curl http://75.124.74.64/wars/gwava.V1.2.4.war --output gwava.war
fi
sudo cp *.war /var/lib/tomcat7/webapps/

cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
./checkFilesTomcatWar.sh
if [ "$( checkInstallError "transmart war file not set up correctly, see install script and redo" )" ] ; then exit -1; fi

echo "Finished installing war files at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Load, configure and start SOLR"
echo "++++++++++++++++++++++++++++"

cd $INSTALL_BASE/transmart-data
sudo -v
source ./vars
make -C solr start > $INSTALL_BASE/transmart-data/solr.log 2>&1 & 
sleep 60
make -C solr rwg_full_import sample_full_import
echo "Finished loading, configuring and starting SOLR at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  start Rserve"
echo "++++++++++++++++++++++++++++"

sudo -v
cd $SCRIPTS_BASE/Scripts/install-ubuntu
sudo -u tomcat7 bash ./runRServe.sh
#sudo service rserve start - is not working - not sure why
echo "Finished starting RServe at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  start Tomcat"
echo "++++++++++++++++++++++++++++"

sudo service tomcat7 restart
echo "Finished starting Tomcat7 at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+ Done with install - making final checks (may take a while)"
echo "++++++++++++++++++++++++++++"

cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks
./checkFilesTomcat.sh
./checkTools.sh
./checkWeb.sh

echo "++++++++++++++++++++++++++++"
echo "+ To redo all checks"
echo "cd $SCRIPTS_BASE/Scripts/install-ubuntu/checks"
echo "./checkAll.sh"
echo "++++++++++++++++++++++++++++"
echo "+ Done with final checks"
echo "++++++++++++++++++++++++++++"


echo "Finished at $(date)"

