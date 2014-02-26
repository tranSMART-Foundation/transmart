AuthenticateWithTransmart <- function(oauthDomain = "localhost:8080", prefetched.request.token = NULL) {
    require(RCurl)
    require(RJSONIO)

    if (exists("transmartClientEnv") && exists(transmartClientEnv$access_token)) {
        cat("Previous authentication will be cleared. Do you wish to continue? Y/N\n")
        choice <- readline()
        if (length(grep("^y|^Y",choice))==0) return("Cancelled. Previous authentication will remain in effect.")
    }

    if (!exists("transmartClientEnv")) assign("transmartClientEnv", new.env(parent = .GlobalEnv), envir = .GlobalEnv)
    transmartClientEnv$oauthDomain <- oauthDomain
    transmartClientEnv$client_id <- "myId"
    transmartClientEnv$client_secret <- "mySecret"

    oauth.request.token.url <- paste(sep = "",
            "http://", transmartClientEnv$oauthDomain,
            "/transmart-rest-api/oauth/authorize?response_type=code&client_id=", 
            transmartClientEnv$client_id,
            "&client_secret=", transmartClientEnv$client_secret,
            "&redirect_uri=http://", transmartClientEnv$oauthDomain,
            "/transmart-rest-api/oauth/verify")

    if (is.null(prefetched.request.token)) {
        cat("Please go to the following url to authorize this RClient:\n")
        cat(oauth.request.token.url)
        cat("\nAnd paste the verifier here:")
        request.token <- readline() 
    } else request.token <- prefetched.request.token

    oauth.exchange.token.url <- paste(sep = "",
            "http://", transmartClientEnv$oauthDomain,
            "/transmart-rest-api/oauth/token?grant_type=authorization_code&client_id=",
            transmartClientEnv$client_id,
            "&client_secret=", transmartClientEnv$client_secret,
            "&code=", request.token,
            "&redirect_uri=http://", transmartClientEnv$oauthDomain,
            "/transmart-rest-api/oauth/verify")
    
    oauthResponse <- getURL(oauth.exchange.token.url,
            verbose = FALSE,
            httpheader = c(Host = oauthDomain))

    list2env(fromJSON(oauthResponse), envir = transmartClientEnv)
}



ConnectToTransmart <- function(transmartDomain = "localhost:8080") {
    require(RCurl)
    require(RJSONIO)

    if (exists("transmartClientEnv")) {
        cat("Previous connection settings will be cleared (authentication will remain intact).
                \nDo you wish to continue? Y/N\n")
        choice <- readline()
        if (length(grep("^y|^Y",choice))==0) return("Cancelled. Previous connection settings will remain in effect.")
    } else { assign("transmartClientEnv", new.env(parent = .GlobalEnv), envir = .GlobalEnv) }

    transmartClientEnv$transmartDomain <- transmartDomain
    
    transmartClientEnv$db_access_url <- paste(sep = "", 
      "http://", transmartClientEnv$transmartDomain, "/transmart-rest-api"
    )

    transmartClientEnv$serverGetRequest <- function(apiCall) {
        httpHeaderFields <- c(Host = transmartDomain)
        if (exists("transmartClientEnv$access_token")) {
            append(httpHeaderFields, Authorization = paste("Bearer ", access_token, sep=""))
        }
        result <- getURL(paste(sep="", db_access_url, apiCall),
          httpheader = httpHeaderFields,
          verbose = FALSE
        )
        fromJSON(result)
    }; environment(transmartClientEnv$serverGetRequest) <- transmartClientEnv 
}

.checkTransmartConnection <- function() {
  require(RCurl)
  require(RJSONIO)
  if (!exists("transmartClientEnv", envir = .GlobalEnv)) stop("Client has not been initialized yet.")
  #ping <- transmartClientEnv$serverGetRequest("")
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

