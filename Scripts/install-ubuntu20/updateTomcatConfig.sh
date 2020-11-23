# edit /etc/default/tomcat replace with '-Xmx128m' with '-Xms512m -Xmx2g'
# in JAVA_OPTS="-Djava.awt.headless=true -Xmx128m -XX:+UseConcMarkSweepGC"
# unless it is already there
if ! (grep "mx2g" /etc/default/tomcat9); then
    sudo cp /etc/default/tomcat9 /etc/default/tomcat9-backup-$(date +%s)
    tempFileName="tempFile$(date +%s).txt"
    sed "s/-Xmx128m/-Xms512m -Xmx2g/g" "/etc/default/tomcat9" > "$tempFileName"
    sudo rm "/etc/default/tomcat9"
    sudo cp "$tempFileName" "/etc/default/tomcat9"
    sed "s/#JAVA_HOME/JAVA_HOME" "/etc/default/tomcat9" > "$tempFileName"
    sudo rm "/etc/default/tomcat9"
    sudo cp "$tempFileName" "/etc/default/tomcat9"
    rm $tempFileName
fi
