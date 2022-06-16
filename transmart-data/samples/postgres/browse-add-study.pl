#!/usr/bin/perl -w

################################################################################
# TO DO:
# ------
#
# Check OverallDesign is stored/read from BioExperiment
# Check there is a default value for species etc.
# Allow species to be optional - use 'Other' if not found
# global variable for validation to help debugging
#    e.g. test DoI, PubmedID, StudyURL
#
# Add citation so we have the normal version plus pubmed and doi to download it
#
# load contact, address, first-mid-last. roles
#
###############################################################################

###############################################################################
#
# Add Browse tab data for a study
#
# Data is defined in a file with prefix: value on each line
#
# Additional values are required to identify the parent program
# Data needed: Items in data/common/amapp/am_tag_item.tsv with:
#
# col 1: tag_template_id = 2
# col 3: required = 1       (all study values are required with '.' as missing)
#
# Code_type_name (value type) fixed-list-value-type
# in display_order
#
#  1 STUDY_IDENTIFIER (FIXED FREETEXT) accession
#  2 STUDY_TITLE (FIXED FREETEXT) title
#  3 STUDY_DESCRIPTION (FIXED FREETEXTAREA) description
#+ 4 OVERALL_DESIGN (FIXED FREETEXTAREA)
#  5 STUDY_TARGET
#  6 STUDY_ETL_ID
#  7 PATHOLOGY (BIO_DISEASE MULTIPICKLIST) [bioDisease after title]
#  8 STUDY_PHASE (CUSTOM MULTIPICKLIST)
#  9 STUDY_OBJECTIVE (CUSTOM MULTIPICKLIST)
# 10 STUDY_DESIGN (FIXED PICKLIST) design
# 11 STUDY_BIOMARKER_TYPE (FIXED MULTIPICKLIST) bioMarkerType
# 12 STUDY_LINK (CUSTOM FREETEXT)
# 13 NUMBER_OF_FOLLOWED_SUBJECTS (CUSTOM FREETEXT)
# 14 NUMBER_OF_SAMPLES (CUSTOM FREETEXT)
# 15 SPECIES (CUSTOM MULTIPICKLIST)
# 16 STUDY_ACCESS_TYPE (FIXED PICKLIST) accessType
# 17 COUNTRY (FIXED MULTIPICKLIST) country
# 18 STUDY_START_DATE (FIXED DATE)
# 19 STUDY_COMPLETE_DATE (FIXED DATE)
# 20 STUDY_PUBMED_ID (CUSTOM FREETEXT)
# 21 STUDY_PUBLICATION_DOI (CUSTOM FREETEXT)
# 22 STUDY_PUBLICATION_CITATION FREETEXT)
# 23 STUDY_PUBLICATION_AUTHOR_LIST (CUSTOM FREETEXT)
# 24 STUDY_PUBLICATION_TITLE (CUSTOM FREETEXT)
# 25 STUDY_PUBLICATION_STATUS (CUSTOM PICKLIST)
#+26 STUDY_PERSON_NAME (CUSTOM FREETEXT)
#+27 STUDY_PERSON_ROLE (CUSTOM FREETEXT)
#+28 STUDY_PERSON_CONTACT (CUSTOM FREETEXT)
#+29 STUDY_INSTITUTION (CUSTOM FREETEXT)
#+30 STUDY_PERSON_ADDRESS (CUSTOM FREETEXT)
#+31 STUDY_ENTRY_TIME (FIXED TIME)
#+32 STUDY_LAST_UPDATE (FIXED CURRENTTIME)
#
#
###############################################################################

# todo

# error if any tag is not found in findTag ... message in findtag
# report empty values at end in case they can be filled
# add to summary of issues to be resolved

# tag values not working
# can see only:
# accession (study identifier)
# study design
# biomarker type
# access type
# country
# (i.e. the data in the bio_experiment)

# multiple entries needed for:

# pathology
# phase
# objective
# biomarker
# organism
# country

if(!defined($ARGV[0])){
    print STDERR "Usage: browse-add.pl input-filename\n";
    exit;
}

@time = localtime(time());
$currenttime = sprintf "%4d-%02d-%02d %02d:%02d:%02d", 1900+$time[5], 1+$time[4], $time[3], $time[2], $time[1], $time[0];
print "Running at $currenttime\n";

$dovalidate=0;
$ispostgres=1;
$doTestPubmed = 0;
$validateMsg = "";
$msg = "";

$maxdesc = 3999;
$maxdesign = 1999;
foreach $arg (@ARGV) {
    if(-e "$arg") {$infile = $arg}
    elsif($arg eq "-validate") {$dovalidate = 1}
    elsif($arg eq "-oracle") {$ispostgres = 0}
    elsif($arg eq "-postgres") {$ispostgres = 1}
    elsif($arg eq "-pubmed") {$doTestPubmed = 1}
    else {
	print "Unknown argument $arg\n";
	exit;
    }
}

if(!$ispostgres) {
    # run sqlplus in silent mode
    # Require ORAPASSWORD and ORAHOST defined
    # ORAPORT defaults to "1521"
    # ORASID defaults to "transmart"
    $sqlplus = "sqlplus -S \"sys";
    $var = $ENV{"ORAPASSWORD"} || die "ORAPASSWORD not defined";
    $sqlplus .= "/$var";
    $var = $ENV{"ORAHOST"} || die "ORAHOST not defined";
    $sqlplus .= "\@$var";
    $var = $ENV{"ORAPORT"} || "1521";
    $sqlplus .= ":$var";
    $var = $ENV{"ORASID"} || "transmart";
    $sqlplus .= "/$var";
    $sqlplus .= "\" AS SYSDBA";
#    print "$sqlplus\n";
}

%linetypes = ("Program" => 0,
	      "Accession" => 1,		# Study accession
	      "Title" => 1,		# Study title
	      "Description" => 2,
	      "Overalldesign" => 2,
	      "Target" => 1,
	      "Etlid" => 1,
	      "Pathology" => 2,		# test BioDisease *
	      "Phase" => 2,		# test BioConceptCode STUDY_PHASE *
	      "Objective" => 2,		# test BioConceptCode STUDY_OBJECTIVE *
	      "Design" => 1,		# test BioConceptCode STUDY_DESIGN .
	      "Biomarker" => 2,		# test BioConceptCode STUDY_BIOMARKER_TYPE *
	      "Link" => 1,		# URL (GEO etc.) or .
	      "Subjects" => 1,		# Number or .
	      "Samples" => 1,		# Number or .
	      "Organism" => 2,		# test BioConceptCode SPECIES *
	      "Access" => 1,		# test BioConceptCode STUDY_ACCESS_TYPE .
	      "Country" => 2,		# test BioConceptCode COUNTRY *
	      "Startdate" => 1,		# yyyy-MM-dd
	      "Completedate" => 1,	# yyyy-MM-dd
	      "Pubmed" => 1,		# PubmedID or .
	      "Doi" => 1,		# DOI or .
	      "Citation" => 1,		# Citation or .
	      "Authors" => 1,		# Authorlist or . (clean characters)
	      "Pubtitle" => 1,		# Title or . (clean characters)
	      "Status" => 1,		# test BioConceptCode STUDY_PUBLICATION_STATUS .
	      "Namepi" => 1,		# name or .
	      "Roles" => 2,		# free text with standard parts
	      "Contact" => 1,		# name or .
	      "Institution" => 1,	# Institute, department, laboratory
	      "Address" => 1,		# address, email, etc
	      # "EntryTime" current time
	      # "LastUpdate" current time
    );

$program = "Public Studies";
$description = "";
$overalldesign = "";
$address = "";
$roles = "";

open(IN,"$infile") || die "Failed to open $infile";
@input = ();
@linetype = ();
$line=0;
while (<IN>) {
    chomp;
    if(/^[\#]/) {next}
    if(/^\s*$/) {next}
    if(/^([A-Z][a-z]+):\s*(.*)/){
	++$line;
#	print "Read line $line: $_\n";
	$type = $1;
	$data = $2;
	$savedata = $data;
	if($ispostgres) {
	    $data =~ s/\'/\'\'/g; # Double up any single quotes
	} else {
	    $data =~ s/\'/\\\'/g; # Escape any single quotes
	}
	if(defined($linetypes{$type})){
	    push @linetype, $type;
	    push @input, $data;
	}
	else {
	    print STDERR "Line $line: Unrecognized type $type;\n";
	    exit;
	}
    }
}

# Check we have all the data we need
foreach $t (keys(%linetypes)){
    if($linetypes{$t}) {
	$required{$t}=0;
    }
}

for($i = 0; $i < $line; $i++) {
#    print "Found line $i: $linetype[$i]\n";
    $type = $linetype[$i];
    $data = $input[$i];

    if($type eq "Name") {$type="Namepi"}

    $required{$type}++;
    if($type eq "Program"){
	$program = $data;
    }
    elsif($type eq "Title"){
	$title = $data;
    }
    elsif($type eq "Accession"){
	$accession = $data;
    }
    elsif($type eq "Description"){
	if($description ne "") {$description .= "<br/><br/>"}
	$description .= $data;
    }
    elsif($type eq "Overalldesign"){
	if($overalldesign ne "") {$overalldesign .= " "}
	$overalldesign .= $data;
    }
    elsif($type eq "Target"){
	$target = $data;
    }
    elsif($type eq "Etlid"){
	$etlid = $data;
    }
    elsif($type eq "Design"){
	$design = $data;
    }
    elsif($type eq "Link"){
	$link = $data;
    }
    elsif($type eq "Subjects"){
	$subjects = $data;
    }
    elsif($type eq "Samples"){
	$samples = $data;
    }
    elsif($type eq "Access"){
	$accesstype = $data;
    }
    elsif($type eq "Startdate"){
	$startdate = $data;
    }
    elsif($type eq "Completedate"){
	$completedate = $data;
    }
    elsif($type eq "Pubmed"){
	$pubmed = $data;
    }
    elsif($type eq "Doi"){
	$doi = $data;
    }
    elsif($type eq "Citation"){
	$citation = $data;
    }
    elsif($type eq "Authors"){
	$authors = $data;
    }
    elsif($type eq "Pubtitle"){
	$pubtitle = $data;
    }
    elsif($type eq "Status"){
	$status = $data;
    }
    elsif($type eq "Namepi"){
	$namepi = $data;
    }
    elsif($type eq "Roles"){
	if($roles ne "") {$roles .= ", "}
	$roles .= $data;
    }
    elsif($type eq "Contact"){
	$contact = $data;
    }
    elsif($type eq "Institution"){
	$institution = $data;
    }
    elsif($type eq "Address"){
	$address = $data;
    }

    # Multiple values (linetype 2)

    elsif($type eq "Pathology"){
	push @pathology, $data;
    }
    elsif($type eq "Phase"){
	push @phase, $data;
    }
    elsif($type eq "Objective"){
	push @objective, $data;
    }
    elsif($type eq "Biomarker"){
	push @biomarker, $data;
    }
    elsif($type eq "Organism"){
	push @organism, $data;
    }
    elsif($type eq "Country"){
	push @country, uc($data);
    }
}

foreach $t (sort(keys(%required))){
    if(!$required{$t}){
	$msg = "Error: Line type '$t' not found\n";
	print STDERR $msg;
	$validateMsg .= $msg;
    }
    elsif($linetypes{$t}==1 && $required{$t}>1){
	$msg = "Error: Line type '$t' found $required{$t} times\n";
	print STDERR $msg;
	$validateMsg .= $msg;
    }
}

if(!$dovalidate && $validateMsg ne "") {
    print STDERR "Aborting with error(s)\n";
    exit;
}

sub testData($$) {
    my ($name,$data) = @_;
    if(!defined($data)) {print STDERR "$name undefined\n";$err++}
    elsif($data eq "") {print STDERR "Empty data value found for $name\n";$err++}
    else{return 1}
    return 0;
}

sub useData($$) {
    my ($name,$data) = @_;
    if(!defined($data)) {print STDERR "$name undefined\n";$err++}
    elsif($data eq "") {print STDERR "Empty data value found for $name\n";$err++}
    else{print "Test text '$name' '$data'\n"}
}

#############################################################
#
# findFolder: Find a program or study folder by type and name
#
#############################################################

sub findFolder($$$) {
    my ($type,$folder,$level) = (@_);
    my $dosql = "select folder_id,folder_type from fmapp.fm_folder where folder_level = $level and folder_name = '$folder'";

    if($ispostgres) {
	open(PSQL, "psql -A -t -c \"$dosql\"|") || die "Failed to start psql";
	while(<PSQL>){
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		if($col[1] ne $type) {
		    $msg = "Folder '$folder' type '$col[1]' expected '$type'\n";
		    print STDERR $msg;
		    $validateMsg .= $msg;
		    last;
		}
		return $col[0];
	    }
	}
	close PSQL;
    } else {
	open(OSQL, "echo  \"$dosql;\" | $sqlplus|") || die "Failed to start sqlplus";
	while(<OSQL>){
	    chomp;
	    s/^\s+//;
	    if(/[|]/) {
		@col = split (/[|]/);
		if($col[1] ne $type) {
		    $msg = "Folder '$folder type '$col[1]' expected '$type'\n";
		    print STDERR $msg;
		    $validateMsg .= $msg;
		    last;
		}
		return $col[0];
	    }
	}
	close OSQL;
    }
    return 0;
}

####################################################################
#
# findTemplate: find a tag_template id for a given tag_template_type
#
####################################################################

sub findTemplate($) {
    my ($type) = (@_);
    my $dosql = "select tag_template_id,tag_template_type from amapp.am_tag_template where tag_template_type = '$type'";
    if($ispostgres) {
	open(PSQL, "psql -A -t -c \"$dosql\"|") || die "Failed to start psql";
	while(<PSQL>){
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		if($col[1] ne $type) {
		    $msg = "Template type '$col[1]' expected '$type'\n";
		    print STDERR $msg;
		    $validateMsg .= $msg;
		    last;
		}
		return $col[0];
	    }
	}
	close PSQL;
    } else {
	open(OSQL, "echo \"$dosql;\" | $sqlplus|") || die "Failed to start sqlplus";
	while(<OSQL>){
	    chomp;
	    s/^\s+//;
	    if(/[|]/) {
		@col = split (/[|]/);
		if($col[1] ne $type) {
		    $msg = "type '$col[1]' expected '$type'\n";
		    print STDERR $msg;
		    $validateMsg .= $msg;
		    last;
		}
		return $col[0];
	    }
	}
	close OSQL;
    }
    return 0;
}

######################################################################
#
# findDisease: find the unique id for a disease in biomart.bio_disease
#
######################################################################

sub findDisease($) {
    my ($disease) = (@_);
    my $dosql = "select bio_data_id, unique_id from biomart.bio_data_uid where bio_data_id = (select bio_disease_id from biomart.bio_disease where (disease = '$disease' or mesh_code = '$disease' or icd9_code = '$disease' or icd10_code = '$disease'))";

    if($ispostgres) {
	open(PSQL, "psql -A -t -c \"$dosql\"|") || die "Failed to start psql";
	while(<PSQL>){
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		return @col;
	    }
	}
	close PSQL;
    } else {
	open(OSQL, "echo \"$dosql;\" | $sqlplus|") || die "Failed to start sqlplus";
	while(<OSQL>){
	    chomp;
	    s/^\s+//;
	    if(/[|]/) {
		@col = split (/[|]/);
		return @col;
	    }
	}
	close OSQL;
    }
    return ();
}

###########################################################################
#
# findConcept: find the unique id for a concept in biomart.bio_concept_code
#
###########################################################################

sub findConcept($$) {
    my ($concept,$type) = (@_);
    my $dosql = "select bio_data_id, unique_id from biomart.bio_data_uid where bio_data_id = (select bio_concept_code_id from biomart.bio_concept_code where (code_name = '$concept' or bio_concept_code = '$concept') and code_type_name = '$type')";

    if($ispostgres) {
	open(PSQL, "psql -A -t -c \"$dosql\"|") || die "Failed to start psql";
	while(<PSQL>){
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		return @col;
	    }
	}
	close PSQL;
    } else {
	open(OSQL, "echo \"$dosql;\" | $sqlplus|") || die "Failed to start sqlplus";
	while(<OSQL>){
	    chomp;
	    s/^\s+//;
	    if(/[|]/) {
		@col = split (/[|]/);
		return @col;
	    }
	}
	close OSQL;
    }
    return ();
}

########################################################
#
# findTag: Find a tag_item_id for a given code_type_name
#
########################################################

sub findTag($) {
    my ($type) = (@_);
    my $dosql = "select tag_item_id,code_type_name from amapp.am_tag_item where code_type_name = '$type'";

    if($ispostgres) {
	open(PSQL, "psql -A -t -c \"$dosql\"|") || die "Failed to start psql";
	while(<PSQL>){
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		return $col[0];
	    }
	}
	close PSQL;
    } else {
	open(OSQL, "echo \"$dosql;\" | $sqlplus|") || die "Failed to start sqlplus";
	while(<OSQL>){
	    chomp;
	    s/^\s+//;
	    if(/[|]/) {
		@col = split (/[|]/);
		return $col[0];
	    }
	}
	close OSQL;
    }
    return 0;
}

###############################################################################
#
# findAccession: find the bio_data_id and unique ID of a biomart.bio_experiment
#                for a given accession (study ID)
#
###############################################################################

sub findAccession($) {
    my ($access) = @_;
    my $dosql = "select bio_data_id, unique_id from biomart.bio_data_uid where bio_data_id = (select bio_experiment_id from biomart.bio_experiment where accession = '$access' and bio_experiment_type = 'Experiment')";

    if($ispostgres) {
	open(PSQL, "psql -A -t -c \"$dosql\"|") || die "Failed to start psql";
	while(<PSQL>){
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		return @col;
	    }
	}
	close PSQL;
    } else {
	open(OSQL, "echo \"$dosql;\" | $sqlplus|") || die "Failed to start sqlplus";
	while(<OSQL>){
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		return @col;
	    }
	}
	close OSQL;
    }
    return ();
    
}

######################################################
#
# testPubmed: Check a pubmedId (run if -pubmed is set)
#
######################################################

sub testPubmed($) {
    my ($pubmed) = @_;
    if($pubmed eq ".") {
	$pubmedtitle = ".";
	$pubmedauthors = ".";
	return;
    }
    open (WEB,"curl --silent https://pubmed.ncbi.nlm.nih.gov/pubmed/$pubmed|") || die "Cannot connect to pubmed central";
    $text = "";
    while (<WEB>) {
	$text .= $_;
    }
    if($text =~ /The following PMID is not available:/) {
	$msg = "PMID failure message from PubMed '$pubmed' retrieval\n";
	print STDERR $msg;
	$validateMsg .= $msg;
	return 0;
    }

    if($text =~ /doi: (\S+)/sm) {
	$pubmeddoi = $1;
	$pubmeddoi =~ s/[.]$//g;
	print "Pubmed $pubmed doi '$pubmeddoi'\n";
    }

    if($text =~ /<title>(.*?)<\/title>/sm) {
	$pubmedtitle = $1;
	$pubmedtitle =~ s/- PubMed//;
	$pubmedtitle =~ s/- NCBI//;
	$pubmedtitle =~ s/\s+/ /g;
	$pubmedtitle =~ s/ $//;
	$pubmedtitle =~ s/[.]$//;
	print "Pubmed $pubmed title '$pubmedtitle'\n";
    }

    if($text =~ /<div class=\"auths\">(.*?)<\/div>/sm) {
	$pubmedauthors = "";
	$authlist = $1;
	$authlist =~ s/<sup>\d+<\/sup>//g;
	while($authlist =~ /<a href=\"\/pubmed\/?[^\"]+\">([^<]+)<\/a>/g){
	    if($pubmedauthors ne "") {$pubmedauthors .= ", "}
	    $pubmedauthors .= $1;
	}
	print "Pubmed $pubmed authors '$pubmedauthors'\n";
    }

    close WEB;
}

$studyProgram = $ENV{STUDY_PROGRAM};
print "Looking for program '$program'\n";
print STDERR "Looking for program '$program'\n";
$programid = findFolder("PROGRAM", $program, 0);

if(!$programid) {print STDERR "program '$program' not found\n";exit}
else {print "program '$program' found with ID $programid\n"}

$studyid = findFolder("STUDY", $title, 1);

if(!$studyid) {
    print "study '$title' not found, can create\n";
} else {
    print STDERR "study '$title' found with ID $studyid\n";
    print STDERR "Study exists. Load canceled\n";
    exit
}

#Accession .... what do we need to check?

($experimentid,$experimentcode) = findAccession($accession);
if(!defined($experimentcode)) {print "Experiment '$accession' not found, can create\n"}
else {print STDERR "Experiment '$accession' already exists id '$experimentid', code '$experimentcode'\n"}
    

print "Testing new study metadata\n";

$pathologytag = findTag("PATHOLOGY");
print "Pathology tagid $pathologytag\n";

$pathologycode = ();
foreach $pathology (@pathology){
    ($id, $code) = findDisease($pathology);

    if(!defined($code)){print STDERR "Pathology '$pathology' not found\n";exit}
    else {
	print "Pathology '$pathology' id '$id', code '$code'\n";
	push @pathologycode, $code;
    }
}

@phasecode = ();
$phasetag = findTag("STUDY_PHASE");
print "Study phase tagid $phasetag\n";

foreach $phase (@phase){
    ($id, $code) = findConcept($phase,"STUDY_PHASE");

    if(!defined($code)){print STDERR "Phase '$phase' not found\n";exit}
    else {
	print "Phase '$phase' id '$id', code '$code'\n";
	push @phasecode, $code;
    }
}

@objectivecode = ();
$objectivetag = findTag("STUDY_OBJECTIVE");
print "Study objective tagid $objectivetag\n";

foreach $objective (@objective){
    ($id, $code) = findConcept($objective,"STUDY_OBJECTIVE");

    if(!defined($code)){print STDERR "Objective '$objective' not found\n";exit}
    else {
	print "Objective '$objective' id '$id', code '$code'\n";
	push @objectivecode, $code;
    }
}

#Description - what do we need to check e.g. length, character set

$description =~ s/<[pP]>/<br\/><br\/>/g; # Use E'$description' to escape characters in PSQL
$description =~ s/<\/[pP]>//g;		 # remove any end-of-paragraph tags

print "Edited description: '$description'\n";

if(length($description) > $maxdesc)  {
    $msg = "Description too long: ".length($description)."\n";
    print STDERR $msg;
    $validateMsg .= $msg;
}

$overalldesign =~ s/<[pP]>/<br\/><br\/>/g; # Use E'$description' to escape characters in PSQL
$overalldesign =~ s/<\/[pP]>//g;		 # remove any end-of-paragraph tags

if(length($overalldesign) > $maxdesign)  {
    $msg = "OverallDesign too long: ".length($overalldesign)."\n";
    print STDERR $msg;
    $validateMsg .= $msg;
}

# store in folder and experiment

#Design

($designid, $designcode) = findConcept($design,"STUDY_DESIGN");

if(!defined($designcode)){print STDERR "Design '$design' not found\n";exit}
else {print STDERR "Design '$design' id '$designid', code '$designcode'\n"}

#Biomarker...

$biomarkerall = "";
foreach $biomarker (@biomarker){
    ($id, $code) = findConcept($biomarker,"STUDY_BIOMARKER_TYPE");

    if(!defined($code)){print STDERR "Biomarker '$biomarker' not found\n";exit}
    else {
	print "Biomarker '$biomarker' id '$id', code '$code'\n";
	if($biomarkerall ne ""){$biomarkerall .= "|"}
	$biomarkerall .= $code;
    }
}


#Link ... test valid HTML page

$linktag = findTag("STUDY_LINK");

print "Link '$link' tag '$linktag'\n";

# GEO returns Could not find a public or private accession "GSE98765"

#Subjects ... test numeric (must be positive number)

$subjectstag = findTag("NUMBER_OF_FOLLOWED_SUBJECTS");

if($subjects !~ /^[1-9][0-9]*$/) {
    $msg = "Subjects '$subjects' is not a valid number\n";
    print STDERR $msg;
    $validateMsg .= $msg;
}
else{print "Subjects '$subjects' tag '$subjectstag'\n"}

#Samples ... test numeric (may be zero)

$samplestag = findTag("NUMBER_OF_SAMPLES");

if($samples ne "." && $samples !~ /^[0-9]+$/) {
    $msg = "Samples '$samples' is not a valid number\n";
    print STDERR $msg;
    $validateMsg .= $msg;
}
else{print "Samples '$samples' tag '$samplestag'\n"}

#Organism...

@organismcode = ();
$organismtag = findTag("SPECIES");
print "Organism tagid $organismtag\n";

foreach $organism (@organism){
    ($id, $code) = findConcept($organism,"SPECIES");

    if(!defined($code)){
	print STDERR "Organism '$organism' not found: Use 'Other'\n";
	$organism = "Other";
	($id, $code) = findConcept($organism,"SPECIES");
    }
    if(!defined($code)){print STDERR "Organism '$organism' not found: Use 'Other'\n";exit}
    else {
	print "Organism '$organism' id '$id', code '$code'\n";
	push @organismcode, $code;
    }
}

#Access
($accessid, $accesscode) = findConcept($accesstype,"STUDY_ACCESS_TYPE");
if(!defined($accesscode)){print STDERR "Access type '$accesstype' not found\n";exit}
else {print "Accesstype '$accesstype' id '$accessid', code '$accesscode'\n"}


#Country...

$countryall = "";
foreach $country (@country){
    if($country eq "."){$country = "Not Applicable"}
    ($id, $code) = findConcept($country,"COUNTRY");

    if(!defined($code)){print STDERR "Country '$country' not found\n";exit}
    else {
	print "Country '$country' id '$id', code '$code'\n";
	if($countryall ne ""){$countryall .= "|"}
	$countryall .= $code;
    }
}

#Start Date
$startdatetag = findTag("STUDY_START_DATE");
print "Start date '$startdate' untested tag '$startdatetag'\n";

#Completion Date
$completedatetag = findTag("STUDY_COMPLETE_DATE");
print "Completion date '$completedate' untested tag '$completedatetag'\n";

#Pubmed try ID?

$pubmedauthors = $pubmedtitle = $pubmeddoi = "";

$pubmedtag = findTag("STUDY_PUBMED_ID");
if($doTestPubmed) {
    $ok = testPubmed($pubmed);
    if(!$ok) {
	$msg = "Pubmed '$pubmed' not found\n";
	print STDERR $msg;
	$validateMsg .= $msg;
    } else {print "Pubmed '$pubmed' found authors '$pubmedauthors' title '$pubmedtitle' doi '$pubmeddoi' tag '$pubmedtag'\n"}
}

$doitag = findTag("STUDY_PUBLICATION_DOI");
if($pubmeddoi ne "") {
    if($pubmeddoi ne $doi) {
	$msg = "Doi '$doi' does not match Pubmed DOI '$pubmeddoi'\n";
	print STDERR $msg;
	$validateMsg .= $msg;
    }
} else {
    $msg = "Doi '$doi' untested tag '$doitag'\n";
    print $msg;
    $validateMsg .= $msg;
}

$citationtag = findTag("STUDY_PUBLICATION_CITATION");

#Authors

$authorstag = findTag("STUDY_PUBLICATION_AUTHOR_LIST");
if($pubmedauthors ne "") {
    if($pubmedauthors ne $authors) {
	$msg = "Authors '$authors' does not match Pubmed authors '$pubmedauthors'\n";
	print STDERR $msg;
	$validateMsg .= $msg;
    }
} else {
    print "Authors '$authors' untested tag '$authorstag'\n";
}

$authorstag =~ s/\'//go;

#Status

($statusid, $statuscode) = findConcept($status,"STUDY_PUBLICATION_STATUS");
if(!defined($statuscode)){print STDERR "Status '$status' not found\n";exit}
else {print "Status '$status' id '$statusid', code '$statuscode'\n"}

if($dovalidate) {
    print "\nValidation completed\n";
    print $validateMsg;
    exit;
}

$nametag = findTag("STUDY_PERSON_NAME");
if(testData("Namepi",$namepi)){print "NamePI '$namepi' tag '$nametag'\n"}

$rolestag = findTag("STUDY_PERSON_ROLES");
if(testData("Roles",$roles)){print "Roles '$roles' tag '$rolestag'\n"}

$addresstag = findTag("STUDY_PERSON_ADDRESS");
if(testData("Address",$address)){print "Address '$address' tag '$addresstag'\n"}


$titletag = findTag("STUDY_PUBLICATION_TITLE");
if($pubmedtitle ne "") {
    if($pubmedtitle ne $pubtitle) {
	$msg = "Pubtitle '$pubtitle' does not match Pubmed title '$pubmedtitle'\n";
	print STDERR $msg;
	$validateMsg .= $msg;
    }
} else {
    print "Pubtitle '$pubtitle' untested tag '$titletag'\n";
}

# Create study folder
# Annotate study folder

#####################
# create study folder
#####################

if(!$studyid) {
    if($ispostgres) {
	open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
	print DOSQL "set search_path = fmapp, pg_catalog;

insert into fmapp.fm_folder (folder_name,parent_id,folder_level,folder_type,active_ind,description)
";
	print DOSQL " values ('$title',$programid,1,'STUDY',true,E'$description');\n";
	close DOSQL;
	print STDERR "Create study\n";
	open(OUT, "psql.out") || die "No psql results found";
	while(<OUT>){
	    if(/^SET$/){next}
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    } else {
	open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

	print DOSQL "insert into fmapp.fm_folder (folder_name,parent_id,folder_level,folder_type,active_ind,description)
       values ('$title',$programid,1,'STUDY','1','$description');
";
	close DOSQL;
	print STDERR "Create study\n";
	open(OUT, "sqlplus.out") || die "No sqlplus results found";
	while(<OUT>){
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    }

    $studyid = findFolder("STUDY", $title, 1);
    print STDERR "Created study '$title' with ID '$studyid'\n";
}


##########################
# create experiment
# and link to study folder
##########################

$studytemplate = findTemplate("STUDY");
print STDERR "Study template id '$studytemplate'\n";

if(!defined($experimentcode)){
    if($ispostgres) {
	open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
	print DOSQL "set search_path = biomart, pg_catalog;

insert into biomart.bio_experiment
    (bio_experiment_type,title,description,design,start_date,completion_date,primary_investigator,contact_field,
     etl_id,status,overall_design,accession,entrydt,updated,institution,country,biomarker_type,target,access_type)
    values ('Experiment','$title',E'$description','$designcode','$startdate','$completedate','$namepi','$contact',
	    '$etlid','$statuscode',E'$overalldesign','$accession','$currenttime','$currenttime','$institution','$countryall','$biomarkerall','$target','$accesscode');

set search_path = amapp, pg_catalog;

insert into amapp.am_tag_template_association (tag_template_id,object_uid)
       values ($studytemplate,'FOL:$studyid');
";
	close DOSQL;
	print STDERR "Create experiment\n";
	open(OUT, "psql.out") || die "No psql results found";
	while(<OUT>){
	    if(/^SET$/){next}
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    } else {
	open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

	print DOSQL "insert into biomart.bio_experiment (bio_experiment_type,title,description,design,accession,country,biomarker_type,access_type)
       values ('Experiment','$title','$description','$designcode','$accession','$countryall','$biomarkerall','$accesscode');

insert into amapp.am_tag_template_association (tag_template_id,object_uid)
       values ($studytemplate,'FOL:$studyid');
";
	close DOSQL;
	print STDERR "Create experiment\n";
	open(OUT, "sqlplus.out") || die "No sqlplus results found";
	while(<OUT>){
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    }

    ($experimentid,$experimentcode) = findAccession($accession);
    print STDERR "Created experiment with ID '$experimentid' code '$experimentcode'\n";
}

if($ispostgres) {
    open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
    print DOSQL "set search_path = fmapp, pg_catalog;

insert into fmapp.fm_folder_association (folder_id,object_uid,object_type)
       values ('$studyid','$experimentcode','org.transmart.biomart.Experiment');
";
    close DOSQL;
    print STDERR "Link experiment\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "insert into fmapp.fm_folder_association (folder_id,object_uid,object_type)
       values ('$studyid','$experimentcode','org.transmart.biomart.Experiment');
";
    close DOSQL;
    print STDERR "Link experiment\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

# add tags
# already in bio_experiment:     bioexperiment
# name
# description
# overalldesign
# accession
# countrycode ('|' for multiple countries)
# biomarkertype ('|' for multiple marker types)
# accesstype

################################################################
# all remaining tags to be added to am_tag_item and am_tag_value
################################################################

####################
# Add tag: pathology
####################

foreach $pathologycode (@pathologycode){
    if($ispostgres) {
	open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
	print DOSQL "set search_path = amapp, pg_catalog;

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','$pathologycode','BIO_DISEASE',$pathologytag)
";
	close DOSQL;
	print STDERR "Add tag for pathology '$pathologycode'\n";
	open(OUT, "psql.out") || die "No psql results found";
	while(<OUT>){
	    if(/^SET$/){next}
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    } else {
	open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

	print DOSQL "insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','$pathologycode','BIO_DISEASE',$pathologytag);
";
	close DOSQL;
	print STDERR "Add tag for pathology '$pathologycode'\n";
	open(OUT, "sqlplus.out") || die "No sqlplus results found";
	while(<OUT>){
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    }
}

######################
# Add tag: study phase
######################

foreach $phasecode (@phasecode) {
    if($ispostgres) {
	open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
	print DOSQL "set search_path = amapp, pg_catalog;

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','$phasecode','STUDY_PHASE',$phasetag)
";
	close DOSQL;
	print STDERR "Add tag for study phase '$phasecode'\n";
	open(OUT, "psql.out") || die "No psql results found";
	while(<OUT>){
	    if(/^SET$/){next}
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    } else {
	open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

	print DOSQL "insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','$phasecode','STUDY_PHASE',$phasetag);
";
	close DOSQL;
	print STDERR "Add tag for study phase '$phasecode'\n";
	open(OUT, "sqlplus.out") || die "No sqlplus results found";
	while(<OUT>){
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    }
}

##########################
# Add tag: study objective
##########################

foreach $objectivecode (@objectivecode) {
    if($ispostgres) {
	open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
	print DOSQL "set search_path = amapp, pg_catalog;

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','$objectivecode','STUDY_OBJECTIVE',$objectivetag)
";
	close DOSQL;
	print STDERR "Add tag for study objective '$objectivecode'\n";
	open(OUT, "psql.out") || die "No psql results found";
	while(<OUT>){
	    if(/^SET$/){next}
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    } else {
	open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

	print DOSQL "insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','$objectivecode','STUDY_OBJECTIVE',$objectivetag);
";
	close DOSQL;
	print STDERR "Add tag for study objective '$objectivecode'\n";
	open(OUT, "sqlplus.out") || die "No sqlplus results found";
	while(<OUT>){
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    }
}

#####################
# Add tag: study link
#####################

if($ispostgres) {
    open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
    print DOSQL "set search_path = amapp, pg_catalog;

with tagvalue as (insert into amapp.am_tag_value (value) values ('$link') returning tag_value_id)

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||(select tag_value_id from tagvalue),'AM_TAG_VALUE',$linktag)
";
    close DOSQL;
    print STDERR "Add tag for study link\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "declare
  v_tv_id int;
begin
insert into amapp.am_tag_value (value)
    values ('$link') returning tag_value_id INTO v_tv_id;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||v_tv_id,'AM_TAG_VALUE',$linktag);
end;
/
";
    close DOSQL;
    print STDERR "Add tag for study link\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

#############################
# Add tag: number of subjects
#############################

if($ispostgres) {
    open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
    print DOSQL "set search_path = amapp, pg_catalog;

with tagvalue as (insert into amapp.am_tag_value (value)
    values ('$subjects') returning tag_value_id)

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||(select tag_value_id from tagvalue),'AM_TAG_VALUE',$subjectstag)
";
    close DOSQL;
    print STDERR "Add tag for number of subjects\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "declare
  v_tv_id int;
begin
insert into amapp.am_tag_value (value)
    values ('$subjects') returning tag_value_id INTO v_tv_id;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||v_tv_id,'AM_TAG_VALUE',$subjectstag);
end;
/
";
    close DOSQL;
    print STDERR "Add tag for number of subjects\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

############################
# Add tag: number of samples
############################

if($ispostgres) {
    open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
    print DOSQL "set search_path = amapp, pg_catalog;

with tagvalue as (insert into amapp.am_tag_value (value)
    values ('$samples') returning tag_value_id)

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||(select tag_value_id from tagvalue),'AM_TAG_VALUE',$samplestag)
";
    close DOSQL;
    print STDERR "Add tag for number of samples\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "declare
  v_tv_id int;
begin
insert into amapp.am_tag_value (value)
    values ('$samples') returning tag_value_id INTO v_tv_id;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||v_tv_id,'AM_TAG_VALUE',$samplestag);
end;
/
";
    close DOSQL;
    print STDERR "Add tag for number of samples\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

###################
# Add tag: organism
###################

foreach $organismcode (@organismcode) {
    if($ispostgres) {
	open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
	print DOSQL "set search_path = amapp, pg_catalog;

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','$organismcode','SPECIES',$organismtag)
";
	close DOSQL;
	print STDERR "Add tag for organism '$organismcode'\n";
	open(OUT, "psql.out") || die "No psql results found";
	while(<OUT>){
	    if(/^SET$/){next}
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    } else {
	open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

	print DOSQL "insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','$organismcode','SPECIES',$organismtag);
";
	close DOSQL;
	print STDERR "Add tag for organism '$organismcode'\n";
	open(OUT, "sqlplus.out") || die "No sqlplus results found";
	while(<OUT>){
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    }
}

#####################
# Add tag: start date
#####################

if($ispostgres) {
    open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
    print DOSQL "set search_path = amapp, pg_catalog;

with tagvalue as (insert into amapp.am_tag_value (value)
    values ('$startdate') returning tag_value_id)

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||(select tag_value_id from tagvalue),'AM_TAG_VALUE',$startdatetag)
";
    close DOSQL;
    print STDERR "Add tag for study start date\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "declare
  v_tv_id int;
begin
insert into amapp.am_tag_value (value)
    values ('$startdate') returning tag_value_id INTO v_tv_id;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||v_tv_id,'AM_TAG_VALUE',$startdatetag);
end;
/
";
    close DOSQL;
    print STDERR "Add tag for study start date\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

##########################
# Add tag: completion date
##########################

if($ispostgres) {
    open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
    print DOSQL "set search_path = amapp, pg_catalog;

with tagvalue as (insert into amapp.am_tag_value (value)
    values ('$completedate') returning tag_value_id)

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||(select tag_value_id from tagvalue),'AM_TAG_VALUE',$completedatetag)
";
    close DOSQL;
    print STDERR "Add tag for study completion date\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "declare
  v_tv_id int;
begin
insert into amapp.am_tag_value (value)
    values ('$completedate') returning tag_value_id INTO v_tv_id;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||v_tv_id,'AM_TAG_VALUE',$completedatetag);
end;
/
";
    close DOSQL;
    print STDERR "Add tag for study completion date\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

####################
# Add tag: pubmed id
####################

if($ispostgres) {
    open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
    print DOSQL "set search_path = amapp, pg_catalog;

with tagvalue as (insert into amapp.am_tag_value (value)
    values ('$pubmed') returning tag_value_id)

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||(select tag_value_id from tagvalue),'AM_TAG_VALUE',$pubmedtag)
";
    close DOSQL;
    print STDERR "Add tag for pubmed ID\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "declare
  v_tv_id int;
begin
insert into amapp.am_tag_value (value)
    values ('$pubmed') returning tag_value_id INTO v_tv_id;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||v_tv_id,'AM_TAG_VALUE',$pubmedtag);
end;
/
";
    close DOSQL;
    print STDERR "Add tag for pubmed ID\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

##################
# Add tag: pub DOI
##################

if($ispostgres) {
    open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
    print DOSQL "set search_path = amapp, pg_catalog;

with tagvalue as (insert into amapp.am_tag_value (value)
    values ('$doi') returning tag_value_id)

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||(select tag_value_id from tagvalue),'AM_TAG_VALUE',$doitag)
";
    close DOSQL;
    print STDERR "Add tag for DOI\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "declare
  v_tv_id int;
begin
insert into amapp.am_tag_value (value)
    values ('$doi') returning tag_value_id INTO v_tv_id;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||v_tv_id,'AM_TAG_VALUE',$doitag);
end;
/
";
    close DOSQL;
    print STDERR "Add tag for DOI\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

#######################
# Add tag: pub Citation
#######################

if($ispostgres) {
    open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
    print DOSQL "set search_path = amapp, pg_catalog;

with tagvalue as (insert into amapp.am_tag_value (value)
    values ('$citation') returning tag_value_id)

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||(select tag_value_id from tagvalue),'AM_TAG_VALUE',$citationtag)
";
    close DOSQL;
    print STDERR "Add tag for citation\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "declare
  v_tv_id int;
begin
insert into amapp.am_tag_value (value)
    values ('$doi') returning tag_value_id INTO v_tv_id;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||v_tv_id,'AM_TAG_VALUE',$doitag);
end;
/
";
    close DOSQL;
    print STDERR "Add tag for citation\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

#########################
# Add tag: pub authorlist
#########################

if($ispostgres) {
    open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
    print DOSQL "set search_path = amapp, pg_catalog;

with tagvalue as (insert into amapp.am_tag_value (value)
    values ('$authors') returning tag_value_id)

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||(select tag_value_id from tagvalue),'AM_TAG_VALUE',$authorstag)
";
    close DOSQL;
    print STDERR "Add tag for authorlist\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "declare
  v_tv_id int;
begin
insert into amapp.am_tag_value (value)
    values ('$authors') returning tag_value_id INTO v_tv_id;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||v_tv_id,'AM_TAG_VALUE',$authorstag);
end;
/
";
    close DOSQL;
    print STDERR "Add tag for authorlist\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

####################
# Add tag: pub title
####################

if($ispostgres) {
   open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
   print DOSQL "set search_path = amapp, pg_catalog;

with tagvalue as (insert into amapp.am_tag_value (value)
    values ('$pubtitle') returning tag_value_id)

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||(select tag_value_id from tagvalue),'AM_TAG_VALUE',$titletag)
";
    close DOSQL;
    print STDERR "Add tag for publication title\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "declare
  v_tv_id int;
begin
insert into amapp.am_tag_value (value)
    values ('$pubtitle') returning tag_value_id INTO v_tv_id;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||v_tv_id,'AM_TAG_VALUE',$titletag);
end;
/
";
    close DOSQL;
    print STDERR "Add tag for publication title\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

################
# Add tag: roles
################

if($ispostgres) {
    open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
    print DOSQL "set search_path = amapp, pg_catalog;

with tagvalue as (insert into amapp.am_tag_value (value)
    values ('$roles') returning tag_value_id)

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||(select tag_value_id from tagvalue),'AM_TAG_VALUE',$rolestag)
";
    close DOSQL;
    print STDERR "Add tag for roles\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "declare
  v_tv_id int;
begin
insert into amapp.am_tag_value (value)
    values ('$roles') returning tag_value_id INTO v_tv_id;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||v_tv_id,'AM_TAG_VALUE',$rolestag);
end;
/
";
    close DOSQL;
    print STDERR "Add tag for roles\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

##################
# Add tag: address
##################

if($ispostgres) {
    open (DOSQL, "|psql > psql.out") || die "Failed to run psql";
    print DOSQL "set search_path = amapp, pg_catalog;

with tagvalue as (insert into amapp.am_tag_value (value)
    values ('$address') returning tag_value_id)

insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||(select tag_value_id from tagvalue),'AM_TAG_VALUE',$addresstag)
";
    close DOSQL;
    print STDERR "Add tag for address\n";
    open(OUT, "psql.out") || die "No psql results found";
    while(<OUT>){
	if(/^SET$/){next}
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
} else {
    open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";

    print DOSQL "declare
  v_tv_id int;
begin
insert into amapp.am_tag_value (value)
    values ('$address') returning tag_value_id INTO v_tv_id;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
    values('FOL:$studyid','TAG:'||v_tv_id,'AM_TAG_VALUE',$addresstag);
end;
/
";
    close DOSQL;
    print STDERR "Add tag for address\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

