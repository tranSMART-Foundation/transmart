# Copyright 2014, 2015 The Hyve B.V.
# Copyright 2014 Janssen Research & Development, LLC.
#
# This file is part of tranSMART R Client: R package allowing access to
# tranSMART's data via its RESTful API.
#
# This program is free software: you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation, either version 3 of the License, or (at your
# option) any later version, along with the following terms:
#
#   1. You may convey a work based on this program in accordance with
#      section 5, provided that you retain the above notices.
#   2. You may convey verbatim copies of this program code as you receive
#      it, in any medium, provided that you retain the above notices.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
# Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program. If not, see <http://www.gnu.org/licenses/>.

# This file contains some basic demo commands

# Example steps to authenticate with, connect to, and retrieve data from tranSMART
# load package
require("transmartRClient")

# Connect to the Hyve's demo transmart instance with the connectToTransmart function.
#   You can log in as a guest user with username "user" and password "user". 
#   Occasionally, access to this demo server will be temporarily blocked when a demo is being given, in which case you cannot log in with these credentials. 
#   In that case: try again later. 
#   If you are not able to login for a persistent time, you can email us for help at: <<SOME_ADRESS>
connectToTransmart("http://transmart-demo.thehyve.net/transmart")

#Alternatively, you can try to log in to the demo server of the transmart foundation.
#   We cannot, however, guarantee that this server will be running, that it has the right tranSMART version and which data will be present there
#   Always check what the latest adress is of the transmart demo server and if this matches with the adress filled in in the connectToTransmart function below. 
#   You can look this up at the foundation's website http://transmartfoundation.org - Go to "PLATFORM" in the menu on top of the page and 
#   follow the link(s) to the transmart foundation demo instance.
connectToTransmart("http://75.124.74.64/transmart")

# retrieve a list of the available studies in the database:
studies <- getStudies()
print(studies)

# to access the studies programmatically use for example: 
# study<-studies$id[1]
# for the examples below we will use GSE8581, a study that aimed to identify biomarkers for COPD
study <- "GSE8581"  

# Retrieve Clinical Data
allObservations <- getObservations(study, as.data.frame = T)

#the clinical data is stored in three tables: 
# - an observation table with the clinical measurements
# - a subjectInfo table which contains information about the subjects/patients, such as their age or sex
# - and a table with information about the variables/concepts and where they can be 
#    found in the data tree  of tranSMART (see the panel "Navigate terms" in the "Analyze" tab of the 
#    tranSMART web app: http://transmart-demo.thehyve.net/transmart/datasetExplorer/index ) 
summary(allObservations)
print(allObservations[[1]][1:12,1:7]) #hint: if you are using RStudio you can also use the function "View" to see the data in a more user-friendly table, e.g.: View(allObservations[[1]])
print(allObservations[[2]][1:12,])
print(allObservations[[3]][1:10,])

# retrieve information about the concepts that are present for this study
concepts <- getConcepts(study)
print(concepts)  

# You can use the concept names or the concept links to only retrieve data for a subset of the variables:

# retrieve observations for the first concept which conceptname contains "Lung Disease"
observations <- getObservations(study, concept.match = "Lung Disease", as.data.frame = T)
print(observations$observations)

# retrieve observations belonging to the first two concepts by using the api.links contained in the getConcepts-result
observations <- getObservations(study, concept.links = concepts$api.link.self.href[c(1,2)])
observations$observations[1:10,] 


# if a concept contains high dimensional data, use the following command to obtain this data. 
# NB: you will be told that you need to select one of the listed projections. For the full command, 
# see next line below. 
getHighdimData(study.name = study, concept.match = "Lung")

# As noted above, you will be told that one of the listed projections needs to be selected. 
# The following will return the actual data, log transformed.
dataDownloaded <- getHighdimData(study.name = "GSE8581", concept.match = "Lung", projection = "log_intensity")

# getHighDimData returns a list containing two objects: 
# a data.frame containing the data, and a hash which maps probe names (labels) to Biomarker (e.g. gene) names
summary(dataDownloaded)

#View the data
dataDownloaded[["data"]][1:10,1:10]

#The hash will return the name of the bioMarker when it is supplied with the probe name 
# (note: R automatically prepends "X" in front of column names that start with a numerical value. 
# Therefore probe "1562446_at" will be found back in the data as "X1562446_at")
dataDownloaded[["labelToBioMarkerMap"]]["1562446_at"] 


#select gene expression data
data<-dataDownloaded[[1]]
data[1:4,1:10]
expression_data<-data[,-c(1:6)]
dim(expression_data)
rownames(expression_data)<-data$patientId

#Make a heatmap
#If the dimensions of the expression_data table are large, you may want to create a subset of the data first. 

# in this case we use the following probelist as a subset for the probes:
# (extracted from 1: "Bhattacharya S., Srisuma S., Demeo D. L., et al.,  Molecular biomarkers for quantitative and discrete COPD phenotypes.
#  American Journal of Respiratory Cell and Molecular Biology. 2009;40(3):359–367. doi: 10.1165/rcmb.2008-0114OC.")
probeNames<- c("1552622_s_at","1555318_at","1557293_at","1558280_s_at","1558411_at","1558515_at","1559964_at","204284_at","205051_s_at","205528_s_at","208835_s_at","209377_s_at","209815_at","211548_s_at","212179_at","212263_at","213156_at","213269_at","213650_at","213878_at","215359_x_at","215933_s_at","218352_at","218490_s_at","220094_s_at","220906_at","220925_at","222108_at","224711_at","225318_at","225595_at","225835_at","225892_at","226316_at","226492_at","226534_at","226666_at","226800_at","227095_at","227105_at","227148_at","227812_at","227852_at","227930_at","227947_at","228157_at","228630_at","228665_at","228760_at","228850_s_at","228875_at","228963_at","229111_at","229572_at","230142_s_at","230986_at","232014_at","235423_at","235810_at","238712_at","238992_at","239842_x_at","239847_at","241936_x_at","242389_at")
probeNames<- paste("X", probeNames, sep = "") #note: R automatically prepends "X" in front of column names that start with a numerical value. Therefore prepend "X"

# select only the cases and controls (excluding the patients for which the lung disease is not specified). Note: in the observation table the database IDs 
# are used to identify the patients and not the patient IDs that are used in the gene expression dataset
cases <- allObservations$observations$subject.id[ allObservations$observations$'Subjects_Lung Disease' == "chronic obstructive pulmonary disease"]
controls <- allObservations$observations$subject.id[allObservations$observations$'Subjects_Lung Disease' == "control"]

# if you are using the Transmart Foundation's instance, you might need to run the following instead:
  #cases <- allObservations$observations$subject.id[!is.na(allObservations$observations$'Subjects_Lung Disease_chronic obstructive pulmonary disease')]
  #controls <- allObservations$observations$subject.id[!is.na(allObservations$observations$'Subjects_Lung Disease_control')]


# now we have the database IDs for the patients, but we need to get the patient IDs. These can be retrieved from the subjectInfo table: 
subjectInfo <- allObservations$subjectInfo
patientIDsCase    <- subjectInfo$subject.inTrialId[ subjectInfo$subject.id %in% cases ] 
patientIDsControl <- subjectInfo$subject.inTrialId[ subjectInfo$subject.id %in% controls] 

patientSet <- c(patientIDsCase,patientIDsControl)

patientSet<-patientSet[which(patientSet %in% rownames(expression_data))]
#make a subset of the data based on the selected patientSet and the probelist, and transpose the table so that the rows now contain probe names
subset<-t(expression_data[patientSet,probeNames]) 

#for ease of recognition: append "Case" and "Control" to the patient names
colnames(subset)[colnames(subset)%in% patientIDsCase] <- paste(colnames(subset)[colnames(subset)%in% patientIDsCase],"Case", sep="_" )
colnames(subset)[colnames(subset)%in% patientIDsControl] <- paste( colnames(subset)[colnames(subset)%in% patientIDsControl] , "Control",sep= "_")

dim(subset)
heatmap(as.matrix(subset), scale = "row")

#there is one patient that seems to be an outlier: GSE8581GSM212810_Case
subset_without_outlier <- subset[,colnames(subset)!= "GSE8581GSM212810_Case"]
heatmap(as.matrix(subset_without_outlier), scale = "row")
