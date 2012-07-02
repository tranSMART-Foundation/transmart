
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
#Retrieve GEX data based on some passed in parameters.
#-----------------------
transmart.getGEXData <- function(
  study.list			=NA,
  gene.list				=NA,
  pathway				=NA,
  signature				=NA,
  patient.list			=NA,
  sample.type.list 		=NA,
  tissue.type.list 		=NA,
  timepoint.list 		= NA,
  platform.list			= NULL,
  probe.list			= NULL,
  platform.removeOnOverlap = NULL,
  show.genes			= FALSE,
  print.statement 		= FALSE,
  data.pivot			= TRUE,
  data.pivot.aggregate	= NULL,
  data.pivot.patient_id = FALSE
)
{

	#Since the partition on the microarray table isn't JUST the study name we need to pull the proper name from the DB. Create the list of partitions here based on the study list.
	partitionList <- tranSMART.getPartitionList(study.list)

	#Create the connection to the oracle DB.
	tranSMART.DB.connection <- tranSMART.DB.establishConnection()

	#If the study list and the patient list are empty, throw an error.
	if(any(is.na(study.list)) && any(is.na(patient.list))) stop("You must provide either a study list or a patient list to run a query.")
	
	#Don't allow multiple gene list filters.
	if((!is.na(pathway) && !is.na(signature)) || (!is.na(pathway) && any(!is.na(gene.list))) || (!is.na(signature) && any(!is.na(gene.list))))
	{
		stop("You cannot filter by a pathway/signature AND a gene list.")
	}

	#This is the base SQL statement to get microarray data, Column section only.
	baseSQLSelectStatement <- gsub("\n", "", "
                    a.PATIENT_ID, 
                    a.RAW_INTENSITY, 
                    a.ZSCORE, 
                    a.LOG_INTENSITY, 
                    a.assay_id,
                    ssm.subject_id,
                    ssm.sample_type,
                    ssm.timepoint,
                    ssm.tissue_type,
                    ssm.trial_name,
                    ssm.GPL_ID,
                    b.probe_id, 
                    b.probeset_id")

	#This is the base of the table clauses.
	baseSQLTableStatement <- gsub("\n", "", "
            FROM deapp.de_subject_microarray_data a 
            INNER JOIN de_subject_sample_mapping ssm ON ssm.assay_id = A.assay_id")
	
	baseSQLWhereStatement <- ""
	
	#We either filter by a patient list or a study list.
	if(any(!is.na(study.list)))
	{
		baseSQLWhereStatement <- gsub("\n", "", " 
                                                  WHERE SSM.trial_name IN (?1)
                                                  AND a.trial_source in (?2)")
		
		studyListString <- paste("UPPER('",study.list,"')",sep="",collapse=",")
		
		studyListStringIndex <- paste("'",partitionList$PART,"'",sep="",collapse=",")
		
		baseSQLWhereStatement <- gsub("\\?1",studyListString,baseSQLWhereStatement)
		
		baseSQLWhereStatement <- gsub("\\?2",studyListStringIndex,baseSQLWhereStatement)

	}
	else
	{
		baseSQLWhereStatement <- " WHERE SSM.patient_id IN (?) "
		
		patientListString <- paste("'",patient.list,"'",sep="",collapse=",")
		
		baseSQLWhereStatement <- gsub("\\?",patientListString,baseSQLWhereStatement)
	}
	
	#If a pathway was included, append more info to the query here.
	if(!is.na(pathway) || any(!is.na(gene.list)) || any(!is.null(probe.list)))
	{
		#Add a SELECT DISTINCT to the select statement.
		baseSQLSelectStatement <- paste("SELECT DISTINCT ",baseSQLSelectStatement,sep=" ")
		
		if(show.genes == TRUE)
		{
			#Add the gene columns to the select.
			baseSQLSelectStatement <- paste(baseSQLSelectStatement,", b.GENE_SYMBOL, b.GENE_ID ",sep= " ")
		}
		
		#Add the table joins.
		newTableJoins <- gsub("\n", "", "
        INNER JOIN deapp.de_mrna_annotation b ON a.probeset_id = b.probeset_id 
        INNER JOIN bio_marker bm ON bm.PRIMARY_EXTERNAL_ID = to_char(b.GENE_ID)
        INNER JOIN bio_marker_correl_mv sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id
        INNER JOIN search_keyword sk ON sk.bio_data_id = sbm.bio_marker_id
        INNER JOIN SEARCH_KEYWORD_TERM skt ON sk.SEARCH_KEYWORD_ID = skt.SEARCH_KEYWORD_ID ")
		
		baseSQLTableStatement <- paste(baseSQLTableStatement,newTableJoins,sep="")
		
		#Create the string of genes or pathways.
		if(!is.na(pathway))
		{
			searchWords <- paste("UPPER('",pathway,"')",sep="",collapse=",")
			baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND skt.KEYWORD_TERM IN (",searchWords,")",sep="")
		}
		else if(any(!is.na(gene.list)))
		{
			searchWords <- paste("UPPER('",gene.list,"')",sep="",collapse=",")
			baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND skt.KEYWORD_TERM IN (",searchWords,")",sep="")
		}
		else if(any(!is.null(probe.list)))
		{
			searchWords <- paste("'",probe.list,"'",sep="",collapse=",")
			baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND b.PROBE_ID IN (",searchWords,")",sep="")
		}
		
	}
	else if(!is.na(signature))
	{
		#Add a SELECT DISTINCT to the select statement.
		baseSQLSelectStatement <- paste("SELECT DISTINCT ",baseSQLSelectStatement,sep=" ")
		
		if(show.genes == TRUE)
		{
			#Add the gene columns to the select.
			baseSQLSelectStatement <- paste(baseSQLSelectStatement,", b.GENE_SYMBOL, b.GENE_ID ",sep= " ")
		}
		
		#Add the table joins.
		newTableJoins <- gsub("\n", "", "
        INNER JOIN 
          (
               SELECT              CASE
                                        WHEN bfg.PROBESET_ID IS NOT NULL THEN bfg.PROBESET_ID
                                        WHEN bbm.PROBESET_ID IS NOT NULL THEN bbm.PROBESET_ID                              
                                   END PROBESET_ID,
                                   CASE
                                        WHEN bfg.PROBESET_ID IS NOT NULL THEN bfg.PROBE_ID
                                        WHEN bbm.PROBESET_ID IS NOT NULL THEN bbm.PROBE_ID                              
                                   END PROBE_ID,          
                                   CASE
                                        WHEN bfg.PROBESET_ID IS NOT NULL THEN bfg.GENE_SYMBOL     
                                        WHEN bbm.PROBESET_ID IS NOT NULL THEN bbm.GENE_SYMBOL                                   
                                   END GENE_SYMBOL                                        
               FROM          SEARCH_KEYWORD_TERM skt
               INNER JOIN search_keyword sk ON sk.SEARCH_KEYWORD_ID = skt.SEARCH_KEYWORD_ID
               INNER JOIN SEARCH_GENE_SIGNATURE SGS ON SGS.SEARCH_GENE_SIGNATURE_ID = sk.bio_data_id
               INNER JOIN SEARCH_GENE_SIGNATURE_ITEM SGSI ON SGS.SEARCH_GENE_SIGNATURE_ID = SGSI.SEARCH_GENE_SIGNATURE_ID
               LEFT JOIN bio_assay_feature_group fg ON fg.bio_assay_feature_group_id = SGSI.bio_assay_feature_group_id
               LEFT JOIN deapp.de_mrna_annotation bfg ON bfg.PROBE_ID = fg.FEATURE_GROUP_NAME
               LEFT JOIN bio_marker bm ON bm.bio_marker_id = SGSI.bio_marker_id
               LEFT JOIN deapp.de_mrna_annotation bbm ON to_char (bbm.GENE_ID) = bm.PRIMARY_EXTERNAL_ID
               WHERE          skt.KEYWORD_TERM IN (?) 
          ) b ON a.PROBESET_ID = b.PROBESET_ID  ")
		
		baseSQLTableStatement <- paste(baseSQLTableStatement,newTableJoins,sep="")
		
		#Create the string of genes or pathways.
		searchWords <- paste("UPPER('",signature,"')",sep="",collapse=",")
		
		#Add the where clauses.
		baseSQLTableStatement <- gsub("\\?",searchWords,baseSQLTableStatement)
	
	}
	else
	{
		#Add a SELECT DISTINCT to the select statement.
		baseSQLSelectStatement <- paste("SELECT DISTINCT ",baseSQLSelectStatement,sep=" ")	
		newTableJoins <- " INNER JOIN deapp.de_mrna_annotation b ON a.probeset_id = b.probeset_id "
		baseSQLTableStatement <- paste(baseSQLTableStatement,newTableJoins,sep="")
	}
	
	#We can add the 3 SSM filters at the end of the query here if they were passed in.
	if(any(!is.na(sample.type.list)))
	{
		sampleList <- paste("'",sample.type.list,"'",sep="",collapse=",")
	
		baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND ssm.sample_type IN (",sampleList,")",sep="")
	}
	
	if(any(!is.na(tissue.type.list)))
	{
		tissueList <- paste("'",tissue.type.list,"'",sep="",collapse=",")
	
		baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND ssm.tissue_type IN (",tissueList,")",sep="")
	}	
	
	if(any(!is.na(timepoint.list)))
	{
		timepointList <- paste("'",timepoint.list,"'",sep="",collapse=",")
	
		baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND ssm.timepoint IN (",timepointList,")",sep="")
	}	
	
	if(any(!is.null(platform.list)))
	{
		platformlist <- paste("'",platform.list,"'",sep="",collapse=",")
	
		baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND ssm.GPL_ID IN (",platformlist,")",sep="")	
	}
	
	#Put together the final query.
	finalQuery <- paste(baseSQLSelectStatement,baseSQLTableStatement,baseSQLWhereStatement,sep=" ")
	
	#Print the SQL statement if flag was set.
	if(print.statement == TRUE)
	{
		print("Statement to be run : ")
		print(finalQuery)
		return()
	}
	
	print("Sending GEX Query.")
	
	#Send the query to the server.
  	rs <- dbSendQuery(tranSMART.DB.connection, finalQuery)
	
	print("Retrieving GEX Records.")
	
	#Retrieve the entire data set.
  	dataToReturn <- fetch(rs)
	
	#Clear the results object.
	dbClearResult(rs)
	
	#Disconnect from the database.
	dbDisconnect(tranSMART.DB.connection)	
	
	#Warn the user if two platforms were returned.
	if(length(unique(dataToReturn$GPL_ID)) > 1)
	{
		print("Warning! Multiple platforms being returned.")
	}
	
	#If the user wants the data pivoted, do so here.
	if(data.pivot == TRUE)
	{
		library(reshape2)
    
		if(data.pivot.patient_id == TRUE)
		{
			pivotPatient = "PATIENT_ID"
		}
		else
		{
			pivotPatient = "SUBJECT_ID"
		}
		
		print("Trimming columns.")
		#These are the column we pull from the GEX dataframe.
		relevantColumns <- c(pivotPatient,'LOG_INTENSITY','PROBE_ID','GPL_ID')
		
		#This is the formula we use to pivot the GEX data.
		pivotFormula <- paste("GPL_ID + PROBE_ID ~ ",pivotPatient)
		
		#If the user wants a gene column add the column to the list of columns to pull and the GENE_SYMBOL to the pivot.
		if(show.genes == TRUE)
		{
			relevantColumns <- c(relevantColumns,'GENE_SYMBOL')
			pivotFormula <- paste("GENE_SYMBOL + GPL_ID + PROBE_ID ~ ",pivotPatient)
		}
		
		#We need to Pivot the data so we have probe id, chip, probe id, then each patient's GEX data.
		#Cut down the results to the relevant columns.
		dataToReturn <- dataToReturn[relevantColumns] 

		print("Pivoting data.")
		
		if(!is.null(data.pivot.aggregate))
		{
			print("WARNING! About to use an aggregation when pivoting data. Do not use this option unless you are aware of the reason for duplicate information.")
			dataToReturn <- dcast(dataToReturn, as.formula(pivotFormula), value.var = 'LOG_INTENSITY',fun.aggregate = data.pivot.aggregate) 			
		}
		else
		{
			#Pivot the trimmed data.
			dataToReturn <- dcast(dataToReturn, as.formula(pivotFormula), value.var = 'LOG_INTENSITY') 			
		}
	}
	
	#If we want to remove platforms on probe overlap, do it here.
	if(any(!is.null(platform.removeOnOverlap)))
	{
		#For any duplicate probes on different platforms we need to always use one value over another.
		#Find the probes that are in both platforms.
		probeData <- data.frame(table(dataToReturn$PROBE_ID))
		
		colnames(probeData) <- c('PROBE_ID','Freq')
		
		#Delete the records from this table with only 1 record.
		probeData <- probeData[which(probeData$Freq > 1),]
		
		#In our final data we remove any records that have a probe in the probeData table, and are the GPL we want to remove.
		dataToReturn <- dataToReturn[which(!(dataToReturn$PROBE_ID %in% probeData$PROBE_ID & dataToReturn$GPL_ID %in% platform.removeOnOverlap)),]

	}
	
	dataToReturn
}

tranSMART.getPartitionList <- function(study.list)
{
	#Create the connection to the oracle DB.
	tranSMART.DB.connection <- tranSMART.DB.establishConnection()
	
	baseSQLSelectStatement <- "select distinct OMIC_SOURCE_STUDY||':'||source_cd from de_subject_sample_mapping WHERE trial_name in (?)"
	
	studyListString <- paste("UPPER('",study.list,"')",sep="",collapse=",")
	
	baseSQLSelectStatement <- gsub("\\?",studyListString,baseSQLSelectStatement)
	
	print("Sending Partition Query.")
	
	#Send the query to the server.
  	rs <- dbSendQuery(tranSMART.DB.connection, baseSQLSelectStatement)
	
	print("Retrieving Partition Records.")
	
	#Retrieve the entire data set.
  	dataToReturn <- fetch(rs)
	
	colnames(dataToReturn) <- c('PART')
	
	#Clear the results object.
	dbClearResult(rs)
	
	#Disconnect from the database.
	dbDisconnect(tranSMART.DB.connection)		
	
	return(dataToReturn)
	
}



