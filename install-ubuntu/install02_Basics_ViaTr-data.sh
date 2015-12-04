cd $HOME/transmart/
curl https://codeload.github.com/tranSMART-Foundation/transmart-data/zip/release-1.2.4 -o transmart-data-release-1.2.4.zip
unzip transmart-data-release-1.2.4.zip
mv transmart-data-release-1.2.4 transmart-data
cd $HOME/transmart/transmart-data
sudo make -C env ubuntu_deps_root
make -C env ubuntu_deps_regular
rm $HOME/transmart/transmart-data/env/groovy