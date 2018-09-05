Use these steps to install transmart on a "clean" RedHat Enterprise Linux system (assumes 7.2).
For details see the instructions at
https://wiki.transmartfoundation.org/display/transmartwiki/Install+the+current+official+release+RHEL

    sudo yum update
    sudo yum install -y git
    git clone https://github.com/tranSMART-Foundation/Scripts.git
    cd Scripts
    git checkout release-16.2
    cd ..
    Scripts/install-rhel/InstallTransmart.sh 2>&1 | tee install.log
    cd Scripts/install-rhel/checks/
    ./checkAll.sh 2>&1 2>&1 | tee ~/checks.log

