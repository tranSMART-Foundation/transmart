\name{getPatientSet}
\alias{getPatientSet}
\title{
  Download a patient set by id.
}
\description{
  This function retrieves information about a patient set, including the patients in that set.
}
\usage{
getPatientSet(id)
}

\arguments{
  \item{id}{an integral number, the id of the patient set}
\details{
  The function will return a named list with properties of the patient set. Patient sets are created in the Transmart web interface or with the \code{\link{getPatientSetId}} call (still to be implemented). Currently there is no support in the legacy Transmart web interface to view the id of a patient set, but the new web app will support that.
}
}
\value{
  A named list with properties of the patient set. The \code{patients} element contains a list of patients.
}
\references{}
\author{Jan Kanis. 
Contact: development@thehyve.nl}
\note{To be able to access a transmart database, you need to be connected to the server the database is on. If you haven't connected to the server yet, establish a connection using the \code{\link{connectToTransmart}} function.}
\seealso{\code{\link{getSubjects}}}
\examples{
\dontrun{
    # The following will retrieve information on the patient set with id 28714
    getPatientSet(28714)
    }
}

\keyword{ database }
\keyword{ transmart }
