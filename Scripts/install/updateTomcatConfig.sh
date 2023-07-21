#!/bin/bash

tomcatprotected=0
case $TMINSTALL_OS in
    ubuntu)
	case $TMINSTALL_OSVERSION in
	    18.04 | 18)
		tomcatdefault="/etc/default/tomcat8"
		;;
	    20.04 | 20 | 22.04 | 22)
		tomcatdefault="/etc/default/tomcat9"
		tomcatservice="tomcat9"
		tomcatservicedef="/lib/systemd/system/tomcat9.service"
		tomcatprotected=1
		;;
	esac
esac

# edit /etc/default/tomcatN replace with '-Xmx128m' with '-Xms512m -Xmx2g'
# in JAVA_OPTS="-Djava.awt.headless=true -Xmx128m -XX:+UseConcMarkSweepGC"
# unless it is already there
if ! (grep "Xmx2g" $tomcatdefault); then
    sudo cp $tomcatdefault ${tomcatdefault}-backup-$(date +%s)
    tempFileName="tempFile$(date +%s).txt"
    sed "s/-XX:\+UseConcMarkSweepGC/-Xms512m -Xmx2g -XX:\+UseConcMarkSweepGC/g" "$tomcatdefault" > "$tempFileName"
    sudo rm "$tomcatdefault"
    sudo cp "$tempFileName" "$tomcatdefault"
    rm $tempFileName
fi

# edit /etc/default/tomcatN replace with '-Xmx128m' with '-Xms512m -Xmx2g'
# in JAVA_OPTS="-Djava.awt.headless=true -Xmx128m -XX:+UseConcMarkSweepGC"
# unless it is already there
if ! (grep "^JAVA_HOME=" $tomcatdefault); then
    sudo cp $tomcatdefault ${tomcatdefault}-backup-$(date +%s)
    tempFileName="tempFile$(date +%s).txt"
    sed "s/^#JAVA_HOME=\/usr\/lib\/jvm\/java-8-openjdk/JAVA_HOME=\/usr\/lib\/jvm\/java-8-openjdk-amd64/g" "$tomcatdefault" > "$tempFileName"
    sudo rm "$tomcatdefault"
    sudo cp "$tempFileName" "$tomcatdefault"
    rm $tempFileName
fi

if [ $tomcatprotected == 1 && ! (grep "^ProtectSystem=false" $tomcatservicedef) ]; then
    echo "Fix $tomcatservice.service definition to share paths for rserve, jobs, etc"
    sudo cp $tomcatservicedef ${tomcatservicedef}-backup-$(date +%s)
    tempFileName="tempFile$(date +%s).txt"
    sed "s/^ProtectSystem=strict/ProtectSystem=false/g" "$tomcatservicedef" > "$tempFileName"
    sudo rm "$tomcatservicedef"
    sudo cp "$tempFileName" "$tomcatservicedef"
    rm $tempFileName
    sudo systemctl daemon-reload
    echo "$tomcatservice.service reloaded"
    sudo systemctl restart $tomcatservice
fi

# Consider enabling AJP 8009 in /etc/tomcatN/server.xml

#    <!-- Define an AJP 1.3 Connector on port 8009 -->
# uncomment here
#    <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
# and here
