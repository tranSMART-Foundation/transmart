#!/bin/bash

#*****************************
#  Script to load R and Rserve
#  for tranSMART release 19.1
#*****************************

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallRserve starting"

echo "+++++++++++++++++++++++++++++++++++++++++++"
echo "+  NN. Install R, Rserve and other packages"
echo "+++++++++++++++++++++++++++++++++++++++++++"

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

sudo TABLESPACES=$TABLESPACES RSERVE_USER="tomcat8" make -C R install_rserve_unit

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} rserve service installed"

cd $TMSCRIPTS_BASE/checks
./checkFilesR.sh
if [ "$( checkInstallError "R install failed; redo install" )" ] ; then exit -1; fi
./checkR.sh
if [ "$( checkInstallError "R install failed; redo install" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Finished installing R and R packages"


echo "++++++++++++++++++++++++++++"
echo "+  start Rserve"
echo "++++++++++++++++++++++++++++"

sudo -v
cd $TMSCRIPTS_BASE

# rserve runs as user tomcat8
# service started using /etc/systemd/system/rserve.service
# and writes to /var/log/tomcat8/rserve-transmart.log

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Starting rserve service"

sudo systemctl enable rserve
sudo systemctl start rserve

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallRserve done. Finished starting RServe"

