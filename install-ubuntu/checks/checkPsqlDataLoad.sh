#!/bin/bash

# ******************************************************************************
# This script checks for and reports missing items in the in the context
# of PostgreSQL - are PostgreSQL and psql installed; is PostgreSQL running;
# does psql respond to queries; has the transmart user loging table been loaded;
# are the admin and guest accounts established; has the GSP8581 database been loaded
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
	echo  "  with the command: sudo /etc/init.d/postgresql restart"  
	exit 1
fi
echo "PostgreSQL appears to be running"

echo "-------------------------------------"
echo "|  Checking for transmart database access: "
echo "|    does 'biomart' use exist "
echo "|    are the transmart users set up: admin and guest  "
echo "|    in the demo dataset GSE8581 loaded  "
echo "-------------------------------------"

echo "Checking SUDO authentication"
sudo echo "Established SUDO authentication"

results=$(sudo -u postgres psql postgres --command="\du biomart" | grep biomart)
if [ -z "$results" ]; then
	echo "The transmart database user 'biomart' does not exist;"
	echo "  it is likely that you did not initialisze the trasnamrt database."
	echo "  See install instructions to do so."
	exit 1
fi 
echo "The transmart database user 'biomart' exists."

results=$(sudo -u postgres psql transmart --command="select username from searchapp.search_auth_user where username='admin'" | grep admin)
if [ -z "$results" ]; then
	echo "The transmartApp user 'admin' does not exist;"
	echo "  it is likely that you did not initialisze the trasnamrt database."
	echo "  See install instructions to do so."
	exit 1
fi 
echo "The transmartApp user 'admin' exists."

results=$(sudo -u postgres psql transmart --command="select username from searchapp.search_auth_user where username='guest'" | grep guest)
if [ -z "$results" ]; then
	echo "The transmartApp user 'guest' does not exist;"
	echo "  it is likely that you did not initialisze the trasnamrt database."
	echo "  See install instructions to do so."
	exit 1
fi 
echo "The transmartApp user 'guest' exists."

results=$(sudo -u postgres psql transmart --command="select concept_path from i2b2demodata.concept_dimension where concept_path like '%GSE8581\\\\'" | grep "GSE8581")
if [ -z "$results" ]; then
	echo "The transmartApp demo dataset GSE8581 has not been loaded"
	echo "  it is likely load this dataset in the trasnamrt database."
	echo "  See install instructions to do so."
	exit 1
fi 
echo "The transmartApp demo dataset GSE8581 is loaded."

echo "Done checking the PostgreSQL transmart database"

exit 0

