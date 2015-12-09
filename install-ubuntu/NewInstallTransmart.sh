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
#   cd ~/Scripts/install-ubuntu
#   ./NewInstallTransmart.sh
#
# to run the checking script
#   cd Scripts
#   cd install-ubuntu/checks
#   ./testAll.sh

echo "++++++++++++++++++++++++++++"
echo "+  set up working dir (transmart) "
echo "++++++++++++++++++++++++++++"

mkdir $HOME/transmart

echo "++++++++++++++++++++++++++++"
echo "+  set up the transmart-date folder"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart
sudo apt-get install -y curl
sudo apt-get install -y unzip
curl https://codeload.github.com/tranSMART-Foundation/transmart-data/zip/release-1.2.4 -o transmart-data-release-1.2.4.zip
unzip transmart-data-release-1.2.4.zip
mv transmart-data-release-1.2.4 transmart-data

echo "++++++++++++++++++++++++++++"
echo "+  Install of basic tools"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
sudo make -C env ubuntu_deps_root
make -C env ubuntu_deps_regular
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

sudo apt-get install -y tomcat7 
sudo service tomcat7 stop
./updateTomcatConfig.sh

# check - checkTomcatInstall.sh

echo "++++++++++++++++++++++++++++"
echo "+  Install R, Rserve and other packages"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
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
# check - 
# (should probably move these to earlier in the script)
# move to just after checkVersions.sh ?

cd $HOME/transmart/transmart-data
source ./vars
make -j4 postgres
make update_datasets
make -C samples/postgres load_clinical_GSE8581
make -C samples/postgres load_ref_annotation_GSE8581
make -C samples/postgres load_expression_GSE8581

echo "++++++++++++++++++++++++++++"
echo "+  Set up configuration files"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
source ./vars
make -C config install
sudo mkdir -p /usr/share/tomcat7/.grails/transmartConfig/
sudo cp /home/transmart/.grails/transmartConfig/*.groovy /usr/share/tomcat7/.grails/transmartConfig/

# check - checkFilesConfig.sh

echo "++++++++++++++++++++++++++++"
echo "+  Install war files"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart
mkdir war-files
cd war-files
curl http://75.124.74.64/wars/transmart.V1.2.4.war --output transmart.war
curl http://75.124.74.64/wars/gwava.V1.2.4.war --output gwava.war
sudo cp *.war /var/lib/tomcat7/webapps/

echo "++++++++++++++++++++++++++++"
echo "+  Load, configure and start SOLR"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
source ./vars
make -C solr start > ~/transmart/transmart-data/solr.log 2>&1 & 
make -C solr rwg_full_import sample_full_import

echo "++++++++++++++++++++++++++++"
echo "+  start Rserve"
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
sudo -u tomcat7 bash -c 'source vars; make -C R start_Rserve' 

echo "++++++++++++++++++++++++++++"
echo "+  start Tomcat"
echo "++++++++++++++++++++++++++++"

sudo service tomcat7 restart

echo "++++++++++++++++++++++++++++"
echo "+  run checking script!"
echo "++++++++++++++++++++++++++++"
cd $HOME/Scripts/install-ubuntu/checks
pwd


