
main <- function() {

    if (!exists('parseInput')) {
        stop("core/input.R is not properly sourced")
    }
    return(list(success=TRUE))
}
