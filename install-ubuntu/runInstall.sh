#!/bin/bash
sudo apt-get update
sudo apt-get install -y git

INSTALL_BASE="$HOME/transmart"
SCRIPTS_BASE="$INSTALL_BASE"

export INSTALL_BASE SCRIPTS_BASE

cd "$SCRIPTS_BASE"
git clone https://github.com/tranSMART-Foundation/Scripts.git
Scripts/install-ubuntu/InstallTransmart.sh 2>&1 | tee install.log
