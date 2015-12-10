sudo apt-get update
sudo apt-get install -y curl
curl -o Scripts.zip https://github.com/tranSMART-Foundation/Scripts/archive/master.zip
sudo apt-get install -y unzip
unzip Scripts.zip
Scripts/install-ubuntu/InstallTransmart.sh

sudo apt-get install -y git
git clone https://github.com/tranSMART-Foundation/Scripts.git
Scripts/InstallTransmart.sh
