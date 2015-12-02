source("web-app/HeimScripts/heatmap/preprocess.R")

test_data_aggregate <- data.frame(Row.Label=c("a1","a2","b1","b2") , Bio.marker= c("a","a","b","b"), Assay1 = c(4,1,4,1), Assay2 = c(4,1,4,1)  )

test_data_drop_empty <- data.frame(Bio.marker=c("","a",NA),b=c(1,2,3) )

#Simple case
test.aggregate <- function()
{
  result <- aggregate.probes(test_data_aggregate)
  expected <- data.frame(Row.Label=c("a1","b1"), Bio.marker = c("a","b"), Assay1=c(4,4), Assay2 = c(4,4), stringsAsFactors = F)
  checkEquals(result, expected)
}

test.drop.empty <- function()
{
  result <- dropEmptyGene(test_data_drop_empty)
  expected <- test_data_drop_empty[2,]
  checkEquals(result, expected)
}

