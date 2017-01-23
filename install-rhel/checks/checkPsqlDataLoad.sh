#!/bin/bash

# ******************************************************************************
# This script checks for and reports missing items in the in the context
# of PostgreSQL - are PostgreSQL and psql installed; is PostgreSQL running;
# does psql respond to queries; has the transmart user logging table been loaded;
# are the admin and guest accounts established; has the GSE8581 database been loaded
# ******************************************************************************

# special case with no echo (any argument will suppress echoes
runWithEcho=1
if [ -z $1 ] ; then 
	runWithEcho=0
fi

if [ $runWithEcho -eq 0 ]  ; then
	echo "-------------------------------------"
	echo "|  Checking for transmart database access: "
	echo "|    does 'biomart' use exist "
	echo "|    are the transmart users set up: admin and guest  "
	echo "|    in the demo dataset GSE8581 loaded  "
	echo "-------------------------------------"
fi

sudo -v
if [ "$runWithEcho" -eq 0 ]  ; then
	echo "Checking SUDO authentication"
	sudo echo "Established SUDO authentication"
fi

if [ "$runWithEcho" -eq 0 ]  ; then
	echo "checking to see if PostgreSQL is running"
fi
postgresRunning=$(ps aux | grep postgres | grep -v "grep" | grep "stats collector process")
if [ -z "$postgresRunning" ]; then 
	if [ "$runWithEcho" -eq 0 ]  ; then
		echo "PostgreSQL does not appear to be running; start it"
		echo  "  with the command: sudo /etc/init.d/postgresql restart"  
	fi
	exit 1
fi
if [ "$runWithEcho" -eq 0 ]  ; then
	echo "PostgreSQL appears to be running"
fi

results=$(sudo -u postgres psql postgres --command="\du biomart" | grep biomart)
if [ -z "$results" ]; then
	if [ "$runWithEcho" -eq 0 ]  ; then
		echo "The transmart database user 'biomart' does not exist;"
		echo "  it is likely that you did not initialize the transmart database."
		echo "  See install instructions to do so."
	fi
	exit 1
fi 
if [ "$runWithEcho" -eq 0 ]  ; then
	echo "The transmart database user 'biomart' exists."
fi

results=$(sudo -u postgres psql transmart --command="select username from searchapp.search_auth_user where username='admin'" | grep admin)
if [ -z "$results" ]; then
	if [ "$runWithEcho" -eq 0 ]  ; then
		echo "The transmartApp user 'admin' does not exist;"
		echo "  it is likely that you did not initialize the transmart database."
		echo "  See install instructions to do so."
	fi
	exit 1
fi 
if [ "$runWithEcho" -eq 0 ]  ; then
	echo "The transmartApp user 'admin' exists."
fi

results=$(sudo -u postgres psql transmart --command="select username from searchapp.search_auth_user where username='guest'" | grep guest)
if [ -z "$results" ]; then
	if [ "$runWithEcho" -eq 0 ]  ; then
		echo "The transmartApp user 'guest' does not exist;"
		echo "  it is likely that you did not initialize the transmart database."
		echo "  See install instructions to do so."
	fi
	exit 1
fi 
if [ "$runWithEcho" -eq 0 ]  ; then
	echo "The transmartApp user 'guest' exists."
fi

results=$(sudo -u postgres psql transmart --command="select count(*) from i2b2demodata.concept_dimension" | grep "0")
if [ -z "$results" ]; then
	if [ "$runWithEcho" -eq 0 ]  ; then
		echo "No demo data has been loaded in the transmartApp database."
		echo "  This is expected at this point in the install process."
		echo "  This message is just a reminder to load demo data as per the script 'load_datasets.sh'."
	fi
	exit 1
fi 

if [ "$runWithEcho" -eq 0 ]  ; then
	echo "Done checking the PostgreSQL transmart database"
fi

exit 0

