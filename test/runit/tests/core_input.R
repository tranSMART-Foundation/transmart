source("web-app/HeimScripts/_core/input.R")

loaded_variables_with_a_vector <- list(should_be_df = c(1,2,3))  # a Vector instead of a df
loaded_variables_df <- data.frame(a = c(1,2,3))  # data.frame instead of a list
loaded_variables_proper1 <- list(box1_n1_s1 = data.frame(a = c(1,2,3) ) )

HDDproper   <- data.frame(Row.Label = letters[1:5], Bio.marker = letters[1:5], NumVect = 1:5 )
ClinicalNumericProper <- data.frame(Row.Label = letters[1:5], NumVect = 1:5 )


# Case of loaded_variables containing smt else than a dataframe
test.validateLoadedVariables1 <- function() {
	checkException( validateLoadedVariables(loaded_variables_with_a_vector) )
}

# Case of loaded_variables of type data.frame instead of list
test.validateLoadedVariables2 <- function() {
	checkException( validateLoadedVariables(loaded_variables_df ) )
}

# Case of a proper loaded_variable
test.validateLoadedVariables3 <- function() {
	# the inner checkException will throw an exception because there is no exception thrown
	# the outer checkException catches it and validates to true
	checkException(checkException( validateLoadedVariables(loaded_variables_proper1  ) ))
}

test.isHDD1 <- function() {
	checkException( isHDD(ClinicalNumericProper) )
}

test.isHDD2 <- function() {
	checkTrue( isHDD(HDDproper) )
}
