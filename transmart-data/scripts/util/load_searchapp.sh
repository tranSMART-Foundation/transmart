#!/bin/bash -e

./loadtable.sh searchapp search_auth_principal
./loadtable.sh searchapp search_role
./loadtable.sh searchapp search_auth_group
./loadtable.sh searchapp search_auth_user
./loadtable.sh searchapp search_role_auth_user
./loadtable.sh searchapp search_sec_access_level
