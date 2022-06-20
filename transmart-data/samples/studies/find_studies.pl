#!/usr/bin/perl -w

open(DATASETS,"../studies/datasets") || die "Cannot find ../studies/datasets";

@targets = ("browse", "samples", "clinical", "ref_annotation", "acgh", "expression",
	    "metabolomics", "mirna", "mirnaqpcr", "mirnaseq",
	    "msproteomics", "proteomics", "rbm", "rnaseq", "vcf");

foreach $t (@targets) {$target{$t}=1}

%found = ();
$found = 0;
while(<DATASETS>) {
    chomp;
    @col = split(/\s+/);
    $study = $col[0];
    $tname = $col[1];
    if(defined($target{$tname})) {
	$found++;
	$found{$study}++;
    }
}
close DATASETS;

if($found) {
#    open(OUT, ">findstudies.txt") || die "Cannot create findstudies.txt";
    foreach $f (sort(keys(%found))){
#	print OUT "$f\n";
	print "$f\n";
    }
#    close OUT;
}
