source("web-app/HeimScripts/heatmap/downloadData.R")

.setUp <- function() {
  assign("origDirectory", getwd(), envir = .GlobalEnv)
  dir <- tempdir()
  dir.create(dir)
  setwd(dir)
}

.tearDown <- function() {
  dir <- getwd()
  setwd(origDirectory)
  unlink(dir, recursive = T, force = T)
}


test.zip.success <- function() {
  write("foobar1", "params.json")
  write("foobar2", "heatmap_data.tsv")
  write("foobar3", "heatmap_orig_values.tsv")

  main()

  checkTrue(file.exists('analysis_data.zip'))

  zipFiles <- unzip(zipfile = 'analysis_data.zip', list = T)$Name
  checkTrue(identical(sort(c("params.json",
                             "heatmap_data.tsv",
                              "heatmap_orig_values.tsv")),
                      sort(zipFiles)))
}

test.zip.misingfile <- function() {
  checkException(main())
}
