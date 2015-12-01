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

test.mergeFetchedData.singledf <- function()
{
  df1 <- data.frame(Row.Label=c("a1","a2","b1","b2") , Bio.marker= c("a","a","b","b"), Assay1 = 1:4, Assay2 = 4:1  )
  test_loaded_variables <- list(d1=df1)
  result <- mergeFetchedData(test_loaded_variables)
  expected <- data.frame(Row.Label=c("a1","a2","b1","b2") , Bio.marker= c("a","a","b","b"), Assay1_d1 = 1:4, Assay2_d1 = 4:1  )
  checkEquals(result,expected)
}

test.mergeFetchedData.twodfs <- function()
{
  df1 <- data.frame(Row.Label=c("a1","a2","b1","b2") , Bio.marker= c("a","a","b","b"), Assay1 = 1:4, Assay2 = 4:1  )
  df2 <- data.frame(Row.Label=c("a1","a2","b1","b2") , Bio.marker= c("a","a","b","b"), Assay3 = 1:4, Assay4 = 4:1  )
  test_loaded_variables <- list(d1=df1, d2=df2)
  merged <- mergeFetchedData(test_loaded_variables)
  checkTrue(all(colnames(merged) == c("Row.Label","Bio.marker","Assay1_d1","Assay2_d1","Assay3_d2","Assay4_d2")))
  checkTrue(nrow(merged) == 4)
}
