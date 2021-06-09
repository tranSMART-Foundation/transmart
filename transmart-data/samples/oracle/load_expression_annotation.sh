#!/bin/bash

#set -x
set -e

STARTTIME=$(date +%s)

# General optional parameters:
#   DATA_LOCATION, STUDY_NAME, STUDY_ID
# Mandatory parameters specific for this upload script:
#   ANNOTATIONS_FILE
# Optional parameter(s) specific for this upload script:
#   PLATFORM_ID, PLATFORM_TITLE, GENOME_RELEASE

UPLOAD_SCRIPTS_DIRECTORY=$(dirname "$0")
UPLOAD_DATA_TYPE="annotation"

# load definitions in annotation.params

source $1
cd $2

if [ -z "$PLATFORM_ID" ] &&  [ ! -z "$PLATFORM" ]; then
    PLATFORM_ID=$PLATFORM
fi

PLATFORM_TITLE=${PLATFORM_TITLE:-${TITLE:-${PLATFORM_ID}}}
make "$JDBC_DRIVER"

groovy -cp "$LIB_CLASSPATH" InsertGplInfo.groovy \
	-p "$PLATFORM_ID" \
	-t "$PLATFORM_TITLE" \
	-m "Gene Expression" \
	-o "$ORGANISM" || { test $? -eq 3 && exit 0; }
# the exit code is 3 if we are to skip the rest
# due to annotation being already loaded

groovy -cp "$LIB_CLASSPATH" LoadTsvFile.groovy \
	-t tm_lz.lt_src_deapp_annot \
	-c gpl_id,probe_id,gene_symbol,gene_id,organism \
	-f $DATA_LOCATION/$ANNOTATIONS_FILE \
	--truncate

groovy -cp "$LIB_CLASSPATH" RunStoredProcedure.groovy

ENDTIME=$(date +%s)

SECONDS=$(($ENDTIME - $STARTTIME))

echo "Completed in $(($SECONDS / 60)) minutes $(($SECONDS % 60)) seconds";
