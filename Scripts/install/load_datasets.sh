#!/bin/bash

echo "Starting at $(date)"
echo "+++++++++++++++++++++++++++++++++++++++++"
echo "+  Checking locations of Script Directory"
echo "+++++++++++++++++++++++++++++++++++++++++"

TMSCRIPTS_BASE="$(dirname -- "$(readlink -f "${BASH_SOURCE}")")"
export TMSCRIPTS_BASE
TMINSTALL_BASE="$(dirname -- "$(dirname -- "$(readlink -f "${TMSCRIPTS_BASE}")")")"
export TMINSTALL_BASE

echo "Set TMSCRIPTS_BASE ${TMSCRIPTS_BASE}"
echo "Set TMINSTALL_BASE ${TMINSTALL_BASE}"

if ! [ -d "$TMINSTALL_BASE/Scripts" ] ; then
	echo "This script assumes that the Scripts directory is installed at $TMINSTALL_BASE/Scripts"
	echo "It does not appear to be there. Please fix that and restart this script."
	echo "  cd $TMINSTALL_BASE"
	echo "  sudo apt-get update"
	echo "  sudo apt-get install -y git"
	echo "  git clone https://github.com/tranSMART-Foundation/Scripts.git"
	exit 1
else
	echo "Script directory found: $TMINSTALL_BASE/Scripts"
fi
echo "Finished checking locations of Script Directory at $(date)"

echo "----------------------------------------------------------------"
echo "To load datasets, use the these two files"
echo "in the Scripts/install directory:"
echo "    datasetsList.txt - the list of possible datasets to load, and"
echo "    load_datasets.sh - the script to load the datasets. "
echo ""
echo "First, in the file datasetsList.txt, un-comment the lines that"
echo "corresponding to the data sets you wish to load. "
echo ""
echo "Then run the file load_datasets.sh with:"
echo "    cd $TMSCRIPTS_BASE"
echo "    ./load_datasets.sh"
echo ""
echo "-- Note that loading the same dataset twice is not recommended" 
echo "   and may produce unpredictable results"
echo "----------------------------------------------------------------"

echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "+  Checking locations of working dir (tranSMART install base)"
echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"

if ! [ -d "$TMINSTALL_BASE/transmart-data" ] ; then
	echo "This script assumes that the transmart-data directory is installed at $TMINSTALL_BASE/transmart-data"
	echo "It does not appear to be there. Please fix that and restart this script."
	echo "see the 'set up the transmart-data folder' step of InstallTransmart.sh"
	exit 1
fi

echo "transmart-data is at this location: $TMINSTALL_BASE/transmart-data"

echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "+  Load studies from the uncommented lines in datasetsList.txt"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"

cd $TMINSTALL_BASE/transmart-data

source ./vars

make update_datasets

while read F  ; do
	[[ $F = \#* ]] && continue
	
	echo "*************************** loading $F ***************************"
	sleep 4 # to let the echo above show before scrolling off the screen
	
    make -C samples/postgres load_clinical_$F
    make -C samples/postgres load_ref_annotation_$F
    make -C samples/postgres load_expression_$F
	echo "************************ done loading $F *************************"
	echo ""
done < $TMSCRIPTS_BASE/datasetList.txt

echo "++++++++++++++++++++++++++++"
echo "+  Done loading studies"
echo "++++++++++++++++++++++++++++"
