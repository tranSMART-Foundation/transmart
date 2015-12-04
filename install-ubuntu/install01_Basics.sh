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