#!/bin/bash

#set -x
set -e

echo "running load_rnaseq_annotation.sh $1"

# locate this shell script, and source a generic shell script to process all params related settings
UPLOAD_SCRIPTS_DIRECTORY=$(dirname "$0")
UPLOAD_DATA_TYPE="annotation"
source "$UPLOAD_SCRIPTS_DIRECTORY/process_params.inc"

# For older params

if [ -z "$ANNOTATION_ID" ] || [ ! -z "$GPL_ID" ]; then
    ANNOTATION_ID=$GPL_ID
fi

# Execute some basic checks
if [ -z "$ANNOTATION_ID" ] || [ -z "$ANNOTATION_TITLE" ]; then
	echo "Following variables need to be set:"
        echo "    ANNOTATION_ID=$ANNOTATION_ID"
	echo "    ANNOTATION_TITLE=$ANNOTATION_TITLE"
    	exit 1
fi

if [ ! -d logs ] ; then mkdir logs; fi

# Is the platform already uploaded?
ALREADY_LOADED=`$PGSQL_BIN/psql -c "select exists \
                (select platform from deapp.de_gpl_info where platform = '$ANNOTATION_ID')" -tA`
if [ $ALREADY_LOADED = 't' ]; then
    echo -e "\e[33mWARNING\e[m: Platform $ANNOTATION_ID already loaded; skipping" >&2
    exit 0
fi

# Start the upload
$KITCHEN -norep -version                                                       \
	 -file="$KETTLE_JOBS/load_rna_annotation.kjb"                          \
	 -level="$KETTLE_LOG_LEVEL"                                            \
	 -logfile="$PWD"/logs/load_rna_annotation_$(date +"%Y%m%d%H%M").log    \
	 -param:ANNOTATION_TITLE="$ANNOTATION_TITLE"                           \
	 -param:DATA_LOCATION="$DATA_LOCATION"                                 \
	 -param:GPL_ID="$ANNOTATION_ID"                                        \
	 -param:LOAD_TYPE=I                                                    \
	 -param:SORT_DIR=/tmp
