\name{getConcepts}
\alias{getConcepts}

\title{
Obtain a list of all the concepts for a certain study, accompanied by information about these concepts.
}
\description{
This function will a retrieve a list of all the concepts for a certain study. This list contains for each concept the name and full name of the concept and the link to the location of this concept on the server.

Concepts are used in tranSMART to organise the stored study data. They are the folders and nodes on the left-hand side in tranSMART's data explorer.
There can be clinical concepts such as age, diagnosis, blood pressure, and high dimensional concepts such as mRNA expression values or copy number variation.

}
\usage{
getConcepts(study.name, as.data.frame = TRUE, cull.columns = TRUE)
}

\arguments{
  \item{study.name}{a character string giving the name of a study}
  \item{as.data.frame}{logical (default setting is TRUE): should the list containing the concept information be converted to a data.frame?}
  \item{cull.columns}{logical. If TRUE (default setting) several non-essential columns are excluded.}
}
\details{ The \code{"link.highdim.href"} column can  be used to find out if high dimensional data is available for a certain study, and if it is available: which high dimensional datasets are available for this study. For details, see the next section ("Value"). 
}

\value{
A data.frame or list with information about all the concepts in a certain study.
The following components may be present, depending on the server definitions, the data and the cull.columns settings:

\item{name}{a character string containing the name of the concept}
\item{key}{a character string. Similar to \code{fullName} column. Not present if cull.columns is set to TRUE}
\item{fullName}{a character string containing the full (path) name of the concept }
\item{api.link.self.href}{a character string containing the link ("\code{concept.link}") pointing to the location of the data of this concept on the server. }
\item{api.link.observations.href}{a character string or empty value. If  clinical observation data is available for this study, then this data.frame column or list element is present. For the concepts that contain clinical observation data, the field in the "\code{api.link.observations.href}" column contains a string representation of the link to the location of the data on the server. If a concept does not contain clinical observation data, this field will be empty.The "\code{api.link.observations.href}" column can therefore be used to find out which clinical datasets are available for a certain study.}

\item{link.highdim.ref}{a character string or empty value. If  high dimensional data is available for this study, then this data.frame column or list element is present. For the concepts that contain high dimensional data, the field in the "\code{link.highdim.href}" column contains a string representation of the link to the location of the data on the server. If a concept does not contain high dimensional data, this field will be empty. The "\code{link.highdim.href}" column can therefore be used to find out which high dimensional datasets are available for a certain study.}

}
\references{}

\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}

\note{To be able to access a transmart database, you need to be connected to the server the database is on. If you haven't connected to the server yet, establish a connection using the \code{\link{connectToTransmart}} function.}

\seealso{\code{\link{getStudies}}}

\examples{
\dontrun{
  # obtain a list of all studies in the database 
  studies <- getStudies()
  
  # select the name of one study from the list of studies
  study1 <- studies$name[[1]]
  
  # the following call will give all concepts for study1
  concepts <- getConcepts(study1)
  }

}

\keyword{ database }
\keyword{ transmart }
