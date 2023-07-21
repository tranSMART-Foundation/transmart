#!/bin/bash

# **************************************************************
# This script checks for tomcat and mods to tomcat configuration
# **************************************************************

# tomcat locations
# ubuntu18 /etc/default/tomcat8 (TOMCAT8_USER, JAVA_HOME, JAVA_OPTS)
#          /etc/tomcat8/ context.xml server.xml tomcat-users.xml
#          
# ubuntu20 tomcat9

case $TMINSTALL_OS in
    ubuntu)
	case $TMINSTALL_OSVERSION in
	    18.04 | 18)
		tomcatuser="tomcat8"
		tomcatservice="tomcat8"
		;;
	    20.04 | 20 | 22.04 | 22)
		tomcatuser="tomcat9"
		tomcatservice="tomcat9"
		;;
	esac
esac

echo "------------------------------------------------------------------------"
echo "|  Checking for tomcat install and modifications to tomcat configuration"
echo "------------------------------------------------------------------------"

if [ ! -e /etc/default/$tomcatuser ] ; then
    echo "It appears that tomcat is not installed"
    echo "Please check install set and repeat test"
    exit 1
fi

sudo service $tomcatservice status | grep -q "servlet engine"
results=$?
if ! [ $results ] ; then
    echo "It appears that tomcat is not installed"
    echo "Please check installation and repeat test"
    exit 1
fi

grep "Xmx2g" /etc/default/$tomcatuser | grep -q "JAVA_OPTS"
results=$?
if ! [ $results ] ; then
    echo "It appears that the tomcat configuration for heap space"
    echo "was not modified; tomcat will not run correctly"
    echo "the heap space in /etc/default/$tomcatuser should be set to -Xmx2g"
    exit 1
fi

echo "-----------------------------------------"
echo "|  tomcat install and configuration is ok"
echo "-----------------------------------------"

exit 0
