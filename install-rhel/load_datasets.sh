#!/bin/bash

if [ -z "$SCRIPTS_BASE" ] ; then SCRIPTS_BASE="$HOME" ; fi

echo "Starting at $(date)"
echo "++++++++++++++++++++++++++++"
echo "+  Checking locations of Script Directory"
echo "++++++++++++++++++++++++++++"
if ! [ -d "$SCRIPTS_BASE/Scripts" ] ; then
	echo "This script assumes that the Scripts directory is installed at $SCRIPTS_BASE/Scripts"
	echo "It does not appear to be there. Please fix that and restart this script."
	echo "  cd $SCRIPTS_BASE"
	echo "  sudo yum update"
	echo "  sudo yum install -y git"
	echo "  git clone https://github.com/ricepeterm/Scripts.git"
	echo "  git checkout rhel"
	exit 1
else
	echo "Script directory found: $SCRIPTS_BASE/Scripts"
fi
echo "Finished checking locations of Script Directory at $(date)"

echo "--------------------------------------------"
echo "To load datasets, use the these two files in the Scripts directory: "
echo "    datasetsList.txt - the list of posible datasets to load, and"
echo "    load_datasets.sh - the script to load the datasets. "
echo ""
echo "First, in the file datasetsList.txt, un-comment the lines that "
echo "corresponding to the data sets you wish to load. "
echo ""
echo "Then run the file load_datasets.sh with:"
echo "    cd $SCRIPTS_BASE"
echo "    ./Scripts/install-rhel/load_datasets.sh"
echo ""
echo "-- Note that loading the same dataset twice is not recommended" 
echo "   and may produce unpredictable results"
echo "--------------------------------------------"

echo "++++++++++++++++++++++++++++"
echo "+  Checking locations of working dir (tranSMART install base) "
echo "++++++++++++++++++++++++++++"

if [ -z "$INSTALL_BASE" ] ; then INSTALL_BASE="$HOME/transmart" ; fi
export INSTALL_BASE

if ! [ -d "$INSTALL_BASE/transmart-data" ] ; then
	echo "This script assumes that the transmart-data directory is installed at $INSTALL_BASE/transmart-data"
	echo "It does not appear to be there. Please fix that and restart this script."
	echo "see the 'set up the transmart-data folder' step of installTransmart.sh"
	exit 1
fi

echo "transmart-data is at this location: $INSTALL_BASE/transmart-data"

echo "++++++++++++++++++++++++++++"
echo "+  set up groovy and grails "
echo "++++++++++++++++++++++++++++"
source $HOME/.sdkman/bin/sdkman-init.sh
echo "done"

echo "++++++++++++++++++++++++++++"
echo "+  Load studies from the uncommented lines in datasetsList.txt "
echo "++++++++++++++++++++++++++++"

cd $INSTALL_BASE/transmart-data

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
done < $SCRIPTS_BASE/Scripts/install-ubuntu/datasetList.txt

echo "++++++++++++++++++++++++++++"
echo "+  Done loading studies"
echo "++++++++++++++++++++++++++++"
