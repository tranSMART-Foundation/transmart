#!/bin/bash

#**********************************
#  Script to load Postgres database
#  for tranSMART release 19.1
#**********************************

echo "+++++++++++++++++++++++++++++++++++++++"
echo "+  NN. set up the transmart-data folder"
echo "+++++++++++++++++++++++++++++++++++++++"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallDatabase starting"

cd $TMINSTALL_BASE/
if ! [ -e transmart-data-release-19.1.zip ] ; then
    fetchZipfile $TMSOURCE "transmart-data"
fi
if ! [ -e transmart-data ] ; then
	unzip -q transmart-data-release-19.1.zip
	mv transmart-data-release-19.1 transmart-data
fi

now="$(date +'%d-%b-%y %H:%M')"
echo "Finished setting up the transmart-data folder at ${now}"

echo "++++++++++++++++++++++++++++++++++++++++++++++"
echo "+  NN. Install of basic tools and dependencies"
echo "++++++++++++++++++++++++++++++++++++++++++++++"

# sudo make -C env ubuntu_deps_root
#   In the makefile target, ubuntu_deps_root, causes a
#   make of these two steps: 
# (1)
sudo -v
cd $TMINSTALL_BASE/transmart-data
sudo make -C env install_ubuntu_packages
sudo make -C env install_ubuntu_packages18
# verify these packages
cd $TMSCRIPTS_BASE/checks
./checkMakefilePackages.sh
if [ "$( checkInstallError "Some Basic Command-Line Tool from 'sudo make -C env install_ubuntu_packages' is missing; redo install" )" ] ; then exit -1; fi
now="$(date +'%d-%b-%y %H:%M')"
echo "sudo make -C env install_ubuntu_packages - finished at ${now}"

# (2)
sudo -v
cd $TMINSTALL_BASE/transmart-data
sudo make -C env /var/lib/postgresql/tablespaces
# verify tablespaces
cd $TMSCRIPTS_BASE/checks
./checkFilesTablespace.sh
if [ "$( checkInstallError "TABLESPACE files (/var/lib/postgresql/tablespaces) not set up properly; redo install" )" ] ; then exit -1; fi
now="$(date +'%d-%b-%y %H:%M')"
echo "sudo make -C env /var/lib/postgresql/tablespaces - finished at ${now}"
echo " "

cd $TMSCRIPTS_BASE
./InstallEtl.sh

echo "++++++++++++++++++++++++++++++++++"
echo "+  NN. Dependency: Install of vars"
echo "++++++++++++++++++++++++++++++++++"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Install vars file"
# (3)
sudo -v
cd $TMINSTALL_BASE/transmart-data
make -C env ../vars
# verify setup of vars file
cd $TMSCRIPTS_BASE/checks
./checkFilesVars.sh
if [ "$( checkInstallError "vars file (transmart-data/vars) not set up properly; redo install" )" ] ; then exit -1; fi

cd $TMINSTALL_BASE/transmart-data
# source the vars file to set the path for groovy below
. ./vars

now="$(date +'%d-%b-%y %H:%M')"
echo "make -C env ../vars - finished at ${now}"
echo "Finished setting ubuntu dependencies (without root) at ${now}"

echo "++++++++++++++++++++++++++++++++++++++++"
echo "+  NN. Dependency: Install of ant, maven"
echo "++++++++++++++++++++++++++++++++++++++++"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Check installed packages: ant, maven"
sudo -v
packageInstall ant
packageInstall maven
now="$(date +'%d-%b-%y %H:%M')"
echo "Finished install of ant and maven at ${now}"

echo "${now} Check installed packages: groovy"
cd $TMSCRIPTS_BASE/checks
./checkGroovy.sh
returnCode=$?
if (( $returnCode )); then
    echo "Installing groovy"
    cd $TMINSTALL_BASE/transmart-data
    make -C env groovy
    now="$(date +'%d-%b-%y %H:%M')"
    echo "make -C env groovy - finished at ${now}"
fi

cd $TMSCRIPTS_BASE/checks
./checkGroovy.sh
if [ "$( checkInstallError "groovy not installed correctly; redo install" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "Finished install of groovy at ${now}"

echo "++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "+  NN. Checks on install of tools and dependencies"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++"

# fix files for postgres
now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Patch directory permissions for TABLESPACES"
sudo -v
cd $TMINSTALL_BASE/transmart-data
sudo chmod 700 $TABLESPACES/*

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Checks on basic load"
cd $TMSCRIPTS_BASE/checks
./basics.sh
if [ "$( checkInstallError "Some Basic Command-Line Tool is missing; redo install" )" ] ; then exit -1; fi
./checkVersions.sh
if [ "$( checkInstallError "There is a Command-Line with an unsupportable version; redo install" )" ] ; then exit -1; fi
./checkFilesBasic.sh
if [ "$( checkInstallError "One of more basic files are missing; redo install" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Check on loading and setup of postgres"

./updatePsqlConfig.sh

cd $TMSCRIPTS_BASE/checks
./checkPsqlInstall.sh 
if [ "$( checkInstallError "PostgreSQL is not installed; redo install" )" ] ; then exit -1; fi

./checkFilesPsql.sh
if [ "$( checkInstallError "Database table folders needed by transmart not correct; fix as indicated; then redo install" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "Finished installing basic tools and dependencies at ${now}"

echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "+  NN. Set up basic PostgreSQL; supports transmart login"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Set up basic PostgreSQL with admin role"

# only load database if not already loaded
set +e

cd $TMSCRIPTS_BASE/checks
now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Check role transmartadmin"
results=$(sudo -u postgres psql postgres --command="\du transmartadmin" | grep "transmartadmin")
if [ -z "$results" ]; then
    # Need to add the locale for createdb - may be missing or C.UTF-8 only
    utf=$(locale -a | grep -i en_US.UTF)
    if [ -z "$utf" ]; then
	sudo locale-gen en_US.UTF-8
	sudo systemctl restart postgresql
    fi
    sudo -u postgres psql -c "create role transmartadmin superuser createdb createrole login password 'transmart'"
fi

./checkPsqlDataLoad.sh quiet
returnCode=$?
set -e

now="$(date +'%d-%b-%y %H:%M')"
if [ "$returnCode" -eq 0 ] ; then
	echo "${now} Database is already loaded"
else
	echo "${now} Setting up PostgreSQL database"
	cd $TMINSTALL_BASE/transmart-data
	source ./vars
	now="$(date +'%d-%b-%y %H:%M')"
	echo "Drop existing transmart database (if present)"
	make postgres_drop
	now="$(date +'%d-%b-%y %H:%M')"
	echo "${now} create clean postgres database"
	make postgres
	now="$(date +'%d-%b-%y %H:%M')"
	echo "${now} Update latest dataset resources"
	make update_datasets
	now="$(date +'%d-%b-%y %H:%M')"
	echo "${now} PostgreSQL database created"
fi
cd $TMSCRIPTS_BASE/checks
now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Check for initial datasets to load"
./checkPsqlDataLoad.sh
if [ "$( checkInstallError "Loading database failed; clear database and run install again" )" ] ; then exit -1; fi
now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Datasets loaded (if any)"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallDatabase done. Finished setting up the PostgreSQL database"

