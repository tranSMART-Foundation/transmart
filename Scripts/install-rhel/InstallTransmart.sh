#!/bin/bash

# Version: Wed Mar  9 21:27:31 EST 2016
# 
#**********************************************************************************
#  Script to load all that is needed to run an example/demo version of tranSMART 16.2 on RHEL 7.2
#**********************************************************************************

# set up with 
#   sudo yum update
#   sudo yum install -y git
#   git clone https://github.com/tranSMART-Foundation/Scripts.git
#   cd Scripts
#   git checkout release-16.2
#   cd ..
#
# to run the install scripts
#   cd $HOME
#   ./Scripts/install-rhel/InstallTransmart.sh
#
# to run the checking scripts
#   Scripts/install-rhel/checks/checkAll.sh

# Script Parameters
TRANSMART_DATA_NAME="transmart-data-release-16.2"
TRANSMART_DATA_ZIP="$TRANSMART_DATA_NAME.zip"
TRANSMART_DATA_URL="http://library.transmartfoundation.org/release/release16_2_0_artifacts/$TRANSMART_DATA_ZIP"
TRANSMART_DATA_SIG_URL="http://library.transmartfoundation.org/release/release16_2_0_artifacts/$TRANSMART_DATA_ZIP.sig"
TRANSMART_DATA_MD5_URL="http://library.transmartfoundation.org/release/release16_2_0_artifacts/$TRANSMART_DATA_ZIP.md5"

TRANSMART_ETL_NAME="tranSMART-ETL-release-16.2"
TRANSMART_ETL_ZIP="$TRANSMART_ETL_NAME.zip"
TRANSMART_ETL_URL="http://library.transmartfoundation.org/release/release16_2_0_artifacts/$TRANSMART_ETL_ZIP"
TRANSMART_ETL_SIG_URL="http://library.transmartfoundation.org/release/release16_2_0_artifacts/$TRANSMART_ETL_ZIP.sig"
TRANSMART_ETL_MD5_URL="http://library.transmartfoundation.org/release/release16_2_0_artifacts/$TRANSMART_ETL_ZIP.md5"

TRANSMART_WAR_NAME="transmart.war"
TRANSMART_WAR_URL="http://library.transmartfoundation.org/release/release16_2_0_artifacts/$TRANSMART_WAR_NAME"
TRANSMART_WAR_SIG_URL="http://library.transmartfoundation.org/release/release16_2_0_artifacts/$TRANSMART_WAR_NAME.sig"
TRANSMART_WAR_MD5_URL="http://library.transmartfoundation.org/release/release16_2_0_artifacts/$TRANSMART_WAR_NAME.md5"

TRANSMART_GWAVA_WAR_NAME="gwava.war"
TRANSMART_GWAVA_WAR_URL="http://library.transmartfoundation.org/release/release16_2_0_artifacts/$TRANSMART_GWAVA_WAR_NAME"
TRANSMART_GWAVA_WAR_SIG_URL="http://library.transmartfoundation.org/release/release16_2_0_artifacts/$TRANSMART_GWAVA_WAR_NAME.sig"
TRANSMART_GWAVA_WAR_MD5_URL="http://library.transmartfoundation.org/release/release16_2_0_artifacts/$TRANSMART_GWAVA_WAR_NAME.md5"

# on error; stop/exit
set -e

# Helper function: use md5 to verify downaload
# This assumes <name> is downloaded file and <name>.md5 is signature file
# on current directory
function verifyWithMd5 {
	filename=$1
	check1=$( gpg --default-key ACC50501 --print-md MD5 $filename )
	check2=$( cut -d* -f1 $filename.md5 )
	echo "MD5 hash from file:    $check1"
	echo "MD5 hash from Library: $check2"
}

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

if [ -z "$INSTALL_BASE" ] ; then INSTALL_BASE="$HOME/transmart" ; fi
export INSTALL_BASE

if ! [ -d "$INSTALL_BASE" ] ; then
	mkdir -p "$INSTALL_BASE"
fi
echo "tranSMART will be installed at this location: $INSTALL_BASE"

if [ -z "$SCRIPTS_BASE" ] ; then SCRIPTS_BASE="$HOME" ; fi

echo "Starting at $(date)"
echo "++++++++++++++++++++++++++++"
echo "+  Checking locations of Script Directory"
echo "++++++++++++++++++++++++++++"
if ! [ -d "$SCRIPTS_BASE/Scripts" ] ; then
	echo "This script assumes that the Scripts directory is installed at $SCRIPTS_BASE/Scripts"
	echo "It does not appear to be there. Please fix that and restart this script."
	echo "Either set \$SCRIPTS_BASE to be the directory of the location of this script"
	echo "which appears to be $HERE; OR, copy the that directory to this location: $SCRIPTS_BASE/Scripts"
	exit 1
else
	echo "Script directory found: $SCRIPTS_BASE/Scripts"
fi
echo "Finished checking locations of Script Directory at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  set up working dir (tranSMART install base) "
echo "++++++++++++++++++++++++++++"

# give user option to suppress sudo timeout (see welcome.sh)
cd $SCRIPTS_BASE/Scripts/install-rhel
source welcome.sh

# set up sudo early
sudo -k
sudo -v

echo "++++++++++++++++++++++++++++"
echo "+  install make, curl, unzip, tar "
echo "++++++++++++++++++++++++++++"
sudo yum update
sudo yum -q install -y make
sudo yum -q install -y curl
sudo yum -q install -y unzip
sudo yum -q install -y tar

echo "++++++++++++++++++++++++++++"
echo "+ install ant and maven"
echo "++++++++++++++++++++++++++++"
sudo yum -q install -y ant
sudo yum -q install -y maven
echo "Finished install of ant and maven at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  set up the transmart-data folder"
echo "++++++++++++++++++++++++++++"

cd $INSTALL_BASE
sudo -v
if ! [ -e $TRANSMART_DATA_ZIP ] ; then
	curl $TRANSMART_DATA_URL -o $TRANSMART_DATA_ZIP
	curl $TRANSMART_DATA_MD5_URL -o $TRANSMART_DATA_ZIP.md5
fi
verifyWithMd5 "$TRANSMART_DATA_ZIP"

if ! [ -e transmart-data ] ; then
	echo "unzipping $TRANSMART_DATA_ZIP"
	unzip $TRANSMART_DATA_ZIP
	mv $TRANSMART_DATA_NAME transmart-data
fi

echo "Finished setting up the transmart-date folder at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  set up the tranSMART-ETL folder"
echo "++++++++++++++++++++++++++++"

cd $INSTALL_BASE/transmart-data/env
sudo -v
if ! [ -e $TRANSMART_ETL_ZIP ] ; then
	curl $TRANSMART_ETL_URL -o $TRANSMART_ETL_ZIP
	curl $TRANSMART_ETL_MD5_URL -o $TRANSMART_ETL_ZIP.md5
fi
verifyWithMd5 "$TRANSMART_ETL_ZIP"

if ! [ -e tranSMART-ETL ] ; then
	echo "unzipping $TRANSMART_ETL_ZIP"
	unzip $TRANSMART_ETL_ZIP
	mv $TRANSMART_ETL_NAME tranSMART-ETL
fi
echo "Finished setting up the tranSMART-ETL folder at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Download and verify war files"
echo "++++++++++++++++++++++++++++"

cd $INSTALL_BASE
sudo -v
if ! [ -d war-files ]; then
	mkdir war-files
fi

cd war-files
if ! [ -e transmart.war ]; then
	curl $TRANSMART_WAR_URL --output transmart.war
	curl $TRANSMART_WAR_MD5_URL --output transmart.war.md5
fi
verifyWithMd5 "transmart.war"

if ! [ -e gwava.war ]; then
	curl $TRANSMART_GWAVA_WAR_URL --output gwava.war
	curl $TRANSMART_GWAVA_WAR_MD5_URL --output gwava.war.md5
fi
verifyWithMd5 "gwava.war"

cd ..
echo "War files as downloaded"
ls -la war-files

echo "Finished downloading and verifying war files at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Install of basic tools and dependencies "
echo "++++++++++++++++++++++++++++"

# sudo make -C env rhel_deps_root
#   In the makefile target, rhel_deps_root, causes a
#   make of these two steps: 
# (1)
sudo -v
cd $INSTALL_BASE/transmart-data
sudo make -C env install_rhel_packages 
# verify these packages
cd $SCRIPTS_BASE/Scripts/install-rhel/checks
./checkMakefilePackages.sh
if [ "$( checkInstallError "Some Basic Command-Line Tool from 'sudo make -C env install_rhel_packages' is missing; redo install" )" ] ; then exit -1; fi
echo "sudo make -C env install_rhel_packages - finished at $(date)"

# (2)
sudo -v
cd $INSTALL_BASE/transmart-data
sudo make -C env /var/lib/postgresql/tablespaces
# verify tablespaces
cd $SCRIPTS_BASE/Scripts/install-rhel/checks
./checkFilesTablespace.sh
if [ "$( checkInstallError "TABLESPACE files (/var/lib/postgresql/tablespaces) not set up properly; redo install" )" ] ; then exit -1; fi
echo "sudo make -C env /var/lib/postgresql/tablespaces - finished at $(date)"
echo " "

echo "Finished setting RHEL dependencies (with root) at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Dependency: Install of tranSMART-ETL"
echo "++++++++++++++++++++++++++++"

# make -C env rhel_deps_regular
#   In the makefile target, rhel_deps_regular, causes a
#   make of these four steps: 
# (1)
sudo -v
cd $INSTALL_BASE/transmart-data
make -C env update_etl
# verify ETL folder
cd $SCRIPTS_BASE/Scripts/install-rhel/checks
./checkFilesETLFolder.sh
if [ "$( checkInstallError "The directory transmart-data/tranSMART-ETL was not installed properly; redo install" )" ] ; then exit -1; fi

echo "make -C env update_etl - finished at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Dependency: Install of data-integration"
echo "++++++++++++++++++++++++++++"

# (2)
sudo -v
cd $INSTALL_BASE/transmart-data
make -C env data-integration 
# verify data-integration folder
cd $SCRIPTS_BASE/Scripts/install-rhel/checks
./checkFilesDataIntegrationFolder.sh
if [ "$( checkInstallError "The directory transmart-data/data-integration was not installed properly; redo install" )" ] ; then exit -1; fi

echo "make -C env data-integration - finished at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Dependency: Install of vars          "
echo "++++++++++++++++++++++++++++"

# (3)
sudo -v
cd $INSTALL_BASE/transmart-data
make -C env ../vars
# verify setup of vars file
cd $SCRIPTS_BASE/Scripts/install-rhel/checks
./checkFilesVars.sh
if [ "$( checkInstallError "vars file (transmart-data/vars) not set up properly; redo install" )" ] ; then exit -1; fi

echo "make -C env ../vars - finished at $(date)"

# (4) -- this last step is replaced by the sdk calls below
# make -C env groovy
echo " "

echo "++++++++++++++++++++++++++++"
echo "+  Dependency: Install of grails, groovy"
echo "++++++++++++++++++++++++++++"

echo "Finished setting RHEL dependencies (without root) at $(date)"

# No longer need to remove this as the make step that creates it is not skipped!
# rm $INSTALL_BASE/transmart-data/env/groovy

curl -s get.sdkman.io | bash
echo "Y" > AnswerYes.txt
source $HOME/.sdkman/bin/sdkman-init.sh
sdk install grails 2.3.11 < AnswerYes.txt
sdk install groovy 2.4.5 < AnswerYes.txt
cd $SCRIPTS_BASE/Scripts/install-rhel/checks
./checkSdkmanApps.sh
if [ "$( checkInstallError "groovy and/or grails not installed correctly; redo install" )" ] ; then exit -1; fi

echo "Finished install of groovy and grails at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Checks on install of tools and dependencies          "
echo "++++++++++++++++++++++++++++"

# fix files for postgres
echo "Patch dir permits for TABLESPACES"
sudo -v
cd $INSTALL_BASE/transmart-data
. ./vars
sudo chmod 700 $TABLESPACES/*

echo "Checks on basic load"
cd $SCRIPTS_BASE/Scripts/install-rhel/checks
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

echo "++++++++++++++++++++++++++++"
echo "+  Install Tomcat 7"
echo "++++++++++++++++++++++++++++"

sudo -v 
cd $HOME
sudo yum -q install -y tomcat7 
sudo service tomcat7 stop
$SCRIPTS_BASE/Scripts/install-rhel/updateTomcatConfig.sh

echo "+  Checks on tomcat install"
cd $SCRIPTS_BASE/Scripts/install-rhel/checks
./checkTomcatInstall.sh
if [ "$( checkInstallError "Tomcat install failed; redo install" )" ] ; then exit -1; fi

echo "Finished installing tomcat at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Install R, Rserve and other packages"
echo "++++++++++++++++++++++++++++"

# could be installed from yum when version 3.1+ becomes available
# as of Dec 23 2015 - current install is 3.0.2 - >=3.1.0 required
# Specifically: https://cran.r-project.org/web/packages/plyr/plyr.pdf

#sudo -v
#sudo yum install -y r-base=3.0.2-1ubuntu1
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

cd $SCRIPTS_BASE/Scripts/install-rhel/checks
./checkFilesR.sh
if [ "$( checkInstallError "R install failed; redo install" )" ] ; then exit -1; fi
./checkR.sh
if [ "$( checkInstallError "R install failed; redo install" )" ] ; then exit -1; fi
echo "Finished installing R and R packages at $(date)"


echo "++++++++++++++++++++++++++++"
echo "+  Set up basic PostgreSQL; supports transmart login"
echo "++++++++++++++++++++++++++++"

# only load database if not already loaded
set +e
cd $SCRIPTS_BASE/Scripts/install-rhel/checks
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
fi
cd $SCRIPTS_BASE/Scripts/install-rhel/checks
./checkPsqlDataLoad.sh
if [ "$( checkInstallError "Loading database failed; clear database and run install again" )" ] ; then exit -1; fi

echo "Finished setting up the PostgreSQL database at $(date)"


echo "++++++++++++++++++++++++++++"
echo "+  Set up configuration files"
echo "++++++++++++++++++++++++++++"

cd $INSTALL_BASE/transmart-data
sudo -v
source ./vars
make -C config install
sudo mkdir -p /usr/share/tomcat7/.grails/transmartConfig/
sudo cp $HOME/.grails/transmartConfig/*.groovy /usr/share/tomcat7/.grails/transmartConfig/
sudo chown -R tomcat7:tomcat7 /usr/share/tomcat7/.grails

cd $SCRIPTS_BASE/Scripts/install-rhel/checks
./checkFilesConfig.sh
if [ "$( checkInstallError "configuration files not set up correctly, see install script and redo" )" ] ; then exit -1; fi

echo "Finished setting up the configuration files at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Install war files"
echo "++++++++++++++++++++++++++++"

cd $INSTALL_BASE/war-files
sudo cp *.war /var/lib/tomcat7/webapps/

cd $SCRIPTS_BASE/Scripts/install-rhel/checks
./checkFilesTomcatWar.sh
if [ "$( checkInstallError "transmart war file not set up correctly, see install script and redo" )" ] ; then exit -1; fi

echo "Finished installing war files at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Load, configure and start SOLR"
echo "++++++++++++++++++++++++++++"

cd $INSTALL_BASE/transmart-data
sudo -v
source ./vars
#  (TODO)  Should check to see if it is already running
make -C solr start > $INSTALL_BASE/transmart-data/solr.log 2>&1 & 
echo "Sleeping - waiting for SOLR to start (2 minutes)"
sleep 2m
make -C solr rwg_full_import sample_full_import
echo "Finished loading, configuring and starting SOLR at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  start Rserve"
echo "++++++++++++++++++++++++++++"

# the (commented out) service command is the correct way to do this
# and unfortunately, this does not work either.
#  (TODO)  Should check to see if it is already running
sudo -v
cd $SCRIPTS_BASE/Scripts/install-rhel
sudo -u tomcat7 bash -c "INSTALL_BASE=\"$INSTALL_BASE\" ./runRServe.sh"
#sudo service rserve start - is not working - not sure why
echo "Finished starting RServe at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  start Tomcat"
echo "++++++++++++++++++++++++++++"

#  (TODO)  Should check to see if it is already running
sudo service tomcat7 restart
echo "Finished starting Tomcat7 at $(date)"
echo "Sleeping - waiting for tomcat/transmart to start (3 minutes)"
sleep 3m

echo "++++++++++++++++++++++++++++"
echo "+ Done with install - making final checks - (may take a while)"
echo "++++++++++++++++++++++++++++"

set e+
cd $SCRIPTS_BASE/Scripts/install-rhel/checks
./checkFilesTomcat.sh
./checkTools.sh
./checkWeb.sh

echo "++++++++++++++++++++++++++++"
echo "+ To redo all checks"
echo "cd $SCRIPTS_BASE/Scripts/install-rhel/checks"
echo "./checkAll.sh"
echo "++++++++++++++++++++++++++++"
echo "+ Done with final checks"
echo "++++++++++++++++++++++++++++"


echo "Finished install of basic transmart system at $(date)"

echo "--------------------------------------------"
echo "To load datasets, use the these two files in the Scripts directory: "
echo "    datasetsList.txt - the list of possible datasets to load, and"
echo "    load_datasets.sh - the script to load the datasets. "
echo ""
echo "First, in the file datasetsList.txt, un-comment the lines that "
echo "corresponding to the data sets you wish to load. "
echo ""
echo "Then run the file load_datasets.sh with:"
echo "    cd $SCRIPTS_BASE"
echo "    ./Scripts/install-rhel/load_datasets.sh"
echo ""
echo "-- Note that loading the same dataset twice is not recommended" 
echo "   and may produce unpredictable results"
echo "--------------------------------------------"

