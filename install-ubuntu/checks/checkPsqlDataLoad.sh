#!/bin/bash

# ******************************************************************************
# This script checks for and reports missing items in the in the context
# of PostgreSQL - are PostgreSQL and psql installed; is PostgreSQL running;
# does psql respond to queries; has the transmart user loging table been loaded;
# are the admin and guest accounts established; has the GSP8581 database been loaded
# ******************************************************************************

# special case with no echo
runWithEcho=1
if [ -z $1 ] ; then 
	runWithEcho=0
fi

if ($runWithEcho) ; then
	echo "-------------------------------------"
	echo "|  Checking for transmart database access: "
	echo "|    does 'biomart' use exist "
	echo "|    are the transmart users set up: admin and guest  "
	echo "|    in the demo dataset GSE8581 loaded  "
	echo "-------------------------------------"
if

sudo -v
if ($runWithEcho) ; then
	echo "Checking SUDO authentication"
	sudo echo "Established SUDO authentication"
fi

if ($runWithEcho) ; then
	echo "checking to see if PostgreSQL is running"
fi
postgresRunning=$(ps aux | grep postgres | grep -v "grep" | grep "stats collector process")
if [ -z "$postgresRunning" ]; then 
	if ($runWithEcho) ; then
		echo "PostgreSQL does not appear to be running; start it"
		echo  "  with the command: sudo /etc/init.d/postgresql restart"  
	fi
	exit 1
fi
if ($runWithEcho) ; then
	echo "PostgreSQL appears to be running"
if

results=$(sudo -u postgres psql postgres --command="\du biomart" | grep biomart)
if [ -z "$results" ]; then
	if ($runWithEcho) ; then
		echo "The transmart database user 'biomart' does not exist;"
		echo "  it is likely that you did not initialisze the trasnamrt database."
		echo "  See install instructions to do so."
	fi
	exit 1
fi 
if ($runWithEcho) ; then
	echo "The transmart database user 'biomart' exists."
fi

results=$(sudo -u postgres psql transmart --command="select username from searchapp.search_auth_user where username='admin'" | grep admin)
if [ -z "$results" ]; then
	if ($runWithEcho) ; then
		echo "The transmartApp user 'admin' does not exist;"
		echo "  it is likely that you did not initialisze the trasnamrt database."
		echo "  See install instructions to do so."
	fi
	exit 1
fi 
if ($runWithEcho) ; then
	echo "The transmartApp user 'admin' exists."
fi

results=$(sudo -u postgres psql transmart --command="select username from searchapp.search_auth_user where username='guest'" | grep guest)
if [ -z "$results" ]; then
	if ($runWithEcho) ; then
		echo "The transmartApp user 'guest' does not exist;"
		echo "  it is likely that you did not initialisze the trasnamrt database."
		echo "  See install instructions to do so."
	fi
	exit 1
fi 
if ($runWithEcho) ; then
	echo "The transmartApp user 'guest' exists."
fi

results=$(sudo -u postgres psql transmart --command="select concept_path from i2b2demodata.concept_dimension where concept_path like '%GSE8581\\\\'" | grep "GSE8581")
if [ -z "$results" ]; then
	if ($runWithEcho) ; then
		echo "The transmartApp demo dataset GSE8581 has not been loaded"
		echo "  it is likely that you did not load this dataset into the trasnamrt database."
		echo "  See install instructions to do so."
	fi
	exit 1
fi 
if ($runWithEcho) ; then
	echo "The transmartApp demo dataset GSE8581 is loaded."
fi

if ($runWithEcho) ; then
	echo "Done checking the PostgreSQL transmart database"
fi

exit 0

