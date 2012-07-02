
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
transmart.listStudies <- function(studyLike, concept.size = 4, gexFlag = FALSE)
{
	#Create the connection to the oracle DB.
	tranSMART.DB.connection <- tranSMART.DB.establishConnection()

	SSMQuery <- gsub("\n", "", "SELECT 	DISTINCT SOURCESYSTEM_CD STUDYCODE,
										SUBSTR(C_FULLNAME,1,REGEXP_INSTR(C_FULLNAME,'\\\\',1,?)) CONCEPT_PATH
								FROM		I2B2
								WHERE		SOURCESYSTEM_CD LIKE (?)
			")

	#First replace is the number of chunks to remove.
	SSMQuery <- sub("\\?",concept.size,SSMQuery)	

	#Second replace is the study like query to replace.
	studyLike <- paste("UPPER('",studyLike,"')",sep="")
	SSMQuery <- sub("\\?",studyLike,SSMQuery)

	#Send the query to the server.
	rs <- dbSendQuery(tranSMART.DB.connection, SSMQuery)

	#Retrieve the entire data set.
	dataToReturn <- fetch(rs)
	
	#Clear the results object.
	dbClearResult(rs)
	
	#If we want a flag indicate the availability of GEX Data, do the query here.
	if(gexFlag == TRUE)
	{
		#Build the Query to pull the studies and their counts from the microarray table.	
		gexCountQuery <- gsub("\n", "", "		SELECT	COUNT(DISTINCT PROBESET_ID),
														TRIAL_NAME
											FROM		deapp.de_subject_microarray_data                                           
											WHERE		TRIAL_NAME IN (?)
											GROUP BY TRIAL_NAME
				")	

		#Collapse the list of studies so we can add it to the query.
		studyListString <- paste("'",dataToReturn$STUDYCODE,"'",sep="",collapse=",")
		
		#Add the study list to the query.
		gexCountQuery <- gsub("\\?",studyListString,gexCountQuery)
		
		#Send the query to the server.
		rs <- dbSendQuery(tranSMART.DB.connection, gexCountQuery)
		
		countData <- fetch(rs)
		
		#Name the columns for merging.
		colnames(countData) <- c('PROBECOUNT','STUDYCODE')
		
		#Merge the two data frames.
		dataToReturn <- merge(dataToReturn,countData,by=c('STUDYCODE'),all.x = TRUE)
				
	}
	
	#Disconnect from the database.
	dbDisconnect(tranSMART.DB.connection)	
	
	#Return the results.
	dataToReturn
  
}
#-----------------------