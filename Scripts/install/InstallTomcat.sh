#!/bin/bash

#******************************
#  Script to install tomcat 8/9
#  for tranSMART release 19.1
#******************************

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallTomcat starting"

echo "+++++++++++++++++++++++++++++"
echo "+  08.01 Install Tomcat 8/9 +"
echo "+++++++++++++++++++++++++++++"

case $TMINSTALL_OS in
    ubuntu)
	case $TMINSTALL_OSVERSION in
	    18.04 | 18)
		tomcatuser="tomcat8"
		tomcatinstall="tomcat8"
		tomcatfixuser=0
		;;
	    20.04 | 20 | 22.04 | 22)
		tomcatuser="tomcat"
		tomcatinstall="tomcat9"
		tomcatfixuser=1
		;;
	esac
esac

sudo -v 
cd $HOME
now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Check installed packages: tomcat"

packageInstall $tomcatinstall 

if [ $tomcatfixuser == 1 ]; then
    # while tomcat9 is stopped:
    # For tomcat9 on Ubuntu20,22... we need to define the tomcat user directory
    # which defaults to root!

    sudo usermod $tomcatuser --home /home/$tomcatuser
    sudo mkdir -p /home/$tomcatuser
    sudo chown -R $tomcatuser:$tomcatuser /home/$tomcatuser

fi

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Stop tomcat service to update configuration"

sudo service $tomcatservice stop
$TMSCRIPTS_BASE/updateTomcatConfig.sh

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Copy transmart config files to new tomcat user"

sudo mkdir -p /var/lib/$tomcatuser/.grails/transmartConfig/
sudo cp $HOME/.grails/transmartConfig/*.groovy /var/lib/$tomcatuser/.grails/transmartConfig/
sudo chown -R $tomcatuser:$tomcatuser /var/lib/$tomcatuser/.grails

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} +  Check tomcat install"

cd $TMSCRIPTS_BASE/checks
./checkTomcatInstall.sh
if [ "$( checkInstallError "Tomcat install failed; redo install" )" ] ; then exit -1; fi

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} Restarting tomcat"

now="$(date +'%d-%b-%y %H:%M')"
echo "${now} InstallTomcat done. Finished installing tomcat"

