
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
#This R Script will be used to interact with the tranSMART datawarehouse.
#-----------------------

#-----------------------
#This function establishes the connection to the DB.
#-----------------------
tranSMART.DB.establishConnection <- function()
{
	library(ROracle)

	tranSMART.DB.username <- get(x='tranSMART.DB.username',pos='.GlobalEnv')
	tranSMART.DB.password <- get(x='tranSMART.DB.password',pos='.GlobalEnv')
	tranSMART.DB.dbname <- get(x='tranSMART.DB.dbname',pos='.GlobalEnv')

	if(is.na(tranSMART.DB.username))
	{
		print("Warning! Database username is not set.")
	}
	
	if(is.na(tranSMART.DB.password))
	{
		print("Warning! Database password is not set.")
	}

	if(is.na(tranSMART.DB.dbname))
	{
		print("Warning! Database name is not set.")
	}	
	
	drv <- dbDriver('Oracle')
	con <- dbConnect(drv, username=tranSMART.DB.username, password=tranSMART.DB.password,dbname=tranSMART.DB.dbname)

	con
}

tranSMART.DB.username <- NA
tranSMART.DB.password <- NA
tranSMART.DB.dbname <- NA






