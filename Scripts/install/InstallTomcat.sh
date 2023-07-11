#!/bin/bash

#******************************
#  Script to install tomcat 8/9
#  for tranSMART release 19.1
#******************************

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallTomcat starting"

echo "+++++++++++++++++++++++++++++"
echo "+  08.01 Install Tomcat 8/9 +"
echo "+++++++++++++++++++++++++++++"

sudo -v 
cd $HOME
now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Check installed packages: tomcat"

packageInstall tomcat8 

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Stop tomcat service to update configuration"

sudo service tomcat8 stop
$TMSCRIPTS_BASE/updateTomcatConfig.sh

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Copy transmart config files to new tomcat user"

sudo mkdir -p /var/lib/tomcat8/.grails/transmartConfig/
sudo cp $HOME/.grails/transmartConfig/*.groovy /var/lib/tomcat8/.grails/transmartConfig/
sudo chown -R tomcat8:tomcat8 /var/lib/tomcat8/.grails

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} +  Check tomcat install"

cd $TMSCRIPTS_BASE/checks
./checkTomcatInstall.sh
if [ "$( checkInstallError "Tomcat install failed; redo install" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Restarting tomcat"

echo "+++++++++++++++++++++++"
echo "+  08.02 start Tomcat +"
echo "+++++++++++++++++++++++"

#  (TODO)  Should check to see if it is already running
sudo systemctl restart tomcat8
now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Finished starting Tomcat8"
echo "${now} Sleeping - waiting for tomcat/transmart to start (3 minutes)"
sleep 3m

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallTomcat done. Finished installing tomcat"

