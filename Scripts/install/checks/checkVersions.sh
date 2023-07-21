#!/bin/bash

# ********************************************************************************
# This script checks for and reports incompatible version numbers in the 
# linux command lines that are needed for the tranSMART install and data loading
# ********************************************************************************

# # ------------------ source helper function -------------------
. $TMSCRIPTS_BASE/checks/versionCompare.sh

versionFile="$TMSCRIPTS_BASE/Versions.txt"

# ----------------------------------------------------------------------------
# Check the version of those command line elements that need specific versions
# ----------------------------------------------------------------------------

echo "-----------------------------------------------------------------"
echo "|  Checking for incompatible version of basic command-line tools."
echo "|  If any problems are reported, then recheck the instructions,"
echo "|  and install or re-install the missing items"
echo "-----------------------------------------------------------------"

returnFlag=0
# check java version, 1.8 (higher versions can break grails+groovy)
desiredJavaVersion="1.8"
excessJavaVersion="1.9"
javaVersion=$(java -version 2>&1 | awk -F '"' '{print $2}')
reportCheckRange "java" $desiredJavaVersion $excessJavaVersion $javaVersion
echo "java $javaVersion" > $versionFile

let "returnFlag=$returnFlag + $?"

# check groovy version, 2.4 or higher
desiredGroovyVersion="2.4"
groovyVersion=$(groovy --version | awk -F '^Groovy Version: ' '{print $2}')
reportCheckOrHigher "groovy" $desiredGroovyVersion $groovyVersion
echo "groovy $groovyVersion" >> $versionFile

let "returnFlag=$returnFlag + $?"

# check php version, 7.2 or higher
desiredPhpVersion="7.2"
phpVersion=$(php --version | awk -F '^PHP ' '{print $2}' | awk -F 'ubuntu' '{print $1}')
reportCheckOrHigher "php" $desiredPhpVersion $phpVersion
echo "php $phpVersion" >> $versionFile

let "returnFlag=$returnFlag + $?"

# check psql version, 10.0 or higher
desiredPsqlVersion="10.0"
version=$(psql --version)
psqlVersion=$( echo "$version" | awk -F '^psql .PostgreSQL. ' '{print $2}')
reportCheckOrHigher "psql" $desiredPsqlVersion $psqlVersion
echo "psql $psqlVersion" >> $versionFile

let "returnFlag=$returnFlag + $?"

# check postgresql server version, 10.0 or higher
# Have to specify default database as transmart may not yet exist
desiredPostgresqlVersion="10.0"
version=$(sudo -u postgres psql -d template1 -c "show server_version" | tail -3 | head -1 | awk '{print $1}')
postgresqlVersion=$( echo "$version" )
reportCheckOrHigher "postgresql" $desiredPostgresqlVersion $postgresqlVersion
echo "postgresql $postgresqlVersion" >> $versionFile

let "returnFlag=$returnFlag + $?"

exit $returnFlag
