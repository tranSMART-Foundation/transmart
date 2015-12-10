sudo apt-get update
sudo apt-get install -y git
git clone https://github.com/tranSMART-Foundation/Scripts.git
Scripts/install-ubuntu/InstallTransmart.sh 2>&1 2>&1 | tee install.log
Scripts/install-ubuntu/checks/checkAll.sh 2>&1 2>&1 | tee checks.log
