Use these steps to install transmart on a "clean" ubuntu system (assumes 14.04).
For details see the instructions at
https://wiki.transmartfoundation.org/display/transmartwiki/Install+the+current+official+release

    sudo apt-get update
    sudo apt-get install -y git
    git clone https://github.com/tranSMART-Foundation/Scripts.git
    Scripts/install-ubuntu14/InstallTransmart.sh 2>&1 | tee install.log
    cd Scripts/install-ubuntu14/checks/
    ./checkAll.sh 2>&1 2>&1 | tee ~/checks.log

