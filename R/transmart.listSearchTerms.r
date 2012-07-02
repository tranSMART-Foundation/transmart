
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
#This will imitate the search in tranSMART and retrieve the top 20 results that closely match the passed in search term.
#-----------------------
transmart.listSearchTerms <- function(term,category = NA,terms.count)
{

	#Create the connection to the oracle DB.
	tranSMART.DB.connection <- tranSMART.DB.establishConnection()

	#Base SQL Query.
	searchQuery <- gsub("\n", "", "SELECT DISTINCT t.searchKeyword, 
                                                t.keywordTerm, 
                                                t.rank
                                FROM search.SearchKeywordTerm t 
                                WHERE t.keywordTerm LIKE ? || '%' 
				   ")
	
	if(!is.na(category))
	{
		searchQuery <- paste("AND t.searchKeyword.dataCategory IN ('",category,"')")
	}
			
	#WHERE ROWNUM <= 20

	searchQuery <- gsub("\\?",term,searchQuery)

	#Send the query to the server.
	rs <- dbSendQuery(tranSMART.DB.connection, searchQuery)

	#Retrieve the entire data set.
	dataToReturn <- fetch(rs)

	#Clear the results object.
	dbClearResult(rs)
	
	#Disconnect from the database.
	dbDisconnect(tranSMART.DB.connection)	 	
	
	dataToReturn
}
#-----------------------
