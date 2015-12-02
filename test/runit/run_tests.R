library(RUnit)

test.suite <- defineTestSuite("Heim",
                              dirs = file.path("test/runit/tests"),
                              testFileRegexp = ".*R$") # any R file

test.result <- runTestSuite(test.suite)

printTextProtocol(test.result)
