Use these steps to install transmart on a "clean" ubuntu system (assumes 16.04).
For details see the instructions at
https://wiki.transmartfoundation.org/display/transmartwiki/Install+the+current+official+release

    sudo apt-get update
    sudo apt-get install -y git
    git clone https://github.com/tranSMART-Foundation/transmart.git
    cd transmart
    git checkout -b release-16.4
    Scripts/install-ubuntu18/InstallTransmart.sh 2>&1 | tee install.log
    cd Scripts/install-ubuntu18/checks/
    ./checkAll.sh 2>&1 2>&1 | tee ~/checks.log
