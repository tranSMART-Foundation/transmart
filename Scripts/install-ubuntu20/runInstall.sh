#!/bin/bash
sudo apt-get -q update
sudo apt-get -q install -y git

# The location of the install directories
INSTALL_BASE="$HOME/transmart"

# The location of the Scripts for installing and verifying 
SCRIPTS_BASE="$INSTALL_BASE"

export INSTALL_BASE SCRIPTS_BASE

cd "$HOME"
git clone https://github.com/tranSMART-Foundation/transmart.git
cd transmart/Scripts
git checkout release-19.0
cd ..
Scripts/install-ubuntu20/InstallTransmart.sh 2>&1 | tee install.log

