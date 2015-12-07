#!/bin/bash

scriptPath="$(readlink -f $0)"
scriptBase="$(dirname $scriptPath)"
checkBase="$scriptBase/checks"

pushd $HOME
if [ ! -e "$HOME/transmart" ]; then
	mkdir $HOME/transmart
	echo "Created tranSMART base directory at $HOME/transmart"
fi

echo "HOME is $HOME"
echo "scriptBase is $scriptBase"
echo "checkBase is $checkBase"

echo "++++++++++++++++++++++++++++"
echo "+  Running the install of basic tools"
echo "++++++++++++++++++++++++++++"

source $scriptBase/install01_Basics.sh
source $scriptBase/install02_TransmartDataBasics.sh

echo "++++++++++++++++++++++++++++"
echo "+  Running the install of R and R packages
echo "++++++++++++++++++++++++++++"

source $scriptBase/install03_RAndRserve.sh

echo "++++++++++++++++++++++++++++"
echo "+  Checking the install of basic tools"
echo "++++++++++++++++++++++++++++"

cd $checkBase
./basics.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "** Some problem with the bascis tools check"
	echo "************"
fi

cd $checkBase
./checkVersions.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "** Some problem with the versions check"
	echo "************"
fi

echo "++++++++++++++++++++++++++++"
echo "+  Checking the install of R and R packages
echo "++++++++++++++++++++++++++++"

cd $checkBase
./checkR.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "** Some problem with the checks for R"
	echo "************"
fi

popd
