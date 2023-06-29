#!/usr/bin/perl -w

$geoStudy = $ARGV[0];


open(LOG, ">parse$geoStudy.log") || die "Cannot open log file";
open(ERR, ">parse$geoStudy.err") || die "Cannot open error file";

%months = ("Jan" => "01", "Feb" => "02", "Mar" => "03", "Apr" => "04", "May" => "05", "Jun" => "06",
	   "Jul", => "07", "Aug"=> "08", "Sep" => "09", "Oct" => "10", "Nov" => "11", "Dec" => "12");

sub fixChars($) {
    my ($t) = @_;
    my $c = "";
    my $ic = 0;
    $t =~ s/;/, /g;
    $t =~ s/^\s+//g;
    $t =~ s/[,\s]+$//g;
    # fix known characters here
#    $t =~ tr//ue/;
    while($t =~ /([^A-Za-z\', -]+)/g) {
	$c = $1;
	print STDERR "fixChars found '$c' in '$t'\n";
	print STDERR "Characters:";
	foreach $x (split(//,$c)){
	    $ic = ord($x);
	    print STDERR " $ic";
	}
	print STDERR "\n";
    }
    return $t;
}

sub trimSpace($) {
    my ($t) = @_;
    $t =~ s/^\s+//g;
    $t =~ s/\s+$//g;
    return $t;
}

sub yesno($) {
    my ($a) = @_;
    if($a eq "1") {return "Yes"}
    if($a eq "0") {return "No"}
    if($a eq "unknown") {return "Unknown"}
    return "$a";
}

sub setValue($@) {
    my ($n,@v) = @_;

    if($n eq ""){$n = 0}

    return $v[$n];
}

sub setHash($%) {
    my ($v,%h) = @_;

    if(!defined($h{"$v"})) {return ""}

    return $h{"$v"};
}
    
sub getValue($) {
    my ($n) = @_;

    if(!defined($n)){$n = ""}

    return $n;
}

sub getBrowse($) {
    my ($n) = @_;

    if(!defined($n)){$n = "."}
    elsif($n eq ""){$n = "."}

    return $n;
}

sub getDate($) {
    my ($d) = @_;

    my $ret = ".";

    if($d =~ /([A-Z][a-z][a-z]) (\d+),? (\d+)$/) {
	my $mon = $1;
	my $d = $2;
	my $y = $3;
	if($y < 100) {$y += 2000}
	my $m = $months{$mon};
	$ret = sprintf "%04d-%02d-%02d", $y, $m, $d;
    } else {
	print STDERR "Unknown date format '$d'\n";
    }
}


%knownCV = (
    "CL:0000738" => "Leukocyte",
    "DOID:4" => "Disease",
    "DOID:0080600" => "COVID-19",
    "NCIT:C12529" => "Leukocyte",
    "NCIT:C53511" => "Intensive Care Unit",
    "NCIT:C70909" => "Mechanical ventilation",
    "UBERON:0003100" => "Female",
    "UBERON:0003101" => "Male",
    );

%taxId = ();
%taxName = ();
%taxCommon = ();
open(TAX, "taxonomy.dat") || die "Cannot find taxonomy.dat";
$iline = 0;
while(<TAX>){
    chomp;
    $iline++;
    s/[#].*//g;
    if(/^\s*$/) {next}
    @tax = split(/;\s+/);
    if(!defined($tax[1])) {print STDERR "Bad taxonomy.dat line $iline: $_\n"}
    $taxid = $tax[0];
    $taxname = $tax[1];
    if(defined($tax[2])){$taxcommon = $tax[2]}
    else{$taxcommon = $taxcommon}
    $taxName{$taxid} = $taxname;
    $taxId{$taxname} = $taxid;
    $taxCommon{$taxid} = $taxcommon;
}
close TAX;

%disId = ();
%disName = ();
%disCommon = ();
open(DIS, "diseases.dat") || die "Cannot find diseases.dat";
$iline = 0;
while(<DIS>){
    chomp;
    $iline++;
    s/[#].*//g;
    if(/^\s*$/) {next}
    @dis = split(/;\s+/);
    if(!defined($dis[1])) {print STDERR "Bad diseases.dat line $iline: $_\n"}
    $disid = $dis[0];
    $disname = $dis[1];
    if(defined($dis[2])){$discommon = $dis[2]}
    else{$discommon = $discommon}
    $disName{$disid} = $disname;
    $disId{$disname} = $disid;
    $disCommon{$disid} = $discommon;
}
close DIS;


%dataTypes = ("Expression profiling by array"                                    => "Expression",
	      "Expression profiling by genome tiling array"                      => "Expression",
	      "Expression profiling by high throughput sequencing"               => "RNAseq",
	      "Genome binding/occupancy profiling by high throughput sequencing" => "Binding",
	      "Methylation profiling by array"                                   => "Methylation",
	      "Methylation profiling by genome tiling array"                     => "Methylation",
	      "Methylation profiling by high throughput sequencing"              => "Methylation",
	      "Non-coding RNA profiling by array"                                => "ncExpression",
	      "Non-coding RNA profiling by high throughput sequencing"           => "ncRNAseq",
	      "CHIP-seq"                                                         => "CHIPseq",
	      "SNP genotyping by SNP array"                                      => "SNP",
	      "Other"                                                            => "Other"
    );

%dataTypeNames = ("Expression"   => "expression",
		  "ncExpression" => "expression",
		  "RNAseq"       => "rnaseq",
		  "ncRNAseq"     => "rnaseq",
		  "Binding"      => "other",
		  "Methylation"  => "rnaseq",
		  "CHIPseq"      => "chipseq",
		  "SNP"          => "snp",
		  "Other"        => "other",
    );
if(!(-s "$geoStudy.geo")) {
    system "wget -q https://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=$geoStudy -O $geoStudy.geo";
}
open(GEO, "$geoStudy.geo") || die "Cannot read data fetched from GEO in $geoStudy.geo";

$inGeo = 0;
while(<GEO>){
    chomp;
    if(!$inGeo && /<tr[^>]*><td>Status<\/td>/) {
	print STDERR "inGeo=1 header found\n";
	$inGeo = 1;
    }
    if(!$inGeo) {next}
    if(/<tr valign="top"><td colspan="2"><strong>Relations<\/strong><\/td><\/tr>/){
	$geoRelations = 1;
	print STDERR "GEO Relations found\n";
	next;
    } elsif(/^<br><table[^>]*><tr[^>]*><td[^>]*><strong>Supplementary file<\/strong><\/td>/) {
	$geoSupFiles = 1;
	print STDERR "GEO Supplementary files found\n";
	next;
    } elsif(/^<tr [^>]*valign="top"><td[^>]*>([^<]*)/) {
	$geoType = $1;
	print "GEO: $geoType\n";
	next;
    }

    if($geoSupFiles) {
    } elsif($geoRelations) {
	if(/^<td[^>]*>(.*)<\/td>$/) {
	    $geoData = $1;
	    if($geoType eq "BioProject") {
		print STDERR "BioProject data: $geoData\n";
	    } elsif($geoType eq "SRA") {
		print STDERR "SRA data: $geoData\n";
	    } else {
		print STDERR "Unknown Relation $geoType: $geoData\n";
	    }
	}
    } else {
	if(/^<td[^>]*>(.*)<\/td>$/) {
	    $geoData = $1;
	    if($geoType eq "Status") {
		print STDERR "   tStatus data: $geoData\n";
	    } elsif($geoType eq "Title") {
		print STDERR "   Title data: $geoData\n";
	    } elsif($geoType eq "Organism") {
		print STDERR "   Organism data: $geoData\n";
	    } elsif($geoType eq "Experiment type") {
		print STDERR "   Experiment type data: $geoData\n";
	    } elsif($geoType eq "Summary") {
		print STDERR "   Summary data: $geoData\n";
	    } elsif($geoType eq "\&nbsp;") {
		print STDERR "   Skip data: $geoData\n";
	    } elsif($geoType eq "Overall design") {
		print STDERR "   Overall design data: $geoData\n";
	    } elsif($geoType eq "Contributor(s)") {
		print STDERR "   Contributor data: $geoData\n";
	    } elsif($geoType eq "Citation(s)") {
		print STDERR "   Citation data: $geoData\n";
	    } elsif($geoType eq "Submission date") {
		print STDERR "   Submission data: $geoData\n";
	    } elsif($geoType eq "Last update date") {
		print STDERR "   Last update data: $geoData\n";
	    } elsif($geoType eq "Contact name") {
		print STDERR "   Contact data: $geoData\n";
	    } elsif($geoType eq "E-mail(s)") {
		print STDERR "   E-mail data: $geoData\n";
	    } elsif($geoType eq "Phone") {
		print STDERR "   Phone data: $geoData\n";
	    } elsif($geoType eq "Fax") {
		print STDERR "   Fax data: $geoData\n";
	    } elsif($geoType eq "URL") {
		print STDERR "   URL data: $geoData\n";
	    } elsif($geoType eq "Organization name") {
		print STDERR "   Organization data: $geoData\n";
	    } elsif($geoType eq "Department") {
		print STDERR "   Department data: $geoData\n";
	    } elsif($geoType eq "Lab") {
		print STDERR "   Lab data: $geoData\n";
	    } elsif($geoType eq "Street address") {
		print STDERR "   Street data: $geoData\n";
	    } elsif($geoType eq "City") {
		print STDERR "   City data: $geoData\n";
	    } elsif($geoType eq "State/province") {
		print STDERR "   State data: $geoData\n";
	    } elsif($geoType eq "ZIP/Postal code") {
		print STDERR "   ZIP data: $geoData\n";
	    } elsif($geoType eq "Country") {
		print STDERR "   Country data: $geoData\n";
	    } elsif($geoType =~ /Platforms \((\d+)\)/) {
		print STDERR "   Platform data: $geoData\n";
	    } elsif($geoType =~ /Samples \((\d+)\)/) {
		print STDERR "   Sample data: $geoData\n";
	    } elsif($geoType eq "Web link") {
		print STDERR "   Web link data: $geoData\n";
	    } elsif($geoType eq "Citation missing") {
		print STDERR "   Citation missing data: $geoData\n";
	    } else {
		print STDERR "Unknown getType $geoType: $geoData\n";
	    }
	}
    }
}
close GEO;

exit();

# Series metadata
#================

$seriesContributors = "";
$seriesRelation = "";


# find and set these values somehow

$studyTitle = $studySummary = "";
$studyOrganism = "";
$studyAuthor="Study";
$studyYear="unpublished";
$studyDataType = "UNKNOWN";	# RNAseq, Expression (text to appear in tree)
$studyDisease = "UNKNOWN";
$samplePlatform = "UNKNOWN";
$sampleTaxIdCount = 0;
$fixSubjectId = 0;

# Standard sample data
# ====================

%sample = ();

@sampleAccession = ();
@sampleId = ();
@sampleSupFile = ();
@sampleTissue = ();
@sampleType = ();
@samplePlatform = ();
@sampleTitle = ();
@sampleCellLine = ();
@sampleCellType = ();
@sampleRelationBiosample = ();
@sampleRelationSra = ();
@sampleRelationGeo = ();
@sampleRelationGeoby = ();
@sampleRelationOther = ();
@seriesSampleId = ();
@sampleTime = ();
@sampleDose = ();
@sampleGenotype = ();
@sampleOrganism = ();
@sampleCommonName = ();
@sampleTaxId = ();
@sampleMolecule = ();
@sampleOrigin = ();
@sampleLy6e = ();

# Standard clinical data
# ======================

@sampleId = ();
@clinicalSubjectId = ();
@clinicalStudyGroup = ();

@clinicalSource = ();
@clinicalStatus = ();

@clinicalInfection = ();
@clinicalCirrhosis = ();
@clinicalW = ();
@clinicalCell = ();

# Standard demographics data
# ==========================

@demographicsAge = ();
@demographicsGender = ();
@demographicsEthnicity = ();
@demographicsRace = ();
@demographicsHeight = ();
@demographicsWeight = ();

# Standard assay data
# ===================

# Set range to get StudyId within series from SampleId
%gsmRange = ("GSE....." => "GSM000001-GSM000002",
    );

foreach $sid (keys(%gsmRange)){
    $range = $gsmRange{$sid};
    ($rmin,$rmax) = ($range =~ /(GSM\d+)-(GSM\d+)/g);
    $gsmMin{$sid} = $rmin;
    $gsmMax{$sid} = $rmax;
}
#
#if(-e "allsamples.out") {
#    open(GSM, "allsamples.out") || die "Failed to open GSM samples extracted data";
#    while (<GSM>) {
#	chomp;
#	@col = split(/\t/);
#	$gsm{$col[3]} = $_;
#    }
#    close GSM;
#}

# Check for disease in directory name
# ===================================

if($filepath =~ /\/SARS\//) {$studyDisease = "D045169";$studyTopnode="SARS"}	       # SARS
elsif($filepath =~ /\/MERS\//) {$studyDisease = "D065207";$studyTopnode="MERS"}     # D018352: MERS but also general covid; D065207: MERS virus
elsif($filepath =~ /\/CoVid19\//) {$studyDisease = "D000086382";$studyTopnode="COVID-19"}  # CoVid19
elsif($filepath =~ /\/OtherCoV\//) {$studyDisease = "D018352";$studyTopnode="Coronavirus"} # coronavirus
elsif($filepath =~ /\/related\//) {$studyDisease = "D035061";$studyTopnode="Public Studies"} # healthy ferrets D035061: Control groups

%singleSample = ();
open(SINGLE, "/data/covid19/single.dat") || die "Cannot read single.dat";
while(<SINGLE>) {
   if(/^#/) {next}
   s/\s*#.*$//g;
   if(/^\s*$/) {next}
   if(/^(\S+)\/(\S+)/) {$singleSample{$2} = 1}
   if(/^(\S+)/) {$singleSample{$1} = 1}
}
close SINGLE;

open(SAMPLE, "/data/covid19/sample.dat") || die "Cannot read sample.dat";
while(<SAMPLE>) {
    if(/^#/) {next}
    if(/^\s*$/) {next}
    if(/^\s+(\S+)/) {
	$alias = $1;
	$sample{$alias} = $sample;
    } elsif(/^(\S+)$/) {
	$sample = $1;
	$sample{$sample} = $sample;
    } elsif(/^(\S+)\s+(.*)/) {
	$sample = $1;
	$rest = $2;
	$sample{$sample} = $sample;
	@alias = split(/\s+/,$rest);
	foreach $alias (@alias) {
	    if($alias ne "") {
		$sample{$alias} = $sample;
	    }
	}
    }
}
close SAMPLE;

open(ROWID, "/data/covid19/rowid.dat") || die "Cannot read rowid.dat";
while (<ROWID>) {
    if(/^#/) {next}
    if(/^\s*$/) {next}
    if(/^[*](\S+)/) {
	$row{$1} = 1;
#	print LOG "Multi  row '$1'\n";
    } elsif(/^(\S+)/) {
	$row{$1} = 1;
#	print LOG "Single row '$1'\n";
    }
}
close ROWID;

# Single value
sub countValue($$) {
    my($id,$value) = @_;
    $countId{$id}++;
    $countVal{"$id"}{"$value"}++;
}

# Array of values
sub countUnique($@) {
    my ($id, @data) = @_;
    my %values = ();
    $value1 = "";
    $value2 = "";
    my $nvalues = 0;
    my $count = ($id ne "");

    if($count){$countId{"$id"}++}
    foreach $d (@data) {
	$d =~ s/^\"(.*)\"$/$1/;
	$d =~ s/\"\"/\"/g;
	if($d eq "") {next}
	if($d eq "NA") {next}
	if($d eq "N/A") {next}
	if($count){$countVal{"$id"}{"$d"}++}
	$values{"$d"}++;
    }
    my @sorted = sort ({$values{$b} <=> $values{$a}} keys(%values));
    my $return = scalar keys %values;
    if($return > 0) {$value1 = $sorted[0]}
    if($return > 1) {$value2 = $sorted[1]}
    return $return;
}

# Test for more than one value in an array
# return 0 if all values are identical or array is empty
# return 1 if multiple values are found
sub testUnique(@) {
    my (@data) = @_;
    my %values = ();
    my $nvalues = 0;
    my $hasempty = 0;
    my $hasna = 0;
    foreach $d (@data) {
	if(!defined($d)){next}
	$d =~ s/^\"(.*)\"$/$1/;
	$d =~ s/\"\"/\"/g;
	if($d eq "") {$hasempty=1;next}
	if($d eq "NA") {$hasna=1;next}
	if($d eq "N/A") {$hasna=1;next}
	$values{"$d"}++;
    }
    my $return = (1 < ($hasempty + $hasna + scalar keys %values));
    return $return;
}

sub testUniqueBysubject(@) {
    my (@data) = @_;
    my %values = ();
    my %svalues = ();
    my $nvalues = 0;

    my $i = 0;
    my $s = "";
    foreach $d (@data) {
	$s = $clinicalSubjectId[$i++];
	if(!defined($d)){next}
	$d =~ s/^\"(.*)\"$/$1/;
	$d =~ s/\"\"/\"/g;
	if($d eq "") {next}
	if($d eq "NA") {next}
	if($d eq "N/A") {next}
	$values{"$d"}++;
	if(defined($svalues{"$s"}) && $svalues{"$s"} ne "$d") { # multiple values for subject
	    print STDERR "Bysubject '$s' '$svalues{$s}' '$d'\n";
	    return 0;
	}
	$svalues{"$s"} = "$d";
    }
    my $return = (0 < scalar keys %values);
    return $return;
}

######################
# Create output files
######################

$dataFile = "clinical_data$filenum{$filename}.txt";
open(MAPDATA, ">clinical_mapping$filenum{$filename}.txt") || die "Cannot open mapping file";
open(OUTDATA, ">$dataFile") || die "Cannot open column file";
open(OUTEXPRESS, ">ExpRawData-counts$filenum{$filename}.txt") || die "Cannot open expression data file";



#############################
# 1. Read series matrix file
#############################

open(IN, $seriesFilename) || die "Cannot read $seriesFilename";

%countId = ();
%countVal = ();

$sampchar=0;
$line = 0;
$expLine = 0;
$doExpression = 0;
while(<IN>) {
    $line++;
    chomp;
    s/\t\t/\t.\t/g;
    s/\t$/\t./g;
    s/ +$//g;
    s/LoM- Naive/LoM-Naive/g; # fix for misprints in GSE155286
    $linetype = "unknown";
    if(/^\!([^\t]+)\t(.*)/) {
	$linetype = $1;
	$rest = $2;
    }

    if($doExpression) {
	if(/^\!series_matrix_table_end$/) {
	    print ERR "Expression data ends at line $line\n";
	    print LOG "Expression data: $expLine lines\n";
	    last
	}
	$expLine++;

	if(/^\"([^\"\t]+)\"\t(.*)/) { # quoted probeId
	    $probe = $1;
	    $rest = $2;
	    if($probe eq "ID_REF") {
		s/\"//g;
		print OUTEXPRESS "$_\n";;
		next;
	    }
	}
	elsif(/^([^\t]+)\t(.*)/) { # unquoted probeId
	    $probe = $1;
	    $rest = $2;
	    if($probe eq "ID_REF") {
		s/\"//g;
		print OUTEXPRESS "$_\n";;
		next;
	    }
	}
	else {print ERR "+++Unable to parse expresssion on line $expLine: $_\n";last}
#	@expData = split(/\t/,$rest);
#	foreach $ed (@expData) {
#	}
	s/\"//g;
	print OUTEXPRESS "$_\n";
	next;
    }

    if(/^\!series_matrix_table_begin$/) {print ERR "Expression starts at line $line\n";$doExpression = 1;next}

    if($linetype ne "unknown") {
	$linetype =~ s/ /_/g;
	if(!defined($row{$linetype})) {
	    print ERR "+++Unknown rowid $line: '$linetype'\n";
	}

	# Series lines have single value
	# Some can be parsed to give per-sample values
	if($linetype =~ /^Series_/) {
	    if($linetype eq "Series_sample_id") {
		$rest =~ s/\"//g;
		$rest =~ s/ +$//g;
		@sampleId = split(/ /,$rest);
		++$known{"sampleId"};
		$unique = countUnique($linetype, @sampleId);
		$total = scalar @sampleId;
		if($unique != $total) {
		    print LOG "+++Duplicate sample id(s) ".($total-$unique)/$total."\n";
		}
		print LOG "'''$linetype:\n";
		print LOG "SAMPLEID: $total $unique '$value1' '$value2'\n";
		foreach $id (@sampleId) {
		    $foundSeries = "";
		    foreach $sid (keys(%gsmRange)){
			if($id ge $gsmMin{$sid} && $id le $gsmMax{$sid}) {$foundSeries = $sid}
		    }
		    push @seriesSampleId, $foundSeries;
		    ++$known{"seriesSampleId"};
		}
		$usedId{$linetype}++;
	    } elsif($linetype eq "Series_sample_taxid") {
		$rest =~ s/\"//g;
		$rest =~ s/ +$//g;
		print LOG "'''$linetype: $rest\n";
		if(defined($species{$rest})) {
		    $organism = $species{$rest};
		} else {
		    $organism = $rest;
		}
		if(defined($commonName{$rest})) {
		    $commonName = $commonName{$rest};
		} else {
		    $commonName = $rest;
		}
		$usedId{$linetype}++;
	    } elsif($linetype =~ /^Series_contact_(\S+)/) { # name, email, etc.
		$contactType = $1;
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		$studyContact{$contactType} = $rest;
		++$known{"studyContact$contactType"};
		if($contactType eq "name"){
		    ($name) = ($rest =~ /,([^,]+)$/g);
		    $name =~ s/^[A-Z][.]\s*//g;
		    if(defined($name)){
			$contactLastname = $name;
			$contactName = $rest;
		    }
		}
		countValue($linetype,$rest);
		$usedId{$linetype}++;
	    } elsif($linetype =~ /^Series_contributor/) { # line for each contributor
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		if($seriesContributors ne "") {$seriesContributors .= ";"}
		$seriesContributors .= $rest;	
		++$known{"seriesContributors"};
		($name) = ($rest =~ /,([^,]+)$/g);
		$name =~ s/^[A-Z][.]\s*//g;
		if(defined($name)){
		    $contribLastname = $name;
		    $contribName = $rest;
		}
		countValue($linetype,$rest);
		$usedId{$linetype}++;
	    } elsif($linetype =~ /^Series_citation/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
#		$seriesCitation = $rest;
#		++$known{"seriesCitation"};
		countValue($linetype,$rest);
		$usedId{$linetype}++;
	    } elsif($linetype =~ /^Series_geo_accession/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		$studyId = $rest;
		++$known{"studyId"};
		countValue($linetype,$rest);
	    } elsif($linetype =~ /^Series_relation/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		$seriesRelation .= $rest;
		++$known{"seriesRelation"};
		countValue($linetype,$rest);
		$usedId{$linetype}++;
	    } elsif($linetype =~ /^Series_supplementary_file/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		$seriesSupFile = $rest;
		countValue($linetype,$rest);
		++$known{"seriesSupFile"};
		$usedId{$linetype}++;
	    } elsif($linetype =~ /^Series_status/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		$studyStatus = $rest;
		++$known{"studyStatus"};
		if($studyStatus =~ /Public on (\S+ \d+ (\d+))$/) {
#		    $studyPublicDate = $1;
		    $studyYear = $2; # used for STUDY_NAME in .params
#		    ++$known{"studyPublicDate"};
		}
	    } elsif($linetype =~ /^Series_submission_date/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		$studySubmissionDate = $rest;
		++$known{"studySubmissionDate"};
	    } elsif($linetype =~ /^Series_last_update_date/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		$studyLastUpdateDate = $rest;
		++$known{"studyLastUpdateDate"};
	    } elsif($linetype =~ /^Series_overall_design/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		if($rest ne "" && defined($studyOverallDesign)){$studyOverallDesign .= "<br/><br/>"}
		$studyOverallDesign .= $rest;
		++$known{"studyOverallDesign"};
		$usedId{$linetype}++;
	    } elsif($linetype =~ /^Series_pubmed_id/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		$studyPubmedId = $rest;
		++$known{"studyPubmedId"};
		$usedId{$linetype}++;
	    } elsif($linetype =~ /^Series_summary/) { # multiple lines
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		if($studySummary ne "") {$studySummary .= "<br/><br/>"}
		$studySummary = $rest;
		++$known{"studySummary"};
		$usedId{$linetype}++;
	    } elsif($linetype =~ /^Series_type/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		$studyType = $rest;
		if(defined($dataTypes{$studyType})) {
		    $studyDataType = $dataTypes{$studyType};
		    ++$known{"studyDataType"};
		}
	    } elsif($linetype =~ /^Series_title/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		$studyTitle = $rest;
		++$known{"studyTitle"};
	    } elsif($linetype =~ /^Series_platform_id/) { # multiple platform lines possible
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		if(defined($studyPlatform)) {$studyPlatform .= ","}
		else {$studyPlatform = ""}
		$studyPlatform .= $rest;
	    } elsif($linetype =~ /^Series_platform_taxid/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		if(defined($species{$rest})) {
		    if(defined($studyOrganism) && $studyOrganism ne "") {
			if($studyOrganism !~ /$rest/) {$studyOrganism .= ",$species{$rest}"}
		    } else {$studyOrganism = $species{$rest}}
		    ++$known{"studyOrganism"};
		}
	    } elsif($linetype =~ /^Series_web_link/) {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "+++$linetype: '$rest'\n";
		$studyWebLink = $rest;
		++$known{"studyWebLink"};
	    } else {
		$rest =~ s/^\"(.*)\"$/$1/;
		$rest =~ s/\"\"/\"/g;
		print LOG "...$linetype: '$rest'\n";
		countValue($linetype,$rest);
	    }
	} elsif ($linetype =~ /^Sample_/) {
	    if($linetype =~ /^Sample_contact_(\S+)/) {
		@col = split(/\t/, $rest);
		$unique = countUnique($linetype,@col);
		$total = scalar @col;
		print LOG "===$linetype: $total $unique '$value1' '$value2'\n";
	    } elsif($linetype eq "Sample_geo_accession") {
		$rest =~ s/\"//g;
		@sampleacc = split(/\t/, $rest);
		$unique = countUnique($linetype,@sampleacc);
		$total = scalar @sampleacc;
		print LOG "===$linetype:\n";
		print LOG "SAMPLEACC: $total $unique '$value1' '$value2'\n";
		$i=0;
		foreach $val (@sampleacc) {
		    $sampleAccession[$i++] = $val;
		    ++$known{"sampleAccession"};
		}
		$usedId{$linetype}++;
	    } elsif($linetype eq "Sample_title") {
		$rest =~ s/\"//g;
		@sampleTitle = split(/\t/, $rest);
		++$known{"sampleTitle"};
		$unique = countUnique($linetype,@sampleTitle);
		$total = scalar @sampleTitle;
		print LOG "===$linetype:\n";
		print LOG "SAMPLETITLE: $total $unique '$value1' '$value2'\n";
		$usedId{$linetype}++;
	    } elsif($linetype eq "Sample_description") {
		$rest =~ s/\"//g;
		@sampleDescription = split(/\t/, $rest);
		++$known{"sampleDescription"};
		$unique = countUnique($linetype,@sampleDescription);
		$total = scalar @sampleDescription;
		print LOG "$linetype: $total $unique '$value1' '$value2'\n";
		$usedId{$linetype}++;
	    } elsif($linetype eq "Sample_data_processing") {
		$rest =~ s/\"//g;
		@dataProcessing = split(/\t/, $rest);
		$unique = countUnique($linetype,@dataProcessing);
		$total = scalar @dataProcessing;
		print LOG "$linetype: $total $unique '$value1' '$value2'\n";
		$i = 0;
		foreach $dp (@dataProcessing) {
		    if($dp ne "") {
			if(defined($sampleDataProcessing[$i])){
			    $sampleDataProcessing[$i] .= "; ";
			}
			$sampleDataProcessing[$i] .= $dataProcessing[$i];
			++$known{"sampleDataProcessing"};
		    }
		    $i++;
		}
		$usedId{$linetype}++;
	    } elsif($linetype eq "Sample_relation") {
		$rest =~ s/\"//g;
		@samplerelation = split(/\t/, $rest);
		$total = scalar @samplerelation;
		print LOG "===$linetype:\n";
		print LOG "SAMPLERELATION: $total $unique '$value1' '$value2'\n";
		$i = 0;
		foreach $val (@samplerelation) {
		    if($val =~ /^BioSample: *(.*)/) {
			$valRel = $1;
			if(defined($sampleRelationBiosample[$i])) {
			    $sampleRelationBiosample[$i] .= "; ";
			    $sampleRelationBiosample[$i++] .= $valRel;
			} else {
			    $sampleRelationBiosample[$i++] .= $valRel;
			}
			countValue("$linetype\_biosample",$valRel);
			$usedId{"$linetype\_biosample"}++;
		    }
		    elsif($val =~ /^SRA: *(.*)/) {
			$valRel = $1;
			if(defined($sampleRelationSra[$i])) {
			    $sampleRelationSra[$i] .= "; ";
			    $sampleRelationSra[$i++] .= $valRel;
			} else {
			    $sampleRelationSra[$i++] .= $valRel;
			}
			countValue("$linetype\_sra",$valRel);
			$usedId{"$linetype\_sra"}++;
		    }
		    elsif($val =~ /^Reanalysis of: *(.*)/) {
			$valRel = $1;
			if(defined($sampleRelationGeo[$i])) {
			    $sampleRelationGeo[$i] .= "; ";
			    $sampleRelationGeo[$i++] .= $valRel;
			} else {
			    $sampleRelationGeo[$i++] .= $valRel;
			}
			countValue("$linetype\_geo",$valRel);
			$usedId{"$linetype\_geo"}++;
		    }
		    elsif($val =~ /^Reanalyzed by: *(.*)/) {
			$valRel = $1;
			if(defined($sampleRelationGeoby[$i])) {
			    $sampleRelationGeoby[$i] .= "; ";
			    $sampleRelationGeoby[$i++] .= $valRel;
			} else {
			    $sampleRelationGeoby[$i++] .= $valRel;
			}
			countValue("$linetype\_geoby",$valRel);
			$usedId{"$linetype\_geobyb"}++;
		    }
		    elsif($val =~ /^([^:]+): *(.*)/) {
			$valType = $1;
			$valRel = $2;
			if(defined($sampleRelationOther[$i])) {
			    $sampleRelationOther[$i] .= "; ";
			    $sampleRelationOther[$i++] .= $valRel;
			} else {
			    $sampleRelationOther[$i++] .= $valRel;
			}
			countValue("$linetype\_$valType",$valRel);
			$usedId{"$linetype\_$valType"}++;
		    }
		}
	    } elsif($linetype =~ /^Sample_source_name_ch[12]$/) {
		@samplesource = split(/\t/, $rest);
		$unique = countUnique($linetype,@samplesource);
		$total = scalar @samplesource;
		$i = 0;
		foreach $s (@samplesource){
		    $s =~ s/\"//g;
		    $clinicalSource[$i] = $s;
		    ++$known{"clinicalSource"};
		    $i++;
		}
		print LOG "'''$linetype:\n";
		print LOG "SAMPLESOURCE: $total $unique $value1 $value2\n";
	    } elsif($linetype =~ /^Sample_taxid_ch[12]$/) {
		$sampleTaxIdCount++;
		$rest =~ s/\"//g;
		@taxid = split(/\t/, $rest);
		$unique = countUnique($linetype,@taxid);
		$total = scalar @taxid;
		print LOG "linetype: $total $unique $value1 $value2\n";
		$i = 0;
		foreach $id (@taxid) {
		    ++$known{"sampleTaxId"};
		    ++$known{"sampleOrganism"};
		    ++$known{"sampleCommonName"};
		    if(defined($species{$id})) {
			$organism = $species{$id};
			$commonName = $commonName{$id};
		    } else {
			$organism = $id;
			$commonName = $id;
			print LOG "UNKNOWN Sample_taxid '$id'\n";
		    }
		    if(defined($sampleTaxId[$i])) {
			$sampleTaxId[$i] .= ",".$id;
			$sampleOrganism[$i] .= ",".$organism;
			$sampleCommonName[$i] .= ",".$commonName;
		    } else {
			$sampleTaxId[$i] = $id;
			$sampleOrganism[$i] = $organism;
			$sampleCommonName[$i] = $commonName;
		    }
		    $i++;
		}
	        $usedId{$linetype}++;
	    } elsif($linetype =~ /^Sample_organism_ch[12]$/) {
		$rest =~ s/\"//g;
		@org = split(/\t/, $rest);
		$unique = countUnique($linetype,@org);
		$total = scalar @org;
		print LOG "linetype: $total $unique $value1 $value2\n";
		$i = 0;
		foreach $o (@org) {
		    ++$known{"sampleOrganism"};
		    $sampleOrganism[$i] = $o;
		    $i++;
		}
	        $usedId{$linetype}++;
	    } elsif($linetype =~ /^Sample_extract_protocol_ch[12]$/) {
		$rest =~ s/\"//g;
		@sampleExtractProtocol = split(/\t/, $rest);
		$unique = countUnique($linetype,@sampleExtractProtocol);
		$total = scalar @sampleExtractProtocol;
		print LOG "linetype: $total $unique $value1 $value2\n";
		++$known{"sampleExtractProtocol"};
		$usedId{$linetype}++;
	    } elsif($linetype =~ /^Sample_growth_protocol_ch[12]$/) {
		$rest =~ s/\"//g;
		@sampleGrowthProtocol = split(/\t/, $rest);
		$unique = countUnique($linetype,@sampleGrowthProtocol);
		$total = scalar @sampleGrowthProtocol;
		print LOG "linetype: $total $unique $value1 $value2\n";
		++$known{"sampleGrowthProtocol"};
		$usedId{$linetype}++;
	    } elsif($linetype =~ /^Sample_treatment_protocol_ch[12]$/) {
		$rest =~ s/\"//g;
		@sampleTreatmentProtocol = split(/\t/, $rest);
		$unique = countUnique($linetype,@sampleTreatmentProtocol);
		$total = scalar @sampleTreatmentProtocol;
		print LOG "linetype: $total $unique $value1 $value2\n";
		++$known{"sampleTreatmentProtocol"};
		$usedId{$linetype}++;
	    } elsif($linetype eq "Sample_supplementary_file") {
		$rest =~ s/\"//g;
		@samplesupfile = split(/\t/, $rest);
		$unique = countUnique($linetype,@samplesupfile);
		$total = scalar @samplesupfile;
		print LOG "===$linetype:\n";
		print LOG "SAMPLEACC: $total $unique '$value1' '$value2'\n";
		$i=0;
		foreach $val (@samplesupfile) {
		    $sampleSupFile[$i++] = $val;
		}
		++$known{"sampleSupplementaryFile"};
		$usedId{$linetype}++;
	    } elsif($linetype =~ /^Sample_characteristics_ch[12]$/) {
		$sampchar++;
		$rest =~ s/\"//g;
		@samplechar = split(/\t/, $rest);
		$unique = countUnique("",@samplechar);
		$total = scalar @samplechar;
		$i=0;
		@chartype = ();
		@charvalue = ();
		print LOG "'''$linetype:\n";
		foreach $c (@samplechar) {
		    if($c =~ /^[^:]+: [^,]+, [^:, ]+: /) {
			@cc = split(/, /, $c);
			foreach $cc (@cc) {
			    if($cc =~ /^(.*): (.*)$/) {
				$chartype = lc($1);
				$charvalue = $2;
				$chartype =~ s/ /_/g;
				if(!$i) {print LOG "combined $chartype: $charvalue\n"}
				countValue("$linetype\_$chartype",$charvalue);
				if(defined($sample{$chartype})) {
				    $chartype = $sample{$chartype};
				    $sampleCount{$chartype}++;
				    push @chartype, $chartype;
				    push @charvalue, $charvalue;
				    $used = 1;
				    if($chartype eq "age") {$demographicsAge[$i] = $charvalue; ++$known{"demographicsAge"}}
				    else{$used=0}
				    if($used){$usedId{"$linetype\_$chartype"}++}
				} else {
				    push @chartype, $chartype;
				    print LOG "***Unknown sampleChar1 $line:$i: '$chartype'\n";
				    $sample{$chartype} = "unknown";
				    $sampleCount{$chartype}++;
				}
			    }
			}
		    } elsif($c =~ /^([^:]+): (.*)$/) {
			$chartype = lc($1);
			$charvalue = $2;
			$chartype =~ s/ /_/g;
			if($studyId eq "GSE147507" && $chartype eq "strain"){$chartype = "virus_strain"}
			if(!$i) {print LOG " simple $chartype: $charvalue\n"}
			if(defined($sample{$chartype})) {
			    $chartype = $sample{$chartype};
			    $sampleCount{$chartype}++;
			    $charvalue =~ s/years, when baseline sample taken//g;
			    push @chartype, $chartype;
			    push @charvalue, $charvalue;
			    countValue("$linetype\_$chartype",$charvalue);
			    $used=1;
			    if($chartype eq "gender") {$demographicsGender[$i] = $charvalue; ++$known{"demographicsGender"}}
			    elsif($chartype eq "age") {$demographicsAge[$i] = $charvalue; ++$known{"demographicsAge"}}
			    elsif($chartype eq "hispanic") {$demographicsEthnicity[$i] = ($charvalue eq "H") ? "Hispanic" : "Non-hispanic"; ++$known{"demographicsEthnicity"}}
			    elsif($chartype eq "ethnicity") {$demographicsEthnicity[$i] = $charvalue; ++$known{"demographicsEthnicity"}}
			    elsif($chartype eq "race") {$demographicsRace[$i] = $charvalue; ++$known{"demographicsRace"}}
			    elsif($chartype eq "geographical_region") {$demographicsRegion[$i] = $charvalue; ++$known{"demographicsRegion"}}
			    elsif($chartype eq "region_of_birth") {$demographicsBirthRegion[$i] = $charvalue; ++$known{"demographicsBirthRegion"}}
			    elsif($chartype eq "height") {$demographicsHeight[$i] = $charvalue; ++$known{"demographicsHeight"}}
			    elsif($chartype eq "weight") {$demographicsWeight[$i] = $charvalue; ++$known{"demographicsWeight"}}
			    elsif($chartype eq "subject_id") {$clinicalSubjectId[$i] = $charvalue; ++$known{"clinicalSubjectId"}}
			    elsif($chartype eq "cell_type") {$sampleCellType[$i] = $charvalue; ++$known{"sampleCellType"}}
			    elsif($chartype eq "tissue") {$sampleTissue[$i] = $charvalue; ++$known{"sampleTissue"}}
			    elsif($chartype eq "target") {$clinicalTarget[$i] = $charvalue; ++$known{"clinicalTarget"}}
			    elsif($chartype eq "virus_accession") {$clinicalVirusAcc[$i] = $charvalue; ++$known{"clinicalVirusAcc"}}
			    elsif($chartype eq "virus_taxon_id") {$clinicalVirusTaxon[$i] = $charvalue; ++$known{"clinicalVirusTaxon"}}
			    elsif($chartype eq "induce_ifnbeta") {$clinicalIfnBeta[$i] = $charvalue; ++$known{"clinicalIfnBeta"}}
			    elsif($chartype eq "ifn_a2b") {$clinicalIfnA2b[$i] = $charvalue; ++$known{"clinicalIfnA2b"}}
			    elsif($chartype eq "study_group") {$clinicalStudyGroup[$i] = $charvalue; ++$known{"clinicalStudyGroup"}}
			    elsif($chartype eq "time") {$sampleTime[$i] = $charvalue; ++$known{"sampleTime"}}
			    elsif($chartype eq "hours_post_infection") {$sampleTime[$i] = "$charvalue hours"; ++$known{"sampleTime"}}
			    elsif($chartype eq "days_post_infection") {$sampleTime[$i] = "$charvalue days"; ++$known{"sampleTime"}}
			    elsif($chartype eq "cause_of_death") {$clinicalCauseOfDeath[$i] = $charvalue; ++$known{"clinicalCauseOfDeath"}}
			    elsif($chartype eq "death_time") {$clinicalDeathTime[$i] = $charvalue; ++$known{"clinicalDeathTime"}}
			    elsif($chartype eq "illness") {$clinicalDisease[$i] = $charvalue; ++$known{"clinicalDisease"}}
			    elsif($chartype eq "diagnosis") {$clinicalDisease[$i] = $charvalue; ++$known{"clinicalDisease"}}
			    elsif($chartype eq "disease_state") {$clinicalDiseaseState[$i] = $charvalue; ++$known{"clinicalDiseaseState"}}
			    elsif($chartype eq "severity") {$clinicalSeverity[$i] = $charvalue; ++$known{"clinicalSeverity"}}
			    elsif($chartype eq "replicate") {$sampleReplicate[$i] = $charvalue; ++$known{"sampleReplicate"}}
			    elsif($chartype eq "genotype") {$sampleGenotype[$i] = $charvalue; ++$known{"sampleGenotype"}}
			    elsif($chartype eq "molecule_subtype") {$sampleMolecule[$i] = $charvalue; ++$known{"sampleMolecule"}}
			    elsif($chartype eq "sample_origin") {$sampleOrigin[$i] = $charvalue; ++$known{"sampleOrigin"}}
			    elsif($chartype eq "ly6e") {$sampleLy6e[$i] = $charvalue; ++$known{"sampleLy6e"}}
			    elsif($chartype eq "infection") {$clinicalInfection[$i] = $charvalue; ++$known{"clinicalInfection"}}
			    elsif($chartype eq "infection_status") {$clinicalInfectionStatus[$i] = $charvalue; ++$known{"clinicalInfectionStatus"}}
			    elsif($chartype eq "treatment") {if($charvalue ne "N/A") {$clinicalTreatment[$i] = $charvalue; ++$known{"clinicalTreatment"}}}
			    elsif($chartype eq "ifn_treatment") {if($charvalue ne "N/A") {$clinicalIfnTreatment[$i] = $charvalue; ++$known{"clinicalIfnTreatment"}}}
			    elsif($chartype eq "clinical__w") {$clinicalW[$i] = $charvalue; ++$known{"clinicalW"}}
			    elsif($chartype eq "cirrhosis_present") {$clinicalCirrhosis[$i] = $charvalue; ++$known{"clinicalCirrhosis"}}
			    elsif($chartype eq "strain") {$clinicalStrain[$i] = $charvalue; ++$known{"clinicalStrain"}}
			    elsif($chartype eq "cell_line") {$sampleCellLine[$i] = $charvalue; ++$known{"sampleCellLine"}}
			    elsif($chartype eq "donor") {$sampleDonor[$i] = $charvalue; ++$known{"sampleDonor"}}
			    elsif($chartype eq "dose") {$sampleDose[$i] = $charvalue; ++$known{"sampleDose"}}
			    elsif($chartype eq "sample_type") {$sampleType[$i] = $charvalue; ++$known{"sampleType"}}
			    elsif($chartype eq "subject_status") {$clinicalStatus[$i] = $charvalue; ++$known{"clinicalStatus"}}
			    elsif($chartype eq "anamnestic_symptom_onset_to_death") {$clinicalAnamnesticDays[$i] = $charvalue; ++$known{"clinicalAnamnesticDays"}}
			    elsif($chartype eq "antiviral_medication") {$medicationAntiviral[$i] = yesno($charvalue); ++$known{"medicationAntiviral"}}
			    elsif($chartype eq "bmi") {$clinicalBMI[$i] = $charvalue; ++$known{"clinicalBMI"}}
			    elsif($chartype eq "copd") {$diagnosisCOPD[$i] = setValue($charvalue,("No COPD","COPD")); ++$known{"diagnosisCOPD"}}
			    elsif($chartype eq "cough") {$clinicalCough[$i] = setValue($charvalue,("No cough", "Cough")); ++$known{"clinicalCough"}}
			    elsif($chartype eq "cvrf") {$clinicalCVRisk[$i] = $charvalue; ++$known{"clinicalCVRisk"}} # cardiovascular risk factors
			    elsif($chartype eq "dad_stage") {$diagnosisDadStage[$i] = setHash($charvalue,("-" => "", "0" => "none", "1" => "exudative",
													 "1 to 2" => "exudative/proliferating/organizing",
													 "2" => "proliferating/organizing", "3" => "fibrotic")); ++$known{"diagnosisDadStage"}}
			    elsif($chartype eq "diabetes") {$diagnosisDiabetes[$i] = setValue($charvalue,("No diabetes","Diabetes")); ++$known{"diagnosisDiabetes"}}
			    elsif($chartype eq "dyspnea_/_tachypnea") {$diagnosisDyspnea[$i] = setValue($charvalue,("No dyspnea","Dyspnea")); ++$known{"diagnosisDyspnea"}}
			    elsif($chartype eq "fever") {$diagnosisFever[$i] = setValue($charvalue,("No fever","Fever")); ++$known{"diagnosisFever"}}
			    elsif($chartype eq "histology_dad") {$pathologyDad[$i] = setValue($charvalue,("No diffuse alveolar damage","Diffuse alveolar damage")); ++$known{"pathologyDad"}}
			    elsif($chartype eq "histopathologic_changes_in_lungs") {$pathologyLung[$i] = setValue($charvalue,("","slight to moderate changes","moderate changes","severe changes")); ++$known{"pathologyLung"}}
			    elsif($chartype eq "hospitalisation_time") {$clinicalHospitalisationTime[$i] = $charvalue; ++$known{"clinicalHospitalisationTime"}}
			    elsif($chartype eq "hypertension") {$diagnosisHypertension[$i] = setValue($charvalue,("No hypertension","Hypertension")); ++$known{"diagnosisHypertension"}}
			    elsif($chartype eq "intraalveolar_edema") {$diagnosisIntraalveolarEdema[$i] = setValue($charvalue,("No intraalveolar edema","Intraalveolar edema")); ++$known{"diagnosisIntraalveolarEdema"}}
			    elsif($chartype eq "intraalveolar_hemorrhage") {$diagnosisIntraalveolarHemorrhage[$i] = setValue($charvalue,("No intraalveolar hemorrhage","Intraalveolar hemorrhage")); ++$known{"diagnosisIntraalveolarHemorrhage"}}
			    elsif($chartype eq "malignant_tumor") {$diagnosisTumorMalignant[$i] = setValue($charvalue,("No malignant tumor","Malignant tumor")); ++$known{"diagnosisTumorMalignant"}}
			    elsif($chartype eq "material") {$clinicalMaterial[$i] = $charvalue; ++$known{"clinicalMaterial"}}
			    elsif($chartype eq "neutrophils") {$labtestNeutrophilsStatus[$i] = setValue($charvalue, ("","few","moderate","numerous")); ++$known{"labtestNeutrophilsStatus"}}
			    elsif($chartype eq "pre-existing_pulmonary_conditions") {$diagnosisPreexistingLung[$i] = setValue($charvalue,("No preexisting pulmonary conditions","Preexisting pulmonary conditions")); ++$known{"diagnosisPreexistingLung"}}
			    elsif($chartype eq "clinical_pathology") {$clinicalPathology[$i] = $charvalue; ++$known{"clinicalPathology"}}
			    elsif($chartype eq "sars-cov-2_genomes_per_million") {$labtestSarscov2Copies[$i] = $charvalue; ++$known{"labtestSarscov2Copies"}}
			    elsif($chartype eq "smoker") {$clinicalSmoker[$i] = setValue($charvalue,("Non-smoker","Smoker")); ++$known{"clinicalSmoker"}}
			    elsif($chartype eq "hours_post_mortem") {if($charvalue ne "NA") {$clinicalPostmortemHours[$i] = $charvalue; ++$known{"clinicalPostmortemHours"}}}
			    elsif($chartype eq "topography") {$clinicalTopography[$i] = $charvalue; ++$known{"clinicalTopography"}}
			    elsif($chartype eq "flowcell") {$clinicalFlowcell[$i] = $charvalue; ++$known{"clinicalFlowcell"}}
			    elsif($chartype eq "used_in_analysis") {$clinicalUsedInAnalysis[$i] = $charvalue; ++$known{"clinicalUsedInAnalysis"}}
			    elsif($chartype eq "analysis_visit") {$clinicalAnalysisVisit[$i] = $charvalue; ++$known{"clinicalAnalysisVisit"}}
			    elsif($chartype eq "used_in_module_construction") {$clinicalModuleConstruct[$i] = $charvalue; ++$known{"clinicalModuleConstruct"}}
			    elsif($chartype eq "mediancvcoverage") {$clinicalCVCoverage[$i] = $charvalue; ++$known{"clinicalCVCoverage"}}
			    elsif($chartype eq "percentaligned") {$clinicalPctAligned[$i] = $charvalue; ++$known{"clinicalPctAligned"}}
			    elsif($chartype eq "alignedreads") {$clinicalAlignedReads[$i] = $charvalue; ++$known{"clinicalAlignedReads"}}
			    elsif($chartype eq "deduplicatedreads") {$clinicalDedupReads[$i] = $charvalue; ++$known{"clinicalDedupReads"}}
			    elsif($chartype eq "rawreads") {$clinicalRawReads[$i] = $charvalue; ++$known{"clinicalRawReads"}}
			    elsif($chartype eq "stitchedreads") {$clinicalStitchedReads[$i] = $charvalue; ++$known{"clinicalStitchedReads"}}
			    elsif($chartype eq "trimmedreads") {$clinicalTrimmedReads[$i] = $charvalue; ++$known{"clinicalTrimmedReads"}}
			    elsif($chartype eq "sequencingsaturation") {$clinicalSeqSat[$i] = $charvalue; ++$known{"clinicalSeqSat"}}
			    elsif($chartype eq "viral_type_at_visit") {$clinicalViralType[$i] = $charvalue; ++$known{"clinicalViralType"}}
			    elsif($chartype eq "case_or_control_status_original") {$clinicalStatusOriginal[$i] = $charvalue; ++$known{"clinicalStatusOriginal"}}
			    elsif($chartype eq "case_or_control_status_event") {$clinicalStatusEvent[$i] = $charvalue; ++$known{"clinicalStatusEvent"}}
			    elsif($chartype eq "case_or_control_status_matched") {$clinicalStatusMatched[$i] = $charvalue; ++$known{"clinicalStatusMatched"}}
			    elsif($chartype eq "hrv_type_at_visit") {$clinicalHRVType[$i] = $charvalue; ++$known{"clinicalHRVType"}}
			    elsif($chartype eq "csteroid_start_relative_to_visit") {$clinicalCSteroidStart[$i] = $charvalue; ++$known{"clinicalCSteroidStart"}}
			    elsif($chartype eq "virus_type") {$clinicalVirusType[$i] = $charvalue; ++$known{"clinicalVirusType"}}
			    elsif($chartype eq "viral_strain") {$clinicalVirusStrain[$i] = $charvalue; ++$known{"clinicalVirusStrain"}}
			    elsif($chartype eq "virus_moi") {$clinicalVirusMOI[$i] = $charvalue; ++$known{"clinicalVirusMOI"}}
			    elsif($chartype eq "viral_titer") {$clinicalVirusTiter[$i] = $charvalue; ++$known{"clinicalVirusTiter"}}
			    elsif($chartype eq "virus_type_ev_hrv") {$clinicalVirusTypeEVHRV[$i] = $charvalue; ++$known{"clinicalVirusTypeEVHRV"}}
			    elsif($chartype eq "virus_type_adv") {$clinicalVirusTypeADV[$i] = $charvalue; ++$known{"clinicalVirusTypeADV"}}
			    elsif($chartype eq "virus_type_boca") {$clinicalVirusTypeBOCA[$i] = $charvalue; ++$known{"clinicalVirusTypeBOCA"}}
			    elsif($chartype eq "virus_type_rsv_a") {$clinicalVirusTypeRSVA[$i] = $charvalue; ++$known{"clinicalVirusTypeRSVA"}}
			    elsif($chartype eq "virus_type_rsv_b") {$clinicalVirusTypeRSVB[$i] = $charvalue; ++$known{"clinicalVirusTypeRSVB"}}
			    elsif($chartype eq "virus_type_cov_hku1") {$clinicalVirusTypeHKU1[$i] = $charvalue; ++$known{"clinicalVirusTypeHKU1"}}
			    elsif($chartype eq "virus_type_cov_nl63") {$clinicalVirusTypeCovNL63[$i] = $charvalue; ++$known{"clinicalVirusTypeCovNL63"}}
			    elsif($chartype eq "virus_type_piv_1") {$clinicalVirusTypePIV1[$i] = $charvalue; ++$known{"clinicalVirusTypePIV1"}}
			    elsif($chartype eq "virus_type_piv_2") {$clinicalVirusTypePIV2[$i] = $charvalue; ++$known{"clinicalVirusTypePIV2"}}
			    elsif($chartype eq "virus_type_piv_3") {$clinicalVirusTypePIV3[$i] = $charvalue; ++$known{"clinicalVirusTypePIV3"}}
			    elsif($chartype eq "virus_type_piv_4") {$clinicalVirusTypePIV4[$i] = $charvalue; ++$known{"clinicalVirusTypePIV4"}}
			    elsif($chartype eq "virus_type_mpv") {$clinicalVirusTypeMPV[$i] = $charvalue; ++$known{"clinicalVirusTypeMPV"}}
			    elsif($chartype eq "virus_type_flu_b") {$clinicalVirusTypeFluB[$i] = $charvalue; ++$known{"clinicalVirusTypeFluB"}}
			    elsif($chartype eq "virus_type_cov_229e") {$clinicalVirusTypeCoV229e[$i] = $charvalue; ++$known{"clinicalVirusTypeCoV229e"}}
			    elsif($chartype eq "nasal_neutrophil_percentage") {$clinicalNasalPctNeutrophil[$i] = $charvalue; ++$known{"clinicalNasalPctNeutrophil"}}
			    elsif($chartype eq "nasal_lymphocyte_percentage") {$clinicalNasalPctLymphocyte[$i] = $charvalue; ++$known{"clinicalNasalPctLymphocyte"}}
			    elsif($chartype eq "nasal_eosinophil_percentage") {$clinicalNasalPctEosinophil[$i] = $charvalue; ++$known{"clinicalNasalPctEosinophil"}}
			    elsif($chartype eq "nasal_macrophage_percentage") {$clinicalNasalPctMacrophage[$i] = $charvalue; ++$known{"clinicalNasalPctMacrophage"}}
			    elsif($chartype eq "nasal_wbc_percentage") {$clinicalNasalPctWhiteBloodCell[$i] = $charvalue; ++$known{"clinicalNasalPctWhiteBloodCell"}}
			    elsif($chartype eq "nasal_epithelial_percentage") {$clinicalNasalPctEpithelial[$i] = $charvalue; ++$known{"clinicalNasalPctEpithelial"}}
			    elsif($chartype eq "nasal_squamous_percentage") {$clinicalNasalPctSquamous[$i] = $charvalue; ++$known{"clinicalNasalPctSquamous"}}
			    elsif($chartype eq "nasal_epi_squa_percentage") {$clinicalNasalPctEpithelialSquamous[$i] = $charvalue; ++$known{"clinicalNasalPctEpithelialSquamous"}}
			    elsif($chartype eq "libcounts") {$clinicalLibCounts[$i] = $charvalue; ++$known{"clinicalLibCounts"}}
			    elsif($chartype eq "position") {$clinicalPosition[$i] = $charvalue; ++$known{"clinicalPosition"}}
			    elsif($chartype eq "blood_basophil_differential") {$clinicalBloodDiffBaso[$i] = $charvalue; ++$known{"clinicalBloodDiffBaso"}}
			    elsif($chartype eq "blood_eosinophil_differential") {$clinicalBloodDiffEosin[$i] = $charvalue; ++$known{"clinicalBloodDiffEosin"}}
			    elsif($chartype eq "blood_lymphocyte_differential") {$clinicalBloodDiffLymph[$i] = $charvalue; ++$known{"clinicalBloodDiffLymph"}}
			    elsif($chartype eq "blood_monocyte_differential") {$clinicalBloodDiffMono[$i] = $charvalue; ++$known{"clinicalBloodDiffMono"}}
			    elsif($chartype eq "blood_neutrophil_differential") {$clinicalBloodDiffNeutro[$i] = $charvalue; ++$known{"clinicalBloodDiffNeutro"}}
			    elsif($chartype eq "blood_erythrocytes_count") {$clinicalBloodCountErythro[$i] = $charvalue; ++$known{"clinicalBloodCountErythro"}}
			    elsif($chartype eq "blood_platelet_count") {$clinicalBloodCountPlatelet[$i] = $charvalue; ++$known{"clinicalBloodCountPlatelet"}}
			    elsif($chartype eq "blood_wbc_count") {$clinicalBloodCountWhite[$i] = $charvalue; ++$known{"clinicalBloodCountWhite"}}
			    elsif($chartype eq "nuclei_count") {$charvalue =~ s/,/./g; $clinicalBloodCountNuclei[$i] = $charvalue; ++$known{"clinicalBloodCountNuclei"}}
			    elsif($chartype eq "provider") {$sampleProvider[$i] = $charvalue; ++$known{"sampleProvider"}}
			    elsif($chartype eq "provider_id") {$sampleProviderId[$i] = $charvalue; ++$known{"sampleProviderId"}}
			    elsif($chartype eq "confirmed_content") {$sampleConfirmedContent[$i] = $charvalue; ++$known{"sampleConfirmedContent"}}
			    elsif($chartype eq "amplification") {$sampleAmplification[$i] = $charvalue; ++$known{"sampleAmplification"}}
			    elsif($chartype eq "layout_version") {$sampleLayoutVersion[$i] = $charvalue; ++$known{"sampleLayoutVersion"}}
			    elsif($chartype eq "antibody") {$clinicalAntibody[$i] = $charvalue; ++$known{"clinicalAntibody"}}
			    elsif($chartype eq "baseline_anti-a/h1n1_ab_titers") {$clinicalTiterBase[$i] = $charvalue; ++$known{"clinicalTiterBase"}}
			    elsif($chartype eq "spring_anti-a/h1n1_ab_titers") {$clinicalTiterSpring[$i] = $charvalue; ++$known{"clinicalTiterSpring"}}
			    elsif($chartype eq "manuscript_library_id") {$sampleLibManu[$i] = $charvalue; ++$known{"sampleLibManu"}}
			    elsif($chartype eq "sequencing_library_id") {$sampleLibSeq[$i] = $charvalue; ++$known{"sampleLibSeq"}}
			    elsif($chartype eq "sort_gate") {$sampleSortGate[$i] = $charvalue; ++$known{"sampleSortGate"}}
			    elsif($chartype eq "plate_number") {$samplePlateNum[$i] = $charvalue; ++$known{"samplePlateNum"}}
			    elsif($chartype eq "pool_number") {$samplePoolNum[$i] = $charvalue; ++$known{"samplePoolNum"}}
			    elsif($chartype eq "technology") {$sampleSeqTech[$i] = $charvalue; ++$known{"sampleSeqTech"}}
			    elsif($chartype eq "sequencing_batch") {$sampleSeqBatch[$i] = $charvalue; ++$known{"sampleSeqBatch"}}
			    elsif($chartype eq "type_of_library") {$sampleLibType[$i] = $charvalue; ++$known{"sampleLibType"}}
			    elsif($chartype eq "developmental_stage") {$sampleDevStage[$i] = $charvalue; ++$known{"sampleDevStage"}}
			    elsif($chartype eq "culture_media") {$sampleMedia[$i] = $charvalue; ++$known{"sampleMedia"}}
			    elsif($chartype eq "isolation_method") {$sampleIsolation[$i] = $charvalue; ++$known{"sampleIsolation"}}
			    elsif($chartype eq "preparation_method") {$samplePreparation[$i] = $charvalue; ++$known{"samplePreparation"}}
			    elsif($chartype eq "passage") {$samplePassage[$i] = $charvalue; ++$known{"samplePassage"}}
			    elsif($chartype eq "dms_treatment") {$sampleDms[$i] = $charvalue; ++$known{"sampleDms"}}
			    elsif($chartype eq "rsem_file_sample_id") {} #ignore - values per sample
			    elsif($chartype eq "animal_number") {$clinicalAnimalNum[$i] = $charvalue; ++$known{"clinicalAnimalNum"}}
			    elsif($chartype eq "case_number") {$clinicalCaseNum[$i] = $charvalue; ++$known{"clinicalCaseNum"}}
			    elsif($chartype eq "lesion_number") {$clinicalLesionNum[$i] = $charvalue; ++$known{"clinicalLesionNum"}}
			    elsif($chartype eq "individual_id") {$clinicalIndivId[$i] = $charvalue; ++$known{"clinicalIndivId"}}
			    elsif($chartype eq "viral_load") {$clinicalViralLoad[$i] = $charvalue; ++$known{"clinicalViralLoad"}}
			    elsif($chartype eq "sars-cov-2_positivity") {$clinicalViralPositivity[$i] = $charvalue; ++$known{"clinicalViralPositivity"}}
			    elsif($chartype eq "geographical_location") {$clinicalLocation[$i] = $charvalue; ++$known{"clinicalLocation"}}
			    elsif($chartype eq "drug") {$medicationDrug[$i] = $charvalue; ++$known{"medicationDrug"}}
			    elsif($chartype eq "sars-cov-2_infected") {$clinicalCov2Infected[$i] = $charvalue; ++$known{"clinicalCov2Infected"}}
			    elsif($chartype eq "sars-cov-2_pcr") {$clinicalCov2Pcr[$i] = $charvalue; ++$known{"clinicalCov2Pcr"}}
			    elsif($chartype eq "sars-cov-2_rpm") {$clinicalCov2Rpm[$i] = $charvalue; ++$known{"clinicalCov2Rpm"}}
			    elsif($chartype eq "rna_pulldown") {$sampleRnaPulldown[$i] = $charvalue; ++$known{"sampleRnaPulldown"}}
			    elsif($chartype eq "single_guide_rna") {$sampleSgrna[$i] = $charvalue; ++$known{"sampleSgrna"}}
			    elsif($chartype eq "sample_name") {$sampleName[$i] = $charvalue; ++$known{"sampleName"}}
			    elsif($chartype eq "sample_lane") {$sampleLane[$i] = $charvalue; ++$known{"sampleLane"}}
			    elsif($chartype eq "days_since_positive_test") {$clinicalDaysPos[$i] = $charvalue; ++$known{"clinicalDaysPos"}}
			    elsif($chartype eq "mouse_model") {$clinicalMouseModel[$i] = $charvalue; ++$known{"clinicalMouseModel"}}
			    elsif($chartype eq "cell_lining") {$clinicalCellLining[$i] = $charvalue; ++$known{"clinicalCellLining"}}
			    elsif($chartype eq "dm_test") {$diagDiabetes[$i] = yesno($charvalue); ++$known{"diagDiabetes"}}
			    elsif($chartype eq "apacheii_icu_score") {$labsIcuApacheii[$i] = $charvalue; ++$known{"labsIcuApacheii"}}
			    elsif($chartype eq "sofa_icu_score") {$labsIcuSofa[$i] = $charvalue; ++$known{"labsIcuSofa"}}
			    elsif($chartype eq "charlson_comorbidity") {$labsCharlson[$i] = $charvalue; ++$known{"labsCharlson"}}
			    elsif($chartype eq "crp_test") {$labsCrp[$i] = $charvalue; ++$known{"labsCrp"}}
			    elsif($chartype eq "ddimer_test") {$labsDDimer[$i] = $charvalue; ++$known{"labsDDimer"}}
			    elsif($chartype eq "ferritin_test") {$labsFerritin[$i] = $charvalue; ++$known{"labsFerritin"}}
			    elsif($chartype eq "fibrinogen_test") {$labsFibrinogen[$i] = $charvalue; ++$known{"labsFibrinogen"}}
			    elsif($chartype eq "lactate_test") {$labsLactate[$i] = $charvalue; ++$known{"labsLactate"}}
			    elsif($chartype eq "procalcitonin_test") {$labsProcalcitonin[$i] = $charvalue; ++$known{"labsProcalcitonin"}}
			    elsif($chartype eq "hospital-free_days_post_45_day_followup") {$clinicalHospFreeDays[$i] = $charvalue; ++$known{"clinicalHospFreeDays"}}
			    elsif($chartype eq "icu") {$clinicalICU[$i] = $charvalue; ++$known{"clinicalICU"}}
			    elsif($chartype eq "mechanical_ventilation") {$clinicalMechVent[$i] = $charvalue; ++$known{"clinicalMechVent"}}
			    elsif($chartype eq "ventilator-free_days") {$clinicalVentFreeDays[$i] = $charvalue; ++$known{"clinicalVentFreeDays"}}
			    elsif($chartype eq "associated_cv_terms") { # included elsewhere - see %knownCV for code values
				@cv = split(/ /, $charvalue);
				foreach $cv (@cv) {
				    if(!defined($knownCV{$cv})){print STDERR "Unknown CV term: $cv\n"}
				}
			    }
			    elsif($chartype eq "rna_population") {$sampleRnaPop[$i] = $charvalue; ++$known{"sampleRnaPop"}}
			    elsif($chartype eq "steroid") {$medicationSteroid[$i] = $charvalue; ++$known{"medicationSteroid"}}
			    elsif($chartype eq "batch") {$sampleBatch[$i] = $charvalue; ++$known{"sampleBatch"}}
			    elsif($chartype eq "viralrna") {$sampleViralRna[$i] = $charvalue; ++$known{"sampleViralRna"}}
			    elsif($chartype eq "treatment/time_point") {
				if($charvalue =~ /SAR[AS]-CoV2_([^,]+), (\d+ dpi)/){
				    $dose = $1;
				    $days = $2;
				    $clinicalViralDose[$i] = $dose; ++$known{"clinicalViralDose"};
				    $sampleTime[$i] = $days; ++$known{"sampleTime"};
				} else {
				    print STDERR "Unknown treatment/time_point '$charvalue'\n";
				    $used = 0;
				}
			    }
			    elsif($chartype eq "ish_status") {$sampleISHStatus[$i] = $charvalue; ++$known{"sampleISHStatus"}}
			    elsif($chartype eq "hyb_code") {$sampleHybCode[$i] = $charvalue; ++$known{"sampleHybCode"}}
			    elsif($chartype eq "segment") {$sampleSegment[$i] = $charvalue; ++$known{"sampleSegment"}}
			    elsif($chartype eq "segment_type") {$sampleSegmentType[$i] = $charvalue; ++$known{"sampleSegmentType"}}
			    elsif($chartype eq "roi") {$sampleROI[$i] = $charvalue; ++$known{"sampleROI"}}
			    elsif($chartype eq "roi_x") {$sampleROIx[$i] = $charvalue; ++$known{"sampleROIx"}}
			    elsif($chartype eq "roi_y") {$sampleROIy[$i] = $charvalue; ++$known{"sampleROIy"}}
			    elsif($chartype eq "negative_normalization_factor") {$sampleNegNorm[$i] = $charvalue; ++$known{"sampleNegNorm"}}
			    elsif($chartype eq "surface_area") {$sampleArea[$i] = $charvalue; ++$known{"sampleArea"}}
			    elsif($chartype eq "low_signal") {$sampleLowSig[$i] = $charvalue; ++$known{"sampleLowSig"}}
			    elsif($chartype eq "tissue_quality") {$sampleTissueQuality[$i] = $charvalue; ++$known{"sampleTissueQuality"}}
			    elsif($chartype eq "tissue_structure") {$sampleTissueStructure[$i] = $charvalue; ++$known{"sampleTissueStructure"}}
			    elsif($chartype eq "tissue_substructure") {$sampleTissueSubstructure[$i] = $charvalue; ++$known{"sampleTissueSubstructure"}}
			    elsif($chartype eq "tissue_notes") {$sampleTissueNotes[$i] = $charvalue; ++$known{"sampleTissueNotes"}}
			    elsif($chartype eq "temperature") {if($charvalue ne "N/A"){$sampleTemperature[$i] = $charvalue; ++$known{"sampleTemperature"}}}
			    elsif($chartype eq "hvc_enrichment") {$sampleHVC[$i] = $charvalue; ++$known{"sampleHVC"}}
			    else{$used=0}
			    if($used){$usedId{"$linetype\_$chartype"}++}
			} else {
			    print LOG "***Unknown sampleChar2 $line:$i: '$chartype' '$charvalue'\n";
			    $sample{$chartype} = "unknown";
			    $sampleCount{$chartype}++;
			}
		    } elsif($c =~ /^([^:]+):$/) {
			$chartype = lc($1);
			$charvalue = "";
			$chartype =~ s/ /_/g;
			push @chartype, $chartype;
			push @charvalue, $charvalue;
			if(!$i) {print LOG " simple $chartype: (no value)\n"}
			countValue("$linetype\_$chartype",$charvalue);
			if(defined($sample{$chartype})) {
			    # This is with no value
			    $sampleCount{$chartype}++;
			} else {
			    print LOG "***Unknown sampleChar3 $line:$i: '$chartype'\n";
			    $sample{$chartype} = "unknown";
			    $sampleCount{$chartype}++;
			}
		    } elsif ($c =~ /_/) {
			@cc = split(/, /, $c);
			foreach $cc (@cc) {
			    if($cc =~ /^(.*)_([^_]+)$/) {
				$chartype = lc($1);
				$charvalue = $2;
				$chartype =~ s/ /_/g;
				if(!$i) {print LOG "combined $chartype: $charvalue\n"}
				countValue("$linetype\_$chartype",$charvalue);
				if(defined($sample{$chartype})) {
				    $chartype = $sample{$chartype};
				    $sampleCount{$chartype}++;
				    push @chartype, $chartype;
				    push @charvalue, $charvalue;
				    $used = 1;
				    if($chartype eq "cell_line") {$sampleCellLine[$i] = $charvalue; ++$known{"sampleCellLine"}}
				    else{$used=0}
				    if($used){$usedId{"$linetype\_$chartype"}++}
				} else {
				    push @chartype, $chartype;
				    push @charvalue, $charvalue;
				    print LOG "***Unknown sampleChar4 $line:$i: '$chartype'\n";
				    $sample{$chartype} = "unknown";
				    $sampleCount{$chartype}++;
				}
			    }
			}
		    }
		    $i++;
		}
		print LOG "SAMPLECHAR-$sampchar: $total $unique '$value1' '$value2'\n";
		$chartotal = scalar @chartype;
		$charunique = countUnique("",@chartype);
		if($charunique == 1) {
		    $charvalue1 = $value1;
		    $valtotal = scalar @charvalue;
		    $valunique = countUnique("",@charvalue);
		    print LOG "SAMPLECHAR-$sampchar: $chartotal $charunique CHAR $charvalue1: $valtotal $valunique '$value1' '$value2'\n";
		} else {
		    print LOG "SAMPLECHAR-$sampchar: $chartotal $charunique KEYS '$value1' '$value2'\n";
		}
	    } else {
		@col = split(/\t/, $rest);
		$unique = countUnique($linetype,@col);
		$total = scalar @col;
		if($#col < 1) {next}
		print LOG "---$linetype: $total $unique '$value1' '$value2'\n";
	    }
	} else {
	    @col = split(/\t/, $rest);
	    $unique = countUnique($linetype,@col);
	    $total = scalar @col;
	    if($#col < 1) {next}
	    print LOG "***NonStandard $linetype: $total $unique '$value1' '$value2'\n";
	}
    }
}

#################################################
# 2. Fix data values
#    o - spelling
#    o - clean up sort order e.g. leading zeroes
#    o - clean up special characters e.g. '+'
#################################################

if(!defined($studyPlatform)){$studyPlatform = "UNKNOWN"}

$accCount = scalar @sampleacc;
print ERR "sampleacc count $accCount hash $#sampleacc\n";
for ($i=0; $i <= $#sampleacc; $i++) {
    if(defined($sampleTitle[$i])) {
	$sampleTitle[$i] =~ s/infecetd/infected/g;
	$sampleTitle[$i] =~ s/respiaratory/respiratory/g;
    }

    $st = $sampleTitle[$i];

    if($studyId eq "GSE154567" && $st =~ /^HTO_(.*)_umi_count/) {
	$clinicalSubjectId[$i] = "HTO_$1";
    }

    if(defined($clinicalTreatment[$i]) && !defined($clinicalStudyGroup[$i])) {
	if($clinicalTreatment[$i] eq "Ma Xing Shi Gan (MXSG) Decoction") {$clinicalStudyGroup[$i] = "MXSG group"}
    }

    # may only have first few values defined - fill in the rest
    if(defined($clinicalModuleConstruct[0]) && !defined($clinicalModuleConstruct[$i])){$clinicalModuleConstruct[$i] = ""}

    if(defined($clinicalCauseOfDeath[$i])) {
	$clinicalCauseOfDeath[$i] =~ s/pssion/passion/s;
    }

    if(defined($clinicalDeathTime[$i])) {
	$clinicalDeathTime[$i] =~ s/^Died_in_(\d\d\d\d)(\d\d)(\d\d)/$1-$2-$3/s; # yyyy-MM-dd
    }

    if(defined($sampleLy6e[$i])) {
	if($sampleLy6e[$i] eq "wt") {$sampleLy6e[$i] = "Wild type"}
	elsif($sampleLy6e[$i] eq "ko") {$sampleLy6e[$i] = "Conditional knockout"}
    }


    if(defined($sampleType[$i])) {
	if($sampleType[$i] eq "BALF"){$sampleType[$i] = "Bronchoalveolar lavage fluid"}
	if($sampleType[$i] eq "bronchoalveolar lavage fluid (BALF)"){$sampleType[$i] = "Bronchoalveolar lavage fluid"}
	if($sampleType[$i] eq "RVA"){$sampleType[$i] = "Rhinovirus A16"}
	if($sampleType[$i] eq "RVC"){$sampleType[$i] = "Rhinovirus C15"}
	$sampleType[$i] =~ s/extacted/extracted/g;
	$sampleType[$i] =~ s/water input control//g;
	$sampleType[$i] =~ s/RNA extracted from human lung tissue/Human RNA/g;
	$sampleType[$i] =~ s/WNV RNA from infected tissue culture, quantified by TaqMan Real-Time PCR/WNV RNA/g;
	if($clinicalSource[$i] =~ /^([A-Za-z0-9-]+ RNA) from infected tissue culture cells$/){
	    $sampleType[$i] = $1;
	    $clinicalSource[$i] = "tissue culture";
        }
	if($clinicalSource[$i] =~ /^RNA extracted from vero cell tissue culture$/){
	    $sampleType[$i] = "RNA";
	    $clinicalSource[$i] = "tissue culture";
        }
    }

    if(defined($clinicalSource[$i])) {
	if($clinicalSource[$i] eq "BALF"){$clinicalSource[$i] = "Lung"}
	if($clinicalSource[$i] eq "nasal_tissue"){$clinicalSource[$i] = "Nasal"}
	$clinicalSource[$i] =~ s/^sections of murine //g;
	$clinicalSource[$i] =~ s/trasnduced with a vector expressing/expressing/g;
	$clinicalSource[$i] =~ s/heatly negative/healthy negative/g;
	$clinicalSource[$i] =~ s/extacted/extracted/g;
	if($clinicalSource[$i] =~ /^(.*)-infected.* Calu-3 subclone 2B4 at (\d+) hours post infection/){
	    $clinicalSource[$i] = "Calu3 subclone 2B4";
	    $sampleType[$i] = "$1";
	    ++$known{"sampleType"};
	}
	$clinicalSource[$i] =~ s/RNA extracted from human lung tissue/infected lung tissue/g;
	$clinicalSource[$i] =~ s/water input control/water control/g;
	$clinicalSource[$i] =~ s/WNV RNA from infected tissue culture, quantified by TaqMan Real-Time PCR/tissue culture/g;
	$clinicalSource[$i] =~ s/^viral culture supernatant total nucleic acid/viral culture supernatant/g;
        if($clinicalSource[$i] =~ /(.*) treated (\S+) cells(.*)/){
	    $clinicalSource[$i] = "$2 cells$3";
	    if(!defined($sampleType[$i])) {
		$sampleType[$i] = "$1 treated";
		++$known{"sampleType"};
	    }
	}
        if($clinicalSource[$i] =~ /(.*) infected (\S+) cells(.*)/){
	    $clinicalSource[$i] = "$2 cells$3";
	    if(!defined($sampleType[$i])) {
		$sampleType[$i] = "$1 infected";
		++$known{"sampleType"};
	    }
	}
        if(defined($clinicalInfection[$i]) && $clinicalSource[$i] =~ /\S+ RNA from (\S+) cells,/){
	    $clinicalCell[$i] = $1;
	    ++$known{"clinicalCell"};
        } elsif(defined($clinicalInfection[$i]) && $clinicalSource[$i] =~ /\S+ RNA from detached (\S+) cells,/){
	    $clinicalCell[$i] = $1;
	    ++$known{"clinicalCell"};
	}
        if($clinicalSource[$i] =~ /^(.*)_mock$/){
	    if($1 eq "DC") {
		$clinicalSource[$i] = "PBMC-derived dendritic cells";
	    } elsif($1 eq "M1") {
		$clinicalSource[$i] = "Macrophage cells";
	    }
		    
	    if(!defined($sampleType[$i])) {
		$sampleType[$i] = "mock infected";
		++$known{"sampleType"};
	    }
	}
        elsif($clinicalSource[$i] =~ /^(.*)_SARS-CoV-2 infection_(\d+)h$/){
	    if($1 eq "DC") {
		$clinicalSource[$i] = "PBMC-derived dendritic cells";
	    } elsif($1 eq "M1") {
		$clinicalSource[$i] = "Macrophage cells";
	    }
		    
	    if(!defined($sampleType[$i])) {
		$sampleType[$i] = "SARS-CoV-2 infected";
		++$known{"sampleType"};
	    }
	    if(!defined($sampleTime[$i])) {
		$sampleTime[$i] = "$2 hours";
		++$known{"sampleTime"};
	    }
	}
    }

    if(!defined($demographicsEthnicity[$i])){$demographicsEthnicity[$i]=""}

    if(defined($demographicsRace[$i])){
	if($demographicsRace[$i] eq "W"){$demographicsRace[$i] = "White"}
	if($demographicsRace[$i] eq "NatAm"){$demographicsRace[$i] = "Native American"}
	if($demographicsRace[$i] eq "AfrAm"){$demographicsRace[$i] = "African American"}
    }

# parse sampleTitle to get more information about subject, timepoints, replicates, etc.
#    $csi = "unknown";
#    if($studyId eq "") {
#	 if($st =~ /pattern/){
#	     $csi = $1;
#	 } else {
#	 }
#        if(!defined($clinicalSubjectId[$i])){
#	     $clinicalSubjectId[$i] = $csi;
#        }
#    }


    # Use sample title as missing clinicalSubjectId if needed
    if($studyId eq "GSE159785" || !defined($clinicalSubjectId[$i])){
	if(defined($singleSample{$studyId})) {
	}

	# CoVid19 studies
	# ===============
       if($studyId eq "GSE106850") {
       } elsif($studyId eq "GSE145926") {
	    $st =~ s/ [\(]scRNA-seq[\)]//g;
	    $st =~ s/ [\(]TCR-seq[\)]//g;
	    $st =~ s/^BALF, //g;
	} elsif($studyId eq "GSE146074") {
	} elsif($studyId eq "GSE147143") {
	    # COVID-19 patient1
	} elsif($studyId eq "GSE147507") {
	    # 1: Series16_A549-ACE2_SARS-CoV-2_Rux_3
	    # 2: Series12_FerretNW_SARS-CoV-2_d7_2
	    #	    $st =~ s/_\d$//g;
	    $st =~ s/FerretTrachea/Trachea/g;
	    $st =~ s/FerretNW/NW/g;
	    $st =~ s/^Series/S/g
	} elsif($studyId eq "GSE147863") {
	    # Engineered Human Kidney Organoids
	} elsif($studyId eq "GSE147903") {
	    # Pancreatic Islets, ICRH122
	    $st =~ s/^Pancreatic Islets, //g;
	} elsif($studyId eq "GSE147975") {
	} elsif($studyId eq "GSE148113") {
	} elsif($studyId eq "GSE148360") {
	} elsif($studyId eq "GSE148696") {
	} elsif($studyId eq "GSE148697") {
	    # ...
	} elsif($studyId eq "GSE148729") {
	    # Calu3_scRNAseq-S2-12h-B Calu3=subject 
	    $st =~ s/[-][AB]$//g;
	} elsif($studyId eq "GSE148815") {
	} elsif($studyId eq "GSE148816") {
	} elsif($studyId eq "GSE148817") {
	} elsif($studyId eq "GSE148829") {
	    # 6 matrices
	    # 1: COVID_BasalStim_BEAS_F07_S163
	    # 2: Mouse_basal_stim_IFNG_2_REP3
	    # 3: Non-Human Primate Lung epithelial cells, SHIV-infected donor, SHIV4
	    # 4: Ileal Small Intestine enterocytes
	    # 5: Non-Human Primate Ileum Small Intestine Enterocytes, SHIV-infected donor, SHIV4
	    # 6: Non-human primate mTB infected granulomas and uninvolved lungs [28918_Array1]
	} elsif($studyId eq "GSE149036") {
	    # scRNA-seq of Mus musculus: lymph nodes
	    $st =~ s/^scRNA-seq of Mus musculus: //g;
	} elsif($studyId eq "GSE149273") {
	    # 6a60_2: Intestinal organoid bulk RNA-seq
	} elsif($studyId eq "GSE149312") {
	    # ...
	} elsif($studyId eq "GSE149601") {
	} elsif($studyId eq "GSE149689") {
	    # ...
	} elsif($studyId eq "GSE149907") {
	    $st = $sampleId[$i]; # sampleTitle is long-winded summary of everything
	} elsif($studyId eq "GSE149973") {
	    # fp_chx_05hr_2
	    $st =~ s/_[2-3]/_1/g;
	} elsif($studyId eq "GSE149878") {
	} elsif($studyId eq "GSE150316") {
	    if($st =~ /^case\d+[-]([a-z]+)/) {
		$sampleTissue[$i] = $1;
	    } elsif($st =~ /^NegControl/) {
		$sampleTissue[$i] = "control";
	    } else {
		$sampleTissue[$i] = "";
	    }
	    # case5-heart1
	} elsif($studyId eq "GSE150708") {
	} elsif($studyId eq "GSE150392") {
	    # ...
	} elsif($studyId eq "GSE150728") {
	    # PBMCs from COVID-19 sample covid_555_2
	    if($st =~ /PBMCs from COVID-19 sample /){
		$st =~ s/PBMCs from COVID-19 sample //g;
		$clinicalSource[$i] = "PBMCs";
		++$known{"clinicalSource"};
		$sampleType[$i] = "CoVid-19";
		++$known{"sampleType"};
	    } elsif ($st =~ /PBMCs from healthy control /) {
		$st =~ s/PBMCs from healthy control //g;
		$clinicalSource[$i] = "PBMCs";
		++$known{"clinicalSource"};
		$sampleType[$i] = "Healthy control";
		++$known{"sampleType"};
	    }
	    $st =~ s/_[1-9]//g;
	} elsif($studyId eq "GSE150819") {
	    $st =~ s/^([^_]+_\d)_.*$/$1/g;
	} elsif($studyId eq "GSE150847") {
	    # lungNHBE_3: hBEpC_rep3
	    $st =~ s/_rep[1-3]//g;
	} elsif($studyId eq "GSE150861") {
	} elsif($studyId eq "GSE150962") {
	    # ...
	} elsif($studyId eq "GSE151161") {
	    # ...
	} elsif($studyId eq "GSE151327") {
	} elsif($studyId eq "GSE151346") {
	    # ...
	} elsif($studyId eq "GSE151764") {
	    $st =~ s/_2020/_20/g;
	} elsif($studyId eq "GSE151803") {
	    # ...
	} elsif($studyId eq "GSE151878") {
	} elsif($studyId eq "GSE151879") {
	} elsif($studyId eq "GSE152075") {
	    # ...
	} elsif($studyId eq "GSE152418") {
	    # ...
	} elsif($studyId eq "GSE152439") {
	    # ...
	} elsif($studyId eq "GSE152522") {
	    # ...
	} elsif($studyId eq "GSE152586") {
	} elsif($studyId eq "GSE152641") {
	    # ...
	} elsif($studyId eq "GSE153218") {
	} elsif($studyId eq "GSE153277") {
	    # ...
	} elsif($studyId eq "GSE153610") {
	    $st =~ s/ patient//g;
	    $st =~ s/ from//g;
	    $st =~ s/ [\(]resequenced[\)]//g;
	    $st =~ s/ [\(]resequenced(\d+)[\)]/ ($1)/g;
	    $st =~ s/ cells//g;
	    $st =~ s/Non-classical/Other/g;
	} elsif($studyId eq "GSE153684") {
	    # ...
	} elsif($studyId eq "GSE153851") {
	    $st =~ s/ in vitro transcribed/transcribed/g;
	} elsif($studyId eq "GSE153931") {
	    # ...
	} elsif($studyId eq "GSE153940") {
	    $st =~ s/ \d$//g;
	} elsif($studyId eq "GSE153970") {
	} elsif($studyId eq "GSE153984") {
	    # ...
	} elsif($studyId eq "GSE154104") {
	} elsif($studyId eq "GSE154171") {
	} elsif($studyId eq "GSE154244") {
	} elsif($studyId eq "GSE154430") {
	    $st =~ s/SARS-CoV-2 infected /SARS-CoV-2 /g;
	    $st =~ s/_input/_in/g;
	} elsif($studyId eq "GSE154564") {
	} elsif($studyId eq "GSE154567") {
	    # ...
	} elsif($studyId eq "GSE154613") {
	    # ...
	} elsif($studyId eq "GSE154662") {
	    # ...
	} elsif($studyId eq "GSE154761") {
	    # ...
	} elsif($studyId eq "GSE154768") {
	    # X7_DPI
	} elsif($studyId eq "GSE154769") {
	    # Pt_7_coll_2
	} elsif($studyId eq "GSE154770") {
	    # POS_001
	} elsif($studyId eq "GSE154782") {
	} elsif($studyId eq "GSE154783") {
	} elsif($studyId eq "GSE154784") {
	} elsif($studyId eq "GSE154936") {
	    $st =~ s/, / /g;
	    $st =~ s/-Cas9NG-mChe//g;
	} elsif($studyId eq "GSE154998") {
	} elsif($studyId eq "GSE155106") {
	    # DC_SARS-CoV-2 infection_2hr
	    # DC_mock
	    $st =~ s/^([^_]+_(mock|SARS)).*/$1/g;
	} elsif($studyId eq "GSE155113") {
	} elsif($studyId eq "GSE155241") {
	} elsif($studyId eq "GSE155249") {
	} elsif($studyId eq "GSE155286") {
	} elsif($studyId eq "GSE155363") {
	} elsif($studyId eq "GSE155518") {
	} elsif($studyId eq "GSE155536") {
	} elsif($studyId eq "GSE155673") {
	} elsif($studyId eq "GSE156005") {
	} elsif($studyId eq "GSE156063") {
	} elsif($studyId eq "GSE156544") {
	} elsif($studyId eq "GSE156701") {
	} elsif($studyId eq "GSE156754") {
	    $st =~ s/Mock Infection/Mock/g
	} elsif($studyId eq "GSE156755") {
	} elsif($studyId eq "GSE157055") {
	} elsif($studyId eq "GSE157057") {
	} elsif($studyId eq "GSE157058") {
	} elsif($studyId eq "GSE157059") {
	} elsif($studyId eq "GSE157103") {
	} elsif($studyId eq "GSE157424") {
	    $st =~ s/ and adjacent tissue//g;
	} elsif($studyId eq "GSE157526") {
	} elsif($studyId eq "GSE157852") {
	} elsif($studyId eq "GSE157859") {
	    $st =~ s/_moderate/_mod/g;
	    $st =~ s/_serious/_ser/g;
	    $st =~ s/Stage//g;
	    $st =~ s/Convalescence/Conv/g;
	    $st =~ s/Rehabilitation/Rehab/g;
	    $st =~ s/Treatment/Treat/g;
	} elsif($studyId eq "GSE158030") {
	} elsif($studyId eq "GSE158034") {
	    $st =~ s/  / /g;
	    $st =~ s/Activated //g;
	    $st =~ s/cells, Set/cells set/g;
	    $st =~ s/[\(\)]//g;
	} elsif($studyId eq "GSE158036") {
	} elsif($studyId eq "GSE158037") {
	} elsif($studyId eq "GSE158038") {
	    $st =~ s/  / /g;
	    $st =~ s/Activated //g;
	    $st =~ s/cells, Set/cells set/g;
	    $st =~ s/[\(\)]//g;
	} elsif($studyId eq "GSE158050") {
	} elsif($studyId eq "GSE158052") {
	    $st =~ s/analysis of //g;
	    $st =~ s/replicate /rep/g;	
	} elsif($studyId eq "GSE158055") {
	} elsif($studyId eq "GSE158127") {
	} elsif($studyId eq "GSE158069") {
	} elsif($studyId eq "GSE158297") {
	    $st =~ s/Mus musculus/Mouse/g;
	    $st =~ s/Macaca mulatta/Rhesus/g;
	    $st =~ s/hACE2 transgenic mice/hACE2/g;
	    $st =~ s/at 0 dpi,  1dpi, 3 dpi and 7 dpi[.]/0-7d/g;
	    $st =~ s/at 1dpi, 2dpi, 3dpi, 5dpi and 7dpi./1-7d/g;
	    $st =~ s/at 6h, 24h, 48h[.]/6-48h/g;
	    $st =~ s/at day 7dpi/7d/g;
	    $st =~ s/at 1dpi, 3dpi, 5dpi, 7dpi[.]/1-7d/g;
	    $st =~ s/ detected in different parts[.]/ detected/g;
	    $st =~ s/at 0 dpi, 3 dpi, and 5 dpi./0-5d/g;
	    $st =~ s/ with//g;
	    $st =~ s/ and SARS-CoV2/\/CoV2/g;
	    $st =~ s/SARS-CoV2/CoV2/g;
	    $st =~ s/SARS-CoV/CoV/g;
	    $st =~ s/IAV and MHV/IAV\/MHV/g;
	    $st =~ s/and rescue medicine[.]/rescue/g;
	    $st =~ s/[.] / /g;
	    $st =~ s/[\(\)]//g;
	} elsif($studyId eq "GSE158298") {
	} elsif($studyId eq "GSE158374") {
	} elsif($studyId eq "GSE158752") {
	} elsif($studyId eq "GSE158930") {
	    $st =~ s/-rep\d//g;
	    $st =~ s/-\d+hpi//g;
	} elsif($studyId eq "GSE159191") {
	} elsif($studyId eq "GSE159212") {
	} elsif($studyId eq "GSE159213") {
	} elsif($studyId eq "GSE159272") {
	} elsif($studyId eq "GSE159316") {
	} elsif($studyId eq "GSE159372") {
	} elsif($studyId eq "GSE159519") {
	    $st =~ s/ECCITE-seq/ECCITE/g;
	} elsif($studyId eq "GSE159522") {
	    $st =~ s/ rep \d+$//g;
	} elsif($studyId eq "GSE159576") {
	} elsif($studyId eq "GSE159584") {
	} elsif($studyId eq "GSE159593") {
	} elsif($studyId eq "GSE159678") {
	} elsif($studyId eq "GSE159717") {
	    $st =~ s/- experiment/expt/g;
	    $st =~ s/Human pancreatic islets - //g;
	} elsif($studyId eq "GSE159785") {
	} elsif($studyId eq "GSE159787") {
	} elsif($studyId eq "GSE160031") {
	} elsif($studyId eq "GSE160032") {
	} elsif($studyId eq "GSE160033") {
	} elsif($studyId eq "GSE160034") {
	    $st =~ s/Purification/Purify/g;
	} elsif($studyId eq "GSE160036") {
	    $st =~ s/Purification/Purify/g;
	} elsif($studyId eq "GSE160163") {
	} elsif($studyId eq "GSE160351") {
	} elsif($studyId eq "GSE160435") {
	} elsif($studyId eq "GSE160824") {
	    $st =~ s/Female/F/g;
	    $st =~ s/Day /d/g;
	    $st =~ s/Male/M/g;
	    $st =~ s/VIDO# /#/g;
	} elsif($studyId eq "GSE160876") {
	    $st =~ s/ \d$//g;
	} elsif($studyId eq "GSE161089") {
	} elsif($studyId eq "GSE161200") {
	    $st =~ s/day/d/g;
	    $st =~ s/10000pfu/10Kpfu/g;
	} elsif($studyId eq "GSE161262") {
	} elsif($studyId eq "GSE161263") {
	    $st =~ s/Replicate1, HLO_Differentiation1_Replicate2, HLO_Differentiation1_Replicate3/Rep1-3/g;
	    $st =~ s/Replicate/Rep/g;
	} elsif($studyId eq "GSE161281") {
	    $st =~ s/peripheral neuron-//g;
	    $st =~ s/ infected//g;
	} elsif($studyId eq "GSE161381") {
	} elsif($studyId eq "GSE161382") {
	} elsif($studyId eq "GSE161615") {
	} elsif($studyId eq "GSE161777") {
	} elsif($studyId eq "GSE161881") {
	} elsif($studyId eq "GSE161915") {
	    $st =~ s/_untreated//g;
	} elsif($studyId eq "GSE161916") {
	} elsif($studyId eq "GSE161918") {
	} elsif($studyId eq "GSE161934") {
	    $st =~ s/Air-liquid interface grown bronchioalveolar cells/ALI-grown bronchioalveolar cells/g;
	} elsif($studyId eq "GSE162038") {
	    $st =~ s/^Brunello_//g;
	} elsif($studyId eq "GSE162039") {
	    $st =~ s/^Krogan_//g;
	} elsif($studyId eq "GSE162040") {
	    $st =~ s/^Human GeCKOv2 plasmid DNA library_? /GeCKOv2 lib_/g;
	    $st =~ s/lib_B_/libB/g;
	} elsif($studyId eq "GSE162086") {
	} elsif($studyId eq "GSE162113") {
	} elsif($studyId eq "GSE162131") {
	} elsif($studyId eq "GSE162208") {
	    $st =~ s/sample for single-cell RNA-seq/scRNAseq/g;
	    $st =~ s/#(\d),/$1/g;
	    $st =~ s/sample for bulk RNA-seq/RNAseq/g;
	} elsif($studyId eq "GSE162247") {
	} elsif($studyId eq "GSE162569") {
	    $st =~ s/ In Vitro//g;
	    $st =~ s/Frameshifting-/Fshift/g;
	    $st =~ s/Nucleocapsid -/Nc/g;
	    $st =~ s/Nucleocapsid-/Nc/g;
	    $st =~ s/Nucleocapsid /Nc/g;
	    $st =~ s/protein/prot/g;
	    $st =~ s/ RNA//g;
	    $st =~ s/-RNA//g;
	    $st =~ s/FshiftRNA/Fshift/g;
	    $st =~ s/NcRNA/Nc/g;
	} elsif($studyId eq "GSE162629") {
	} elsif($studyId eq "GSE162736") {
	} elsif($studyId eq "GSE162835") {
	} elsif($studyId eq "GSE162899") {
	} elsif($studyId eq "GSE163151") {
	} elsif($studyId eq "GSE163426") {
	} elsif($studyId eq "GSE163547") {
	    $st =~ s/-Rep//g;
	} elsif($studyId eq "GSE163623") {
	} elsif($studyId eq "GSE163624") {
	} elsif($studyId eq "GSE163668") {
	    $st =~ s/10X GEX //g;
	    $st =~ s/libraries/libs/g;
	    $st =~ s/library/lib/g;
	    $st =~ s/ for//g;
	    $st =~ s/ and //g;
	} elsif($studyId eq "GSE163688") {
	} elsif($studyId eq "GSE163919") {
	} elsif($studyId eq "GSE164073") {
	} elsif($studyId eq "GSE164332") {
	} elsif($studyId eq "GSE164386") {
	} elsif($studyId eq "GSE164547") {
	} elsif($studyId eq "GSE164805") {
	} elsif($studyId eq "GSE165190") {
	} elsif($studyId eq "GSE165200") {
	} elsif($studyId eq "GSE165340") {
	    $st =~ s/Series/S/g;
	    $st =~ s/SARS-CoV-2/SARSCoV2/g;
	} elsif($studyId eq "GSE165461") {
	} elsif($studyId eq "GSE165477") {
	} elsif($studyId eq "GSE165747") {
	    $st =~ s/Group/Grp/g;
	    $st =~ s/p20136-//g;
	} elsif($studyId eq "GSE165890") {
	} elsif($studyId eq "GSE165955") {
	} elsif($studyId eq "GSE166253") {
	} elsif($studyId eq "GSE166281") {
	} elsif($studyId eq "GSE166552") {
	} elsif($studyId eq "GSE166651") {
	} elsif($studyId eq "GSE166766") {
	} elsif($studyId eq "GSE166992") {
	} elsif($studyId eq "GSE167000") {
	} elsif($studyId eq "GSE167118") {
	} elsif($studyId eq "GSE167131") {
	} elsif($studyId eq "GSE167334") {
	} elsif($studyId eq "GSE167336") {
	} elsif($studyId eq "GSE167480") {
	} elsif($studyId eq "GSE167528") {
	} elsif($studyId eq "GSE168286") {
	    $st =~ s/ scRNA-seq//g;
	} elsif($studyId eq "GSE168453") {
	}
	

	# MERS studies
	# ============

	elsif($studyId eq "GSE47957") {
	    # ...
	} elsif($studyId eq "GSE55023") {
	    # Marmoset_19_infected_day3_lesion_1_run_2
	    $st =~ s/_run_[1-9]$//g;
	} elsif($studyId eq "GSE56189") {
	    # SARS_MRC5HighMOI_48hr_3
	    $st =~ s/_[1-9]$//g;
	} elsif($studyId eq "GSE56192") {
	    # SARS_MRC5HighMOI_24hr_3
	    $st =~ s/_[1-9]$//g;
	} elsif($studyId eq "GSE56193") {
	    # SARS_VEROHighMOI_24hr_3
	    $st =~ s/_[1-9]$//g;
	} elsif($studyId eq "GSE56677") {
	    # ECL003_LoCov_7h_3
	    $st =~ s/_[1-9]$//g;
	} elsif($studyId eq "GSE65574") {
	    # MCL001_d3-5-MERS_24hr_3_mRNA
	    $st =~ s/_[1-9]_mRNA$//g;
	    $st =~ s/_[1-9]_microRNA$//g;
	} elsif($studyId eq "GSE79172") {
	    # MDC001_icMERS_0h_RNA_3
	    $st =~ s/_[1-9]$//g;
	} elsif($studyId eq "GSE79216" || $studyId eq "GSE79218") {
	    # MMVE001_Mock_24h_microRNA_5
	    $st =~ s/_[1-9]$//g;
	} elsif($studyId eq "GSE79458" || $studyId eq "GSE79459" || $studyId eq "GSE81852" || $studyId eq "GSE81909" || $studyId eq "GSE86528" || $studyId eq "GSE86529" || $studyId eq "GSE86530") {
	    # MFB001_icMERS_24h_RNA_D
	} elsif($studyId eq "GSE100496") {
	    # MFB003_icMERS_12HRS_RNA_E
	} elsif($studyId eq "GSE100504") {
	    # MHAE003_icMERS_12HRS_RNA_E
	} elsif($studyId eq "GSE100509") {
	    # MMVE003_icMERS_0HRS_RNA_E
	} elsif($studyId eq "GSE108594") {
	    # MM001_Mouse_1_MERS_10e6_Day_7_1-30-2017
	} elsif($studyId eq "GSE108881") {
	} elsif($studyId eq "GSE108882") {
	    # ...
	} elsif($studyId eq "GSE122876") {
	    # AM580_3
	    $st =~ s/_[1-9]$//g;
	} elsif($studyId eq "GSE131936") {
	    # MERS-CoV-infected-rIFN-treated-2dpi-sacrifice-4dpi [2T4]
	    $st =~ s/ [\[][1-9][TC][1-9][\]]$//g;
	} elsif($studyId eq "GSE139516") {
	    # 24hpi_3_circRNA
	}
    
	# Other studies
	# =============

	elsif($studyId eq "GSE6198") {
	    $st =~ s/Gene Transcription Profiles during Development of Mucosal Immunity //g;
	} elsif($studyId eq "GSE6211") {
	    $st =~ s/Gene Transcription Profile of Local Innate and Adaptive Immunity during Early Phase of IBV Infection //g;
	    $st =~ s/Gene Transcription Profile of Local Innate and Adaptive Immunity During Early Phase of IBV Infection //g;
	} elsif($studyId eq "GSE8053") {
	} elsif($studyId eq "GSE13879") {
	    
	} elsif($studyId eq "GSE24700") {
	    # subject_id for DeRset2_4
	} elsif($studyId eq "GSE25846") {
	    $st =~ s/ that are//g;
	    $st =~ s/Subject Brain/Brain/g;
	} elsif($studyId eq "GSE41756") {
	    $st =~ s/ infected cells,//g;
	} elsif($studyId eq "GSE44333") {
	} elsif($studyId eq "GSE64737") {
	} elsif($studyId eq "GSE73423") {
	} elsif($studyId eq "GSE89158") {
	} elsif($studyId eq "GSE89159") {
	} elsif($studyId eq "GSE89160") {
	} elsif($studyId eq "GSE89166") {
	} elsif($studyId eq "GSE89190") {
	    $st =~ s/, biological rep/rep/g;
	} elsif($studyId eq "GSE89212") {
	    # ChIP-seq analysis of H3K27ac in Huh7 cells ( IL-1 )
	} elsif($studyId eq "GSE115770") {
	    $st = $clinicalSource[$i];
	} elsif($studyId eq "GSE115823") {
	    $st = $clinicalSource[$i];
	} elsif($studyId eq "GSE115824") {
	    $st = $clinicalSource[$i];
	} elsif($studyId eq "GSE151020") {
	    # ...
	} elsif($studyId eq "GSE152676") {
	} elsif($studyId eq "GSE155986") {
	} elsif($studyId eq "GSE156988") {
	}

	# Related studies
	# ===============

	elsif($studyId eq "GSE7779") {
	    # Brain_normal_5_rep2
	    $st =~ s/_rep[1-9]$//;
	} elsif($studyId eq "GSE19398") {
	    # ...
	} elsif($studyId eq "GSE138294") {
	    $st =~ s/^[^_]+_/Panda/g;
	} elsif($studyId eq "GSE153428") {
	    $st =~ s/biological replicate /rep/g;
	    $st =~ s/Interferon lambda/IF lambda/g;
	    $st =~ s/ treatment,//g;
	} elsif($studyId eq "GSE156445") {
#	    $st =~ s/^Cells treated with //g;
#	    $st =~ s/ [Rr]eplicate \d$//g;
	} elsif($studyId eq "GSE156759") {
	} elsif($studyId eq "GSE158832") {
	} elsif($studyId eq "GSE163014") {
	} elsif($studyId eq "GSE167521") {
	}

	# SARS studies
	# ============
	elsif($studyId eq "GSE546") {
	} elsif($studyId eq "GSE1739") {
	} elsif($studyId eq "GSE5972") {
	    # ...
	} elsif($studyId eq "GSE7779") {
	    # 73070_#841_A2_470PMT_050506_532median
	} elsif($studyId eq "GSE11704") {
	    if($st =~ /^D(\d+)_/) {
		$d = $1 - 3;
		$sampleTime[$i] = "$d days";
		++$known{"sampleTime"};
	    }
	} elsif($studyId eq "GSE17400") {
	    # SARS-CoV-infected (MOI=0.1) Calu-3 subclone 2B4 at 12 hours post infection #3
	    $st =~ s/ [#][1-9]$//g;
	} elsif($studyId eq "GSE19137") {
	    # Mouse lung with SARS-CoV (TOR-2) on Day 28, biological rep2
	    $st =~ s/, biological rep[1-9]$//g;
	} elsif($studyId eq "GSE21319") {
	    # 1: West Nile Virus NY99 (10^4 Copies, 10ng Human Lung RNA) 04
	    # 2: Negative Control (Vero cell tissue culture supernatant) 02
	    $st =~ s/ 0[1-9]$//g;
	} elsif($studyId eq "GSE22581") {
	    $st =~ s/Ferret Lung, /FL/g;
	    $st =~ s/Ferret Blood, /FB/g;
	    $st =~ s/injected with IFN-a2b /i/g;
	    $st =~ s/Not injected with IFN-a2b, /n/g;
	    $st =~ s/uninfected, /u/g;
	    $st =~ s/infecetd with SARS-CoV /s/g;
	    $st =~ s/on Day (\d+), /-d$1/g;
	    $st =~ s/biological rep\d+//g;
	} elsif($studyId eq "GSE23955") {
	    # Ferret Lung, Not injected with IFN-a2b, on Day 0, biological rep4
	    $st =~ s/, biological rep[1-9]$//g;
	} elsif($studyId eq "GSE23955") {
	    # SARS-CoV-GZ02-day-21-replicate-3
	    $st =~ s/-replicate-[1-3]$//g;
	} elsif($studyId eq "GSE30589") {
	    # Vero E6 SARS CoV DeltaE 15hpi rep1
	    $st =~ s/ rep[1-3]$//g;
	} elsif($studyId eq "GSE33266" || $studyId eq "GSE33267") {
	    # SCL005 WT 12H 3
	    $st =~ s/ [123]$//g;
	} elsif($studyId eq "GSE36016") {
	    # sars_mu_lung_STAT_Day5_rMA15_#3
	    $st =~ s/[#][23]$/#1/g;
 	} elsif($studyId eq "GSE36969") {
	    # BalbC_SARS_MA15e_Aged_12h_B_2
	} elsif($studyId eq "GSE36971") {
	    # small RNA from lung tissue in Mouse Strain WSB infected with Flu, rep2
	    $st =~ s/, rep[1-3]$//g;
	} elsif($studyId eq "GSE40824" || $studyId eq "GSE40827" || $studyId eq "GSE40840") {
	    # SM015_4.5_Tnfrsf1a1/b_mock_d7_3
	} elsif($studyId eq "GSE44274") {
	    # SARSmus, UV-V+TLR, Lung, 1dpi(inf), 06
	    $st =~ s/[\(]inf[\)]//g;
	} elsif($studyId eq "GSE37827" || $studyId eq "GSE45042") {
	    # ECL001_Mock_12hr_3
	} elsif($studyId eq "GSE47960" || $studyId eq "GSE47961" || $studyId eq "GSE47962" || $studyId eq "GSE47963") {
	    # SHAE004_Mock_96h_3
	} elsif($studyId eq "GSE50855") {
	    # SARSmus, UV-V+TLR, MEM, PM, 1dpi, 01
	    $st =~ s/,//g;
	} elsif($studyId eq "GSE49262" || $studyId eq "GSE49263" || $studyId eq "GSE50000" || $studyId eq "GSE50878" || $studyId eq "GSE51386" || $studyId eq "GSE51387" || $studyId eq "GSE52405") {
	    # SMnnnn_C57BL6J_MA15_D2_81 # SMnnn=substudy MA15 etc=infection D2 time?  C57BL6J=hoststrain
	} elsif($studyId eq "GSE59185" || $studyId eq "GSE52920") {
	    $st =~ s/REP[2-3]/REP1/g;
	} elsif($studyId eq "GSE64660") {
	    # 4_4_1_Murf2_10_5_Day 2_1
	} elsif($studyId eq "GSE68820") {
	    # SM036 3.1 Mock TLR3 d4 RNA
	} elsif($studyId eq "GSE90624") {
	    $st =~ s/Rep[2-3]/Rep1/g;
	    if(!defined($sampleTime[$i]) && $st =~ / (\d+)dpi/){
		$sampleTime[$i] = "$1 days";
		++$known{"sampleTime"};
	    }
	    if(!defined($sampleType[$i]) && $st =~ /Infected [\(]([^\(]+)[\)]/){
		$sampleType[$i] = "$1";
		++$known{"sampleType"};
	    }
	    elsif(!defined($sampleType[$i]) && $st =~ /Mock/){
		$sampleType[$i] = "Mock";
		++$known{"sampleType"};
	    }
	    # Infected (deltaE) - 2dpi - Rep1 [E2A]
	} elsif($studyId eq "GSE93283") {
	} elsif($studyId eq "GSE147194") {
	    $st =~ s/Replicate [12]; //g;
	    $st =~ s/sorted for low binding to the RBD of S protein from SARS-CoV-2/sorted for low binding to RBD/g;
	    $st =~ s/Library of ACE2/ACE2 library of/g;
	} elsif($studyId eq "GSE156598") {
	    $st =~ s/^U2OS_Lib/U2OS-/g;
	    $st =~ s/, lib/-/g;
	} else {
	    print ERR "+++No subject_id for '$st' in subjects$filenum{$filename}.dat for '$studyId'\n";
	}
	$clinicalSubjectId[$i] = $st;
	++$known{"clinicalSubjectId"};
	$fixSubjectId = 1;
    }

    if($fixSubjects && !$badSubjects) {
	if(defined($newSubject{"$clinicalSubjectId[$i]"})){
	    $clinicalSubjectId[$i] = $newSubject{"$clinicalSubjectId[$i]"};
	} else {
	    print ERR "+++ No short subject for '$clinicalSubjectId[$i]' in subjects$filenum{$filename}.dat\n";
	}
    }

    $fullSubject = "$studyId:$clinicalSubjectId[$i]";

    if(length($fullSubject) > 48) {
	$ls = length($fullSubject);
	if($fixSubjects) {
	    if($badSubjects) {
		print ERR "+++Subject too long $ls bad subjects$filenum{$filename}.dat file: '$clinicalSubjectId[$i]'\n";
	    } else {
		print ERR "+++Fixed subject too long $ls: '$clinicalSubjectId[$i]' in subjects$filenum{$filename}.dat\n";
	    }
	} else {
	    if(!$writeSubjects) {
		$writeSubjects = 1;
		print ERR "+++Fixed subject(s) too long $ls, created subjects$filenum{$filename}.dat: '$clinicalSubjectId[$i]'\n";
		%writeSubjects = ();
	    }
	}
    }

    if(defined($clinicalStudyGroup[$i])){
	if($clinicalStudyGroup[$i] eq "validataion") {$clinicalStudyGroup[$i] = "Validation"}
    }

    if(!defined($clinicalInfection[$i])) {$clinicalInfection[$i] = ""}
    elsif(defined($clinicalStrain[$i])) {$clinicalInfection[$i] .= " $clinicalStrain[$i]"}

    if(defined($sampleMolecule[$i])) {
	$sampleMolecule[$i] =~ s/ extracted from whole cells//g;
    }

    if(defined($clinicalStatus[$i])) {
	if($clinicalStatus[$i] =~ /healthy/) {
	    $clinicalStatusBrief[$i] = "Healthy";
	} elsif($clinicalStatus[$i] =~ /deceased/) {
	    $clinicalStatusBrief[$i] = "Dead";
	}
    }
    if(!defined($sampleType[$i])) {$sampleType[$i] = ""}
    if(!defined($samplePlatform[$i])) {$samplePlatform[$i] = $studyPlatform; ++$known{"samplePlatform"}}
    if(!defined($sampleOrganism[$i])) {$sampleOrganism[$i] = $studyOrganism; ++$known{"sampleOrganism"}}

    if(!defined($sampleTime[$i])) {$sampleTime[$i] = ""}
    elsif($sampleTime[$i] =~ /_months/) {
	$sampleTime[$i] =~  s/_months/ months/g;
	$sampleTime[$i] =~  s/^([0-9]) /0$1/g;
    }
    elsif($sampleTime[$i] =~ /_days/) {
	$sampleTime[$i] =~  s/_days/ days/g;
    }
    elsif($sampleTime[$i] =~ /[Dd]ay(\d+)$/) {
	$sampleTime[$i] =~  s/[Dd]ay(\d+)/$1 days/g;
    }
    elsif($sampleTime[$i] =~ /[Dd]ay (\d+)$/) {
	$sampleTime[$i] =~  s/[Dd]ay (\d+)/$1 days/g;
    }
    elsif($sampleTime[$i] =~ /[0-9]d$/) {
	$sampleTime[$i] =~  s/d$/ days/g;
    }
    elsif($sampleTime[$i] =~ /[0-9] d[ .]?p[ .]?i[.]?$/) {
	$sampleTime[$i] =~  s/d[ .]?p[ .]?i[.]?$/days/g;
    }
    elsif($sampleTime[$i] =~ /[0-9]h$/) {
	$sampleTime[$i] =~  s/h$/ hours/g;
    }
    elsif($sampleTime[$i] =~ /[0-9]hr$/) {
	$sampleTime[$i] =~  s/hr$/ hours/g;
    }
    elsif($sampleTime[$i] =~ /^([0-9]+ days) post ?infection days$/) {
	$sampleTime[$i] =  "$1 post infection";
    }
    elsif($sampleTime[$i] =~ /[0-9]hrs after treatment$/) {
	$sampleTime[$i] =~  s/hrs after treatment$/ hours/g;
    }
    elsif($sampleTime[$i] =~ /[0-9] hour infection [\(].*$/) {
	$sampleTime[$i] =~  s/([0-9]+) hour infection [\(].*/$1 hours/g;
    }

    $sampleTime[$i] =~  s/^([0-9] )/0$1/g;

    if($sampleTime[$i] =~ /^00 /) {$sampleTime[$i] = "Baseline"}

    if(!defined($demographicsGender[$i])){$demographicsGender[$i]=""}
    elsif($demographicsGender[$i] eq "M"){$demographicsGender[$i]="Male"}
    elsif($demographicsGender[$i] eq "F"){$demographicsGender[$i]="Female"}

    if(!defined($demographicsAge[$i])){$demographicsAge[$i]=""}
    elsif($demographicsAge[$i] eq "NA"){$demographicsAge[$i]=""}
    elsif($demographicsAge[$i] eq "N/A"){$demographicsAge[$i]=""}
    elsif($demographicsAge[$i] =~ /[\>](\d+)/){
	$ageGt = 1+$1;
	if($clinicalSubjectId [$i] =~ /_(\d+)y+/){$demographicsAge[$i]="$1"}
	else {$demographicsAge[$i]="$ageGt"}
    }

#    if($sampleTime[$i] eq "Baseline") {
#	if(!defined($knownSubject{$clinicalSubjectId[$i]})){
#	    $knownSubject{$clinicalSubjectId[$i]} = $i;
#	}
#    }

}

if($writeSubjects) {
    open(OUTSUBJECTS, ">subjects$filenum{$filename}.dat") || die "Failed to create subjects$filenum{$filename}.dat";
    for ($i=0; $i <= $#sampleacc;$i++){
	$writeSubjects{"$clinicalSubjectId[$i]"}++ ||
	    print OUTSUBJECTS "\t$clinicalSubjectId[$i]\n";
    }
    close OUTSUBJECTS;
}

if($fixSubjectId) {
    open(ALLSAMPLES, ">allsamples$filenum{$filename}.dat") || die "Failed to create allsamples$filenum{$filename}.dat";
    for ($i=0; $i <= $#sampleacc;$i++){
	print ALLSAMPLES "x\t$clinicalSubjectId[$i]\n";
    }
    close ALLSAMPLES;
}

#############################################
# 3. Identify master row for each subject ID
#    %knownSubject
#############################################

for ($i=0; $i <= $#sampleacc;$i++){
    $id = $clinicalSubjectId[$i];
    if(!defined($knownSubject{$id})) {
	$knownSubject{$id} = $i;
    }
}

#######################################################
# 4. Merge non-base rows into master row
#    o - longitudinal values to visit-specific columns
#######################################################

for ($i=0; $i <= $#sampleacc;$i++){
    $id = $clinicalSubjectId[$i];
    $basei = $knownSubject{$id};

    # fix data for subjects in more than one study
    if($basei != $i) {
    }
}

####################################
# 5. Further cleanup of data values
####################################

for ($i=0; $i <= $#sampleacc;$i++){

    if(!defined($demographicsRegion[$i])){$demographicsRegion[$i] = ""}
    if(!defined($demographicsBirthRegion[$i])){$demographicsBirthRegion[$i] = ""}

    if($knownSubject{$clinicalSubjectId[$i]} != $i) {next}

}

###########################################
# 6. Check extra data values to be reported
###########################################


$doRace = testUnique(@demographicsRace);
$doHeight = testUnique(@demographicsHeight);
$doWeight = testUnique(@demographicsHeight);
$doTissue = countUnique("",@clinicalSource);
$doSample = countUnique("",@sampleType) > 1;
$doTime = countUnique("",@sampleTime) > 1;
$doOrganism = testUnique(@sampleOrganism) || ($studyOrganism ne "Homo sapiens");
$doStudyGroup = testUniqueBysubject(@clinicalStudyGroup);
$doStrain = $doOrganism && defined($clinicalStrain[0]);
$doCirrhosis = testUnique(@clinicalCirrhosis);
$doStatus = testUniqueBysubject(@clinicalStatus);
$doNeutrophils = testUnique(@clinicalNeutrophils);
$doNeutrophilsStatus = testUnique(@labtestNeutrophilsStatus);
$doCauseOfDeath = testUnique(@clinicalCauseOfDeath);
$doDeathTime = testUnique(@clinicalDeathTime);
$doDisease = testUniqueBysubject(@clinicalDisease);
$doDiseaseState = testUniqueBysubject(@clinicalDiseaseState);
$doDiagAnamnestic = testUnique(@clinicalAnamnesticDays);
$doMedAntiviral = testUnique(@medicationAntiviral);
$doMedTreatment = testUniqueBysubject(@clinicalTreatment);
$doIfnTreatment = testUniqueBysubject(@clinicalIfnTreatment);
$doBMI = testUnique(@clinicalBMI);
$doDiagCopd = testUnique(@diagnosisCOPD);
$doDiagCough = testUnique(@clinicalCough);
$doCVRisk = testUnique(@clinicalCVRisk);
$doDadStage = testUnique(@diagnosisDadStage);
$doDadHist = testUnique(@pathologyDad);
$doDiagDiabetes = testUnique(@diagnosisDiabetes);
$doDiagDyspnea = testUnique(@diagnosisDyspnea);
$doDiagLungHist = testUnique(@pathologyLung);
$doPathology = testUnique(@clinicalPathology);
$doDiagFever = testUnique(@diagnosisFever);
$doTimeHosp = testUnique(@clinicalHospitalisationTime);
$doDiagHypertension = testUniqueBysubject(@diagnosisHypertension);
$doDiagIAEdema = testUniqueBysubject(@diagnosisIntraalveolarEdema);
$doDiagIAHem = testUniqueBysubject(@diagnosisIntraalveolarHemorrhage);
$doDiagTumorMal = testUnique(@diagnosisTumorMalignant);
$doMaterial = testUniqueBysubject(@clinicalMaterial);
$doDiagLung = testUnique(@diagnosisPreexistingLung);
$doLabtestCovCount = testUnique(@labtestSarscov2Copies);
$doSmoker = testUnique(@clinicalSmoker);
$doPMTime = testUnique(@clinicalPostmortemHours);
$doTopo = testUnique(@clinicalTopography);
$doFlowcell = testUnique(@clinicalFlowcell);
$doUsedInAnalysis = testUniqueBysubject(@clinicalUsedInAnalysis);
$doAnalysisVisit = testUnique(@clinicalAnalysisVisit);
$doModuleConstruct = testUnique(@clinicalModuleConstruct);
$doCVCoverage = testUnique(@clinicalCVCoverage);
$doPctAligned = testUnique(@clinicalPctAligned);
$doReadAlign = testUnique(@clinicalAlignedReads);
$doReadDedup = testUnique(@clinicalDedupReads);
$doReadRaw = testUnique(@clinicalRawReads);
$doReadStitch = testUnique(@clinicalStitchedReads);
$doReadTrim = testUnique(@clinicalTrimmedReads);
$doSeqSat = testUnique(@clinicalSeqSat);
$doViralType = testUnique(@clinicalViralType);
$doStatusOriginal = testUnique(@clinicalStatusOriginal);
$doStatusEvent = testUnique(@clinicalStatusEvent);
$doStatusMatched = testUnique(@clinicalStatusMatched);
$doHRVType = testUnique(@clinicalHRVType);
$doCSteroidStart = testUnique(@clinicalCSteroidStart);
$doVirusStrain = testUniqueBysubject(@clinicalVirusStrain);
$doVirusMOI = testUniqueBysubject(@clinicalVirusMOI);
$doVirusTiter = testUniqueBysubject(@clinicalVirusTiter);
$doVirusType = testUniqueBysubject(@clinicalVirusType);
$doVirusTypeEVHRV = testUnique(@clinicalVirusTypeEVHRV);
$doVirusTypeADV = testUnique(@clinicalVirusTypeADV);
$doVirusTypeBOCA = testUnique(@clinicalVirusTypeBOCA);
$doVirusTypeRSVA = testUnique(@clinicalVirusTypeRSVA);
$doVirusTypeRSVB = testUnique(@clinicalVirusTypeRSVB);
$doVirusTypeHKU1 = testUnique(@clinicalVirusTypeHKU1);
$doVirusTypeCovNL63 = testUnique(@clinicalVirusTypeCovNL63);
$doVirusTypePIV1 = testUnique(@clinicalVirusTypePIV1);
$doVirusTypePIV2 = testUnique(@clinicalVirusTypePIV2);
$doVirusTypePIV3 = testUnique(@clinicalVirusTypePIV3);
$doVirusTypePIV4 = testUnique(@clinicalVirusTypePIV4);
$doVirusTypeMPV = testUnique(@clinicalVirusTypeMPV);
$doVirusTypeFluB = testUnique(@clinicalVirusTypeFluB);
$doVirusTypeCoV229e = testUnique(@clinicalVirusTypeCoV229e);
$doNasalPctNeutrophil = testUnique(@clinicalNasalPctNeutrophil);
$doNasalPctLymphocyte = testUnique(@clinicalNasalPctLymphocyte);
$doNasalPctEosinophil = testUnique(@clinicalNasalPctEosinophil);
$doNasalPctMacrophage = testUnique(@clinicalNasalPctMacrophage);
$doNasalPctWhiteBloodCell = testUnique(@clinicalNasalPctWhiteBloodCell);
$doNasalPctEpithelial = testUnique(@clinicalNasalPctEpithelial);
$doNasalPctSquamous = testUnique(@clinicalNasalPctSquamous);
$doNasalPctEpithelialSquamous = testUnique(@clinicalNasalPctEpithelialSquamous);
$doBloodDiffBaso = testUnique(@clinicalBloodDiffBaso);
$doBloodDiffEosin = testUnique(@clinicalBloodDiffEosin);
$doBloodDiffLymph = testUnique(@clinicalBloodDiffLymph);
$doBloodDiffMono = testUnique(@clinicalBloodDiffMono);
$doBloodDiffNeutro = testUnique(@clinicalBloodDiffNeutro);
$doBloodCountErythro = testUnique(@clinicalBloodCountErythro);
$doBloodCountPlatelet = testUnique(@clinicalBloodCountPlatelet);
$doBloodCountWhite = testUnique(@clinicalBloodCountWhite);
$doBloodCountNuclei = testUnique(@clinicalBloodCountNuclei);
$doLibCounts = testUnique(@clinicalLibCounts);
$doPosition = testUnique(@clinicalPosition);
$doAnimalNum = testUnique(@clinicalAnimalNum);
$doCaseNum = testUnique(@clinicalCaseNum);
$doLesionNum = testUnique(@clinicalLesionNum);
$doIndivId = testUnique(@clinicalIndivId);
$doViralLoad = testUnique(@clinicalViralLoad);
$doViralPositivity = testUnique(@clinicalViralPositivity);
$doLocation = testUniqueBysubject(@clinicalLocation);
$doDrug = testUniqueBysubject(@medicationDrug);
$doCov2 = testUniqueBysubject(@clinicalCov2Infected);
$doCov2Pcr = testUniqueBysubject(@clinicalCov2Pcr);
$doCov2Rpm = testUniqueBysubject(@clinicalCov2Rpm);
$doDaysPos = testUniqueBysubject(@clinicalDaysPos);
$doMouseModel = testUniqueBysubject(@clinicalMouseModel);
$doCellLining = testUniqueBysubject(@clinicalCellLining);
$doDiagDM = testUniqueBysubject(@diagDiabetes);
$doLabIcuApacheii = testUniqueBysubject(@labsIcuApacheii);
$doLabIcuSofa = testUniqueBysubject(@labsIcuSofa);
$doLabCharlson = testUniqueBysubject(@labsCharlson);
$doLabCrp = testUniqueBysubject(@labsCrp);
$doLabDDimer = testUniqueBysubject(@labsDDimer);
$doLabFerritin = testUniqueBysubject(@labsFerritin);
$doLabFibrinogen = testUniqueBysubject(@labsFibrinogen);
$doLabLactate = testUniqueBysubject(@labsLactate);
$doLabProcalcitonin = testUniqueBysubject(@labsProcalcitonin);
$doHospFree = testUniqueBysubject(@clinicalHospFreeDays);
$doICU = testUniqueBysubject(@clinicalICU);
$doMechVent = testUniqueBysubject(@clinicalMechVent);
$doVentFree = testUniqueBysubject(@clinicalVentFreeDays);
$doMedSteroid = testUniqueBysubject(@medicationSteroid);
$doViralDose = testUniqueBysubject(@clinicalViralDose);

$doSeverity = testUnique(@clinicalSeverity);
$doCellType = testUniqueBysubject(@sampleCellLine);
$doDonor = testUniqueBysubject(@sampleDonor);
$doW = testUnique(@clinicalW);
$doMolecule = testUnique(@sampleMolecule);
$doOrigin = testUnique(@sampleOrigin);
$doLy6e = testUnique(@sampleLy6e);
$doCell = testUniqueBysubject(@clinicalCell);
$doTarget = testUnique(@clinicalTarget);
$doVirusAcc = testUniqueBysubject(@clinicalVirusAcc);
$doVirusTaxon = testUniqueBysubject(@clinicalVirusTaxon);
$doIfnA2b = testUnique(@clinicalIfnA2b);
$doIfnBeta = testUnique(@clinicalIfnBeta);
$doAntibody = testUniqueBysubject(@clinicalAntibody);
$doTiterBase = testUnique(@clinicalTiterBase);
$doTiterSpring = testUnique(@clinicalTiterSpring);
$doSampleGenotype = testUniqueBysubject(@sampleGenotype);

$txtPlatform = $txtTissue = $txtSample = $txtTime="";

# Platform type, or description, inserted in tree
if($studyDataType ne "UNKNOWN") {
    if($studyDataType eq "Expression") {$txtPlatform = "+PLATFORM"}
    elsif($studyDataType eq "RNAseq") {$txtPlatform = "+RNAseq"}
    else {$txtPlatform = "+$studyDataType"}
}

if($doTissue){$txtTissue = "+TISSUETYPE"}
if($doSample){$txtSample = "+ATTR1"}
if($doTime){$txtTime = "+ATTR2"}

if(defined($contribLastname)) {$studyAuthor = $contribLastname; ++$known{"studyAuthor"}}
elsif(defined($contactLastname)) {$studyAuthor = $contactLastname; ++$known{"studyAuthor"}}

#################################################
# 7. Print headers with any extra columns
#################################################

if(defined($dataTypeNames{$studyDataType})){
    $mapPrefix = $dataTypeNames{$studyDataType};
} else {
    $mapPrefix = "rnaseq";
}
open(MAPEXPRESS, ">$mapPrefix\_sample_subject_mapping$filenum{$filename}.txt") || die "Cannot open sample file";

open(OUTSAMPLES, ">samples$filenum{$filename}.txt") || die "Cannot open sample explorer data file";
open(SAMPLESPARAMS, ">samples.params$filenum{$filename}.out") || die "Cannot open sample explorer data params file";
open(OUTBROWSE,  ">browse$filenum{$filename}.txt") || die "Cannot open browse study file";
open(BROWSEPARAMS,  ">browse.params$filenum{$filename}.out") || die "Cannot open browse params file";

print MAPEXPRESS "STUDY_ID\tSITE_ID\tSUBJECT_ID\tSAMPLE_CD\tPLATFORM\tTISSUETYPE\tATTR1\tATTR2\tCATEGORY_CD\tSOURCE_CD\n";

#####################
# Print clinical data
#####################

print OUTDATA "STUDY_ID\tSUBJ_ID\tSAMPLE_ID\tAge\tGender\tEthnicity";


print MAPDATA "Filename\tCategory_Code\tColumn_Number\tData_Label\n";
print MAPDATA "clinical_data$filenum{$filename}.txt\t\t1\tOMIT\n";
print MAPDATA "clinical_data$filenum{$filename}.txt\tSubject\t2\tSUBJ_ID\n";
print MAPDATA "clinical_data$filenum{$filename}.txt\t\t3\tOMIT\n";
print MAPDATA "clinical_data$filenum{$filename}.txt\tDemographics\t4\tAge\n";
print MAPDATA "clinical_data$filenum{$filename}.txt\tDemographics\t5\tGender\n";
print MAPDATA "clinical_data$filenum{$filename}.txt\tDemographics\t6\tEthnicity\n";
$mapcol = 6;

$subjectCount = countUnique("",@clinicalSubjectId);
$rowCount = 1+$#sampleacc;

if($fixSubjectId && $subjectCount != $rowCount) {
    print ERR "+++Subjects/rows $subjectCount/$rowCount\n";
}

if($doRace) {
    ++$mapcol;
    print OUTDATA "\tRace";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDemographics\t$mapcol\tRace\n";
}
if($doHeight) {
    ++$mapcol;
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDemographics\t$mapcol\tHeight\n";
    print OUTDATA "\tHeight";
}
if($doWeight) {
    ++$mapcol;
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDemographics\t$mapcol\tWeight\n";
    print OUTDATA "\tWeight";
}
if($doStudyGroup) {
    ++$mapcol;
    print OUTDATA "\tStudy_group";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tTrial_Design\t$mapcol\tStudy_group\n";
}
if($doOrganism) {
    ++$mapcol;
    print OUTDATA "\tSpecies";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tOrganism\t$mapcol\tSpecies\n";
    ++$mapcol;
    print OUTDATA "\tCommon_name";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tOrganism\t$mapcol\tCommon_name\n";
    if($doStrain) {
	++$mapcol;
	print OUTDATA "\tStrain";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tOrganism\t$mapcol\tStrain\n";
    }
    $orgLastCol = $mapcol;
}
if($doCirrhosis) {
    ++$mapcol;
    print OUTDATA "\tCirrhosis";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tCirrhosis_present\n";
}
if($doStatus) {
    ++$mapcol;
    print OUTDATA "\tSubject_status";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tReports+Vital_Status\t$mapcol\tDetail\n";
}
if($doNeutrophils) {
    ++$mapcol;
    print OUTDATA "\tNeutrophils";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tNeutrophils\n";
}
if($doNeutrophilsStatus) {
    ++$mapcol;
    print OUTDATA "\tNeutrophilLevel";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tNeutrophil level\n";
}
if($doCauseOfDeath) {
    ++$mapcol;
    print OUTDATA "\tCause_of_death";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tCause of death\n";
}
if($doDeathTime) {
    ++$mapcol;
    print OUTDATA "\tTime_of_death";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tReports+Vital_Status\t$mapcol\tTime of death\n";
}
if($doDisease) {
    ++$mapcol;
    print OUTDATA "\tDiagnosis";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tDiagnosis\n";
}
if($doDiseaseState) {
    ++$mapcol;
    print OUTDATA "\tDisease_state";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tDisease_state\n";
}
if($doDiagAnamnestic) {
    ++$mapcol;
    print OUTDATA "\tAnamnestic_days";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tAnamnestic days\n";
}
if($doMedAntiviral) {
    ++$mapcol;
    print OUTDATA "\tAntivirals";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tMedication\t$mapcol\tAntivirals\n";
}
if($doMedTreatment) {
    ++$mapcol;
    print OUTDATA "\tTreatment";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tMedication\t$mapcol\tTreatment\n";
}
if($doIfnTreatment) {
    ++$mapcol;
    print OUTDATA "\tIfn_treatment";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tMedication\t$mapcol\tInterferon treatment\n";
}
if($doBMI) {
    ++$mapcol;
    print OUTDATA "\tBMI";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSubject\t$mapcol\tBMI\n";
}
if($doDiagCopd) {
    ++$mapcol;
    print OUTDATA "\tCOPD";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tCOPD\n";
}
if($doDiagCough) {
    ++$mapcol;
    print OUTDATA "\tCough";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tCough\n";
}
if($doCVRisk) {
    ++$mapcol;
    print OUTDATA "\tCVRF";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSubject\t$mapcol\tCV risk\n";
}
if($doDadStage) {
    ++$mapcol;
    print OUTDATA "\tDAD_Stage";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tDAD stage\n";
}
if($doDadHist) {
    ++$mapcol;
    print OUTDATA "\tDAD";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tDiffuse alveolar damage\n";
}
if($doDiagDiabetes) {
    ++$mapcol;
    print OUTDATA "\tDiabetes";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tDiabetes\n";
}
if($doDiagDyspnea) {
    ++$mapcol;
    print OUTDATA "\tDyspnea";
    print MAPDATA "clinical_data$filenum{$filename}.txt\t\t$mapcol\tDyspnea\n";
}
if($doDiagLungHist) {
    ++$mapcol;
    print OUTDATA "\tLungHist";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tLung histology\n";
}
if($doPathology) {
    ++$mapcol;
    print OUTDATA "\tPathology";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tPathology\n";
}
if($doDiagFever) {
    ++$mapcol;
    print OUTDATA "\tFever";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tFever\n";
}
if($doTimeHosp) {
    ++$mapcol;
    print OUTDATA "\tHospitalDays";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tVital_Status\t$mapcol\tHospitalized days\n";
}
if($doDiagHypertension) {
    ++$mapcol;
    print OUTDATA "\tHypertension";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tHypertension\n";
}
if($doDiagIAEdema) {
    ++$mapcol;
    print OUTDATA "\tIAEdema";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tIntraalveloar edema\n";
}
if($doDiagIAHem) {
    ++$mapcol;
    print OUTDATA "\tIAHemorrhage";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tIntraalveolar hemorrhage\n";
}
if($doDiagTumorMal) {
    ++$mapcol;
    print OUTDATA "\tTumorMalignant";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tTumor (malignant)\n";
}
if($doMaterial) {
    ++$mapcol;
    print OUTDATA "\tMaterial";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tMaterial\n";
}
if($doDiagLung) {
    ++$mapcol;
    print OUTDATA "\tPreLung";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tPre-existing pulmonary conditions\n";
}
if($doLabtestCovCount) {
    ++$mapcol;
    print OUTDATA "\tGenomeCount";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tViral genomes/million\n";
}
if($doSmoker) {
    ++$mapcol;
    print OUTDATA "\tSmoker";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSubject\t$mapcol\tSmoker\n";
}
if($doPMTime) {
    ++$mapcol;
    print OUTDATA "\tTimePM";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tVital Status\t$mapcol\tPost mortem hours\n";
}
if($doTopo) {
    ++$mapcol;
    print OUTDATA "\tTopology";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tTopology\n";
}
if($doFlowcell) {
    ++$mapcol;
    print OUTDATA "\tFlowcell";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tFlowcell\n";
}
if($doUsedInAnalysis) {
    ++$mapcol;
    print OUTDATA "\tUsed_in_analysis";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tUsed in analysis\n";
}
if($doAnalysisVisit) {
    ++$mapcol;
    print OUTDATA "\tAnalysis_visit";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tVISIT_NAME\n";
}
if($doModuleConstruct) {
    ++$mapcol;
    print OUTDATA "\tUsed_in_module_construction";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tUsed in module construction\n";
}
if($doCVCoverage) {
    ++$mapcol;
    print OUTDATA "\tCV_coverage";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tCV coverage\n";
}
if($doPctAligned) {
    ++$mapcol;
    print OUTDATA "\tPct_aligned";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tPercent aligned\n";
}
if($doReadAlign) {
    ++$mapcol;
    print OUTDATA "\tAligned_reads";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Reads\t$mapcol\tAligned reads\n";
}
if($doReadDedup) {
    ++$mapcol;
    print OUTDATA "\tDeduplicated_reads";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Reads\t$mapcol\tDeduplicated reads\n";
}
if($doReadRaw) {
    ++$mapcol;
    print OUTDATA "\tRaw_reads";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Reads\t$mapcol\tRaw reads\n";
}
if($doReadStitch) {
    ++$mapcol;
    print OUTDATA "\tStitched_reads";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Reads\t$mapcol\tStitched reads\n";
}
if($doReadTrim) {
    ++$mapcol;
    print OUTDATA "\tTrimmed_reads";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Reads\t$mapcol\tTrimmed reads\n";
}
if($doSeqSat) {
    ++$mapcol;
    print OUTDATA "\tSequence_saturation";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Reads\t$mapcol\tSequence saturation\n";
}
if($doViralType) {
    ++$mapcol;
    print OUTDATA "\tViral_type";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tViral type\n";
}
if($doStatusOriginal) {
    ++$mapcol;
    print OUTDATA "\tStatus_original";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tTrial_Design\t$mapcol\tStatus original\n";
}
if($doStatusEvent) {
    ++$mapcol;
    print OUTDATA "\tStatus_event";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tTrial_Design\t$mapcol\tStatus event\n";
}
if($doStatusMatched) {
    ++$mapcol;
    print OUTDATA "\tStatus_matched";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tTrial_Design\t$mapcol\tStatus matched\n";
}
if($doHRVType) {
    ++$mapcol;
    print OUTDATA "\tHRV_type";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tHuman respiratory virus type\n";
}
if($doCSteroidStart) {
    ++$mapcol;
    print OUTDATA "\tCsteroidstart";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tMedication\t$mapcol\tCorticosteroid start\n";
}
if($doVirusTypeEVHRV) {
    ++$mapcol;
    print OUTDATA "\tVirus_evhrv";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tEnterovirus HRV\n";
}
if($doVirusTypeADV) {
    ++$mapcol;
    print OUTDATA "\tVirus_adv";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tAdenovirus\n";
}
if($doVirusTypeBOCA) {
    ++$mapcol;
    print OUTDATA "\tVirus_boca";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tBocavirus\n";
}
if($doVirusTypeRSVA) {
    ++$mapcol;
    print OUTDATA "\tVirus_rsva";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tRespiratory syncytial virus A\n";
}
if($doVirusTypeRSVB) {
    ++$mapcol;
    print OUTDATA "\tVirus_rsvb";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tRespiratory syncytial virus B\n";
}
if($doVirusTypeHKU1) {
    ++$mapcol;
    print OUTDATA "\tVirus_hku1";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tCoronavirus HKU1\n";
}
if($doVirusTypeCovNL63) {
    ++$mapcol;
    print OUTDATA "\tVirus+nl63";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tCoronavirus NL63\n";
}
if($doVirusTypePIV1) {
    ++$mapcol;
    print OUTDATA "\tVirus_piv1";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tParainfluenza 1\n";
}
if($doVirusTypePIV2) {
    ++$mapcol;
    print OUTDATA "\tVirus_piv2";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tParainfluenza 2\n";
}
if($doVirusTypePIV3) {
    ++$mapcol;
    print OUTDATA "\tVirus_piv3";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tParainfluenza 3\n";
}
if($doVirusTypePIV4) {
    ++$mapcol;
    print OUTDATA "\tVirus_piv4";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tParainfluenza 4\n";
}
if($doVirusTypeMPV) {
    ++$mapcol;
    print OUTDATA "\tVirus_mpv";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tMetapneumovirus\n";
}
if($doVirusTypeFluB) {
    ++$mapcol;
    print OUTDATA "\tVirus_flub";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tInfluenza B\n";
}
if($doVirusTypeCoV229e) {
    ++$mapcol;
    print OUTDATA "\tVirus_cov229e";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis+Virus_Type\t$mapcol\tCoronavirus 229E\n";
}
if($doNasalPctNeutrophil) {
    ++$mapcol;
    print OUTDATA "\tPct_neutro";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Percent\t$mapcol\tNeutrophil\n";
}
if($doNasalPctLymphocyte) {
    ++$mapcol;
    print OUTDATA "\tPct_lymph";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Percent\t$mapcol\tLymphocyte\n";
}
if($doNasalPctEosinophil) {
    ++$mapcol;
    print OUTDATA "\tPct_eosin";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Percent\t$mapcol\tEosinophil\n";
}
if($doNasalPctMacrophage) {
    ++$mapcol;
    print OUTDATA "\tPct_macro";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Percent\t$mapcol\tMacrophage\n";
}
if($doNasalPctWhiteBloodCell) {
    ++$mapcol;
    print OUTDATA "\tPct_wbc";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Percent\t$mapcol\tWhite blood cell\n";
}
if($doNasalPctEpithelial) {
    ++$mapcol;
    print OUTDATA "\tPct_epi";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Percent\t$mapcol\tEpithelial\n";
}
if($doNasalPctSquamous) {
    ++$mapcol;
    print OUTDATA "\tPct_squam";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Percent\t$mapcol\tSquamous\n";
}
if($doNasalPctEpithelialSquamous) {
    ++$mapcol;
    print OUTDATA "\tPct_epi_suam";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Percent\t$mapcol\tEpithelial/squamous\n";
}
if($doBloodDiffBaso) {
    ++$mapcol;
    print OUTDATA "\tBlood_diff_baso";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Differential\t$mapcol\tBasophils\n";
}
if($doBloodDiffEosin) {
    ++$mapcol;
    print OUTDATA "\tBlood_diff_eosin";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Differential\t$mapcol\tEosinophils\n";
}
if($doBloodDiffLymph) {
    ++$mapcol;
    print OUTDATA "\tBlood_diff_lymph";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Differential\t$mapcol\tLymphocytes\n";
}
if($doBloodDiffMono) {
    ++$mapcol;
    print OUTDATA "\tBlood_diff_mono";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Differential\t$mapcol\tMonocytes\n";
}
if($doBloodDiffNeutro) {
    ++$mapcol;
    print OUTDATA "\tBlood_diff_neutro";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Differential\t$mapcol\tNeutrophils\n";
}
if($doBloodCountErythro) {
    ++$mapcol;
    print OUTDATA "\tBlood_cnt_ery";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Counts\t$mapcol\tErythrocytes\n";
}
if($doBloodCountPlatelet) {
    ++$mapcol;
    print OUTDATA "\tBlood_cnt_plate";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Counts\t$mapcol\tPlatelets\n";
}
if($doBloodCountWhite) {
    ++$mapcol;
    print OUTDATA "\tBlood_cnt_white";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Counts\t$mapcol\tWhite blood cells\n";
}
if($doBloodCountNuclei) {
    ++$mapcol;
    print OUTDATA "\tBlood_cnt_nuclei";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Cell_Counts\t$mapcol\tNuclei\n";
}
if($doLibCounts) {
    ++$mapcol;
    print OUTDATA "\tLib_counts";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tLibrary counts\n";
}
if($doPosition) {
    ++$mapcol;
    print OUTDATA "\tPosition";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tPosition\n";
}
if($doAnimalNum) {
    ++$mapcol;
    print OUTDATA "\tAnimal_num";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tAnimal number\n";
}
if($doCaseNum) {
    ++$mapcol;
    print OUTDATA "\tCase_num";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tCase number\n";
}
if($doLesionNum) {
    ++$mapcol;
    print OUTDATA "\tLesion_num";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tLesion number\n";
}
if($doIndivId) {
    ++$mapcol;
    print OUTDATA "\tIndiv_id";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSubject\t$mapcol\tIndividual ID\n";
}
if($doViralLoad) {
    ++$mapcol;
    print OUTDATA "\tViral_load";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tViral load\n";
}
if($doViralPositivity) {
    ++$mapcol;
    print OUTDATA "\tViral_pos";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tViral positivity\n";
}
if($doLocation) {
    ++$mapcol;
    print OUTDATA "\tLocation";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSubject\t$mapcol\tLocation\n";
}
if($doDrug) {
    ++$mapcol;
    print OUTDATA "\tDrug";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tMedication\t$mapcol\tDrug\n";
}
if($doCov2) {
    ++$mapcol;
    print OUTDATA "\tCov2_infected";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis\t$mapcol\tSARS-CoV-2 infected\n";
}
if($doDaysPos) {
    ++$mapcol;
    print OUTDATA "\tDays_pos";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis\t$mapcol\tDays since positive test\n";
}
if($doMouseModel) {
    ++$mapcol;
    print OUTDATA "\tMouse_model";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tOrganism\t$mapcol\tMouse model\n";
}
if($doCellLining) {
    ++$mapcol;
    print OUTDATA "\tCell_lining";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tCell lining\n";
}
if($doDiagDM) {
    ++$mapcol;
    print OUTDATA "\tDiabetes";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tDiabetes mellitus\n";
}
if($doLabCrp) {
    ++$mapcol;
    print OUTDATA "\tCrp";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Tests\t$mapcol\tC-reactive protein (mg/l)\n";
}
if($doLabDDimer) {
    ++$mapcol;
    print OUTDATA "\tD-dimer";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Tests\t$mapcol\tD-dimer (mg/l FEU)\n";
}
if($doLabFerritin) {
    ++$mapcol;
    print OUTDATA "\tFerritin";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Tests\t$mapcol\tFerritin (ng/ml)\n";
}
if($doLabFibrinogen) {
    ++$mapcol;
    print OUTDATA "\tFibrinogen";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Tests\t$mapcol\tFibrinogen (mg/dl)\n";
}
if($doLabLactate) {
    ++$mapcol;
    print OUTDATA "\tLactate";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Tests\t$mapcol\tLactate (mmol/l)\n";
}
if($doLabProcalcitonin) {
    ++$mapcol;
    print OUTDATA "\tProcalcitonin";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Blood_Tests\t$mapcol\tProcalcitonin (ng/ml)\n";
}
if($doLabIcuApacheii) {
    ++$mapcol;
    print OUTDATA "\tApacheII";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Test_Scores\t$mapcol\tApache II ICU score\n";
}
if($doLabCharlson) {
    ++$mapcol;
    print OUTDATA "\tCharlson";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Test_Scores\t$mapcol\tCharlson comorbidity score\n";
}
if($doLabIcuSofa) {
    ++$mapcol;
    print OUTDATA "\tSofa";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Test_Scores\t$mapcol\tSOFA ICU score\n";
}
if($doHospFree) {
    ++$mapcol;
    print OUTDATA "\tHosp_free";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tTreatment\t$mapcol\tHospital-free days\n";
}
if($doICU) {
    ++$mapcol;
    print OUTDATA "\tICU";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tTreatment\t$mapcol\tICU\n";
}
if($doMechVent) {
    ++$mapcol;
    print OUTDATA "\tMech_vent";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tTreatment\t$mapcol\tMechanical ventilation\n";
}
if($doVentFree) {
    ++$mapcol;
    print OUTDATA "\tVent_free";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tTreatment\t$mapcol\tVentilator-free days\n";
}
if($doMedSteroid) {
    ++$mapcol;
    print OUTDATA "\tSteroid";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tMedications\t$mapcol\tSteroid\n";
}
if($doViralDose) {
    ++$mapcol;
    print OUTDATA "\tViral_dose";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tViral dose\n";
}
if($doSeverity) {
    ++$mapcol;
    print OUTDATA "\tDisease_severity";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnoses\t$mapcol\tDisease severity\n";
}
if($doCellType) {
    ++$mapcol;
    print OUTDATA "\tCell_line";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tCell line\n";
}
if($doDonor) {
    ++$mapcol;
    print OUTDATA "\tCell_line_donor";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tCell line donor\n";
}
if($doCell) {
    ++$mapcol;
    print OUTDATA "\tCell";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tCell\n";
}
if($doW) {
    ++$mapcol;
    print OUTDATA "\tW";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tOther\t$mapcol\tW\n";
}
if($doMolecule) {
    ++$mapcol;
    print OUTDATA "\tMolecule";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tMolecule\n";
}
if($doOrigin) {
    ++$mapcol;
    print OUTDATA "\tSample_origin";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSample origin\n";
}
if($doLy6e) {
    ++$mapcol;
    print OUTDATA "\tLy6e_Knockout";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tOrganism\t$mapcol\tLy6e Knockout\n";
}
if($doTarget) {
    ++$mapcol;
    print OUTDATA "\tTarget";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tTarget\n";
}
if($doVirusType) {
    ++$mapcol;
    print OUTDATA "\tVirus_type";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis\t$mapcol\tVirus type\n";
}
if($doVirusStrain) {
    ++$mapcol;
    print OUTDATA "\tVirus_strain";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis\t$mapcol\tVirus strain\n";
}
if($doVirusMOI) {
    ++$mapcol;
    print OUTDATA "\tVirus_moi";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis\t$mapcol\tVirus MOI\n";
}
if($doVirusTiter) {
    ++$mapcol;
    print OUTDATA "\tVirus_titer";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis\t$mapcol\tVirus titer\n";
}
if($doVirusAcc) {
    ++$mapcol;
    print OUTDATA "\tVirus_accession";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tVirus_accession\n";
}
if($doVirusTaxon) {
    ++$mapcol;
    print OUTDATA "\tVirus_taxon";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tVirus_taxon\n";
    ++$mapcol;
    print OUTDATA "\tVirus_species";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tVirus_species\n";
}
if($doIfnA2b) {
    ++$mapcol;
    print OUTDATA "\tInterferon_A2b";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tInterferon_A2b\n";
}
if($doIfnBeta) {
    ++$mapcol;
    print OUTDATA "\tInterferon_Beta";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tInterferon_Beta\n";
}

if($doAntibody) {
    ++$mapcol;
    print OUTDATA "\tAntibody";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests\t$mapcol\tAntibody\n";
}
if($doTiterBase) {
    ++$mapcol;
    print OUTDATA "\tTiter_base";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Titers_anti_A/H1N1_AB\t$mapcol\tBaseline\n";
}
if($doTiterSpring) {
    ++$mapcol;
    print OUTDATA "\tTiter_spring";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tLaboratory_Tests+Titers_anti_A/H1N1_AB\t$mapcol\tSpring\n";
}
if($doSampleGenotype) {
    ++$mapcol;
    print OUTDATA "\tGenotype";
    print MAPDATA "clinical_data$filenum{$filename}.txt\tOrganism\t$mapcol\tGenotype\n";
}

$addSampleData = 0;
if($mapcol < 7 || ($doOrganism && $mapcol == $orgLastCol)) {
    if(keys(%knownSubject) == (1+$#sampleacc)) { # one subject per sample
	$addSampleData = 1;
	print ERR "+++No clinical data: add sample data to clinical data ".keys(%knownSubject)." subjects+samples\n";
    } else {
	$addSampleData = 1;
	print ERR "+++No clinical data subjects: ".keys(%knownSubject)." samples: ".(1+$#sampleacc)."\n";
    }
} elsif(keys(%knownSubject) == (1+$#sampleacc)) {
    if(-e "subjects$filenum{$filename}.dat") {
	print ERR "...One sample per subject confirmed by subjects$filenum{$filename}.dat\n";
	$addSampleData = 1;
    } elsif(-e "addsamples$filenum{$filename}.control") {
	print ERR "...One sample per subject: addsample$filenum{$filename}.control found\n";
	$addSampleData = 1;
    } elsif(-e "skipsamples$filenum{$filename}.control") {
	print ERR "...One sample per subject: skipsample$filenum{$filename}.control found\n";
	$addSampleData = 0;
    } else {
	print ERR "+++One sample per subject: check subject IDs and consider adding samples\n";
    }
}

if($addSampleData) {
    $doInfectionStatus = testUniqueBysubject(@clinicalInfectionStatus);
    $doSampleSource = testUniqueBysubject(@clinicalSource);
    $doSampleTissue = testUniqueBysubject(@sampleTissue);
    $doSampleCell = testUniqueBysubject(@clinicalCell);
    $doSampleCellLine = testUniqueBysubject(@sampleCellLine);
    $doDonor = testUniqueBysubject(@sampleDonor);
    $doSampleInfection = testUniqueBysubject(@clinicalInfection);
    $doSampleDose = testUniqueBysubject(@sampleDose);
    $doSampleMolecule = testUniqueBysubject(@sampleMolecule);
    $doSampleType = testUniqueBysubject(@sampleType);
    $doSampleTime = testUniqueBysubject(@sampleTime);
    $doSampleProvider = testUniqueBysubject(@sampleProvider);
    $doSampleProviderId = testUniqueBysubject(@sampleProviderId);
    $doSampleConfirmedContent = testUniqueBysubject(@sampleConfirmedContent);
    $doSampleAmplification = testUniqueBysubject(@sampleAmplification);
    $doSampleLayoutVersion = testUniqueBysubject(@sampleLayoutVersion);
    $doSampleLibManu = testUniqueBysubject(@sampleLibManu);
    $doSampleLibSeq = testUniqueBysubject(@sampleLibSeq);
    $doSampleSortGate = testUniqueBysubject(@sampleSortGate);
    $doSamplePlateNum = testUniqueBysubject(@samplePlateNum);
    $doSamplePoolNum = testUniqueBysubject(@samplePoolNum);
    $doSampleSeqTech = testUniqueBysubject(@sampleSeqTech);
    $doSampleSeqBatch = testUniqueBysubject(@sampleSeqBatch);
    $doSampleLibType = testUniqueBysubject(@sampleLibType);
    $doSampleDevStage = testUniqueBysubject(@sampleDevStage);
    $doSampleMedia = testUniqueBysubject(@sampleMedia);
    $doSampleIsolation = testUniqueBysubject(@sampleIsolation);
    $doSampleHVC = testUniqueBysubject(@sampleHVC);
    $doSamplePassage = testUniqueBysubject(@samplePassage);
    $doSampleDms = testUniqueBysubject(@sampleDms);
    $doSampleRnaPulldown = testUniqueBysubject(@sampleRnaPulldown);
    $doSampleSgrna = testUniqueBysubject(@sampleSgrna);
    $doSampleName = testUniqueBysubject(@sampleName);
    $doSampleLane = testUniqueBysubject(@sampleLane);
    $doRnaPop = testUniqueBysubject(@sampleRnaPop);
    $doBatch = testUniqueBysubject(@sampleBatch);
    $doViralRna = testUniqueBysubject(@sampleViralRna);
    $doISHStatus = testUniqueBysubject(@sampleISHStatus);
    $doHybCode = testUniqueBysubject(@sampleHybCode);
    $doSegment = testUniqueBysubject(@sampleSegment);
    $doSegmentType = testUniqueBysubject(@sampleSegmentType);
    $doROI = testUniqueBysubject(@sampleROI);
    $doROIx = testUniqueBysubject(@sampleROIx);
    $doROIy = testUniqueBysubject(@sampleROIy);
    $doNegNorm = testUniqueBysubject(@sampleNegNorm);
    $doArea = testUniqueBysubject(@sampleArea);
    $doLowSig = testUniqueBysubject(@sampleLowSig);
    $doTissueQuality = testUniqueBysubject(@sampleTissueQuality);
    $doTissueStructure = testUniqueBysubject(@sampleTissueStructure);
    $doTissueSubstructure = testUnique(@sampleTissueSubstructure);
    $doTissueNotes = testUniqueBysubject(@sampleTissueNotes);
    $doTemperature = testUniqueBysubject(@sampleTemperature);

    if($doInfectionStatus) {
	++$mapcol;
	print OUTDATA "\tInfection_status";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis\t$mapcol\tInfection status\n";
    }
    if($doSampleSource) {
	++$mapcol;
	print OUTDATA "\tSource";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSource\n";
	$txtTissue = "";
    }
    if($doSampleTissue) {
	++$mapcol;
	print OUTDATA "\tTissue";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tTissue\n";
	$txtTissue = "";
    }
    if($doSampleCell) {
	++$mapcol;
	print OUTDATA "\tCell";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tCell\n";
    }
    if($doSampleCellLine) {
	++$mapcol;
	print OUTDATA "\tCell_line";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tCell line\n";
    }
    if($doDonor) {
	++$mapcol;
	print OUTDATA "\tCell_line_donor";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tCell line donor\n";
    }
    if($doSampleInfection) {
	++$mapcol;
	print OUTDATA "\tInfection";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tInfection\n";
    }
    if($doSampleDose) {
	++$mapcol;
	print OUTDATA "\tDose";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tDose\n";
    }
    if($doSampleMolecule) {
	++$mapcol;
	print OUTDATA "\tMolecule";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tMolecule\n";
    }
    if($doSampleType) {
	++$mapcol;
	print OUTDATA "\tTreatment";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tTreatment\n";
	$txtSample = "";
    }
    if($doSampleTime) {
	++$mapcol;
	print OUTDATA "\tTimepoint";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tTimepoint\n";
	$txtTime = "";
    }
    if($doSampleProvider) {
	++$mapcol;
	print OUTDATA "\tProvider";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tProvider\n";
    }
    if($doSampleProviderId) {
	++$mapcol;
	print OUTDATA "\tProvider_id";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tProvider ID\n";
    }
    if($doSampleConfirmedContent) {
	++$mapcol;
	print OUTDATA "\tConfirmed_content";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tConfirmed content\n";
    }
    if($doSampleAmplification) {
	++$mapcol;
	print OUTDATA "\tAmplification";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tAmplification\n";
    }
    if($doSampleLayoutVersion) {
	++$mapcol;
	print OUTDATA "\tLayout_version";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tLayout version\n";
    }
    if($doSampleLibManu) {
	++$mapcol;
	print OUTDATA "\tLib_manuscript";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Libraries\t$mapcol\tManuscript library\n";
    }
    if($doSampleLibSeq) {
	++$mapcol;
	print OUTDATA "\tLib_seq";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Libraries\t$mapcol\tSequencing library\n";
    }
    if($doSampleSortGate) {
	++$mapcol;
	print OUTDATA "\tSort_gate";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSort gate\n";
    }
    if($doSamplePlateNum) {
	++$mapcol;
	print OUTDATA "\tPlate_num";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tPlate number\n";
    }
    if($doSamplePoolNum) {
	++$mapcol;
	print OUTDATA "\tPool_num";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tPool number\n";
    }
    if($doSampleSeqTech) {
	++$mapcol;
	print OUTDATA "\tSeq_technology";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSequencing technology\n";
    }
    if($doSampleSeqBatch) {
	++$mapcol;
	print OUTDATA "\tSeq_batch";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSequencing batch\n";
    }
    if($doSampleLibType) {
	++$mapcol;
	print OUTDATA "\tLib_type";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tLibrary type\n";
    }
    if($doSampleDevStage) {
	++$mapcol;
	print OUTDATA "\tDev_stage";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tDevelopmental stage\n";
    }
    if($doSampleMedia) {
	++$mapcol;
	print OUTDATA "\t";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tCulture media\n";
    }
    if($doSampleIsolation) {
	++$mapcol;
	print OUTDATA "\tIsolation_method";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tIsolation method\n";
    }
    if($doSamplePreparation) {
	++$mapcol;
	print OUTDATA "\tPreparation_method";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tPreparation method\n";
    }
    if($doSamplePassage) {
	++$mapcol;
	print OUTDATA "\tPassage";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tPassage\n";
    }
    if($doSampleDms) {
	++$mapcol;
	print OUTDATA "\tDMS_treat";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tDMS treatment\n";
    }
    if($doSampleRnaPulldown) {
	++$mapcol;
	print OUTDATA "\tRNA_pulldown";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tRNA pulldown\n";
    }
    if($doSampleSgrna) {
	++$mapcol;
	print OUTDATA "\tsgRNA";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSingle guide RNA\n";
    }
    if($doSampleName) {
	++$mapcol;
	print OUTDATA "\tSample_name";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSample name\n";
    }
    if($doSampleLane) {
	++$mapcol;
	print OUTDATA "\tSample_lane";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSample lane\n";
    }
    if($doRnaPop) {
	++$mapcol;
	print OUTDATA "\tRNA_pop";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tRNA population\n";
    }
    if($doBatch) {
	++$mapcol;
	print OUTDATA "\tBatch";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tBatch\n";
    }
    if($doViralRna) {
	++$mapcol;
	print OUTDATA "\tViral_RNA";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tViral RNA\n";
    }
    if($doISHStatus) {
	++$mapcol;
	print OUTDATA "\tISH_status";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+In-site_Hybridization\t$mapcol\tISH status\n";
    }
    if($doHybCode) {
	++$mapcol;
	print OUTDATA "\tHyb_code";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+In-site_Hybridization\t$mapcol\tHybridization code\n";
    }
    if($doSegment) {
	++$mapcol;
	print OUTDATA "\tSegment";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Segments\t$mapcol\tSegment\n";
    }
    if($doSegmentType) {
	++$mapcol;
	print OUTDATA "\tSeg_type";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Segments\t$mapcol\tSegment type\n";
    }
    if($doROI) {
	++$mapcol;
	print OUTDATA "\troi";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Images+Region_of_Interest\t$mapcol\tROI\n";
    }
    if($doROIx) {
	++$mapcol;
	print OUTDATA "\troi_x";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Images+Region_of_Interest\t$mapcol\tx coordinate\n";
    }
    if($doROIy) {
	++$mapcol;
	print OUTDATA "\troi_y";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Images+Region_of_Interest\t$mapcol\ty coordinate\n";
    }
    if($doNegNorm) {
	++$mapcol;
	print OUTDATA "\tneg_norm";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Quality\t$mapcol\tNegative normalization factor\n";
    }
    if($doLowSig) {
	++$mapcol;
	print OUTDATA "\tlow_sig";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Quality\t$mapcol\tLow signal\n";
    }
    if($doArea) {
	++$mapcol;
	print OUTDATA "\tarea";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Properties\t$mapcol\tSurface area\n";
    }
    if($doTissueQuality) {
	++$mapcol;
	print OUTDATA "\ttiss_quality";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Properties\t$mapcol\tTissue quality\n";
    }
    if($doTissueStructure) {
	++$mapcol;
	print OUTDATA "\ttiss_struct";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Properties\t$mapcol\tTissue structure\n";
    }
    if($doTissueSubstructure) {
	++$mapcol;
	print OUTDATA "\ttiss_substruct";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Properties\t$mapcol\tTissue substructure\n";
    }
    if($doTissueNotes) {
	++$mapcol;
	print OUTDATA "\ttiss_notes";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Properties\t$mapcol\tTissue notes\n";
    }
    if($doTemperature) {
	++$mapcol;
	print OUTDATA "\ttemperature";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Properties\t$mapcol\tTemperature\n";
    }
    if($doSampleHVC) {
	++$mapcol;
	print OUTDATA "\tChimera_enrichment_method";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tChimera enrichment method\n";
    }
} else {			# add any possible clinical data with these variable names
    $doInfection = testUniqueBysubject(@clinicalInfection);
    $doInfectionStatus = testUniqueBysubject(@clinicalInfectionStatus);
    $doSampleCell = testUniqueBysubject(@clinicalCell);
    $doSampleDose = testUniqueBysubject(@sampleDose);
    $doSampleMolecule = testUniqueBysubject(@sampleMolecule);
    $doSampleType = testUniqueBysubject(@sampleType);
    $doSampleLibManu = testUniqueBysubject(@sampleLibManu);
    $doSampleLibSeq = testUniqueBysubject(@sampleLibSeq);
    $doSampleSortGate = testUniqueBysubject(@sampleSortGate);
    $doSamplePlateNum = testUniqueBysubject(@samplePlateNum);
    $doSamplePoolNum = testUniqueBysubject(@samplePoolNum);
    $doSampleSeqTech = testUniqueBysubject(@sampleSeqTech);
    $doSampleSeqBatch = testUniqueBysubject(@sampleSeqBatch);
    $doSampleLibType = testUniqueBysubject(@sampleLibType);
    $doSampleDevStage = testUniqueBysubject(@sampleDevStage);
    $doSampleMedia = testUniqueBysubject(@sampleMedia);
    $doSampleIsolation = testUniqueBysubject(@sampleIsolation);
    $doSampleDms = testUniqueBysubject(@sampleDms);
    $doSampleRnaPulldown = testUniqueBysubject(@sampleRnaPulldown);
    $doSampleSgrna = testUniqueBysubject(@sampleSgrna);
    $doSampleName = testUniqueBysubject(@sampleName);
    $doSampleLane = testUniqueBysubject(@sampleLane);
    $doRnaPop = testUniqueBysubject(@sampleRnaPop);
    $doBatch = testUniqueBysubject(@sampleBatch);
    $doViralRna = testUniqueBysubject(@sampleViralRna);
    $doISHStatus = testUnique(@sampleISHStatus);
    $doHybCode = testUniqueBysubject(@sampleHybCode);
    $doSegment = testUniqueBysubject(@sampleSegment);
    $doSegmentType = testUnique(@sampleSegmentType);
    $doROI = testUnique(@sampleROI);
    $doROIx = testUnique(@sampleROIx);
    $doROIy = testUnique(@sampleROIy);
    $doNegNorm = testUniqueBysubject(@sampleNegNorm);
    $doArea = testUniqueBysubject(@sampleArea);
    $doLowSig = testUniqueBysubject(@sampleLowSig);
    $doTissueQuality = testUnique(@sampleTissueQuality);
    $doTissueStructure = testUnique(@sampleTissueStructure);
    $doTissueSubstructure = testUnique(@sampleTissueSubstructure);
    $doTissueNotes = testUnique(@sampleTissueNotes);
    $doTemperature = testUnique(@sampleTemperature);
    $doSampleHVC = testUnique(@sampleHVC);

    if($doInfection) {
	++$mapcol;
	print OUTDATA "\tInfection";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis\t$mapcol\tInfection\n";
    }
    if($doInfectionStatus) {
	++$mapcol;
	print OUTDATA "\tInfection_status";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tDiagnosis\t$mapcol\tInfection status\n";
    }
    if($doSampleCell) {
	++$mapcol;
	print OUTDATA "\tCell";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tCell\n";
    }
    if($doSampleDose) {
	++$mapcol;
	print OUTDATA "\tDose";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tDose\n";
    }
    if($doSampleMolecule) {
	++$mapcol;
	print OUTDATA "\tMolecule";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tMolecule\n";
    }
    if($doSampleType) {
	++$mapcol;
	print OUTDATA "\tTreatment";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tTreatment\n";
    }
    if($doSampleLibManu) {
	++$mapcol;
	print OUTDATA "\tLib_manuscript";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Libraries\t$mapcol\tManuscript library\n";
    }
    if($doSampleLibSeq) {
	++$mapcol;
	print OUTDATA "\tLib_seq";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Libraries\t$mapcol\tSequencing library\n";
    }
    if($doSampleSortGate) {
	++$mapcol;
	print OUTDATA "\tSort_gate";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSort gate\n";
    }
    if($doSamplePlateNum) {
	++$mapcol;
	print OUTDATA "\tPlate_num";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tPlate number\n";
    }
    if($doSamplePoolNum) {
	++$mapcol;
	print OUTDATA "\tPool_num";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tPool number\n";
    }
    if($doSampleSeqTech) {
	++$mapcol;
	print OUTDATA "\tSeq_technology";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSequencing technology\n";
    }
    if($doSampleSeqBatch) {
	++$mapcol;
	print OUTDATA "\tSeq_batch";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSequencing batch\n";
    }
    if($doSampleLibType) {
	++$mapcol;
	print OUTDATA "\tLib_type";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tLibrary type\n";
    }
    if($doSampleDevStage) {
	++$mapcol;
	print OUTDATA "\tDev_stage";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tDevelopmental stage\n";
    }
    if($doSampleMedia) {
	++$mapcol;
	print OUTDATA "\tCulture_media";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tCulture media\n";
    }
    if($doSampleIsolation) {
	++$mapcol;
	print OUTDATA "\tIsolation_method";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tIsolation method\n";
    }
    if($doSamplePreparation) {
	++$mapcol;
	print OUTDATA "\tPreparation_method";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tPreparation method\n";
    }
    if($doSampleDms) {
	++$mapcol;
	print OUTDATA "\tDMS_treat";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tDMS treatment\n";
    }
    if($doSampleRnaPulldown) {
	++$mapcol;
	print OUTDATA "\tRNA_pulldown";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tRNA pulldown\n";
    }
    if($doSampleSgrna) {
	++$mapcol;
	print OUTDATA "\tsgRNA";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSingle guide RNA\n";
    }
    if($doSampleName) {
	++$mapcol;
	print OUTDATA "\tSample_name";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSample name\n";
    }
    if($doSampleLane) {
	++$mapcol;
	print OUTDATA "\tSample_lane";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tSample Lane\n";
    }
    if($doRnaPop) {
	++$mapcol;
	print OUTDATA "\tRNA_pop";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tRNA population\n";
    }
    if($doBatch) {
	++$mapcol;
	print OUTDATA "\tBatch";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tBatch\n";
    }
    if($doViralRna) {
	++$mapcol;
	print OUTDATA "\tViral_RNA";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tViral RNA\n";
    }
    if($doISHStatus) {
	++$mapcol;
	print OUTDATA "\tISH_status";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+In-situ Hybridization\t$mapcol\tISH status\n";
    }
    if($doHybCode) {
	++$mapcol;
	print OUTDATA "\tHyb_code";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+In-site Hybridization\t$mapcol\tHybridization code\n";
    }
    if($doSegment) {
	++$mapcol;
	print OUTDATA "\tSegment";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Segments\t$mapcol\tSegment\n";
    }
    if($doSegmentType) {
	++$mapcol;
	print OUTDATA "\tSeg_type";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Segments\t$mapcol\tSegment type\n";
    }
    if($doROI) {
	++$mapcol;
	print OUTDATA "\troi";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Images+Region_of_Interest\t$mapcol\tROI\n";
    }
    if($doROIx) {
	++$mapcol;
	print OUTDATA "\troi_x";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Images+Region_of_Interest\t$mapcol\tx coordinate\n";
    }
    if($doROIy) {
	++$mapcol;
	print OUTDATA "\troi_y";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Images+Region_of_Interest\t$mapcol\ty coordinate\n";
    }
    if($doNegNorm) {
	++$mapcol;
	print OUTDATA "\tneg_norm";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Quality\t$mapcol\tNegative normalization factor\n";
    }
    if($doLowSig) {
	++$mapcol;
	print OUTDATA "\tlow_sig";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Quality\t$mapcol\tLow signal\n";
    }
    if($doArea) {
	++$mapcol;
	print OUTDATA "\tarea";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Properties\t$mapcol\tSurface area\n";
    }
    if($doTissueQuality) {
	++$mapcol;
	print OUTDATA "\ttiss_quality";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Properties\t$mapcol\tTissue quality\n";
    }
    if($doTissueStructure) {
	++$mapcol;
	print OUTDATA "\ttiss_struct";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Properties\t$mapcol\tTissue structure\n";
    }
    if($doTissueSubstructure) {
	++$mapcol;
	print OUTDATA "\ttiss_substruct";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Properties\t$mapcol\tTissue substructure\n";
    }
    if($doTissueNotes) {
	++$mapcol;
	print OUTDATA "\ttiss_notes";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Properties\t$mapcol\tTissue notes\n";
    }
    if($doTemperature) {
	++$mapcol;
	print OUTDATA "\ttemperature";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples+Properties\t$mapcol\tTemperature\n";
    }
    if($doSampleHVC) {
	++$mapcol;
	print OUTDATA "\tChimera_enrichment_method";
	print MAPDATA "clinical_data$filenum{$filename}.txt\tSamples\t$mapcol\tChimera enrichment method\n";
    }
}

print OUTDATA "\n";


#################################################
# 8. Print out all rows for sample data
#    Print out the master rows for clinical data
##################################################

for ($i=0; $i <= $#sampleacc;$i++){
    $myTxtSample = $txtSample;
    $myTxtPlatform = $txtPlatform;
    # missing time values
    if($doTime && $sampleTime[$i] eq "") {$sampleTime[$i] = "Baseline";++$known{"sampleTime"}}

    # status may be for only some rows
    if($doStatus && !defined($clinicalStatus[$i])) {$clinicalStatus[$i] = ""}
    if($doCellType && !defined($sampleCellLine[$i])) {$sampleCellLine[$i] = ""}
    if($doCell && !defined($clinicalCell[$i])) {$clinicalCell[$i] = ""}
    if($doMolecule && defined($sampleMolecule[$i])) {
	if($sampleMolecule[$i] =~ /polyA/) {$myTxtPlatform = "+RNAseq"}
	elsif($sampleMolecule[$i] =~ /total/) {$myTxtPlatform = "+RNAseq"}
	elsif($sampleMolecule[$i] =~ /single-cell/) {$myTxtPlatform = "+scRNAseq"}
	else {$myTxtPlatform = "+ncRNAseq"}
    }
    if($doCell && defined($clinicalCell[$i])){
	$clinicalSource[$i] = $clinicalCell[$i];
	++$known{"clinicalSource"};
	if(defined($clinicalInfection[$i])) {
	    $sampleType[$i] = $clinicalInfection[$i];
	    ++$known{"sampleType"};
	    if(!$addSampleData) {$myTxtSample = "+ATTR1"}
	}
    } elsif ($sampleType[$i] eq "") {
	if(!$addSampleData) {$myTxtSample =~ s/[+]ATTR1//g}
    }

    if(defined($sampleAccession[$i])){
	print MAPEXPRESS "$studyId\_COVID\t\t$clinicalSubjectId[$i]\t$sampleAccession[$i]\t$samplePlatform[$i]";
	++$usedMap{"studyId"};
	++$usedMap{"clinicalSubjectId"};
	++$usedMap{"sampleAccession"};
	++$usedMap{"sampleId"};	# alternative to sampleAccession
	++$usedMap{"seriesSampleId"};	# alternative to sampleAccession
	++$usedMap{"samplePlatform"};
    } elsif(defined($seriesSampleId[$i])) {
	print MAPEXPRESS "$studyId\_COVID\t\t$clinicalSubjectId[$i]\t$seriesSampleId[$i]\t$samplePlatform[$i]";
	++$usedMap{"studyId"};
	++$usedMap{"clinicalSubjectId"};
	++$usedMap{"seriesSampleId"};
	++$usedMap{"sampleId"}; # alternative to seriesSampleId
	++$usedMap{"samplePlatform"};
    } else {
	print MAPEXPRESS "$studyId\_COVID\t\t$clinicalSubjectId[$i]\t$sampleId[$i]\t$samplePlatform[$i]";
	++$usedMap{"studyId"};
	++$usedMap{"clinicalSubjectId"};
	++$usedMap{"sampleId"};
	++$usedMap{"samplePlatform"};
    }
    if(defined($sampleTissue[$i])){
	print MAPEXPRESS "\t$sampleTissue[$i]"; ++$usedMap{"sampleTissue"};
    } else {
	print MAPEXPRESS "\t$clinicalSource[$i]"; ++$usedMap{"clinicalSource"};
    }
    print MAPEXPRESS "\t$sampleType[$i]\t$sampleTime[$i]";
    if($sampleType[$i] ne "") {++$usedMap{"sampleType"}}
    if($sampleTime[$i] ne "") {++$usedMap{"sampleTime"}}

    print MAPEXPRESS "\tBiomarker_Data$myTxtPlatform$txtTissue$myTxtSample$txtTime\tGEO\n";

    if(defined($sampleAccession[$i])){
	print OUTSAMPLES "SAMPLEID: $sampleAccession[$i]\n"; ++$usedSample{"sampleAccession"};
    } else {
	print OUTSAMPLES "SAMPLEID: $sampleId[$i]\n"; ++$usedSample{"sampleId"};
    }

    print OUTSAMPLES "TRIAL_NAME: $studyId\_COVID\n"; ++$usedSample{"studyId"};

    print OUTSAMPLES "SOURCE_ORGANISM: $sampleOrganism[$i]\n";
    ++$usedSample{"sampleOrganism"};
    ++$usedSample{"sampleCommonName"};
    ++$usedSample{"sampleTaxId"};

    if(defined($sampleTime[$i]) && $sampleTime[$i] ne ""){
	print OUTSAMPLES "TIMEPOINT: $sampleTime[$i]\n"; ++$usedSample{"sampleTime"};
    }
    print OUTSAMPLES "TISSUE_TYPE: $clinicalSource[$i]\n"; ++$usedSample{"clinicalSource"};

    print OUTSAMPLES "TITLE: $sampleTitle[$i]\n"; ++$usedSample{"sampleTitle"};

    if(defined($sampleDescription[$i])){
	print OUTSAMPLES "DESCRIPTION: $sampleDescription[$i]\n"; ++$usedSample{"sampleDescription"};
    }

    if(defined($sampleTreatmentProtocol[$i])){
	print OUTSAMPLES "treatment_protocol: $sampleTreatmentProtocol[$i]\n"; ++$usedSample{"sampleTreatmentProtocol"};
    }

    if(defined($sampleGrowthProtocol[$i])){
	print OUTSAMPLES "growth_protocol: $sampleGrowthProtocol[$i]\n"; ++$usedSample{"sampleGrowthProtocol"};
    }

    if(defined($sampleExtractProtocol[$i])){
	print OUTSAMPLES "extraction_protocol: $sampleExtractProtocol[$i]\n"; ++$usedSample{"sampleExtractProtocol"};
    }

    if(defined($sampleDataProcessing[$i])){
	print OUTSAMPLES "data_processing: $sampleDataProcessing[$i]\n"; ++$usedSample{"sampleDataProcessing"};
    }

    if(defined($sampleReplicate[$i])){
	print OUTSAMPLES "replicate: $sampleReplicate[$i]\n"; ++$usedSample{"sampleReplicate"};
    }

    if(defined($sampleSupFile[$i])){
	print OUTSAMPLES "supplementary_file: $sampleSupFile[$i]\n"; ++$usedSample{"sampleSupplementaryFile"};
    }

    print OUTSAMPLES "\n";

    if(!$fixSubjectId && defined($doneSubject{$clinicalSubjectId[$i]})){
	print ERR "...Repeat output for subject $clinicalSubjectId[$i]\n";
    }
    ++$doneSubject{$clinicalSubjectId[$i]};

    if($doAnalysisVisit || ($knownSubject{$clinicalSubjectId[$i]} == $i)) {

	print OUTDATA "$studyId\_COVID\t$clinicalSubjectId[$i]";
	++$usedData{"studyId"};
	++$usedData{"clinicalSubjectId"};

	if(defined($sampleAccession[$i])) {
	    print OUTDATA "\t$sampleAccession[$i]";
	    ++$usedData{"sampleAccession"};
	} else {
	    print OUTDATA "\t$sampleId[$i]";
	    ++$usedData{"sampleId"};
	}
	print OUTDATA "\t$demographicsAge[$i]\t$demographicsGender[$i]\t$demographicsEthnicity[$i]";
	if($demographicsAge[$i] ne "") {++$usedData{"demographicsAge"}}
	if($demographicsGender[$i] ne "") {++$usedData{"demographicsGender"}}
	if($demographicsEthnicity[$i] ne "") {++$usedData{"demographicsEthnicity"}}
	if($doRace) {print OUTDATA "\t$demographicsRace[$i]"; ++$usedData{"demographicsRace"}}
	if($doWeight) {print OUTDATA "\t$demographicsWeight[$i]"; ++$usedData{"demographicsWeight"}}
	if($doHeight) {print OUTDATA "\t$demographicsHeight[$i]"; ++$usedData{"demographicsHeight"}}
	if($doStudyGroup) {$val=getValue($clinicalStudyGroup[$i]);print OUTDATA "\t$val"; ++$usedData{"clinicalStudyGroup"}}
	if($doOrganism) {
	    if(defined($sampleCommonName[$i])) {
		$cn = $sampleCommonName[$i];
	    } elsif(defined($sampleTaxId[$i]) && defined($commonName{$sampleTaxId[$i]})) {
		$cn = $commonName{$sampleTaxId[$i]};
	    } else {
		$cn = $sampleOrganism[$i];
		foreach $o (keys(%species)) {
		    if($species{$o} eq $cn){
			$cn = $commonName{$o};
		    }
		}
	    }
	    print OUTDATA "\t$sampleOrganism[$i]\t$cn";
	    ++$usedData{"sampleOrganism"};
	    ++$usedData{"sampleTaxId"};
	    ++$usedData{"sampleCommonName"};
	}

	if($doStrain) {$val = getValue($clinicalStrain[$i]);print OUTDATA "\t$val"; ++$usedData{"clinicalStrain"}}
	if($doCirrhosis) {print OUTDATA "\t$clinicalCirrhosis[$i]"; ++$usedData{"clinicalCirrhosis"}}
	if($doStatus) {print OUTDATA "\t$clinicalStatus[$i]"; ++$usedData{"clinicalStatus"}}
	if($doNeutrophils) {print OUTDATA "\t$clinicalNeutrophils[$i]"; ++$usedData{"clinicalNeutrophils"}}
	if($doNeutrophilsStatus) {print OUTDATA "\t$labtestNeutrophilsStatus[$i]"; ++$usedData{"labtestNeutrophilsStatus"}}
	if($doCauseOfDeath) {print OUTDATA "\t$clinicalCauseOfDeath[$i]"; ++$usedData{"clinicalCauseOfDeath"}}
	if($doDeathTime) {print OUTDATA "\t$clinicalDeathTime[$i]"; ++$usedData{"clinicalDeathTime"}}
	if($doDisease) {print OUTDATA "\t$clinicalDisease[$i]"; ++$usedData{"clinicalDisease"}}
	if($doDiseaseState) {$val = getValue($clinicalDiseaseState[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalDiseaseState"}}
	if($doDiagAnamnestic) {print OUTDATA "\t$clinicalAnamnesticDays[$i]"; ++$usedData{"clinicalAnamnesticDays"}}
	if($doMedAntiviral) {print OUTDATA "\t$medicationAntiviral[$i]"; ++$usedData{"medicationAntiviral"}}
	if($doMedTreatment) {$val = getValue($clinicalTreatment[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalTreatment"}}
	if($doIfnTreatment) {$val = getValue($clinicalIfnTreatment[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalIfnTreatment"}}
	if($doBMI) {print OUTDATA "\t$clinicalBMI[$i]"; ++$usedData{"clinicalBMI"}}
	if($doDiagCopd) {print OUTDATA "\t$diagnosisCOPD[$i]"; ++$usedData{"diagnosisCOPD"}}
	if($doDiagCough) {print OUTDATA "\t$clinicalCough[$i]"; ++$usedData{"clinicalCough"}}
	if($doCVRisk) {print OUTDATA "\t$clinicalCVRisk[$i]"; ++$usedData{"clinicalCVRisk"}}
	if($doDadStage) {print OUTDATA "\t$diagnosisDadStage[$i]"; ++$usedData{"diagnosisDadStage"}}
	if($doDadHist) {print OUTDATA "\t$pathologyDad[$i]"; ++$usedData{"pathologyDad"}}
	if($doDiagDiabetes) {print OUTDATA "\t$diagnosisDiabetes[$i]"; ++$usedData{"diagnosisDiabetes"}}
	if($doDiagDyspnea) {print OUTDATA "\t$diagnosisDyspnea[$i]"; ++$usedData{"diagnosisDyspnea"}}
	if($doDiagLungHist) {print OUTDATA "\t$pathologyLung[$i]"; ++$usedData{"pathologyLung"}}
	if($doPathology) {print OUTDATA "\t$clinicalPathology[$i]"; ++$usedData{"clinicalPathology"}}
	if($doDiagFever) {print OUTDATA "\t$diagnosisFever[$i]"; ++$usedData{"diagnosisFever"}}
	if($doTimeHosp) {print OUTDATA "\t$clinicalHospitalisationTime[$i]"; ++$usedData{"clinicalHospitalisationTime"}}
	if($doDiagHypertension) {print OUTDATA "\t$diagnosisHypertension[$i]"; ++$usedData{"diagnosisHypertension"}}
	if($doDiagIAEdema) {print OUTDATA "\t$diagnosisIntraalveolarEdema[$i]"; ++$usedData{"diagnosisIntraalveolarEdema"}}
	if($doDiagIAHem) {print OUTDATA "\t$diagnosisIntraalveolarHemorrhage[$i]"; ++$usedData{"diagnosisIntraalveolarHemorrhage"}}
	if($doDiagTumorMal) {print OUTDATA "\t$diagnosisTumorMalignant[$i]"; ++$usedData{"diagnosisTumorMalignant"}}
	if($doMaterial) {print OUTDATA "\t$clinicalMaterial[$i]"; ++$usedData{"clinicalMaterial"}}
	if($doDiagLung) {print OUTDATA "\t$diagnosisPreexistingLung[$i]"; ++$usedData{"diagnosisPreexistingLung"}}
	if($doLabtestCovCount) {print OUTDATA "\t$labtestSarscov2Copies[$i]"; ++$usedData{"labtestSarscov2Copies"}}
	if($doSmoker) {print OUTDATA "\t$clinicalSmoker[$i]"; ++$usedData{"clinicalSmoker"}}
	if($doPMTime) {$val = getValue($clinicalPostmortemHours[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalPostmortemHours"}}
	if($doTopo) {print OUTDATA "\t$clinicalTopography[$i]"; ++$usedData{"clinicalTopography"}}
	if($doFlowcell) {print OUTDATA "\t$clinicalFlowcell[$i]"; ++$usedData{"clinicalFlowcell"}}
	if($doUsedInAnalysis) {$val = getValue($clinicalUsedInAnalysis[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalUsedInAnalysis"}}
	if($doAnalysisVisit) {$val = getValue($clinicalAnalysisVisit[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalAnalysisVisit"}}
	if($doModuleConstruct) {$val = getValue($clinicalModuleConstruct[$i]); print OUTDATA "\t$clinicalModuleConstruct[$i]"; ++$usedData{"clinicalModuleConstruct"}}
	if($doCVCoverage) {$val = getValue($clinicalCVCoverage[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalCVCoverage"}}
	if($doPctAligned) {$val = getValue($clinicalPctAligned[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalPctAligned"}}
	if($doReadAlign) {$val = getValue($clinicalAlignedReads[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalAlignedReads"}}
	if($doReadDedup) {$val = getValue($clinicalDedupReads[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalDedupReads"}}
	if($doReadRaw) {$val = getValue($clinicalRawReads[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalRawReads"}}
	if($doReadStitch) {$val = getValue($clinicalStitchedReads[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalStitchedReads"}}
	if($doReadTrim) {$val = getValue($clinicalTrimmedReads[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalTrimmedReads"}}
	if($doSeqSat) {$val = getValue($clinicalSeqSat[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalSeqSat"}}
	if($doViralType) {$val = getValue($clinicalViralType[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalViralType"}}
	if($doStatusOriginal) {$val = getValue($clinicalStatusOriginal[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalStatusOriginal"}}
	if($doStatusEvent) {$val = getValue($clinicalStatusEvent[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalStatusEvent"}}
	if($doStatusMatched) {$val = getValue($clinicalStatusMatched[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalStatusMatched"}}
	if($doHRVType) {$val = getValue($clinicalHRVType[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalHRVType"}}
	if($doCSteroidStart) {$val = getValue($clinicalCSteroidStart[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalCSteroidStart"}}
	if($doVirusTypeEVHRV) {$val = getValue($clinicalVirusTypeEVHRV[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypeEVHRV"}}
	if($doVirusTypeADV) {$val = getValue($clinicalVirusTypeADV[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypeADV"}}
	if($doVirusTypeBOCA) {$val = getValue($clinicalVirusTypeBOCA[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypeBOCA"}}
	if($doVirusTypeRSVA) {$val = getValue($clinicalVirusTypeRSVA[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypeRSVA"}}
	if($doVirusTypeRSVB) {$val = getValue($clinicalVirusTypeRSVB[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypeRSVB"}}
	if($doVirusTypeHKU1) {$val = getValue($clinicalVirusTypeHKU1[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypeHKU1"}}
	if($doVirusTypeCovNL63) {$val = getValue($clinicalVirusTypeCovNL63[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypeCovNL63"}}
	if($doVirusTypePIV1) {$val = getValue($clinicalVirusTypePIV1[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypePIV1"}}
	if($doVirusTypePIV2) {$val = getValue($clinicalVirusTypePIV2[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypePIV2"}}
	if($doVirusTypePIV3) {$val = getValue($clinicalVirusTypePIV3[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypePIV3"}}
	if($doVirusTypePIV4) {$val = getValue($clinicalVirusTypePIV4[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypePIV4"}}
	if($doVirusTypeMPV) {$val = getValue($clinicalVirusTypeMPV[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypeMPV"}}
	if($doVirusTypeFluB) {$val = getValue($clinicalVirusTypeFluB[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypeFluB"}}
	if($doVirusTypeCoV229e) {$val = getValue($clinicalVirusTypeCoV229e[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTypeCoV229e"}}
	if($doNasalPctNeutrophil) {$val = getValue($clinicalNasalPctNeutrophil[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalNasalPctNeutrophil"}}
	if($doNasalPctLymphocyte) {$val = getValue($clinicalNasalPctLymphocyte[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalNasalPctLymphocyte"}}
	if($doNasalPctEosinophil) {$val = getValue($clinicalNasalPctEosinophil[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalNasalPctEosinophil"}}
	if($doNasalPctMacrophage) {$val = getValue($clinicalNasalPctMacrophage[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalNasalPctMacrophage"}}
	if($doNasalPctWhiteBloodCell) {$val = getValue($clinicalNasalPctWhiteBloodCell[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalNasalPctWhiteBloodCell"}}
	if($doNasalPctEpithelial) {$val = getValue($clinicalNasalPctEpithelial[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalNasalPctEpithelial"}}
	if($doNasalPctSquamous) {$val = getValue($clinicalNasalPctSquamous[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalNasalPctSquamous"}}
	if($doNasalPctEpithelialSquamous) {$val = getValue($clinicalNasalPctEpithelialSquamous[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalNasalPctEpithelialSquamous"}}
	if($doBloodDiffBaso) {$val = getValue($clinicalBloodDiffBaso[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalBloodDiffBaso"}}
	if($doBloodDiffEosin) {$val = getValue($clinicalBloodDiffEosin[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalBloodDiffEosin"}}
	if($doBloodDiffLymph) {$val = getValue($clinicalBloodDiffLymph[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalBloodDiffLymph"}}
	if($doBloodDiffMono) {$val = getValue($clinicalBloodDiffMono[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalBloodDiffMono"}}
	if($doBloodDiffNeutro) {$val = getValue($clinicalBloodDiffNeutro[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalBloodDiffNeutro"}}
	if($doBloodCountErythro) {$val = getValue($clinicalBloodCountErythro[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalBloodCountErythro"}}
	if($doBloodCountPlatelet) {$val = getValue($clinicalBloodCountPlatelet[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalBloodCountPlatelet"}}
	if($doBloodCountWhite) {$val = getValue($clinicalBloodCountWhite[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalBloodCountWhite"}}
	if($doBloodCountNuclei) {$val = getValue($clinicalBloodCountNuclei[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalBloodCountNuclei"}}
	if($doLibCounts) {$val = getValue($clinicalLibCounts[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalLibCounts"}}
	if($doPosition) {$val = getValue($clinicalPosition[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalPosition"}}
	if($doAnimalNum) {$val = getValue($clinicalAnimalNum[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalAnimalNum"}}
	if($doCaseNum) {$val = getValue($clinicalCaseNum[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalCaseNum"}}
	if($doLesionNum) {$val = getValue($clinicalLesionNum[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalLesionNum"}}
	if($doIndivId) {$val = getValue($clinicalIndivId[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalIndivId"}}
	if($doViralLoad) {$val = getValue($clinicalViralLoad[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalViralLoad"}}
	if($doViralPositivity) {$val = getValue($clinicalViralPositivity[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalViralPositivity"}}
	if($doLocation) {$val = getValue($clinicalLocation[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalLocation"}}
	if($doDrug) {$val = getValue($medicationDrug[$i]); print OUTDATA "\t$val"; ++$usedData{"medicationDrug"}}
	if($doCov2) {$val = getValue($clinicalCov2Infected[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalCov2Infected"}}
	if($doCov2Pcr) {$val = getValue($clinicalCov2Pcr[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalCov2Pcr"}}
	if($doCov2Rpm) {$val = getValue($clinicalCov2Rpm[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalCov2Rpm"}}
	if($doDaysPos) {$val = getValue($clinicalDaysPos[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalDaysPos"}}
	if($doMouseModel) {$val = getValue($clinicalMouseModel[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalMouseModel"}}
	if($doCellLining) {$val = getValue($clinicalCellLining[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalCellLining"}}
	if($doDiagDM) {$val = getValue($diagDiabetes[$i]); print OUTDATA "\t$val"; ++$usedData{"diagDiabetes"}}
	if($doLabCrp) {$val = getValue($labsCrp[$i]); print OUTDATA "\t$val"; ++$usedData{"labsCrp"}}
	if($doLabDDimer) {$val = getValue($labsDDimer[$i]); print OUTDATA "\t$val"; ++$usedData{"labsDDimer"}}
	if($doLabFerritin) {$val = getValue($labsFerritin[$i]); print OUTDATA "\t$val"; ++$usedData{"labsFerritin"}}
	if($doLabFibrinogen) {$val = getValue($labsFibrinogen[$i]); print OUTDATA "\t$val"; ++$usedData{"labsFibrinogen"}}
	if($doLabLactate) {$val = getValue($labsLactate[$i]); print OUTDATA "\t$val"; ++$usedData{"labsLactate"}}
	if($doLabProcalcitonin) {$val = getValue($labsProcalcitonin[$i]); print OUTDATA "\t$val"; ++$usedData{"labsProcalcitonin"}}
	if($doLabIcuApacheii) {$val = getValue($labsIcuApacheii[$i]); print OUTDATA "\t$val"; ++$usedData{"labsIcuApacheii"}}
	if($doLabIcuSofa) {$val = getValue($labsIcuSofa[$i]); print OUTDATA "\t$val"; ++$usedData{"labsIcuSofa"}}
	if($doLabCharlson) {$val = getValue($labsCharlson[$i]); print OUTDATA "\t$val"; ++$usedData{"labsCharlson"}}
	if($doHospFree) {$val = getValue($clinicalHospFreeDays[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalHospFreeDays"}}
	if($doICU) {$val = getValue($clinicalICU[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalICU"}}
	if($doMechVent) {$val = getValue($clinicalMechVent[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalMechVent"}}
	if($doVentFree) {$val = getValue($clinicalVentFreeDays[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVentFreeDays"}}
	if($doMedSteroid) {$val = getValue($medicationSteroid[$i]); print OUTDATA "\t$val"; ++$usedData{"medicationSteroid"}}
	if($doViralDose) {$val = getValue($clinicalViralDose[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalViralDose"}}

	if($doSeverity) {print OUTDATA "\t$clinicalSeverity[$i]"; ++$usedData{"clinicalSeverity"}}
	if($doCellType) {print OUTDATA "\t$sampleCellLine[$i]"; ++$usedData{"sampleCellLine"}}
	if($doDonor) {print OUTDATA "\t$sampleDonor[$i]"; ++$usedData{"sampleDonor"}}
	if($doCell) {print OUTDATA "\t$clinicalCell[$i]"; ++$usedData{"clinicalCell"}}
	if($doW) {print OUTDATA "\t$clinicalW[$i]"; ++$usedData{"clinicalW"}}
	if($doOrigin) {print OUTDATA "\t$sampleOrigin[$i]"; ++$usedData{"sampleOrigin"}}
	if($doLy6e) {print OUTDATA "\t$sampleLy6e[$i]"; ++$usedData{"sampleLy6e"}}
	if($doTarget) {print OUTDATA "\t$clinicalTarget[$i]"; ++$usedData{"clinicalTarget"}}
	if($doVirusType) {$val = getValue($clinicalVirusType[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusType"}}
	if($doVirusStrain) {$val = getValue($clinicalVirusStrain[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusStrain"}}
	if($doVirusMOI) {$val = getValue($clinicalVirusMOI[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusMOI"}}
	if($doVirusTiter) {$val = getValue($clinicalVirusTiter[$i]); print OUTDATA "\t$val"; ++$usedData{"clinicalVirusTiter"}}
	if($doVirusAcc) {
	    if(defined($clinicalVirusAcc[$i])) {
		print OUTDATA "\t$clinicalVirusAcc[$i]";
		++$usedData{"clinicalVirusAcc"}
	    } else {
		print OUTDATA "\t";
	    }
	}
	if($doVirusTaxon) {
	    if(defined($clinicalVirusTaxon[$i])){
		print OUTDATA "\t$clinicalVirusTaxon[$i]";
		++$usedData{"clinicalVirusTaxon"};
		$vt = $clinicalVirusTaxon[$i];
		if(defined($species{$vt})){
		    print OUTDATA "\t$species{$vt}";
		} else {
		    print OUTDATA"\t";
		}
	    } else {
		print OUTDATA "\t\t";
	    }
	}
	if($doIfnA2b) {
	    if(defined($clinicalIfnA2b[$i])){
		print OUTDATA "\t$clinicalIfnA2b[$i]";
		++$usedData{"clinicalIfnA2b"};
	    } else {
		print OUTDATA "\t";
	    }
	}
	if($doIfnBeta) {
	    if(defined($clinicalIfnBeta[$i])){
		print OUTDATA "\t$clinicalIfnBeta[$i]";
		++$usedData{"clinicalIfnBeta"};
	    } else {
		print OUTDATA "\t";
	    }
	}
	if($doAntibody) {
	    if(defined($clinicalAntibody[$i])){
		print OUTDATA "\t$clinicalAntibody[$i]";
	    } else {
		print OUTDATA "\t";
	    }
	    ++$usedData{"clinicalAntibody"};
	}
	if($doTiterBase) {
	    if(defined($clinicalTiterBase[$i])){
		print OUTDATA "\t$clinicalTiterBase[$i]";
		++$usedData{"clinicalTiterBase"};
	    } else {
		print OUTDATA "\t";
	    }
	}
	if($doTiterSpring) {
	    if(defined($clinicalTiterSpring[$i])){
		print OUTDATA "\t$clinicalTiterSpring[$i]";
		++$usedData{"clinicalTiterSpring"};
	    } else {
		print OUTDATA "\t";
	    }
	}
	if($doSampleGenotype) {
	    if(defined($sampleGenotype[$i])){
		print OUTDATA "\t$sampleGenotype[$i]";
		++$usedData{"sampleGenotype"};
	    } else {
		print OUTDATA "\t";
	    }
	}

	if($addSampleData) {	# one sample per subject : add sample data for use in query building
	    if($doInfection) {
		print OUTDATA "\t$clinicalInfection[$i]";
		++$usedData{"clinicalInfection"};
	    }
	    if($doInfectionStatus) {
		print OUTDATA "\t$clinicalInfectionStatus[$i]";
		++$usedData{"clinicalInfectionStatus"};
	    }
	    if($doSampleSource) {
		print OUTDATA "\t$clinicalSource[$i]";
		++$usedData{"clinicalSource"};
	    }
	    if($doSampleTissue) {
		$val = getValue($sampleTissue[$i]);
		print OUTDATA "\t$val";
		++$usedData{"sampleTissue"};
	    }
	    if($doSampleCell) {
		print OUTDATA "\t$clinicalCell[$i]";
		++$usedData{"clinicalCell"};
	    }
	    if($doSampleCellLine) {
		print OUTDATA "\t$sampleCellLine[$i]";
		++$usedData{"sampleCellLine"};
	    }
	    if($doDonor) {
		print OUTDATA "\t$sampleDonor[$i]";
		++$usedData{"sampleDonor"};
	    }
	    if($doSampleInfection) {
		print OUTDATA "\t$clinicalInfection[$i]";
		++$usedData{"clinicalInfection"};
	    }
	    if($doSampleMolecule) {
		$val = getValue($sampleMolecule[$i]);
		print OUTDATA "\t$val";
		++$usedData{"sampleMolecule"};
	    }
	    if($doSampleDose) {
		$val = getValue($sampleDose[$i]);
		print OUTDATA "\t$val";
		++$usedData{"sampleDose"};
	    }
	    if($doSampleType) {
		print OUTDATA "\t$sampleType[$i]";
		++$usedData{"sampleType"};
	    }
	    if($doSampleTime) {
		print OUTDATA "\t$sampleTime[$i]";
		++$usedData{"sampleTime"};
	    }
	    if($doSampleProvider) {
		print OUTDATA "\t$sampleProvider[$i]";
		++$usedData{"sampleProvider"};
	    }
	    if($doSampleProviderId) {
		print OUTDATA "\t$sampleProviderId[$i]";
		++$usedData{"sampleProviderId"};
	    }
	    if($doSampleConfirmedContent) {
		print OUTDATA "\t$sampleConfirmedContent[$i]";
		++$usedData{"sampleConfirmedContent"};
	    }
	    if($doSampleAmplification) {
		print OUTDATA "\t$sampleAmplification[$i]";
		++$usedData{"sampleAmplification"};
	    }
	    if($doSampleLayoutVersion) {
		print OUTDATA "\t$sampleLayoutVersion[$i]";
		++$usedData{"sampleLayoutVersion"};
	    }
	    if($doSampleLayoutVersion) {
		print OUTDATA "\t$sampleLayoutVersion[$i]";
		++$usedData{"sampleLayoutVersion"};
	    }
	    if($doSampleLibManu) {
		print OUTDATA "\t$sampleLibManu[$i]";
		++$usedData{"sampleLibManu"};
	    }
	    if($doSampleLibSeq) {
		print OUTDATA "\t$sampleLibSeq[$i]";
		++$usedData{"sampleLibSeq"};
	    }
	    if($doSampleSortGate) {
		print OUTDATA "\t$sampleSortGate[$i]";
		++$usedData{"sampleSortGate"};
	    }
	    if($doSamplePlateNum) {
		print OUTDATA "\t$samplePlateNum[$i]";
		++$usedData{"samplePlateNum"};
	    }
	    if($doSamplePoolNum) {
		$val = getValue($samplePoolNum[$i]);
		print OUTDATA "\t$val";
		++$usedData{"samplePoolNum"};
	    }
	    if($doSampleSeqTech) {
		$val = getValue($sampleSeqTech[$i]);
		print OUTDATA "\t$val";
		++$usedData{"sampleSeqTech"};
	    }
	    if($doSampleSeqBatch) {
		$val = getValue($sampleSeqBatch[$i]);
		print OUTDATA "\t$val";
		++$usedData{"sampleSeqBatch"};
	    }
	    if($doSampleLibType) {
		print OUTDATA "\t$sampleLibType[$i]";
		++$usedData{"sampleLibType"};
	    }
	    if($doSampleDevStage) {
		print OUTDATA "\t$sampleDevStage[$i]";
		++$usedData{"sampleDevStage"};
	    }
	    if($doSampleMedia) {
		print OUTDATA "\t$sampleMedia[$i]";
		++$usedData{"sampleMedia"};
	    }
	    if($doSampleIsolation) {
		print OUTDATA "\t$sampleIsolation[$i]";
		++$usedData{"sampleIsolation"};
	    }
	    if($doSamplePreparation) {
		print OUTDATA "\t$samplePreparation[$i]";
		++$usedData{"samplePreparation"};
	    }
	    if($doSamplePassage) {
		print OUTDATA "\t$samplePassage[$i]";
		++$usedData{"samplePassage"};
	    }
	    if($doSampleDms) {
		print OUTDATA "\t$sampleDms[$i]";
		++$usedData{"sampleDms"};
	    }
	    if($doSampleRnaPulldown) {
		print OUTDATA "\t$sampleRnaPulldown[$i]";
		++$usedData{"sampleRnaPulldown"};
	    }
	    if($doSampleSgrna) {
		if(defined($sampleSgrna[$i])) {
		    print OUTDATA "\t$sampleSgrna[$i]";
		} else {
		    print OUTDATA "\t";
		}
		++$usedData{"sampleSgrna"};
	    }
	    if($doSampleName) {
		print OUTDATA "\t$sampleName[$i]";
		++$usedData{"sampleName"};
	    }
	    if($doSampleLane) {
		print OUTDATA "\t$sampleLane[$i]";
		++$usedData{"sampleLane"};
	    }
	    if($doRnaPop) {
		print OUTDATA "\t$sampleRnaPop[$i]";
		++$usedData{"sampleRnaPop"};
	    }
	    if($doBatch) {
		print OUTDATA "\t$sampleBatch[$i]";
		++$usedData{"sampleBatch"};
	    }
	    if($doViralRna) {
		print OUTDATA "\t$sampleViralRna[$i]";
		++$usedData{"sampleViralRna"};
	    }
	    if($doISHStatus) {
		print OUTDATA "\t$sampleISHStatus[$i]";
		++$usedData{"sampleISHStatus"};
	    }
	    if($doHybCode) {
		print OUTDATA "\t$sampleHybCode[$i]";
		++$usedData{"sampleHybCode"};
	    }
	    if($doSegment) {
		print OUTDATA "\t$sampleSegment[$i]";
		++$usedData{"sampleSegment"};
	    }
	    if($doSegmentType) {
		print OUTDATA "\t$sampleSegmentType[$i]";
		++$usedData{"sampleSegmentType"};
	    }
	    if($doROI) {
		print OUTDATA "\t$sampleROI[$i]";
		++$usedData{"sampleROI"};
	    }
	    if($doROIx) {
		print OUTDATA "\t$sampleROIx[$i]";
		++$usedData{"sampleROIx"};
	    }
	    if($doROIy) {
		print OUTDATA "\t$sampleROIy[$i]";
		++$usedData{"sampleROIy"};
	    }
	    if($doNegNorm) {
		print OUTDATA "\t$sampleNegNorm[$i]";
		++$usedData{"sampleNegNorm"};
	    }
	    if($doArea) {
		print OUTDATA "\t$sampleArea[$i]";
		++$usedData{"sampleArea"};
	    }
	    if($doLowSig) {
		print OUTDATA "\t$sampleLowSig[$i]";
		++$usedData{"sampleLowSig"};
	    }
	    if($doTissueQuality) {
		print OUTDATA "\t$sampleTissueQuality[$i]";
		++$usedData{"sampleTissueQuality"};
	    }
	    if($doTissueStructure) {
		print OUTDATA "\t$sampleTissueStructure[$i]";
		++$usedData{"sampleTissueStructure"};
	    }
	    if($doTissueSubstructure) {
		print OUTDATA "\t$sampleTissueSubstructure[$i]";
		++$usedData{"sampleTissueSubstructure"};
	    }
	    if($doTissueNotes) {
		$val = getValue($sampleTissueNotes[$i]);
		print OUTDATA "\t$val";
		++$usedData{"sampleTissueNotes"};
	    }
	    if($doTemperature) {
		$val = getValue($sampleTemperature[$i]);
		print OUTDATA "\t$val";
		++$usedData{"sampleTemperature"};
	    }
	    if($doSampleHVC) {
		print OUTDATA "\t$sampleHVC[$i]";
		++$usedData{"sampleHVC"};
	    }
	} else {	
	    if($doInfection) {
		print OUTDATA "\t$clinicalInfection[$i]";
		++$usedData{"clinicalInfection"};
	    }
	    if($doInfectionStatus) {
		print OUTDATA "\t$clinicalInfectionStatus[$i]";
		++$usedData{"clinicalInfectionStatus"};
	    }
	    if($doSampleCell) {
		print OUTDATA "\t$clinicalCell[$i]";
		++$usedData{"clinicalCell"};
	    }
	    if($doSampleDose) {
		$val = getValue($sampleDose[$i]);
		print OUTDATA "\t$val";
		++$usedData{"sampleDose"};
	    }
	    if($doSampleMolecule) {
		print OUTDATA "\t$sampleMolecule[$i]";
		++$usedData{"sampleMolecule"};
	    }
	    if($doSampleType) {
		print OUTDATA "\t$sampleType[$i]";
		++$usedData{"sampleType"};
	    }
	    if($doSampleLibManu) {
		print OUTDATA "\t$sampleLibManu[$i]";
		++$usedData{"sampleLibManu"};
	    }
	    if($doSampleLibSeq) {
		print OUTDATA "\t$sampleLibSeq[$i]";
		++$usedData{"sampleLibSeq"};
	    }
	    if($doSampleSortGate) {
		print OUTDATA "\t$sampleSortGate[$i]";
		++$usedData{"sampleSortGate"};
	    }
	    if($doSamplePlateNum) {
		print OUTDATA "\t$samplePlateNum[$i]";
		++$usedData{"samplePlateNum"};
	    }
	    if($doSamplePoolNum) {
		$val = getValue($samplePoolNum[$i]);
		print OUTDATA "\t$val";
		++$usedData{"samplePoolNum"};
	    }
	    if($doSampleSeqTech) {
		$val = getValue($sampleSeqTech[$i]);
		print OUTDATA "\t$val";
		++$usedData{"sampleSeqTech"};
	    }
	    if($doSampleSeqBatch) {
		$val = getValue($sampleSeqBatch[$i]);
		print OUTDATA "\t$val";
		++$usedData{"sampleSeqBatch"};
	    }
	    if($doSampleLibType) {
		print OUTDATA "\t$sampleLibType[$i]";
		++$usedData{"sampleLibType"};
	    }
	    if($doSampleDevStage) {
		print OUTDATA "\t$sampleDevStage[$i]";
		++$usedData{"sampleDevStage"};
	    }
	    if($doSampleMedia) {
		print OUTDATA "\t$sampleMedia[$i]";
		++$usedData{"sampleMedia"};
	    }
	    if($doSampleIsolation) {
		print OUTDATA "\t$sampleIsolation[$i]";
		++$usedData{"sampleIsolation"};
	    }
	    if($doSamplePreparation) {
		print OUTDATA "\t$samplePreparation[$i]";
		++$usedData{"samplePreparation"};
	    }
	    if($doSampleDms) {
		print OUTDATA "\t$sampleDms[$i]";
		++$usedData{"sampleDms"};
	    }
	    if($doSampleRnaPulldown) {
		print OUTDATA "\t$sampleRnaPulldown[$i]";
		++$usedData{"sampleRnaPulldown"};
	    }
	    if($doSampleSgrna) {
		if(defined($sampleSgrna[$i])) {
		    print OUTDATA "\t$sampleSgrna[$i]";
		} else {
		    print OUTDATA "\t";
		}
		++$usedData{"sampleSgrna"};
	    }
	    if($doSampleName) {
		print OUTDATA "\t$sampleName[$i]";
		++$usedData{"sampleName"};
	    }
	    if($doSampleLane) {
		print OUTDATA "\t$sampleLane[$i]";
		++$usedData{"sampleLane"};
	    }
	    if($doRnaPop) {
		print OUTDATA "\t$sampleRnaPop[$i]";
		++$usedData{"sampleRnaPop"};
	    }
	    if($doBatch) {
		print OUTDATA "\t$sampleBatch[$i]";
		++$usedData{"sampleBatch"};
	    }
	    if($doViralRna) {
		print OUTDATA "\t$sampleViralRna[$i]";
		++$usedData{"sampleViralRna"};
	    }
	    if($doISHStatus) {
		print OUTDATA "\t$sampleISHStatus[$i]";
		++$usedData{"sampleISHStatus"};
	    }
	    if($doHybCode) {
		print OUTDATA "\t$sampleHybCode[$i]";
		++$usedData{"sampleHybCode"};
	    }
	    if($doSegment) {
		print OUTDATA "\t$sampleSegment[$i]";
		++$usedData{"sampleSegment"};
	    }
	    if($doSegmentType) {
		print OUTDATA "\t$sampleSegmentType[$i]";
		++$usedData{"sampleSegmentType"};
	    }
	    if($doROI) {
		print OUTDATA "\t$sampleROI[$i]";
		++$usedData{"sampleROI"};
	    }
	    if($doROIx) {
		print OUTDATA "\t$sampleROIx[$i]";
		++$usedData{"sampleROIx"};
	    }
	    if($doROIy) {
		print OUTDATA "\t$sampleROIy[$i]";
		++$usedData{"sampleROIy"};
	    }
	    if($doNegNorm) {
		print OUTDATA "\t$sampleNegNorm[$i]";
		++$usedData{"sampleNegNorm"};
	    }
	    if($doArea) {
		print OUTDATA "\t$sampleArea[$i]";
		++$usedData{"sampleArea"};
	    }
	    if($doLowSig) {
		print OUTDATA "\t$sampleLowSig[$i]";
		++$usedData{"sampleLowSig"};
	    }
	    if($doTissueQuality) {
		print OUTDATA "\t$sampleTissueQuality[$i]";
		++$usedData{"sampleTissueQuality"};
	    }
	    if($doTissueStructure) {
		print OUTDATA "\t$sampleTissueStructure[$i]";
		++$usedData{"sampleTissueStructure"};
	    }
	    if($doTissueSubstructure) {
		print OUTDATA "\t$sampleTissueSubstructure[$i]";
		++$usedData{"sampleTissueSubstructure"};
	    }
	    if($doTissueNotes) {
		$val = getValue($sampleTissueNotes[$i]);
		print OUTDATA "\t$val";
		++$usedData{"sampleTissueNotes"};
	    }
	    if($doTemperature) {
		$val = getValue($sampleTemperature[$i]);
		print OUTDATA "\t$val";
		++$usedData{"sampleTemperature"};
	    }
	    if($doSampleHVC) {
		print OUTDATA "\t$sampleHVC[$i]";
		++$usedData{"sampleHVC"};
	    }
	}

	print OUTDATA "\n";
    }
}

print OUTBROWSE "Program: $studyTopnode\n";

print OUTBROWSE "Accession: $studyId\_COVID\n";

print OUTBROWSE "Title: $studyAuthor($studyYear) $studyId\n";

$bval = getBrowse($studyTitle);
print OUTBROWSE "Description: $bval\n";
if($bval ne ".") {++$usedBrowse{"studyTitle"}}

$bval = getBrowse($studySummary);
print OUTBROWSE "Description: $bval\n";
if($bval ne ".") {++$usedBrowse{"studySummary"}}

$bval = getBrowse($studyOverallDesign);
print OUTBROWSE "Overalldesign: $bval\n";
if($bval ne ".") {++$usedBrowse{"studyOverallDesign"}}

print OUTBROWSE "Target: Not applicable\n";

print OUTBROWSE "Etlid: $studyId\_COVID\n";


$bval = getBrowse($studyTopnode);
if($bval eq "SARS") {$bval = "Severe Acute Respiratory Syndrome"}
if($bval eq "MERS") {$bval = "Coronavirus Infections"} # includes MERS as a synonym
print OUTBROWSE "Pathology: $bval\n"; # set according to program

print OUTBROWSE "Phase: Not applicable\n"; # STUDY_PHASE

print OUTBROWSE "Objective: Not applicable\n"; # STUDY_OBJECTIVE

print OUTBROWSE "Design: Observational\n"; # set by time, treatment as "Observational/Interventional" + " longitudinal" STUDY_DESIGN

print OUTBROWSE "Biomarker: Not applicable\n"; # STUDY_BIOMARKER

print OUTBROWSE "Link: https://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=$studyId\n"; # GEO URL

print OUTBROWSE "Subjects: $subjectCount\n";

print OUTBROWSE "Samples: $rowCount\n";

$bval = getBrowse($studyOrganism);
if($bval eq ".") {$bval = "Other species"}
foreach $o (split(/,/,$bval)){
    print OUTBROWSE "Organism: $o\n";
}
if($bval ne ".") {++$usedBrowse{"studyOrganism"}}

print OUTBROWSE "Access: Public\n";

$bval = getBrowse($studyContact{"country"});
if($bval eq ".") {$bval = "Not applicable"}
foreach $c (split(/,/,$bval)){
    $uc = uc($c);
    if($uc eq "USA"){$uc = "UNITED STATES"}
    if($uc eq "UK"){$uc = "UNITED KINGDOM"}
    if($uc eq "SOUTH KOREA"){$uc = "KOREA REPUBLIC OF"}
    print OUTBROWSE "Country: $uc\n";
}
if($bval ne ".") {++$usedBrowse{"studyContactcountry"}}

$bval = getBrowse($studySubmissionDate);
$date = getDate($bval);
print OUTBROWSE "Startdate: $date\n"; # earliest date we can find
if($bval ne ".") {++$usedBrowse{"studySubmissionDate"}}

print OUTBROWSE "Completedate: $date\n";

$pid = getBrowse($studyPubmedId);
print OUTBROWSE "Pubmed: $pid\n";


if($pid ne ".") {
    ++$usedBrowse{"studyPubmedId"};
    # fetch pubmed data
    # compare to any other information in the GEO record
    if(! (-s "pubmed.txt") ) {
	open (TMP, ">pubmed.txt") || die "Cannot write pubmed.txt";
	open (CURL,"curl --silent https://pubmed.ncbi.nlm.nih.gov/$pid/|") || die "Cannot connect to pubmed central";
	while(<CURL>){
	    print TMP;
	}
	close CURL;
	close TMP;
    }
    open (PUB, "pubmed.txt") || die "Cannot read pubmed.txt";
    $pubtext = "";
    while(<PUB>) {
	$pubtext .= $_;
    }
    close PUB;
    $pubTitle = $pubAuthors = $pubDate = $pubJournal = $pubDoi = $pubCitation = ".";
    if($pubtext =~ /<meta name=\"citation_title\" content=\"([^\"]+)\">/) {
	$pubTitle = $1;
	++$known{"pubTitle"};
#	print STDERR "Title: '$1'\n";
    }
    if($pubtext =~ /<meta name=\"citation_authors\" content=\"([^\"]+)\">/) {
	$pubAuthors = fixChars($1);
	$pubAuthors =~ s/;/; /g;
	++$known{"pubAuthors"};
#	print STDERR "Authors: '$1'\n";
    }
    if($pubtext =~ /<meta name=\"citation_publisher\" content=\"([^\"]+)\">/) {
	$pubJournal = $1;
	$pubCitation = $pubJournal;
	++$known{"pubCitation"};
#	print STDERR "Publisher: '$1'\n";
    }
    if($pubtext =~ /<meta name=\"citation_date\" content=\"([^\"]+)\">/) {
	$pubDate = $1;
#	print STDERR "Date: '$1'\n";
    }
    if($pubtext =~ /<span class=\"cit\">(.*?)<\/span>/sog) {
	$pubCit = $1;
	$pubCit =~ s/[.]$//g;
	$pubCitation .= " $pubCit";
	++$known{"pubCitation"};
	print STDERR "Cit: '$pubCitation'\n";
    }
    if($pubtext =~ /<meta name=\"citation_journal_title\" content=\"([^\"]+)\">/) {
#	print STDERR "JournalTitle: '$1'\n";
    }
    if($pubtext =~ /<meta name=\"citation_pmid\" content=\"([^\"]+)\">/) {
#	print STDERR "PMID: '$1'\n";
    }
    if($pubtext =~ /<meta name=\"citation_abstract_html_url\" content=\"([^\"]+)\">/) {
#	print STDERR "AbstractHtmlUrl: '$1'\n";
    }
    if($pubtext =~ /<meta name=\"citation_volume\" content=\"([^\"]+)\">/) {
#	print STDERR "Volume: '$1'\n";
    }
    if($pubtext =~ /<meta name=\"citation_issue\" content=\"([^\"]+)\">/) {
#	print STDERR "Issue: '$1'\n";
    }
    if($pubtext =~ /<meta name=\"citation_doi\" content=\"([^\"]+)\">/) {
	$pubDoi = $1;
	++$known{"pubDoi"};
#	print STDERR "DOI: '$1'\n";
    }
    if($pubtext =~ /<meta name=\"citation_issn\" content=\"([^\"]+)\">/) {
#	print STDERR "ISSN: '$1'\n";
    }
    if($pubtext =~ /<h1 class=\"heading-title\">(.*?)<\/h1>/sog) {
#	$txt = trimSpace($1);
#	print STDERR "HeadTitle: '$txt'\n";
    }
}

$doival = getBrowse($pubDoi);
print OUTBROWSE "Doi: $doival\n";
if($doival ne ".") {++$usedBrowse{"pubDoi"}}

$citval = getBrowse($pubCitation);
print OUTBROWSE "Citation: $citval\n";
if($citval ne ".") {++$usedBrowse{"pubCitation"}}

$bval = getBrowse($pubAuthors);
print OUTBROWSE "Authors: $bval\n";
if($bval ne ".") {++$usedBrowse{"pubAuthors"}}

$bval = getBrowse($pubTitle);
print OUTBROWSE "Pubtitle: $bval\n";
if($bval ne ".") {++$usedBrowse{"pubTitle"}}

if($citval =~ /[a-zA-Z]/ || $doival ne "." || $pid ne ".") {
    print OUTBROWSE "Status: Published\n";
} elsif($citval =~ /r[Xx]iv/) {
    print OUTBROWSE "Status: Submitted\n";
} else {
    print OUTBROWSE "Status: Not applicable\n";
}
++$usedBrowse{"studyStatus"};

$bval = getBrowse($contribName);
$cval = getBrowse($contactName);
if($bval ne ".") {
    $role = "Principal investigator";
    if($cval eq $bval) {$role .= ", Study contact"}
    $bval =~ s/,,/,/g;
    $bval =~ s/,/ /g;
    print OUTBROWSE "Name: $bval\n";
} else {
    $cval =~ s/,,/,/g;
    $cval =~ s/,/ /g;
    print OUTBROWSE "Name: $cval\n";
    $role = "Study contact";
}
if($bval ne ".") {++$usedBrowse{"studyAuthor"}}

print OUTBROWSE "Roles: $role\n";

$bval = getBrowse($studyContact{"name"});
$bval =~s/,,/,/g;
$bval =~s/,/ /g;
print OUTBROWSE "Contact: $bval\n";
if($bval ne ".") {++$usedBrowse{"studyContactname"}}

$ival = getBrowse($studyContact{"institute"});
$lval = getBrowse($studyContact{"laboratory"});
$dval = getBrowse($studyContact{"department"});
$inst = "";
if($ival ne ".") {$inst = "$ival, ";++$usedBrowse{"studyContactinstitute"}}
if($dval ne ".") {$inst .= "$dval, ";++$usedBrowse{"studyContactdepartment"}}
if($lval ne ".") {$inst .= "$lval";++$usedBrowse{"studyContactlaboratory"}}
else {$inst =~ s/[, ]+$//g}
print OUTBROWSE "Institution: $inst\n";

$addr = "";
$email = getBrowse($studyContact{"email"});
if($email ne ".") {$addr .= "email: $email ";++$usedBrowse{"studyContactemail"}}
$phone = getBrowse($studyContact{"phone"});
if($phone ne ".") {$addr .= "phone: $phone ";++$usedBrowse{"studyContactphone"}}
$address = getBrowse($studyContact{"address"});
if($address ne ".") {$addr .= "$address, ";++$usedBrowse{"studyContactaddress"}}
$city = getBrowse($studyContact{"city"});
if($city ne ".") {$addr .= "$city, ";++$usedBrowse{"studyContactcity"}}
$state = getBrowse($studyContact{"state"});
if($state ne ".") {$addr .= "$state ";++$usedBrowse{"studyContactstate"}}
$zip = getBrowse($studyContact{"zip/postal_code"});
if($zip ne ".") {$addr .= "$zip, ";++$usedBrowse{"studyContactzip/postal_code"}}
$country = getBrowse($studyContact{"country"});
if($country ne ".") {$addr .= "$country";++$usedBrowse{"studyContactcountry"}}
else {$addr =~ s/[, ]+$/g/}
print OUTBROWSE "Address: $addr\n";

$bval = getBrowse($seriesContributors);
if($bval ne "."){
    $bval =~ s/,,/,/g;
    $bval =~ s/,/ /g;
    $bval =~ s/;/; /g;
    print OUTBROWSE "#Contributor: $bval\n";
    ++$usedBrowse{"seriesContributors"};
}

if($seriesRelation ne ""){
    print OUTBROWSE "#Relation: $seriesRelation\n";
    ++$usedBrowse{"seriesRelation"};
}

if(defined($seriesSupFile)){
    print OUTBROWSE "#SupplementaryFile: $seriesSupFile\n";
    ++$usedBrowse{"seriesSupFile"};
}

if(defined($studyWebLink)){
    print OUTBROWSE "#Web_link: $studyWebLink\n";
    ++$usedBrowse{"studyWebLink"};
}

$bval = getBrowse($studyLastUpdateDate);
print OUTBROWSE "#UpdateDate: $studyLastUpdateDate\n";
if($bval ne ".") {++$usedBrowse{"studyLastUpdateDate"}}



if(defined($dataTypeNames{$studyDataType})){
    print OUTBROWSE "#DataType: $dataTypeNames{$studyDataType}\n";
    ++$usedBrowse{"studyDataType"};
} else {
    print OUTBROWSE "#DataType: $studyDataType\n";
    ++$usedBrowse{"studyDataType"};
}

close OUTSAMPLES;
close OUTBROWSE;

close MAPDATA;

close OUTDATA;

close OUTEXPRESS;

close MAPEXPRESS;

open(PARAMSDATA, ">clinical.params$filenum{$filename}.out") || die "Cannot open clinical params file";

print PARAMSDATA "## Mandatory\n";
print PARAMSDATA "COLUMN_MAP_FILE=\"clinical_mapping.txt\"\n";
print PARAMSDATA "WORD_MAP_FILE=x\n";
print PARAMSDATA "RECORD_EXCLUSION_FILE=x\n";
print PARAMSDATA "\n";
print PARAMSDATA "# Optional\n";
print PARAMSDATA "STUDY_ID=$studyId\_COVID\n";
print PARAMSDATA "STUDY_NAME=\"$studyAuthor($studyYear) $studyId\"\n";
print PARAMSDATA "SECURITY_REQUIRED=N\n";
print PARAMSDATA "TOP_NODE_PREFIX=\"$studyTopnode\"\n";

close PARAMSDATA;

print BROWSEPARAMS "STUDY_ID=$studyId\_COVID\n";
print BROWSEPARAMS "STUDY_PROGRAM=$studyTopnode\n";
close BROWSEPARAMS;

print SAMPLESPARAMS "STUDY_ID=$studyId\_COVID\n";
close SAMPLESPARAMS;



if($mapPrefix eq "rnaseq") {
    open(PARAMSEXPRESS, ">$mapPrefix.params$filenum{$filename}.out") || die "Cannot open $mapPrefix params file";
    print PARAMSEXPRESS "## Mandatory\n";
    print PARAMSEXPRESS "DATA_FILE_PREFIX=\"$mapPrefix\_data_\"\n";
    print PARAMSEXPRESS "SUBJECT_SAMPLE_MAPPING=\"$mapPrefix\_sample_subject_mapping.txt\"\n";
    print PARAMSEXPRESS "SAMPLE_MAP_FILENAME=\"$mapPrefix\_sample_subject_mapping.txt\"\n";
    if($mapPrefix eq "rnaseq") {
	print PARAMSEXPRESS "RNASEQ_TYPE=RNASEQ\n";
    }
    print PARAMSEXPRESS "\n";
    print PARAMSEXPRESS "DATA_TYPE=R\n";
    print PARAMSEXPRESS "LOG_BASE=2\n";
    print PARAMSEXPRESS "STUDY_ID=$studyId\_COVID\n";
    print PARAMSEXPRESS "STUDY_NAME=\"$studyAuthor($studyYear) $studyId\"\n";
    print PARAMSEXPRESS "SECURITY_REQUIRED=N\n";
    print PARAMSEXPRESS "TOP_NODE_PREFIX=\"$studyTopnode\"\n";
    print PARAMSEXPRESS "\n";
    print PARAMSEXPRESS "# Optional\n";
    print PARAMSEXPRESS "SOURCE_CD=\"GEO\"\n";
    print PARAMSEXPRESS "\n";
    print PARAMSEXPRESS "# For information only\n";
    
    close PARAMSEXPRESS;
} elsif($mapPrefix eq "expression") {
    open(PARAMSEXPRESS, ">$mapPrefix.params$filenum{$filename}.out") || die "Cannot open $mapPrefix params file";
    print PARAMSEXPRESS "## Mandatory\n";
    print PARAMSEXPRESS "DATA_FILE_PREFIX=\"ExpRawData-counts\"\n";
    print PARAMSEXPRESS "MAP_FILENAME=\"$mapPrefix\_sample_subject_mapping.txt\"\n";
    print PARAMSEXPRESS "\n";
    print PARAMSEXPRESS "DATA_TYPE=R\n";
    print PARAMSEXPRESS "LOG_BASE=2\n";
    print PARAMSEXPRESS "SECURITY_REQUIRED=N\n";
    print PARAMSEXPRESS "\n";
    print PARAMSEXPRESS "STUDY_ID=$studyId\_COVID\n";
    print PARAMSEXPRESS "STUDY_NAME=\"$studyAuthor($studyYear) $studyId\"\n";
    print PARAMSEXPRESS "TOP_NODE_PREFIX=\"$studyTopnode\"\n";
    print PARAMSEXPRESS "\n";
    print PARAMSEXPRESS "# Optional\n";
    print PARAMSEXPRESS "SOURCE_CD=\"GEO\"\n";
    print PARAMSEXPRESS "\n";
    print PARAMSEXPRESS "# For information only\n";
    
    close PARAMSEXPRESS;
}
else {
    print ERR "+++UNKNOWN datatype $studyDataType for params file generation\n";
    open(PARAMSEXPRESS, ">$mapPrefix.params$filenum{$filename}.out") || die "Cannot open $mapPrefix params file";
    print PARAMSEXPRESS "## Mandatory\n";
    print PARAMSEXPRESS "DATA_FILE_PREFIX=\"$mapPrefix\_data_\"\n";
    print PARAMSEXPRESS "MAP_FILENAME=\"$mapPrefix\_sample_subject_mapping.txt\"\n";
    print PARAMSEXPRESS "\n";
    print PARAMSEXPRESS "# Optional\n";
    print PARAMSEXPRESS "DATA_TYPE=R\n";
    print PARAMSEXPRESS "LOG_BASE=2\n";
    print PARAMSEXPRESS "SECURITY_REQUIRED=N\n";
    print PARAMSEXPRESS "STUDY_ID=$studyId\_COVID\n";
    print PARAMSEXPRESS "STUDY_NAME=\"$studyAuthor($studyYear) $studyId\"\n";
    print PARAMSEXPRESS "TOP_NODE_PREFIX=\"$studyTopnode\"\n";
    print PARAMSEXPRESS "SOURCE_CD=\"GEO\"\n";
    print PARAMSEXPRESS "\n";
    print PARAMSEXPRESS "# For information only\n";
    
    close PARAMSEXPRESS;
}

#################################################
# 8. Reporting sample data
#    columns test (scalar keys %sampleXxxx)
#    for each row test defined(sampleXxxx{"$id"})
#################################################

foreach $ct (sort(keys(%sample))) {
    if($sample{$ct} eq "unknown") {
	print LOG "UNKNOWN samplechar $ct $sampleCount{$ct}\n";
    }
}
foreach $ct (sort(keys(%sample))) {
    if($sample{$ct} ne "unknown" && defined($sampleCount{$ct})) {
	print LOG "samplechar $ct $sampleCount{$ct}\n";
    }
}

foreach $id (sort(keys(%countId))){
    $used = "";
    $text = $id;
    $nkeys = scalar keys(%{$countVal{"$id"}});
    if(defined($usedId{"$id"})){
	if($nkeys==1) {
	    $used = "only";	# single value for all
	} elsif($countId{"$id"} == 1) {
	    $used = "each";	# all unique values
	} else {
	    $used = "used";	# multiple values useful for queries
	}
    }
    elsif($nkeys > 1 && $nkeys < $countId{"$id"}){
	$used=">>>>";
    }
    elsif($nkeys > 1 && $nkeys == $countId{"$id"}){
	$used="....";
    }
    if($nkeys > 1) {
	@testSorted = sort(keys(%{$countVal{"$id"}}));
	$text .= "\t('".$testSorted[0]."' ... '".$testSorted[$#testSorted]."')";
    }
    elsif($nkeys==1) {
	@testSorted = sort(keys(%{$countVal{"$id"}}));
	$text .= "\t'".$testSorted[0]."'";
    }

    printf LOG "%4s %5d %5d %s\n", $used, $countId{"$id"}, scalar keys(%{$countVal{"$id"}}), $text;
}

#################################################################################################
#
# Update for individual study
# Run once and check parse.out for warnings
#              check parse.txt for unused values
#                                  and for expected values that are missing
#
# Section 1: Check for any extra fields to be captured
#            see parse.out for data not used
#
# Section 2: Set OUT column headers
#            Check MAPSAMPLE column headers (usually fixed)
#
# Section 3: Check any additional fixing of values e.g. time, ethnicity, sample parsing
#            Enable parseSampleTitle to extract values not shown elsewhere
#
# Section 4: (no changes needed)
#
# Section 5: Merge visits etc into main row
#
# Section 6: Further data value cleanup
#            Enable parseClinicalSource to process source string
#
# Section 7: Check clinicalSource values for TISSUETYPE
#            Check visit time (if any)
#            Select data for OUT rows
#
#################################################################################################

foreach $t (sort(keys(%known))){
    if(!defined($usedMap{"$t"}) && !defined($usedData{"$t"}) && !defined($usedSample{"$t"}) && !defined($usedBrowse{"$t"})) {
	print STDERR "Unused data: $t\n";
    }
}

foreach $t (sort(keys(%usedMap))){
    if(!defined($known{"$t"})) {
	print STDERR "Undefined clinical map: $t\n";
    }
}
					    
foreach $t (sort(keys(%usedData))){
    if(!defined($known{"$t"})) {
	print STDERR "Undefined clinical data: $t\n";
    }
}
					    
foreach $t (sort(keys(%usedSample))){
    if(!defined($known{"$t"})) {
	print STDERR "Undefined sample data: $t\n";
    }
}

foreach $t (sort(keys(%usedBrowse))){
    if(!defined($known{"$t"})) {
	print STDERR "Undefined browse data: $t\n";
    }
}
					    

# values to be tracked down

# sampleStudy for series
# sampleTaxId
# seriesRelation
# studySampleOrganism
# studySampleTaxId
# studyStatus
# studyYear

#  undefined:
# map sampleTime
# map demographicsEthnicity

close ERR;
close LOG;
