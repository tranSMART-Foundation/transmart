# Copyright (c) 2014 The Hyve B.V.
# This code is licensed under the GNU General Public License, version 3

AuthenticateWithTransmart <- function(oauthDomain = "localhost:8080", prefetched.request.token = NULL) {
    require(RCurl)
    require(RJSONIO)

    if (exists("transmartClientEnv") && exists("access_token", envir = transmartClientEnv)) {
        cat("Previous authentication will be cleared. Do you wish to continue? Y/N\n")
        choice <- readline()
        if (length(grep("^y|^Y",choice))==0) return("Cancelled. Previous authentication will remain in effect.")
    }

    if (!exists("transmartClientEnv")) assign("transmartClientEnv", new.env(parent = .GlobalEnv), envir = .GlobalEnv)
    transmartClientEnv$oauthDomain <- oauthDomain
    transmartClientEnv$client_id <- "api-client"
    transmartClientEnv$client_secret <- "api-client"

    oauth.request.token.url <- paste(sep = "",
            "http://", transmartClientEnv$oauthDomain,
            "/transmart/oauth/authorize?response_type=code&client_id=", 
            transmartClientEnv$client_id,
            "&client_secret=", transmartClientEnv$client_secret,
            "&redirect_uri=http://", transmartClientEnv$oauthDomain,
            "/transmart/oauth/verify")

    if (is.null(prefetched.request.token)) {
        cat("Please go to the following url to authorize this RClient:\n")
        cat(oauth.request.token.url)
        cat("\nAnd paste the verifier here:")
        request.token <- readline() 
    } else request.token <- prefetched.request.token

    oauth.exchange.token.url <- paste(sep = "",
            "http://", transmartClientEnv$oauthDomain,
            "/transmart/oauth/token?grant_type=authorization_code&client_id=",
            transmartClientEnv$client_id,
            "&client_secret=", transmartClientEnv$client_secret,
            "&code=", request.token,
            "&redirect_uri=http://", transmartClientEnv$oauthDomain,
            "/transmart/oauth/verify")
    
    oauthResponse <- getURL(oauth.exchange.token.url,
            verbose = FALSE,
            httpheader = c(Host = oauthDomain))

    list2env(fromJSON(oauthResponse), envir = transmartClientEnv)
}


ConnectToTransmart <- function(transmartDomain = "localhost:8080") {
    if (exists("transmartClientEnv") && exists("transmartDomain", envir = transmartClientEnv)) {
        cat("Previous connection settings will be cleared (authentication will remain intact).
                \nDo you wish to continue? Y/N\n")
        choice <- readline()
        if (length(grep("^y|^Y",choice))==0) return("Cancelled. Previous connection settings will remain in effect.")
    } 
    
    if (!exists("transmartClientEnv")) assign("transmartClientEnv", new.env(parent = .GlobalEnv), envir = .GlobalEnv)
    transmartClientEnv$transmartDomain <- transmartDomain
    
    transmartClientEnv$db_access_url <- paste(sep = "", 
      "http://", transmartClientEnv$transmartDomain, "/transmart"
    )
    .checkTransmartConnection()
}

.transmartServerGetRequest <- function(apiCall, use.HAL) {
  httpHeaderFields <- c(Host = transmartClientEnv$transmartDomain)
  if (use.HAL) { httpHeaderFields <- c(httpHeaderFields, accept = "application/hal+json") }
  if (exists("access_token", envir = transmartClientEnv)) {
    httpHeaderFields <- c(httpHeaderFields, Authorization = paste("Bearer ", transmartClientEnv$access_token, sep=""))
  }
  result <- getURL(paste(sep="", transmartClientEnv$db_access_url, apiCall),
                   httpheader = httpHeaderFields,
                   verbose = FALSE
  )
  if (is.null(result) || result == "null") {return(NULL)}
  tryCatch(result <- fromJSON(result, asText = TRUE), 
           error = function(e) {
             stop("Error converting result from tranSMART. Please check the details of your request.")
           }
  )
  if (use.HAL) { return( .simplifyHalList(result) ) }
  result
}

.checkTransmartConnection <- function() {
    require(RCurl)
    require(RJSONIO)
    if (!exists("transmartClientEnv", envir = .GlobalEnv)) {
        stop("Client has not been initialized yet. Please use ConnectToTransmart()")
    }
    ping <- transmartClientEnv$serverGetRequest("/oauth/verify")
    if (!is.null(ping)) {
        cat(paste(sep=" \n", 
                  "Cannot connect to tranSMART database.",
                  "Check your connection, and possible use AuthenticateWithTransmart() to",
                  "refresh your authentication, or use ConnectToTransmart() to reset your",
                  "connection settings. Technical details:"))
        stop(ping)
    }
    return("Connection has been succesfully established")
}


# this function is needed for .listToDataFrame to recursively replace NULL
# values with NA, otherwise, unlist() will exclude those values in the next step
.recursiveReplaceNullWithNa <- function(list) {
    for (i in 1:length(list)) {
        if (is.list(list[[i]])) {
            list[[i]] <- .recursiveReplaceNullWithNa(list[[i]])
        } else {
            if (is.null(list[[i]])) list[[i]] <- NA
        }
    }
    list
}


.listToDataFrame <- function(list) {
    # replace NULL values with NA values in list
    list <- .recursiveReplaceNullWithNa(list)
    
    # add each list-element as a new row to a matrix
    df <- c()
    for (el in list) df <- rbind(df, unlist(el))
    if (is.null(names(list))) { 
      rownames(df) <- NULL
    } else { rownames(df) <- names(list) }
    # convert matrix to data.frame
    as.data.frame(df, stringsAsFactors = FALSE)
}
