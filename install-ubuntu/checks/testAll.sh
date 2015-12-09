#!/bin/bash

# ********************************************************************************
# This script runs all of the scripts for checking code sanity; reports return values
# ********************************************************************************

function checkReturnError {
	returnValue=$?
	errorMessage=$1
	if (( $returnValue )); then
		echo "************"
		echo "** $errorMessage"
		echo "************"
	fi
}

./basics.sh
checkReturnError "Some problem with the loading of basic command line tools and initial configuration"

./checkVersions.sh
checkReturnError "Some problem with the versions check"

./checkFilesBasic.sh
checkReturnError "Some problem with the files or folders for basic setup"

./checkTomcatInstall.sh
checkReturnError "Some problem with the tomcat install"

./checkFilesR.sh
checkReturnError "Some problem with the configuration of R files and folders"

./checkR.sh
checkReturnError "Some problem with the instillation of R and/or R packages"

./checkPsqlInstall.sh 
checkReturnError "Some problem with the instillation of RostgreSQL"

./checkFilesPsql.sh
checkReturnError "Some problem with the transmart/PostgreSQL folder"

./checkPsqlDataLoad.sh
checkReturnError "Some problem setting up the database or loading data"

./checkFilesConfig.sh
checkReturnError "Some problem with the configuration files"

./checkFilesTomcatWar.sh
checkReturnError "Some problem installing the tramsmart war file in tomcat"

./checkFilesTomcat.sh
checkReturnError "Some tomcat files or folders are missing"

./checkTools.sh
checkReturnError "One or more of the required tools are not running"

./checkWeb.sh
checkReturnError "One or more of the required web sites are failing"
