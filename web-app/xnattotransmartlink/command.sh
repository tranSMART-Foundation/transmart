#!/bin/bash

rm -f  "command-running.txt"
touch "command-running.txt"

if [ -z "$3" ]; then
    export KITCHEN_DIR="/home/transmart/transmart/transmart-data/env/data-integration"
else
    export KITCHEN_DIR="$3"
fi

if [ -z "$4" ]; then
    export KETTLE_SCRIPTS="/home/transmart/transmart/transmart-data/env/tranSMART-ETL/Kettle/postgres/Kettle-ETL"
else
    export KETTLE_SCRIPTS="$4"
fi

if [ ! -z "$5" ]; then
    export KETTLE_HOME="$5"
    echo "KETTLE_HOME: $KETTLE_HOME"
fi

echo "pwd: " `pwd` >> command-running.txt
echo "sh $KITCHEN_DIR/kitchen.sh \ " >> command-running.txt
echo "-norep=N \ " >> command-running.txt
echo "-file=$KETTLE_SCRIPTS/create_clinical_data.kjb \ " >> command-running.txt
echo "-log=load_clinical_data.log \ " >> command-running.txt
echo "-param:LOAD_TYPE=I \ " >> command-running.txt
echo "-param:COLUMN_MAP_FILE=xnat.tmm \ " >> command-running.txt
echo "-param:DATA_LOCATION=$2 \ " >> command-running.txt
echo "-param:TOP_NODE=\\Public Studies\\$1\\ \ " >> command-running.txt
echo "-param:STUDY_ID=$1 \ " >> command-running.txt
echo "-param:SORT_DIR=/tmp/XNAT_ETL \ " >> command-running.txt
echo "-logging=Rowlevel \ " >> command-running.txt
echo "-level=Rowlevel \ " >> command-running.txt
echo "> command.out" >> command-running.txt


sh "$KITCHEN_DIR/kitchen.sh" \
   -norep=N \
   -file="$KETTLE_SCRIPTS/create_clinical_data.kjb" \
   -log=load_clinical_data.log \
   -param:LOAD_TYPE=I \
   -param:COLUMN_MAP_FILE=xnat.tmm \
   -param:DATA_LOCATION="$2" \
   -param:TOP_NODE="\\Public Studies\\$1\\" \
   -param:STUDY_ID="$1" \
   -param:SORT_DIR=/tmp/XNAT_ETL \
   -logging=Rowlevel \
   -level=Rowlevel \
> command.out
