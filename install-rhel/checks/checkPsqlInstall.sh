#!/bin/bash

# ******************************************************************************
# This script checks for and reports missing items in the in the context
# of PostgreSQL - are PostgreSQL and psql installed; is PostgreSQL running;
# does psql respond to queries; has the transmart user logging table been loaded;
# are the admin and guest accounts established; has the GSE8581 database been loaded
# ******************************************************************************

# # ------------------ helper function -------------------
. ./basicsHelper.sh

echo "-------------------------------------"
echo "|  Checking for PostgreSQL basics: "
echo "|    are PostgreSQL and psql installed; "
echo "|   is PostgreSQL running."
echo "-------------------------------------"

echo "checking to see if PostgreSQL has been installed"
if ! checkForCommandLineTool "psql"; then
    echo "it would appear that PostgreSQL has not been installed"
    echo "check loading instructions for how to test further"
    exit 1
fi
echo "PostgreSQL appears to be installed"

echo "checking to see if PostgreSQL is running"
postgresRunning=$(ps aux | grep postgres | grep -v "grep" | grep "stats collector process")
if [ -z "$postgresRunning" ]; then 
	echo "PostgreSQL does not appear to be running; start it"
	#todo rhel check service restart command
	echo  "  with the command: sudo /etc/init.d/postgresql restart"  
	exit 1
fi
echo "PostgreSQL appears to be running"

echo "Done checking the PostgreSQL basics"

exit 0

