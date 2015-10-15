# You have several variables available here

# data.cohort1 # a list containing all concepts assigned to cohort 1 (defined via register in the input view)
# data.cohort2 # a list containing all concepts assigned to cohort 2 (defined via register in the input view)
#
# settings # a list containing all settings you specified in the input view
#
# output # This empty list must contain all data you want in your visualization once this script has finished


#### EXAMPLE ####

# remember registerConceptBox('somedata', [1], ...) ?
# This is where it appears again
data <- data.cohort1$somedata

# remember getSettings()?
# Here the list entries of 'settings' map whatever map you returned in the input view!
doStuff <- strtoi(settings$doStuff)

# do some statistics or data filtering if you want
abc <- c()
if (doStuff) {
	abc <- foobar(data)
}

# don't bother where output comes from or where it goes
# Just put everything what you need in your visualization in it
output$data <- data
output$foobarResults <- abc