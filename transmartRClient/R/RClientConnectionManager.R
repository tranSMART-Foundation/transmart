# Copyright (c) 2014 The Hyve B.V.
# This code is licensed under the GNU General Public License, version 3

ConnectToTransmart <- 
function (transmartDomain, authenticate = TRUE, ...) {
  if (!exists("transmartClientEnv")) assign("transmartClientEnv", new.env(parent = .GlobalEnv), envir = .GlobalEnv)
  
  transmartClientEnv$transmartDomain <- transmartDomain
  transmartClientEnv$db_access_url <- paste(sep = "", 
                                            "http://", transmartClientEnv$transmartDomain, "/transmart"
  )
  
  if (authenticate) { 
      cat(AuthenticateWithTransmart(...), "\n")
  }
  
  .checkTransmartConnection()
}

AuthenticateWithTransmart <- 
function (oauthDomain = transmartClientEnv$transmartDomain, prefetched.request.token = NULL) {
    require(RCurl)
    require(RJSONIO)  
  
    if (exists("transmartClientEnv") && exists("access_token", envir = transmartClientEnv)) {
        cat("Would you like to re-authenticate? (Previous authentication will be cleared).\n",
            "Do you wish to continue? Y/N\n")
        if (!grepl("^y|^Y", readline())) return("Cancelled re-authentication.")
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
        cat("Please go to the following url to authorize this RClient:\n\n",
            oauth.request.token.url, "\n\n",
            "And paste the verifier here:\n")
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
    
    tryCatch(
        oauthResponse <- getURL(oauth.exchange.token.url,
            verbose = FALSE,
            httpheader = c(Host = transmartClientEnv$oauthDomain)), 
        error = function(e) { stop("Error with connecting to verification server.") }
    )
    
    if (grepl("access_token", oauthResponse)) {
      list2env(fromJSON(oauthResponse), envir = transmartClientEnv) 
      return("Authentication completed")
    }
    stop("Authentication failed.")
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
             stop("Error in parsing response from tranSMART server. Please check the details of your request.")
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
    ping <- .transmartServerGetRequest("/oauth/verify", use.HAL = FALSE)
    if (!is.null(ping)) {
        if (grepl("^invalid_token", ping["error"])) {
          cat("Authentication token not accepted. Details:\n",
              paste(ping, collapse = ": "),"\n")
              AuthenticateWithTransmart(oauthDomain = transmartClientEnv$oauthDomain)
        } else {
          cat("Cannot connect to tranSMART database.\n",
              "Technical details:\n")
          stop(paste(ping, collapse = ": "))
        }
    } else { return("Connection has been succesfully established") }
}


# this function is needed for .listToDataFrame to recursively replace NULL
# values with NA, otherwise, unlist() will exclude those values.
.recursiveReplaceNullWithNa <- function(list) {
    if (length(list) == 0) return(list())
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

.simplifyHalList <- function(halList) {
  # rename _links element to api.link
  names(halList)[which(names(halList) == "_links")] <- "api.link"
  # remove embedded intermediate element and add its sub-elements to this level
  if ("_embedded" %in% names(halList)) {
    halList <- as.list(c(halList, halList[["_embedded"]]))
    halList[["_embedded"]] <- NULL
  }
  # recursion: apply this function to list-elements of current list
  if (length(halList) > 0) {
    for (elementIndex in 1:length(halList)) {
      if (is.list(halList[[elementIndex]])) {
        halList[[elementIndex]] <- .simplifyHalList(halList[[elementIndex]])
      }
    }
  }
  return(halList)
}


