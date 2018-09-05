cd /home/bamboo/deposit
if [ -e "gwava.war" ]
then
 echo "found gwava.war"
 DATE=$(date -d "today" +"%Y%m%d%H%M")
 echo "Date = " $DATE
 mv /var/lib/tomcat7/webapps/gwava.war /var/lib/tomcat7/webapps/gwava.$DATE.savewar
 cp gwava.war /var/lib/tomcat7/webapps/gwava.war
 mv gwava.war gwava.$DATE.war
fi
