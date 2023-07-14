#!/bin/bash

# **********************************************************************************
# This script checks for and reports missing items in the in the context
# of PostgreSQL - are PostgreSQL and psql installed; is PostgreSQL running;
# does psql respond to queries; has the transmart user loging table been loaded;
# are the admin and guest accounts established; has the GSP8581 database been loaded
# **********************************************************************************

# # ------------------ helper function -------------------

echo "------------------------------------------"
echo "|  Checking for transmart database access:"
echo "|    does 'biomart_user' role exist"
echo "|    are the transmart users set up: admin"
echo "|    is the demo dataset loaded: GSE8581"
echo "------------------------------------------"

echo "Checking SUDO authentication"
sudo echo "Established SUDO authentication"

postgresRunning=$(ps aux | grep postgres | grep -v "grep" | grep "autovacuum launcher")
if [ -z "$postgresRunning" ]; then 
	echo "Postgres is not running"
	exit 1
fi
echo "Postgres is running"

results=$(sudo -u postgres psql transmart --command="select rolname from pg_roles where rolname = 'biomart_user'" | grep biomart_user)
if [ -z "$results" ]; then
	# the transmart biomart_user role does not exist
	echo "The transmart database is not set up"
	exit 2
fi 
echo "The transmartApp role 'biomart_user' exists."

results=$(sudo -u postgres psql transmart --command="select username from searchapp.search_auth_user where username='admin'" | grep admin)
if [ -z "$results" ]; then
	# the transmart admin user does not exist
	echo "The transmart database is not fully set up"
	exit 2
fi 
echo "The transmartApp user 'admin' exists."

results=$(sudo -u postgres psql transmart --command="select username from searchapp.search_auth_user where username='guest'" | grep admin)
if [ -z "$results" ]; then
	# the transmart guest user does not exist
	echo "The transmart database is not set up"
	exit 2
fi 
echo "The transmartApp user 'guest' exists."

results=$(sudo -u postgres psql transmart --command="select concept_path from i2b2demodata.concept_dimension where concept_path like '%GSE8581\\\\'" | grep "GSE8581")
if [ -z "$results" ]; then
	echo "The database is set up but the dataset GSE8581 is not loaded."
	exit 3	
fi 

echo "The transmartApp demo dataset GSE8581 is already loaded."
exit 0
