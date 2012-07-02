
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
#Retrieve a data frame that has the patient_num to subject_id mapping.
#-----------------------
transmart.getPatientMapping <- function(studyList)
{

	#Create the connection to the oracle DB.
	tranSMART.DB.connection <- tranSMART.DB.establishConnection()

	SSMQuery <- gsub("\n", "", "SELECT
                                    PT.TRIAL TRIAL_NAME,
                                    REPLACE(PD.SOURCESYSTEM_CD,PT.TRIAL || ':') SUBJECT_ID,
                                    PD.PATIENT_NUM PATIENT_ID
                    FROM PATIENT_TRIAL PT
                    INNER JOIN PATIENT_DIMENSION PD ON PD.PATIENT_NUM = PT.PATIENT_NUM
                    WHERE PT.TRIAL IN (?)")

	studyList <- paste("UPPER('",studyList,"')",sep="",collapse=",")

	SSMQuery <- gsub("\\?",studyList,SSMQuery)

	#Send the query to the server.
	rs <- dbSendQuery(tranSMART.DB.connection, SSMQuery)

	#Retrieve the entire data set.
	dataToReturn <- fetch(rs)

	#Clear the results object.
	dbClearResult(rs)
	
	#Disconnect from the database.
	dbDisconnect(tranSMART.DB.connection)	
	
	dataToReturn
  
}
#-----------------------