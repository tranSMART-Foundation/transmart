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

echo "+  Checks on basic load"
cd $HOME/Scripts/install-ubuntu/checks
./basics.sh
if [ "$( checkInstallError "Some Basic Command-Line Tool is missing" )" ] ; then exit -1; fi
./checkVersions.sh
if [ "$( checkInstallError "There is a Command-Line with an unsupportable version" )" ] ; then exit -1; fi
./checkFilesBasic.sh
if [ "$( checkInstallError "One of more basic files are missing" )" ] ; then exit -1; fi
echo "Finished installing basic tools at $(date)"
exit 0

echo "++++++++++++++++++++++++++++"
echo "+  Install Tomcat 7"
echo "++++++++++++++++++++++++++++"

cd $HOME
sudo -v 
sudo apt-get install -y tomcat7 
sudo service tomcat7 stop
$HOME/Scripts/install-ubuntu/updateTomcatConfig.sh
# check - checkTomcatInstall.sh
echo "Finished installing tomcat at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Install R, Rserve and other packages"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
sudo -v
source ./vars
make -C R install_packages
sudo -v
cd $HOME
echo "export PATH=${HOME}/transmart/transmart-data/R/root/bin:\$PATH" > Rpath.sh
sudo mv Rpath.sh /etc/profile.d/
source /etc/profile.d/Rpath.sh

# check - checkFilesR.sh
# check - checkR.sh
echo "Finished installing R and R packages at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  Load study GSE8581 in database"
echo "++++++++++++++++++++++++++++"

# check - checkPsqlInstall.sh 
# check - checkFilesPsql.sh
# (should probably move these to earlier in the script)
# move to just after checkVersions.sh ?

cd $HOME/transmart/transmart-data
sudo -v
source ./vars
make -j4 postgres
echo "Finished setting up the PostgreSQL database at $(date)"
make update_datasets
sudo -v
make -C samples/postgres load_clinical_GSE8581
make -C samples/postgres load_ref_annotation_GSE8581
make -C samples/postgres load_expression_GSE8581

# check - checkPsqlDataload.sh

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

# check - checkFilesConfig.sh
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

#check - checkFilesToncatWar.sh
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

cd $HOME/transmart/transmart-data
sudo -v
sudo -u tomcat7 bash -c 'source vars && source /etc/profile.d/Rpath.sh && make -C R start_Rserve' 
echo "Finished starting RServe at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+  start Tomcat"
echo "++++++++++++++++++++++++++++"

sudo service tomcat7 restart
echo "Finished starting Tomcat7 at $(date)"

echo "++++++++++++++++++++++++++++"
echo "+ Done"
echo "++++++++++++++++++++++++++++"

#check - checkFilesTomcat.sh
#check - checkTools.sh
#check - checkWeb.sh

echo "Finished at $(date)"

