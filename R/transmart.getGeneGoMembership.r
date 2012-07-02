
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
# Get a distinct gene list of GeneGo Pathways based on a pathway name.
# Default will be getting all the GeneGo pathway memberships
#-----------------------

transmart.getGeneGoMembership <- function(genegoName = NA)
{
	#Create the connection to the oracle DB.
	tranSMART.DB.connection <- tranSMART.DB.establishConnection()

	#This is the base SQL statement to get GeneGo pathway name and associated genes.
	baseSQLSelectStatement <- gsub("\n", "", "SELECT DISTINCT BMA.BIO_MARKER_NAME AS PATHWAY_NAME, BMB.BIO_MARKER_NAME AS GENE")
	baseSQLTableStatement <- gsub("\n", "", "FROM BIO_MARKER BMA, BIO_DATA_CORRELATION BDC, BIO_MARKER BMB")
	baseSQLWhereStatement <- "WHERE BMA.BIO_MARKER_ID = BDC.BIO_DATA_ID and BDC.ASSO_BIO_DATA_ID = BMB.BIO_MARKER_ID and BMA.PRIMARY_SOURCE_CODE = 'GENEGO'"
	baseSQLSortStatement <- "order by BMA.BIO_MARKER_NAME, BMB.BIO_MARKER_NAME"
	
	# If a pathway name is specified, restrict the output to the gene list of this pathway
	# Default will be getting all the GeneGo pathway memberships
	
	if(any(!is.na(genegoName)))
	{
		searchWord <- paste("'",toupper(genegoName),"'",sep="",collapse=",")
		baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND UPPER(BMA.bio_marker_name) IN (",searchWord,")",sep="")
	}
	
	#Put together the final query.
	finalQuery <- paste(baseSQLSelectStatement,baseSQLTableStatement,baseSQLWhereStatement,baseSQLSortStatement,sep=" ")	
	
	#Send the query to the server.
	rs <- dbSendQuery(tranSMART.DB.connection, finalQuery)

	#Retrieve the entire data set.
	dataToReturn <- fetch(rs)
	
	#Clear the results object.
	dbClearResult(rs)
	
	#Disconnect from the database.
	dbDisconnect(tranSMART.DB.connection)

	#Return the results.
	dataToReturn
  
}
#-----------------------