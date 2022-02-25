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
UPLOAD_SCRIPTS_DIRECTORY=$(dirname "$0")
UPLOAD_DATA_TYPE="browse"
source "$UPLOAD_SCRIPTS_DIRECTORY/process_params.inc"

# Check if mandatory variables are set
if [ -z "$STUDY_ID" ] || [ -z "$SOME_VARIABLE" ]; then
	echo "Following variables need to be set:"
	echo "    STUDY_ID=$STUDY_ID"
	echo "    SOME_VARIABLE=$SOME_VARIABLE"
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

