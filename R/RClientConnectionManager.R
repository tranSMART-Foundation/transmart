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

connectToTransmart <- 
function (transmartDomain, use.authentication = TRUE, ...) {
    if (!exists("transmartClientEnv") || transmartClientEnv$transmartDomain != transmartDomain) { 
        assign("transmartClientEnv", new.env(parent = .GlobalEnv), envir = .GlobalEnv)
    }

    transmartClientEnv$transmartDomain <- transmartDomain
    transmartClientEnv$db_access_url <- transmartClientEnv$transmartDomain
  
    authenticated <- TRUE
    
    if (use.authentication && !exists("access_token", envir = transmartClientEnv)) {
        authenticated <- authenticateWithTransmart(...)
    } else { if (!use.authentication && exists("access_token", envir = transmartClientEnv)) {
            remove("access_token", envir = transmartClientEnv)
        }
    }

    if(!.checkTransmartConnection()) {
        if (use.authentication && authenticated && !exists("access_token", envir = transmartClientEnv)) {
            # The access token has been removed: this must mean the applying the refresh token
            # (in .checkTransmartConnection) has failed.
            #
            # Trying to reauthenticate...
            #
            # (Note: might cause an infinite loop if authentication succeeds, but checking the connection
            # fails and triggers refreshing the authentication, which fails and removes the access token.)
            connectToTransmart(transmartDomain, use.authentication, ...)
        } else {
            stop("Connection unsuccessful. Type: ?connectToTransmart for help.")
        }
    } else {
        message("Connection successful.")
    }
}

authenticateWithTransmart <- 
function (oauthDomain = transmartClientEnv$transmartDomain, prefetched.request.token = NULL) {
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

    oauthResponse <- NULL
    tryCatch(oauthResponse <- getURL(oauth.exchange.token.url, verbose = getOption("verbose")), 
            error = function(e) {
                if (getOption("verbose")) { message(e, "\n", oauthResponse) }
                stop("Error with connection to verification server.") 
            })

    if (grepl("access_token", oauthResponse)) {
        list2env(fromJSON(oauthResponse), envir = transmartClientEnv)
        transmartClientEnv$access_token.timestamp <- Sys.time()
        cat("Authentication completed.\n")
        return(TRUE)
    } else {
        cat("Authentication failed.\n")
        return(FALSE)
    }
}

refreshToken <- function(oauthDomain = transmartClientEnv$transmartDomain) {
    transmartClientEnv$oauthDomain <- oauthDomain
    transmartClientEnv$client_id <- "api-client"
    transmartClientEnv$client_secret <- "api-client"
    message("Trying to reauthenticate using the refresh token: ", transmartClientEnv$refresh_token, "...")
    refreshUrl <- paste(sep = "",
                        transmartClientEnv$oauthDomain,
                        "/oauth/token?grant_type=refresh_token",
                        "&client_id=", transmartClientEnv$client_id,
                        "&client_secret=", transmartClientEnv$client_secret,
                        "&refresh_token=", transmartClientEnv$refresh_token,
                        "&redirect_uri=", transmartClientEnv$oauthDomain,
                        "/oauth/verify",
                        "")
    
    oauthResponse <- NULL
    tryCatch(oauthResponse <- getURL(refreshUrl, verbose = getOption("verbose")),
             error = function(e) {
               if (getOption("verbose")) { message(e, "\n", oauthResponse) }
               stop("Error with connection to verification server.")
             })
    if (getOption("verbose")) { message("Server response:\n\n", oauthResponse, "\n") }
    if (grepl("access_token", oauthResponse)) {
        list2env(fromJSON(oauthResponse), envir = transmartClientEnv)
        transmartClientEnv$access_token.timestamp <- Sys.time()
        cat("Authentication completed.\n")
        return(TRUE)
    } else {
        cat("Authentication failed.\n")
        return(FALSE)
    }
}

.checkTransmartConnection <- function(reauthentice.if.invalid.token = TRUE) {
    if (!exists("transmartClientEnv", envir = .GlobalEnv)) {
        stop("No connection to tranSMART has been set up. For details, type: ?connectToTransmart")
    }

    if (!exists("access_token", envir = transmartClientEnv)) {
        return(TRUE)
    }

    ping <- .transmartServerGetRequest("/oauth/inspectToken", accept.type = "default")
    if (getOption("verbose")) { message(paste(ping, collapse = ": ")) }

    if (!is.null(ping)) {
        if ("error" %in% names(ping)) {
            message("Error ", ping["error"],  ": ", ping["error_description"])
            if (ping["error"] == "invalid_token") {
                # try to refresh authentication
                if (refreshToken()) {
                    message("Access token refreshed.")
                    return(.checkTransmartConnection(reauthentice.if.invalid.token))
                } else {
                    message("Removing access token from the environment.")
                    remove("access_token", envir = transmartClientEnv)
                    return(FALSE)
                }
            }
            return(FALSE)
        }
        # perhaps check or update information about tokens and principal.
        return(TRUE)
    }
    # if check fails, use refresh token to update (or ask for it).
    return(FALSE)
}

.transmartServerGetRequest <- function(apiCall, ...)  {
    if (exists("access_token", envir = transmartClientEnv)) {
        httpHeaderFields <- c(Authorization = paste("Bearer ", transmartClientEnv$access_token, sep=""))
    } else { httpHeaderFields <- "" }

    tryCatch(result <- .serverMessageExchange(apiCall, httpHeaderFields, ...), 
            error = function(e) {
                message("Sorry, the R client was unable to carry out your request.",
                        "Please make sure that the transmart server is still running. \n\n",
                        "If the server is not down, you've encountered a bug.\n",
                        "You can help fix it by contacting us. Type ?transmartRClient for contact details.\n", 
                        "Optional: type options(verbose = TRUE) and replicate the bug to find out more details.")
                stop(e)
            })
    result
}

.serverMessageExchange <- 
function(apiCall, httpHeaderFields, accept.type = "default", progress = .make.progresscallback.download()) {
    if (any(accept.type == c("default", "hal"))) {
        if (accept.type == "hal") { httpHeaderFields <- c(httpHeaderFields, accept = "application/hal+json") }
        result <- getURL(paste(sep="", transmartClientEnv$db_access_url, apiCall),
                verbose = getOption("verbose"),
                httpheader = httpHeaderFields)
        if (getOption("verbose")) { message("Server response:\n\n", result, "\n") }
        if (is.null(result) || result == "null") { return(NULL) }
        result <- fromJSON(result, asText = TRUE, nullValue = NA)
        if (accept.type == "hal") { return(.simplifyHalList(result)) }
        return(result)
    } else if (accept.type == "binary") {
        progress$start(NA_integer_)
        result <- list()
        h <- basicTextGatherer()
        result$content <- getBinaryURL(paste(sep="", transmartClientEnv$db_access_url, apiCall),
                .opts = list(headerfunction = h$update),
                noprogress = FALSE,
                progressfunction = function(down, up) {up[which(up == 0)] <- NA; progress$update(down, up) },
                httpheader = httpHeaderFields)
        progress$end()
        result$header <- parseHTTPHeader(h$value())
        if (getOption("verbose")) {
            message(paste("Server binary response header:", as.character(data.frame(result$header)), "", sep="\n"))
        }
        return(result)
    }
    return(NULL)
}

.make.progresscallback.download <- function() {
    lst <- list()
    lst$start <- function(.total) cat("Retrieving data: \n")
    lst$update <- function(current, .total) {
        # This trick unfortunately doesn't work in RStudio if we write to stderr.
        cat(paste("\r", format(current / (1024*1024), digits=3, nsmall=3), "MiB downloaded."))
    }
    lst$end <- function() cat("\nDownload complete.\n")
    return(lst)
}


.listToDataFrame <- function(list) {
    # TODO: (timdo) dependency on 'plyr' package removed; figure out whether dependency is present elsewhere, or remove dependency
    # add each list-element as a new row to a matrix, in two passes
    # first pass: go through each list element, unlist it and remember future column names
    columnNames <- c()
    if (length(list) > 0) {
        for (i in 1:(length(list))) {
            cat('i: ', i, '\n')
            list[[i]] <- unlist(list[[i]])
            columnNames <- union(columnNames, names(list[[i]]))
        }
    }
    
    # second pass: go through each list element and add its elements to correct column
    df <- matrix(nrow = length(list), ncol = length(columnNames))
    if (length(list) > 0) {
        for (i in 1:(length(list))) {
            df[i, match(names(list[[i]]), columnNames)] <- list[[i]]
        }
    }
    colnames(df) <- columnNames

    # check whether list contains valid row names, and if true; use them
    if (length(list) < 1 || is.null(names(list)) || is.na(names(list)) || length(names(list)) != length(list)) { 
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
