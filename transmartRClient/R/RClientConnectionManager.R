AuthenticateClientSession <- function(oauthDomain = "localhost:8080",  transmartDomain = "localhost:8080", prefetched.verifier = NULL) {
  require(RCurl)
  require(RJSONIO)
  
  if (exists("RClientEnv")) {
    cat("Previous connection settings will be cleared. Do you wish to continue? Y/N\n")
    choice <- readline()
    if (length(grep("^y|^Y",choice))==0) return("Cancelled. Previous settings remain in effect.")
  }
  
  .initConnectionSettings(oauthDomain, transmartDomain)
  
  if (is.null(prefetched.verifier)) {
    cat("Please go to the following url to authorize this RClient:\n")
    cat(RClientEnv$oauth_url)
    cat("\nAnd paste the verifier here:")
    temp_verifier <- readline() 
  } else temp_verifier <- prefetched.verifier
  
  response <- getURL(RClientEnv$getTokenExchangeURI(temp_verifier), verbose = FALSE, curl = RClientEnv$curlHandle, httpheader = c(Host = RClientEnv$oauthDomain))
  jsonList <- fromJSON(response)
  list2env(jsonList, envir = RClientEnv)
  print("Validation accepted. You now have access to the database.")
}



.initConnectionSettings <- function(oauthDomain, transmartDomain) {
  assign("RClientEnv", new.env(parent = .GlobalEnv), envir = .GlobalEnv)
  
  require(RCurl)
  RClientEnv$curlHandle <- getCurlHandle()
  
  RClientEnv$oauthDomain <- oauthDomain
  RClientEnv$transmartDomain <- transmartDomain
  
  RClientEnv$client_id <- "myId"
  RClientEnv$client_secret <- "mySecret"
  
  RClientEnv$oauth_url <- paste(sep = "",
    "http://", RClientEnv$oauthDomain,
    "/transmart-rest-api/oauth/authorize?response_type=code&client_id=", RClientEnv$client_id,
    "&client_secret=", RClientEnv$client_secret,
    "&redirect_uri=http://", RClientEnv$oauthDomain,
    "/transmart-rest-api/oauth/verify"
  )
  
  RClientEnv$db_access_url <- paste(sep = "", 
    "http://", RClientEnv$transmartDomain, "/transmart-rest-api"
  )
  
  RClientEnv$getTokenExchangeURI <- function(verification_code) {
    paste(sep = "",
      oauthDomain,
      "/transmart-rest-api/oauth/token?grant_type=authorization_code&client_id=", client_id,
      "&client_secret=", client_secret,
      "&code=", verification_code,
      "&redirect_uri=http://", oauthDomain,
      "/transmart-rest-api/oauth/verify"
    )
  }; environment(RClientEnv$getTokenExchangeURI) <- RClientEnv 
  
  RClientEnv$serverGetRequest <- function(url) {
    result <- getURL(
      paste(sep="",
          RClientEnv$db_access_url, url 
      ),
      httpheader = c(
        Host = "localhost:8080",
        Authorization = paste("Bearer ", RClientEnv$access_token, sep="")
      ),
      #verbose = TRUE,
      curl = RClientEnv$curlHandle
    )
    fromJSON(result)
  }; environment(RClientEnv$serverGetRequest) <- RClientEnv 
}



.checkRClientEnvironment <- function() {
  require(RCurl)
  require(RJSONIO)
  if (!exists("RClientEnv", envir = .GlobalEnv)) stop("Client has not been initialized yet.")
}

