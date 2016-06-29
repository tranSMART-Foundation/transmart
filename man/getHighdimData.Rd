\name{getHighdimData}
\alias{getHighdimData}

\title{
Obtain a high dimensional dataset for a certain study
}
\description{
This function will retrieve a highdimensional dataset for a specific concept of a certain study.
It retrieves the data from the server and parses it, converting the high dimensional data to a data.frame.
}

\usage{
getHighdimData(study.name, concept.match = NULL, concept.link = NULL, projection = NULL,
                data.constraints = list(), assay.constraints = list(),
                progress.download = .make.progresscallback.download(),
                progress.parse = .make.progresscallback.parse(),
                trial.name = NULL,
                patient.set = NULL,
                ontology.term = NULL,
                assay.ids = NULL,
                patient.ids = NULL,
                search.keywords = NULL,
                chromosome = NULL,
                genes = NULL,
                gene.ids = NULL,
                proteins = NULL,
                protein.ids = NULL,
                pathways = NULL,
                pathway.ids = NULL,
                gene.signatures = NULL,
                gene.signature.ids = NULL,
                snps = NULL,
                assay.or = NULL,
                data.or = NULL)
}
\arguments{
  \item{study.name}{a character string giving the name of a study.}
  \item{concept.match}{ a character string containing the concept name that should be matched. The \code{getHighdimData} function will search within the requested study for the first concept which name matches the given string value. It uses the name column of the result from \code{\link{getConcepts}} to perform the matching.}
  \item{concept.link}{a character string containing the link pointing to the location of the data on the server. Candidate values for this argument can be found in \code{api.link.self.href} column of the \code{\link{getConcepts}} results. It is the most exact way to refer to a concept, and it overwrites the \code{concept.match} argument.}
  \item{projection}{
  a character string specifying what part of the dataset should be returned: should all data be returned or only a certain type of data, for example only the log values for an mRNA data set. Candidate values can be obtained, see details section on how. (note: the types of data present may differ between studies).
}
  \item{data.constraints}{A list of raw data constraints. These are converted to JSON with \code{\link{toJSON}} and passed to the server as-is. For a more user friendly way to specify constraints, see the assay constraint parameters.}
  \item{assay.constraints}{Just like \code{data.constraints} but for assay constraints. See the data constraint parameters for a more user friendly syntax to pass such constraints.}
  \item{progress.download}{(The default should be fine for most users) A list of functions that will be called during the download, this allows displaying download progress to the user. The list must contain three items of the following signature:

  \code{%
    progress.download$start <- function(total) {\ldots}\cr
    progress.download$update <- function(current, total) {\ldots}\cr
    progress.download$end <- function() {\ldots}
  }

  \describe{
    \item{\code{start}}{is called before the download starts, with \code{total} set to the total number of bytes to be processed. \code{total} may be \code{NA} if this information is not available.}
    \item{\code{update}}{is called during the download, with \code{total} as the total number of bytes to be processed and \code{current} the current number of bytes that have been processed. \code{total} will generally be the same value as in the call to \code{start()}.}
    \item{\code{close}}{will be called once the download has finished.}
  }
  }
  \item{progress.parse}{(The default should be fine for most users) This argument functions identical to \code{progress.download} but the functions will be called before, during and after the parsing of the downloaded data. }

The remaining parameters are constraints that limit the amount of data that is returned.

Assay constraints:

  \item{trial.name}{A single character string with the trial name.}
  \item{patient.set}{A number indicating the patient set.}
  \item{ontology.term}{A single character string containing the concept path.}
  \item{assay.ids}{A numeric vector containing the id's of the assays you want to retrieve.}
  \item{patient.ids}{A character vector with the patient ids that you want to retrieve.}
  \item{assay.or}{Should be a named list containing multiple of the above assay constraints. The returned assays are those that match at least one of the specified constraints.}

Data constraints:

  \item{chromosome}{Either the name of a chromosome, or a named list containing names \code{chromosome}, \code{start} and \code{end}. \code{chromosome} should be a single character vector with the chromosome name, \code{start} and \code{end} are single numbers indicating the region of the chromosome you want to select.}
  \item{genes}{A character vector of gene symbols.}
  \item{gene.ids}{A character vector of NCBI gene accessions.}
  \item{proteins}{A character vector of protein names.}
  \item{protein.ids}{A character vector of UniProt ids.}
  \item{pathways}{A character vector of pathway names.}
  \item{pathway.ids}{A character vector of pathway names in the form \code{<database>:<db specific id>}.}
  \item{gene.signatures}{A character vector of gene signature names.}
  \item{gene.signature.ids}{A character vector of gene signature ids.}
  \item{snps}{A character vector of SNPs to select.}
  \item{data.or}{Similar to \code{assay.or}. A named list containing multiple of the above data constraints. The returned data will match at least one of those constraints.}

}

\details{
  Retrieving and parsing the data may take some time, depending on the size of the data requested and on your connection characteristics.

  A dataframe containing the high dimensional data will only be returned if a projection is specified.

  To discover what projections are possible, see \code{\link{highdimInfo}}. If you call \code{getHighDimData} while only providing a study name and concept, i.e. \code{getHighdimData(study.name, concept.match)} or \code{getHighDimData(study.name, concept.link)} it will also tell you the valid projections.


}
\value{
 If a projection is specified, this function returns a list containing the highdimensional data, with the following components:
 \item{data}{a dataframe containing the high dimensional data.}
 \item{labelToBioMarkerMap}{a hash describing which (column) labels refer to which bioMarker. This component will only be present if biomarker data is available for this particular dataset. See \code{\link{hash}} for more details about how to use hashes.}

If no projection is specified this function returns a list of the projections available for the requested study. No highdimensional data is returned. It will also print the instruction to set the projection argument and a list of the projections that are available.

}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen.
Contact: development@thehyve.nl}
\note{To be able to access a transmart database, you need to be connected to the server the database is on. If you haven't connected to the server yet, establish a connection using the \code{\link{connectToTransmart}} function.}
\seealso{\code{\link{hash}, \link{highdimInfo}, \link{getStudies}, \link{getConcepts}}.}

\examples{
\dontrun{

  # if a concept contains high dimensional data, use the following command to obtain this data:
  getHighdimData(study.name = "GSE8581", concept.match = "Lung")

  # you will be told that one of the listed projections needs to be selected:
  "No valid projection selected.
  Set the projection argument to one of the following options:
    default_real_projection
    zscore
    log_intensity
    all_data"

  # the following will return the actual data:
  data <- getHighdimData(study.name = "GSE8581", concept.match = "Lung", projection = "zscore")
  }
}


\keyword{ database }
\keyword{ transmart }
