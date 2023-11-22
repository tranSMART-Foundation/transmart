#!/bin/bash

#************************************
#  Script to load configuration files
#  for tranSMART release 19.1
#************************************

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallConfig starting"

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

echo "+++++++++++++++++++++++++++++++++++++"
echo "+  03.01 Set up configuration files +"
echo "+++++++++++++++++++++++++++++++++++++"

cd $TMINSTALL_BASE/transmart-data
sudo -v
source ./vars
make -C config install
sudo mkdir -p /var/lib/$tomcatuser/.grails/transmartConfig/
sudo cp $HOME/.grails/transmartConfig/*.groovy /var/lib/$tomcatuser/.grails/transmartConfig/
sudo chown -R $tomcatuser:$tomcatuser /var/lib/$tomcatuser/.grails

sudo mkdir -p /var/lib/$tomcatdir/webapps/transmartimages
sudo chown -R $tomcatuser:$tomcatuser /var/lib/$tomcatdir/webapps/transmartimages

cd $TMSCRIPTS_BASE/checks
./checkFilesConfig.sh
if [ "$( checkInstallError "configuration files not set up correctly, see InstallConfig.sh script and redo" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallConfig done. Finished setting up the configuration files"

