#!/usr/bin/perl -w

$findstudy = $ARGV[0];
$msg = "";

defined($ENV{TRANSMARTDATA}) || die "Undefined path: TRANSMARTDATA";
$datatop = $ENV{TRANSMARTDATA};

(-d "$datatop") || die "Cannot find directory TRANSMARTDATA: '$datatop'";

chdir("$datatop") || die "Failed to chdir to $datatop";

open(DATASETS,"samples/studies/datasets") || die "Cannot find samples/studies/datasets";

@targets = ("browse", "samples", "clinical", "ref_annotation", "acgh", "expression",
	    "metabolomics", "mirna", "mirnaqpcr", "mirnaseq",
	    "msproteomics", "proteomics", "rbm", "rnaseq", "vcf");

foreach $t (@targets) {$target{$t}=1}

%study = ();

while(<DATASETS>) {
    chomp;
    @col = split(/\s+/);
    $study = $col[0];
    if($study ne $findstudy) {next}
    $tname = $col[1];
    if(defined($target{$tname})) {
	$study{$tname}=1;
    }
}
close DATASETS;

if(defined($study{$findstudy})) {
    mkdir "samples/studies/$findstudy";
    open(LOADSTUDY,">samples/studies/$findstudy/loadstudy.params") || die "Cannot create loadstudy.params";
    foreach $t (sort(keys(%study))){
	print LOADSTUDY "$t\n";
    }
    close LOADSTUDY;
}
    
sub msg($) {
    my ($txt) = @_;
    print $txt;
    $msg .= $txt;
}
