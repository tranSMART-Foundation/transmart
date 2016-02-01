demo <- paste(remoteScriptDir, "/func_test/sourced.R", sep="")
source(demo)

main <- function() {
    res <- testFun()
    list("shouldBeTest"=res)
}

