mkdir ~/transmart
cd ~/transmart
sudo apt-get install -y curl
curl https://codeload.github.com/tranSMART-Foundation/transmart-data/zip/release-1.2.4 -o transmart-data-release-1.2.4.zip
unzip transmart-data-release-1.2.4.zip
mv transmart-data-release-1.2.4 transmart-data
cd transmart-data
sudo make -C env ubuntu_deps_root
make -C env ubuntu_deps_regular
sudo apt-get install -y ant
sudo apt-get install -y maven
rm ~/transmart/transmart-data/env/groovy
curl -s get.sdkman.io | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install grails 2.3.11
sdk install groovy 2.4.5
sudo apt-get install -y tomcat7 
sudo service tomcat7 stop
sudo cp /etc/default/tomcat7 /etc/default/tomcat7-backup
# need to edit /etc/default/tomcat7 with JAVA_OPTS="-Djava.awt.headless=true -Xms512m -Xmx2g -XX:+UseConcMarkSweepGC"
cd ~/transmart/transmart-data
. ./vars
make -C R install_packages
cd ~
echo "export PATH=${HOME}/transmart/transmart-data/R/root/bin:$PATH" > Rpath.sh
sudo mv Rpath.sh /etc/profile.d/
source /etc/profile.d/Rpath.sh
cd ~/transmart/transmart-data
. ./vars
make -j4 postgres
make update_datasets
make -C samples/postgres load_clinical_GSE8581
make -C samples/postgres load_ref_annotation_GSE8581
make -C samples/postgres load_expression_GSE8581
cd ~/transmart/transmart-data
. ./vars
make -C config install
sudo mkdir -p /usr/share/tomcat7/.grails/transmartConfig/
sudo cp /home/transmart/.grails/transmartConfig/*.groovy /usr/share/tomcat7/.grails/transmartConfig/
cd ~/transmart
mkdir war-files
cd war-files
curl http://75.124.74.64/wars/transmart.V1.2.4.war --output transmart.war
curl http://75.124.74.64/wars/gwava.V1.2.4.war --output gwava.war
sudo cp *.war /var/lib/tomcat7/webapps/
cd ~/transmart/transmart-data
. ./vars
make -C solr start > ~/transmart/transmart-data/solr.log 2>&1 & 
make -C solr rwg_full_import sample_full_import
cd ~/transmart/transmart-data
sudo -u tomcat7 bash -c 'source vars; make -C R start_Rserve' 
sudo service tomcat7 restart

