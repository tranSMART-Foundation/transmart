library('RUnit')
require("transmartRClient")

unittestsLocation <- system.file("unittests", package="transmartRClient")

test.suite <- defineTestSuite("highdimTests",
                              dirs = unittestsLocation,
                              testFileRegexp = "^runit.+\\.[rR]$")

test.result <- runTestSuite(test.suite)

printTextProtocol(test.result)
