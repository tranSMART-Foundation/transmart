#!/bin/bash
cd $HOME/Scripts/install-ubuntu16/checks/
./checkAll.sh 2>&1 2>&1 | tee ~/checks.log
