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
#. $scriptBase/install02_TransmartDataBasics.sh

echo "$scriptBase/install01_Basics.sh"
echo "$scriptBase/install02_TransmartDataBasics.sh"

echo "++++++++++++++++++++++++++++"
echo "+  Checking the install of basic tools"
echo "++++++++++++++++++++++++++++"

#cd $checkBase
#./basics.sh
#returnValue=$?
#if (( $returnValue )); then
#	echo "************"
#	echo "** Some problem with the bascis tools check"
#	echo "************"
#fi

#cd $checkBase
#./checkVersions.sh
#returnValue=$?
#if (( $returnValue )); then
#	echo "************"
#	echo "** Some problem with the versions check"
#	echo "************"
#fi
#
popd
