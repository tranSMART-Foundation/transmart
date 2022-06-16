#!/bin/bash

#set -x
set -e

# General optional parameters:
#   DATA_LOCATION, STUDY_NAME, STUDY_ID
# Specific mandatory parameters for this upload script:
#   COLUMN_MAP_FILE, WORD_MAP_FILE, either R_JOBS_PSQL or KETTLE_JOBS_PSQL
# Specific optional parameters for this upload script:
#   TOP_NODE_PREFIX, SECURITY_REQUIRED, USE_R_UPLOAD

# locate this shell script, and source a generic shell script to process all params related settings
UPLOAD_SCRIPTS_DIRECTORY=$(dirname $(realpath "$0"))/.
UPLOAD_DATA_TYPE="program"
echo "UPLOAD_SCRIPTS_DIRECTORY $UPLOAD_SCRIPTS_DIRECTORY"
echo "Parameter '$1'"
source "$UPLOAD_SCRIPTS_DIRECTORY/process_params.inc"

# Check if mandatory variables are set
if [ -z "$PROGRAM_ID" ] || [ -z "$DATA_LOCATION" ]; then
	echo "Following variables need to be set:"
	echo "    PROGRAM_ID=$PROGRAM_ID"
	echo "    DATA_LOCATION=$DATA_LOCATION"
	exit -1
fi

# read program.params file

# check program exists

# load program if required

# read program file
# check disease etc. exists

# need scripts to add missing values
# disease
# therapeutic domain

# similar scripts needed for:
# assay
# analysis
# folder
# file

# Find how to add line breaks
# and new paragraphs in browse tab
# by adding in browser and checking content of column

pwd
echo "UPLOAD_SCRIPTS_DIRECTORY $UPLOAD_SCRIPTS_DIRECTORY"

$UPLOAD_SCRIPTS_DIRECTORY/browse-add-program.pl $DATA_LOCATION/program.txt
