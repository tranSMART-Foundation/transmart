#!/bin/bash

TMSCRIPTS_BASE="$(dirname -- "$(readlink -f "${BASH_SOURCE}")")"
export TMSCRIPTS_BASE
TMINSTALL_BASE="$(dirname -- "$(dirname -- "$(readlink -f "${TMSCRIPTS_BASE}")")")"
export TMINSTALL_BASE

# Remove everything except Scripts (as that would remove this script)

for PART in "war-files" "transmart-data" "transmart-etl" "transmart-manual" "transmart-batch" "RInterface" "transmart_ICE"
do
    if [ -d "${TMINSTALL_BASE}/$PART" ]; then
	echo "	rm -rf ${TMINSTALL_BASE}/$PART"
    fi;
    if [ -d "${TMINSTALL_BASE}/$PART*.zip" ]; then
	echo "rm  ${TMINSTALL_BASE}/$PART*.zip"
    fi;
    
done
