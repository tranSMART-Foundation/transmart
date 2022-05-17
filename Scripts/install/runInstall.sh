#!/bin/bash
#sudo apt-get -q update
#sudo apt-get -q install -y git

if [ "$TMGITHOME" == "" ]; then
    # The location of the git clone download
    TMGITHOME="$(readlink -f "${HOME}")"
    export TMGITHOME
else
    cd "$TMGITHOME"
fi

cd "$TMGITHOME"

if [ -d "transmart" ]; then
    echo "Directory $TMGITHOME/transmart already exists"
    exit 1
fi

echo "Downloading transmart source code in $TMGITHOME"
git clone https://github.com/tranSMART-Foundation/transmart.git
git checkout release-19.1

echo "Launching transmart install script"

cd transmart/Scripts
./InstallTransmart.sh 2>&1 | tee install.log

