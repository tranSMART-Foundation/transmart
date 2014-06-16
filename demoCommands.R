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
connectToTransmart("http://test-api.thehyve.net/transmart")

studies <- getStudies()
print(studies)

allObservations <- getObservations(studies$name[1], as.data.frame = T)

concepts <- getConcepts(studies$name[1])
# retrieve observations for study 1 for the first concept containing "e"
observations <- getObservations(studies$name[1], concept.match = "e")
# retrieve observations belonging to the first two concepts by using the api.links contained in the getConcepts-result
observations <- getObservations(studies$name[1], concept.links = concepts$api.link.self.href[c(1,2)])

# if a concept contains high dimensional data, use the following command to obtain this data
getHighdimData(study.name = "GSE8581", concept.match = "Lung")
# you will be told that one of the listed projections needs to be selected. The following will return the actual data.
data <- getHighdimData(study.name = "GSE8581", concept.match = "Lung", projection = "zscore")
names(data)
data[["data"]][1:10,1:10]
data[[2]]["214503_x_at"]
