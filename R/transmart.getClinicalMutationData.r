
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
#Retrieve a data frame that has patient and mutation information.
#-----------------------
transmart.getClinicalMutationData <- function(study.list,gene.list,trimLength = 4)
{ 
  
  #Get a distinct list of patients for this study.
  patientList <- transmart.getPatientMapping(study.list)
  
  #I couldn't find an R savvy way to merge data frames dynamically. Loop through each of the genes and do a merge after retrieving the data.
  for (gene in gene.list) 
  {
    
    geneDataFrame <- getClinicalMutationDataPerGene(gene,study.list,trimLength)
    patientList <- merge(patientList,geneDataFrame[,c('PATIENT_ID',gene)],by=c('PATIENT_ID'),all.x = TRUE)
	  geneAlleleDataFrame <- getClinicalAlleleFrequencyPerGene(gene,study.list,trimLength)
	  patientList <- merge(patientList,geneAlleleDataFrame[,c('PATIENT_ID',paste(gene,"_Allele_Frequency",sep=""))],by=c('PATIENT_ID'),all.x = TRUE)
  }  
  
  
  return(patientList)
}


getClinicalMutationDataPerGene <- function(gene,study.list,trimLength)
{
  #Create a clause to get the mutation type concept for this gene.
  parameterList <- paste("%Mutations%",gene,"%Mutation Type%",sep="")
  
  #Get the MUTTYPE concept codes.
  mutationConcepts <- transmart.getDistinctConcepts(studyList = study.list,pathMatchList = parameterList)
  
  #Pull the mutation data without pivoting it.  
  mutationDataUnPivot <- transmart.getClinicalData(mutationConcepts$CONCEPT_CD,data.pivot = FALSE)
  
  mutationDataUnPivot$CONCEPT_PATH <- unlist(lapply(strsplit(mutationDataUnPivot$CONCEPT_PATH,split="\\\\"),tailFunction,trimLength))
  
  colnames(mutationDataUnPivot)[1] <- 'PATIENT_ID'
  colnames(mutationDataUnPivot)[4] <- gene
  
  return(mutationDataUnPivot)
  
}

getClinicalAlleleFrequencyPerGene <- function(gene,study.list,trimLength)
{
  #Create a clause to get the mutation type concept for this gene.
  parameterList <- c(paste("%Mutations%",gene,"%Mutant Allele Frequency%",sep=""))
  
  #Get the MUTTYPE concept codes.
  mutationConcepts <- transmart.getDistinctConcepts(studyList = study.list,pathMatchList = parameterList)
  #Pull the mutation data without pivoting it.  
  mutationDataUnPivot <- transmart.getClinicalData(mutationConcepts$CONCEPT_CD,data.pivot = FALSE)
  
  mutationDataUnPivot$CONCEPT_PATH <- unlist(lapply(strsplit(mutationDataUnPivot$CONCEPT_PATH,split="\\\\"),tailFunction,trimLength))
  
  colnames(mutationDataUnPivot)[1] <- 'PATIENT_ID'
  colnames(mutationDataUnPivot)[6] <- paste(gene,"_Allele_Frequency",sep="")
  
  return(mutationDataUnPivot)
  
}

#We use this function to trim concept paths.
tailFunction <- function(vectorList,concepts.trimLengths)
{
  paste(tail(vectorList,concepts.trimLengths),collapse="\\")
}
#-----------------------