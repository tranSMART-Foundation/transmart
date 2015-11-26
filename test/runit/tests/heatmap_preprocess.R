source("web-app/HeimScripts/heatmap/preprocess.R")

test_data <- data.frame(Row.Label=c("a1","a2","b1","b2") , Bio.marker= c("a","a","b","b"), Assay1 = 1:4, Assay2 = 4:1  )

# When aggregate is FALSE do not alter dataframe in any way
#test.main.donotaggregate <- function()
#{
#  main(aggregate=FALSE)
#  result <- loaded_variables["label"]
#  expected <- test_data
#  checkEquals(result,expected)
#}

#Dashes are allowed in probe_ids
test.aggregate <- function()
{
  result <- aggregate.probes(test_data)
  expected <- data.frame(Row.Label=c("a1","b1"), Bio.marker = c("a","b"), Assay1=c(1,3), Assay2 = c(4,2), stringsAsFactors = F)
  checkEquals(result,expected)
}
