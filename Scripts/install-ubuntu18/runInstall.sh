#!/bin/bash
sudo apt-get -q update
sudo apt-get -q install -y git

# The location of the install directories
INSTALL_BASE="$HOME/transmart"

# The location of the Scripts for installing and verifying 
SCRIPTS_BASE="$INSTALL_BASE"

export INSTALL_BASE SCRIPTS_BASE

mkdir -p "$INSTALL_BASE"
mkdir -p "$SCRIPTS_BASE"

cd "$SCRIPTS_BASE"
git clone https://github.com/tranSMART-Foundation/transmart.git
cd transmart/Scripts
git checkout release-16.4
cd ..
Scripts/install-ubuntu18/InstallTransmart.sh 2>&1 | tee install.log

