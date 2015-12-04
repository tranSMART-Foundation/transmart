scriptBase=dirname $0
checkBase=$scriptBase/checks

pushd $HOME
mkdir $HOME/transmart
cd $HOME/transmart

echo "++++++++++++++++++++++++++++"
echo "+  Running the install of basic tools
echo "++++++++++++++++++++++++++++"

. ./install01_Basics.sh
. ./install02_TransmartData.sh

echo "++++++++++++++++++++++++++++"
echo "+  Checking the install of basic tools
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

popd