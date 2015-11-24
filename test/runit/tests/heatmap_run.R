source("web-app/HeimScripts/heatmap/run.R")

# Test slash being removed - issue that used to broke sorting
test.fixString.slash.case <- function()
{
  testString <- "a/b"
  result <- fixString(testString)
  expected <- "ab"
  checkEquals(result,expected)
}

#Dashes are allowed in probe_ids
test.fixString.dash.case <- function()
{
  testString <- "a-b"
  result <- fixString(testString)
  expected <- "a-b"
  checkEquals(result,expected)
}
