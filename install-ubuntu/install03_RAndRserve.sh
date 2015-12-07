cd $HOME/transmart/transmart-data
. ./vars
make -C R install_packages
echo "export PATH=${HOME}/transmart/transmart-data/R/root/bin:$PATH" > Rpath.sh
sudo mv Rpath.sh /etc/profile.d/
source /etc/profile.d/Rpath.sh
