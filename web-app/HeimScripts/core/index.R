if (!exists("remoteScriptDir")) {  #  Needed for unit-tests
  remoteScriptDir <- "web-app/HeimScripts/core"
}

inputUtils <- paste(remoteScriptDir, "/core/input.R", sep="")
source(inputUtils)
