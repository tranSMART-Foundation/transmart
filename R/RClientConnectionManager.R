# Copyright 2014, 2015 The Hyve B.V.
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
function (transmartDomain, use.authentication = TRUE, token = NULL, .access.token = NULL, ...) {
    if (!exists("transmartClientEnv") || transmartClientEnv$transmartDomain != transmartDomain) { 
        assign("transmartClientEnv", new.env(parent = .GlobalEnv), envir = .GlobalEnv)
    }

    transmartClientEnv$transmartDomain <- transmartDomain
    transmartClientEnv$db_access_url <- transmartClientEnv$transmartDomain
    if (!is.null(token)) {
        transmartClientEnv$refresh_token <- token
    }
    if (!is.null(.access.token)) {
        transmartClientEnv$access_token <- .access.token
    }

    if(.checkTransmartConnection()) {
        message("Connection active")
        return(invisible(TRUE))
    }

    if (use.authentication && !exists("access_token", envir = transmartClientEnv)) {
        authenticated <- authenticateWithTransmart(...)
        if(is.null(authenticated)) return()
    } else if (!use.authentication && exists("access_token", envir = transmartClientEnv)) {
        remove("access_token", envir = transmartClientEnv)
    }

    if(!.checkTransmartConnection()) {
        stop("Connection unsuccessful. Type: ?connectToTransmart for help.")
    } else {
        message("Connection successful.")
        return(invisible(TRUE))
    }
}

getTransmartToken <- function() {
    if(exists("transmartClientEnv")) return(transmartClientEnv$refresh_token)
}

authenticateWithTransmart <- 
function (oauthDomain = transmartClientEnv$transmartDomain, prefetched.request.token = NULL,
          client.id = "api-client", client.secret = "api-client") {
    if (!exists("transmartClientEnv")) assign("transmartClientEnv", new.env(parent = .GlobalEnv), envir = .GlobalEnv)

    transmartClientEnv$oauthDomain <- oauthDomain
    transmartClientEnv$client_id <- client.id
    transmartClientEnv$client_secret <- client.secret

    oauth.request.token.url <- paste(sep = "",
            transmartClientEnv$oauthDomain,
            "/oauth/authorize?response_type=code&client_id=", 
            transmartClientEnv$client_id,
            "&client_secret=", transmartClientEnv$client_secret,
            "&redirect_uri=", URLencode(transmartClientEnv$oauthDomain, TRUE),
            URLencode("/oauth/verify", TRUE))

    if (!is.null(prefetched.request.token)) {
        request.token <- prefetched.request.token
    } else {
        cat("No access token specified. Please visit the following url to authenticate this RClient ",
            "(enter nothing to cancel):\n\n",
            oauth.request.token.url, "\n\n",
            "And paste the verifier token here:\n")
        request.token <- readline() 
    }

    if (request.token == "") { 
        cat("Authentication cancelled\n")
        return(FALSE)
    }

    oauth.exchange.token.path <- "/oauth/token"
    post.body <- list(
        grant_type="authorization_code",
        code=request.token,
        redirect_uri=paste(transmartClientEnv$oauthDomain, "/oauth/verify", sep=""))

    oauthResponse <- .transmartServerPostOauthRequest(oauth.exchange.token.path, "Authentication", post.body)
    if (is.null(oauthResponse)) return(FALSE)

    list2env(oauthResponse$content, envir = transmartClientEnv)
    transmartClientEnv$access_token.timestamp <- Sys.time()
    cat("Authentication completed\n")
    return(TRUE)
}

.refreshToken <- function(oauthDomain = transmartClientEnv$transmartDomain) {
    if (!exists("refresh_token", envir=transmartClientEnv)) {
        message("Unable to refresh the connection, no refresh token found")
        return(FALSE)
    }
    transmartClientEnv$oauthDomain <- oauthDomain
    transmartClientEnv$client_id <- "api-client"
    transmartClientEnv$client_secret <- "api-client"
    message("Trying to reauthenticate using the refresh token...")
    refreshPath <- "/oauth/token"
    post.body <- list(grant_type="refresh_token",
        refresh_token=transmartClientEnv$refresh_token,
        redirect_uri=paste(transmartClientEnv$oauthDomain, "/oauth/verify", sep=""))
    
    oauthResponse <- .transmartServerPostOauthRequest(refreshPath, "Refreshing access", post.body)
    if (is.null(oauthResponse)) return(FALSE)
    if (!'access_token' %in% names(oauthResponse$content)) {
        message("Refreshing access failed, server response did not contain access_token. HTTP", statusString)
        return(FALSE)
    }
    list2env(oauthResponse$content, envir = transmartClientEnv)
    transmartClientEnv$access_token.timestamp <- Sys.time()
    return(TRUE)
}

.transmartServerPostOauthRequest <- function(path, action, post.body) {
    oauthResponse <- .transmartServerGetRequest(path, onlyContent=F, post.body=post.body)
    statusString <- paste("status code ", oauthResponse$status, ": ", oauthResponse$headers[['statusMessage']], sep='')
    if (!oauthResponse$JSON) {
        cat(action, " failed, could not parse server response of type ", oauthResponse$headers[['Content-Type']], ". ", statusString, "\n", sep='')
        return(NULL)
    }
    if ('error' %in% names(oauthResponse$content)) {
        cat(action, " failed, removing refresh_token:", oauthResponse$content[['error_description']], "\n", sep='')
        rm(refresh_token, envir=transmartClientEnv)
        return(NULL)
    }
    if (!oauthResponse$status == 200) {
        cat(action, "error: HTTP", statusString, "\n")
        return(NULL)
    }
    return(oauthResponse)
}

.ensureTransmartConnection <- function() {return(.checkTransmartConnection(stop.on.error = TRUE))}

.checkTransmartConnection <- function(stop.on.error = FALSE) {
    if(stop.on.error) {
        stopfn <- stop
    } else {
        stopfn <- function(e) {message(e); return(FALSE)}
    }

    if (!exists("transmartClientEnv", envir = .GlobalEnv)) {
        return(stopfn("No connection to tranSMART has been set up. For details, type: ?connectToTransmart"))
    }

    if (exists("access_token", envir = transmartClientEnv)) {
        ping <- .transmartServerGetRequest("/oauth/inspectToken", accept.type = "default", onlyContent = F)
        if(ping$status == 404) {
            # Maybe we're talking to an older version of Transmart that uses the version 1 oauth plugin
            ping <- .transmartServerGetRequest("/oauth/verify", accept.type = "default", onlyContent = F)
        }
        if (getOption("verbose")) { message(paste(ping$content, collapse = ": ")) }

        if(ping$status == 200) { return(TRUE) }

        if(!'error' %in% names(ping$content)) {
            return(stopfn(paste("HTTP ", ping$status, ": ", ping$statusMessage, sep='')))
        }
        if(ping$status != 401 || ping$content[['error']] != "invalid_token") {
            return(stopfn(paste("HTTP ", ping$status, ": ", ping$statusMessage, "\n", ping$content[['error']],  ": ", ping$content[['error_description']], sep='')))
        }
    } else if (!exists("refresh_token", envir = transmartClientEnv)) {
        return(stopfn("Unable to refresh authentication: no refresh token"))
    }

    # try to refresh authentication
    if (.refreshToken()) {
        message("Access token refreshed.")
        return(TRUE)
    } else {
        message("Removing access token from the environment.")
        remove("access_token", envir = transmartClientEnv)
        return(stopfn("Refreshing access failed"))
    }
}

.requestErrorHandler <- function(e, result=NULL) {
    message("Sorry, the R client encountered the following error: ", e,
            "\n\nPlease make sure that the transmart server is still running. ",
            "If the server is not down, you may have encountered a bug.\n",
            "You can help fix it by contacting us. Type ?transmartRClient for contact details.\n", 
            "Optional: type options(verbose = TRUE) and replicate the bug to find out more details.")
    # If e is a condition adding the call. parameter triggers another warning
    if(inherits(e, "condition")) {
        stop(e)
    } else {
        stop(e, call.=FALSE)
    }
}

.transmartGetJSON <- function(apiCall, ...) { .transmartServerGetRequest(apiCall, ensureJSON = TRUE, accept.type = "hal", ...) }

# If you just want a result, use the default parameters. If you want to do your own error handling, call with
# onlyContent = NULL, this will return a list with data, headers and status code.
.transmartServerGetRequest <- function(apiCall, errorHandler = .requestErrorHandler, onlyContent = c(200),
        ensureJSON = FALSE, ...)  {
    if (exists("access_token", envir = transmartClientEnv)) {
        httpHeaderFields <- c(Authorization = paste("Bearer ", transmartClientEnv$access_token, sep=""))
    } else { httpHeaderFields <- character(0) }

    tryCatch(result <- .serverMessageExchange(apiCall, httpHeaderFields, ...), error = errorHandler)
    if(!exists("result")) { return(NULL) }
    if(is.numeric(onlyContent)) {
        errmsg <- ''
        if(result$JSON && 'error' %in% names(result$content)) {
            errmsg <- paste(":", result$content[['error']])
            if('error_description' %in% names(result$content)) {
                errmsg <- paste(errmsg, ": ", result$content[['error_description']], sep='')
            }
        }
        if(!result$status %in% onlyContent) {
            errmsg <- paste("HTTP", result$status, result$statusMessage, "(expected result code(s):", toString(onlyContent), ")")
            if(result$JSON && 'error' %in% names(result$content)) {
                errmsg <- paste(errmsg, ": ", result$content[['error']], sep='')
                if('error_description' %in% names(result$content)) {
                    errmsg <- paste(errmsg, ": ", result$content[['error_description']], sep='')
                }
            }
            return(errorHandler(errmsg, result))
        }
        if(ensureJSON && !result$JSON) {
            return(errorHandler(paste("No JSON returned but", result$headers[['Content-Type']]), result))
        }
        return(result$content)
    }
    result
}

.contentType <- function(headers) {
    if(! 'content-type' %in% names(headers)) {
        return('content-type header not found')
    }
    h <- headers[['content-type']]
    if(grepl("^application/json(;|\\W|$)", h)) {
        return('json')
    }
    if(grepl("^application/hal\\+json(;|\\W|$)", h)) {
        return('hal')
    }
    if(grepl("^text/html(;|\\W|$)", h)) {
        return('html')
    }
    return('unknown')
}

.serverMessageExchange <- 
function(apiCall, httpHeaderFields, accept.type = "default", post.body = NULL) {
    if (any(accept.type == c("default", "hal"))) {
        if (accept.type == "hal") {
            httpHeaderFields <- c(httpHeaderFields, Accept = "application/hal+json;charset=UTF-8")
        }
        result <- list(JSON = FALSE)
        api.url <- paste0(transmartClientEnv$db_access_url, apiCall)
        if (is.null(post.body)) {
            req <- GET(api.url,
                       add_headers(httpHeaderFields),
                       authenticate(transmartClientEnv$client_id, transmartClientEnv$client_secret),
                       config(verbose = getOption("verbose")))
        } else {
            req <- POST(api.url,
                        body = post.body,
                        add_headers(httpHeaderFields),
                        authenticate(transmartClientEnv$client_id, transmartClientEnv$client_secret),
                        encode='form',
                        config(verbose = getOption("verbose")))
            if (getOption("verbose")) { message("POST body:\n", .list2string(post.body), "\n") }
        }
        result$content <- content(req, "text")
        if (getOption("verbose")) { message("Server response:\n", result$content, "\n") }
        result$headers <- headers(req)
        result$status <- req$status_code
        result$statusMessage <- http_status(req)$message
    	switch(.contentType(result$headers),
               json = {
                   result$content <- fromJSON(result$content)
                   result$JSON <- TRUE
               },
               hal = {
                   result$content <- .simplifyHalList(fromJSON(result$content))
                   result$JSON <- TRUE
               })
        return(result)
    } else if (accept.type == "binary") {
        result <- list(JSON = FALSE)
        api.url <- paste(sep="", transmartClientEnv$db_access_url, apiCall)
        if (is.null(post.body)) {
            req <- GET(api.url,
                       add_headers(httpHeaderFields),
                       authenticate(transmartClientEnv$client_id, transmartClientEnv$client_secret),
                       progress(),
                       config(verbose = getOption("verbose")))
        } else {
            req <- POST(api.url,
                        body = post.body,
                        add_headers(httpHeaderFields),
                        authenticate(transmartClientEnv$client_id, transmartClientEnv$client_secret),
                        progress(),
                        encode='form',
                        config(verbose = getOption("verbose")))
        }
        result$content <- content(req, "raw")
        result$headers <- headers(req)
        result$status <- req$status_code
        result$statusMessage <- http_status(req)$message
        return(result)
    }
    return(NULL)
}

.listToDataFrame <- function(l) {
    # TODO: (timdo) dependency on 'plyr' package removed; figure out whether dependency is present elsewhere, or remove dependency
    # add each list-element as a new row to a matrix, in two passes
    # first pass: go through each list element, unlist it and remember future column names
    columnNames <- c()
    if (length(l) > 0) {
        for (i in 1:(length(l))) {
            l[[i]] <- unlist(l[[i]])
            columnNames <- union(columnNames, names(l[[i]]))
        }
    }
    
    # second pass: go through each list element and add its elements to correct column
    df <- matrix(nrow = length(l), ncol = length(columnNames))
    if (length(l) > 0) {
        for (i in 1:(length(l))) {
            df[i, match(names(l[[i]]), columnNames)] <- l[[i]]
        }
    }
    colnames(df) <- columnNames

    # check whether list contains valid row names, and if true; use them
    if (length(l) < 1 || is.null(names(l)) || is.na(names(l)) || length(names(l)) != length(l)) { 
        rownames(df) <- NULL
    } else { rownames(df) <- names(l) }
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

.list2string <- function(lst) {
    if(is.null(names(lst))) return(paste(lst, sep=", "))

    final <- character(length(lst)*2)
    paste(mapply(function(name, val) {paste0(name, ': "', encodeString(val), '"')}, names(post), post), collapse=", ")
}
