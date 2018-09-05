#!/bin/bash

# ******************************************************************************
# This script checks for and reports missing items in the in the context
# of PostgreSQL - are PostgreSQL and psql installed; is PostgreSQL running;
# does psql respond to queries; has the transmart user logging table been loaded;
# are the admin and guest accounts established; has the GSE8581 database been loaded
# ******************************************************************************

# # ------------------ helper function -------------------

echo "-------------------------------------"
echo "|  Checking for transmart database access: "
echo "|    does 'biomart' use exist "
echo "|    are the transmart users set up: admin and guest  "
echo "|    in the demo dataset GSE8581 loaded  "
echo "-------------------------------------"

echo "Checking SUDO authentication"
sudo echo "Established SUDO authentication"

postgresRunning=$(ps aux | grep postgres | grep -v "grep" | grep "stats collector process")
if [ -z "$postgresRunning" ]; then 
	echo "Postgres is not running"
	exit 1
fi
echo "Postgres is running"

results=$(sudo -u postgres psql transmart --command="select username from searchapp.search_auth_user where username='admin'" | grep admin)
if [ -z "$results" ]; then
	# the transmart admin user does not exist
	echo "The transmart database is not set up"
	exit 2
fi 
echo "The transmartApp user 'admin' exists."

results=$(sudo -u postgres psql transmart --command="select concept_path from i2b2demodata.concept_dimension where concept_path like '%GSE8581\\\\'" | grep "GSE8581")
if [ -z "$results" ]; then
	echo "The database is set up but the dataset GSE8581 is not loaded."
	exit 3	
fi 

echo "The transmartApp demo dataset GSE8581 is already loaded."
exit 0
