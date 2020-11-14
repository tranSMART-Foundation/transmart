#!/usr/bin/perl -w

###############################################################################
#
# Add Browse tab data for a program
#
# Data is defined in a file with prefix: value on each line
#
# For a program, a couple of extra values are needed
#
# Data needed: Items in data/common/amapp/am_tag_item.tsv with:
# col 1: tag_template_id = 1
# col 3: required = 1
#
# code_type_name for required items:
#
# PROGRAM_IDENTIFIER (CUSTOM FREETEXT)
# PROGRAM_TITLE (FIXED FREETEXT) folderName
# PROGRAM_DESCRIPTION (FIXED FREETEXTAREA) description
# PROGRAM_TARGET_PATHWAY_PHENOTYPE (PROGRAM_TARGET MULTIPICKLIST) [programTarget after title]
# THERAPEUTIC_DOMAIN (CUSTOM MULTIPICKLIST)
#
###############################################################################

# todo

# error if any tag is not found in findTag ... message in findTag
# report empty values at end in case they can be filled
# add to summary of issues to be resolved

if(!defined($ARGV[0])){
    print STDERR "Usage: browse-add-program.pl input-filename\n";
    exit;
}

$dovalidate=0;
$dodebug=0;
$ispostgres=1;
$iarg=0;
foreach $arg (@ARGV) {
    if(!$iarg++) {
	$infile = $ARGV[0];
    }
    else {
	if($arg eq "-validate") {$dovalidate = 1}
	elsif($arg eq "-debug") {$dodebug = 1}
	elsif($arg eq "-oracle") {$ispostgres = 0}
	elsif($arg eq "-postgres") {$ispostgres = 1}
	else {
	    print "Unknown argument $arg\n";
	    exit;
	}
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
#    print STDERR "$sqlplus\n";
}

%linetypes = ("Program" => 1,
	      "Description" => 2,
	      "Target" => 2,
	      "Domain" => 2
    );

$description = "";

open(IN,"$infile") || die "Failed to open $infile";
@input = ();
@linetype = ();
$line=0;
while (<IN>) {
    chomp;
    if(/^[\#]/) {next}
    if(/^\s*$/) {next}
    if(/^([A-Z][a-z]+):\s+(.*)/){
	++$line;
#	print "Read line $line: $_\n";
	$type = $1;
	$data = $2;
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
#    print STDERR "Found line $i: $linetype[$i]\n";
    $type = $linetype[$i];
    $data = $input[$i];
    
    $required{$type}++;
    if($type eq "Program"){
	$program = $data;
    }
    elsif($type eq "Description"){
	if($description ne "") {
	    if($description !~ /[.]$/) {$description .= "."}
	    $description .= "<br/><br/>"}
	$description .= $data;
    }
    elsif($type eq "Target"){
	push @target, $data;
    }
    elsif($type eq "Domain"){
	push @domain, $data;
    }
    else {
	print STDERR "Unknown line type: $type\n";
	exit;
    }
}

foreach $t (sort(keys(%required))){
    if(!$required{$t}){
	print STDERR "Error: Line type '$t' not found\n";
    }
    elsif($linetypes{$t}==1 && $required{$t}>1){
	print STDERR "Error: Line type '$t' found $required{$t} times\n";
    }
}

sub findFolder($$$) {
    my ($type,$folder,$level) = (@_);
    if($ispostgres) {
	if($dodebug) {print "psql -A -t -c \"select folder_id,folder_type from fmapp.fm_folder where folder_level = $level and folder_name = '$folder'\"\n"}
	open(PSQL, "psql -A -t -c \"select folder_id,folder_type from fmapp.fm_folder where folder_level = $level and folder_name = '$folder'\"|") || die "Failed to start psql";
	while(<PSQL>){
	    if($dodebug){print}
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		if($col[1] ne $type) {print STDERR "type '$col[1]'\n";last}
		return $col[0];
	    }
	}
	close PSQL;
	if($dodebug){print "\n"}
    }
    else {
	if($dodebug){print "echo \"select folder_id,folder_type from fmapp.fm_folder where folder_level = $level and folder_name = '$folder';\"  | $sqlplus\n"}
	open(OSQL, "echo \"select folder_id,folder_type from fmapp.fm_folder where folder_level = $level and folder_name = '$folder';\" | $sqlplus|") || die "Failed to start sqlplus";
	while(<OSQL>){
	    if($dodebug){print}
	    chomp;
	    s/^\s+//;
	    if(/[|]/) {
		@col = split (/[|]/);
		if($col[1] ne $type) {print STDERR "type '$col[1]'\n";last}
		return $col[0];
	    }
	}
	close OSQL;
	if($dodebug){print "\n"}
    }
    return 0;
}

sub findTemplate($) {
    my ($type) = (@_);
    if($ispostgres) {
	if($dodebug) {print "psql -A -t -c \"select tag_template_id,tag_template_type from amapp.am_tag_template where tag_template_type = '$type'\"\n"}
	open(PSQL, "psql -A -t -c \"select tag_template_id,tag_template_type from amapp.am_tag_template where tag_template_type = '$type'\"|") || die "Failed to start psql";
	while(<PSQL>){
	    if($dodebug){print}
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		if($col[1] ne $type) {print STDERR "type '$col[1]'\n";last}
		return $col[0];
	    }
	}
	close PSQL;
	if($dodebug){print "\n"}
    } else {
	if($dodebug){print "echo \"select tag_template_id,tag_template_type from amapp.am_tag_template where tag_template_type = '$type';\" | $sqlplus\n"}
	open(OSQL, "echo \"select tag_template_id,tag_template_type from amapp.am_tag_template where tag_template_type = '$type';\" | $sqlplus|") || die "Failed to start sqlplus";
	while(<OSQL>){
	    if($dodebug){print}
	    chomp;
	    s/^\s+//;
	    if(/[|]/) {
		@col = split (/[|]/);
		if($col[1] ne $type) {print STDERR "type '$col[1]'\n";last}
		return $col[0];
	    }
	}
	close OSQL;
	if($dodebug){print "\n"}
    }
    return 0;
}

sub findDisease($) {
    my ($disease) = (@_);
    if($ispostgres) {
	if($dodebug){print "psql -A -t -c \"select bio_data_id, unique_id from biomart.bio_data_uid where bio_data_id = (select bio_disease_id from biomart.bio_disease where (disease = '$disease' or mesh_code = '$disease' or icd9_code = '$disease' or icd10_code = '$disease'))\"\n"}
	open(PSQL, "psql -A -t -c \"select bio_data_id, unique_id from biomart.bio_data_uid where bio_data_id = (select bio_disease_id from biomart.bio_disease where (disease = '$disease' or mesh_code = '$disease' or icd9_code = '$disease' or icd10_code = '$disease'))\"|") || die "Failed to start psql";
	while(<PSQL>){
	    if($dodebug){print}
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		return @col;
	    }
	}
	close PSQL;
	if($dodebug){print "\n"}
    } else {
	if($dodebug){print "echo \"select bio_data_id, unique_id from biomart.bio_data_uid where bio_data_id = (select bio_disease_id from biomart.bio_disease where (disease = '$disease' or mesh_code = '$disease' or icd9_code = '$disease' or icd10_code = '$disease'));\" | $sqlplus\n"}
	open(OSQL, "echo \"select bio_data_id, unique_id from biomart.bio_data_uid where bio_data_id = (select bio_disease_id from biomart.bio_disease where (disease = '$disease' or mesh_code = '$disease' or icd9_code = '$disease' or icd10_code = '$disease'));\" | $sqlplus|") || die "Failed to start sqlplus";
	while(<OSQL>){
	    if($dodebug){print}
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		return @col;
	    }
	}
	close OSQL;
	if($dodebug){print "\n"}
    }
    return ();
}

sub findConcept($$) {
    my ($concept,$type) = (@_);
    if($ispostgres) {
	if($dodebug){print "psql -A -t -c \"select bio_data_id, unique_id from biomart.bio_data_uid where bio_data_id = (select bio_concept_code_id from biomart.bio_concept_code where (code_name = '$concept' or bio_concept_code = '$concept') and code_type_name = '$type')\"\n"}
	open(PSQL, "psql -A -t -c \"select bio_data_id, unique_id from biomart.bio_data_uid where bio_data_id = (select bio_concept_code_id from biomart.bio_concept_code where (code_name = '$concept' or bio_concept_code = '$concept') and code_type_name = '$type')\"|") || die "Failed to start psql";
	while(<PSQL>){
	    if($dodebug){print}
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		return @col;
	    }
	}
	close PSQL;
	if($dodebug){print "\n"}
    } else {
	if($dodebug){print "echo \"select bio_data_id, unique_id from biomart.bio_data_uid where bio_data_id = (select bio_concept_code_id from biomart.bio_concept_code where (code_name = '$concept' or bio_concept_code = '$concept') and code_type_name = '$type');\" | $sqlplus\n"}
	open(OSQL, "echo \"select bio_data_id, unique_id from biomart.bio_data_uid where bio_data_id = (select bio_concept_code_id from biomart.bio_concept_code where (code_name = '$concept' or bio_concept_code = '$concept') and code_type_name = '$type');\" | $sqlplus|") || die "Failed to start sqplus";
	while(<OSQL>){
	    if($dodebug){print}
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		return @col;
	    }
	}
	close OSQL;
	if($dodebug){print "\n"}
    }
    return ();
}

sub findTag($) {
    my ($type) = (@_);
    if($ispostgres) {
	if($dodebug){print "psql -A -t -c \"select tag_item_id,code_type_name from amapp.am_tag_item where code_type_name = '$type'\"\n"}
	open(PSQL, "psql -A -t -c \"select tag_item_id,code_type_name from amapp.am_tag_item where code_type_name = '$type'\"|") || die "Failed to start psql";
	while(<PSQL>){
	    if($dodebug){print}
	    chomp;
	    if(/[|]/) {
		@col = split (/[|]/);
		return $col[0];
	    }
	}
	close PSQL;
	if($dodebug){print "\n"}
    } else {
	if($dodebug){print "echo \"select tag_item_id,code_type_name from amapp.am_tag_item where code_type_name = '$type';\" | $sqlplus\n"}
	open(OSQL, "echo \"select tag_item_id,code_type_name from amapp.am_tag_item where code_type_name = '$type';\" | $sqlplus|") || die "Failed to start sqplus";
	while(<OSQL>){
	    if($dodebug){print}
	    chomp;
	    s/^\s+//;
	    if(/[|]/) {
	    @col = split (/[|]/);
	    return $col[0];
	    }
	}
	close OSQL;
	if($dodebug){print "\n"}
    }
    return 0;
}

$programid = findFolder("PROGRAM", $program, 0);

if(!$programid) {print STDERR "program '$program' not found, creating...\n";}
else {print STDERR "program '$program' found with ID $programid\n";exit}

if($dovalidate) {
    print STDERR "validation completed\n";
    exit;
}

$targettag = findTag("PROGRAM_TARGET_PATHWAY_PHENOTYPE");
print STDERR "Target tagid $targettag\n";

@targetcode = ();
foreach $target (@target){
    ($id, $code) = findDisease($target);

    if(!defined($code)){print STDERR "Disease target '$target' not found\n";exit}
    else {
	print STDERR "Target '$target' id '$id', code '$code'\n";
	push @targetcode, $code;
    }
}


$domaintag = findTag("THERAPEUTIC_DOMAIN");
print STDERR "Domain tagid $domaintag\n";

@domaincode = ();
foreach $domain (@domain){
    ($id, $code) = findConcept($domain,"THERAPEUTIC_DOMAIN");

    if(!defined($code)){print STDERR "Domain '$domain' not found\n";exit}
    else {
	print STDERR "Domain '$domain' id '$id', code '$code'\n";
	push @domaincode, $code;
    }
}



      
# Add new program

if(!$programid) {
    if($ispostgres) {
	if($dodebug){print "psql -c \"set search_path = fmapp, pg_catalog;

insert into fmapp.fm_folder (folder_name,parent_id,folder_level,folder_type,active_ind,description)
       values ('$program',NULL,0,'PROGRAM',true,E'$description');
\"\n"}
	$psqlstatus = system "psql -c \"set search_path = fmapp, pg_catalog;

insert into fmapp.fm_folder (folder_name,parent_id,folder_level,folder_type,active_ind,description)
       values ('$program',NULL,0,'PROGRAM',true,E'$description');
\"";
	if($psqlstatus) {
	    print STDERR "Create program failed: status $psqlstatus\n";
	}
    } else {
	open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";
	if($dodebug){print "insert into fmapp.fm_folder (folder_name,parent_id,folder_level,folder_type,active_ind,description)
values ('$program',NULL,0,'PROGRAM','1','$description');
\n"}
	print DOSQL "insert into fmapp.fm_folder (folder_name,parent_id,folder_level,folder_type,active_ind,description)
values ('$program',NULL,0,'PROGRAM','1','$description');
";
	close DOSQL;
	print STDERR "Create program\n";
	open(OUT, "sqlplus.out") || die "No sqlplus results found";
	while(<OUT>){
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    }

    $programid = findFolder("PROGRAM", $program, 0);
    print STDERR "Created program with ID '$programid'\n";
}

if($ispostgres) {

    if($dodebug){print "psql -c \"set search_path = amapp, pg_catalog;

insert into amapp.am_tag_template_association (tag_template_id,object_uid)
       select (select tag_template_id from amapp.am_tag_template where tag_template_name = 'Program'),
              (select 'FOL:'||folder_id from fmapp.fm_folder where folder_name = '$program')
              where not exists (select NULL from amapp.am_tag_template_association where object_uid = 
                      (select 'FOL:'||folder_id from fmapp.fm_folder where folder_name = '$program'));
    \"\n"}
       $psqlstatus = system "psql -c \"set search_path = amapp, pg_catalog;

insert into amapp.am_tag_template_association (tag_template_id,object_uid)
       select (select tag_template_id from amapp.am_tag_template where tag_template_name = 'Program'),
              (select 'FOL:'||folder_id from fmapp.fm_folder where folder_name = '$program')
              where not exists (select NULL from amapp.am_tag_template_association where object_uid = 
                      (select 'FOL:'||folder_id from fmapp.fm_folder where folder_name = '$program'));
    \"";
    if($psqlstatus) {
	print STDERR "Insert am_tag_template_association failed: status $psqlstatus";
    }
} else {
    open(DOSQL, "|$sqlplus > sqlplus.out") || die "Cannot run sqlplus";
    if($dodebug){print "insert into amapp.am_tag_template_association (tag_template_id,object_uid)
       select (select tag_template_id from amapp.am_tag_template where tag_template_name = 'Program'),
              (select 'FOL:'||folder_id from fmapp.fm_folder where folder_name = '$program')
              from DUAL where not exists (select NULL from amapp.am_tag_template_association where object_uid = 
                      (select 'FOL:'||folder_id from fmapp.fm_folder where folder_name = '$program'));
\n"}
    print DOSQL "insert into amapp.am_tag_template_association (tag_template_id,object_uid)
       select (select tag_template_id from amapp.am_tag_template where tag_template_name = 'Program'),
              (select 'FOL:'||folder_id from fmapp.fm_folder where folder_name = '$program')
              from DUAL where not exists (select NULL from amapp.am_tag_template_association where object_uid = 
                      (select 'FOL:'||folder_id from fmapp.fm_folder where folder_name = '$program'));
";
    close DOSQL;
    print STDERR "Insert am_tag_template_association\n";
    open(OUT, "sqlplus.out") || die "No sqlplus results found";
    while(<OUT>){
	if(/\S/) {print "OUT: $_";}
    }
    close OUT;
}

foreach $targetcode (@targetcode) {
    if($ispostgres) {
	if($dodebug){print "psql -c \"set search_path = amapp, pg_catalog;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
values('FOL:$programid','$targetcode','PROGRAM_TARGET',$targettag)\"\n"}
	$psqlstatus = system "psql -c \"set search_path = amapp, pg_catalog;
insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
values('FOL:$programid','$targetcode','PROGRAM_TARGET',$targettag)\"";

	if($psqlstatus) {
	    print STDERR "Add tag for program target '$targetcode' failed: status $psqlstatus\n";
	}
    } else {
	open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";
	if($dodebug){print "insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
values('FOL:$programid','$targetcode','PROGRAM_TARGET',$targettag);
\n"}
	print DOSQL "insert into amapp.am_tag_association (subject_uid, object_uid, object_type, tag_item_id)
values('FOL:$programid','$targetcode','PROGRAM_TARGET',$targettag);
";
	close DOSQL;
	print STDERR "Add tag for program target '$targetcode'\n";
	open(OUT, "sqlplus.out") || die "No sqlplus results found";
	while(<OUT>){
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    }
}

foreach $domaincode (@domaincode) {
    if($ispostgres) {
	if($dodebug){print "psql -c \"set search_path = amapp, pg_catalog;

insert into amapp.am_tag_association (subject_uid,object_uid,object_type,tag_item_id)
       values ('FOL:'||$programid,
              '$domaincode',
              'BIO_CONCEPT_CODE',
              '$domaintag');
\"\n"}
	$psqlstatus = system "psql -c \"set search_path = amapp, pg_catalog;

insert into amapp.am_tag_association (subject_uid,object_uid,object_type,tag_item_id)
       values ('FOL:'||$programid,
              '$domaincode',
              'BIO_CONCEPT_CODE',
              '$domaintag');
\"";

	if($psqlstatus) {
	    print STDERR "Add tag for therapeutic domain '$domaincode' failed: status $psqlstatus\n";
	}
    } else {
	open (DOSQL, "|$sqlplus > sqlplus.out") || die "Failed to run sqlplus";
	if($dodebug){print "insert into amapp.am_tag_association (subject_uid,object_uid,object_type,tag_item_id)
       values ('FOL:'||$programid,
              '$domaincode',
              'BIO_CONCEPT_CODE',
              '$domaintag');
\n"}
	print DOSQL "insert into amapp.am_tag_association (subject_uid,object_uid,object_type,tag_item_id)
       values ('FOL:'||$programid,
              '$domaincode',
              'BIO_CONCEPT_CODE',
              '$domaintag');
";
	close DOSQL;
	print STDERR "Add tag for therapeutic domain '$domaincode'\n";
	open(OUT, "sqlplus.out") || die "No sqlplus results found";
	while(<OUT>){
	    if(/\S/) {print "OUT: $_";}
	}
	close OUT;
    }
}
