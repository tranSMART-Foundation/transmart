# edit /etc/default/tomcat7 replace with '-Xmx128m' with '-Xms512m -Xmx2g'
# in JAVA_OPTS="-Djava.awt.headless=true -Xms512m -Xmx2g -XX:+UseConcMarkSweepGC
"
# unless it is already there
if ! (grep "mx2g" /etc/default/tomcat7); then
    sudo cp /etc/default/tomcat7 /etc/default/tomcat7-backup-$(date +%s)
    tempFileName="tempFile$(date +%s).txt"
    sed "s/-Xmx128m/-Xms512m -Xmx2g/g" "/etc/default/tomcat7" > "$tempFileName"
    sudo rm "/etc/default/tomcat7"
    sudo cp "$tempFileName" "/etc/default/tomcat7"
    rm $tempFileName
fi
