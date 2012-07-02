
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
#This method will retrieve a list of concepts and their associated patient counts.
#studyList - List of studies to find the concepts in.
#pathMatchList - A list of strings that represent the concept paths to be matched. The search is case sensitive and looks on both sides of the string.
#-----------------------
transmart.getDistinctConcepts <- function(studyList = NULL,pathMatchList)
{

	#Create the connection to the oracle DB.
	tranSMART.DB.connection <- tranSMART.DB.establishConnection()

	#Our base SQL Query.
	baseQuery <- gsub("\n", "", "SELECT	CD.CONCEPT_CD,
										CD.CONCEPT_PATH,
										CD.SOURCESYSTEM_CD STUDYCODE,
										COUNT(1)
							FROM	CONCEPT_DIMENSION CD
							INNER JOIN OBSERVATION_FACT OBSF ON OBSF.CONCEPT_CD = CD.CONCEPT_CD
							WHERE 
                   ")
	
	#If we have a study list add it to the query here.
	if(!is.null(studyList))
	{
		baseQuery <- paste(baseQuery," CD.SOURCESYSTEM_CD IN (?) AND ")
		
		#Collapse the study list to a comma separated string.  
		studyList <- paste("UPPER('",studyList,"')",sep="",collapse=",")

		#Substitute the study string for the question mark.
		baseQuery <- gsub("\\?",studyList,baseQuery)
	}
				   
	#Add the filters for the list of concepts.
	conceptFilter <- paste(paste(" upper(CD.CONCEPT_PATH) LIKE upper('%",pathMatchList,"%')",sep=""),collapse=" OR ")
	conceptFilter <- paste(" (",conceptFilter,")",sep="")
	
	#Add the group by clause so we can get a count of patients for each concept.
	groupByQuery <- gsub("\n", "", "
				   GROUP BY 	CD.CONCEPT_PATH,
								CD.CONCEPT_CD,
								CD.SOURCESYSTEM_CD
					")
		
	baseQuery <- 	paste(baseQuery,conceptFilter,groupByQuery,sep="")   

	#Send the query to the server.
	rs <- dbSendQuery(tranSMART.DB.connection, baseQuery)

	#Retrieve the entire data set.
	dataToReturn <- fetch(rs)

	#Clear the results object.
	dbClearResult(rs)
	
	#Disconnect from the database.
	dbDisconnect(tranSMART.DB.connection)		
	
	dataToReturn
  
}
#-----------------------