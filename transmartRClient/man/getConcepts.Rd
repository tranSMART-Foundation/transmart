\name{getConcepts}
\alias{getConcepts}

\title{
Obtain a dataframe or list of all the concepts for a certain study, accompanied by information about these concepts.
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
  \item{study.name}{a character string giving the name of the study}
  \item{as.data.frame}{logical: should the list containing the concept information be converted to a dataframe?}
  \item{cull.columns}{logical. If TRUE (default setting) several non-essential columns are excluded.}
}
\details{ The "link.highdim.href" column can  be used to find out if high dimensional data is available for a certain study, and if so: which high dimensional datasets are available for this study. For details, see the next section ("Value"). }

\value{
A dataframe or list with information about all the concepts for a certain study.
The following components may be present, depending on the server definitions, data and the cull.columns settings:

\item{name}{a character string containing the name of the concept}
\item{key}{a character string ..COMPLETE... Not present if cull.columns is set to TRUE}% FINISH THIS ONE
\item{fullName}{a character string ...COMPLETE ... } % LIJKT HET ZELFDE ALS KEY
\item{api.link.self.href}{ a character string containing the link pointing ("concept.link") to the location of the data of this concept on the server. }
\item{api.link.observations.href}{ If  clinical observation data is available for this study, then this column or list element is present. For the concepts that contain clinical observation data, the field in the "api.link.observations.href" contains a stringrepresentation of the link to the location of the data on the server. If a concept does not contain clinical observation data, this field will be empty.}

\item{link.highdim.ref}{If  high dimensional data is available for this study, then an extra column or element is provided called "link.highdim.href". For the concepts that contain high dimensional data, the field in the "link.highdim.href" contains a stringrepresentation of the link to the location of the data on the server. If a concept does not contain high dimensional data, this field will be empty. The "link.highdim.href" column can therefore be used to find out which high dimensional datasets are available for a certain study.}



}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}
\note{}
\seealso{}
\examples{
## not run##

  # obtain a list of all studies in the database 
  studies <- getStudies()
  
  # select the name of one study from the list of studies
  study1 <- studies$name[[1]]
  
  # the following call will give all concepts for study1
  concepts <- getConcepts(study1)

}

\keyword{database}
