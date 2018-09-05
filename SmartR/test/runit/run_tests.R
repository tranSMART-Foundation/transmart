library(RUnit)

test.suite <- defineTestSuite("Heim",
                              dirs = file.path("test/runit/tests"),
                              testFileRegexp = ".*R$") # any R file

test.result <- runTestSuite(test.suite)

printTextProtocol(test.result)
if (test.result$Heim$nFail > 0) {
    stop("Some Unit Tests failed - check the log for details.")
} else {
  print("All R tests passed.")
}
