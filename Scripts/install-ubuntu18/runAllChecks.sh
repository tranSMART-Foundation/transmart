#!/bin/bash
cd $HOME/Scripts/install-ubuntu18/checks/
./checkAll.sh 2>&1 2>&1 | tee ~/checks.log
