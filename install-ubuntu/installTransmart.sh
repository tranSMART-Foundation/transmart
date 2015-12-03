scriptBase=dirname $0
checkBase=$scriptBase/checks

pushd $HOME
mkdir $HOME/transmart
cd $HOME/transmart

echo "++++++++++++++++++++++++++++"
echo "+  Running the install of basic tools
echo "++++++++++++++++++++++++++++"

sudo apt-get install -y curl
sudo apt-get install -y ant
sudo apt-get install -y maven
sudo apt-get install -y vim
sudo apt-get install -y tomcat7 
sudo service tomcat7 stop
curl -s get.sdkman.io | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install grails 2.3.11
sdk install groovy 2.4.5
cd $HOME/transmart/
curl https://codeload.github.com/tranSMART-Foundation/transmart-data/zip/release-1.2.4 -o transmart-data-release-1.2.4.zip
unzip transmart-data-release-1.2.4.zip
mv transmart-data-release-1.2.4 transmart-data
cd $HOME/transmart/transmart-data
sudo make -C env ubuntu_deps_root
make -C env ubuntu_deps_regular
rm $HOME/transmart/transmart-data/env/groovy

echo "++++++++++++++++++++++++++++"
echo "+  Checking the install of basic tools
echo "++++++++++++++++++++++++++++"

cd $checkBase
./basics.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "** Some problem with the bascis tools check"
	echo "************"
fi

cd $checkBase
./checkVersions.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "** Some problem with the versions check"
	echo "************"
fi


echo "++++++++++++++++++++++++++++"
echo "+  Running the install of R and R packages
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
. ./vars
make -C R install_packages
echo "export PATH=${HOME}/transmart/transmart-data/R/root/bin:$PATH" > Rpath.sh
sudo mv Rpath.sh /etc/profile.d/
source /etc/profile.d/Rpath.sh

echo "++++++++++++++++++++++++++++"
echo "+  Checking the install of R and R packages
echo "++++++++++++++++++++++++++++"

cd $checkBase
./checkR.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "** Some problem with the checks for R"
	echo "************"
fi

echo "++++++++++++++++++++++++++++"
echo "+  Running the install of PostgreSQL database
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
. ./vars
make -j4 postgres
make update_datasets
make -C samples/postgres load_clinical_GSE8581
make -C samples/postgres load_ref_annotation_GSE8581
make -C samples/postgres load_expression_GSE8581

echo "++++++++++++++++++++++++++++"
echo "+  Checking the install of PostgreSQL database
echo "++++++++++++++++++++++++++++"

cd $checkBase
./checkPsql.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "** Some problem with PostgreSQL, psql, or the transmartApp database"
	echo "************"
fi

echo "++++++++++++++++++++++++++++"
echo "+  Running the install of tomcat
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
. ./vars
make -C config install
sudo mkdir -p /usr/share/tomcat7/.grails/transmartConfig/
sudo cp /home/transmart/.grails/transmartConfig/*.groovy /usr/share/tomcat7/.grails/transmartConfig/
cd $HOME/transmart
mkdir war-files
cd war-files
curl http://75.124.74.64/wars/transmart.V1.2.4.war --output transmart.war
curl http://75.124.74.64/wars/gwava.V1.2.4.war --output gwava.war
sudo cp *.war /var/lib/tomcat7/webapps/

echo "++++++++++++++++++++++++++++"
echo "+  Checking the install of tomcat, config files, and other files and dirs
echo "++++++++++++++++++++++++++++"

cd $checkBase
./checkFiles.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "** Some problem with the required files and folders check"
	echo "************"
fi

echo "++++++++++++++++++++++++++++"
echo "+  Starting the tranSMART tools, processes
echo "++++++++++++++++++++++++++++"

cd $HOME/transmart/transmart-data
. ./vars
make -C solr start > ~/transmart/transmart-data/solr.log 2>&1 & 
make -C solr rwg_full_import sample_full_import
cd $HOME/transmart/transmart-data
sudo -u tomcat7 bash -c 'source vars; make -C R start_Rserve' 
sudo service tomcat7 restart

echo "++++++++++++++++++++++++++++"
echo "+  Checking the tranSMART tools, processes, and web sites
echo "++++++++++++++++++++++++++++"

cd $checkBase
./checkTools.sh
if (( $returnValue )); then
	echo "************"
	echo "** Some problem with the required tool processes"
	echo "************"
fi

cd $checkBase
./checkWeb.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "** Some problem with the required web sites"
	echo "************"
fi

popd