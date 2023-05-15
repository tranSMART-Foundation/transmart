if (!exists("remoteScriptDir")) {  #  Needed for unit-tests
  remoteScriptDir <- "WEB-INF/HeimScripts/_core"
}

inputUtils <- paste(remoteScriptDir, "/_core/input.R", sep="")
source(inputUtils)
