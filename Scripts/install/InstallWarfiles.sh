#!/bin/bash

#**************************************************
#  Script to load war files for tranSMART and GWAVA
#  for tranSMART release 19.1
#**************************************************

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallWarfiles starting"

echo "Testing OS '$TMINSTALL_OS' VERSION '$TMINSTALL_OSVERSION'"

case $TMINSTALL_OS in
    ubuntu)
	case $TMINSTALL_OSVERSION in
	    18.04 | 18)
		tomcatdir="tomcat8"
		tomcatservice="tomcat8"
		;;
	    20.04 | 20 | 22.04 | 22)
		tomcatdir="tomcat9"
		tomcatservice="tomcat9"
		;;
	esac
esac


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

sudo cp *.war /var/lib/$tomcatdir/webapps/

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} War files copied"

cd $TMSCRIPTS_BASE/checks
sudo ./checkFilesTomcatWar.sh $TMINSTALL_OS $TMINSTALL_OSVERSION
if [ "$( checkInstallError "transmart war and/or gwava.war file(s) not set up correctly, see install script and redo" )" ] ; then exit -1; fi

echo "+++++++++++++++++++++++++"
echo "+  04.02 restart Tomcat +"
echo "+++++++++++++++++++++++++"

#  (TODO)  Should check to see if it is already running
sudo systemctl restart $tomcatservice
now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Finished starting $tomcatservice"
echo "${now} Sleeping - waiting for tomcat and transmart to start (3 minutes)"
sleep 3m

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallWarfiles done. Finished installing war files"

