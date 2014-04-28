\name{getSubjects}
\alias{getSubjects}
\title{
  Obtain a list of all subjects in a certain study.
}
\description{
  This function will retrieve a list of all subjects and their personal information for a certain study.
}
\usage{
getSubjects(study.name, as.data.frame = TRUE)
}

\arguments{
  \item{study.name}{a character string giving the name of a study}
  \item{as.data.frame}{logical. If TRUE (default setting) the subject information is returned as a data.frame. If set to FALSE the function returns the information as a list.}
}
\details{ The function will return for each subject the subjectID and their personal information, such as for example sex, age and ethnicity.

 
 }
\value{
  A data.frame (or list) is returned, whereby each row (or list element) contains the information partaining to
  one subject.
}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}
\note{To be able to access a transmart database, you need to be connected to the server the database is on. If you haven't connected to the server yet, establish a connection using the \code{\link{connectToTransmart}} function.}
\seealso{\code{\link{getStudies}}}
\examples{
\dontrun{
    # The following will retrieve a data.frame with subject-information for the study "foo"
    getSubjects("foo")
    }
}

\keyword{ database }
\keyword{ transmart }
