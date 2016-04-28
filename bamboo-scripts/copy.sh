cd /home/bamboo/deposit
if [ -e "transmart.war" ]
then
 echo "found transmart.war"
 DATE=$(date -d "today" +"%Y%m%d%H%M")
 echo "Date = " $DATE
 mv /var/lib/tomcat7/webapps/transmart.war /var/lib/tomcat7/webapps/transmart.$DATE.savewar
 cp transmart.war /var/lib/tomcat7/webapps/transmart.war
 mv transmart.war transmart.$DATE.war
fi
