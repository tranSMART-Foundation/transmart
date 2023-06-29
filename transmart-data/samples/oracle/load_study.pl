#!/usr/bin/perl -w


$loadstudy = $ARGV[0];
$doreload = 1;
$msg = "";
$jobClinical = $jobExpression = $jobRnaseq = 0;

if(defined($ARGV[1]) && $ARGV[1] eq "-resume") {$doreload = 0; print "Resuming $loadstudy\n";}
if(defined($ARGV[1]) && $ARGV[1] eq "-reload") {$doreload = 1; print "Reloading $loadstudy\n";}

defined($ENV{TRANSMARTDATA}) || die "Undefined path: TRANSMARTDATA";
$datatop = $ENV{TRANSMARTDATA};

(-d "$datatop") || die "Cannot find directory TRANSMARTDATA: '$datatop'";

chdir("$datatop") || die "Failed to chdir to $dbtop";

open(DATASETS,"samples/studies/datasets") || die "Cannot find samples/studies/datasets";

while(<DATASETS>) {
    chomp;
    @col = split(/\s+/);
    $study = $col[0];
    $target = $col[1];
    $url = $col[2];
    ($file) = ($url =~ /\/([^\/]+)$/g);

    if($target eq "browse") {$browse{$study} = $file}
    if($target eq "samples") {$samples{$study} = $file}
    if($target eq "clinical") {$clinical{$study} = $file}
    if($target eq "ref_annotation") {$ref_annotation{$study} = $file}
    if($target eq "expression") {$expression{$study} = $file}
    if($target eq "rnaseq") {$rnaseq{$study} = $file}
}
close DATASETS;

sub msg($) {
    my ($txt) = @_;
    print $txt;
    $msg .= $txt;
}

$nload = 0;
$annotation = 0;

# Process in this order:
# Browse Study (and missing Program) - can be loaded without clinical and other data
# Clinical - can be missing e.g. for one Sanofi test study
# Ref_Annotation - Load (or check) all annotation platforms
# High-dimensional data in alphabetical order:
#	acgh expression metabolomics mirna mirnaqpcr mirnaseq msproteomics proteomics rbm rnaseq vcf

if(defined($browse{$loadstudy})) {
    $browsestudy = $browseprogram = 0;
    msg("Browse loading\n");
    if(-e "samples/studies/$loadstudy/$browse{$loadstudy}") {
	unlink("samples/studies/$loadstudy/$browse{$loadstudy}");
    }
    system "make -C samples/oracle load_browse_$loadstudy 2>\&1 | tee browse.log ";
    open(BLOG, "browse.log") || die "Cannot read browse.log";
    while(<BLOG>) {
	if(/program '.*' found with ID \d+/){
	    $browseprogram = 1;
	    msg("   Program exists\n");
	}
	if(/Created program with ID '\d+'/){
	    $browseprogram = 2;
	    msg("   Program created\n");
	}
	if(/study '(.*)' found with ID \d+/){
	    $browsestudy = 1;
	    $studyname = $1;
	    msg("   Study '$studyname' exists\n");
	}
	if(/Created study '(.*)' with ID '\d+'/){
	    $browsestudy = 2;
	    $studyname = $1;
	    msg("   Study '$studyname' created\n");
	}
    }
    close BLOG;
    if($browsestudy && $browseprogram) {
	++$nload;
	msg("Browse success\n\n");
    } else {
	msg("Browse FAILED\n\n");
    }
}

if(defined($clinical{$loadstudy})) {
    $clinical = 0;
    msg("Clinical loading\n");
    if(-e "samples/studies/$loadstudy/$clinical{$loadstudy}") {
	if($doreload) {
	    msg("   Clinical reloading\n");
	    unlink("samples/studies/$loadstudy/$clinical{$loadstudy}");
	} else {
	    msg("   Clinical already loaded\n");
	    $clinical = 1;
	}
    }
    if(!$clinical) {
	$clintime = "unknown time";
	system "make -C samples/oracle load_clinical_$loadstudy 2>\&1 > clinical.log ";
	open(CLOG, "clinical.log") || die "Cannot read clinical.log";
	while(<CLOG>) {
	    if(/Write to log.0 - top_node = .*\\([^\\]+)\\$/) {
		$clinstudyname = $1;
		msg("   Clinical study: '$clinstudyname'\n");
	    }
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   Clinical did NOT complete successfully\n");
		$clinicalFail = 1;
	    }
	    if(/create_clinical_data - Finished  entry \[run i2b2_load_clinical_data\] \(result=\[false\]\)/) {
		msg("   Clinical i2b2_load_clinical_data false\n");
		$clinicalFail = 1;
	    }
	    if(/Write to log[.]0 - job_id = (\d+)/) {
		$jobClinical = $1;
	    }
	    if(/create_clinical_data - Finished job entry \[run i2b2_load_clinical_data\] \(result=\[true\]\)/) {
		msg("   Clinical i2b2_load_clinical data completed\n");
		$clinical = 1;
	    }
	    if(/create_clinical_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   Clinical: $_");
		$clinicalFail = 1;
	    }
	    if(/ - Processing ended after ([^.]+)[.]/) {
		$clintime = $1;
	    }
	}
	close CLOG;

	if($clinicalFail) {
	    system "make -C samples/oracle showdblog > clinical.showdblog";
	    open(CDBLOG, "clinical.showdblog");
	    $dashes = 0;
	    while(<CDBLOG>) {
		if (/^-----------------/){++$dashes}
		elsif($dashes == 2) {
		    msg("   Clinical showdblog: $_");
		}
		if(/[\|] ERRO/) {
		    msg("   Clinical showdblog: $_\n");
		}
		if(/[\|] FAIL/) {
		    msg("   Clinical showdblog: $_\n");
		}
	    }
	    close CDBLOG;
	}
    }
    if($clinical && !$clinicalFail) {
	++$nload;
	msg("Clinical success (job $jobClinical) $clintime\n\n");
    } else {
	msg("Clinical FAILED job $jobClinical\n\n");
    }
}

#if(defined($samples{$loadstudy})) {
#    msg("Loading samples\n");
#    ++$nload;
#}


# ref_annotation
# -reload only rechecks the ref_annotation target
# manually remove the annotation download if that needs to reload
# as other studies will also depend on it

if($clinical && defined($ref_annotation{$loadstudy})) {
    $annotation = 0;
    msg("Annotation loading\n");
    if(-e "samples/studies/$loadstudy/$ref_annotation{$loadstudy}") {
	unlink("samples/studies/$loadstudy/$ref_annotation{$loadstudy}");
    }
    system "make -C samples/oracle load_ref_annotation_$loadstudy 2>\&1 | tee ref_annotation.log ";
    open(ALOG, "ref_annotation.log") || die "Cannot read ref_annotation.log";
    while(<ALOG>) {
	if(/Platform (\S+) already loaded; skipping/) {
	    $platform = $1;
	    msg("   Annotation $1 already loaded\n");
	    $annotation = 1;
	}
	if(/^(\S+) platform$/){
	    $annotationType = $1;
	    msg("   Annotation $annotationType\n");
	}
	if(/make ([^\/]+)\/([^\/]?)_annotation.tar.xz/) {
	    $platform = $1;
	}
	if(/[.][.][.][.] Done after (\d+) rows/) {
	    msg("   Annotation loaded: $1 rows\n");
	    $annotation = 2;
	}
	if(/Platform (\S+) added to DE_GPL_INFO/) {
	    $platform = $1;
	    msg("   Annotation loaded rnaseq platform: $platform\n");
	    $annotation = 2;
	}
	if(/Completed in \d+ minutes \d+ seconds/) {
	    msg("   Annotation: $_");
	    $annotation = 2;
	}
    }
    close ALOG;

    if($annotation) {
	msg("Annotation success\n\n");
	++$nload;
    } else {
	msg("Annotation FAILED\n\n");
    }
}

if($annotation && defined($expression{$loadstudy})) {
    $expression = 0;
    msg("Expression loading\n");
    if(-e "samples/studies/$loadstudy/$expression{$loadstudy}") {
	if($doreload) {
	    msg("   Expression reloading\n");
	    unlink("samples/studies/$loadstudy/$expression{$loadstudy}");
	} else {
	    msg("   Expression already loaded\n");
	    $expression = 1;
	}
    }
    if(!$expression) {
	$expressiontime = "unknown time";
	system "make -C samples/oracle load_expression_$loadstudy 2>\&1 > expression.log ";
	open(ELOG, "expression.log") || die "Cannot read expression.log";
	while(<ELOG>) {
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   Expression did NOT complete successfully\n");
		$expressionFail = 1;
	    }
	    if(/load_gene_expression_data - Finished job entry \[run i2b2_process_mrna_data\] \(result=\[false\]\)/) {
		msg("   Expression i2b2_process_mrna_data false\n");
		$expressionFail = 1;
	    }
	    if(/Write parameters[.]0 - job_id = (\d+)/) {
		$jobExpression = $1;
	    }
	    if(/load_gene_expression_data - Finished job entry \[run i2b2_process_mrna_data\] \(result=\[true\]\)/) {
		msg("   Expression i2b2_load_gene_expression_data data completed\n");
		$expression = 1;
	    }
	    if(/load_gene_expression_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   Expression: $_");
		$expressionFail = 1;
	    }
	    if(/ - Processing ended after ([^.]+)[.]/) {
		$expressiontime = $1;
	    }
	}
	close ELOG;
	if($expressionFail) {
	    system "make -C samples/oracle showdblog > expression.showdblog";
	    open(EDBLOG, "expression.showdblog");
	    $dashes = 0;
	    while(<EDBLOG>) {
		if (/^-----------------/){++$dashes}
		elsif($dashes == 2) {
		    msg("   Expression showdblog: $_");
		}
		if(/[\|] ERRO/) {
		    msg("   Expression showdblog: $_\n");
		}
		if(/[\|] FAIL/) {
		    msg("   Expression showdblog: $_\n");
		}
	    }
	    close EDBLOG;
	}
    }
    if($expression && !$expressionFail) {
	++$nload;
	msg("Expression success (job $jobExpression) $expressiontime\n\n");
    } else {
	msg("Expression FAILED job $jobExpression\n\n");
    }
}

if($annotation && defined($rnaseq{$loadstudy})) {
    $rnaseq = 0;
    $rnaseqtime = "unknown time";
    msg("Rnaseq loading\n");
    if(-e "samples/studies/$loadstudy/$rnaseq{$loadstudy}") {
	if($doreload) {
	    msg("   Rnaseq reloading\n");
	    unlink("samples/studies/$loadstudy/$rnaseq{$loadstudy}");
	} else {
	    msg("   Rnaseq already loaded\n");
	    $rnaseq = 1;
	}
    }
    if(!$rnaseq) {
	system "make -C samples/oracle load_rnaseq_$loadstudy 2>\&1 > rnaseq.log ";
	open(RLOG, "rnaseq.log") || die "Cannot read rnaseq.log";
	while(<RLOG>) {
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   Rnaseq did NOT complete successfully\n");
		$rnaseqFail = 1;
	    }
	    if(/load_rna_data - Finished job entry \[run i2b2_process_rna_data\] \(result=\[false\]\)/) {
		msg("   Rnaseq i2b2_process_rna_data false\n");
		$rnaseqFail = 1;
	    }
	    if(/Write parameters[.]0 - job_id = (\d+)/) {
		$jobRnaseq = $1;
	    }
	    if(/load_rna_data - Finished job entry \[run i2b2_process_rna_data\] \(result=\[true\]\)/) {
		msg("   Rnaseq i2b2_load_rna_data data completed\n");
		$rnaseq = 1;
	    }
	    if(/load_rna_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   Rnaseq: $_");
		$rnaseqFail = 1;
	    }
	    if(/ - Processing ended after ([^.]+)[.]/) {
		$rnaseqtime = $1;
	    }
	}
	close RLOG;
	if($rnaseqFail) {
	    system "make -C samples/oracle showdblog > rnaseq.showdblog";
	    open(RDBLOG, "rnaseq.showdblog");
	    $dashes = 0;
	    while(<RDBLOG>) {
		if (/^-----------------/){++$dashes}
		elsif($dashes == 2) {
		    msg("   Rnaseq showdblog: $_");
		}
		if(/[\|] ERRO/) {
		    msg("   Rnaseq showdblog: $_\n");
		}
		if(/[\|] FAIL/) {
		    msg("   Rnaseq showdblog: $_\n");
		}
	    }
	    close RDBLOG;
	}
    }
    if($rnaseq &&!$rnaseqFail) {
	++$nload;
	msg("Rnaseq success (job $jobRnaseq) $rnaseqtime\n\n");
    } else {
	msg("Rnaseq FAILED job $jobRnaseq\n\n");
    }
}


print $msg;

if(!$nload) {
    print "FAILED: Cannot find targets for $loadstudy\n";
} else {
    if(defined($studyname)) {
	print "Study completed $loadstudy '$studyname'\n";
    } elsif(defined($clinstudyname)) {
	print "Study completed $loadstudy '$clinstudyname'\n";
    } else {
	print "Study completed $loadstudy 'unknown top node'\n";
    }
}
