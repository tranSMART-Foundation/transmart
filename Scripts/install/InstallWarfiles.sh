#!/bin/bash

#**************************************************
#  Script to load war files for tranSMART and GWAVA
#  for tranSMART release 19.1
#**************************************************

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallWarfiles starting"

echo "++++++++++++++++++++++++++++"
echo "+  04.01 Install war files +"
echo "++++++++++++++++++++++++++++"

cd $TMINSTALL_BASE
sudo -v
if ! [ -d war-files ]; then
	mkdir war-files
fi

cd war-files
if ! [ -e transmart.war ]; then
    fetchWarfile "$TMSOURCE" transmartApp transmart
fi
if ! [ -e gwava.war ]; then
    fetchWarfile "$TMSOURCE" gwava gwava
fi

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Copy transmart and gwava war files to tomcat directory"

sudo cp *.war /var/lib/tomcat8/webapps/

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} War files copied"

cd $TMSCRIPTS_BASE/checks
sudo ./checkFilesTomcatWar.sh
if [ "$( checkInstallError "transmart war and/or gwava.war file(s) not set up correctly, see install script and redo" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallWarfiles done. Finished installing war files"

