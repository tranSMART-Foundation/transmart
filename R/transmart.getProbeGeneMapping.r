
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
transmart.getProbeGeneMapping <- function(probeIds = NA,geneIds = NA)
{
	#Create the connection to the oracle DB.
	tranSMART.DB.connection <- tranSMART.DB.establishConnection()

	mappingQuery <- gsub("\n", "", "SELECT DISTINCT PROBE_ID, GENE_SYMBOL FROM de_mrna_annotation WHERE ")
	filterQuery <- ""
	
	if(any(is.na(probeIds)) & any(is.na(geneIds)))
	{
		stop('No Probes or Genes specified for lookup.')
	}
	
	if(any(!is.na(probeIds)) & any(!is.na(geneIds)))
	{
		stop('Please only specify a probe ID or a gene ID, not both.')
	}
	
	if(any(!is.na(probeIds)))
	{
		filterQuery <- " PROBE_ID IN (?) "
		
		probeIds <- paste("LOWER('",probeIds,"')",sep="",collapse=",")
		
		filterQuery <- gsub("\\?",probeIds,filterQuery)
	}
	
	if(any(!is.na(geneIds)))
	{
		filterQuery <- " GENE_SYMBOL IN (?) "
		
		geneIds <- paste("UPPER('",geneIds,"')",sep="",collapse=",")
		
		filterQuery <- gsub("\\?",geneIds,filterQuery)	
	}	
	
	mappingQuery <- paste(mappingQuery,filterQuery,sep="")   

	#Send the query to the server.
	rs <- dbSendQuery(tranSMART.DB.connection, mappingQuery)

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