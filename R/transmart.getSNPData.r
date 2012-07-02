
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
#Retrieve SNP data based on some passed in parameters.
#-----------------------
transmart.getSNPData <- function(
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
  show.genes			= FALSE,
  print.statement 		= FALSE,
  data.pivot			= TRUE,
  data.CN.pivot.aggregate	= NULL,
  data.GT.pivot.aggregate	= NULL,
  data.pivot.patient_id = FALSE
)
{

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
                        SNP_GENO.SNP_NAME AS SNP,
                        DSM.PATIENT_ID, 
                        DSM.SUBJECT_ID, 
                        DSM.sample_type,
                        DSM.timepoint,
                        DSM.tissue_type,
                        SNP_GENO.SNP_CALLS AS GENOTYPE,
                        SNP_COPY.COPY_NUMBER AS COPYNUMBER,
                        PD.sourcesystem_cd,
                        DSM.GPL_ID")

	#This is the base of the table clauses.
	baseSQLTableStatement <- gsub("\n", "", "
                            FROM DE_SUBJECT_SAMPLE_MAPPING DSM
                            INNER JOIN patient_dimension PD ON DSM.patient_id = PD.patient_num 
                            LEFT JOIN DE_SNP_CALLS_BY_GSM SNP_GENO ON DSM.OMIC_PATIENT_ID = SNP_GENO.PATIENT_NUM AND DSM.SAMPLE_CD = SNP_GENO.GSM_NUM
                            LEFT JOIN DE_SNP_COPY_NUMBER SNP_COPY ON DSM.OMIC_PATIENT_ID = SNP_COPY.PATIENT_NUM AND SNP_GENO.snp_name = SNP_COPY.snp_name
                            ")
	
	baseSQLWhereStatement <- ""
	
	#We either filter by a patient list or a study list.
	if(any(!is.na(study.list)))
	{
		baseSQLWhereStatement <- gsub("\n", "", " 
                                                 WHERE DSM.trial_name IN (?1)")
		
		studyListString <- paste("UPPER('",study.list,"')",sep="",collapse=",")
		
		baseSQLWhereStatement <- gsub("\\?1",studyListString,baseSQLWhereStatement)
		
	}
	else
	{
		baseSQLWhereStatement <- " WHERE DSM.patient_id IN (?) "
		
		patientListString <- paste("'",patient.list,"'",sep="",collapse=",")
		
		baseSQLWhereStatement <- gsub("\\?",patientListString,baseSQLWhereStatement)
	}
	
	#If a pathway was included, append more info to the query here.
	if(!is.na(pathway) || any(!is.na(gene.list)) || any(!is.null(probe.list)))
	{
		#Add a SELECT to the select statement.
		baseSQLSelectStatement <- paste("SELECT ",baseSQLSelectStatement,sep=" ")
		
		if(show.genes == TRUE)
		{
			#Add the gene columns to the select.
			baseSQLSelectStatement <- paste(baseSQLSelectStatement,", bm.BIO_MARKER_NAME AS GENE ",sep= " ")
		}
		
		
		if(!is.na(pathway) || any(!is.na(gene.list)))
		{
			#Add the table joins.
			newTableJoins <- gsub("\n", "", "
			INNER JOIN DE_SNP_GENE_MAP D2 ON D2.SNP_NAME = SNP_GENO.SNP_NAME
			INNER JOIN bio_marker bm ON bm.PRIMARY_EXTERNAL_ID = to_char(D2.ENTREZ_GENE_ID)
			INNER JOIN bio_marker_correl_mv sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id
			INNER JOIN search_keyword sk ON sk.bio_data_id = sbm.bio_marker_id
			INNER JOIN SEARCH_KEYWORD_TERM skt ON sk.SEARCH_KEYWORD_ID = skt.SEARCH_KEYWORD_ID 
			 ")
		}
		else
		{
			newTableJoins <- ""
		}
		
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
			baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND SNP_GENO.SNP_NAME IN (",searchWords,")",sep="")
		}
		
		baseSQLTableStatement <- paste(baseSQLTableStatement,newTableJoins,sep="")
		
	}
	else if(!is.na(signature))
	{
		#Add a SELECT to the select statement.
		baseSQLSelectStatement <- paste("SELECT ",baseSQLSelectStatement,sep=" ")
		
		if(show.genes == TRUE)
		{
			#Add the gene columns to the select.
			baseSQLSelectStatement <- paste(baseSQLSelectStatement,", bm.BIO_MARKER_NAME AS GENE ",sep= " ")
		}
		
		#Add the table joins.
		newTableJoins <- gsub("\n", "", "
                INNER JOIN DE_SNP_GENE_MAP D2 ON D2.SNP_NAME = SNP_GENO.SNP_NAME
                INNER JOIN bio_marker bm ON bm.PRIMARY_EXTERNAL_ID = to_char(D2.ENTREZ_GENE_ID) 
                INNER JOIN search_bio_mkr_correl_fast_mv sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id 
                INNER JOIN search_keyword sk ON sk.bio_data_id = sbm.domain_object_id
                INNER JOIN SEARCH_KEYWORD_TERM skt ON sk.SEARCH_KEYWORD_ID = skt.SEARCH_KEYWORD_ID ")
		
		baseSQLTableStatement <- paste(baseSQLTableStatement,newTableJoins,sep="")
		
		#Create the string of genes or pathways.
		searchWords <- paste("UPPER('",signature,"')",sep="",collapse=",")
		
		#Add the where clauses.
		baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND skt.KEYWORD_TERM IN (",searchWords,")",sep="")
	
	}
	else
	{
		#Add a SELECT to the select statement.
		baseSQLSelectStatement <- paste("SELECT ",baseSQLSelectStatement,sep=" ")	
		
		if(show.genes == TRUE)
		{
			#Add the gene columns to the select.
			baseSQLSelectStatement <- paste(baseSQLSelectStatement,", bm.BIO_MARKER_NAME AS GENE ",sep= " ")
		}		
		
		newTableJoins <- " INNER JOIN DE_SNP_GENE_MAP D2 ON D2.SNP_NAME = SNP_GENO.SNP_NAME
                           INNER JOIN bio_marker bm ON bm.PRIMARY_EXTERNAL_ID = to_char(D2.ENTREZ_GENE_ID) "
		baseSQLTableStatement <- paste(baseSQLTableStatement,newTableJoins,sep="")
	}
	
	#We can add the 3 SSM filters at the end of the query here if they were passed in.
	if(!is.na(sample.type.list))
	{
		sampleList <- paste("'",sample.type.list,"'",sep="",collapse=",")
	
		baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND DSM.sample_type IN (",sampleList,")",sep="")
	}
	
	if(!is.na(tissue.type.list))
	{
		tissueList <- paste("'",tissue.type.list,"'",sep="",collapse=",")
	
		baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND DSM.tissue_type IN (",tissueList,")",sep="")
	}	
	
	if(!is.na(timepoint.list))
	{
		timepointList <- paste("'",timepoint.list,"'",sep="",collapse=",")
	
		baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND DSM.timepoint IN (",timepointList,")",sep="")
	}	
	
	if(any(!is.null(platform.list)))
	{
		platformlist <- paste("'",platform.list,"'",sep="",collapse=",")
	
		baseSQLWhereStatement <- paste(baseSQLWhereStatement," AND DSM.GPL_ID IN (",platformlist,")",sep="")	
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
	
	print("Sending SNP Query.")
	
	#Send the query to the server.
  	rs <- dbSendQuery(tranSMART.DB.connection, finalQuery)
	
	print("Retrieving SNP Records.")
	
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
		#These are the column we pull from the SNP dataframe.
		relevantCNColumns <- c(pivotPatient,'COPYNUMBER','SNP','GPL_ID')
		relevantGTColumns <- c(pivotPatient,'GENOTYPE','SNP','GPL_ID')
		
		#This is the formula we use to pivot the GEX data.
		pivotFormula <- paste("GPL_ID + SNP ~ ",pivotPatient)
		
		#If the user wants a gene column add the column to the list of columns to pull and the GENE to the pivot.
		if(show.genes == TRUE)
		{
			relevantCNColumns <- c(relevantCNColumns,'GENE')
			relevantGTColumns <- c(relevantGTColumns,'GENE')
			
			pivotFormula <- paste("GENE + GPL_ID + SNP ~ ",pivotPatient)
		}
		
		#We need to Pivot the data so we have SNP id, chip, probe id, then each patient's SNP data.
		
		#Cut down the results to the relevant columns.
		dataToReturnCN <- dataToReturn[relevantCNColumns] 
		dataToReturnGT <- dataToReturn[relevantGTColumns] 

		print("Pivoting data.")
		
		#Make sure both aggregation functions are supplied if one is.
		if((!is.null(data.GT.pivot.aggregate) && is.null(data.CN.pivot.aggregate)) || (is.null(data.GT.pivot.aggregate) && !is.null(data.CN.pivot.aggregate)))
		{
			stop("If supplying an aggregation function you must supply both a copy number and genotype aggregation function.")
		}
		
		if(!is.null(data.GT.pivot.aggregate) && !is.null(data.CN.pivot.aggregate))
		{
			print("WARNING! About to use an aggregation when pivoting data. Do not use this option unless you are aware of the reason for duplicate information.")
			dataToReturnCN <- dcast(dataToReturnCN, as.formula(pivotFormula), value.var = 'COPYNUMBER',fun.aggregate = data.CN.pivot.aggregate) 			
			dataToReturnGT <- dcast(dataToReturnGT, as.formula(pivotFormula), value.var = 'GENOTYPE',fun.aggregate = data.GT.pivot.aggregate)
		}
		else
		{
			#Pivot the trimmed data.
			dataToReturnCN <- dcast(dataToReturnCN, as.formula(pivotFormula), value.var = 'COPYNUMBER') 			
			dataToReturnGT <- dcast(dataToReturnGT, as.formula(pivotFormula), value.var = 'GENOTYPE') 			
		}
		
		dataToReturn <- list(dataToReturnCN,dataToReturnGT)
	}
	
	dataToReturn
}