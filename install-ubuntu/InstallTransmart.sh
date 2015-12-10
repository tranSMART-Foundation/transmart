#!/bin/bash

#**********************************************************************************
#  Script to load all that is need to run an example/demo version of tranSMART 1.2.4
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
# to run the checking script
#   Scripts/install-ubuntu/checks/testAll.sh

# set up sudo early
sudo -v

echo "++++++++++++++++++++++++++++"
echo "+  Checking locations of Script Directory
echo "++++++++++++++++++++++++++++"
if ! [ -e $HOME/Scripts ] ; then
	echo "This script assumes that the Scripts directory is installed at $HOME/Script"
	echo "It does not appear to be there. Please fix that and restart this script."
	exit 1
fi

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

echo "++++++++++++++++++++++++++++"
echo "+  Install of basic tools"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
sudo -v
sudo make -C env ubuntu_deps_root
sudo -v
make -C env ubuntu_deps_regular
sudo -v
sudo apt-get install -y ant
sudo apt-get install -y maven
rm $HOME/transmart/transmart-data/env/groovy
curl -s get.sdkman.io | bash
echo "Y" > AnswerYes.txt
source $HOME/.sdkman/bin/sdkman-init.sh
sdk install grails 2.3.11 < AnswerYes.txt
sdk install groovy 2.4.5 < AnswerYes.txt

# check - basics.sh
# check - checkVersions.sh
# check - checkFilesBasic.sh

echo "++++++++++++++++++++++++++++"
echo "+  Install Tomcat 7"
echo "++++++++++++++++++++++++++++"

cd $HOME
sudo -v 
sudo apt-get install -y tomcat7 
sudo service tomcat7 stop
$HOME/Scripts/install-ubuntu/updateTomcatConfig.sh

# check - checkTomcatInstall.sh

echo "++++++++++++++++++++++++++++"
echo "+  Install R, Rserve and other packages"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
sudo -v
source ./vars
make -C R install_packages
cd $HOME
echo "export PATH=${HOME}/transmart/transmart-data/R/root/bin:\$PATH" > Rpath.sh
sudo mv Rpath.sh /etc/profile.d/
source /etc/profile.d/Rpath.sh

# check - checkFilesR.sh
# check - checkR.sh

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
make update_datasets
make -C samples/postgres load_clinical_GSE8581
make -C samples/postgres load_ref_annotation_GSE8581
make -C samples/postgres load_expression_GSE8581

# check - checkPsqlDataload.sh

echo "++++++++++++++++++++++++++++"
echo "+  Set up configuration files"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
sudo -v
source ./vars
make -C config install
sudo mkdir -p /usr/share/tomcat7/.grails/transmartConfig/
sudo cp /home/transmart/.grails/transmartConfig/*.groovy /usr/share/tomcat7/.grails/transmartConfig/

# check - checkFilesConfig.sh

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

echo "++++++++++++++++++++++++++++"
echo "+  Load, configure and start SOLR"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
source ./vars
make -C solr start > ~/transmart/transmart-data/solr.log 2>&1 & 
sleep 60
make -C solr rwg_full_import sample_full_import

echo "++++++++++++++++++++++++++++"
echo "+  start Rserve"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
sudo -v
sudo -u tomcat7 bash -c 'source vars && source /etc/profile.d/Rpath.sh && make -C R start_Rserve' 

echo "++++++++++++++++++++++++++++"
echo "+  start Tomcat"
echo "++++++++++++++++++++++++++++"

sudo service tomcat7 restart

echo "++++++++++++++++++++++++++++"
echo "+  run final checking scripts"
echo "++++++++++++++++++++++++++++"

#check - checkFilesTomcat.sh
#check - checkTools.sh
#check - checkWeb.sh

