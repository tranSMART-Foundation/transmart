#!/usr/bin/perl -w


$loadstudy = $ARGV[0];
$doreload = 1;
$msg = "";

if(defined($ARGV[1]) && $ARGV[1] eq "-update") {$doreload = 0; print "Updating $loadstudy\n";}

defined($ENV{TRANSMARTDATA}) || die "Undefined path: TRANSMARTDATA";
$datatop = $ENV{TRANSMARTDATA};

(-d "$datatop") || die "Cannot find directory TRANSMARTDATA: '$datatop'";

chdir("$datatop") || die "Failed to chdir to $datatop";

open(DATASETS,"samples/studies/datasets") || die "Cannot find samples/studies/datasets";

while(<DATASETS>) {
    chomp;
    @col = split(/\s+/);
    $study = $col[0];
    if($study ne $loadstudy){next}
    $target = $col[1];
    $url = $col[2];
    ($file) = ($url =~ /\/([^\/]+)$/g);

    if($target eq "browse") {$browse{$study} = $file}
    if($target eq "samples") {$samples{$study} = $file}
    if($target eq "clinical") {$clinical{$study} = $file}
    if($target eq "ref_annotation") {$ref_annotation{$study} = $file}
    if($target eq "acgh") {$acgh{$study} = $file}
    if($target eq "expression") {$expression{$study} = $file}
    if($target eq "metabolomics") {$metabolomics{$study} = $file}
    if($target eq "mirna") {$mirna{$study} = $file}
    if($target eq "mirnaqpcr") {$mirnaqpcr{$study} = $file}
    if($target eq "mirnaseq") {$mirnaseq{$study} = $file}
    if($target eq "msproteomics") {$msproteomics{$study} = $file}
    if($target eq "proteomics") {$proteomics{$study} = $file}
    if($target eq "rbm") {$rbm{$study} = $file}
    if($target eq "rnaseq") {$rnaseq{$study} = $file}
    if($target eq "vcf") {$vcf{$study} = $file}
}
close DATASETS;

sub msg($) {
    my ($txt) = @_;
    print $txt;
    $msg .= $txt;
}

$nload = $nfail = 0;
$annotation = 0;

# Process in this order:
# Browse Study (and missing Program) - can be loaded without clinical and other data
# Clinical - can be issing e.g. for one Sanofi test study
# Ref_Annotation - Load (or check) all annotation platforms
# High-dimensional data in alphabetical order:
#	acgh expression metabolomics mirna mirnaqpcr mirnaseq msproteomics proteomics rbm rnaseq vcf

if(defined($browse{$loadstudy})) {
    $browsestudy = $browseprogram = 0;
    msg("Browse loading\n");
    if(-e "samples/studies/$loadstudy/$browse{$loadstudy}") {
	unlink("samples/studies/$loadstudy/$browse{$loadstudy}");
    }
    system "make -C samples/postgres load_browse_$loadstudy 2>\&1 | tee browse.log ";
    open(LOG, "browse.log") || die "Cannot read browse.log";
    while(<LOG>) {
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
	    $browsestudyname = $1;
	    msg("   Study '$browsestudyname' exists\n");
	}
	if(/Created study '(.*)' with ID '\d+'/){
	    $browsestudy = 2;
	    $browsestudyname = $1;
	    msg("   Study '$browsestudyname' created\n");
	}
    }
    close LOG;
    if($browsestudy && $browseprogram) {
	++$nload;
	msg("Browse success\n\n");
    } else {
	msg("Browse FAILED\n\n");
	++$nfail;
    }
}

# Clinical data
# =============

if(defined($clinical{$loadstudy})) {
    $clinical = 0;
    $jobClinical = 0;
    $failClinical = 0;
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
	$timeClinical = "unknown time";
	system "make -C samples/postgres load_clinical_$loadstudy 2>\&1 > clinical.log ";
	open(LOG, "clinical.log") || die "Cannot read clinical.log";
	while(<LOG>) {
	    if(/Write to log.0 - top_node = .*\\([^\\]+)\\$/) {
		$clinstudyname = $1;
		msg("   Clinical study: '$clinstudyname'\n");
	    }
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   Clinical did NOT complete successfully\n");
		$failClinical += 1;
	    }
	    if(/(create|increment)_clinical_data - Finished job entry \[run i2b2_load_clinical(_inc)?_data\] \(result=\[false\]\)/) {
		msg("   Clinical i2b2_load_clinical_data false\n");
		$failClinical += 2;
	    }
	    if(/Write to log[.]0 - job_id = (\d+)/) {
		$jobClinical = $1;
	    }
	    if(/(create|increment)_clinical_data - Finished job entry \[run i2b2_load_clinical(_inc)?_data\] \(result=\[true\]\)/) {
		msg("   Clinical i2b2_load_clinical data completed\n");
		$clinical = 1;
	    }
	    if(/(create|increment)_clinical_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   Clinical: $_");
		$failClinical += 4;
	    }
	    if(/Kitchen - Processing ended after ([^.]+)[.]/) {
		$timeClinical = $1;
	    }
	}
	close LOG;

	if($failClinical) {
	    system "make -C samples/postgres showdblog > clinical.showdblog";
	    open(DBLOG, "clinical.showdblog");
	    $dashes = 0;
	    while(<DBLOG>) {
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
	    close DBLOG;
	}
    }
    if($clinical && !$failClinical) {
	++$nload;
	msg("Clinical success (job $jobClinical) $timeClinical\n\n");
    } else {
	$binfail = sprintf("%03b", $failClinical);
	msg("Clinical FAILED ($binfail) job $jobClinical\n\n");
	++$nfail;
    }
}

if(defined($samples{$loadstudy})) {
    msg("Loading samples data to be added in a future release\n");
#    ++$nload;
}


# ref_annotation
# -reload only rechecks the ref_annotation target
# manually remove the annotation download if that needs to reload
# as other studies may also depend on it

if(defined($ref_annotation{$loadstudy})) {
    $annotation = $failAnnotation = $ref_annotation = 0;
    $timeAnnotation = "unknown time";
    msg("Annotation loading\n");
    if(-e "samples/studies/$loadstudy/$ref_annotation{$loadstudy}") {
	unlink("samples/studies/$loadstudy/$ref_annotation{$loadstudy}");
    }
    system "make -C samples/postgres load_ref_annotation_$loadstudy 2>\&1 | tee ref_annotation.log ";
    open(LOG, "ref_annotation.log") || die "Cannot read ref_annotation.log";
    while(<LOG>) {
	if(/WARNING.*: Platform (\S+) already loaded; skipping/) {
	    msg("   Annotation $1 already loaded\n");
	    $timeAnnotation = "already loaded";
	    $annotation = 1;
	}
	if(/^running load_([^_]+)_annotation.sh .*\/([^\/]*?)_annotation.tar.xz/){
	    $annotationType = $1;
	    $platform = $2;
	    msg("   Annotation $annotationType platform: $platform\n");
	}
	if(/make ([^\/]+)\/(.*?)_annotation.tar.xz/) {
	    $platform = $1;
	}
	if(/Number of rows uploaded: (\d+)/) {		# Success: expression
	    msg("   Annotation loaded: $1 rows\n");
	    $annotation = 2;
	}
	if(/load_([^_]+)_annotation - Finished job entry \[Success\] \(result=\[true\]\)/) {
	    msg("   Annotation loaded $1 platform: $platform\n");
	    $annotation = 2;
	}
	elsif(/load_qpcr_mirna_annotation - Finished job entry \[Success\] \(result=\[true\]\)/) {
	    msg("   Annotation loaded qpcr_mirna platform: $platform\n");
	    $annotation = 2;
	}
	if(/load_([^_]+)_annotation - Finished job entry .* \(result=\[false\]\)/) {
	    msg("   Annotation: $_");
	}
	if(/Kitchen - Processing ended after ([^.]+)[.]/) {
	    $timeAnnotation = $1;
	}
    }
    close LOG;

    if($failAnnotation) {
	msg("Annotation FAILED\n\n");
    } elsif($annotation) {
	msg("Annotation success $timeAnnotation\n\n");
	++$nload;
    } else {
	msg("Annotation FAILED\n\n");
	$failAnnotation = 1;
    }
    if($failAnnotation){
	$annotation = 0;
	++$nfail;
    }
}

if($annotation && defined($acgh{$loadstudy})) {
    $acgh = 0;
    msg("ArrayCGH loading\n");
    if(-e "samples/studies/$loadstudy/$acgh{$loadstudy}") {
	if($doreload) {
	    msg("   ArrayCGH reloading\n");
	    unlink("samples/studies/$loadstudy/$acgh{$loadstudy}");
	} else {
	    msg("   ArrayCGH already loaded\n");
	    $acgh = 1;
	}
    }
    if(!$acgh) {
	$jobAcgh = 0;
	$failAcgh = 0;
	$timeAcgh = "unknown time";
	system "make -C samples/postgres load_acgh_$loadstudy 2>\&1 > acgh.log ";
	open(LOG, "acgh.log") || die "Cannot read acgh.log";
	while(<LOG>) {
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   ArrayCGH did NOT complete successfully\n");
		$failAcgh += 1;
	    }
	    if(/load_acgh_data - Finished job entry \[run i2b2_process_acgh_data\] \(result=\[false\]\)/) {
		msg("   ArrayCGH i2b2_process_acgh_data false\n");
		$failAcgh += 2;
	    }
	    if(/Write parameters[.]0 - job_id = (\d+)/) {
		$jobAcgh = $1;
	    }
	    if(/load_acgh_data - Finished job entry \[run i2b2_process_acgh_data\] \(result=\[true\]\)/) {
		msg("   ArrayCGH i2b2_load_acgh_data completed\n");
		$acgh = 1;
	    }
	    if(/load_acgh_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   ArrayCGH: $_");
		$failAcgh += 4;
	    }
	    if(/Kitchen - Processing ended after ([^.]+)[.]/) {
		$timeAcgh = $1;
	    }
	}
	close LOG;
	if($failAcgh) {
	    system "make -C samples/postgres showdblog > acgh.showdblog";
	    open(DBLOG, "acgh.showdblog");
	    $dashes = 0;
	    while(<DBLOG>) {
		if (/^-----------------/){++$dashes}
		elsif($dashes == 2) {
		    msg("   ArrayCGH showdblog: $_");
		}
		if(/[\|] ERRO/) {
		    msg("   ArrayCGH showdblog: $_\n");
		}
		if(/[\|] FAIL/) {
		    msg("   ArrayCGH showdblog: $_\n");
		}
	    }
	    close DBLOG;
	}
    }
    if($acgh && !$failAcgh) {
	++$nload;
	msg("ArrayCGH success (job $jobAcgh) $timeAcgh\n\n");
    } else {
	$binfail = sprintf("%03b", $failAcgh);
	msg("ArrayCGH FAILED ($binfail) job $jobAcgh\n\n");
	++$nfail;
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
	$jobExpression = 0;
	$failExpression = 0;
	$timeExpression = "unknown time";
	system "make -C samples/postgres load_expression_$loadstudy 2>\&1 > expression.log ";
	open(LOG, "expression.log") || die "Cannot read expression.log";
	while(<LOG>) {
	    if(/Write parameters.0 - top_node = .*\\([^\\]+)\\$/) {
		$datastudyname = $1;
	    }
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   Expression did NOT complete successfully\n");
		$failExpression += 1;
	    }
	    if(/load_gene_expression_data - Finished job entry \[run i2b2_process_mrna_data\] \(result=\[false\]\)/) {
		msg("   Expression i2b2_process_mrna_data false\n");
		$failExpression += 2;
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
		$failExpression += 4;
	    }
	    if(/Kitchen - Processing ended after ([^.]+)[.]/) {
		$timeExpression = $1;
	    }
	}
	close LOG;
	if($failExpression) {
	    system "make -C samples/postgres showdblog > expression.showdblog";
	    open(DBLOG, "expression.showdblog");
	    $dashes = 0;
	    while(<DBLOG>) {
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
	    close DBLOG;
	}
    }
    if($expression && !$failExpression) {
	++$nload;
	msg("Expression success (job $jobExpression) $timeExpression\n\n");
    } else {
	$binfail = sprintf("%03b", $failExpression);
	msg("Expression FAILED ($binfail) job $jobExpression\n\n");
	++$nfail;
    }
}

if($annotation && defined($metabolomics{$loadstudy})) {
    $metabolomics = 0;
    msg("Metabolomics loading\n");
    if(-e "samples/studies/$loadstudy/$metabolomics{$loadstudy}") {
	if($doreload) {
	    msg("   Metabolomics reloading\n");
	    unlink("samples/studies/$loadstudy/$metabolomics{$loadstudy}");
	} else {
	    msg("   Metabolomics already loaded\n");
	    $metabolomics = 1;
	}
    }
    if(!$metabolomics) {
	$jobMetabolomics = 0;
	$failMetabolomics = 0;
	$timeMetabolomics = "unknown time";
	system "make -C samples/postgres load_metabolomics_$loadstudy 2>\&1 > metabolomics.log ";
	open(LOG, "metabolomics.log") || die "Cannot read metabolomics.log";
	while(<LOG>) {
	    if(/Write parameters.0 - top_node = .*\\([^\\]+)\\$/) {
		$datastudyname = $1;
	    }
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   Metabolomics did NOT complete successfully\n");
		$failMetabolomics += 1;
	    }
	    if(/load_metabolomic_data - Finished job entry \[run i2b2_process_metabolomic_data\] \(result=\[false\]\)/) {
		msg("   Metabolomics i2b2_process_metabolomics_data false\n");
		$failMetabolomics += 2;
	    }
	    if(/Write parameters[.]0 - job_id = (\d+)/) {
		$jobMetabolomics = $1;
	    }
	    if(/load_metabolomic_data - Finished job entry \[run i2b2_process_metabolomic_data\] \(result=\[true\]\)/) {
		msg("   Metabolomics i2b2_load_metabolomics_data completed\n");
		$metabolomics = 1;
	    }
	    if(/load_metabolomic_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   Metabolomics: $_");
		$failMetabolomics += 4;
	    }
	    if(/Kitchen - Processing ended after ([^.]+)[.]/) {
		$timeMetabolomics = $1;
	    }
	}
	close LOG;
	if($failMetabolomics) {
	    system "make -C samples/postgres showdblog > metabolomics.showdblog";
	    open(DBLOG, "metabolomics.showdblog");
	    $dashes = 0;
	    while(<DBLOG>) {
		if (/^-----------------/){++$dashes}
		elsif($dashes == 2) {
		    msg("   Metabolomics showdblog: $_");
		}
		if(/[\|] ERRO/) {
		    msg("   Metabolomics showdblog: $_\n");
		}
		if(/[\|] FAIL/) {
		    msg("  Metabolomics showdblog: $_\n");
		}
	    }
	    close DBLOG;
	}
    }
    if($metabolomics && !$failMetabolomics) {
	++$nload;
	msg("Metabolomics success (job $jobMetabolomics) $timeMetabolomics\n\n");
    } else {
	$binfail = sprintf("%03b", $failMetabolomics);
	msg("Metabolomics FAILED ($binfail) job $jobMetabolomics\n\n");
	++$nfail;
    }
}

if($annotation && defined($mirna{$loadstudy})) {
    $mirna = 0;
    msg("MiRNA loading\n");
    if(-e "samples/studies/$loadstudy/$mirna{$loadstudy}") {
	if($doreload) {
	    msg("   MiRNA reloading\n");
	    unlink("samples/studies/$loadstudy/$mirna{$loadstudy}");
	} else {
	    msg("   MiRNA already loaded\n");
	    $mirna = 1;
	}
    }
    if(!$mirna) {
	$jobMirna = 0;
	$failMirna = 0;
	$timeMirna = "unknown time";
	system "make -C samples/postgres load_mirna_$loadstudy 2>\&1 > mirna.log ";
	open(LOG, "mirna.log") || die "Cannot read mirna.log";
	while(<LOG>) {
	    if(/Write parameters.0 - top_node = .*\\([^\\]+)\\$/) {
		$datastudyname = $1;
	    }
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   MiRNA did NOT complete successfully\n");
		$failMirna += 1;
	    }
	    if(/load_mirna_data - Finished job entry \[run i2b2_process_mirna_data\] \(result=\[false\]\)/) {
		msg("   MiRNA i2b2_process_mirna_data false\n");
		$failMirna += 2;
	    }
	    if(/Write parameters[.]0 - job_id = (\d+)/) {
		$jobMirna = $1;
	    }
	    if(/load_mirna_data - Finished job entry \[run i2b2_process_mirna_data\] \(result=\[true\]\)/) {
		msg("   MiRNA i2b2_load_mirna_data completed\n");
		$mirna = 1;
	    }
	    if(/load_mirna_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   MiRNA: $_");
		$failMirna += 4;
	    }
	    if(/Kitchen - Processing ended after ([^.]+)[.]/) {
		$timeMirna = $1;
	    }
	}
	close LOG;
	if($failMirna) {
	    system "make -C samples/postgres showdblog > mirna.showdblog";
	    open(DBLOG, "mirna.showdblog");
	    $dashes = 0;
	    while(<DBLOG>) {
		if (/^-----------------/){++$dashes}
		elsif($dashes == 2) {
		    msg("   MiRNA showdblog: $_");
		}
		if(/[\|] ERRO/) {
		    msg("   MiRNA showdblog: $_\n");
		}
		if(/[\|] FAIL/) {
		    msg("   MiRNA showdblog: $_\n");
		}
	    }
	    close DBLOG;
	}
    }
    if($mirna && !$failMirna) {
	++$nload;
	msg("MiRNA success (job $jobMirna) $timeMirna\n\n");
    } else {
	$binfail = sprintf("%03b", $failMirna);
	msg("MiRNA FAILED ($binfail) job $jobMirna\n\n");
	++$nfail;
    }
}

if($annotation && defined($mirnaqpcr{$loadstudy})) {
    $mirnaqpcr = 0;
    msg("MiRNAqpcr loading\n");
    if(-e "samples/studies/$loadstudy/$mirnaqpcr{$loadstudy}") {
	if($doreload) {
	    msg("   MiRNAqpcr reloading\n");
	    unlink("samples/studies/$loadstudy/$mirnaqpcr{$loadstudy}");
	} else {
	    msg("   MiRNAqpcr already loaded\n");
	    $mirnaqpcr = 1;
	}
    }
    if(!$mirnaqpcr) {
	$jobMirnaqpcr = 0;
	$failMirnaqpcr = 0;
	$timeMirnaqpcr = "unknown time";
	system "make -C samples/postgres load_mirnaqpcr_$loadstudy 2>\&1 > mirnaqpcr.log ";
	open(LOG, "mirnaqpcr.log") || die "Cannot read mirnaqpcr.log";
	while(<LOG>) {
	    if(/Write parameters.0 - top_node = .*\\([^\\]+)\\$/) {
		$datastudyname = $1;
	    }
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   MiRNAqpcr did NOT complete successfully\n");
		$failMirnaqpcr += 1;
	    }
	    if(/load_qpcr_mirna_data - Finished job entry \[run i2b2_process_qpcr_mirna_data\] \(result=\[false\]\)/) {
		msg("   MiRNAqpcr i2b2_process_mirnaqpcr_data false\n");
		$failMirnaqpcr += 2;
	    }
	    if(/Write parameters[.]0 - job_id = (\d+)/) {
		$jobMirnaqpcr = $1;
	    }
	    if(/load_qpcr_mirna_data - Finished job entry \[run i2b2_process_qpcr_mirna_data\] \(result=\[true\]\)/) {
		msg("   MiRNAqpcr i2b2_load_mirnaqpcr_data completed\n");
		$mirnaqpcr = 1;
	    }
	    if(/load_qpcr_mirna_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   MiRNAqpcr: $_");
		$failMirnaqpcr += 4;
	    }
	    if(/Kitchen - Processing ended after ([^.]+)[.]/) {
		$timeMirnaqpcr = $1;
	    }
	}
	close LOG;
	if($failMirnaqpcr) {
	    system "make -C samples/postgres showdblog > mirnaqpcr.showdblog";
	    open(DBLOG, "mirnaqpcr.showdblog");
	    $dashes = 0;
	    while(<DBLOG>) {
		if (/^-----------------/){++$dashes}
		elsif($dashes == 2) {
		    msg("   MiRNAqpcr showdblog: $_");
		}
		if(/[\|] ERRO/) {
		    msg("   MiRNAqpcr showdblog: $_\n");
		}
		if(/[\|] FAIL/) {
		    msg("   MiRNAqpcr showdblog: $_\n");
		}
	    }
	    close DBLOG;
	}
    }
    if($mirnaqpcr && !$failMirnaqpcr) {
	++$nload;
	msg("MiRNAqpcr success (job $jobMirnaqpcr) $timeMirnaqpcr\n\n");
    } else {
	$binfail = sprintf("%03b", $failMirnaqpcr);
	msg("MiRNAqpcr FAILED ($binfail) job $jobMirnaqpcr\n\n");
	++$nfail;
    }
}

if($annotation && defined($mirnaseq{$loadstudy})) {
    $mirnaseq = 0;
    msg("MiRNAseq loading\n");
    if(-e "samples/studies/$loadstudy/$mirnaseq{$loadstudy}") {
	if($doreload) {
	    msg("   MiRNAseq reloading\n");
	    unlink("samples/studies/$loadstudy/$mirnaseq{$loadstudy}");
	} else {
	    msg("   MiRNAseq already loaded\n");
	    $mirnaseq = 1;
	}
    }
    if(!$mirnaseq) {
	$jobMirnaseq = 0;
	$failMirnaseq = 0;
	$timeMirnaseq = "unknown time";
	system "make -C samples/postgres load_mirnaseq_$loadstudy 2>\&1 > mirnaseq.log ";
	open(LOG, "mirnaseq.log") || die "Cannot read mirnaseq.log";
	while(<LOG>) {
	    if(/Write parameters.0 - top_node = .*\\([^\\]+)\\$/) {
		$datastudyname = $1;
	    }
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   MiRNAseq did NOT complete successfully\n");
		$failMirnaseq += 1;
	    }
	    if(/load_qpcr_mirna_data - Finished job entry \[run i2b2_process_qpcr_mirna_data\] \(result=\[false\]\)/) {
		msg("   MiRNAseq i2b2_process_qpcr_mirna_data false\n");
		$failMirnaseq += 2;
	    }
	    if(/Write parameters[.]0 - job_id = (\d+)/) {
		$jobMirnaseq = $1;
	    }
	    if(/load_qpcr_mirna_data - Finished job entry \[run i2b2_process_qpcr_mirna_data\] \(result=\[true\]\)/) {
		msg("   MiRNAseq i2b2_load_qpcr_mirna_data completed\n");
		$mirnaseq = 1;
	    }
	    if(/load_qpcr_mirna_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   MiRNAseq: $_");
		$failMirnaseq += 4;
	    }
	    if(/Kitchen - Processing ended after ([^.]+)[.]/) {
		$timeMirnaseq = $1;
	    }
	}
	close LOG;
	if($failMirnaseq) {
	    system "make -C samples/postgres showdblog > mirnaseq.showdblog";
	    open(DBLOG, "mirnaseq.showdblog");
	    $dashes = 0;
	    while(<DBLOG>) {
		if (/^-----------------/){++$dashes}
		elsif($dashes == 2) {
		    msg("   MiRNAseq showdblog: $_");
		}
		if(/[\|] ERRO/) {
		    msg("   MiRNAseq showdblog: $_\n");
		}
		if(/[\|] FAIL/) {
		    msg("   MiRNAseq showdblog: $_\n");
		}
	    }
	    close DBLOG;
	}
    }
    if($mirnaseq && !$failMirnaseq) {
	++$nload;
	msg("MiRNAseq success (job $jobMirnaseq) $timeMirnaseq\n\n");
    } else {
	$binfail = sprintf("%03b", $failMirnaseq);
	msg("MiRNAseq FAILED ($binfail) job $jobMirnaseq\n\n");
	++$nfail;
    }
}

if($annotation && defined($msproteomics{$loadstudy})) {
    $msproteomics = 0;
    msg("MsProteomics loading\n");
    if(-e "samples/studies/$loadstudy/$msproteomics{$loadstudy}") {
	if($doreload) {
	    msg("   MsProteomics reloading\n");
	    unlink("samples/studies/$loadstudy/$msproteomics{$loadstudy}");
	} else {
	    msg("   MsProteomics already loaded\n");
	    $msproteomics = 1;
	}
    }
    if(!$msproteomics) {
	$jobMsproteomics = 0;
	$failMsproteomics = 0;
	$timeMsproteomics = "unknown time";
	system "make -C samples/postgres load_msproteomics_$loadstudy 2>\&1 > msproteomics.log ";
	open(LOG, "msproteomics.log") || die "Cannot read msproteomics.log";
	while(<LOG>) {
	    if(/Write parameters.0 - top_node = .*\\([^\\]+)\\$/) {
		$datastudyname = $1;
	    }
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   MsProteomics did NOT complete successfully\n");
		$failMsproteomics += 1;
	    }
	    if(/load_proteomics_data - Finished job entry \[run i2b2_process_proteomics_data\] \(result=\[false\]\)/) {
		msg("   MsProteomics i2b2_process_msproteomics_data false\n");
		$failMsproteomics += 2;
	    }
	    if(/Write parameters[.]0 - job_id = (\d+)/) {
		$jobMsproteomics = $1;
	    }
	    if(/load_proteomics_data - Finished job entry \[run i2b2_process_proteomics_data\] \(result=\[true\]\)/) {
		msg("   MsProteomics i2b2_load_msproteomics_data completed\n");
		$msproteomics = 1;
	    }
	    if(/load_proteomics_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   MsProteomics: $_");
		$failMsproteomics += 4;
	    }
	    if(/Kitchen - Processing ended after ([^.]+)[.]/) {
		$timeMsproteomics = $1;
	    }
	}
	close LOG;
	if($failMsproteomics) {
	    system "make -C samples/postgres showdblog > msproteomics.showdblog";
	    open(DBLOG, "msproteomics.showdblog");
	    $dashes = 0;
	    while(<DBLOG>) {
		if (/^-----------------/){++$dashes}
		elsif($dashes == 2) {
		    msg("   MsProteomics showdblog: $_");
		}
		if(/[\|] ERRO/) {
		    msg("   MsProteomics showdblog: $_\n");
		}
		if(/[\|] FAIL/) {
		    msg("   MsProteomics showdblog: $_\n");
		}
	    }
	    close DBLOG;
	}
    }
    if($msproteomics && !$failMsproteomics) {
	++$nload;
	msg("MsProteomics success (job $jobMsproteomics) $timeMsproteomics\n\n");
    } else {
	$binfail = sprintf("%03b", $failMsproteomics);
	msg("MsProteomics FAILED ($binfail) job $jobMsproteomics\n\n");
	++$nfail;
    }
}

if($annotation && defined($proteomics{$loadstudy})) {
    $proteomics = 0;
    msg("Proteomics loading\n");
    if(-e "samples/studies/$loadstudy/$proteomics{$loadstudy}") {
	if($doreload) {
	    msg("   Proteomics reloading\n");
	    unlink("samples/studies/$loadstudy/$proteomics{$loadstudy}");
	} else {
	    msg("   Proteomics already loaded\n");
	    $proteomics = 1;
	}
    }
    if(!$proteomics) {
	$jobProteomics = 0;
	$failProteomics = 0;
	$timeProteomics = "unknown time";
	system "make -C samples/postgres load_proteomics_$loadstudy 2>\&1 > proteomics.log ";
	open(LOG, "proteomics.log") || die "Cannot read proteomics.log";
	while(<LOG>) {
	    if(/Write parameters.0 - top_node = .*\\([^\\]+)\\$/) {
		$datastudyname = $1;
	    }
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   Proteomics did NOT complete successfully\n");
		$failProteomics += 1;
	    }
	    if(/load_proteomics_data - Finished job entry \[run i2b2_process_proteomics_data\] \(result=\[false\]\)/) {
		msg("   Proteomics i2b2_process_proteomics_data false\n");
		$failProteomics += 2;
	    }
	    if(/Write parameters[.]0 - job_id = (\d+)/) {
		$jobProteomics = $1;
	    }
	    if(/load_proteomics_data - Finished job entry \[run i2b2_process_proteomics_data\] \(result=\[true\]\)/) {
		msg("   Proteomics i2b2_load_proteomics_data completed\n");
		$proteomics = 1;
	    }
	    if(/load_proteomics_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   Proteomics: $_");
		$failProteomics += 4;
	    }
	    if(/Kitchen - Processing ended after ([^.]+)[.]/) {
		$timeProteomics = $1;
	    }
	}
	close LOG;
	if($failProteomics) {
	    system "make -C samples/postgres showdblog > proteomics.showdblog";
	    open(DBLOG, "proteomics.showdblog");
	    $dashes = 0;
	    while(<DBLOG>) {
		if (/^-----------------/){++$dashes}
		elsif($dashes == 2) {
		    msg("   Proteomics showdblog: $_");
		}
		if(/[\|] ERRO/) {
		    msg("   Proteomics showdblog: $_\n");
		}
		if(/[\|] FAIL/) {
		    msg("   Proteomics showdblog: $_\n");
		}
	    }
	    close DBLOG;
	}
    }
    if($proteomics && !$failProteomics) {
	++$nload;
	msg("Proteomics success (job $jobProteomics) $timeProteomics\n\n");
    } else {
	$binfail = sprintf("%03b", $failProteomics);
	msg("Proteomics FAILED ($binfail) job $jobProteomics\n\n");
	++$nfail;
    }
}

if($annotation && defined($rbm{$loadstudy})) {
    $rbm = 0;
    msg("RBM loading\n");
    if(-e "samples/studies/$loadstudy/$rbm{$loadstudy}") {
	if($doreload) {
	    msg("   RBM reloading\n");
	    unlink("samples/studies/$loadstudy/$rbm{$loadstudy}");
	} else {
	    msg("   RBM already loaded\n");
	    $rbm = 1;
	}
    }
    if(!$rbm) {
	$jobRbm = 0;
	$failRbm = 0;
	$timeRbm = "unknown time";
	system "make -C samples/postgres load_rbm_$loadstudy 2>\&1 > rbm.log ";
	open(LOG, "rbm.log") || die "Cannot read rbm.log";
	while(<LOG>) {
	    if(/Write parameters.0 - top_node = .*\\([^\\]+)\\$/) {
		$datastudyname = $1;
	    }
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   RBM did NOT complete successfully\n");
		$failRbm += 1;
	    }
	    if(/load_rbm_data - Finished job entry \[i2b2_load_rbm_data\] \(result=\[false\]\)/) {
		msg("   RBM i2b2_process_rbm_data false\n");
		$failRbm += 2;
	    }
	    if(/Write parameters[.]0 - job_id = (\d+)/) {
		$jobRbm = $1;
	    }
	    if(/load_rbm_data - Finished job entry \[i2b2_load_rbm_data\] \(result=\[true\]\)/) {
		msg("   RBM i2b2_load_rbm_data completed\n");
		$rbm = 1;
	    }
	    if(/load_rbm_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   RBM: $_");
		$failRbm += 4;
	    }
	    if(/Kitchen - Processing ended after ([^.]+)[.]/) {
		$timeRbm = $1;
	    }
	}
	close LOG;
	if($failRbm) {
	    system "make -C samples/postgres showdblog > rbm.showdblog";
	    open(DBLOG, "rbm.showdblog");
	    $dashes = 0;
	    while(<DBLOG>) {
		if (/^-----------------/){++$dashes}
		elsif($dashes == 2) {
		    msg("   RBM showdblog: $_");
		}
		if(/[\|] ERRO/) {
		    msg("   RBM showdblog: $_\n");
		}
		if(/[\|] FAIL/) {
		    msg("   RBM showdblog: $_\n");
		}
	    }
	    close DBLOG;
	}
    }
    if($rbm && !$failRbm) {
	++$nload;
	msg("RBM success (job $jobRbm) $timeRbm\n\n");
    } else {
	$binfail = sprintf("%03b", $failRbm);
	msg("RBM FAILED ($binfail) job $jobRbm\n\n");
	++$nfail;
    }
}

if($clinical && $annotation && defined($rnaseq{$loadstudy})) {
    $rnaseq = 0;
    $jobRnaseq = 0;
    $timeRnaseq = "unknown time";
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
	$jobRnaseq = 0;
	$failRnaseq = 0;
	$timeRnaseq = "unknown time";
	system "make -C samples/postgres load_rnaseq_$loadstudy 2>\&1 > rnaseq.log ";
	open(LOG, "rnaseq.log") || die "Cannot read rnaseq.log";
	while(<LOG>) {
	    if(/Write parameters.0 - top_node = .*\\([^\\]+)\\$/) {
		$datastudyname = $1;
	    }
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   Rnaseq did NOT complete successfully\n");
		$failRnaseq += 1;
	    }
	    if(/load_rna_data - Finished job entry \[run i2b2_process_rna_data\] \(result=\[false\]\)/) {
		msg("   Rnaseq i2b2_process_rna_data false\n");
		$failRnaseq += 2;
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
		$failRnaseq += 4;
	    }
	    if(/Kitchen - Processing ended after ([^.]+)[.]/) {
		$timeRnaseq = $1;
	    }
	}
	close LOG;
	if($failRnaseq) {
	    system "make -C samples/postgres showdblog > rnaseq.showdblog";
	    open(DBLOG, "rnaseq.showdblog");
	    $dashes = 0;
	    while(<DBLOG>) {
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
	    close DBLOG;
	}
    }
    if($rnaseq &&!$failRnaseq) {
	++$nload;
	msg("Rnaseq success (job $jobRnaseq) $timeRnaseq\n\n");
    } else {
	$binfail = sprintf("%03b", $failRnaseq);
	msg("Rnaseq FAILED ($binfail) job $jobRnaseq\n\n");
	++$nfail;
    }
}

if($annotation && defined($vcf{$loadstudy})) {
    $vcf = 0;
    msg("VCF loading\n");
    if(-e "samples/studies/$loadstudy/$vcf{$loadstudy}") {
	if($doreload) {
	    msg("   VCF reloading\n");
	    unlink("samples/studies/$loadstudy/$vcf{$loadstudy}");
	} else {
	    msg("   VCF already loaded\n");
	    $vcf = 1;
	}
    }
    if(!$vcf) {
	$jobVcf = 0;
	$failVcf = 0;
	$timeVcf = "unknown time";
	system "make -C samples/postgres load_vcf_$loadstudy 2>\&1 > vcf.log ";
	open(LOG, "vcf.log") || die "Cannot read vcf.log";
	while(<LOG>) {
	    if(/Write parameters.0 - top_node = .*\\([^\\]+)\\$/) {
		$datastudyname = $1;
	    }
	    if(/Write to log 2[.]0 - did NOT complete successfully/) {
		msg("   VCF did NOT complete successfully\n");
		$failVcf += 1;
	    }
	    if(/load_vcf_data - Finished job entry \[run i2b2_process_vcf_data\] \(result=\[false\]\)/) {
		msg("   VCF i2b2_process_vcf_data false\n");
		$failVcf += 2;
	    }
	    if(/Write parameters[.]0 - job_id = (\d+)/) {
		$jobVcf = $1;
	    }
	    if(/load_vcf_data - Finished job entry \[run i2b2_process_vcf_data\] \(result=\[true\]\)/) {
		msg("   VCF i2b2_load_vcf_data completed\n");
		$vcf = 1;
	    }
	    if(/load_vcf_data - Finished job entry .* \(result=\[false\]\)/) {
		msg("   VCF: $_");
		$failVcf += 4;
	    }
	    if(/Kitchen - Processing ended after ([^.]+)[.]/) {
		$timeVcf = $1;
	    }
	}
	close LOG;
	if($failVcf) {
	    system "make -C samples/postgres showdblog > vcf.showdblog";
	    open(DBLOG, "vcf.showdblog");
	    $dashes = 0;
	    while(<DBLOG>) {
		if (/^-----------------/){++$dashes}
		elsif($dashes == 2) {
		    msg("   VCF showdblog: $_");
		}
		if(/[\|] ERRO/) {
		    msg("   VCF showdblog: $_\n");
		}
		if(/[\|] FAIL/) {
		    msg("   VCF showdblog: $_\n");
		}
	    }
	    close DBLOG;
	}
    }
    if($vcf && !$failVcf) {
	++$nload;
	msg("VCF success (job $jobVcf) $timeVcf\n\n");
    } else {
	$binfail = sprintf("%03b", $failVcf);
	msg("VCF FAILED ($binfail) job $jobVcf\n\n");
	++$nfail;
    }
}

print "Final summary\n";
print "-------------\n";
print "\n";

print $msg;

if(!$nload) {
    print "FAILED: Cannot find targets for $loadstudy\n";
} else { 
    if(defined($clinstudyname)) {
	print "Study completed $loadstudy '$clinstudyname'";
    } elsif(defined($datastudyname)) {
	print "Study completed $loadstudy '$datastudyname'";
    } elsif(defined($browsestudyname)) {
	print "Study completed $loadstudy '$browsestudyname'";
    } else {
	print "Study completed $loadstudy 'unknown top node'";
    }
    if($nfail) {
	print ": $nfail steps failed - see above for details";
    }
    print "\n";
}
