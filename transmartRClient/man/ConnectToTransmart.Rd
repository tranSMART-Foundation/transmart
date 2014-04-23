\name{connectToTransmart}
\alias{connectToTransmart}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
  Initializes a new connection to the tranSMART database via the RESTful API
}
\description{
  This function initializes the necessary connection settings to access the
  tranSMART database.
}
\usage{
connectToTransmart("http://localhost:8080/transmart", use.authentication = TRUE, ...)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{transmartDomain}{The domain of the tranSMART server}
  \item{use.authentication}{Whether this tranSMART server requires OAuth authentication}
  \item{\dots}{Passed on to authenticateWithTransmart}
}
\details{
  This function initialises the connection settings to a tranSMART server.
  If not disabled, it will also guide the user through the authentication process.
}
\value{
  This function will set up the necessary connection settings inside the \emph{transmartClientEnv}
  environment (if non-existent, this environment is created), which will enable this client to
  access the tranSMART database through the available get methods (eg. \code{getStudies()}).
}
\references{}
\author{Tim Dorscheidt, Jan Kanis, Rianne Jansen. 
Contact: development@thehyve.nl}
\note{}
\seealso{}
\examples{
  # Set the function's argument to the location of the tranSMART database 
  localTransmartDomain <- "http://localhost:8080/transmart"
  connectToTransmart(localTransmartDomain)
  # Follow the instructions for authentication.
}
\keyword{ environment }
\keyword{ connection }
\keyword{ database }
