#!/bin/bash

#***********************************
#  Script to install transmartmanual
#  for tranSMART release 19.1
#***********************************

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallManual starting"

echo "+++++++++++++++++++++++++++++++"
echo "+  NN. Install transmart-manual"
echo "+++++++++++++++++++++++++++++++"

cd $TMINSTALL_BASE

sudo -v
now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Download manual"

rm -rf transmart-manual-release-19.1
fetchZipfile $TMSOURCE "transmart-manual"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} unzip manual"

unzip -q transmart-manual-release-19.1.zip

sudo rm -rf /var/lib/tomcat8/webapps/transmartmanual

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Copy manual to tomcat directory"
sudo cp -r transmart-manual-release-19.1 /var/lib/tomcat8/webapps/transmartmanual

sudo chown -R tomcat8.tomcat8 /var/lib/tomcat8/webapps/transmartmanual

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Manual installed"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallManual done"
