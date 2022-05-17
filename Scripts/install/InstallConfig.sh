#!/bin/bash

#************************************
#  Script to load configuration files
#  for tranSMART release 19.1
#************************************

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallConfig starting"

echo "+++++++++++++++++++++++++++++++++"
echo "+  NN. Set up configuration files"
echo "+++++++++++++++++++++++++++++++++"

cd $TMINSTALL_BASE/transmart-data
sudo -v
source ./vars
make -C config install
sudo mkdir -p /var/lib/tomcat8/.grails/transmartConfig/
sudo cp $HOME/.grails/transmartConfig/*.groovy /var/lib/tomcat8/.grails/transmartConfig/
sudo chown -R tomcat8:tomcat8 /var/lib/tomcat8/.grails

cd $TMSCRIPTS_BASE/checks
./checkFilesConfig.sh
if [ "$( checkInstallError "configuration files not set up correctly, see install script and redo" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallConfig done. Finished setting up the configuration files"

