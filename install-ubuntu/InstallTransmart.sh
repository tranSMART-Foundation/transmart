#!/bin/bash

# Version: Fri Dec 11 16:30:10 EST 2015

#**********************************************************************************
#  Script to load all that is needed to run an example/demo version of tranSMART 1.2.4
#**********************************************************************************

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

# set up with 
#   sudo apt-get update
#   sudo apt-get install -y git
#   git clone https://github.com/tranSMART-Foundation/Scripts.git
#
# to run the install scripts
#   cd ~
#   /Scripts/install-ubuntu/InstallTransmart.sh
#
# to run the checking script
#   Scripts/install-ubuntu/checks/checkAll.sh

# set up sudo early
sudo -v

echo "Starting at $(date)"
echo "++++++++++++++++++++++++++++"
echo "+  Checking locations of Script Directory"
echo "++++++++++++++++++++++++++++"
if ! [ -e $HOME/Scripts ] ; then
	echo "This script assumes that the Scripts directory is installed at $HOME/Scripts"
	echo "It does not appear to be there. Please fix that and restart this script."
	exit 1
else
	echo "Script directory found: $HOME/Scripts"
fi
echo "Finished checking locations of Script Directory at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  install make "
echo "++++++++++++++++++++++++++++"
sudo apt-get install -y make

echo "++++++++++++++++++++++++++++"
echo "+  set up working dir (transmart) "
echo "++++++++++++++++++++++++++++"

mkdir $HOME/transmart

echo "++++++++++++++++++++++++++++"
echo "+  set up the transmart-date folder"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart
sudo -v
sudo apt-get install -y curl
sudo apt-get install -y unzip
curl https://codeload.github.com/tranSMART-Foundation/transmart-data/zip/release-1.2.4 -o transmart-data-release-1.2.4.zip
unzip transmart-data-release-1.2.4.zip
mv transmart-data-release-1.2.4 transmart-data

echo "Finished setting up the transmart-date folder at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Install of basic tools"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
sudo -v
sudo make -C env ubuntu_deps_root
echo "Finished setting ubuntu dependencies (with root) at $(date)"
sudo -v
make -C env ubuntu_deps_regular
echo "Finished setting ubuntu dependencies (without root) at $(date)"
sudo -v
sudo apt-get install -y ant
sudo apt-get install -y maven
rm $HOME/transmart/transmart-data/env/groovy
curl -s get.sdkman.io | bash
echo "Y" > AnswerYes.txt
source $HOME/.sdkman/bin/sdkman-init.sh
sdk install grails 2.3.11 < AnswerYes.txt
sdk install groovy 2.4.5 < AnswerYes.txt

echo "Checks on basic load"
cd $HOME/Scripts/install-ubuntu/checks
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
sudo apt-get install -y tomcat7 
sudo service tomcat7 stop
$HOME/Scripts/install-ubuntu/updateTomcatConfig.sh

echo "+  Checks on tomcat install"
cd $HOME/Scripts/install-ubuntu/checks
./checkTomcatInstall.sh
if [ "$( checkInstallError "Tomcat install failed; redo install" )" ] ; then exit -1; fi

echo "Finished installing tomcat at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Install R, Rserve and other packages"
echo "++++++++++++++++++++++++++++"


base="$HOME/transmart/transmart-data"
baseR="$base/R"
filepath="$baseR/root/bin"
if [ -e "$filepath" ]; then
    echo "+  R is already installed"
else
	echo "+  installing R at $filepath"
    sudo -v
    cd $HOME/transmart/transmart-data
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

cd $HOME/Scripts/install-ubuntu/checks
./checkFilesR.sh
if [ "$( checkInstallError "R install failed; redo install" )" ] ; then exit -1; fi
./checkR.sh
if [ "$( checkInstallError "R install failed; redo install" )" ] ; then exit -1; fi
echo "Finished installing R and R packages at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Load study GSE8581 in database"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
source ./vars
make -j4 postgres
echo "Finished setting up the PostgreSQL database at $(date)"
make update_datasets
make -C samples/postgres load_clinical_GSE8581
make -C samples/postgres load_ref_annotation_GSE8581
make -C samples/postgres load_expression_GSE8581

cd $HOME/Scripts/install-ubuntu/checks
./checkPsqlDataLoad.sh
if [ "$( checkInstallError "Loading database failed; clear database and restart install" )" ] ; then exit -1; fi

echo "Finished loading data in the PostgreSQL database at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Set up configuration files"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
sudo -v
source ./vars
make -C config install
sudo mkdir -p /usr/share/tomcat7/.grails/transmartConfig/
sudo cp $HOME/.grails/transmartConfig/*.groovy /usr/share/tomcat7/.grails/transmartConfig/

cd $HOME/Scripts/install-ubuntu/checks
./checkFilesConfig.sh
if [ "$( checkInstallError "configuration files not set up correctly, see install script and redo" )" ] ; then exit -1; fi

echo "Finished setting up the configuration files at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Install war files"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart
sudo -v
mkdir war-files
cd war-files
curl http://75.124.74.64/wars/transmart.V1.2.4.war --output transmart.war
curl http://75.124.74.64/wars/gwava.V1.2.4.war --output gwava.war
sudo cp *.war /var/lib/tomcat7/webapps/

cd $HOME/Scripts/install-ubuntu/checks
./checkFilesTomcatWar.sh
if [ "$( checkInstallError "transmart war file not set up correctly, see install script and redo" )" ] ; then exit -1; fi

echo "Finished installing war files at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Load, configure and start SOLR"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
sudo -v
source ./vars
make -C solr start > $HOME/transmart/transmart-data/solr.log 2>&1 & 
sleep 60
make -C solr rwg_full_import sample_full_import
echo "Finished loading, configuring and starting SOLR at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  start Rserve"
echo "++++++++++++++++++++++++++++"

cd $HOME/Scripts/install-ubuntu
sudo -v
sudo -u tomcat7 bash ./runRServe.sh
echo "Finished starting RServe at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  start Tomcat"
echo "++++++++++++++++++++++++++++"

sudo service tomcat7 restart
echo "Finished starting Tomcat7 at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+ Done with install - making final checks (may take a while)"
echo "++++++++++++++++++++++++++++"

cd $HOME/Scripts/install-ubuntu/checks
./checkFilesTomcat.sh
./checkTools.sh
./checkWeb.sh

echo "++++++++++++++++++++++++++++"
echo "+ To redo all checks"
echo "cd ~/Scripts/install-ubuntu/checks/"
echo "./checkAll.sh"
echo "++++++++++++++++++++++++++++"
echo "+ Done with final checks"
echo "++++++++++++++++++++++++++++"


echo "Finished at $(date)"

