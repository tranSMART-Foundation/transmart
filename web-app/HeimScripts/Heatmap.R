# You have several variables available here

# lowDimData # contains your low dimensional data
# highDimData_cohort1 # contains your high dimensional data for cohort 1
# highDimData_cohort2 # contains your high dimensional data for cohort 2
# 
# settings # contains all settings you specified in the input view
# 
# output # This empty list must contain all data you want in your visualization once the script has finished


# EXAMPLE
data <- lowDimData$data
doStuff <- strtoi(settings$doStuff)

abc <- c()
if (doStuff) {
	abc <- foobar(data)
}

output$data <- data
output$foobarResults <- abc