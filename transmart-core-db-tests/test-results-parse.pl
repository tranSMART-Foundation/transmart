#!/usr/bin/perl -w

$file = $ARGV[0];
open(IN, "$file") || die "Failed to open output file $file";

$unitTests = 0;
while (<IN>) {
    if(/^\| Running 98 unit tests\.\.\.$/){$unitTests = 1}
    if($unitTests) {
	if(/\| Completed (\d+) unit tests, (\d+) failed in \d+m \d+s/) {
	    $unitDone = $1;
	    $unitFail = $2;
	}
    }
}

close IN;
