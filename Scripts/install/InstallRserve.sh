#!/bin/bash

#*****************************
#  Script to load R and Rserve
#  for tranSMART release 19.1
#*****************************

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallRserve starting"

echo "+++++++++++++++++++++++++++++++++++++++++++++++"
echo "+  06.01 Install R, Rserve and other packages +"
echo "+++++++++++++++++++++++++++++++++++++++++++++++"

case $TMINSTALL_OS in
    ubuntu)
	case $TMINSTALL_OSVERSION in
	    18.04 | 18)
		tomcatuser="tomcat8"
		tomcatdir="tomcat8"
		;;
	    20.04 | 20 | 22.04 | 22)
		tomcatuser="tomcat"
		tomcatdir="tomcat"
		;;
	esac
esac

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Check R installation"

base="$TMINSTALL_BASE/transmart-data"
baseR="$base/R"
filepath="$base/R/root/bin"
now="$(date +'%d-%b-%y %H:%M')"
if [ -e "$filepath" ]; then
    echo "${now} +   R is already installed"
else
    echo "${now} +  installing R at $filepath"
    sudo -v
    cd $TMINSTALL_BASE/transmart-data
    source ./vars
    make -C R install_packages
    now="$(date +'%d-%b-%y %H:%M')"
    echo "${now} R install completed"
fi

sudo -v
cd $HOME
if ! [ -e /etc/profile.d/Rpath.sh ] ; then
    echo "export PATH=${filepath}:\$PATH" > Rpath.sh
    sudo mv Rpath.sh /etc/profile.d/
    now="$(date +'%d-%b-%y %H:%M')"
    echo "${now} /etc/profile.d/Rpath.sh created"
else
    now="$(date +'%d-%b-%y %H:%M')"
    echo "${now} /etc/profile.d/Rpath.sh already exists"
fi

source /etc/profile.d/Rpath.sh

sudo -v
cd $TMINSTALL_BASE/transmart-data
source ./vars

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Installing rserve service"

sudo TABLESPACES=$TABLESPACES RSERVE_USER="$tomcatuser" make -C R install_rserve_unit

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} rserve service installed"

cd $TMSCRIPTS_BASE/checks
./checkFilesR.sh
if [ "$( checkInstallError "R install failed; redo install" )" ] ; then exit -1; fi
./checkR.sh
if [ "$( checkInstallError "R install failed; redo install" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Finished installing R and R packages"


echo "+++++++++++++++++++++++"
echo "+  06.02 start Rserve +"
echo "+++++++++++++++++++++++"

sudo -v
cd $TMSCRIPTS_BASE

# rserve runs as user tomcatN
# service started using /etc/systemd/system/rserve.service
# and writes to /var/log/tomcatN/rserve-transmart.log

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Starting rserve service"

sudo systemctl enable rserve
sudo systemctl start rserve

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallRserve done. Finished starting RServe"

