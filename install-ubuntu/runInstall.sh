#!/bin/bash
sudo apt-get -qq update
sudo apt-get -qq install -y git

# The location of the install directories
INSTALL_BASE="$HOME/transmart"

# The location of the Scripts for installing and verifying 
SCRIPTS_BASE="$INSTALL_BASE"

export INSTALL_BASE SCRIPTS_BASE

cd "$SCRIPTS_BASE"
git clone https://github.com/tranSMART-Foundation/Scripts.git
Scripts/install-ubuntu/InstallTransmart.sh 2>&1 | tee install.log

