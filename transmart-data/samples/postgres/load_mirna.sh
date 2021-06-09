#!/bin/bash

#set -x
set -e

# locate this shell script, and source a generic shell script to process all params related settings
UPLOAD_SCRIPTS_DIRECTORY=$(dirname "$0")
UPLOAD_DATA_TYPE="mirna"
source "$UPLOAD_SCRIPTS_DIRECTORY/process_params.inc"

# Check if mandatory parameter values are provided
if [ -z "$DATA_FILE_PREFIX" ] || [ -z "$MAP_FILENAME" ] || [ -z "$MIRNA_TYPE" ] || [ -z "$DATA_TYPE" ]; then
        echo "Following variables need to be set:"
	echo "    DATA_FILE_PREFIX=$DATA_FILE_PREFIX"
	echo "    MAP_FILENAME=$MAP_FILENAME"
	echo "    MIRNA_TYPE=$MIRNA_TYPE"
	echo "    DATA_TYPE=$DATA_TYPE"
    	exit 1
fi

SECURITY_REQUIRED=${SECURITY_REQUIRED:-N}
if [ -z "$TOP_NODE_PREFIX" ]; then
    if [ $SECURITY_REQUIRED = 'Y' ]; then
        TOP_NODE_PREFIX='Private Studies'
    else
        TOP_NODE_PREFIX='Public Studies'
    fi
fi
TOP_NODE="\\${TOP_NODE_PREFIX}\\${STUDY_NAME}\\"

if [ ! -d logs ] ; then mkdir logs; fi

# lt_src_qpcr_mirna_data is truncated by Kettle
$PGSQL_BIN/psql -c "truncate tm_lz.lt_src_qpcr_mirna_data"
echo "Loading zone tables truncated"

$KITCHEN -norep -version                                                \
	 -file="$KETTLE_JOBS/load_qpcr_mirna_data.kjb"                  \
         -level="$KETTLE_LOG_LEVEL"                                     \
	 -logfile="$PWD"/logs/load_mirna_data_$(date +"%Y%m%d%H%M").log \
	 -param:DATA_FILE_PREFIX="$DATA_FILE_PREFIX"                    \
	 -param:DATA_LOCATION="$DATA_LOCATION"                          \
	 -param:DATA_TYPE="$DATA_TYPE"                                  \
	 -param:INC_LOAD="$INC_LOAD"                                    \
	 -param:LOAD_TYPE=I                                             \
	 -param:MAP_FILENAME="$MAP_FILENAME"                            \
	 -param:MIRNA_TYPE="$MIRNA_TYPE"                                \
	 -param:SAMPLE_MAP_FILENAME="$SAMPLE_MAP_FILENAME"              \
	 -param:SECURITY_REQUIRED="$SECURITY_REQUIRED"                  \
	 -param:SORT_DIR=/tmp                                           \
	 -param:STUDY_ID="$STUDY_ID"                                    \
	 -param:TOP_NODE="$TOP_NODE"
