#!/bin/bash

#***********************************
#  Script to install transmartmanual
#  for tranSMART release 19.1
#***********************************

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallManual starting"

echo "+++++++++++++++++++++++++++++++++++"
echo "+  07.01 Install transmart-manual +"
echo "+++++++++++++++++++++++++++++++++++"

case $TMINSTALL_OS in
    ubuntu)
	case $TMINSTALL_OSVERSION in
	    18.04 | 18)
		tomcatdir="tomcat8"
		tomcatuser="tomcat8"
		;;
	    20.04 | 20 | 22.04 | 22)
		tomcatdir="tomcat9"
		tomcatuser="tomcat"
		;;
	esac
esac

cd $TMINSTALL_BASE

sudo -v
now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Download manual"

rm -rf transmart-manual-release-19.1
fetchZipfile $TMSOURCE "transmart-manual"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} unzip manual"

unzip -q transmart-manual-release-19.1.zip

sudo rm -rf /var/lib/$tomcatdir/webapps/transmartmanual

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Copy manual to tomcat directory"
sudo cp -r transmart-manual-release-19.1 /var/lib/$tomcatdir/webapps/transmartmanual

sudo chown -R $tomcatuser:$tomcatuser /var/lib/$tomcatdir/webapps/transmartmanual

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Manual installed"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallManual done"
