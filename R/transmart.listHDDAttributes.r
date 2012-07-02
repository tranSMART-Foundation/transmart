
# ********************************************************************************
#   Copyright 2012   Recombinant Data Corp.
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
# ********************************************************************************

#-----------------------
#Get a distinct list of studies based on a study prefix.
#-----------------------
transmart.listHDDAttributes <- function(studyList)
{
	#Create the connection to the oracle DB.
	tranSMART.DB.connection <- tranSMART.DB.establishConnection()

	sampleTypeQuery <- gsub("\n", "", "SELECT DISTINCT SAMPLE_TYPE FROM DE_SUBJECT_SAMPLE_MAPPING WHERE TRIAL_NAME IN (?)")
	tissueTypeQuery <- gsub("\n", "", "SELECT DISTINCT TISSUE_TYPE FROM DE_SUBJECT_SAMPLE_MAPPING WHERE TRIAL_NAME IN (?)")
	timepointQuery <- gsub("\n", "", "SELECT DISTINCT TIMEPOINT FROM DE_SUBJECT_SAMPLE_MAPPING WHERE TRIAL_NAME IN (?)")

	studyList <- paste("UPPER('",studyList,"')",sep="",collapse=",")

	sampleTypeQuery <- gsub("\\?",studyList,sampleTypeQuery)
	tissueTypeQuery <- gsub("\\?",studyList,tissueTypeQuery)
	timepointQuery <- gsub("\\?",studyList,timepointQuery)

	#Send the query to the server.
	rsSample <- dbSendQuery(tranSMART.DB.connection, sampleTypeQuery)
	rsTissue <- dbSendQuery(tranSMART.DB.connection, tissueTypeQuery)
	rsTimepoint <- dbSendQuery(tranSMART.DB.connection, timepointQuery)
	
	#Retrieve the entire data set.
	sampleData 		<- fetch(rsSample)
	tissueData 		<- fetch(rsTissue)
	timePointData 	<- fetch(rsTimepoint)
	
	#Clear the results object.
	dbClearResult(rsSample)
	dbClearResult(rsTissue)
	dbClearResult(rsTimepoint)
	
	#Disconnect from the database.
	dbDisconnect(tranSMART.DB.connection)	
	
	#Return the results.
	list(sampleData,tissueData,timePointData)
}
#-----------------------