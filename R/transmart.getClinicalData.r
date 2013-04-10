
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
#This method will retrieve Clinical data based on a list of concepts.
#concepts.codelist - Concept codes, it is safe to use the column returned from calling getDistinctConcepts.
#data.pivot - Boolean indicating if the data should be pivoted, which makes each row a patient and each column the value for a concept code.
#concepts.prePivotTrim - If you want to trim some of the concept code off before pivoting, set this to true.
#concepts.trimLengths - This describes how much to trim the concept code.
#-----------------------
transmart.getClinicalData <- function(concepts.codelist,data.pivot = TRUE,concepts.prePivotTrim = TRUE,concepts.trimLengths = 4,sql.print = FALSE)
{

	#Create the connection to the oracle DB.
	tranSMART.DB.connection <- tranSMART.DB.establishConnection()

	#Our base SQL Query.
	baseQuery <- gsub("\n", "", "SELECT DISTINCT PT.PATIENT_NUM,
                                REPLACE(PD.SOURCESYSTEM_CD,PT.TRIAL || ':') SUBJECT_ID,
                                PT.TRIAL TRIAL_NAME,
                               CD.CONCEPT_PATH, 
                               CD.CONCEPT_CD, 
                               CD.NAME_CHAR,
                               case OBSF.VALTYPE_CD
                                    WHEN 'T' THEN TVAL_CHAR
                                    WHEN 'N' THEN CAST(NVAL_NUM AS varchar2(30))
                               END VALUE 
                            FROM CONCEPT_DIMENSION CD
                            INNER JOIN OBSERVATION_FACT OBSF ON OBSF.CONCEPT_CD = CD.CONCEPT_CD
                            INNER JOIN PATIENT_TRIAL PT  ON PT.PATIENT_NUM = OBSF.PATIENT_NUM
                            INNER JOIN PATIENT_DIMENSION PD ON PD.PATIENT_NUM = PT.PATIENT_NUM
                            WHERE CD.CONCEPT_CD IN (?)
                   ") 
											   
	#Collapse the concepts into a string.  
	concepts.codelist <- paste("'",concepts.codelist,"'",sep="",collapse=",")
  
	#Substitute the question mark for the list of concepts.
	baseQuery <- gsub("\\?",concepts.codelist,baseQuery)
	
	#Send the query to the server.
	rs <- dbSendQuery(tranSMART.DB.connection, baseQuery)
	
	#Retrieve the entire data set.
	dataToReturn <- fetch(rs)
  
	#Clear the results object.
	dbClearResult(rs)
	
	#Disconnect from the database.
	dbDisconnect(tranSMART.DB.connection)	  
  
	#If we are pivoting the data we need to do some manipulation before returning it to the user.
	if(data.pivot == TRUE)
	{
		#We will use the reshape2 package to do the pivot.
		require(reshape2)
		require(plyr)
		
		#If we are pivoting we need to shorten some of the concept paths. This takes out any concept that has a character value at the end.
		dataToReturn <- adply(dataToReturn,1,transform, CONCEPT_PATH=gsub(paste("\\\\",VALUE,"\\\\$",sep=""), '', CONCEPT_PATH))
		
		#In order to collapse like records from the same study we take off the first few sets "\TEXT\" blocks.
		if(concepts.prePivotTrim)
		{
			if(concepts.trimLengths > 0)
			{
				dataToReturn$CONCEPT_PATH <- sub(pattern=paste("^\\\\(.*?\\\\){",concepts.trimLengths,"}",sep=""),replacement="",x=dataToReturn$CONCEPT_PATH,perl=TRUE)
			}
			else if(concepts.trimLengths < 0)
			{
				#Reverse the sign on the number of items to get from the end.
				concepts.trimLengths <- -1 * concepts.trimLengths
				
				#Yuck. Take the concept path, split it by "\\", make it a something besides a list, take the last X elements (This is the parameter), paste those elements together with a "\\" separator.
				tailFunction <- function(vectorList,concepts.trimLengths)
				{
					paste(tail(vectorList,concepts.trimLengths),collapse="\\")
				}
				
				dataToReturn$CONCEPT_PATH <- unlist(lapply(strsplit(dataToReturn$CONCEPT_PATH,split="\\\\"),tailFunction,concepts.trimLengths))
			}

		}
  
		#Do the pivot.
		dataToReturn <- tryCatch({
      dcast(dataToReturn, PATIENT_NUM + SUBJECT_ID + TRIAL_NAME ~ CONCEPT_PATH, value.var = 'VALUE') 	
		}, message = function(m) { m })

		#Slightly crude way of catching when we have clashing columns...
    if (inherits(dataToReturn, 'message')) {
      if (dataToReturn['message'] == 'Aggregation function missing: defaulting to length\n') {
        stop(paste("||FRIENDLY|| Could not aggregate columns: there is more than one folder with the same name", sep=""))
      }
    }

	}	
  
	dataToReturn	
}
#-----------------------