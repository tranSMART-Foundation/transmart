# Copyright (c) 2014 The Hyve B.V.
# This code is licensed under the GNU General Public License,
# version 3, or (at your option) any later version.

connectToTransmart <- 
function (transmartDomain, use.authentication = TRUE, ...) {
    if (!exists("transmartClientEnv") || transmartClientEnv$transmartDomain != transmartDomain) { 
        assign("transmartClientEnv", new.env(parent = .GlobalEnv), envir = .GlobalEnv)
    }
    
    transmartClientEnv$transmartDomain <- transmartDomain
    transmartClientEnv$db_access_url <- transmartClientEnv$transmartDomain
    
    if (use.authentication && !exists("access_token", envir = transmartClientEnv)) {
        authenticateWithTransmart(...)
    } else { if (!use.authentication && exists("access_token", envir = transmartClientEnv)) {
            remove("access_token", envir = transmartClientEnv)
        }
    }
    
    if(!.checkTransmartConnection()) {
      cat("Connection unsuccessful. Type: ?connectToTransmart for help.\n")
    } else {
      cat("Connection successful.\n")
    }
}

authenticateWithTransmart <- 
function (oauthDomain = transmartClientEnv$transmartDomain, prefetched.request.token = NULL) {
    require(RCurl)
    require(RJSONIO)
    
    if (!exists("transmartClientEnv")) assign("transmartClientEnv", new.env(parent = .GlobalEnv), envir = .GlobalEnv)
    
    transmartClientEnv$oauthDomain <- oauthDomain
    transmartClientEnv$client_id <- "api-client"
    transmartClientEnv$client_secret <- "api-client"
    
    oauth.request.token.url <- paste(sep = "",
                                     transmartClientEnv$oauthDomain,
                                     "/oauth/authorize?response_type=code&client_id=", 
                                     transmartClientEnv$client_id,
                                     "&client_secret=", transmartClientEnv$client_secret,
                                     "&redirect_uri=", transmartClientEnv$oauthDomain,
                                     "/oauth/verify")
    
    if (is.null(prefetched.request.token)) {
      cat("Please visit the following url to authenticate this RClient (enter nothing to cancel):\n\n",
          oauth.request.token.url, "\n\n",
          "And paste the verifier token here:\n")
      request.token <- readline() 
    } else request.token <- prefetched.request.token
    
    if (request.token == "") { 
      cat("Authentication cancelled.\n")
      return()
    }
    
    oauth.exchange.token.url <- paste(sep = "",
                                      transmartClientEnv$oauthDomain,
                                      "/oauth/token?grant_type=authorization_code&client_id=",
                                      transmartClientEnv$client_id,
                                      "&client_secret=", transmartClientEnv$client_secret,
                                      "&code=", request.token,
                                      "&redirect_uri=", transmartClientEnv$oauthDomain,
                                      "/oauth/verify")
    
    tryCatch(
            oauthResponse <- getURL(oauth.exchange.token.url,
                    verbose = getOption("verbose"),
                    httpheader = c(Host = transmartClientEnv$oauthDomain)), 
            error = function(e) {
                if (getOption("verbose")) { cat(e, "\n", oauthresponse) }
                stop("Error with connection to verification server.") 
            })
    
    if (grepl("access_token", oauthResponse)) {
        list2env(fromJSON(oauthResponse), envir = transmartClientEnv)
        transmartClientEnv$access_token.timestamp <- Sys.time()
        cat("Authentication completed.\n")
    } else {
        cat("Authentication failed.\n")
    }
  }

.checkTransmartConnection <- function(reauthentice.if.invalid.token = TRUE) {
  if (!exists("transmartClientEnv", envir = .GlobalEnv)) {
    stop("No connection to tranSMART has been set up. For details, type: ?connectToTransmart")
  }
  
  if (!exists("access_token", envir = transmartClientEnv)) {
      cat("TODO: Cannot test connection without authentication.\n")
      return(TRUE)
  }
  
  ping <- .transmartServerGetRequest("/oauth/verify", use.HAL = FALSE)
  if (getOption("verbose")) { cat(paste(ping, collapse = ": "), "\n") }
  
  if (!is.null(ping)) {
    if (reauthentice.if.invalid.token && grepl("^invalid_token", ping["error"])) {
      cat("Authentication token not accepted.\n")
      authenticateWithTransmart(oauthDomain = transmartClientEnv$oauthDomain)
      return(.checkTransmartConnection(reauthentice.if.invalid.token = FALSE))
    }
    cat("Cannot connect to tranSMART database.\n")
    return(FALSE)
  }
  return(TRUE)
}

.transmartServerGetRequest <- function(apiCall, ...)  {
  httpHeaderFields <- c(Host = transmartClientEnv$transmartDomain)
  if (exists("access_token", envir = transmartClientEnv)) {
      httpHeaderFields <- c(httpHeaderFields, Authorization = paste("Bearer ", transmartClientEnv$access_token, sep=""))
  }
  
  tryCatch(result <- .serverMessageExchange(apiCall, httpHeaderFields, ...), 
          error = function(e) {
              cat("Sorry. You've encountered a bug.\n",
                      "You can help fix it by contacting us. Type ?transmartRClient for contact details.\n", 
                      "Optional: type options(verbose = TRUE) and replicate the bug to find out more details.\n")
              stop(e)
          }
  )
  result
}

.serverMessageExchange <- function(apiCall, httpHeaderFields, use.HAL = FALSE) {
  require(RCurl)
  require(RJSONIO)
  
  if (use.HAL) { httpHeaderFields <- c(httpHeaderFields, accept = "application/hal+json") }
  
  result <- getURL(paste(sep="", transmartClientEnv$db_access_url, apiCall),
          verbose = getOption("verbose"),
          httpheader = httpHeaderFields)
  if (getOption("verbose")) { cat("Server response:\n\n", result, "\n\n") }
  
  if (is.null(result) || result == "null") { return(NULL) }
  
  result <- fromJSON(result, asText = TRUE, nullValue = NA)
  if (use.HAL) { result <- .simplifyHalList(result) }
  
  result
}


.listToDataFrame <- function(list) {
    # replace NULL values with NA values in list
    # TODO: .serverExchangeMessage now has argument in fromJSON which should make function in next line obsolete
    #list <- .recursiveReplaceNullWithNa(list)

    # add each list-element as a new row to a matrix
    df <- c()
    for (el in list) df <- rbind(df, unlist(el))
        if (is.null(names(list)) || is.na(names(list)) || length(names) != length(list)) { 
        rownames(df) <- NULL
    } else { rownames(df) <- names(list) }
    # convert matrix to data.frame
    as.data.frame(df, stringsAsFactors = FALSE)
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


