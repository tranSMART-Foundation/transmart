# edit /etc/default/tomcat8 replace with '-Xmx128m' with '-Xms512m -Xmx2g'
# in JAVA_OPTS="-Djava.awt.headless=true -Xmx128m -XX:+UseConcMarkSweepGC"
# unless it is already there
if ! (grep "mx2g" /etc/default/tomcat8); then
    sudo cp /etc/default/tomcat8 /etc/default/tomcat8-backup-$(date +%s)
    tempFileName="tempFile$(date +%s).txt"
    sed "s/-Xmx128m/-Xms512m -Xmx2g/g" "/etc/default/tomcat8" > "$tempFileName"
    sudo rm "/etc/default/tomcat8"
    sudo cp "$tempFileName" "/etc/default/tomcat8"
    rm $tempFileName
fi
