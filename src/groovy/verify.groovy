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
File actualOutputFile = new File(actualFolder, outputFilename)

if (!expectedOutputFile.exists()) {
    err "expected file $expectedOutputFile does not exist"
    System.exit(1)
}

if (!actualOutputFile.exists()) {
    err "expected file $actualOutputFile does not exist"
    System.exit(1)
}

println "Comparing $outputFilename..."
if (!compareCSVFiles(expectedOutputFile, actualOutputFile, System.err)) {
    System.exit(1)
}
println "$outputFilename matches"




// ************** DEFS *************

def boolean equalsIgnoresQuotes(String[] array1, String[] array2) {
    if (array1.length != array2.length) {
        return false
    }
    def it1 = array1.iterator()
    def it2 = array2.iterator()
    while (it1.hasNext()) {
        def v1 = unquoted(it1.next())
        def v2 = unquoted(it2.next())
        if (v1 != v2) {
            return false
        }
    }
    true
}

def String unquoted(String str) {

    if (str == null || str == '') {
        return str
    }
    str = str.trim()
    if (str.startsWith('"') && str.endsWith('"')) {
        str.substring(1, str.length() - 2)
    } else {
        str
    }
}

def boolean compareCSVFiles(File file1, File file2, PrintStream out = System.out) {
    CSVReader reader1 = new CSVReader(file1.newReader(), '\t' as char)
    CSVReader reader2 = new CSVReader(file2.newReader(), '\t' as char)
    def expected  = null
    def actual  = null
    boolean done = false
    int idx = 0

    while (!done) {
        expected = reader1.readNext()
        actual = reader2.readNext()

        if (expected != null && !equalsIgnoresQuotes(expected, actual)) {
            out.println "Found difference in line $idx:"
            out.println "Expected $expected"
            out.println "Actual $actual"
            return false
        }
        idx++

        done = (expected == null)
    }
    return true
}
