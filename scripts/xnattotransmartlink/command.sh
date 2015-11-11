#!/bin/bash
export KETTLE_HOME="/home/transmart/transmart-data/samples/postgres/kettle-home/"

sh "/home/transmart/transmart-data/env/data-integration/kitchen.sh" \
-norep=N \
-file="/home/transmart/transmart-data/env/tranSMART-ETL/Postgres/GPL-1.0/Kettle/Kettle-ETL/create_clinical_data.kjb" \
-log=load_clinical_data.log \
-param:LOAD_TYPE=I \
-param:COLUMN_MAP_FILE=xnat.tmm \
-param:DATA_LOCATION=$2 \
-param:TOP_NODE="\\Public Studies\\$1\\" \
-param:STUDY_ID=$1 \
-param:SORT_DIR=/home/transmart/ETL \
-logging=Rowlevel \
-level=Rowlevel \
> command.out
