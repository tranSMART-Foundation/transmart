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
require("transmartRClient")
connectToTransmart("http://75.124.74.46:5880/transmart")

studies <- getStudies()
print(studies)

# to access the studies programmatically use: 
# study<-studies$id[1]
# for the examples below, we will use a specific study:
study <- "GSE8581"

# Retrieve Clinical Data
allObservations <- getObservations(study, as.data.frame = T)
summary(allObservations)
print(allObservations[[1]][1:12,1:7])
print(allObservations[[2]][1:12,])


concepts <- getConcepts(study)
print(concepts) 


# retrieve observations for study 1 for the first concept containing "Lung Disease"
observations <- getObservations(study, concept.match = "Lung Disease", as.data.frame = T)
print(observations$observations)

# retrieve observations belonging to the first two concepts by using the api.links contained in the getConcepts-result
observations <- getObservations(study, concept.links = concepts$api.link.self.href[c(1,2)])
observations$observations[1:10,]


# if a concept contains high dimensional data, use the following command to obtain this data
getHighdimData(study.name = study, concept.match = "Lung")

# you will be told that one of the listed projections needs to be selected. The following will return the actual data.
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

#make a heatmap
subset<-expression_data

#If the dimensions of the expression_data table are large, you may want to create a subset of the data first: 
#e.g. select only the columns and rows for which at least one intensity value > 10
# note: if you do not make a subset when the table is very large, it may take a while to produce the heatmap; 
#       clustering will take a lot of time then
#To make a subset, uncomment the lines below:
#dm1<-apply((expression_data), MARGIN=1, max,na.rm=T)
#dm2<-apply((expression_data), MARGIN=2, max,na.rm=T)
# r1<-which(dm1>10)
# c1<-which(dm2>10)
#length(r1)
#length(c1)
#subset<-expression_data[r1,c1]

heatmap(as.matrix(subset), scale="none")

