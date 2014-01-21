#!/usr/bin/groovy

import groovy.lang.Grapes
@Grapes([
  @Grab(group='net.sf.opencsv', module='opencsv', version='2.3'),
])
import au.com.bytecode.opencsv.CSVReader

def err = System.err.&println

if (args.length < 2) {
    err 'Usage: verify.groovy <expectedResultsFolder> <actualResultsfolder>'
    System.exit(1)
}

File expectedFolder = new File(args[0])
File actualFolder = new File(args[1])

if (!expectedFolder.exists()) {
    err "expected folder $expectedFolder does not exist"
    System.exit(1)
}

if (!actualFolder.exists()) {
    err "actual folder $actualFolder does not exist"
    System.exit(1)
}

def outputFilename = 'outputFile'
File expectedOutputFile = new File(expectedFolder, outputFilename)
File actualOutputFile = new File(expectedFolder, outputFilename)

if (!expectedOutputFile.exists()) {
    err "expected file $expectedOutputFile does not exist"
    System.exit(1)
}

if (!actualOutputFile.exists()) {
    err "expected file $actualOutputFile does not exist"
    System.exit(1)
}

println "Comparing $outputFilename..."
if (compareCSVFiles(expectedOutputFile, actualOutputFile, System.err)) {
    System.exit(1)

}

def boolean compareCSVFiles(File file1, File file2, PrintStream out = System.out) {
    CSVReader reader1 = new CSVReader(file1.newReader())
    CSVReader reader2 = new CSVReader(file2.newReader())
    def expected  = null
    def actual  = null
    boolean done = false
    int idx = 0

    while (!done) {
        expected = reader1.readNext()
        actual = reader2.readNext()

        if (expected != actual) {
            out "Found difference in line $idx: Expected $expected Actual $actual"
            return false
        }
        idx++

        done = (expected == null)
    }
    return true
}
