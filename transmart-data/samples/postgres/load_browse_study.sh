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
UPLOAD_DATA_TYPE="browse"
echo "UPLOAD_SCRIPTS_DIRECTORY $UPLOAD_SCRIPTS_DIRECTORY"
echo "Parameter '$1'"
source "$UPLOAD_SCRIPTS_DIRECTORY/process_params.inc"

# Check if mandatory variables are set
if [ -z "$STUDY_ID" ] || [ -z "$STUDY_PROGRAM" ] || [ -z "$DATA_LOCATION" ]; then
	echo "Following variables need to be set:"
	echo "    STUDY_ID=$STUDY_ID"
	echo "    STUDY_PROGRAM=$STUDY_PROGRAM"
	echo "    DATA_LOCATION=$DATA_LOCATION"
	exit -1
fi

# read browse.params file

# check program exists

# load program if required

# read browse file
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

$UPLOAD_SCRIPTS_DIRECTORY/browse-add-study.pl $DATA_LOCATION/browse.txt
