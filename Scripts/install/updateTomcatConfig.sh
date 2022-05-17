# edit /etc/default/tomcat8 replace with '-Xmx128m' with '-Xms512m -Xmx2g'
# in JAVA_OPTS="-Djava.awt.headless=true -Xmx128m -XX:+UseConcMarkSweepGC"
# unless it is already there
if ! (grep "Xmx2g" /etc/default/tomcat8); then
    sudo cp /etc/default/tomcat8 /etc/default/tomcat8-backup-$(date +%s)
    tempFileName="tempFile$(date +%s).txt"
    sed "s/-XX:\+UseConcMarkSweepGC/-Xms512m -Xmx2g -XX:\+UseConcMarkSweepGC/g" "/etc/default/tomcat8" > "$tempFileName"
    sudo rm "/etc/default/tomcat8"
    sudo cp "$tempFileName" "/etc/default/tomcat8"
    rm $tempFileName
fi
# edit /etc/default/tomcat8 replace with '-Xmx128m' with '-Xms512m -Xmx2g'
# in JAVA_OPTS="-Djava.awt.headless=true -Xmx128m -XX:+UseConcMarkSweepGC"
# unless it is already there
if ! (grep "^JAVA_HOME=" /etc/default/tomcat8); then
    sudo cp /etc/default/tomcat8 /etc/default/tomcat8-backup-$(date +%s)
    tempFileName="tempFile$(date +%s).txt"
    sed "s/^#JAVA_HOME=\/usr\/lib\/jvm\/java-8-openjdk/JAVA_HOME=\/usr\/lib\/jvm\/java-8-openjdk-amd64/g" "/etc/default/tomcat8" > "$tempFileName"
    sudo rm "/etc/default/tomcat8"
    sudo cp "$tempFileName" "/etc/default/tomcat8"
    rm $tempFileName
fi


# How about enabling AJP 8009 in /etc/tomcat8/server.xml


#    <!-- Define an AJP 1.3 Connector on port 8009 -->
# uncomment here
#    <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
# and here
