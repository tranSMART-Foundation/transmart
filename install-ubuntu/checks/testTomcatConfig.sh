sudo cp /etc/default/tomcat7 /etc/default/tomcat7-backup-$(Date +%s)
# edit /etc/default/tomcat7 replace with '-Xmx128m' with '-Xms512m -Xmx2g'
# in JAVA_OPTS="-Djava.awt.headless=true -Xms512m -Xmx2g -XX:+UseConcMarkSweepGC"
# unless it is already there
tempFileName="tempFile$(Date +%s).txt"
if ! (grep "mx2g" /etc/default/tomcat7); then
	sed "s/-Xmx128m/-Xms512m -Xmx2g/g" "/etc/default/tomcat7" > $tempFileName
	sudo mv $tmpFileName /etc/default/tomcat7
if
