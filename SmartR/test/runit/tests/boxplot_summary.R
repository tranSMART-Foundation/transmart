source("web-app/HeimScripts/boxplot/summary.R")


### main test data ###
test_set <-
  data.frame(
    Row.Label = c("1007_s_at", "1053_at", "117_at", "121_at", "1255_g_at"),
    clinical_numeric = c(179.26, NA, 0, 0.00734,-23.0234),
    stringsAsFactors = F
  )

test_set_measurements <-
  test_set["clinical_numeric"]

test_data <- list("n0_s1" = test_set)
test_data_measurements <- list("n0_s1" = test_set_measurements)

test_set_preprocessed  <- test_set
colnames(test_set_preprocessed) <-
  c("Row.Label" ,"clinical_numeric_n0_s1")

.setUp <- function() {
  # A .png file is created by produce_boxplot. Should be written to temporary directory
  assign("origDirectory", getwd(), envir = .GlobalEnv)
  dir <- tempdir()
  dir.create(dir)
  setwd(dir)

  #loaded_variables and preprocessed are sometimes removed by a test function
  assign("loaded_variables", test_data, envir = .GlobalEnv)
}

.tearDown <- function() {
  dir <- getwd()
  setwd(origDirectory)
  unlink(dir, recursive = T, force = T)
}



### unit tests for function extract_measurements ###

#test if function works if data has a "Row.Label" and a "Bio.marker" column
test.extract_measurements.simplecase1 <- function() {
  checkEquals(test_data_measurements, extract_measurements(test_data))
}

#test if it works if data has "Row.Label" column but no "Bio.marker" column
test.extract_measurements.simplecase2 <- function() {
  test_data_tmp <-
    list("n0_s1" = test_set[, c("Row.Label", "clinical_numeric")])
  checkEquals(test_data_measurements, extract_measurements(test_data_tmp))
}
