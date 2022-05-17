#!/bin/bash

# **********************************************************************************
# This script checks for and reports missing items in the in the context
# of PostgreSQL - are PostgreSQL and psql installed; is PostgreSQL running;
# does psql respond to queries; has the transmart user loging table been loaded;
# are the admin and guest accounts established; has the GSE8581 database been loaded
# **********************************************************************************

# # ------------------ helper function -------------------
. $TMSCRIPTS_BASE/checks/basicsHelper.sh

echo "---------------------------------------"
echo "|  Checking for PostgreSQL basics:"
echo "|    are PostgreSQL and psql installed;"
echo "|    is PostgreSQL running."
echo "---------------------------------------"

echo "checking to see if PostgreSQL has been installed"
if ! checkForCommandLineTool "psql"; then
    echo "It would appear that PostgreSQL has not been installed"
    echo "check loading instructions for how to test further"
    exit 1
fi
echo "PostgreSQL appears to be installed"

echo "checking to see if PostgreSQL is running"
postgresRunning=$(ps aux | grep postgres | grep -v "grep" | grep "stats collector")
if [ -z "$postgresRunning" ]; then 
	echo "PostgreSQL server does not appear to be running;"
	echo  "  start with the command:"
	echo  "  sudo service postgresql restart"  
	exit 1
fi
postgresVersion=$(sudo -u postgres psql -d template1 -c "show server_version" | tail -3 | head -1 | awk '{print $1}')
if [ -z "$postgresVersion" ]; then 
	echo "Postgres psql does not appear to be running"
	echo  "  Check postgreSQL installation"
	exit 1
fi

echo "PostgreSQL appears to be running with version $postgresVersion"

echo "Done checking the PostgreSQL basics"

exit 0

