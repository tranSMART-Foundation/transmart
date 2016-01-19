
main <- function() {
    demo <- paste(remoteScriptDir, "/sourced.R", sep="")
    source(demo)
    res <- testFun()
    list("shouldBeTest"=res)
}

