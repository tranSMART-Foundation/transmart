#!/usr/bin/perl -w

# /data/scratch/git-master/transmart/transmart-data/ddl/comparison/ora-pg-compare.pl > x.x 2> y.y
# i2b2 sqlserver and oracle have FUNCTION and PROCEDURE definitions together - merge

##################################################
#
# Need to capture and compare function arguments
# Need to catch "CREATE UNIQUE INDEX" and compare
#
##################################################

##################################################
#
# Parsing i2b2 *data ... postgres looks simpler to do
# procedures may need to do both?
# data_build.xml parse transaction src= db_type = postgresql oracle sqlserver
# db.properties sets details for Oracle by default
# scripts/
#  clean* to clear data
#   *create*tables* with dbtype
#   *insert_data general or 3 separate
# *triggers*
# dbtype directory
#    dbtype-specific insert_data CrcData, Imdata
# procedures/dbtype directory
# stored procedures/functions
#
# note: archive_observation_fact is a copy of observation_fact plus an extra ID
# # note: create* may end with INSERT .... VALUES ... ; (2 lines each time)
# count #inserts for each table
#
##################################################

##################################################
#
# postgres serial column: add sequence and trigger
#
# compare 3 index types - Prikey UniKey Forkey
# sorted lists for tables
#
# review checks vs totals for any other oddities
# Note lists to be added - functions etc.
#
# Map triggers with added trg_ prefix and flag as OK
#
##################################################

use Cwd;

$showSame = 1;
foreach $arg (@ARGV) {
    if($arg eq "-nosame") {$showSame = 0}
}

%dodir = ("amapp" => "",
	  "fmapp" => "",
	  "biomart" => "",
	  "deapp" => "",
	  "searchapp" => "",
	  "tm_cz" => "",
	  "tm_lz" => "",
	  "tm_wz" => "",
	  "galaxy" => "",
	  "gwas_plink" => "",
	  "biomart_user" => "",
	  "biomart_stage" => "",
	  "ts_batch" => "",
	  "i2b2demodata" => "",
	  "i2b2hive" => "",
	  "i2b2imdata" => "",
	  "i2b2metadata" => "",
	  "i2b2pm" => "",
	  "i2b2workdata" => "",
	  "_scripts" => "",
	  "GLOBAL" => ""
);

%dopdir = ("META" => "",
	   "support" => "",
	   "macroed_functions" => ""
);

%doiodir = ("scripts" => "",
	    "demo" => "",
	    "oracle" => "",
	    "procedures" => "",
	    "Crcdata" => "i2b2demodata",
	    "Hivedata" => "i2b2hive",
	    "Imdata" => "i2b2imdata",
	    "Metadata" => "i2b2metadata",
	    "Pmdata" => "i2b2pm",
	    "Workdata" => "i2b2workdata"
);
%doipdir = ("scripts" => "",
	    "demo" => "",
	    "postgresql" => "",
	    "procedures" => "",
	    "Crcdata" => "i2b2demodata",
	    "Hivedata" => "i2b2hive",
	    "Imdata" => "i2b2imdata",
	    "Metadata" => "i2b2metadata",
	    "Pmdata" => "i2b2pm",
	    "Workdata" => "i2b2workdata"
);
%doisdir = ("scripts" => "",
	    "demo" => "",
	    "sqlserver" => "",
	    "procedures" => "",
	    "Crcdata" => "i2b2demodata",
	    "Hivedata" => "i2b2hive",
	    "Imdata" => "i2b2imdata",
	    "Metadata" => "i2b2metadata",
	    "Pmdata" => "i2b2pm",
	    "Workdata" => "i2b2workdata"
);

%ioparsed = ("" => 0);
%ipparsed = ("" => 0);
%isparsed = ("" => 0);
%oparsed = ("" => 0);
%pparsed = ("" => 0);

%iounparsed = ();
%ipunparsed = ();
%isunparsed = ();
%ounparsed = ();
%punparsed = ();

# tables and the files they are defined in
%ioTableFile = ();
%ipTableFile = ();
%isTableFile = ();
%oTableFile = ();
%pTableFile = ();

# triggers and the files they are defined in
%ioTriggerFile = ();
%ipTriggerFile = ();
%isTriggerFile = ();
%oTriggerFile = ();
%pTriggerFile = ();

# sequences and the files they are defined in
%ioSequenceFile = ();
%ipSequenceFile = ();
%isSequenceFile = ();
%oSequenceFile = ();
%pSequenceFile = ();

# functions and the files they are defined in
%ioFunctionFile = ();
%ipFunctionFile = ();
%isFunctionFile = ();
%oFunctionFile = ();
%pFunctionFile = ();

# functions and the type they return
%ioFunctionReturn = ();
%ipFunctionReturn = ();
%isFunctionReturn = ();
%oFunctionReturn = ();
%pFunctionReturn = ();

# procedures and the files they are defined in (not postgres)
%ioProcFile = ();
%isProcFile = ();
%oProcFile = ();

# views and the files they are defined in
%ioViewFile = ();
%ipViewFile = ();
%isViewFile = ();
%oViewFile = ();
%pViewFile = ();

# tables and their schemas
%ioTableColumn = ();
%ipTableColumn = ();
%isTableColumn = ();
%oTableColumn = ();
%pTableColumn = ();
%ioTableForkey = ();
%ipTableForkey = ();
%isTableForkey = ();
%oTableForkey = ();
%pTableForkey = ();


sub compareForkey($$$$) {
    my ($keya,$keyb,$table,$compare) = @_;
    if($keya eq $keyb) {return 1}
    my @keya = split(/;/,$keya);
    my @keyb = split(/;/,$keyb);
    my %keya = ();
    my %keyb = ();
    my $k;
    my $id;
    my $rest;

    # global text message to flag reason for rejection
    
    $compareForkey = "";

    foreach $k (@keya) {
	if($k =~ /^(.*)([\(].*[\)] .*[\(].*[\)])/g){
	    $id = $1;
	    $rest = $2;
	    $keya{$id} = $rest;
	} else {
	    $compareForkey = "Cannot parse keya: '$k' $table $compare";
	    return 0;
	}
    }
    foreach $k (@keyb) {
	if($k =~ /^(.*)([\(].*[\)] .*[\(].*[\)])/g){
	    $id = $1;
	    $rest = $2;
	    $keyb{$id} = $rest;
	} else {
	    $compareForkey =  "Cannot parse keyb: '$k'\ $table $compare";
	    return 0;
	}
    }
    foreach $k(sort(keys(%keya))) {
	if(!defined($keyb{$k})){
	    $compareForkey = "$k defined for first set only: $keya{$k} for $compare";
	    return 0;
	}
	if($keya{$k} ne $keyb{$k}){
	    $compareForkey = "$k differs; $keya{$k} $keyb{$k}";
	    return 0;
	}
    }
    foreach $k(sort(keys(%keyb))) {
	if(!defined($keyb{$k})){
	    $compareForkey = "$k defined for second set only: $keyb{$k} for $compare";
	    return 0;
	}
    }
    return 1;
}

sub parseI2b2Properties($$){
    my ($d,$f) = @_;
    local *IPROP;

    $ischema = "unknown";

    open(IPROP, "$d/$f") || die "failed to open $d/$f";
    while(<IPROP>){
	if(/^db.username=(\S+)/) {$ischema = uc($1)}
    }
    close IPROP;
#    print "parseI2b2Properties $d\n";
    return;
}

sub i2b2OracleParsed($$){
    my ($d,$f) = @_;
    $ioparsed{$f}++; 
}

sub i2b2PostgresParsed($$){
    my ($d,$f) = @_;
    $ipparsed{$f}++; 
}

sub i2b2SqlserverParsed($$){
    my ($d,$f) = @_;
    $isparsed{$f}++; 
}

sub oracleParsed($$){
    my ($d,$f) = @_;
    $oparsed{$f}++; 
}

sub postgresParsed($$){
    my ($d,$f) = @_;
    $pparsed{$f}++; 
}

sub i2b2OracleUnparsed($$){
    my ($d,$f) = @_;
    $iounparsed{$f} .= "$d;"; 
}

sub i2b2PostgresUnparsed($$){
    my ($d,$f) = @_;
    $ipunparsed{$f} .= "$d;"; 
}

sub i2b2SqlserverUnparsed($$){
    my ($d,$f) = @_;
    $isunparsed{$f} .= "$d;"; 
}

sub oracleUnparsed($$){
    my ($d,$f) = @_;
    $ounparsed{$f} .= "$d;"; 
}

sub postgresUnparsed($$){
    my ($d,$f) = @_;
    $punparsed{$f} .= "$d;"; 
}

sub parseOracleTop($$){
    my ($d,$f) = @_;
    local *IN;
    my $err = 0;
    my @f;

    if($f eq "Makefile") {
    }
    elsif($f eq "grants.php") {
    }
    elsif($f eq "synonyms.php") {
    }
    elsif($f eq "drop_users.php") {
    }
    elsif($f eq "create_tablespaces.php") {
    }
    elsif($f eq "drop_tablespaces.sql") {
    }
    else {
	print "Oracle parse $d/$f\n";
	return 1;
    }
    oracleUnparsed("$d/$f",$f);
    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
    while(<IN>) {
    }
    close IN;

    return $err;
}

sub parseOracleFunctions($){
    my ($d) = @_;
    local *OSDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my $subd = $d;

    $subd =~ s/^$oplus\///g;

    opendir(OSDIR,"$d") || die "parseOracleFunctions failed to open $d";

    while($f = readdir(OSDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if($f =~ /_bk[.]sql$/) {next}
	if($f =~ /_gsk[.]sql$/) {next}
	if(-d "$dir$d/$f") {
	    print "OracleFunctions subdir $d/$f\n";
	    next;
	}
	if($f =~ /[.]sql$/) {
#	    print "Oracle parse $d/$f\n";
	    $orsql{"$subd/$f"}++;

	    oracleParsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		if(/^\s*(.*\S)\s+(FUNCTION|function)\s+([^.]+)[.](\S+)/) {
		    $fuse = $1;
		    $schema = $3;
		    $func = $4;
		    $fuse = uc($fuse);
		    $schema = uc($schema);
		    $func = uc($func);
		    $schema =~ s/\"//g;
		    $func =~ s/\"//g;
		    if($fuse =~ /^CREATE/) {
			$oFunctionFile{"$schema.$func"} = "$d/$f";
			$cfunc = 1;
		    }
		    else {
			print STDERR "$d/$f unexpected function $func     $fuse\n";
		    }
		    if($cfunc && /RETURN (\S+) AS/) {
			$oFunctionReturn{"$schema.$func"} = $1;
		    }
		    if($cfunc && /^\s*[\)]/) {$cfunc = 0}
		}
	    }
	    close IN;
	}
    }
    closedir(OSDIR);
    return $err;
}

sub parseOracleProcedures($){
    my ($d) = @_;
    local *OSDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my $subd = $d;

    $subd =~ s/^$oplus\///g;

    opendir(OSDIR,"$d") || die "parseOracleProcedures failed to open $d";

    while($f = readdir(OSDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if($f =~ /_bk[.]sql$/) {next}
	if($f =~ /_gsk[.]sql$/) {next}
	if(-d "$dir$d/$f") {
	    print "Oracleprocedures subdir $d/$f\n";
	    next;
	}
	if($f =~ /[.]sql$/) {
#	    print "Oracle parse $d/$f\n";
	    $orsql{"$subd/$f"}++;

	    oracleParsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		if(/^\s*(.*\S)\s+(PROCEDURE|procedure)\s+([^.]+)[.](\S+)/) {
		    $puse = $1;
		    $schema = $3;
		    $proc = $4;
		    $puse = uc($puse);
		    $schema = uc($schema);
		    $proc = uc($proc);
		    $schema =~ s/\"//g;
		    $proc =~ s/\"//g;
		    if($puse =~ /^CREATE/) {
			$oProcFile{"$schema.$proc"} = "$d/$f";
			$cproc = 1;
		    }
		    else {
			print STDERR "$d/$f unexpected procedure $proc     $puse\n";
		    }
		}
	    }
	    close IN;
	}
    }
    closedir(OSDIR);
    return $err;
}

sub parseOracleViews($){
    my ($d) = @_;
    local *OSDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my $subd = $d;

    $subd =~ s/^$oplus\///g;

    opendir(OSDIR,"$d") || die "parseOracleViews failed to open $d";

    while($f = readdir(OSDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if($f =~ /_bk[.]sql$/) {next}
	if($f =~ /_gsk[.]sql$/) {next}
	if(-d "$dir$d/$f") {
	    print "OracleViews subdir $d/$f\n";
	    next;
	}
	if($f =~ /[.]sql$/) {
#	    print "Oracle parse $d/$f\n";
	    $orsql{"$subd/$f"}++;

	    oracleParsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		if(/^\s*(.*\S)\s+(VIEW|view)\s+([^.]+)[.](\S+)\s+(.*)/) {
		    $vuse = $1;
		    $schema = $3;
		    $view = $4;
		    $rest = $5;
		    $vuse = uc($vuse);
		    $schema = uc($schema);
		    $view = uc($view);
		    $rest = uc($rest);
		    $schema =~ s/\"//g;
		    $view =~ s/\"//g;
		    $rest =~ s/\"//g;
		    $rest =~ s/^\(//g;
		    $rest =~ s/\)$//g;
		    if($vuse =~ /^CREATE/) {
			$oViewFile{"$schema.$view"} = "$d/$f";
			$cview = 1;
		    }
		    elsif($vuse =~ /^COMMENT/) {#COMMENT ON VIEW - ignore
		    }
		    else {
			print STDERR "$d/$f unexpected view $view     $vuse     '$rest'\n";
		    }
		}
	    }
	    close IN;
	}
    }
    return $err;
}

sub parseOracleScripts($){
    my ($d) = @_;
    local *OSDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;

    opendir(OSDIR,"$d") || die "parseOracleScripts failed to open $d";

    while($f = readdir(OSDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    if($f eq "inc") {
	    }
	    else {
		print "OracleScripts subdir $d/$f\n";
	    }
	    next;
	}
	if($f =~ /[.]php$/) {
	}
	elsif($f =~ /[.]groovy$/){
	}
	else {
	    print "Oracle parse $d/$f\n";
	}
	oracleUnparsed("$d/$f",$f);
	open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	while(<IN>) {
	}
	close IN;
    }
    closedir(OSDIR);
    return $err;
}

sub parseOracle($){
    my ($d) = @_;
    local *OSDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my ($tuse,$schema,$table);
    my $subd = $d;

    $subd =~ s/^$oplus\///g;

    opendir(OSDIR,"$d") || die "parseOracle failed to open $d";

    while($f = readdir(OSDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if($f =~ /^[#]/) {next}
	if($f =~ /_bk[.]sql$/) {next}
	if($f =~ /_gsk[.]sql$/) {next}
	if(-d "$dir$d/$f") {
	    if($f eq "functions") {
		parseOracleFunctions("$d/$f");
	    }
	    elsif($f eq "procedures"){
		parseOracleProcedures("$d/$f");
	    }
	    elsif($f eq "views"){
		parseOracleViews("$d/$f");
	    }
	    else {
		print "Oracle subdir $d/$f\n";
	    }
	    next;
	}
	if($f =~ /[.]sql$/) {
	    if($f =~ /^_.*[.]sql$/){
		if($f eq "_cross.sql"){
		}
		elsif($f eq "_grants.sql"){
		}
		elsif($f eq "_misc.sql"){
		}
		elsif($f eq "_synonyms.sql"){
		}
		elsif($f eq "util_grant_all.sql"){
		}
		else {
		    print "Oracle parse $d/$f\n";
		    next;
		}
	    }
#           print "Oracle parse $d/$f\n";
	    if($f ne "_cross.sql" && $subd ne "GLOBAL"){
		$orsql{"$subd/$f"}++;
	    }

	    oracleParsed("$d/$f",$f);

	    $ctable = 0;
	    $ctrig  = 0;
	    $cfunc  = 0;
	    $cproc  = 0;
	    $cview  = 0;
	    $ctype  = 0;
	    $cseq = 0;
	    $tseq = "";
	    $forkey = 0;

	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		if($ctrig) {
		    if(/select ([^.]+)[.]nextval into :NEW[.]\"([^\"]+)\" from dual;/){
			$nid = $1;
			$ncol=$2;
			$oNextval{"$schema.$table"} = "$ncol.$nid";
			$oNexttrig{"$schema.$trig"} = "$schema.$table";
		    }
		    if(/ALTER TRIGGER \S+ ENABLE/) {$ctrig = 0}
		}
		if($forkey) {
		    if(/^\s+REFERENCES \"([^\"]+)\"[.]\"([^\"]+)\" \(\"([^\"]+)\"\) (ON DELETE CASCADE )?(EN|DIS)ABLE;/) {
			$pk = " ";
			$pk .= uc($1);
			$pk .= ".";
			$pk .= uc($2);
			$pk .= "(";
			$pk .= uc($3);
			$pk .= ");";
			$oTableForkey{"$schema.$table"} .= $pk;
		    }
		    else {
			print STDERR "$d/$f Unexpected foreign key format $d/$f: $_";
		    }
		    $forkey = 0;
		}
		if(/(\S+)\s+GLOBAL\s+TEMPORARY\s+(TABLE|table)\s+([^.]+)[.](\S+)/) {
		    $tuse = $1;
		    $schema = $3;
		    $table = $4;
		    $tuse = uc($tuse);
		    $schema = uc($schema);
		    $table = uc($table);
		    $schema =~ s/\"//g;
		    $table =~ s/\"//g;
		    if($tuse eq "CREATE") {
			$oTableFile{"$schema.$table"} = "$d/$f";
			$ctable = 1;
		    }
		    elsif($tuse eq "ALTER") {
			if(/CONSTRAINT\s+(\S+)\s+FOREIGN KEY (\([^\)]+\))/){
			    $pc = $1;
			    $pk = $2;
			    $pc =~ s/\"//g;
			    if(length($pc) > 31){print STDERR "Oracle constraint length ".length($pc)." '$pc'\n"}
			    $pfk = uc($pc).uc($pk);
			    $pfk =~ s/\"//g;
			    $oTableForkey{"$schema.$table"} .= $pfk;
			    $oForkey{"$schema.$pc"} .= $pfk;
			    $forkey=1;
			}
		    }

		}
		elsif(/(\S+)\s+(TABLE|table)\s+([^.]+)[.](\S+)/) {
		    $tuse = $1;
		    $schema = $3;
		    $table = $4;
		    $tuse = uc($tuse);
		    $schema = uc($schema);
		    $table = uc($table);
		    $schema =~ s/\"//g;
		    $table =~ s/\"//g;
		    if($tuse eq "CREATE") {
			$oTableFile{"$schema.$table"} = "$d/$f";
			$ctable = 1;
		    }
		    elsif($tuse eq "ALTER") {
			if(/CONSTRAINT\s+(\S+)\s+FOREIGN KEY (\([^\)]+\))/){
			    $pc = $1;
			    $pk = $2;
			    $pc =~ s/\"//g;
			    if(length($pc) > 31){print STDERR "Oracle constraint length ".length($pc)." '$pc'\n"}
			    $pfk = uc($pc).uc($pk);
			    $pfk =~ s/\"//g;
			    $oTableForkey{"$schema.$table"} .= $pfk;
			    $oForkey{"$schema.$pc"} .= $pfk;
			    $forkey=1;
			}
		    }

		}
		elsif($ctable) {
		    if(/;/){$ctable=0; next}
		    if(/^\s*\(/){s/^\s*\(\s*//}
		    if(/^\s*\)/){$ctable=2; s/^\s*\)\s*//}
		    if(/^\s*(\"\S+)\s+(.*?),?$/) {
			$col = $1;
			$cdef = $2;
			$cdef =~ s/,\s+$//g;
			$col =~ s/\"//g;
			$oTableColumn{"$schema.$table"} .= "$col $cdef;";
		    }
		    if(/^\s*(CONSTRAINT (\S+)\s+)?PRIMARY KEY \(([^\)]+)\)/){
			$pkc = $2;
			$pk = uc($3);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $oTablePrikeyName{"$schema.$table"} = $pkc;
			    $oTablePrikey{"$schema.$table"} = $pk;
			}
			elsif(defined($pk)){
			    print STDERR "++ Oracle primary key unnamed for $schema.$table\n";
			    $oTablePrikeyName{"$schema.$table"} = "unnamed"; # There is no name
			    $oTablePrikey{"$schema.$table"} = $pk;
			}
		    }
		    if(/^\s*(CONSTRAINT (\S+)\s+)?UNIQUE \(([^\)]+)\)/){
			$pkc = $2;
			$pk = uc($3);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $oTableUnikey{"$schema.$table"} .= "$pkc $pk;";
			}
			else {$oTableUnikey{"$schema.$table"} .= ". $pk;"}
		    }
		    if(/^\s*(CONSTRAINT (\S+)\s+)?FOREIGN KEY (\([^\)]+\))/){
			if(defined($1)) {$pk = uc($2).uc($3)}
			else{$pk = "unnamed ".uc($3)}
			$pk =~ s/\"//g;
			$oTableForkey{"$schema.$table"} .= $pk;
			$forkey=1;
		    }
		}

		if($cseq == 1 && /([^;]*)(;?)/) {
		    $tseq .= $1;
		    if(defined($2)) {$cseq = 2}
		}
		if(/^\s*(\S+)\s+BITMAP\s+(INDEX|index)\s+([^.]+)[.](\S+)\s+ON\s+([^.]+)[.]([^\( ]+)\s*\(([^\)]+)\)/) {
		    $iuse = $1;
		    $schema = $3;
		    $idx = $4;
		    $oschema = $5;
		    $itable=$6;
		    $icols=$7;
		    $schema =~ s/"//g;
		    $idx =~ s/"//g;
		    $idx = uc($idx);
		    $oschema =~ s/"//g;
		    $ischema =~ s/"//g;
		    $itable =~ s/"//g;
		    $icols =~ s/"//g;
		    $icols =~ s/\s//g;
		    if($iuse =~ /^CREATE/) {
			$oIndexFile{"$schema.$idx"} = "$d/$f";
			$oIndex{"$oschema.$itable"} .= "$schema.$idx($icols);";
		    }
		}
		elsif(/^\s*(\S+.*)\s+(INDEX|index)\s+([^.]+)[.](\S+)\s+ON\s+([^.]+)[.]([^\( ]+)\s*\(([^\)]+)\)/) {
		    $iuse = $1;
		    $schema = $3;
		    $idx = $4;
		    $oschema = $5;
		    $itable=$6;
		    $icols=$7;
		    $schema =~ s/"//g;
		    $idx =~ s/"//g;
		    $idx = uc($idx);
		    $oschema =~ s/"//g;
		    $ischema =~ s/"//g;
		    $itable =~ s/"//g;
		    $icols =~ s/"//g;
		    $icols =~ s/\s//g;
		    if($iuse =~ /^CREATE/) {
			$oIndexFile{"$schema.$idx"} = "$d/$f";
			$oIndex{"$oschema.$itable"} .= "$schema.$idx($icols);";
		    }
		}
		if(/^\s*(.*\S)\s+(SEQUENCE|sequence)\s+([^.]+)[.](\S+)([^;]*)([;]?)/) {
		    $suse = $1;
		    $schema = $3;
		    $seq = $4;
		    $rest = $5;
		    $cdone = $6;
		    $suse = uc($suse);
		    $schema = uc($schema);
		    $seq = uc($seq);
		    $schema =~ s/\"//g;
		    $seq =~ s/\"//g;
#		    print "$d/$f sequence $seq     $suse\n";
		    if($suse =~ /^CREATE/) {
			$oSequenceFile{"$schema.$seq"} = "$d/$f";
			$cseq = 1;
			if(defined($cdone)){$cseq = 2}
			$tseq = $rest;
		    }
		}
		if($cseq == 2){
		    $cseq = 0;
		    $oSequenceText{"$schema.$seq"} = $tseq;
		    $tseq = "";
		}

		if(/^\s*(.*\S)\s+(TRIGGER|trigger)\s+([^.]+)[.](\S+)/) {
		    $tuse = $1;
		    $schema = $3;
		    $trig = $4;
		    $tuse = uc($tuse);
		    $schema = uc($schema);
		    $trig = uc($trig);
		    $schema =~ s/\"//g;
		    $trig =~ s/\"//g;
#		    print "$d/$f trigger $trig     $tuse\n";
		    if($tuse =~ /^CREATE/) {
			$oTriggerFile{"$schema.$trig"} = "$d/$f";
			$ctrig = 1;
#			if($trig !~ /^TRG_/){
#			    print STDERR "ctrig set for $schema.$trig\n";
#			}
		    }
		}

		if(/^\s*(.*\S)\s+(VIEW|view)\s+([^.]+)[.](\S+)\s+(.*)/) {
		    $vuse = $1;
		    $schema = $3;
		    $view = $4;
		    $rest = $5;
		    $vuse = uc($vuse);
		    $schema = uc($schema);
		    $view = uc($view);
		    $rest = uc($rest);
		    $schema =~ s/\"//g;
		    $view =~ s/\"//g;
		    $rest =~ s/\"//g;
		    $rest =~ s/^\(//g;
		    $rest =~ s/\)$//g;
		    if($vuse =~ /^CREATE/) {
			$oViewFile{"$schema.$view"} = "$d/$f";
			$cview = 1;
		    }
		    elsif($vuse =~ /^COMMENT/) {#COMMENT ON VIEW - ignore
		    }
		    else {
			print STDERR "$d/$f unexpected view $view     $vuse     '$rest'\n";
		    }
		}
		if(/^\s*(.*\S)\s+(TYPE BODY|type body)\s+([^.]+)[.](\S+)\s+[AI]S\s+(.*)/) {
		    $tyuse = $1;
		    $schema = $3;
		    $type = $4;
		    $rest = $5;
		    $tyuse = uc($tyuse);
		    $schema = uc($schema);
		    $type = uc($type);
		    $rest = uc($rest);
		    $schema =~ s/\"//g;
		    $type =~ s/\"//g;
		    $rest =~ s/\"//g;
		    $rest =~ s/^\(//g;
		    $rest =~ s/\)$//g;
		    if($tyuse =~ /^CREATE/) {
			$oTypeFile{"$schema.$type"} = "$d/$f";
			$ctype = 1;
		    }
		    else {
			print STDERR "$d/$f unexpected type $type     $tyuse     '$rest'\n";
		    }
		}
		elsif(/^\s*(.*\S)\s+(TYPE|type)\s+([^.]+)[.](\S+)\s+[AI]S\s+(.*)/) {
		    $tyuse = $1;
		    $schema = $3;
		    $type = $4;
		    $rest = $5;
		    $tyuse = uc($tyuse);
		    $schema = uc($schema);
		    $type = uc($type);
		    $rest = uc($rest);
		    $schema =~ s/\"//g;
		    $type =~ s/\"//g;
		    $rest =~ s/\"//g;
		    $rest =~ s/^\(//g;
		    $rest =~ s/\)$//g;
		    if($tyuse =~ /^CREATE/) {
			$oTypeFile{"$schema.$type"} = "$d/$f";
			$ctype = 1;
		    }
		    else {
			print STDERR "$d/$f unexpected type $type     $tyuse     '$rest'\n";
		    }
		}
	    }
	    close IN;
	}
	elsif($f eq "items.json"){
#	    print "Oracle parse json $d/$f\n";
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    $itemText = "";
	    while(<IN>) {
		$itemText .= $_;
		if(/^\s+\"file\" : \"(\S+)\"/){
		    $ofile = $1;
		    if($ofile !~ /\/_cross[.]sql$/){
			$orload{"$ofile"}++;
		    }
		}
	    }
	    close IN;
	    ($itemsDepend, $itemsFiles) = ($itemText =~ /\"dependencies\" : [\[]( [\{].*[\}]) [\]],\s+\"fileAssignments\" : [\[]( [\{].*[\}]) [\]]\s+[\}]$/gosm);
	    if(!defined($itemsFiles)) {print STDERR "Failed to parse $dir$d/$f\n"}
	    else {
		print STDERR "Oracle parse json $d/$f itemsDepend ".length($itemsDepend)." itemsFiles ".length($itemsFiles)."\n";
		while($itemsFiles =~ /[\{]\s+\"item\" : [\{]\s+\"type\" : \"([^\"]+)\",\s+\"owner\" : \"([^\"]+)\",\s+\"name\" : \"([^\"]+)\"\s+[\}],\s+\"file\" : \"([^\"]+)\"\s+[\}]/gosm) {
		    $ijType = $1;
		    $ijOwner = $2;
		    $ijName = $3;
		    $ijFile = $4;
#		    print STDERR "$dir$d/$f\titem\t$ijType\t$ijOwner.$ijName\t\t$ijFile\n";
		    $ifn = "$ijOwner.$ijName";
		    if(defined($itemsFile{$ifn})){print STDERR "$ifn in items.json twice: $itemsFile{$ifn} $ijFile\n"}
		    $itemsFile{$ifn} = $ijFile;
		    if($ijType eq "FUNCTION") {
			$itemsFunction{$ifn} = $ijFile;
		    } elsif($ijType eq "INDEX") {
			$itemsIndex{$ifn} = $ijFile;
		    } elsif($ijType eq "MATERIALIZED_VIEW") {
			$itemsMaterializedView{$ifn} = $ijFile;
		    } elsif($ijType eq "PROCEDURE") {
			$itemsProcedure{$ifn} = $ijFile;
		    } elsif($ijType eq "REF_CONSTRAINT") {
			$itemsRefConstraint{$ifn} = $ijFile;
		    } elsif($ijType eq "SEQUENCE") {
			$itemsSequence{$ifn} = $ijFile;
		    } elsif($ijType eq "TABLE") {
			$itemsTable{$ifn} = $ijFile;
		    } elsif($ijType eq "TRIGGER") {
			$itemsTrigger{$ifn} = $ijFile;
		    } elsif($ijType eq "TYPE") {
			$itemsType{$ifn} = $ijFile;
		    } elsif($ijType eq "VIEW") {
			$itemsView{$ifn} = $ijFile;
		    } else {print STDERR "Unknown items.json item type: $ijType\n"}
		}
		while($itemsDepend =~ /[\{]\s+\"child\" : [\{]\s+\"type\" : \"([^\"]+)\",\s+\"owner\" : \"([^\"]+)\",\s+\"name\" : \"([^\"]+)\"\s+[\}],\s+\"parents\" : [\[]([^\]]+)[\]]\s+[\}]/gosm) {
		    $ijType = $1;
		    $ijOwner = $2;
		    $ijName = $3;
		    $ijParents = $4;
		    $ijPlist = "";
		    while($ijParents =~ /[\{]\s+\"type\" : \"([^\"]+)\",\s+\"owner\" : \"([^\"]+)\",\s+\"name\" : \"([^\"]+)\"\s+[\}]/gosm){
			$ijPlist .= " $1 $2.$3;";
		    }
		    if($ijPlist ne "") {$dependParent{"$ijType:$ijOwner.$ijName"} = $ijPlist}
		    
#		    print STDERR "$dir$d/$f\tchild\t$ijType\t$ijOwner.$ijName\t$ijPlist\n";
		    if($ijType eq "FUNCTION") {
			$dependFunction{$ifn} = $ijFile;
		    } elsif($ijType eq "INDEX") {
			$dependIndex{$ifn} = $ijFile;
		    } elsif($ijType eq "MATERIALIZED_VIEW") {
			$dependMaterializedView{$ifn} = $ijFile;
		    } elsif($ijType eq "PROCEDURE") {
			$dependProcedure{$ifn} = $ijFile;
		    } elsif($ijType eq "REF_CONSTRAINT") {
			$dependRefConstraint{$ifn} = $ijFile;
		    } elsif($ijType eq "SEQUENCE") {
			$dependSequence{$ifn} = $ijFile;
		    } elsif($ijType eq "SYNONYM") {
			$dependSynonym{$ifn} = $ijFile;
		    } elsif($ijType eq "TABLE") {
			$dependTable{$ifn} = $ijFile;
		    } elsif($ijType eq "TRIGGER") {
			$dependTrigger{$ifn} = $ijFile;
		    } elsif($ijType eq "TYPE") {
			$dependType{$ifn} = $ijFile;
		    } elsif($ijType eq "VIEW") {
			$dependView{$ifn} = $ijFile;
		    } else {print STDERR "Unknown items.json child type: $ijType\n"}
		}
	    }
	}
	else {
	    print "Oracle file $d/$f\n";
	}
    }
    closedir(OSDIR);
    return $err;
}

sub parsePostgresTop($$){
    my ($d,$f) = @_;
    local *IN;
    my $err = 0;
    my @f;

    if($f eq "Makefile") {
    }
    elsif($f eq "manual_objects_list.php") {
    }
    else {
	print "Postgres parse $d/$f\n";
	return 1;
    }
    postgresUnparsed("$d/$f",$f);
    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
    while(<IN>) {
    }
    close IN;
    return $err;
}

sub parsePostgresFunctions($){
    my ($d) = @_;
    local *PSDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my $subd = $d;

    $subd =~ s/^$pplus\///g;

    opendir(PSDIR,"$d") || die "parsePostgresFunctions failed to open $d";

    while($f = readdir(PSDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    print STDERR "PostgresFunctions subdirectory $d/$f\n";
	    next;
	}
	if($f =~ /[.]sql$/) {
#	    print "Postgres parse $d/$f\n";
	    $pgsql{"$subd/$f"}++;
	    $noret=0;

	    postgresParsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		s/\(-1\)/-1/g;
		if(/^\s*CREATE\s+(OR\s+REPLACE\s+)?FUNCTION\s+(\S+)\s*\(([^\)]*)\)\s*RETURNS (.*)/) {
		    $func = $2;
		    $ret = $3;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)\/functions$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    $func =~ s/^[^.]+[.]//g;
		    if($ret ne "trigger"){
			$pFunctionFile{"$schema.$func"} = "$d/$f";
			$pFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
		elsif(/^\s*CREATE\s+(OR\s+REPLACE\s+)?FUNCTION\s+(\S+)\s+RETURNS (.*)/) {
		    $func = $2;
		    $ret = $3;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)\/functions$/);
		    $func = uc($func);
		    $func =~ s/^[^.]+[.]//g;
		    $schema = uc($schema);
		    if($ret ne "trigger"){
			$pFunctionFile{"$schema.$func"} = "$d/$f";
			$pFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
		elsif(/^\s*CREATE\s+(OR\s+REPLACE\s+)?FUNCTION\s+(\S+)\s*\($/) {
#		    print STDERR "Parsing postgres function: $_";
		    $func = $2;
		    $noret = 1;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)\/functions$/);
		    $func = uc($func);
		    $func =~ s/^[^.]+[.]//g;
		    $schema = uc($schema);
		    $pFunctionFile{"$schema.$func"} = "$d/$f";
		}
		elsif($noret && /^\s*RETURNS (\S+) AS/) {
		    $ret = $1;
		    if($ret ne "trigger"){
			$pFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
	    }
	    close IN;
	}
    }
    closedir(PSDIR);
    return $err;
}

sub parsePostgresViews($){
    my ($d) = @_;
    local *PSDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my $subd = $d;

    $subd =~ s/^$pplus\///g;

    opendir(PSDIR,"$d") || die "parsePostgresViews failed to open $d";

    while($f = readdir(PSDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    print "postgresViews subdir $d/$f\n";
	    next;
	}
	if($f =~ /[.]sql$/) {
#	    print "Postgres parse $d/$f\n";
	    $pgsql{"$subd/$f"}++;

	    postgresParsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		if(/^\s*(.*\S)\s+(VIEW|view)\s+(\S+)/) {
		    $vuse = $1;
		    $view = $3;
		    ($schema) =  ($d =~ /\/([^\/]+)\/views$/);
		    $vuse = uc($vuse);
		    $schema = uc($schema);
		    $view = uc($view);
		    $schema =~ s/\"//g;
		    $view =~ s/\"//g;
		    $view =~ s/^$schema[.]//;
		    if($vuse =~ /^CREATE/) {
			$pViewFile{"$schema.$view"} = "$d/$f";
			$cview = 1;
		    }
		    elsif($vuse =~ /^COMMENT/) {#COMMENT ON VIEW - ignore
		    }
		    else {
			print STDERR "$d/$f unexpected view $view     $vuse     '$rest'\n";
		    }
		}
	    }
	    close IN;
	}
    }
    closedir(PSDIR);
    return $err;
}

sub parsePostgresScripts($){
    my ($d) = @_;
    local *PSDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;

    opendir(PSDIR,"$d") || die "parsePostgresScripts failed to open $d";

    while($f = readdir(PSDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    print "PostgresScripts subdir $d/$f\n";
	    next;
	}
	if($f =~ /[.]php$/) {
	}
	else {
	    print "Postgres parse $d/$f\n";
	    next;
	}
	postgresUnparsed("$d/$f",$f);
	open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	while(<IN>) {
	}
	close IN;
    }
    closedir(PSDIR);
    return $err;
}

sub parsePostgresGlobal($){
    my ($d) = @_;
    local *PSDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;

    opendir(PSDIR,"$d") || die "parsePostgresGlobal failed to open $d";

    while($f = readdir(PSDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    print "PostgresGlobal subdir $d/$f\n";
	    next;
	}
	if($f eq "Makefile") {
	}
	elsif($f =~ /[.]sql$/) {
	}
	else {
	    print "Postgres parse $d/$f\n";
	    next;
	}
	postgresUnparsed("$d/$f",$f);
	open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	while(<IN>) {
	}
	close IN;
    }
    closedir(PSDIR);
    return $err;
}

sub parsePostgresMacrofun($){
    my ($d) = @_;
    local *PSDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;

    opendir(PSDIR,"$d") || die "parsePostgresMacrofun failed to open $d";

    while($f = readdir(PSDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    print "PostgresMacrofun subdir $d/$f\n";
	    next;
	}
	if($f eq "README.txt") {
	}
	elsif($f =~ /[.]sql$/) {
	}
	else {
	    print "Postgres parse $d/$f\n";
	    next;
	}
	postgresUnparsed("$d/$f",$f);
	open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	while(<IN>) {
	}
	close IN;
    }
    closedir(PSDIR);
    return $err;
}

sub parsePostgresMeta($){
    my ($d) = @_;
    local *PSDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;

    opendir(PSDIR,"$d") || die "parsePostgresMeta failed to open $d";

    while($f = readdir(PSDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    print "PostgresMeta subdir $d/$f\n";
	    next;
	}
	if($f eq "Makefile") {
	}
	elsif($f =~ /[.]sql$/) {
	}
	elsif($f =~ /[.]php$/) {
	}
	elsif($f =~ /[.]tsv$/) {
	}
	else {
	    print "Postgres parse $d/$f\n";
	    next;
	}

	open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	while(<IN>) {
	}
	close IN;
    }
    closedir(PSDIR);
    return $err;
}

sub parsePostgresLoadall($){
    my ($f) = @_;
    local *IN;

    open(IN,"$f") || die "failed to open '$f";
    while(<IN>){
	if(/\\i (\S+[.]sql$)/) {
	    $pgload{"$1"}++;
	}
	else {
	    print STDERR "Unexpected line in parsePostgresLoadall $f: $_\n";
	}
    }
    close IN;
}

sub parsePostgres($){
    my ($d) = @_;
    local *PSDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my ($tuse,$schema,$table);
    my $subd = $d;

    $subd =~ s/^$pplus\///g;

    opendir(PSDIR,"$d") || die "parsePostgres failed to open $d";

    while($f = readdir(PSDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    if($f eq "functions") {
		parsePostgresFunctions("$d/$f");
	    }
	    elsif($f eq "views"){
		parsePostgresViews("$d/$f");
	    }
	    else {
		print "Postgres subdir $d/$f\n";
	    }
	    next;
	}
	if($f =~ /[.]sql$/) {
	    if($f =~ /^_.*[.]sql$/){
		if($f eq "_cross.sql"){ # added to postgres for RC2 port
		}
		elsif($f eq "_misc.sql"){
		}
		elsif($f eq "_load_all.sql"){
		    parsePostgresLoadall("$d/$f");
		    next;
		}
		else {
		    print "postgres parse $d/$f\n";
		    next;
		}
	    }

	    if($f ne "_cross.sql"){
		$pgsql{"$subd/$f"}++;
	    }

	    postgresParsed("$d/$f",$f);
	    $ctable = 0;
	    $ctrig  = 0;
	    $cfunc  = 0;
	    $cproc  = 0;
	    $cview  = 0;
	    $cseq = 0;
	    $alterctable = 0;
	    $altertable = "undefined";
	    $tseq = "";

	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;

		if($alterctable) {
		    if(/^\s*ADD\s+CONSTRAINT\s+(\S+)\s+PRIMARY\s+KEY\s+\(([^\)]+)\)/) {
			$pkc = uc($1);
			$pk = uc($2);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			$pTablePrikey{$altertable} = $pk;
			if(defined($pkc)){$pTablePrikeyName{"$schema.$table"} = $pkc}
		    }
		    if(/^\s*ADD\s+CONSTRAINT\s+(\S+)\s+UNIQUE\s+\(([^\)]+)\)/) {
			$pkc = uc($1);
			$pk = uc($2);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){$pTableUnikey{$altertable} .= "$pkc $pk;"}
			else {$pTableUnikey{$altertable} .= ". $pk;"}
		    }
		    if(/^\s*ADD\s+CONSTRAINT\s+(\S+)\s+FOREIGN\s+KEY\s+(\(\S+\)\s+)REFERENCES\s+([^\(]+\([^\)]+\))/){
			$pk = uc($1).uc($2);
			$pk .= uc($schema);
			$pk .= ".";	
			$pk .= uc($3);
			$pk .= ";";
			$pTableForkey{"$schema.$table"} .= $pk;
		    }
		    if(/;/) {$alterctable = 0}
		}
		if(/(\S+)\s+TEMPORARY\s+(TABLE|table)\s+(ONLY\s+)?(\S+)/) {
		    $tuse = $1;
		    $table = $4;
		    if($table =~ /(^[.]+)[.](.*)/) {
			$schema = $1;
			$table = $2;
		    }
		    else {
			($schema) = ($d =~ /\/([^\/]+)$/);
		    }
		    $tuse = uc($tuse);
		    $schema = uc($schema);
		    $table = uc($table);
		    $schema =~ s/\"//g;
		    $table =~ s/\"//g;
#		    print STDERR "Postgres temptable tuse: $tuse table: $table schema: $schema\n";
		    if($tuse eq "CREATE") {
			$pTableFile{"$schema.$table"} = "$d/$f";
			$ctable = 1;
		    }
		    if($tuse eq "ALTER") {
			$altertable = "$schema.$table";
			$alterctable = 1;
		    }
		}
		if(/(\S+)\s+(TABLE|table)\s+(ONLY\s+)?(\S+)/) {
		    $tuse = $1;
		    $table = $4;
		    if($table =~ /(^[.]+)[.](.*)/) {
			$schema = $1;
			$table = $2;
		    }
		    else {
			($schema) = ($d =~ /\/([^\/]+)$/);
		    }
		    $tuse = uc($tuse);
		    $schema = uc($schema);
		    $table = uc($table);
		    $schema =~ s/\"//g;
		    $table =~ s/\"//g;
		    if($tuse eq "CREATE") {
			$pTableFile{"$schema.$table"} = "$d/$f";
			$ctable = 1;
		    }
		    if($tuse eq "ALTER") {
			$altertable = "$schema.$table";
			$alterctable = 1;
		    }
		}
		elsif($ctable) {
		    if(/;/){$ctable=0; next}
		    if(/^\s*\(/){s/^\s*\(\s*//}
		    if(/^\s*\"position\"\s+/){s/\"position\"/position/} # used in de_variant_subject_idx
		    if(/^\s*\)/){$ctable=2; s/^\s*\)\s*//}
		    if(/^\s*([a-z]\S+)\s+(.*?),?$/) {
			$col = $1;
			$cdef = $2;
			$col = uc($col);
			$cdef =~ s/,\s+$//g;
			$pTableColumn{"$schema.$table"} .= "$col $cdef;";
			if($cdef =~ /\s+DEFAULT\s+nextval\(\'([^\']+)\'(::regclass)?\) NOT NULL$/){
			    $cid = $1;
			    $cid = uc($1);
			    $pNextval{"$schema.$table"} = "$col.$cid";
			}
			elsif($cdef =~ /DEFAULT\s+nextval/){print STDERR "$d/$f DEFAULT nextval not recognized: '$cdef'\n"}
		    }
		    if(/^\s*(CONSTRAINT\s+(\S+)\s+)?PRIMARY\s+KEY\s+\(([^\)]+)\)/){
			$pkc = $2;
			$pk = uc($3);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			$pTablePrikey{"$schema.$table"} = $pk;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $pTablePrikeyName{"$schema.$table"} = $pkc;
			}
		    }
		    if(/^\s*(CONSTRAINT (\S+)\s+)?UNIQUE\s+\(([^\)]+)\)/){
			$pkc = $2;
			$pk = uc($3);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $pTableUnikey{"$schema.$table"} .= "$pkc $pk;";
			}
			else {$pTableUnikey{"$schema.$table"} .= ". $pk;"}
		    }
		}

		if($cseq == 1 && /([^;]*)/) {
		    $tseq .= $1;
		    if(/;/) {$cseq = 2}
		}
		if(/^\s*(\S+)\s+UNIQUE\s+(INDEX|index)\s+(\S+)\s+ON\s+(\S+)\s+USING\s+(\S+)\s+\(([^\)]+)\)/) {
#		    print STDERR "Parsing postgres unique index: $_";
		    $iuse = $1;
		    $idx = $3;
		    $itable = $4;
		    $idxtype = $5;
		    $icols = $6;
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $schema = uc($schema);
		    $idx =~ s/"//g;
		    $idx = uc($idx);
		    $itable =~ s/"//g;
		    $itable = uc($itable);
		    $icols =~ s/"//g;
		    $icols =~ s/\s//g;
		    $icols = uc($icols);
		    if($iuse =~ /^CREATE/) {
			$pTableUnikey{"$schema.$itable"} .= "$idx $icols;";
		    }
		}
		elsif(/^\s*(\S+)\s+BINARY\s+(INDEX|index)\s+(\S+)\s+ON\s+(\S+)\s+USING\s+(\S+)\s+\(([^\)]+)\)/) {
#		    print STDERR "Parsing postgres index: $_";
		    $iuse = $1;
		    $idx = $3;
		    $itable = $4;
		    $idxtype = $5;
		    $icols = $6;
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $schema = uc($schema);
		    $idx =~ s/"//g;
		    $idx = uc($idx);
		    $itable =~ s/"//g;
		    $itable = uc($itable);
		    $icols =~ s/"//g;
		    $icols =~ s/\s//g;
		    $icols = uc($icols);
		    if($iuse =~ /^CREATE/) {
			$pIndexFile{"$schema.$idx"} = "$d/$f";
			$pIndex{"$schema.$itable"} .= "$schema.$idx($icols);";
		    }
		}
		elsif(/^\s*(\S+)\s+(INDEX|index)\s+(\S+)\s+ON\s+(\S+)\s+USING\s+(\S+)\s+\(([^\)]+)\)/) {
#		    print STDERR "Parsing postgres index: $_";
		    $iuse = $1;
		    $idx = $3;
		    $itable = $4;
		    $idxtype = $5;
		    $icols = $6;
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $schema = uc($schema);
		    $idx =~ s/"//g;
		    $idx = uc($idx);
		    $itable =~ s/"//g;
		    $itable = uc($itable);
		    $icols =~ s/"//g;
		    $icols =~ s/\s//g;
		    $icols = uc($icols);
		    if($iuse =~ /^CREATE/) {
			$pIndexFile{"$schema.$idx"} = "$d/$f";
			$pIndex{"$schema.$itable"} .= "$schema.$idx($icols);";
		    }
		}
		if(/^\s*(.*\S)\s+(SEQUENCE|sequence)\s+(\S+)(.*)/) {
		    $suse = $1;
		    $seq = $3;
		    $rest = $4;
		    $suse = uc($suse);
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $schema = uc($schema);
		    $seq = uc($seq);
		    $schema =~ s/\"//g;
		    $seq =~ s/\"//g;
#		    print "$d/$f sequence $seq     $suse\n";
		    if($suse =~ /^CREATE/) {
			$pSequenceFile{"$schema.$seq"} = "$d/$f";
			$cseq = 1;
			$tseq = $rest;
		    }
		}
		if($cseq == 2){
		    $cseq = 0;
		    $pSequenceText{"$schema.$seq"} = $tseq;
		    $tseq = "";
		}

		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s+RETURNS\s+trigger/) {
		    $trig = $1;
		    $trig =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $trig = uc($trig);
		    if($trig !~ /^TF_/ &&
			$trig !~ /_FUN$/) {print "trigger name '$trig' $f\n"}
		    $trig =~ s/^TF_//g;
		    $trig =~ s/_FUN$//g;
		    $schema = uc($schema);
#		    print "$d/$f trigger $trig     create\n";
		    $pTriggerFile{"$schema.$trig"} = "$d/$f";
		    $ctrig = 1;
		}
		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s+RETURNS (.*)/) {
		    $func = $1;
		    $ret = $2;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    if($ret ne "trigger") {
			$pFunctionFile{"$schema.$func"} = "$d/$f";
			$pFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s*\(([^\)]*)\)\s*RETURNS\s+(.*)/) {
		    $func = $1;
		    $ret = $2;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    if($ret ne "trigger") {
			$pFunctionFile{"$schema.$func"} = "$d/$f";
			$pFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
		if(/^\s*(.*\S)\s+(VIEW|view)\s+(\S+)/) {
		    $vuse = $1;
		    $view = $3;
		    ($schema) =  ($d =~ /\/([^\/]+)$/);
		    $vuse = uc($vuse);
		    $schema = uc($schema);
		    $view = uc($view);
		    $schema =~ s/\"//g;
		    $view =~ s/\"//g;
		    if($vuse =~ /^CREATE/) {
			$pViewFile{"$schema.$view"} = "$d/$f";
			$cview = 1;
		    }
		    elsif($vuse =~ /^COMMENT/) {#COMMENT ON VIEW - ignore
		    }
		    else {
			print STDERR "$d/$f unexpected view $view     $vuse     '$rest'\n";
		    }
		}
	    }
	    close IN;
	}
	elsif($f eq "dependencies.php"){
#	    print "Postgres parse dependencies.php $d/$f\n";
	    postgresUnparsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
	    }
	    close IN;
	}
	else {
#	    print "Postgres file $d/$f\n";
	}
    }
    closedir(PSDIR);
    return $err;
}

sub compareTypes($$$$){
    my ($st,$c,$ot,$pt) = @_;
    my $s;
    my $t;

    $otrigger = "";

    if($ot eq $pt) {return 0}

#    if($pt =~ /DEFAULT NEXTVAL\S+/ && $ot =~ /\/\* POSTGRES NEXTVAL NEEDS TRIGGER \*\//){
#	$pt =~ s/DEFAULT NEXTVAL\S+ //g;
#	$ot =~ s/\/\*[^*]+\*\/ //g;
#    }

    $ot =~ s/\s+/ /g;
    $pt =~ s/\s+/ /g;

    $ot =~ s/NUMBER \(/NUMBER(/;
    $ot =~ s/VARCHAR2 \(/VARCHAR2(/;
    $ot =~ s/VARCHAR \(/VARCHAR(/;

    $pt =~ s/VARCHAR2 \(/VARCHAR2(/;
    $pt =~ s/VARCHAR \(/VARCHAR(/;

    if($pt =~ /TIMESTAMP\)$/) {
	$pt =~ s/\)$//;
    }

# clean up matching NULL and NOT NULL with optional ENABLE

    if($ot =~ / NOT NULL$/ && $pt =~ / NOT NULL ENABLE$/) {
	$ot =~ s/ NOT NULL$//;
	$pt =~ s/ NOT NULL ENABLE$//;
    }

    if($ot =~ / NOT NULL ENABLE$/ && $pt =~ / NOT NULL$/) {
	$ot =~ s/ NOT NULL ENABLE$//;
	$pt =~ s/ NOT NULL$//;
    }

    if($ot =~ / NOT NULL$/ && $pt =~ / NOT NULL$/) {
	$ot =~ s/ NOT NULL$//;
	$pt =~ s/ NOT NULL$//;
    }

    if($ot =~ /\) NULL$/ && $pt =~ /\) NULL$/) {
	$ot =~ s/\) NULL$/\)/;
	$pt =~ s/\) NULL$/\)/;
    }

    if($ot =~ /(\S\S\S) NULL$/) {
	if($1 ne "NOT" && $pt =~ /(\S\S\S) NULL$/) {
	    if($1 ne "NOT") {
		$ot =~ s/ NULL$//;
		$pt =~ s/ NULL$//;
	    }
	}
    }

    if($pt !~ / NULL$/) {
	if($ot =~ /(\S\S\S) NULL$/) {
	    if($1 ne "NOT") {
		$ot =~ s/ NULL$//;
	    }
	}
    }

    if($ot !~ / NULL$/) {
	if($pt =~ /(\S\S\S) NULL$/) {
	    if($1 ne "NOT") {
		$pt =~ s/ NULL$//;
	    }
	}
    }

    if($ot =~ / NOT NULL ENABLE$/ && $pt =~ /SERIAL$/) {
	$ot =~ s/ NOT NULL ENABLE$//;
    }

    if($ot =~ / NOT NULL$/ && $pt =~ /SERIAL$/) {
	$ot =~ s/ NOT NULL$//;
    }

    $ot =~ s/ WITH LOCAL TIME ZONE//g; # only allows local time display, storage unchanged

    if($ot =~ / DEFAULT '1'$/ && $pt =~ / DEFAULT \(1\)$/) {
	$ot =~ s/ \S+ \S+$//;
	$pt =~ s/ \S+ \S+$//;
    }
    
    if($pt =~ / DEFAULT NOW\(\)/ && $ot =~ / DEFAULT SYSDATE/) {
	$pt =~ s/ DEFAULT \S+//;
	$ot =~ s/ DEFAULT \S+//;
    }
    if($pt =~ / DEFAULT CURRENT_TIMESTAMP/ && $ot =~ / DEFAULT SYSDATE/) {
	$pt =~ s/ DEFAULT \S+//;
	$ot =~ s/ DEFAULT \S+//;
    }

    if($pt =~ /^BIGINT/) {
	if($ot =~ /^NUMBER\s/ || $ot eq "NUMBER") {
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	elsif($ot =~ /^NUMBER\((\d+),0\)/){
	    if($1 >= 1 && $1 <= 22) {
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+/matched/;
	    }
	}
	elsif($ot =~ /^NUMBER\([*],0\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^BIGSERIAL/) {	# used for unique identifiers in i2b2 postgres
	if($ot =~ /^NUMBER\((\d+),0\)/){
	    if($1 >= 1 && $1 <= 22) {
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+/matched/;
	    }
	}
	elsif($ot =~ /^NUMBER\([*],0\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	elsif($ot =~ /^NUMBER\s/ || $ot eq "NUMBER") {
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^INT/) {
	if($ot =~ /^INTEGER/) {
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	elsif($ot =~ /^NUMBER\((\d+),0\)/){
	    $isize = 22;
	    
	    # Allowed exceptions where i2b2 uses a larger Oracle datatype
	    if($c eq "AGE_IN_YEARS_NUM") {$isize = 38}
	    if($c eq "CRC_UPLOAD_ID") {$isize = 38}
	    if($c eq "ENCOUNTER_NUM") {$isize = 38}
	    if($c eq "I2B2_ID") {$isize = 38}
	    if($c eq "INSTANCE_NUM") {$isize = 38}
	    if($c eq "LENGTH_OF_STAY") {$isize = 38}
	    if($c eq "PATIENT_COUNT") {$isize = 38}
	    if($c eq "PATIENT_ID") {$isize = 38}
	    if($c eq "PATIENT_NUM") {$isize = 38}
	    if($c eq "SEARCH_KEYWORD_ID") {$isize = 38}
	    if($c eq "SET_TYPE_ID") {$isize = 38}
	    if($c eq "UPLOAD_ID") {$isize = 38}
	    if($st eq "I2B2DEMODATA.DATAMART_REPORT") {$isize = 38}
	    if($1 >= 1 && $1 <=$isize) {
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+/matched/;
	    }
	}
	elsif($ot =~ /^NUMBER\([*],0\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	elsif($ot =~ /^NUMBER\s/ || $ot eq "NUMBER") {
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^SERIAL/) {	# used for unique identifiers in i2b2 postgres
	if($ot =~ /^NUMBER\s/ || $ot eq "NUMBER") {
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	elsif($ot =~ /^NUMBER\((\d+),0\)/){
	    $isize = 22;
	    if($c eq "I2B2_ID") {$isize = 38}
	    if($c eq "UPLOAD_ID") {$isize = 38}
	    if($1 >= 5 && $1 <= $isize) {
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+/matched/;
	    }
	}
	elsif($ot =~ /^NUMBER\([*],0\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^SMALLINT/) {
	if($ot =~ /^NUMBER\s/ || $ot eq "NUMBER") {
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	elsif($ot =~ /^NUMBER\((\d+),0\)/){
	    if($1 >= 1 && $1 <= 4) {
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+/matched/;
	    }
	}
	elsif($ot =~ /^NUMBER\([*],0\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^BOOLEAN DEFAULT FALSE/) { # treat boolean as never NULL
	if($ot =~ /^NUMBER\(1,0\) DEFAULT 0 NOT NULL ENABLE/){
	    $ot =~ s/^\S+ DEFAULT 0 NOT NULL ENABLE/matched/;
	    $pt =~ s/^\S+ DEFAULT FALSE/matched/;
	}
	elsif($ot =~ /^CHAR\(1 BYTE\) DEFAULT 0/){
	    $ot =~ s/^\S+ \S+ DEFAULT 0/matched/;
	    $pt =~ s/^\S+ DEFAULT FALSE/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^BOOLEAN/) {
	if($ot =~ /^NUMBER\(1,0\)/) {
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	elsif($ot =~ /^CHAR\(1 BYTE\)/) {
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^REAL/) {
	if($ot =~ /^NUMBER\((\d+),(\d+)\)/){
	    if($1 >= 2 && $1 <= 22 && $2 > 0) {
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+/matched/;
	    }
	}
    }
    elsif($pt =~ /^DOUBLE PRECISION/) {
	if($ot =~ /^NUMBER\((\d+),(\d+)\)/){
	    if($1 >= 9 && $1 <= 38 && $2 > 0) {
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+ \S+/matched/;
	    }
	}
	elsif($ot =~ /^NUMBER\s/ || $ot eq "NUMBER"){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	elsif($ot =~ /^FLOAT\((\d+)\)/){ # (n) is the precision
	    if($1 > 0) {
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+ \S+/matched/;
	    }
	}
	elsif($ot =~ /^BINARY_DOUBLE/){ # (n) is the precision
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^DECIMAL\((\d+),(\d+)\)/) {
	$size=$1;
	$prec=$2;
	if($ot =~ /NUMBER\($size,$prec\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^NUMERIC\((\d+),(\d+)\)/) {
	$size=$1;
	$prec=$2;
	if($ot =~ /NUMBER\($size,$prec\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^NUMERIC/) {
	$size=$1;
	$prec=$2;
	if($ot =~ /NUMBER\(([*]|\d+),(\d+)\)/){
	    if($2 == 0) {
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+/matched/;
	    }
	}
	elsif($ot =~ /NUMBER\(/){
	    return 1;
	}
	elsif($ot =~ /NUMBER/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^VARCHAR2?\((\d+)\)/) {
	$size = $1;
	if($ot =~ /^VARCHAR2?\($size BYTE\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	if($ot =~ /^VARCHAR2?\($size CHAR\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	if($ot =~ /^VARCHAR2?\($size\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	if($ot =~ /^NVARCHAR2\($size BYTE\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	if($ot =~ /^NVARCHAR2\($size CHAR\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	if($ot =~ /^NVARCHAR2\($size\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	if($ot =~ / DEFAULT \'([^\']*)\'$/) {
	    $oval = $1;
	    if($pt =~ /DEFAULT \'$oval\'::CHARACTER VARYING$/) {
		$ot =~ s/ DEFAULT \'([^\']*)\'$//;
		$pt =~ s/ DEFAULT \'$oval\'::CHARACTER VARYING$//;
	    }
	}
    }

    elsif($pt =~ /^VARCHAR2?\((\d+) BYTE\)/) {
	$size = $1;
	if($ot =~ /^VARCHAR2?\($size BYTE\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /^VARCHAR2?\($size CHAR\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /^VARCHAR2?\($size\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /^NVARCHAR2\($size BYTE\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /^NVARCHAR2\($size CHAR\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /^NVARCHAR2\($size\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ / DEFAULT \'([^\']*)\'$/) {
	    $oval = $1;
	    if($pt =~ /DEFAULT \'$oval\'::CHARACTER VARYING$/) {
		$ot =~ s/ DEFAULT \'([^\']*)\'$//;
		$pt =~ s/ DEFAULT \'$oval\'::CHARACTER VARYING$//;
	    }
	}
    }

    elsif($pt =~ /^CHARACTER VARYING\((\d+)\)/) {
	$size = $1;
	if($ot =~ /^VARCHAR2?\($size BYTE\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /^VARCHAR2?\($size CHAR\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /^VARCHAR2?\($size\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /^NVARCHAR2\($size BYTE\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /^NVARCHAR2\($size CHAR\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /^NVARCHAR2\($size\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /^CLOB/ && $size >= 2000){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ / DEFAULT \'([^\']*)\'$/) {
	    $oval = $1;
	    if($pt =~ /DEFAULT \'$oval\'::CHARACTER VARYING$/) {
		$ot =~ s/ DEFAULT \'([^\']*)\'$//;
		$pt =~ s/ DEFAULT \'$oval\'::CHARACTER VARYING$//;
	    }
	}
    }

    elsif($pt =~ /^CHARACTER\((\d+)\)/) {
	$size = $1;
	if($ot =~ /CHAR\($size BYTE\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	if($ot =~ /CHAR\($size CHAR\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	if($ot =~ /ROWID/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
    }

    elsif($ot =~ /^CHAR\((\d+)\)/) {
	$size = $1;
	if($pt =~ /CHAR\($size BYTE\)/){
	    $pt =~ s/^\S+ \S+/matched/;
	    $ot =~ s/^\S+/matched/;
	}
	if($pt =~ /CHAR\($size CHAR\)/){
	    $pt =~ s/^\S+ \S+/matched/;
	    $ot =~ s/^\S+/matched/;
	}
    }

    elsif($pt =~ /^OID/) {
	$size = $1;
	if($ot =~ /^BLOB/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	if($ot =~ /^CLOB/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
    }

    elsif($pt =~ /^BYTEA/) {
	$size = $1;
	if($ot =~ /^[BC]LOB/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
    }

    elsif($pt =~ /^TIMESTAMP(\((\d)\))? WITHOUT TIME ZONE/){
	if(!defined($1)) {$it = 6}
	else {$it = $2}
	if($ot =~ /^DATE/) {
	    $pt =~ s/^\S+ \S+ \S+ \S+/matched/;
	    $ot =~ s/^\S+/matched/;
	}
	elsif($ot =~ /^TIMESTAMP \(9\)/ && $it == 6) {
	    $pt =~ s/^\S+ \S+ \S+ \S+/matched/;
	    $ot =~ s/^\S+ \S+/matched/;
	}
	elsif($ot =~ /^TIMESTAMP$/ && $it == 6) {
	    $pt =~ s/^\S+ \S+ \S+ \S+/matched/;
	    $ot =~ s/^\S+/matched/;
	}
	elsif($ot =~ /^TIMESTAMP \($it\)/) {
	    $pt =~ s/^\S+ \S+ \S+ \S+/matched/;
	    $ot =~ s/^\S+ \S+/matched/;
	}
	if($pt =~ / DEFAULT NOW\(\)/ && $ot =~ / DEFAULT SYSDATE/) {
	    $pt =~ s/ DEFAULT \S+//;
	    $ot =~ s/ DEFAULT \S+//;
	}
	if($pt =~ / DEFAULT CURRENT_TIMESTAMP/ && $ot =~ / DEFAULT SYSDATE/) {
	    $pt =~ s/ DEFAULT \S+//;
	    $ot =~ s/ DEFAULT \S+//;
	}
    }
    elsif($ot =~ /^TIMESTAMP (\(1\))/){
	if($pt =~ /^TIMESTAMP$/) {
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
    }
    elsif($pt =~ /^TIMESTAMP (\(6\))/){
	if($ot =~ /^DATE$/) {
	    $pt =~ s/^\S+ \S+/matched/;
	    $ot =~ s/^\S+/matched/;
	}
    }
    elsif($pt =~ /^TIMESTAMP(\(\d\))/){
	if($ot =~ /^TIMESTAMP \(\d\)/) {
	    $ot =~ s/^(\S+) (\S+)/$1$2/;
	}
    }
    elsif($pt =~ /^TIMESTAMP$/){
	if($ot =~ /^DATE$/) {
	    $pt =~ s/^\S+/matched/;
	    $ot =~ s/^\S+/matched/;
	}
	if($ot =~ /^TIMESTAMP \(6\)$/) {
	    $pt =~ s/^\S+/matched/;
	    $ot =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /^TIMESTAMP \(9\)$/) {
	    $pt =~ s/^\S+/matched/;
	    $ot =~ s/^\S+ \S+/matched/;
	}
    }
    elsif($pt =~ /^DATE\b/){
	if($ot =~ /^DATE\b/) {
	    $ot =~ s/^(\S+)/matched/;
	    $pt =~ s/^(\S+)/matched/;
	}
	if($pt =~ / DEFAULT NOW\(\)/ && $ot =~ / DEFAULT SYSDATE/) {
	    $pt =~ s/ DEFAULT \S+//;
	    $ot =~ s/ DEFAULT \S+//;
	}
    }
    elsif($pt =~ /^TEXT/){
	if($ot =~ /^N?[CB]LOB/) {
	    $pt =~ s/^\S+/matched/;
	    $ot =~ s/^\S+/matched/;
	}
    }

    if($pt =~ /DEFAULT '\S'::BPCHAR/) {
	$pt =~ s/(DEFAULT '\S')::BPCHAR/$1/;
    }

    if($pt =~ /^matched DEFAULT NEXTVAL\(\'([^\']+)\'::REGCLASS\)/) {
	$sq = $1;
	($s,$t) = ($st =~ /([^.]+)[.](.*)/);
	my $onv = "";
	my $pnv = "";
	if(defined($oNextval{"$s.$t"})) {$onv = $oNextval{"$s.$t"}}
	if(defined($pNextval{"$s.$t"})) {$pnv = $pNextval{"$s.$t"}}
	if($ot eq "matched" && $onv eq $pnv) {
	    $ot =~ s/ \S+$//;
	    $pt =~ s/ \S+ \S+$//;
	}
	else {
	    print STDERR "Create trigger onv: '$onv' pnv: '$pnv'\n";
	    my $trig = "TRG_$t\_$c";
	    $trig =~ s/PATIENT/PAT/g;
	    $trig =~ s/COLLECTION/COL/g;
	    $trig =~ s/QUERY_MASTER/QM/g;
	    $trig =~ s/QUERY_RESULT_INSTANCE/QRI/g;
	    $trig =~ s/QUERY_INSTANCE/QI/g;
	    $trig =~ s/QUERY_RESULT/QR/g;
	    $trig =~ s/RESULT_INSTANCE/RI/g;
	    $trig =~ s/QUERY/Q/g;
	    $trig =~ s/RESULT/RES/g;
	    $otrigger = "--
-- Type: TRIGGER; Owner: $s; Name: $trig
--
  CREATE OR REPLACE TRIGGER \"$s\".\"$trig\" 
   before insert on \"$s\".\"$t\" 
   for each row 
begin  
   if inserting then 
      if :NEW.\"$c\" is null then 
         select $sq.nextval into :NEW.\"$c\" from dual; 
      end if; 
   end if; 
end;
/
ALTER TRIGGER \"$s\".\"$trig\" ENABLE;
";

	}
    }
    if($ot eq $pt) {return 0} 

#    print "comparetypes $st.$c '$ot' '$pt'\n";
    return 1;
}

#####################################################
#
# Compare two Oracle databases : i2b2 and tranSMART
#
#####################################################

sub compareOracleTypes($$$$){
    my ($st,$c,$ibt,$tmt) = @_;
    my $s;
    my $t;

    $ibtrigger = "";

    if($ibt eq $tmt) {return 0}

#    if($tmt =~ /DEFAULT NEXTVAL\S+/ && $ot =~ /\/\* POSTGRES NEXTVAL NEEDS TRIGGER \*\//){
#	$tmt =~ s/DEFAULT NEXTVAL\S+ //g;
#	$ibt =~ s/\/\*[^*]+\*\/ //g;
#    }

    $ibt =~ s/\s+/ /g;
    $tmt =~ s/\s+/ /g;

    $ibt =~ s/NUMBER \(/NUMBER(/;
    $ibt =~ s/VARCHAR2 \(/VARCHAR2(/;
    $ibt =~ s/VARCHAR \(/VARCHAR(/;

    # clean up quotes around column name

    $ibt =~ s/\"([A-Za-z0-9_]+)\"/$1/;
    $tmt =~ s/\"([A-Za-z0-9_]+)\"/$1/;

# clean up matching NULL and NOT NULL with optional ENABLE
   
    if($ibt =~ / NOT NULL ENABLE$/ && $tmt =~ / NOT NULL ENABLE$/) {
	$ibt =~ s/ NOT NULL ENABLE$//;
	$tmt =~ s/ NOT NULL ENABLE$//;
    }

    if($ibt =~ / NOT NULL$/ && $tmt =~ / NOT NULL ENABLE$/) {
	$ibt =~ s/ NOT NULL//;
	$tmt =~ s/ NOT NULL ENABLE$//;
    }

    if($ibt =~ / NOT NULL$/ && $tmt =~ / NOT NULL$/) {
	$ibt =~ s/ NOT NULL$//;
	$tmt =~ s/ NOT NULL$//;
    }

    if($ibt =~ /DATE NULL$/ && $tmt =~ /DATE$/) {
	$ibt =~ s/ NULL$//;
	$tmt =~ s/ NULL$//;
    }

    if($ibt =~ /(\S\S\S) NULL$/) {
	if($1 ne "NOT" && $tmt =~ /(\S\S\S) NULL$/) {
	    if($1 ne "NOT") {
		$ibt =~ s/ NULL$//;
		$tmt =~ s/ NULL$//;
	    }
	}
    }

    if($tmt !~ / NULL$/) {
	if($ibt =~ /(\S\S\S) NULL$/) {
	    if($1 ne "NOT") {
		$ibt =~ s/ NULL$//;
	    }
	}
    }

    if($ibt !~ / NULL$/) {
	if($tmt =~ /(\S\S\S) NULL$/) {
	    if($1 ne "NOT") {
		$tmt =~ s/ NULL$//;
	    }
	}
    }

    if($ibt =~ /DATE NULL$/ && $tmt =~ /TIMESTAMP NULL$/) {
	$ibt =~ s/ NULL$//;
	$tmt =~ s/ NULL$//;
    }

    $ibt =~ s/ WITH LOCAL TIME ZONE//g; # only allows local time display, storage unchanged

    if($tmt =~ /^BIGINT/) {
	if($ibt =~ /^NUMBER\s/ || $ibt eq "NUMBER") {
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	elsif($ot =~ /^NUMBER\((\d+),0\)/){
	    if($1 >= 1 && $1 <= 22) {
		$ibt =~ s/^\S+/matched/;
		$tmt =~ s/^\S+/matched/;
	    }
	}
	elsif($ibt =~ /^NUMBER\([*],0\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($tmt =~ /^BIGSERIAL/) {	# used for unique identifiers in i2b2 postgres
	if($ibt =~ /^NUMBER\((\d+),0\)/){
	    if($1 >= 1 && $1 <= 22) {
		$ibt =~ s/^\S+/matched/;
		$tmt =~ s/^\S+/matched/;
	    }
	}
	elsif($ibt =~ /^NUMBER\([*],0\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	elsif($ibt =~ /^NUMBER\s/ || $ibt eq "NUMBER") {
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($ibt =~ /^INT/) {
	if($tmt =~ /^NUMBER\([*],0\)/) {
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
    }

    elsif($tmt =~ /^INT/) {
	if($ibt =~ /^INTEGER/) {
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	elsif($ibt =~ /^NUMBER\((\d+),0\)/){
	    if($1 >= 1 && $1 <= 22) {
		$ibt =~ s/^\S+/matched/;
		$tmt =~ s/^\S+/matched/;
	    }
	}
	elsif($ibt =~ /^NUMBER\([*],0\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	elsif($ibt =~ /^NUMBER\s/ || $ibt eq "NUMBER") {
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($tmt =~ /^SERIAL/) {	# used for unique identifiers in i2b2 postgres
	if($ibt =~ /^NUMBER\s/ || $ibt eq "NUMBER") {
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	elsif($ibt =~ /^NUMBER\((\d+),0\)/){
	    if($1 >= 5 && $1 <= 22) {
		$ibt =~ s/^\S+/matched/;
		$tmt =~ s/^\S+/matched/;
	    }
	}
	elsif($ibt =~ /^NUMBER\([*],0\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($tmt =~ /^SMALLINT/) {
	if($ibt =~ /^NUMBER\s/ || $ibt eq "NUMBER") {
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	elsif($ibt =~ /^NUMBER\((\d+),0\)/){
	    if($1 >= 1 && $1 <= 4) {
		$ibt =~ s/^\S+/matched/;
		$tmt =~ s/^\S+/matched/;
	    }
	}
	elsif($ibt =~ /^NUMBER\([*],0\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($tmt =~ /^BOOLEAN DEFAULT FALSE/) { # treat boolean as never NULL
	if($ibt =~ /^NUMBER\(1,0\) DEFAULT 0 NOT NULL ENABLE/){
	    $ibt =~ s/^\S+ DEFAULT 0 NOT NULL ENABLE/matched/;
	    $tmt =~ s/^\S+ DEFAULT FALSE/matched/;
	}
	elsif($ibt =~ /^CHAR\(1 BYTE\) DEFAULT 0/){
	    $ibt =~ s/^\S+ \S+ DEFAULT 0/matched/;
	    $tmt =~ s/^\S+ DEFAULT FALSE/matched/;
	}
	else {return 1}
    }

    elsif($tmt =~ /^BOOLEAN/) {
	if($ibt =~ /^NUMBER\(1,0\)/) {
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	elsif($ibt =~ /^CHAR\(1 BYTE\)/) {
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($tmt =~ /^DOUBLE PRECISION/) {
	if($ibt =~ /^NUMBER\((\d+),(\d+)\)/){
	    if($1 >= 9 && $1 <= 38 && $2 > 0) {
		$ibt =~ s/^\S+/matched/;
		$tmt =~ s/^\S+ \S+/matched/;
	    }
	}
	elsif($ibt =~ /^NUMBER\s/ || $ot eq "NUMBER"){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	elsif($ibt =~ /^FLOAT\((\d+)\)/){ # (n) is the precision
	    if($1 > 0) {
		$ibt =~ s/^\S+/matched/;
		$tmt =~ s/^\S+ \S+/matched/;
	    }
	}
	elsif($ibt =~ /^BINARY_DOUBLE/){ # (n) is the precision
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	else {return 1}
    }

    elsif($tmt =~ /^DECIMAL\((\d+),(\d+)\)/) {
	$size=$1;
	$prec=$2;
	if($ibt =~ /NUMBER\($size,$prec\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($tmt =~ /^NUMERIC\((\d+),(\d+)\)/) {
	$size=$1;
	$prec=$2;
	if($ibt =~ /NUMBER\($size,$prec\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($tmt =~ /^NUMERIC/) {
	$size=$1;
	$prec=$2;
	if($ibt =~ /NUMBER\(([*]|\d+),(\d+)\)/){
	    if($2 == 0) {
		$ibt =~ s/^\S+/matched/;
		$tmt =~ s/^\S+/matched/;
	    }
	}
	elsif($ibt =~ /NUMBER\(/){
	    return 1;
	}
	elsif($ibt =~ /NUMBER/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($tmt =~ /^VARCHAR2?\((\d+)\)/) {
	$size = $1;
	if($ibt =~ /^VARCHAR2?\($size BYTE\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	if($ibt =~ /^VARCHAR2?\($size CHAR\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	if($ibt =~ /^VARCHAR2?\($size\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	if($ibt =~ /^NVARCHAR2\($size BYTE\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	if($ibt =~ /^NVARCHAR2\($size CHAR\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	if($ibt =~ /^NVARCHAR2\($size\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	if($ibt =~ / DEFAULT \'([^\']*)\'$/) {
	    if($tmt =~ /DEFAULT \'$oval\'::CHARACTER VARYING$/) {
		$ibt =~ s/ DEFAULT \'([^\']*)\'$//;
		$tmt =~ s/ DEFAULT \'$oval\'::CHARACTER VARYING$//;
	    }
	}
    }

    elsif($tmt =~ /^VARCHAR2?\((\d+) BYTE\)/) {
	$size = $1;
	if($ibt =~ /^VARCHAR2?\($size BYTE\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ /^VARCHAR2?\($size CHAR\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ /^VARCHAR2?\($size\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ /^NVARCHAR2\($size BYTE\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ /^NVARCHAR2\($size CHAR\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ /^NVARCHAR2\($size\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ / DEFAULT \'([^\']*)\'$/) {
	    $oval = $1;
	    if($tmt =~ /DEFAULT \'$oval\'::CHARACTER VARYING$/) {
		$ibt =~ s/ DEFAULT \'([^\']*)\'$//;
		$tmt =~ s/ DEFAULT \'$oval\'::CHARACTER VARYING$//;
	    }
	}
    }

    elsif($tmt =~ /^CHARACTER VARYING\((\d+)\)/) {
	$size = $1;
	if($ibt =~ /^VARCHAR2?\($size BYTE\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ /^VARCHAR2?\($size CHAR\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ /^VARCHAR2?\($size\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ /^NVARCHAR2\($size BYTE\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ /^NVARCHAR2\($size CHAR\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ /^NVARCHAR2\($size\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ /^CLOB/ && $size >= 2000){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($ibt =~ / DEFAULT \'([^\']*)\'$/) {
	    $oval = $1;
	    if($tmt =~ /DEFAULT \'$oval\'::CHARACTER VARYING$/) {
		$ibt =~ s/ DEFAULT \'([^\']*)\'$//;
		$tmt =~ s/ DEFAULT \'$oval\'::CHARACTER VARYING$//;
	    }
	}
    }

    elsif($tmt =~ /^CHARACTER\((\d+)\)/) {
	$size = $1;
	if($ibt =~ /^CHAR\($size BYTE\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	if($ibt =~ /^CHAR\($size CHAR\)/){
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
    }

    elsif($ibt =~ /^CHAR\((\d+)\)/) {
	$size = $1;
	if($tmt =~ /^CHAR\($size BYTE\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
	if($tmt =~ /^CHAR\($size CHAR\)/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+ \S+/matched/;
	}
    }

    elsif($tmt =~ /^OID/) {
	$size = $1;
	if($ibt =~ /^BLOB/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
	if($ibt =~ /^CLOB/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
    }

    elsif($tmt =~ /^BYTEA/) {
	$size = $1;
	if($ibt =~ /^CLOB/){
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
    }

    elsif($tmt =~ /^TIMESTAMP(\((\d)\))? WITHOUT TIME ZONE/){
	if(!defined($1)) {$it = 6}
	else {$it = $2}
	if($ibt =~ /^DATE/) {
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+ \S+ \S+ \S+/matched/;
	}
	elsif($ibt =~ /^TIMESTAMP \(9\)/ && $it == 6) {
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+ \S+ \S+ \S+/matched/;
	}
	elsif($ibt =~ /^TIMESTAMP$/ && $it == 6) {
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+ \S+ \S+ \S+/matched/;
	}
	elsif($ibt =~ /^TIMESTAMP \($it\)/) {
	    $ibt =~ s/^\S+ \S+/matched/;
	    $tmt =~ s/^\S+ \S+ \S+ \S+/matched/;
	}
	if($tmt =~ / DEFAULT NOW\(\)/ && $ibt =~ / DEFAULT SYSDATE/) {
	    $ibt =~ s/ DEFAULT \S+//;
	    $tmt =~ s/ DEFAULT \S+//;
	}
	if($tmt =~ / DEFAULT CURRENT_TIMESTAMP/ && $ibt =~ / DEFAULT SYSDATE/) {
	    $ibt =~ s/ DEFAULT \S+//;
	    $tmt =~ s/ DEFAULT \S+//;
	}
    }
    elsif($tmt =~ /^TIMESTAMP (\(6\))/){
	if($ibt =~ /^DATE$/) {
	    $ibt =~ s/\S+$//;
	    $tmt =~ s/\S+ \S+$//;
	}
	if($ibt =~ /^TIMESTAMP$/) {
	    $ibt =~ s/\S+$//;
	    $tmt =~ s/\S+ \S+$//;
	}
    }
    elsif($tmt =~ /^TIMESTAMP(\(\d\))/){
	if($ibt =~ /^TIMESTAMP \(\d\)/) {
	    $ibt =~ s/^(\S+) (\S+)/$1$2/;
	}
    }
    elsif($tmt =~ /^TIMESTAMP$/){
	if($ibt =~ /^DATE$/) {
	    $ibt =~ s/\S+$//;
	    $tmt =~ s/\S+$//;
	}
	if($ibt =~ /^TIMESTAMP \(6\)$/) {
	    $ibt =~ s/\S+ \S+$//;
	    $tmt =~ s/\S+$//;
	}
	if($ibt =~ /^TIMESTAMP \(9\)$/) {
	    $ibt =~ s/\S+ \S+$//;
	    $tmt =~ s/\S+$//;
	}
	if($tmt =~ / DEFAULT NOW\(\)/ && $ibt =~ / DEFAULT SYSDATE/) {
	    $ibt =~ s/ DEFAULT \S+//;
	    $tmt =~ s/ DEFAULT \S+//;
	}
    }
    elsif($tmt =~ /^DATE\b/){
	if($ibt =~ /^DATE\b/) {
	    $ibt =~ s/^(\S+)/matched/;
	    $tmt =~ s/^(\S+)/matched/;
	}
	if($tmt =~ / DEFAULT NOW\(\)/ && $ibt =~ / DEFAULT SYSDATE/) {
	    $ibt =~ s/ DEFAULT \S+//;
	    $tmt =~ s/ DEFAULT \S+//;
	}
    }
    elsif($tmt =~ /^TEXT/){
	if($ibt =~ /^N?[CB]LOB/) {
	    $ibt =~ s/^\S+/matched/;
	    $tmt =~ s/^\S+/matched/;
	}
    }

    if($tmt =~ /DEFAULT '\S'::BPCHAR/) {
	$tmt =~ s/(DEFAULT '\S')::BPCHAR/$1/;
    }

    if($tmt =~ /^matched DEFAULT NEXTVAL\(\'([^\']+)\'::REGCLASS\)/) {
	$sq = $1;
	($s,$t) = ($st =~ /([^.]+)[.](.*)/);
	my $ibnv = "";
	my $tmnv = "";
	if(defined($ioNextval{"$s.$t"})) {$ibnv = $ioNextval{"$s.$t"}}
	if(defined($oNextval{"$s.$t"})) {$tmnv = $oNextval{"$s.$t"}}
	if($ibt eq "matched" && $ibnv eq $tmnv) {
	    $ibt =~ s/ \S+$//;
	    $tmt =~ s/ \S+ \S+$//;
	}
	else {
	    print STDERR "Create trigger ibnv: '$ibnv' tmnv: '$tmnv'\n";
	    my $trig = "TRG_$t\_$c";
	    $trig =~ s/PATIENT/PAT/g;
	    $trig =~ s/COLLECTION/COL/g;
	    $trig =~ s/QUERY_MASTER/QM/g;
	    $trig =~ s/QUERY_RESULT_INSTANCE/QRI/g;
	    $trig =~ s/QUERY_INSTANCE/QI/g;
	    $trig =~ s/QUERY_RESULT/QR/g;
	    $trig =~ s/RESULT_INSTANCE/RI/g;
	    $trig =~ s/QUERY/Q/g;
	    $trig =~ s/RESULT/RES/g;
	    $ibtrigger = "--
-- Type: TRIGGER; Owner: $s; Name: $trig
--
  CREATE OR REPLACE TRIGGER \"$s\".\"$trig\" 
   before insert on \"$s\".\"$t\" 
   for each row 
begin  
   if inserting then 
      if :NEW.\"$c\" is null then 
         select $sq.nextval into :NEW.\"$c\" from dual; 
      end if; 
   end if; 
end;
/
ALTER TRIGGER \"$s\".\"$trig\" ENABLE;
";

	}
    }
    if($ibt eq $tmt) {return 0} 

    return 1;
}

sub compareColumns($$){
    my ($otc,$ptc) = @_;
    if(!defined($ptc)){
	print STDERR "Unknown compareColumns second table for $t\n";
	return "Unknown compareColumns second table for $t\n";
    }
    my @ocols = split(/;/,$otc);
    my @pcols = split(/;/,$ptc);
    my $c;
    my %ocol = ();
    my %pcol = ();
    my $col;
    my $def;
    my $compstr = "";

    $head = "Compare table $t\n";
    foreach $c (@ocols) {
	($col, $def) = ($c =~ /^(\S+)\s+(.*)/);
	$col = uc($col);
	$def = uc($def);
	$ocol{$col} = $def;
    }

    foreach $c (@pcols) {
	($col, $def) = ($c =~ /^(\S+)\s+(.*)/);
	$col = uc($col);
	$def = uc($def);
	$pcol{$col} = $def;
    }

    $onewcol = 0;
    $pnewcol = 0;
    foreach $c (sort(keys(%ocol))) {
	if(!defined($pcol{$c})) {
	    $pnewcol++;
	    $compstr .= sprintf "$head"."column not in Postgres:  %-32s %s\n", $c, $ocol{$c};
	    $head = "";
	}
	elsif(compareTypes($t,$c,$ocol{$c},$pcol{$c})) {
	    $compstr .= sprintf "$head"."column %-32s %-45s <=> %-45s\n",
	                        $c, "'$ocol{$c}'", "'$pcol{$c}'";
            if($otrigger ne "") {$compstr .= $otrigger}
	    $head="";
	}
	else {
#	    printf "column %-32s matched\n", $c
	}
    }
    foreach $c (sort(keys(%pcol))) {
	if(!defined($ocol{$c})) {
	    $onewcol++;
	    $compstr .= sprintf "$head"."column not in Oracle:  %-32s %s\n", $c, $pcol{$c};
	    $head="";
	}
    }

    # Compare primary key constraints

    my $okey = "undefined";
    my $pkey = "undefined";
    my @okey = ();
    my @pkey = ();

    if(defined($oTablePrikey{$t})){$okey = $oTablePrikey{$t}}
    if(defined($pTablePrikey{$t})){$pkey = $pTablePrikey{$t}}
    if($okey ne $pkey) {
	$compstr .= "PRIMARY KEY     $okey    $pkey\n";
	if($okey eq "undefined") {
	    if(!defined($pTablePrikeyName{$t})) {
		$compstr .= "PRIMARY KEY (\"$pTablePrikey{$t}\")\n";
	    }else{
		$compstr .= "CONSTRAINT \"$pTablePrikeyName{$t}\" PRIMARY KEY (\"$pTablePrikey{$t}\")\n";
	    }
	}
	if($pkey eq "undefined") {
	    $lk = lc($oTablePrikey{$t});
	    if(!defined($oTablePrikeyName{$t})) {
		$compstr .= "PRIMARY KEY (lk)\n";
	    }else{
		($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
		$lc = lc($oTablePrikeyName{$t});
		$compstr .= "--
-- Name: $lc; Type: CONSTRAINT; Schema: $ts; Owner: -
--
ALTER TABLE ONLY $tt\n";
 		$compstr .= "    ADD CONSTRAINT $lc PRIMARY KEY ($lk);\n";
	    }
	}
    }

    # Compare unique key constraints

    $okey=0;
    $pkey=0;
    if(defined($oTableUnikey{$t})){@okey = sort(split(/;/,$oTableUnikey{$t}));$okey=$#okey+1}
    if(defined($pTableUnikey{$t})){@pkey = sort(split(/;/,$pTableUnikey{$t}));$pkey=$#pkey+1}
    if($okey && $pkey) {
	if($okey == $pkey) {
	    for ($i = 0; $i < $okey; $i++) {
		if($okey[$i] ne $pkey[$i]) {
		    $compstr .= "UNIQUE      oracle: $okey[$i]\n";
		    $compstr .= "          postgres: $pkey[$i]\n";
		}
	    }
	}
	else {
	    $compstr .= "UNIQUE      count $okey    $pkey\n";
	    for ($i = 0; $i < $okey; $i++) {
		$compstr .= "            oracle: $okey[$i]\n";
	    }
	    for ($i = 0; $i < $pkey; $i++) {
		$compstr .= "          postgres: $pkey[$i]\n";
	    }
	}
    }
    elsif($#okey >= 0) {
	$compstr .= "UNIQUE oracle only $okey\n";
	for ($i = 0; $i < $okey; $i++) {
	    $pk = lc($okey[$i]);
	    $compstr .= "            oracle: $pk\n";
	}
	for ($i = 0; $i < $okey; $i++) {
	    ($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
	    $ts = lc($ts);
	    $tt = lc($tt);
	    ($kn,$ki) = ($pk =~ /(\S+) (\S+)/);
	    $compstr .= "--
-- Name: $kn; Type: CONSTRAINT; Schema: $ts; Owner: -
--
ALTER TABLE ONLY $tt
    ADD CONSTRAINT $kn UNIQUE ($ki);\n"
	}
    }
    elsif($#pkey >= 0) {
	$compstr .= "UNIQUE postgres only $pkey\n";
	for ($i = 0; $i < $pkey; $i++) {
	    $compstr .= "          postgres: $pkey[$i]\n";
	}
	($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
	for ($i = 0; $i < $pkey; $i++) {
	    ($kn,$ki) = ($pkey[$i] =~ /(\S+) (\S+)/);
	    $ki =~ s/,/\",\"/g;
	    $compstr .= "--
-- Type: REF_CONSTRAINT; Owner: $ts; Name: $kn
--
ALTER TABLE \"$ts\".\"$tt\"
    ADD CONSTRAINT \"$kn\" UNIQUE (\"$ki\");
";
	}
    }

#    if($okey ne $pkey) {
#	$compstr .= "UNIQUE     $okey    $pkey\n";
#	if($okey eq "undefined") {
#	    $uk = $pTableUnikey{$t};
#	    $uk =~ s/,/\",\"/g;
#	    if(!defined($pTableUnikeycon{$t})) {
#		$compstr .= "UNIQUE (\"$uk\")\n";
#	    }else{
#		$compstr .= "CONSTRAINT \"$pTableUnikeycon{$t}\" UNIQUE (\"$uk\")\n";
#	    }
#	}
#	if($pkey eq "undefined") {
#	    $lk = lc($oTableUnikey{$t});
#	    if(!defined($oTableUnikeycon{$t})) {
#		$compstr .= "UNIQUE ($lk)\n";
#	    }else{
#		($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
#		$lc = lc($oTableUnikeycon{$t});
#		$compstr .= "--
#-- Name: $lc; Type: CONSTRAINT; Schema: $ts; Owner: -
#--
#ALTER TABLE ONLY $tt
#    ADD CONSTRAINT $lc UNIQUE ($lk);\n"
#	    }
#	}
#   }

    # compare foreign key constraints

    @okey = ();
    @pkey = ();
    $okey=0;
    $pkey=0;
    if(defined($oTableForkey{$t})){@okey = sort(split(/;/,$oTableForkey{$t}));$okey=$#okey+1}
    if(defined($pTableForkey{$t})){@pkey = sort(split(/;/,$pTableForkey{$t}));$pkey=$#pkey+1}
    
    if($okey && $pkey) {
	if($okey == $pkey) {
	    for ($i = 0; $i < $okey; $i++) {
		if($okey[$i] ne $pkey[$i]) {
		    $compstr .= "FOREIGN KEY oracle: $okey[$i]\n";
		    $compstr .= "          postgres: $pkey[$i]\n";
		}
	    }
	}
	else {
	    $compstr .= "FOREIGN KEY count $okey    $pkey\n";
	    for ($i = 0; $i < $okey; $i++) {
		$compstr .= "            oracle: $okey[$i]\n";
	    }
	    for ($i = 0; $i < $pkey; $i++) {
		$compstr .= "          postgres: $pkey[$i]\n";
	    }
	}
    }
    elsif($#okey >= 0) {
	$compstr .= "FOREIGN KEY oracle only $okey\n";
	    for ($i = 0; $i < $okey; $i++) {
		$pk = lc($okey[$i]);
		$compstr .= "            oracle: $pk\n";
	    }
    }
    elsif($#pkey >= 0) {
	$compstr .= "FOREIGN KEY postgres only $pkey\n";
	for ($i = 0; $i < $pkey; $i++) {
	    $compstr .= "          postgres: $pkey[$i]\n";
	}
	($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
	for ($i = 0; $i < $pkey; $i++) {
	    ($kn,$ki,$ks,$kt,$kr) = ($pkey[$i] =~ /(\S+) \(([^\)]+)\) ([^.]+)[.]([^\(]+)\(([^\)]+)\)/);
	    $compstr .= "--
-- Type: REF_CONSTRAINT; Owner: $ts; Name: $kn
--
ALTER TABLE \"$ts\".\"$tt\" ADD CONSTRAINT \"$kn\" FOREIGN KEY (\"$ki\")
 REFERENCES \"$ks\".\"$kt\" (\"$kr\") ENABLE;
";
	}
    }

    # Compare index definitions

    $okey = 0;
    $pkey = 0;
    @okey = ();
    @pkey = ();

    if(defined($oIndex{$t})){@okey = sort(split(/;/,$oIndex{$t}));$okey=$#okey+1}
    if(defined($pIndex{$t})){@pkey = sort(split(/;/,$pIndex{$t}));$pkey=$#pkey+1}

    if($okey && $pkey) {
	my $jo = 0;
	my $jp = 0;
	my $cmp;
	while($jo < $okey && $jp < $pkey) {
	    $cmp = ($okey[$jo] cmp $pkey[$jp]);
	    if($cmp == 0) {	# two indexes match
#		$compstr .= "indexboth        oracle: $okey[$jo]\n";
#		$compstr .= "indexboth      postgres: $pkey[$jp]\n";
		++$jo;
		++$jp;
	    } elsif ($cmp < 0) { # okey extra
		$compstr .= "INDEXONLY        oracle: $okey[$jo]\n";
		++$jo;
	    } else {	     # pkey extra
		$compstr .= "INDEXONLY      postgres: $pkey[$jp]\n";
		++$jp;
	    }
	}
	while($jo < $okey) {
	    $compstr .= "INDEXONLY        oracle: $okey[$jo]\n";
	    ++$jo;
	}
	while($jp < $pkey) {
	    $compstr .= "INDEXONLY      postgres: $pkey[$jp]\n";
	    ++$jp;
	}
    } elsif($okey) {
	for ($i = 0; $i < $okey; $i++) {
	    $compstr .= "INDEXONLY        oracle: $okey[$i] for '$t'\n";
	}
    } elsif($pkey) {
	for ($i = 0; $i < $pkey; $i++) {
	    $compstr .= "INDEXONLY      postgres: $pkey[$i] for '$t'\n";
	}
    }

    if($head eq "") {$compstr .= "\n"}
    return $compstr;
}

sub compareI2b2Columns($$){
    my ($otc,$ptc) = @_;
    my @ocols = split(/;/,$otc);
    my @pcols = split(/;/,$ptc);
    my $c;
    my %ocol = ();
    my %pcol = ();
    my $col;
    my $def;
    my $compstr = "";

    $head = "Compare table $t\n";
    foreach $c (@ocols) {
	($col, $def) = ($c =~ /^(\S+)\s+(.*)/);
	$col = uc($col);
	$def = uc($def);
	$ocol{$col} = $def;
    }

    foreach $c (@pcols) {
	($col, $def) = ($c =~ /^(\S+)\s+(.*)/);
	$col = uc($col);
	$def = uc($def);
	$pcol{$col} = $def;
    }

    $ionewcol = 0;
    $ipnewcol = 0;
    foreach $c (sort(keys(%ocol))) {
	if(!defined($pcol{$c})) {
	    $ipnewcol++;
	    $compstr .= sprintf "$head"."column not in PostgresI2b2:  %-32s %s\n", $c, $ocol{$c};
	    $head = "";
	}
	elsif(compareTypes($t,$c,$ocol{$c},$pcol{$c})) { 		# compares postgres to i2b2 for transmart or i2b2
	    $compstr .= sprintf "$head"."column %-32s %-45s <=> %-45s\n",
	                        $c, "'$ocol{$c}'", "'$pcol{$c}'";
            if($otrigger ne "") {$compstr .= $otrigger}
	    $head="";
	}
	else {
#	    printf "column %-32s matched\n", $c
	}
    }
    foreach $c (sort(keys(%pcol))) {
	if(!defined($ocol{$c})) {
	    $ionewcol++;
	    $compstr .= sprintf "$head"."column not in OracleI2b2:  %-32s %s\n", $c, $pcol{$c};
	    $head="";
	}
    }

    my $okey = "undefined";
    my $pkey = "undefined";
    my @okey = ();
    my @pkey = ();

    if(defined($ioTablePrikey{$t})){$okey = $ioTablePrikey{$t}}
    if(defined($ipTablePrikey{$t})){$pkey = $ipTablePrikey{$t}}
    if($okey ne $pkey) {
	$compstr .= "PRIMARY KEY     $okey    $pkey\n";
	if($okey eq "undefined") {
	    if(!defined($ipTablePrikeyName{$t})) {
		$compstr .= "PRIMARY KEY (\"$pTablePrikey{$t}\")\n";
	    }else{
		$compstr .= "CONSTRAINT \"$pTablePrikeyName{$t}\" PRIMARY KEY (\"$pTablePrikey{$t}\")\n";
	    }
	}
	if($pkey eq "undefined") {
	    $lk = lc($ioTablePrikey{$t});
	    if(!defined($ioTablePrikeyName{$t})) {
		$compstr .= "PRIMARY KEY (lk)\n";
	    }else{
		($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
		$lc = lc($ioTablePrikeyName{$t});
		$compstr .= "--
-- Name: $lc; Type: CONSTRAINT; Schema: $ts; Owner: -
--
ALTER TABLE ONLY $tt\n";
 		$compstr .= "    ADD CONSTRAINT $lc PRIMARY KEY ($lk);\n";
	    }
	}
    }

    $okey=0;
    $pkey=0;
    if(defined($ioTableUnikey{$t})){@okey = sort(split(/;/,$ioTableUnikey{$t}));$okey=$#okey+1}
    if(defined($ipTableUnikey{$t})){@pkey = sort(split(/;/,$ipTableUnikey{$t}));$pkey=$#pkey+1}
    if($okey && $pkey) {
	if($okey == $pkey) {
	    for ($i = 0; $i < $okey; $i++) {
		if($okey[$i] ne $pkey[$i]) {
		    $compstr .= "UNIQUE      oracle i2b2: $okey[$i]\n";
		    $compstr .= "          postgres i2b2: $pkey[$i]\n";
		}
	    }
	}
	else {
	    $compstr .= "UNIQUE      count $okey    $pkey\n";
	    for ($i = 0; $i < $okey; $i++) {
		$compstr .= "            oracle i2b2: $okey[$i]\n";
	    }
	    for ($i = 0; $i < $pkey; $i++) {
		$compstr .= "          postgres i2b2: $pkey[$i]\n";
	    }
	}
    }
    elsif($#okey >= 0) {
	$compstr .= "UNIQUE oracle i2b2 only $okey\n";
	for ($i = 0; $i < $okey; $i++) {
	    $pk = lc($okey[$i]);
	    $compstr .= "            oracle i2b2: $pk\n";
	}
	for ($i = 0; $i < $okey; $i++) {
	    ($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
	    $ts = lc($ts);
	    $tt = lc($tt);
	    ($kn,$ki) = ($pk =~ /(\S+) (\S+)/);
	    $compstr .= "--
-- Name: $kn; Type: CONSTRAINT; Schema: $ts; Owner: -
--
ALTER TABLE ONLY $tt
    ADD CONSTRAINT $kn UNIQUE ($ki);\n"
	}
    }
    elsif($#pkey >= 0) {
	$compstr .= "UNIQUE postgres i2b2 only $pkey\n";
	for ($i = 0; $i < $pkey; $i++) {
	    $compstr .= "          postgres i2b2: $pkey[$i]\n";
	}
	($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
	for ($i = 0; $i < $pkey; $i++) {
	    ($kn,$ki) = ($pkey[$i] =~ /(\S+) (\S+)/);
	    $ki =~ s/,/\",\"/g;
	    $compstr .= "--
-- Type: REF_CONSTRAINT; Owner: $ts; Name: $kn
--
ALTER TABLE \"$ts\".\"$tt\"
    ADD CONSTRAINT \"$kn\" UNIQUE (\"$ki\");
";
	}
    }

#    if($okey ne $pkey) {
#	$compstr .= "UNIQUE     $okey    $pkey\n";
#	if($okey eq "undefined") {
#	    $uk = $pTableUnikey{$t};
#	    $uk =~ s/,/\",\"/g;
#	    if(!defined($ipTableUnikeycon{$t})) {
#		$compstr .= "UNIQUE (\"$uk\")\n";
#	    }else{
#		$compstr .= "CONSTRAINT \"$ipTableUnikeycon{$t}\" UNIQUE (\"$uk\")\n";
#	    }
#	}
#	if($pkey eq "undefined") {
#	    $lk = lc($ioTableUnikey{$t});
#	    if(!defined($ioTableUnikeycon{$t})) {
#		$compstr .= "UNIQUE ($lk)\n";
#	    }else{
#		($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
#		$lc = lc($ioTableUnikeycon{$t});
#		$compstr .= "--
#-- Name: $lc; Type: CONSTRAINT; Schema: $ts; Owner: -
#--
#ALTER TABLE ONLY $tt
#    ADD CONSTRAINT $lc UNIQUE ($lk);\n"
#	    }
#	}
#   }

    @okey = ();
    @pkey = ();
    $okey=0;
    $pkey=0;
    if(defined($ioTableForkey{$t})){@okey = sort(split(/;/,$ioTableForkey{$t}));$okey=$#okey+1}
    if(defined($ipTableForkey{$t})){@pkey = sort(split(/;/,$ipTableForkey{$t}));$pkey=$#pkey+1}
    
    if($okey && $pkey) {
	if($okey == $pkey) {
	    for ($i = 0; $i < $okey; $i++) {
		if($okey[$i] ne $pkey[$i]) {
		    $compstr .= "FOREIGN KEY oracle i2b2: $okey[$i]\n";
		    $compstr .= "          postgres i2b2: $pkey[$i]\n";
		}
	    }
	}
	else {
	    $compstr .= "FOREIGN KEY count $okey    $pkey\n";
	    for ($i = 0; $i < $okey; $i++) {
		$compstr .= "            oracle i2b2: $okey[$i]\n";
	    }
	    for ($i = 0; $i < $pkey; $i++) {
		$compstr .= "          postgres i2b2: $pkey[$i]\n";
	    }
	}
    }
    elsif($#okey >= 0) {
	$compstr .= "FOREIGN KEY oracle i2b2 only $okey\n";
	    for ($i = 0; $i < $okey; $i++) {
		$pk = lc($okey[$i]);
		$compstr .= "            oracle i2b2: $pk\n";
	    }
    }
    elsif($#pkey >= 0) {
	$compstr .= "FOREIGN KEY postgres i2b2 only $pkey\n";
	for ($i = 0; $i < $pkey; $i++) {
	    $compstr .= "          postgres i2b2: $pkey[$i]\n";
	}
	($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
	for ($i = 0; $i < $pkey; $i++) {
	    ($kn,$ki,$ks,$kt,$kr) = ($pkey[$i] =~ /(\S+) \(([^\)]+)\) ([^.]+)[.]([^\(]+)\(([^\)]+)\)/);
	    $compstr .= "--
-- Type: REF_CONSTRAINT; Owner: $ts; Name: $kn
--
ALTER TABLE \"$ts\".\"$tt\" ADD CONSTRAINT \"$kn\" FOREIGN KEY (\"$ki\")
 REFERENCES \"$ks\".\"$kt\" (\"$kr\") ENABLE;
";
	}
    }

    # Compare index definitions

    $okey = 0;
    $pkey = 0;
    @okey = ();
    @pkey = ();

    if(defined($ioIndex{$t})){@okey = sort(split(/;/,$ioIndex{$t}));$okey=$#okey+1}
    if(defined($ipIndex{$t})){@pkey = sort(split(/;/,$ipIndex{$t}));$pkey=$#pkey+1}
    if($okey && $pkey) {
	my $jo = 0;
	my $jp = 0;
	my $cmp;
	while($jo < $okey && $jp < $pkey) {
	    $cmp = ($okey[$jo] cmp $pkey[$jp]);
	    if($cmp == 0) {	# two indexes match
#		$compstr .= "indexboth   oracle i2b2: $okey[$jo]\n";
#		$compstr .= "indexboth postgres i2b2: $pkey[$jp]\n";
		++$jo;
		++$jp;
	    } elsif ($cmp < 0) { # okey extra
		$compstr .= "INDEXONLY   oracle i2b2: $okey[$jo]\n";
		++$jo;
	    } else {	     # pkey extra
		$compstr .= "INDEXONLY postgres i2b2: $pkey[$jp]\n";
		++$jp;
	    }
	}
	while($jo < $okey) {
	    $compstr .= "INDEXONLY   oracle i2b2: $okey[$jo]\n";
	    ++$jo;
	}
	while($jp < $pkey) {
	    $compstr .= "INDEXONLY postgres i2b2: $pkey[$jp]\n";
	    ++$jp;
	}
    } elsif($okey) {
	for ($i = 0; $i < $okey; $i++) {
	    $compstr .= "INDEXONLY   oracle i2b2: $okey[$i] for '$t'\n";
	}
    } elsif($pkey) {
	for ($i = 0; $i < $pkey; $i++) {
	    $compstr .= "INDEXONLY postgres i2b2: $pkey[$i] for '$t'\n";
	}
    }

    if($head eq "") {$compstr .= "\n"}
    return $compstr;
}

########################################
# Compare Oracle I2B2 v Oracle TranSMART
########################################

sub compareOracleColumns($$){
    my ($ibtc,$tmtc) = @_;
    $ibtc =~ s/\"//g;
    my @ibcols = split(/;/,$ibtc);
    my @tmcols = split(/;/,$tmtc);
    my $c;
    my %ibcol = ();
    my %tmcol = ();
    my $col;
    my $def;
    my $compstr = "";

    $head = "Compare table $t\n";
    foreach $c (@ibcols) {
	($col, $def) = ($c =~ /^(\S+)\s+(.*)/);
	$col = uc($col);
	$def = uc($def);
	$ibcol{$col} = $def;
    }

    foreach $c (@tmcols) {
	($col, $def) = ($c =~ /^(\S+)\s+(.*)/);
	$col = uc($col);
	$def = uc($def);
	$tmcol{$col} = $def;
    }

    $iibnewcol = 0;
    $itmnewcol = 0;
    foreach $c (sort(keys(%ibcol))) {
	if(!defined($tmcol{$c})) {
	    $iibnewcol++;
	    $compstr .= sprintf "$head"."column not in OracleTranSMART:  %-32s %s\n", $c, $ibcol{$c};
	    $head = "";
	}
	elsif(compareOracleTypes($t,$c,$ibcol{$c},$tmcol{$c})) {
	    $compstr .= sprintf "$head"."column %-32s %-45s <=> %-45s\n",
	                        $c, "'$ibcol{$c}'", "'$tmcol{$c}'";
            if($ibtrigger ne "") {$compstr .= $ibtrigger}
	    $head="";
	}
	else {
#	    printf "column %-32s matched\n", $c
	}
    }
    foreach $c (sort(keys(%tmcol))) {
	if(!defined($ibcol{$c})) {
	    $itmnewcol++;
	    $compstr .= sprintf "$head"."column not in OracleI2b2:  %-32s %s\n", $c, $tmcol{$c};
	    $head="";
	}
    }

    my $ibkey = "undefined";
    my $tmkey = "undefined";
    my @ibkey = ();
    my @tmkey = ();

# Compare PRIMARY keys
# --------------------
    
    if(defined($ioTablePrikey{$t})){$ibkey = $ioTablePrikey{$t}}
    if(defined($oTablePrikey{$t})){$tmkey = $oTablePrikey{$t}}
    if($ibkey ne $tmkey) {
	$compstr .= "PRIMARY KEY     $ibkey    $tmkey\n";
	if($ibkey eq "undefined") {
	    if(!defined($oTablePrikeyName{$t})) {
		$compstr .= "PRIMARY KEY (\"$pTablePrikey{$t}\")\n";
	    }else{
		$compstr .= "CONSTRAINT \"$pTablePrikeyName{$t}\" PRIMARY KEY (\"$pTablePrikey{$t}\")\n";
	    }
	}
	if($tmkey eq "undefined") {
	    $lk = lc($ioTablePrikey{$t});
	    if(!defined($ioTablePrikeyName{$t})) {
		$compstr .= "PRIMARY KEY (lk)\n";
	    }else{
		($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
		$lc = lc($ioTablePrikeyName{$t});
		$compstr .= "--
-- Name: $lc; Type: CONSTRAINT; Schema: $ts; Owner: -
--
ALTER TABLE ONLY $tt\n";
 		$compstr .= "    ADD CONSTRAINT $lc PRIMARY KEY ($lk);\n";
	    }
	}
    }

    $ibkey=0;
    $tmkey=0;
    if(defined($ioTableUnikey{$t})){@ibkey = sort(split(/;/,$ioTableUnikey{$t}));$ibkey=$#ibkey+1}
    if(defined($oTableUnikey{$t})){@tmkey = sort(split(/;/,$oTableUnikey{$t}));$tmkey=$#tmkey+1}
    if($ibkey && $tmkey) {
	if($ibkey == $tmkey) {
	    for ($i = 0; $i < $ibkey; $i++) {
		if($ibkey[$i] ne $tmkey[$i]) {
		    $compstr .= "UNIQUE      oracle: $ibkey[$i]\n";
		    $compstr .= "          postgres: $tmkey[$i]\n";
		}
	    }
	}
	else {
	    $compstr .= "UNIQUE      count $ibkey    $tmkey\n";
	    for ($i = 0; $i < $ibkey; $i++) {
		$compstr .= "            oracle: $ibkey[$i]\n";
	    }
	    for ($i = 0; $i < $tmkey; $i++) {
		$compstr .= "          postgres: $tmkey[$i]\n";
	    }
	}
    }
    elsif($#ibkey >= 0) {
	$compstr .= "UNIQUE oracle i2b2 only $ibkey\n";
	for ($i = 0; $i < $ibkey; $i++) {
	    $ibk[$i] = lc($ibkey[$i]);
	    $compstr .= "            oracle: $ibk[$i]\n";
	}
	for ($i = 0; $i < $ibkey; $i++) {
	    ($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
	    $ts = lc($ts);
	    $tt = lc($tt);
	    ($kn,$ki) = ($ibk[$i] =~ /(\S+) (\S+)/);
	    $compstr .= "--
-- Name: $kn; Type: CONSTRAINT; Schema: $ts; Owner: -
--
ALTER TABLE ONLY $tt
    ADD CONSTRAINT $kn UNIQUE ($ki);\n"
	}
    }
    elsif($#tmkey >= 0) {
	$compstr .= "UNIQUE oracle transmart only $tmkey\n";
	for ($i = 0; $i < $tmkey; $i++) {
	    $compstr .= "          postgres: $tmkey[$i]\n";
	}
	($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
	for ($i = 0; $i < $tmkey; $i++) {
	    ($kn,$ki) = ($tmkey[$i] =~ /(\S+) (\S+)/);
	    $ki =~ s/,/\",\"/g;
	    $compstr .= "--
-- Type: REF_CONSTRAINT; Owner: $ts; Name: $kn
--
ALTER TABLE \"$ts\".\"$tt\"
    ADD CONSTRAINT \"$kn\" UNIQUE (\"$ki\");
";
	}
    }

#    if($ibkey ne $tmkey) {
#	$compstr .= "UNIQUE     $ibkey    $tmkey\n";
#	if($ibkey eq "undefined") {
#	    $uk = $oTableUnikey{$t};
#	    $uk =~ s/,/\",\"/g;
#	    if(!defined($oTableUnikeycon{$t})) {
#		$compstr .= "UNIQUE (\"$uk\")\n";
#	    }else{
#		$compstr .= "CONSTRAINT \"$oTableUnikeycon{$t}\" UNIQUE (\"$uk\")\n";
#	    }
#	}
#	if($tmkey eq "undefined") {
#	    $lk = lc($ioTableUnikey{$t});
#	    if(!defined($ioTableUnikeycon{$t})) {
#		$compstr .= "UNIQUE ($lk)\n";
#	    }else{
#		($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
#		$lc = lc($ioTableUnikeycon{$t});
#		$compstr .= "--
#-- Name: $lc; Type: CONSTRAINT; Schema: $ts; Owner: -
#--
#ALTER TABLE ONLY $tt
#    ADD CONSTRAINT $lc UNIQUE ($lk);\n"
#	    }
#	}
#   }

    @ibkey = ();
    @tmkey = ();
    $ibkey=0;
    $tmkey=0;
    if(defined($ioTableForkey{$t})){@ibkey = sort(split(/;/,$ioTableForkey{$t}));$ibkey=$#ibkey+1}
    if(defined($oTableForkey{$t})){@tmkey = sort(split(/;/,$oTableForkey{$t}));$tmkey=$#tmkey+1}
    
    if($ibkey && $tmkey) {
	if($ibkey == $tmkey) {
	    for ($i = 0; $i < $ibkey; $i++) {
		if($ibkey[$i] ne $tmkey[$i]) {
		    $compstr .= "FOREIGN KEY oracle i2b2: $ibkey[$i]\n";
		    $compstr .= "       oracle transmart: $tmkey[$i]\n";
		}
	    }
	}
	else {
	    $compstr .= "FOREIGN KEY count $ibkey    $tmkey\n";
	    for ($i = 0; $i < $ibkey; $i++) {
		$compstr .= "       oracle i2b2: $ibkey[$i]\n";
	    }
	    for ($i = 0; $i < $tmkey; $i++) {
		$compstr .= "  oracle transmart: $tmkey[$i]\n";
	    }
	}
    }
    elsif($#ibkey >= 0) {
	$compstr .= "FOREIGN KEY oracle i2b2 only $ibkey\n";
	    for ($i = 0; $i < $ibkey; $i++) {
		$ibk = lc($ibkey[$i]);
		$compstr .= "       oracle i2b2: $ibk\n";
	    }
    }
    elsif($#tmkey >= 0) {
	$compstr .= "FOREIGN KEY oracle transmart only $tmkey\n";
	for ($i = 0; $i < $tmkey; $i++) {
	    $compstr .= "      oracle transmart: $tmkey[$i]\n";
	}
	($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
	for ($i = 0; $i < $tmkey; $i++) {
	    ($kn,$ki,$ks,$kt,$kr) = ($tmkey[$i] =~ /(\S+) \(([^\)]+)\) ([^.]+)[.]([^\(]+)\(([^\)]+)\)/);
	    $compstr .= "--
-- Type: REF_CONSTRAINT; Owner: $ts; Name: $kn
--
ALTER TABLE \"$ts\".\"$tt\" ADD CONSTRAINT \"$kn\" FOREIGN KEY (\"$ki\")
 REFERENCES \"$ks\".\"$kt\" (\"$kr\") ENABLE;
";
	}
    }

    # Compare index definitions

    $ibkey = 0;
    $tmkey = 0;
    @ibkey = ();
    @tmkey = ();

    if(defined($ioIndex{$t})){@ibkey = sort(split(/;/,$ioIndex{$t}));$ibkey=$#ibkey+1}
    if(defined($oIndex{$t})){@tmkey = sort(split(/;/,$oIndex{$t}));$tmkey=$#tmkey+1}
    if($ibkey && $tmkey) {
	if($ioIndex{$t} ne $oIndex{$t}) {print STDERR "Compare i2b2-TM $t counts $ibkey $tmkey INDEX i2b2: $ioIndex{$t} TM: $oIndex{$t}\n"}
	my $jib = 0;
	my $jtm = 0;
	my $cmp;
	while($jib < $ibkey && $jtm < $tmkey) {
	    $cmp = ($ibkey[$jib] cmp $tmkey[$jtm]);
	    if($cmp == 0) {	# two indexes match
#		$compstr .= "indexboth   i2b2 oracle: $ibkey[$jib]\n";
#		$compstr .= "indexboth  tmart oracle: $tmkey[$jtm]\n";
		++$jib;
		++$jtm;
	    } elsif ($cmp < 0) { # ibkey extra
		$compstr .= "INDEXONLY   i2b2 oracle: $ibkey[$jib]\n";
		++$jib;
	    } else {	     # tmkey extra
		$compstr .= "INDEXONLY  tmart oracle: $tmkey[$jtm]\n";
		++$jtm;
	    }
	}
	while($jib < $ibkey) {
	    $compstr .= "INDEXONLY   i2b2 oracle: $ibkey[$jib]\n";
	    ++$jib;
	}
	while($jtm < $tmkey) {
	    $compstr .= "INDEXONLY  tmart oracle: $tmkey[$jtm]\n";
	    ++$jtm;
	}
    } elsif($ibkey) {
	print STDERR "Only i2b2 $t INDEX i2b2: $ioIndex{$t}\n";
	for ($i = 0; $i < $ibkey; $i++) {
	    $compstr .= "INDEXONLY   i2b2 oracle: $ibkey[$i] for '$t'\n";
	}
    } elsif($tmkey) {
	print STDERR "Only tmart $t INDEX TM: $oIndex{$t}\n";
	for ($i = 0; $i < $tmkey; $i++) {
	    $compstr .= "INDEXONLY  tmart oracle: $tmkey[$i] for '$t'\n";
	}
    }

    if($head eq "") {$compstr .= "\n"}
    return $compstr;
}

sub compareSequence($$){
    my ($otxt,$ptxt) = @_;
    my $compstr = "";

    my $ov;
    my $pv;

    if(!defined($otxt)){$compstr .= "No oracle sequence text for '$t'\n"}
    if(!defined($ptxt)){$compstr .= "No postgres sequence text for '$t'\n"}

    if($compstr ne "") {return $compstr}

    $otxt =~ s/\s+/ /g;
    $ptxt =~ s/\s+/ /g;

    $otxt =~ s/^\s+//g;
    $ptxt =~ s/^\s+//g;

    $otxt =~ s/\s+$//g;
    $ptxt =~ s/\s+$//g;

    $ptxt =~ s/NO MINVALUE/MINVALUE 1/g;
    $otxt =~ s/MAXVALUE 999999[9]+/MAXVALUE 999999999999999999999999999/g;
    $ptxt =~ s/NO MAXVALUE/MAXVALUE 999999999999999999999999999/g;

    $otxt =~ s/NOCACHE/CACHE 1/;
    $ptxt =~ s/NOCACHE/CACHE 1/;

    $otxt =~ s/NOORDER//;
    $otxt =~ s/NOCYCLE//;

    if($otxt eq $ptxt) {return ""}

    ($ov) = ($otxt =~ /START WITH (\S+)/);
    ($pv) = ($ptxt =~ /START WITH (\S+)/);
    if(!defined($ov) || !defined($pv) || ($ov ne $pv)){
	if(!defined($ov)){$ov = "undefined"}
	if(!defined($pv)){$pv = "undefined"}
	$compstr .= "START WITH $ov $pv\n"
    }

    ($ov) = ($otxt =~ /INCREMENT BY (\S+)/);
    ($pv) = ($ptxt =~ /INCREMENT BY (\S+)/);
    if(!defined($ov) || !defined($pv) || ($ov ne $pv)){
	if(!defined($ov)){$ov = "undefined"}
	if(!defined($pv)){$pv = "undefined"}
	$compstr .= "INCREMENT BY $ov $pv\n"
    }

    ($ov) = ($otxt =~ /MINVALUE (\S+)/);
    ($pv) = ($ptxt =~ /MINVALUE (\S+)/);
    if(!defined($ov) || !defined($pv) || ($ov ne $pv)){
	if(!defined($ov)){$ov = "undefined"}
	if(!defined($pv)){$pv = "undefined"}
	$compstr .= "MINVALUE $ov $pv\n";
    }

    ($ov) = ($otxt =~ /MAXVALUE (\S+)/);
    ($pv) = ($ptxt =~ /MAXVALUE (\S+)/);
    if(!defined($ov) || !defined($pv) || ($ov ne $pv)){
	if(!defined($ov)){$ov = "undefined"}
	if(!defined($pv)){$pv = "undefined"}
	$compstr .= "MAXVALUE $ov $pv\n";
    }

    ($ov) = ($otxt =~ /CACHE (\S+)/);
    ($pv) = ($ptxt =~ /CACHE (\S+)/);
    if(!defined($ov) || !defined($pv) || ($ov ne $pv)){
	if(!defined($ov)){$ov = "undefined"}
	if(!defined($pv)){$pv = "undefined"}
	# check for the default values specified
	if($ov ne "20" || $pv ne "1") {$compstr .= "CACHE $ov $pv\n"}
    }

    return $compstr;
}

sub parseI2b2OracleTop($$){
    my ($d,$f) = @_;
    local *IN;
    my $err = 0;
    my @f;

    if($f eq "README.md") {
    }
    elsif($f =~ /[.]jar$/) {
    }
    else {
#	print "I2b2 parse $d/$f\n";
	return 1;
    }
    i2b2OracleUnparsed("$d/$f",$f);
    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
    while(<IN>) {
    }
    close IN;

    return $err;
}

sub parseI2b2OracleFunctions($);
sub parseI2b2OracleFunctions($){
    my ($d) = @_;
    local *IODIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my $subd = $d;

    $subd =~ s/^$iplus\///g;

    opendir(IODIR,"$d") || die "parseI2b2OracleFunctions failed to open $d";

    while($f = readdir(IODIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if($f =~ /_bk[.]sql$/) {next}
	if($f =~ /_gsk[.]sql$/) {next}

	if(-d "$dir$d/$f") {
	    if($f eq "scripts" || $f eq "procedures" || $f eq "oracle") {
		parseI2b2OracleFunctions("$d/$f");
		next;
	    } elsif ($f eq "postgresql" || $f eq "sqlserver") {
		next;
	    }
	    print STDERR "parseI2b2OracleFunctions $subd/$d\n";
	    next;
	}

	if($f =~ /[.]sql$/ && "$subd/$f" !~ /postgres.*[.]sql$/ && "$subd/$f" !~ /sqlserver.*[.]sql$/) {
#	    print "I2b2Oracle parse $d/$f\n";
	    $iosql{"$subd/$f"}++;

	    i2b2OracleParsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		if(/^\s*(.*\S)\s+(FUNCTION|function)\s+(\S+)/) {
		    $fuse = $1;
		    $schema = $ischema;
		    $func = $3;
		    $fuse = uc($fuse);
		    $schema = uc($schema);
		    $func = uc($func);
		    $schema =~ s/\"//g;
		    $func =~ s/\"//g;
		    if($fuse =~ /^CREATE/) {
			$ioFunctionFile{"$schema.$func"} = "$d/$f";
			$cfunc = 1;
		    }
		    else {
			print STDERR "$d/$f unexpected function $func     $fuse\n";
		    }
		    if($cfunc && /RETURN (\S+) AS/) {
			$ioFunctionReturn{"$schema.$func"} = $1;
		    }
		    if($cfunc && /^\s*[\)]/) {$cfunc = 0}
		}
	    }
	    close IN;
	}
    }
    closedir(IODIR);
    return $err;
}

sub parseI2b2OracleProcedures($);
sub parseI2b2OracleProcedures($){
    my ($d) = @_;
    local *IODIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my $subd = $d;

    $subd =~ s/^$iplus\///g;

    opendir(IODIR,"$d") || die "parseI2b2OracleProcedures failed to open $d";

    while($f = readdir(IODIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if($f =~ /_bk[.]sql$/) {next}
	if($f =~ /_gsk[.]sql$/) {next}

	if(-d "$dir$d/$f") {
	    if($f eq "scripts" || $f eq "procedures" || $f eq "oracle") {
		parseI2b2OracleProcedures("$d/$f");
		next;
	    } elsif ($f eq "postgresql" || $f eq "sqlserver") {
		next;
	    }
	    print STDERR "parseI2b2OracleProcedures $subd/$d\n";
	    next;
	}

	if($f =~ /[.]sql$/ && "$subd/$f" !~ /postgres.*[.]sql$/ && "$subd/$f" !~ /sqlserver.*[.]sql$/) {
#	    print "I2b2Oracle parse $d/$f\n";
	    $iosql{"$subd/$f"}++;

	    i2b2OracleParsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		if(/^\s*(.*\S)\s+(PROCEDURE|procedure)\s+(\S[^\(]+)/) {
		    $puse = $1;
		    $schema = $ischema;
		    $proc = $3;
		    $puse = uc($puse);
		    $schema = uc($schema);
		    $proc = uc($proc);
		    $schema =~ s/\"//g;
		    $proc =~ s/\s+$//g;
		    $proc =~ s/\"//g;
		    if($puse =~ /^CREATE/) {
			$ioProcFile{"$schema.$proc"} = "$d/$f";
			$cproc = 1;
		    }
		    else {
			print STDERR "$d/$f unexpected procedure $proc     $puse\n";
		    }
		}
	    }
	    close IN;
	}
    }
    closedir(IODIR);
    return $err;
}

sub parseI2b2OracleViews($){
    my ($d) = @_;
    local *IODIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my $subd = $d;

    $subd =~ s/^$iplus\///g;

    opendir(IODIR,"$d") || die "parseI2b2OracleViews failed to open $d";

    while($f = readdir(IODIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if($f =~ /_bk[.]sql$/) {next}
	if($f =~ /_gsk[.]sql$/) {next}
	if(-d "$dir$d/$f") {
#	    print "I2b2OracleViews subdir $d/$f\n";
	    next;
	}
	if($f =~ /[.]sql$/ && "$subd/$f" !~ /postgres.*[.]sql$/ && "$subd/$f" !~ /sqlserver.*[.]sql$/) {
#	    print "I2b2Oracle parse $d/$f\n";
	    $iosql{"$subd/$f"}++;

	    i2b2OracleParsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		if(/^\s*(.*\S)\s+(VIEW|view)\s+(\S+)\s+(.*)/) {
		    $vuse = $1;
		    $schema = $ischema;
		    $view = $3;
		    $rest = $4;
		    $vuse = uc($vuse);
		    $schema = uc($schema);
		    $view = uc($view);
		    $rest = uc($rest);
		    $schema =~ s/\"//g;
		    $view =~ s/\"//g;
		    $rest =~ s/\"//g;
		    $rest =~ s/^\(//g;
		    $rest =~ s/\)$//g;
		    if($vuse =~ /^CREATE/) {
			$ioViewFile{"$schema.$view"} = "$d/$f";
			$cview = 1;
		    }
		    elsif($vuse =~ /^COMMENT/) {#COMMENT ON VIEW - ignore
		    }
		    else {
#			print STDERR "$d/$f unexpected view $view     $vuse     '$rest'\n";
		    }
		}
	    }
	    close IN;
	}
    }
    return $err;
}

sub parseI2b2OracleScripts($){
    my ($d) = @_;
    local *IODIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;

    opendir(IODIR,"$d") || die "parseI2b2OracleScripts failed to open $d";

    while($f = readdir(IODIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    if($f eq "inc") {
	    }
	    else {
		print "I2b2OracleScripts subdir $d/$f\n";
	    }
	    next;
	}
	if($f =~ /[.]php$/) {
	}
	elsif($f =~ /[.]groovy$/){
	}
	else {
	    print "I2b2OracleScripts parse $d/$f\n";
	}
	i2b2OracleUnparsed("$d/$f",$f);
	open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	while(<IN>) {
	}
	close IN;
    }
    closedir(IODIR);
    return $err;
}

sub parseI2b2Oracle($);
sub parseI2b2Oracle($){
    my ($d) = @_;
    local *IODIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my ($tuse,$schema,$table);
    my $subd = $d;
    my $target = "unknown";
    my $line = 0;
    my $cindx = 0;
    my $tindx = "";

    $subd =~ s/^$iplus\///g;

    opendir(IODIR,"$d") || die "parseI2b2Oracle failed to open $d";

    while($f = readdir(IODIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if($f =~ /^[#]/) {next}
	if($f =~ /_bk[.]sql$/) {next}
	if($f =~ /_gsk[.]sql$/) {next}
	if(-d "$dir$d/$f") {
	    if($f eq "functions") {
		parseI2b2OracleFunctions("$d/$f");
	    }
	    elsif($f eq "procedures"){
		parseI2b2OracleProcedures("$d/$f");
	    }
	    elsif($f eq "oracle"){
		parseI2b2Oracle("$d/$f");
	    }
	    elsif($f eq "scripts"){
		parseI2b2Oracle("$d/$f");
	    }
	    elsif($f eq "demo"){
		parseI2b2Oracle("$d/$f");
	    }
	    else {
#		print "I2b2Oracle subdir $d/$f\n";
	    }
	    next;
	}
	if($f =~ /[.]sql$/ && "$subd/$f" !~ /postgresql.*[.]sql$/ && "$subd/$f" !~ /sqlserver.*[.]sql$/) {
#           print "I2b2Oracle parse $d/$f\n";
	    if($f ne "_cross.sql" && $subd ne "GLOBAL"){
		$iosql{"$subd/$f"}++;
	    }
	    if($f =~ /insert_data[.]sql$/) {
		next;
	    }

	    i2b2OracleParsed("$d/$f",$f);

	    $ctable = 0;
	    $ctrig  = 0;
	    $cfunc  = 0;
	    $cproc  = 0;
	    $cview  = 0;
	    $cseq = 0;
	    $tseq = "";
	    $forkey = 0;

	    $line=0;
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		++$line;
		s/\s*--.*//g;
		if($ctrig) {
		    if(/select ([^.]+)[.]nextval into :NEW[.]\"([^\"]+)\" from dual;/){
			$nid = $1;
			$ncol=$2;
			$ioNextval{"$schema.$table"} = "$ncol.$nid";
			$ioNexttrig{"$schema.$trig"} = "$schema.$table";
		    }
		    if(/ALTER TRIGGER \S+ ENABLE/) {$ctrig = 0}
		}
		if($forkey) {
		    if(/^\s+REFERENCES ([^\( ]+) *\(([^\) ]+)\s*\)/) {
			$pk = " ";
			$pk .= $ischema;
			$pk .= ".";
			$pk .= uc($1);
			$pk .= "(";
			$pk .= uc($2);
			$pk .= ");";
			$ioTableForkey{"$schema.$table"} .= $pk;
#			print STDERR "Parsed Oracle forkeyref '$pk' from $_\n";
		    }
		    else {
			print STDERR "$d/$f $line I2b2 Unexpected foreign key format $d/$f: $_";
		    }
		    $forkey = 0;
		}
		if(/(\S+)\s+GLOBAL\s+TEMPORARY\s+(TABLE|table)\s+(\S+)/) {
		    $tuse = $1;
		    $schema = $ischema;
		    $table = $3;
		    $tuse = uc($tuse);
		    $schema = uc($schema);
		    $table = uc($table);
		    $schema =~ s/\"//g;
		    $table =~ s/\"//g;
		    if($tuse eq "CREATE") {
			$ioTableFile{"$schema.$table"} = "$d/$f";
			$ctable = 1;
		    }
		    elsif($tuse eq "ALTER") {
			if(/CONSTRAINT (\S+ )FOREIGN KEY *(\([^\)]+\))/){
			    $pc = $1;
			    $pk = $2;
			    $pc =~ s/\"//g;
			    $pk =~ s/\s$//g;
			    if(length($pc) > 31){print STDERR "I2b2Oracle constraint length ".length($pc)." '$pc'\n"}
			    $pfk = uc($pc).uc($pk);
			    $pfk =~ s/\"//g;
			    $ioTableForkey{"$schema.$table"} .= $pfk;
			    $forkey=1;
			}
		    }

		}
		elsif(/(\S+)\s+(TABLE|table)\s+(\S+)/) {
		    $tuse = $1;
		    $schema = $ischema;
		    $table = $3;
		    $tuse = uc($tuse);
		    $schema = uc($schema);
		    $table = uc($table);
		    $schema =~ s/\"//g;
		    $table =~ s/\"//g;
		    if($tuse eq "CREATE") {
			$ioTableFile{"$schema.$table"} = "$d/$f";
			$ctable = 1;
		    }
		    elsif($tuse eq "ALTER") {
			if(/CONSTRAINT (\S+ )FOREIGN KEY *(\([^\)]+\))/){
			    $pc = $1;
			    $pk = $2;
			    $pc =~ s/\"//g;
			    if(length($pc) > 31){print STDERR "I2b2Oracle constraint length ".length($pc)." '$pc'\n"}
			    $pfk = uc($pc).uc($pk);
			    $pfk =~ s/\"//g;
			    $ioTableForkey{"$schema.$table"} .= $pfk;
			    $forkey=1;
			}
		    }

		}
		elsif($ctable) {
		    if(/;/){$ctable=0; next}
		    if(/^\s*\(/){s/^\s*\(\s*//}
		    if(/^\s*\)/){$ctable=2; s/^\s*\)\s*//}
		    s/\s+/ /g;
		    s/ $//g;
		    s/ ,$/,/g;
		    if(/^\s*(CONSTRAINT (\S+)\s+)?PRIMARY KEY *\(([^\)]+)\)/){
			$pkc = $2;
			$pk = uc($3);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $ioTablePrikeyName{"$schema.$table"} = $pkc;
			}
			$ioTablePrikey{"$schema.$table"} = $pk;
		    }
		    elsif(/^\s*(CONSTRAINT (\S+)\s+)?UNIQUE *\(([^\)]+)\)/){
			$pkc = $2;
			$pk = uc($3);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $ioTableUnikey{"$schema.$table"} .= "$pkc $pk;";
			}
			else {$ioTableUnikey{"$schema.$table"} .= ". $pk;"}
		    }
		    elsif(/^\s*(CONSTRAINT (\S+)\s*)?FOREIGN KEY *(\([^\)]+\)) REFERENCES ([^\( ]+) *\(([^\) ]+)\s*\)/){
			if(defined($1)) {$pk = uc($2).uc($3)." $schema.$4($5);"}
			else{$pk = "unnamed ".uc($3)}
			$pk =~ s/\"//g;
			$ioTableForkey{"$schema.$table"} .= $pk;
		    }
		    elsif(/^\s*(CONSTRAINT (\S+)\s*)?FOREIGN KEY *(\([^\)]+\))/){
			if(defined($1)) {$pk = uc($2).uc($3)}
			else{$pk = "unnamed ".uc($3)}
			$pk =~ s/\"//g;
			$ioTableForkey{"$schema.$table"} .= $pk;
			$forkey=1;
		    }
		    elsif(/^\s*ON COMMIT /i){
			print STDERR "Commit skip $d/$f $_\n";
			# skip commit options for temporary tables
		    }
		    elsif(/^\s*REFERENCES ([^\( ]+) *\(([^\)]+)\)/){
			# skip tail of a foreign key definition
		    }
		    elsif(/^\s*(\S+)\s+(.*?),?$/) {
			$col = $1;
			$cdef = $2;
			$col =~ s/\"//g;
			$cdef =~ s/,\s*$//g;
			if($cdef =~ / PRIMARY KEY/g) {
			    if($cdef !~ / NOT NULL ENABLE/){
				$cdef =~ s/ PRIMARY KEY/ NOT NULL ENABLE/g;
			    } else {
				$cdef =~ s/ PRIMARY KEY//g;
			    }
			    $ioTablePrikeyName{"$schema.$table"} = $col;
			    $ioTablePrikey{"$schema.$table"} = $col;
			}
			$ioTableColumn{"$schema.$table"} .= "$col $cdef;";
		    }
		}
		else {		# ALTER TABLE
		    if(/^\s*ADD\s*\(\s*PRIMARY KEY\s*\(([^\)]+)\)/){
			$pkc = $1;
			$pk = uc($1);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $ioTablePrikeyName{"$schema.$table"} = $pkc;
			}
			$ioTablePrikey{"$schema.$table"} = $pk;
		    }		    
		}

		if($cseq == 1 && /([^;]*)(;?)/) {
		    $tseq .= $1;
		    if(defined($2)) {$cseq = 2}
		}
		if($cindx) {
		    $tindx .= $_;
		    if(/;/) {
			$tindx =~ s/\s+/ /gosm;
			if($tindx =~ /^\s*(\S+)\s+(INDEX|index)\s+(\S+)\s+ON\s+(\S[^\( ]+)\s*\(([^\)]+)\)/) {
			    $iuse = $1;
			    $idx = $3;
			    $itable=$4;
			    $icols=$5;
			    $schema = $ischema;
			    $schema =~ s/"//g;
			    $idx =~ s/"//g;
			    $idx = uc($idx);
			    $ischema =~ s/"//g;
			    $itable =~ s/"//g;
			    $itable = uc($itable);
			    $icols =~ s/"//g;
			    $icols =~ s/\s//g;
			    $icols = uc($icols);
			    if($iuse =~ /^CREATE/) {
				$ioIndexFile{"$schema.$idx"} = "$d/$f";
				$ioIndex{"$ischema.$itable"} .= "$schema.$idx($icols);";
			    } else {
				print STDERR "Unexpected i2b2 oracle $iuse INDEX $d/$f\n$tindx\n";
			    }
			} else {
			    print STDERR "Unexpected i2b2 oracle INDEX $d/$f\n$tindx\n";
			}
			$cindx = 0;
			$tindx = "";
		    }
		}
		if(/^\s*(\S+)\s+(INDEX|index)\s+(\S+)\s+ON\s+(\S[^\( ]+)\s*(\(([^\)]+)\))?/) {
		    $iuse = $1;
		    $idx = $3;
		    $itable=$4;
		    $icols=$6;
		    if(/;/){
			$schema = $ischema;
			$schema =~ s/"//g;
			$idx =~ s/"//g;
			$idx = uc($idx);
			$ischema =~ s/"//g;
			$itable =~ s/"//g;
			$itable = uc($itable);
			$icols =~ s/"//g;
			$icols =~ s/\s//g;
			$icols = uc($icols);
			if($iuse =~ /^CREATE/) {
			    $ioIndexFile{"$schema.$idx"} = "$d/$f";
			    $ioIndex{"$ischema.$itable"} .= "$schema.$idx($icols);";
			} else {
			    print STDERR "Unexpected i2b2 oracle oneline $iuse INDEX $d/$f\n$_";
			}
		    } else {
			$cindx = 1;
			$tindx = $_;
		    }
		}
		if(/^\s*(.*\S)\s+(SEQUENCE|sequence)\s+(\S+)([^;]*)([;]?)/) {
		    $suse = $1;
		    $schema = $ischema;
		    $seq = $3;
		    $rest = $4;
		    $cdone = $5;
		    $suse = uc($suse);
		    $schema = uc($schema);
		    $seq = uc($seq);
		    $schema =~ s/\"//g;
		    $seq =~ s/\"//g;
#		    print "$d/$f sequence $seq     $suse\n";
		    if($suse =~ /^CREATE/) {
			$ioSequenceFile{"$schema.$seq"} = "$d/$f";
			$cseq = 1;
			if(defined($cdone)){$cseq = 2}
			$tseq = $rest;
		    }
		}
		if($cseq == 2){
		    $cseq = 0;
		    $ioSequenceText{"$schema.$seq"} = $tseq;
		    $tseq = "";
		}

		if(/^\s*(.*\S)\s+(TRIGGER|trigger)\s+(\S+)/) {
		    $tuse = $1;
		    $schema = $ischema;
		    $trig = $3;
		    $tuse = uc($tuse);
		    $schema = uc($schema);
		    $trig = uc($trig);
		    $schema =~ s/\"//g;
		    $trig =~ s/\"//g;
#		    print "$d/$f trigger $trig     $tuse\n";
		    if($tuse =~ /^CREATE/) {
			$ioTriggerFile{"$schema.$trig"} = "$d/$f";
			$ctrig = 1;
#			if($trig !~ /^TRG_/){
#			    print STDERR "ctrig set for $schema.$trig\n";
#			}
		    }
		}

		if(/^\s*(.*\S)\s+(VIEW|view)\s+(\S+)\s+(.*)/) {
		    $vuse = $1;
		    $schema = $ischema;
		    $view = $3;
		    $rest = $4;
		    $vuse = uc($vuse);
		    $schema = uc($schema);
		    $view = uc($view);
		    $rest = uc($rest);
		    $schema =~ s/\"//g;
		    $view =~ s/\"//g;
		    $rest =~ s/\"//g;
		    $rest =~ s/^\(//g;
		    $rest =~ s/\)$//g;
		    if($vuse =~ /^CREATE/) {
			$ioViewFile{"$schema.$view"} = "$d/$f";
			$cview = 1;
		    }
		    elsif($vuse =~ /^COMMENT/) {#COMMENT ON VIEW - ignore
		    }
		    else {
#			print STDERR "$d/$f unexpected view $view     $vuse     '$rest'\n";
		    }
		}
	    }
	    close IN;
	}
	elsif($f eq "db.properties"){
	    parseI2b2Properties($d,$f);
	}
	elsif($f eq "data_build.xml"){
#	    print "I2b2Oracle parse data_build.xml $d/$f\n";
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		if(/<target name=\"([^\"]+)\"/) {
		    $target = $1;
		}
		if(/<transaction src=\"[.]\/([^\"]+)\"/) {
		    $sql = $1;
		    $sql =~ s/\$\{db[.]type\}/oracle/;
		    $sql =~ s/\$\{db[.]project\}/demo/;
#		    print "parseI2b2OracleDataBuild $d/$sql\n";
		    $ioload{"$subd/$sql"}++;
		    $iotarget{"$subd/$sql"} = $target;
		}
	    }
	    close IN;
	}
	else {
#	    print "I2b2Oracle file $d/$f\n";
	}
    }
    closedir(IODIR);
    return $err;
}

sub parseI2b2PostgresTop($$){
    my ($d,$f) = @_;
    local *IN;
    my $err = 0;
    my @f;

    if($f eq "README.md") {
    }
    elsif($f =~ /[.]jar$/) {
    }
    else {
#	print "I2b2 parse $d/$f\n";
	return 1;
    }
    i2b2PostgresUnparsed("$d/$f",$f);
    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
    while(<IN>) {
    }
    close IN;

    return $err;
}

sub parseI2b2PostgresFunctions($);

sub parseI2b2PostgresFunctions($){
    my ($d) = @_;
    local *IPDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my $subd = $d;

    $subd =~ s/^$iplus\///g;

    opendir(IPDIR,"$d") || die "parseI2b2PostgresFunctions failed to open $d";

    while($f = readdir(IPDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    if($f eq "scripts" || $f eq "procedures" || $f eq "postgresql") {
		parseI2b2PostgresFunctions("$d/$f");
		next;
	    } elsif ($f eq "oracle" || $f eq "sqlserver") {
		next;
	    }

	    print STDERR "I2b2PostgresFunctions subdirectory $subd/$f\n";
	    next;
	}
	if($f =~ /[.]sql$/ && "$subd/$f" !~ /oracle.*[.]sql$/ && "$subd/$f" !~ /sqlserver.*[.]sql$/) {
#	    print "I2b2Postgres parse $d/$f\n";
	    $ipsql{"$subd/$f"}++;
	    $noret=0;

	    i2b2PostgresParsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		s/\(-1\)/-1/g;
		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s*\(([^\)]*)\)\s*RETURNS (.*)/) {
		    $func = $1;
		    $ret = $2;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)\/functions$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    if($ret ne "trigger"){
			$ipFunctionFile{"$schema.$func"} = "$d/$f";
			$ipFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
		elsif(/^\s*CREATE\s+FUNCTION\s+(\S+)\s+RETURNS (.*)/) {
		    $func = $1;
		    $ret = $2;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)\/functions$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    if($ret ne "trigger"){
			$ipFunctionFile{"$schema.$func"} = "$d/$f";
			$ipFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
		elsif(/^\s*CREATE\s+(OR\s+REPLACE\s+)?FUNCTION\s+(\S+)\s+\($/) {
		    $func = $2;
		    $noret = 1;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)\/functions$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    $ipFunctionFile{"$schema.$func"} = "$d/$f";
		}
		elsif($noret && /^\s*RETURNS (\S+) AS/) {
		    $ret = $1;
		    if($ret ne "trigger"){
			$ipFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
	    }
	    close IN;
	}
    }
    closedir(IPDIR);
    return $err;
}

sub parseI2b2PostgresViews($){
    my ($d) = @_;
    local *IPDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my $subd = $d;

    $subd =~ s/^$iplus\///g;

    opendir(IPDIR,"$d") || die "parseI2b2PostgresViews failed to open $d";

    while($f = readdir(IPDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    print "I2b2PostgresViews subdir $d/$f\n";
	    next;
	}
	if($f =~ /[.]sql$/ && "$subd/$f" !~ /oracle.*[.]sql$/ && "$subd/$f" !~ /sqlserver.*[.]sql$/) {
#	    print "I2b2Postgres parse $d/$f\n";
	    $ipsql{"$subd/$f"}++;

	    i2b2PostgresParsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		if(/^\s*(.*\S)\s+(VIEW|view)\s+(\S+)/) {
		    $vuse = $1;
		    $view = $3;
		    ($schema) =  ($d =~ /\/([^\/]+)\/views$/);
		    $vuse = uc($vuse);
		    $schema = uc($schema);
		    $view = uc($view);
		    $schema =~ s/\"//g;
		    $view =~ s/\"//g;
		    if($vuse =~ /^CREATE/) {
			$ipViewFile{"$schema.$view"} = "$d/$f";
			$cview = 1;
		    }
		    elsif($vuse =~ /^COMMENT/) {#COMMENT ON VIEW - ignore
		    }
		    else {
#			print STDERR "$d/$f unexpected view $view     $vuse     '$rest'\n";
		    }
		}
	    }
	    close IN;
	}
    }
    closedir(IPDIR);
    return $err;
}

sub parseI2b2PostgresScripts($){
    my ($d) = @_;
    local *IPDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;

    opendir(IPDIR,"$d") || die "parseI2b2PostgresScripts failed to open $d";

    while($f = readdir(IPDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    print "I2b2PostgresScripts subdir $d/$f\n";
	    next;
	}
	if($f =~ /[.]php$/) {
	}
	else {
	    print "I2b2PostgresScripts parse $d/$f\n";
	    next;
	}
	i2b2PostgresUnparsed("$d/$f",$f);
	open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	while(<IN>) {
	}
	close IN;
    }
    closedir(IPDIR);
    return $err;
}

sub parseI2b2PostgresDataBuild($){
    my ($d) = @_;
    local *IN;
    my $target = "unknown";
    my $sql;
    my $subd = $d;

    $subd =~ s/^$iplus\///g;

    open(IN,"$d/data_build.xml") || die "failed to open '$d/data_build.xml";
    while(<IN>){
	if(/<target name=\"([^\"]+)\"/) {
	    $target = $1;
	}
	if(/<transaction src=\"[.]\/([^\"]+)\"/) {
	    $sql = $1;
	    $sql =~ s/\$\{db[.]type\}/postgresql/;
	    $sql =~ s/\$\{db[.]project\}/demo/;
#	    print "parseI2b2PostgresDataBuild $d/$sql\n";
	    $ipload{"$subd/$sql"}++;
	    $iptarget{"$subd/$sql"} = $target;
	}
    }
    close IN;
}

sub parseI2b2Postgres($);
sub parseI2b2Postgres($){
    my ($d) = @_;
    local *IPDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my ($tuse,$schema,$table);
    my $subd = $d;
    my $cindx = 0;
    my $tindx = "";

    $subd =~ s/^$iplus\///g;

    opendir(IPDIR,"$d") || die "parseI2b2Postgres failed to open $d";

    while($f = readdir(IPDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    if($f eq "procedures") {
		parseI2b2PostgresFunctions("$d/$f");
	    }
	    elsif($f eq "postgresql"){
		parseI2b2Postgres("$d/$f");
	    }
	    elsif($f eq "scripts"){
		parseI2b2Postgres("$d/$f");
	    }
	    elsif($f eq "demo"){
		parseI2b2Postgres("$d/$f");
	    }
	    else {
#		print "I2b2Postgres subdir $d/$f\n";
	    }
#	    print STDERR "parseI2b2Postgres $subd $d\n";
	    next;
	}

	if($f eq "db.properties"){
	    parseI2b2Properties($d,$f);
	    next;
	}

	if($f eq "data_build.xml"){
	    parseI2b2PostgresDataBuild("$d");
	    next;
	}

	if($f =~ /[.]sql$/ && "$subd/$f" !~ /oracle.*[.]sql$/ && "$subd/$f" !~ /sqlserver.*[.]sql$/) {
	    $ipsql{"$subd/$f"}++;

	    if($f =~ /insert_data[.]sql$/) {
		next;
	    }

	    i2b2PostgresParsed("$d/$f",$f);
	    $ctable = 0;
	    $ctrig  = 0;
	    $cfunc  = 0;
	    $cproc  = 0;
	    $cview  = 0;
	    $cseq = 0;
	    $alterctable = 0;
	    $altertable = "undefined";
	    $tseq = "";

	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		if($alterctable) {
		    if(/^\s*ADD CONSTRAINT (\S+) PRIMARY KEY *\(([^\)]+)\)/) {
			$pkc = uc($1);
			$pk = uc($2);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			$ipTablePrikey{$altertable} = $pk;
			if(defined($pkc)){$ipTablePrikeyName{"$schema.$table"} = $pkc}
		    }
		    if(/^\s*ADD CONSTRAINT (\S+) UNIQUE *\(([^\)]+)\)/) {
			$pkc = uc($1);
			$pk = uc($2);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){$ipTableUnikey{$altertable} .= "$pkc $pk;"}
			else {$ipTableUnikey{$altertable} .= ". $pk;"}
		    }
		    if(/^\s*ADD\s+CONSTRAINT\s+(\S+ )FOREIGN KEY *(\(\S+\) )REFERENCES *([^\(]+\([^\)]+\))/){
			$pk = uc($1).uc($2);
			$pk .= uc($schema);
			$pk .= ".";
			$pk .= uc($3);
			$pk .= ";";
			$ipTableForkey{"$schema.$table"} .= $pk;
		    }
		    if(/^\s*ADD\s+PRIMARY KEY\s*\(([^\)]+)\)/){
			$pkc = $1;
			$pk = uc($1);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $ipTablePrikeyName{"$schema.$table"} = $pkc;
			}
			$ipTablePrikey{"$schema.$table"} = $pk;
		    }		    
		    if(/;/) {$alterctable = 0}
		}
		if(/(\S+)\s+GLOBAL\s+TEMPORARY\s+(TABLE|table)\s+(ONLY\s+)?(\S+)/) {
		    $tuse = $1;
		    $table = $4;
		    if ($ischema ne "unknown") {
			($schema) = $ischema;
		    }
		    else {
			($schema) = ($d =~ /\/([^\/]+)$/);
		    }
		    $tuse = uc($tuse);
		    $schema = uc($schema);
		    $table = uc($table);
		    $schema =~ s/\"//g;
		    $table =~ s/\"//g;
		    $table =~ s/\($//g;
		    if($tuse eq "CREATE") {
			$ipTableFile{"$schema.$table"} = "$d/$f";
			$ctable = 1;
		    }
		    if($tuse eq "ALTER") {
			$altertable = "$schema.$table";
			$alterctable = 1;
		    }
		}
		elsif(/(\S+)\s+(TABLE|table)\s+(ONLY\s+)?(\S+)/) {
		    $tuse = $1;
		    $table = $4;
		    if ($ischema ne "unknown") {
			($schema) = $ischema;
		    }
		    else {
			($schema) = ($d =~ /\/([^\/]+)$/);
		    }
		    $tuse = uc($tuse);
		    $schema = uc($schema);
		    $table = uc($table);
		    $schema =~ s/\"//g;
		    $table =~ s/\"//g;
		    $table =~ s/\($//g;
		    if($tuse eq "CREATE") {
			$ipTableFile{"$schema.$table"} = "$d/$f";
			$ctable = 1;
		    }
		    if($tuse eq "ALTER") {
			$altertable = "$schema.$table";
			$alterctable = 1;
		    }
		}
		elsif($ctable) {
		    if(/;/){$ctable=0; next}
		    if(/^\s*\(/){s/^\s*\(\s*//}
		    if(/^\s*\"position\"\s+/){s/\"position\"/position/} # used in de_variant_subject_idx
		    if(/^\s*\)/){$ctable=2; s/^\s*\)\s*//}
		    s/\s+/ /g;
		    s/ $//g;
		    s/ ,$/,/g;
		    if(/^\s*(CONSTRAINT (\S+)\s+)?PRIMARY KEY *\(([^\)]+)\)/){
			$pkc = $2;
			$pk = uc($3);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			$ipTablePrikey{"$schema.$table"} = $pk;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $ipTablePrikeyName{"$schema.$table"} = $pkc;
			}
		    }
		    elsif(/^\s*(CONSTRAINT (\S+)\s+)?UNIQUE *\(([^\)]+)\)/){
			$pkc = $2;
			$pk = uc($3);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $ipTableUnikey{"$schema.$table"} .= "$pkc $pk;";
			}
			else {$ipTableUnikey{"$schema.$table"} .= ". $pk;"}
		    }
		    elsif(/^\s*(CONSTRAINT (\S+)\s+)?FOREIGN KEY *(\([^\)]+\))\s*REFERENCES ([^\( ]+) *(\([^\)]+\))/){
			if(defined($1)) {$pk = uc($2).uc($3)." $schema.$4$5"}
			else{$pk = "unnamed ".uc($3)}
			$pk =~ s/\"//g;
			$pk =~ s/ \)/\)/g;
			$ipTableForkey{"$schema.$table"} .= $pk;
#			print STDERR "I2b2 postgres forkeyall '$pk' from $_\n";
		    }
		    elsif(/^\s*(CONSTRAINT (\S+)\s+)?FOREIGN KEY *(\([^\)]+\))/){
			if(defined($1)) {$pk = uc($2).uc($3)}
			else{$pk = "unnamed ".uc($3)}
			$pk =~ s/\"//g;
			$pk =~ s/ \)/\)/g;
			$ipTableForkey{"$schema.$table"} .= $pk;
#			print STDERR "I2b2 postgres forkey '$pk' from $_\n";
		    }
		    elsif(/^\s*REFERENCES ([^\( ]+) *\(([^\)]+)\)/){
			$pk = " $schema.$1($2);";
			$ipTableForkey{"$schema.$table"} .= $pk;
#			print STDERR "I2b2 postgres forkeyref '$pk' from $_\n";
		    }
		    elsif(/^\s*ON COMMIT PRESERVE ROWS/ig){
		    }
		    elsif(/^\s*([A-Za-z]\S+)\s+(.*?),?$/) {
			$col = $1;
			$cdef = $2;
			$col = uc($col);
			$cdef = uc($cdef);
			$cdef =~ s/,\s+$//g;
			if($cdef =~ / PRIMARY KEY/g) {
			    if($cdef !~ / NOT NULL/){
				$cdef =~ s/ PRIMARY KEY/ NOT NULL/g;
			    } else {
				$cdef =~ s/ PRIMARY KEY//g;
			    }
			    $cdef =~ s/ PRIMARY KEY//g;
			    $ipTablePrikeyName{"$schema.$table"} = $col;
			    $ipTablePrikey{"$schema.$table"} = $col;
			}
			$ipTableColumn{"$schema.$table"} .= "$col $cdef;";
			if($cdef =~ / DEFAULT nextval\(\'([^\']+)\'::regclass\) NOT NULL$/){
			    $cid = $1;
			    $cid = uc($1);
			    $ipNextval{"$schema.$table"} = "$col.$cid";
			}
			elsif($cdef =~ /DEFAULT nextval/){print STDERR "$d/$f DEFAULT nextval not recognized: '$cdef'\n"}
		    }
		}

		if($cseq == 1 && /([^;]*)/) {
		    $tseq .= $1;
		    if(/;/) {$cseq = 2}
		}
		if($cindx) {
		    $tindx .= $_;
		    if(/;/) {
			if($tindx =~ /^\s*(.*\S)\s+(INDEX|index)\s+(\S+)\s+ON\s+(\S+)\s*\(([^\)]+)\)\s*/) {
			    $iuse = $1;
			    $idx = $3;
			    $itable=$4;
			    $itable = uc($itable);
			    $icols=$5;
			    $schema = $ischema;
			    $schema = uc($schema);
			    $idx =~ s/"//g;
			    $idx = uc($idx);
			    $itable =~ s/"//g;
			    $icols =~ s/"//g;
			    $icols =~ s/\s//g;
			    $icols = uc($icols);
			    if($iuse =~ /^CREATE/) {
				$ipIndexFile{"$schema.$idx"} = "$d/$f";
				$ipIndex{"$schema.$itable"} .= "$schema.$idx($icols);";
				#			print STDERR "Found i2b2 postgres index $schema.$itable.$idx\n";
			    } else {
				print STDERR "Unexpected i2b2 postgres INDEX $d/$f\n$tindx\n";
			   }
			}
			$cindx = 0;
			$tindx = "";
		    }
		}
		if(/^\s*(.*\S)\s+UNIQUE\s+(INDEX|index)\s+(\S+)\s+ON\s+(\S[^\( ]+)\s*\(([^\)]+)\)\s*(;?)/) {
		    $iuse = $1;
		    $idx = $3;
		    $itable=$4;
		    $itable = uc($itable);
		    $icols=$5;
		    if(defined($6)){
			$schema = $ischema;
			$schema = uc($schema);
			$idx =~ s/"//g;
			$idx = uc($idx);
			$itable =~ s/"//g;
			$icols =~ s/"//g;
			$icols =~ s/\s//g;
			$icols = uc($icols);
			if($iuse =~ /^CREATE/) {
			    $ipTableUnikey{"$schema.$itable"} .= "$idx $icols;";
			}
		    } else {
			$cindx = 1;
			$tindx = $_;
		    }
		}
		elsif(/^\s*(.*\S)\s+(INDEX|index)\s+(\S+)\s+ON\s+(\S[^\( ]+)\s*\(([^\)]+)\)\s*(;?)/) {
		    $iuse = $1;
		    $idx = $3;
		    $itable=$4;
		    $itable = uc($itable);
		    $icols=$5;
		    if(defined($6)){
			$schema = $ischema;
			$schema = uc($schema);
			$idx =~ s/"//g;
			$idx = uc($idx);
			$itable =~ s/"//g;
			$icols =~ s/"//g;
			$icols =~ s/\s//g;
			$icols = uc($icols);
			if($iuse =~ /^CREATE/) {
			    $ipIndexFile{"$schema.$idx"} = "$d/$f";
			    $ipIndex{"$schema.$itable"} .= "$schema.$idx($icols);";
			    #			print STDERR "Found i2b2 postgres index $schema.$itable.$idx\n";
			}
		    } else {
			$cindx = 1;
			$tindx = $_;
		    }
		}
		if(/^\s*(.*\S)\s+(SEQUENCE|sequence)\s+(\S+)(.*)/) {
		    $suse = $1;
		    $seq = $3;
		    $rest = $4;
		    $suse = uc($suse);
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $schema = uc($schema);
		    $seq = uc($seq);
		    $schema =~ s/\"//g;
		    $seq =~ s/\"//g;
#		    print "$d/$f sequence $seq     $suse\n";
		    if($suse =~ /^CREATE/) {
			$ipSequenceFile{"$schema.$seq"} = "$d/$f";
			$cseq = 1;
			$tseq = $rest;
		    }
		}
		if($cseq == 2){
		    $cseq = 0;
		    $ipSequenceText{"$schema.$seq"} = $tseq;
		    $tseq = "";
		}

		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s+RETURNS trigger/) {
		    $trig = $1;
		    $trig =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $trig = uc($trig);
		    if($trig !~ /^TF_/ &&
			$trig !~ /_FUN$/) {print "trigger name '$trig' $f\n"}
		    $trig =~ s/^TF_//g;
		    $trig =~ s/_FUN$//g;
		    $schema = uc($schema);
#		    print "$d/$f trigger $trig     create\n";
		    $ipTriggerFile{"$schema.$trig"} = "$d/$f";
		    $ctrig = 1;
		}
		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s+RETURNS (.*)/) {
		    $func = $1;
		    $ret = $2;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    if($ret ne "trigger") {
			$ipFunctionFile{"$schema.$func"} = "$d/$f";
			$ipFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s*\(([^\)]*)\)\s*RETURNS (.*)/) {
		    $func = $1;
		    $ret = $2;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    if($ret ne "trigger") {
			$ipFunctionFile{"$schema.$func"} = "$d/$f";
			$ipFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
		if(/^\s*(.*\S)\s+(VIEW|view)\s+(\S+)/) {
		    $vuse = $1;
		    $view = $3;
		    ($schema) =  ($d =~ /\/([^\/]+)$/);
		    $vuse = uc($vuse);
		    $schema = uc($schema);
		    $view = uc($view);
		    $schema =~ s/\"//g;
		    $view =~ s/\"//g;
		    if($vuse =~ /^CREATE/) {
			$ipViewFile{"$schema.$view"} = "$d/$f";
			$cview = 1;
		    }
		    elsif($vuse =~ /^COMMENT/) {#COMMENT ON VIEW - ignore
		    }
		    else {
#			print STDERR "$d/$f unexpected view $view     $vuse     '$rest'\n";
		    }
		}
	    }
	    close IN;
	}
	else {
#	    print "Postgres file $d/$f\n";
	}
    }
    closedir(IPDIR);
    return $err;
}

### I2b2 Sqlserver parsers

sub parseI2b2SqlserverTop($$){
    my ($d,$f) = @_;
    local *IN;
    my $err = 0;
    my @f;

    if($f eq "README.md") {
    }
    elsif($f =~ /[.]jar$/) {
    }
    else {
#	print "I2b2 parse $d/$f\n";
	return 1;
    }
    i2b2SqlserverUnparsed("$d/$f",$f);
    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
    while(<IN>) {
    }
    close IN;

    return $err;
}


sub parseI2b2SqlserverFunctions($);

sub parseI2b2SqlserverFunctions($){
    my ($d) = @_;
    local *ISDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my $subd = $d;

    $subd =~ s/^$iplus\///g;

    opendir(ISDIR,"$d") || die "parseI2b2SqlserverFunctions failed to open $d";

    while($f = readdir(ISDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}

	if(-d "$dir$d/$f") {
	    if($f eq "sqlserver") {
		parseI2b2SqlserverFunctions("$d/$f");
	    } elsif ($f eq "oracle" || $f eq "postgresql") {
		next;
	    }
#	    print STDERR "parseI2b2SqlserverFunctions $subd $d\n";
	    next;
	}

	if($f =~ /[.]sql$/ && "$subd/$f" !~ /oracle.*[.]sql$/ && "$subd/$f" !~ /postgresql.*[.]sql$/) {
#	    print "I2b2Sqlserver parse $d/$f\n";
	    $issql{"$subd/$f"}++;
	    $noret=0;

	    i2b2SqlserverParsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		s/\(-1\)/-1/g;
		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s*\(([^\)]*)\)\s*RETURNS (.*)/) {
		    $func = $1;
		    $ret = $2;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)\/functions$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    if($ret ne "trigger"){
			$isFunctionFile{"$schema.$func"} = "$d/$f";
			$isFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
		elsif(/^\s*CREATE\s+FUNCTION\s+(\S+)\s+RETURNS (.*)/) {
		    $func = $1;
		    $ret = $2;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)\/functions$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    if($ret ne "trigger"){
			$isFunctionFile{"$schema.$func"} = "$d/$f";
			$isFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
		elsif(/^\s*CREATE\s+(OR\s+REPLACE\s+)?FUNCTION\s+(\S+)\s+\($/) {
		    $func = $2;
		    $noret = 1;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)\/functions$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    $isFunctionFile{"$schema.$func"} = "$d/$f";
		}
		elsif($noret && /^\s*RETURNS (\S+) AS/) {
		    $ret = $1;
		    if($ret ne "trigger"){
			$isFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
	    }
	    close IN;
	}
    }
    closedir(ISDIR);
    return $err;
}

sub parseI2b2SqlserverViews($){
    my ($d) = @_;
    local *ISDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my $subd = $d;

    $subd =~ s/^$iplus\///g;

    opendir(ISDIR,"$d") || die "parseI2b2SqlserverViews failed to open $d";

    while($f = readdir(ISDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    print "I2b2SqlserverViews subdir $d/$f\n";
	    next;
	}
	if($f =~ /[.]sql$/ && "$subd/$f" !~ /oracle.*[.]sql$/ && "$subd/$f" !~ /postgresql.*[.]sql$/) {
#	    print "I2b2Sqlserver parse $d/$f\n";
	    $issql{"$subd/$f"}++;

	    i2b2SqlserverParsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;
		if(/^\s*(.*\S)\s+(VIEW|view)\s+(\S+)/) {
		    $vuse = $1;
		    $view = $3;
		    ($schema) =  ($d =~ /\/([^\/]+)\/views$/);
		    $vuse = uc($vuse);
		    $schema = uc($schema);
		    $view = uc($view);
		    $schema =~ s/\"//g;
		    $view =~ s/\"//g;
		    if($vuse =~ /^CREATE/) {
			$isViewFile{"$schema.$view"} = "$d/$f";
			$cview = 1;
		    }
		    elsif($vuse =~ /^COMMENT/) {#COMMENT ON VIEW - ignore
		    }
		    else {
#			print STDERR "$d/$f unexpected view $view     $vuse     '$rest'\n";
		    }
		}
	    }
	    close IN;
	}
    }
    closedir(ISDIR);
    return $err;
}

sub parseI2b2SqlserverScripts($){
    my ($d) = @_;
    local *ISDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;

    opendir(ISDIR,"$d") || die "parseI2b2SqlserverScripts failed to open $d";

    while($f = readdir(ISDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    print "I2b2SqlserverScripts subdir $d/$f\n";
	    next;
	}
	if($f =~ /[.]php$/) {
	}
	else {
	    print "I2b2Sqlserver parse $d/$f\n";
	    next;
	}
	i2b2SqlserverUnparsed("$d/$f",$f);
	open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	while(<IN>) {
	}
	close IN;
    }
    closedir(ISDIR);
    return $err;
}

sub parseI2b2SqlserverGlobal($){
    my ($d) = @_;
    local *ISDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;

    opendir(ISDIR,"$d") || die "parseI2b2SqlserverGlobal failed to open $d";

    while($f = readdir(ISDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    print "I2b2SqlserverGlobal subdir $d/$f\n";
	    next;
	}
	if($f eq "Makefile") {
	}
	elsif($f =~ /[.]sql$/ && "$subd/$f" !~ /oracle.*[.]sql$/ && "$subd/$f" !~ /postgresql.*[.]sql$/) {
	}
	else {
	    print "I2b2Sqlserver parse $d/$f\n";
	    next;
	}
	i2b2SqlserverUnparsed("$d/$f",$f);
	open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	while(<IN>) {
	}
	close IN;
    }
    closedir(ISDIR);
    return $err;
}

sub parseI2b2Sqlserver($);
sub parseI2b2Sqlserver($){
    my ($d) = @_;
    local *ISDIR;
    local *IN;
    my $err = 0;
    my @f;
    my $f;
    my ($tuse,$schema,$table);
    my $subd = $d;
    my $target = "unknown";

    $subd =~ s/^$iplus\///g;

    opendir(ISDIR,"$d") || die "parseI2b2Sqlserver failed to open $d";

    while($f = readdir(ISDIR)){
	if($f =~ /^[.]/) {next}
	if($f =~ /[~]$/) {next}
	if(-d "$dir$d/$f") {
	    if($f eq "procedures") {
		parseI2b2SqlserverFunctions("$d/$f");
	    }
	    elsif($f eq "sqlserver"){
		parseI2b2Sqlserver("$d/$f");
	    }
	    elsif($f eq "scripts"){
		parseI2b2Sqlserver("$d/$f");
	    }
	    elsif($f eq "demo"){
		parseI2b2Sqlserver("$d/$f");
	    }
	    else {
#		print "I2b2Sqlserver subdir $d/$f\n";
	    }
	    next;
	}

	if($f =~ /[.]sql$/ && "$subd/$f" !~ /oracle.*[.]sql$/ && "$subd/$f" !~ /postgresql.*[.]sql$/) {
	    $issql{"$subd/$f"}++;

	    if($f =~ /insert_data[.]sql$/) {
		next;
	    }

	    i2b2SqlserverParsed("$d/$f",$f);
	    $ctable = 0;
	    $ctrig  = 0;
	    $cfunc  = 0;
	    $cproc  = 0;
	    $cview  = 0;
	    $cseq = 0;
	    $alterctable = 0;
	    $altertable = "undefined";
	    $tseq = "";

	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		s/\s*--.*//g;

		if($alterctable) {
		    if(/^\s*ADD CONSTRAINT (\S+) PRIMARY KEY \(([^\)]+)\)/) {
			$pkc = uc($1);
			$pk = uc($2);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			$isTablePrikey{$altertable} = $pk;
			if(defined($pkc)){$isTablePrikeyName{"$schema.$table"} = $pkc}
		    }
		    if(/^\s*ADD CONSTRAINT (\S+) UNIQUE \(([^\)]+)\)/) {
			$pkc = uc($1);
			$pk = uc($2);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){$isTableUnikey{$altertable} .= "$pkc $pk;"}
			else {$isTableUnikey{$altertable} .= ". $pk;"}
		    }
		    if(/^\s*ADD CONSTRAINT (\S+ )FOREIGN KEY (\(\S+\) )REFERENCES ([^\(]+\([^\)]+\))/){
			$pk = uc($1).uc($2);
			$pk .= uc($schema);
			$pk .= ".";
			$pk .= uc($3);
			$pk .= ";";
			$isTableForkey{"$schema.$table"} .= $pk;
		    }
		    if(/;/) {$alterctable = 0}
		}
		if(/(\S+)\s+(TABLE|table)\s+(ONLY\s+)?(\S+)/) {
		    $tuse = $1;
		    $table = $4;
		    if ($ischema ne "unknown") {
			($schema) = $ischema;
		    }
		    else {
			($schema) = ($d =~ /\/([^\/]+)$/);
		    }

		    $tuse = uc($tuse);
		    $schema = uc($schema);
		    $table = uc($table);
		    $schema =~ s/\"//g;
		    $table =~ s/\"//g;
		    if($tuse eq "CREATE") {
			$isTableFile{"$schema.$table"} = "$d/$f";
			$ctable = 1;
		    }
		    if($tuse eq "ALTER") {
			$altertable = "$schema.$table";
			$alterctable = 1;
		    }
		}
		elsif($ctable) {
		    if(/;/){$ctable=0; next}
		    if(/^\s*\(/){s/^\s*\(\s*//}
		    if(/^\s*\"position\"\s+/){s/\"position\"/position/} # used in de_variant_subject_idx
		    if(/^\s*\)/){$ctable=2; s/^\s*\)\s*//}
		    if(/^\s*([a-z]\S+)\s+(.*?),?$/) {
			$col = $1;
			$cdef = $2;
			$col = uc($col);
			$cdef =~ s/,\s+$//g;
			$isTableColumn{"$schema.$table"} .= "$col $cdef;";
			if($cdef =~ / DEFAULT nextval\(\'([^\']+)\'::regclass\) NOT NULL$/){
			    $cid = $1;
			    $cid = uc($1);
			    $isNextval{"$schema.$table"} = "$col.$cid";
			}
			elsif($cdef =~ /DEFAULT nextval/){print STDERR "$d/$f DEFAULT nextval not recognized: '$cdef'\n"}
		    }
		    if(/^\s*(CONSTRAINT (\S+)\s+)?PRIMARY KEY \(([^\)]+)\)/){
			$pkc = $2;
			$pk = uc($3);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			$isTablePrikey{"$schema.$table"} = $pk;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $isTablePrikeyName{"$schema.$table"} = $pkc;
			}
		    }
		    if(/^\s*(CONSTRAINT (\S+)\s+)?UNIQUE \(([^\)]+)\)/){
			$pkc = $2;
			$pk = uc($3);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $isTableUnikey{"$schema.$table"} .= "$pkc $pk;";
			}
			else {$isTableUnikey{"$schema.$table"} .= ". $pk;"}
		    }
		}

		if($cseq == 1 && /([^;]*)/) {
		    $tseq .= $1;
		    if(/;/) {$cseq = 2}
		}
		if(/^\s*(.*\S)\s+(SEQUENCE|sequence)\s+(\S+)(.*)/) {
		    $suse = $1;
		    $seq = $3;
		    $rest = $4;
		    $suse = uc($suse);
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $schema = uc($schema);
		    $seq = uc($seq);
		    $schema =~ s/\"//g;
		    $seq =~ s/\"//g;
#		    print "$d/$f sequence $seq     $suse\n";
		    if($suse =~ /^CREATE/) {
			$isSequenceFile{"$schema.$seq"} = "$d/$f";
			$cseq = 1;
			$tseq = $rest;
		    }
		}
		if($cseq == 2){
		    $cseq = 0;
		    $isSequenceText{"$schema.$seq"} = $tseq;
		    $tseq = "";
		}

		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s+RETURNS trigger/) {
		    $trig = $1;
		    $trig =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $trig = uc($trig);
		    if($trig !~ /^TF_/ &&
			$trig !~ /_FUN$/) {print "trigger name '$trig' $f\n"}
		    $trig =~ s/^TF_//g;
		    $trig =~ s/_FUN$//g;
		    $schema = uc($schema);
#		    print "$d/$f trigger $trig     create\n";
		    $isTriggerFile{"$schema.$trig"} = "$d/$f";
		    $ctrig = 1;
		}
		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s+RETURNS (.*)/) {
		    $func = $1;
		    $ret = $2;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    if($ret ne "trigger") {
			$isFunctionFile{"$schema.$func"} = "$d/$f";
			$isFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s*\(([^\)]*)\)\s*RETURNS (.*)/) {
		    $func = $1;
		    $ret = $2;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    if($ret ne "trigger") {
			$isFunctionFile{"$schema.$func"} = "$d/$f";
			$isFunctionReturn{"$schema.$func"} = "$ret";
			$cfunc = 1;
		    }
		}
		if(/^\s*(.*\S)\s+(VIEW|view)\s+(\S+)/) {
		    $vuse = $1;
		    $view = $3;
		    ($schema) =  ($d =~ /\/([^\/]+)$/);
		    $vuse = uc($vuse);
		    $schema = uc($schema);
		    $view = uc($view);
		    $schema =~ s/\"//g;
		    $view =~ s/\"//g;
		    if($vuse =~ /^CREATE/) {
			$isViewFile{"$schema.$view"} = "$d/$f";
			$cview = 1;
		    }
		    elsif($vuse =~ /^COMMENT/) {#COMMENT ON VIEW - ignore
		    }
		    else {
#			print STDERR "$d/$f unexpected view $view     $vuse     '$rest'\n";
		    }
		}
	    }
	    close IN;
	}
	elsif($f eq "db.properties"){
	    parseI2b2Properties($d,$f);
	}
	elsif($f eq "data_build.xml"){
#	    print "I2b2Sqlserver parse data_build.xml $d/$f\n";
	    i2b2OracleUnparsed("$d/$f",$f);
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		if(/<target name=\"([^\"]+)\"/) {
		    $target = $1;
		}
		if(/<transaction src=\"[.]\/([^\"]+)\"/) {
		    $sql = $1;
		    $sql =~ s/\$\{db[.]type\}/sqlserver/;
		    $sql =~ s/\$\{db[.]project\}/demo/;
#		    print "parseI2b2SqlServerDataBuild $d/$sql\n";
		    $isload{"$subd/$sql"}++;
		    $istarget{"$subd/$sql"} = $target;
		}
	    }
	    close IN;
	}
	else {
#	    print "I2b2Sqlserver file $d/$f\n";
	}
    }
    closedir(ISDIR);
    return $err;
}


#####################################################################################
# MAIN CODE STARTS HERE
#####################################################################################

$dir = getcwd();
$dir .= "/";
print "$dir\n";
$iplus = "../../../../../git-i2b2/i2b2-data/edu.harvard.i2b2.data/Release_1-7/NewInstall";
$oplus = "../../ddl/oracle";
$pplus = "../../ddl/postgres";
$ischema = "undefined";

# Check lists of skips

%iskip = ();
%orskip = ();
%pgskip = ();

open(SKIPI, "skip_i2b2.txt") || print STDERR "Unable to open skip_i2b2.txt";
if(defined(SKIPI)) {
    while(<SKIPI>){
	if(/(\S+)/) {$iskip{"$iplus/$1"}=0}
    }
    close SKIPI;
}

open(SKIPO, "skip_oracle.txt") || print STDERR "Unable to open skip_oracle.txt";
if(defined(SKIPO)) {
    while(<SKIPO>){
	if(/(\S+)/) {$pgskip{"$pplus/$1"}=0}
    }
    close SKIPO;
}

open(SKIPP, "skip_postgres.txt") || print STDERR "Unable to open skip_postgres.txt";
if(defined(SKIPP)) {
    while(<SKIPP>){
	if(/(\S+)/) {$orskip{"$oplus/$1"}=0}
    }
    close SKIPP;
}

# Triggers to ignore in Postgres
# e.g. logon_trigger to set Oracle user identifier
open(SKIPOT, "skip_oracle_trigger.txt") || print STDERR "Unable to open skip_oracle_trigger.txt";
if(defined(SKIPOT)) {
    while(<SKIPOT>){
	if (/(\S+)/) {$oSkipTrigger{"$1"}=0}
    }
    close SKIPOT;
}
if(-e "skip_oracle_i2b2_trigger.txt") {
    open(SKIPOT, "skip_oracle_i2b2_trigger.txt") || print STDERR "Unable to open skip_oracle_i2b2_trigger.txt";
    if(defined(SKIPOT)) {
	while(<SKIPOT>){
	    if (/(\S+)/) {$ioSkipTrigger{"$1"}=0}
	}
	close SKIPOT;
    }
}


# DDL directories for tranSMART and i2b2

opendir(IODIR, "$iplus") || die "Failed to open i2b2 installation $iplus";
opendir(IPDIR, "$iplus") || die "Failed to open i2b2 installation $iplus";
opendir(ISDIR, "$iplus") || die "Failed to open i2b2 installation $iplus";
opendir(ODIR, "$oplus") || die "Failed to open oracle DDLs $oplus";
opendir(PDIR, "$pplus") || die "Failed to open postres DDLs $pplus";

%orsql = ();
%pgsql = ();
%iosql = ();
%ipsql = ();
%issql = ();
%orload = ();
%pgload = ();
%ioload = ();
%ipload = ();
%isload = ();


# Read and select files in each directory tree


%odir = ();
$plus = $oplus;
while($d = readdir(ODIR)){
    if($d =~ /^[.]/) {next}
    if($d =~ /[~]$/) {next}
    if(-d "$dir$plus/$d") {
	if(defined($dodir{$d})){
	    $odir{$d} = "$plus/$d";
	}
	else {
	    print "Additional directory $plus/$d\n";
	}
    }
    else {
	parseOracleTop("$plus",$d);
    }
}

%pdir = ();
$plus = $pplus;
while($d = readdir(PDIR)){
    if($d =~ /^[.]/) {next}
    if($d =~ /[~]$/) {next}
    if(-d "$dir$plus/$d") {
	if(defined($dodir{$d})){
	    $pdir{$d} = "$plus/$d";
	}
	elsif(defined($dopdir{$d})){
	    $pdir{$d} = "$plus/$d";
	}
	else {
	    print "Additional directory $plus/$d\n";
	}
    }
    else {
	parsePostgresTop("$plus",$d);
    }
}

%iodir = ();
$plus = $iplus;
while($d = readdir(IODIR)){
    if($d =~ /^[.]/) {next}
    if($d =~ /[~]$/) {next}
    if(-d "$dir$plus/$d") {
	if(defined($doiodir{$d})){
	    $iodir{$d} = "$plus/$d";
	}
	else {
	    print "Additional directory $plus/$d\n";
	}
    }
    else {
	parseI2b2OracleTop("$plus",$d);
    }
}


%ipdir = ();
$plus = $iplus;
while($d = readdir(IPDIR)){
    if($d =~ /^[.]/) {next}
    if($d =~ /[~]$/) {next}
    if(-d "$dir$plus/$d") {
	if(defined($doipdir{$d})){
	    $ipdir{$d} = "$plus/$d";
	}
	else {
	    print "Additional directory $plus/$d\n";
	}
    }
    else {
	parseI2b2PostgresTop("$plus",$d);
    }
}


%isdir = ();
$plus = $iplus;
while($d = readdir(ISDIR)){
    if($d =~ /^[.]/) {next}
    if($d =~ /[~]$/) {next}
    if(-d "$dir$plus/$d") {
	if(defined($doisdir{$d})){
	    $isdir{$d} = "$plus/$d";
	}
	else {
	    print "Additional directory $plus/$d\n";
	}
    }
    else {
	parseI2b2SqlserverTop("$plus",$d);
    }
}


# Process files in directories

foreach $d (sort(keys(%odir))) {
#    print "Oracle $d\n";
    if($d eq "_scripts"){
	parseOracleScripts($odir{$d});
    }
    else {
	parseOracle($odir{$d});
    }
}

foreach $d (sort(keys(%pdir))) {
#    print "Postgres $d\n";
    if($d eq "_scripts"){
	parsePostgresScripts($pdir{$d});
    }
    elsif($d eq "macroed_functions"){
	parsePostgresMacrofun($pdir{$d});
    }
    elsif($d eq "META"){
	parsePostgresMeta($pdir{$d});
    }
    elsif($d eq "GLOBAL"){
	parsePostgresGlobal($pdir{$d});
    }
    else {
	parsePostgres($pdir{$d});
    }
}

foreach $d (sort(keys(%iodir))) {
#    print "I2b2Oracle $d\n";
    parseI2b2Oracle($iodir{$d});
}

foreach $d (sort(keys(%ipdir))) {
#    print "I2b2Postgres $d\n";
    parseI2b2Postgres($ipdir{$d});
}

foreach $d (sort(keys(%isdir))) {
#    print "I2b2Sqlserver $d\n";
    parseI2b2Sqlserver($isdir{$d});
}

# Report unparsed files

foreach $u (sort(keys(%iounparsed))){
    @u = split(/;/,$iounparsed{$u});
    my $tot = 1 + $#u;
    if($tot > 1) {
	print "I2b2 Oracle $u: $tot\n";
    }
}

foreach $u (sort(keys(%ipunparsed))){
    @u = split(/;/,$ipunparsed{$u});
    my $tot = 1 + $#u;
    if($tot > 1) {
	print "I2b2 Postgres $u: $tot\n";
    }
}

foreach $u (sort(keys(%isunparsed))){
    @u = split(/;/,$isunparsed{$u});
    my $tot = 1 + $#u;
    if($tot > 1) {
	print "I2b2 Sqlserver $u: $tot\n";
    }
}

foreach $u (sort(keys(%ounparsed))){
    @u = split(/;/,$ounparsed{$u});
    my $tot = 1 + $#u;
    if($tot > 1) {
	print "Oracle $u: $tot\n";
    }
}

foreach $u (sort(keys(%punparsed))){
    @u = split(/;/,$punparsed{$u});
    my $tot = 1 + $#u;
    if($tot > 1) {
	print "Postgres $u: $tot\n";
    }
}

# Check the Transmart Oracle items.json files define everything that we parsed from the files
# The items.json files were originally created by a dump and them manually maintained
# so errors and omissions can be expected
# ===========================================================================================

foreach $it (sort(keys(%itemsFunction))){
    if(!defined($oFunctionFile{$it})){print STDERR "Oracle itemsFunction not found $it\n"}
}

foreach $it (sort(keys(%itemsIndex))){
    if(!defined($oIndexFile{$it})){print STDERR "Oracle itemsIndex not found $it\n"}
}

foreach $it (sort(keys(%itemsMaterializedView))){
    if(!defined($oViewFile{$it})){print STDERR "Oracle itemsMaterializedView not found $it\n"}
}

foreach $it (sort(keys(%itemsProcedure))){
    if(!defined($oProcFile{$it})){print STDERR "Oracle itemsProcedure not found $it\n"}
}

foreach $it (sort(keys(%itemsRefConstraint))){
    if(!defined($oForkey{$it})){print STDERR "Oracle itemsRefConstraint not found $it\n"}
}

foreach $it (sort(keys(%itemsSequence))){
    if(!defined($oSequenceFile{$it})){print STDERR "Oracle itemsSequence not found $it\n"}
}

foreach $it (sort(keys(%itemsTable))){
    if(!defined($oTableFile{$it})){print STDERR "Oracle itemsTable not found $it\n"}
}

foreach $it (sort(keys(%itemsTrigger))){
    if(!defined($oTriggerFile{$it})){print STDERR "Oracle itemsTrigger not found $it\n"}
}

foreach $it (sort(keys(%itemsType))){
    if(!defined($oTypeFile{$it})){print STDERR "Oracle itemsType not found $it\n"}
}

foreach $it (sort(keys(%itemsView))){
    if(!defined($oViewFile{$it})){print STDERR "Oracle itemsView not found $it\n"}
}

# Now check everything that should be in an items.json file was found there

foreach $it (sort(keys(%oFunctionFile))){
    if(!defined($itemsFunction{$it})){print STDERR "Oracle no itemsFunction $it $oFunctionFile{$it}\n"}
}

foreach $it (sort(keys(%oIndexFile))){
    if(!defined($itemsIndex{$it})){print STDERR "Oracle no itemsIndex $it $oIndexFile{$it}\n"}
}

foreach $it (sort(keys(%oProcFile))){
    if(!defined($itemsProcedure{$it})){print STDERR "Oracle no itemsProcedure $it $oProcFile{$it}\n"}
}

foreach $it (sort(keys(%oForkey))){
    if(!defined($itemsRefConstraint{$it})){print STDERR "Oracle no itemsRefConstraint $it $oForkey{$it}\n"}
}

foreach $it (sort(keys(%oSequenceFile))){
    if(!defined($itemsSequence{$it})){print STDERR "Oracle no itemsSequence $it $oSequenceFile{$it}\n"}
}

foreach $it (sort(keys(%oTableFile))){
    if(!defined($itemsTable{$it})){print STDERR "Oracle no itemsTable $it $oTableFile{$it}\n"}
}

foreach $it (sort(keys(%oTriggerFile))){
    if(!defined($itemsTrigger{$it})){print STDERR "Oracle no itemsTrigger $it $oTriggerFile{$it}\n"}
}

foreach $it (sort(keys(%oTypeFile))){
    if(!defined($itemsType{$it})){print STDERR "Oracle no itemsType $it $oTypeFile{$it}\n"}
}

foreach $it (sort(keys(%oViewFile))){
    if(!defined($itemsView{$it})&& !defined($itemsMaterializedView{$it})){print STDERR "Oracle no itemsView $it $oViewFile{$it}\n"}
}




# Compare tables Oracle + Postgres

$notable = 0;
$onlyotable = 0;
foreach $t (sort(keys(%oTableFile))) {
    if(defined($orskip{$oTableFile{$t}})){next}
    ++$notable;
    @ocols = split(/;/,$oTableColumn{$t});
    $nocol = 1 + $#ocols;
    if(!defined($pTableFile{$t})){
	printf "Oracle table %3d %-50s %s\n", $nocol, $t, $oTableFile{$t};
	++$onlyotable;
	push @onlyotable, $t;
    }
    else {
	$compstr = compareColumns($oTableColumn{$t},$pTableColumn{$t});
	@pcols = split(/;/,$pTableColumn{$t});
	$npcol = 1 + $#pcols;
	if($nocol != $npcol) {$diff = "MOD"}
	elsif($compstr ne ""){$diff = "CMP"}
	else {$diff = "   "}
	$pfile = $pTableFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $oTableFile{$t}) {$pfile = "   (same)"}
	else{$pfile = "   $pTableFile{$t}"}
	if($showSame || $compstr ne "") {
	    printf "Both %3s %3d %3d %-50s %s%s\n",
		$diff, $npcol, $nocol, $t, $oTableFile{$t}, $pfile;
	    print $compstr;
	}
    }
}

$nptable = 0;
$onlyptable = 0;
foreach $t (sort(keys(%pTableFile))) {
    if(defined($pgskip{$pTableFile{$t}})){next}
    ++$nptable;
    if(!defined($oTableFile{$t})){
	printf "Postgres table %-50s %s\n", $t, $pTableFile{$t};
	++$onlyptable;
	push @onlyptable, $t;
    }
}

# Compare sequences Oracle + Postgres

$noseq = 0;
$onlyoseq = 0;
foreach $t (sort(keys(%oSequenceFile))) {
    ++$noseq;
    if(!defined($pSequenceFile{$t})){
	printf "Oracle sequence %-50s %s\n", $t, $oSequenceFile{$t};
	++$onlyoseq;
	push @onlyoseq, $t;
    }
    else {
	$compstr = compareSequence($oSequenceText{$t},$pSequenceText{$t});
	$pfile = $pSequenceFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $oSequenceFile{$t}) {$pfile = "   (same)"}
	else{$pfile = "   $pSequenceFile{$t}"}
	if($compstr eq "") {$diff = "   "}
	else {$diff = "CMP"}
	if($showSame || $compstr ne "") {
	    printf "Both %s sequence %-50s %s%s\n", $diff, $t, $oSequenceFile{$t}, $pfile;
	    print $compstr;
	}
    }
}

$npseq = 0;
$onlypseq = 0;
foreach $t (sort(keys(%pSequenceFile))) {
    ++$npseq;
    if(!defined($oSequenceFile{$t})){
	printf "Postgres sequence %-50s %s\n", $t, $pSequenceFile{$t};
	++$onlypseq;
	push @onlypseq, $t;
    }
}

# Compare triggers Oracle + Postgres

$notrig = 0;
$onlyotrig = 0;
foreach $t (sort(keys(%oTriggerFile))) {
    if(defined($oSkipTrigger{$t})){next}
    ++$notrig;
    if(!defined($pTriggerFile{$t})){
# check for Postgres nextval default
# implemented as a trigger in Oracle
	$pnext=0;
	if(defined($oNexttrig{$t})){
#	    ($tn) = ($t =~ /[^.]+[.](.*)/);
	    $st = $oNexttrig{$t};
	    $nvo = $oNextval{$st};
	    if(defined($pNextval{$st})){
		$nvp = $pNextval{$st};
		if($nvo eq $nvp) {
		    $pnext=1;
		}
		else {
#		    print STDERR "Triggers mismatch '$t' '$tn' '$nvo' '$nvp'\n";
		}
	    }
	    else {
#		print STDERR "Triggers unknown '$t' '$tn' '$nvo'\n";
	    }
	}
	if(!$pnext){
	    printf "Oracle trigger %-50s %s\n", $t, $oTriggerFile{$t};
	    ++$onlyotrig;
	    push @onlyotrig, $t;
	}
    }
    else {
	$pfile = $pTriggerFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $oTriggerFile{$t}) {$pfile = "   (same)"}
	else{$pfile = "   $pTriggerFile{$t}"}
	$tfname = $t;
	$tfname =~ s/[.]/.TF_/g;
	if($showSame || !defined($pFunctionFile{"$tfname"})) {
	    printf "Both   trigger %-50s %s%s\n", $t, $oTriggerFile{$t}, $pfile;
	}
	if(!defined($pFunctionFile{"$tfname"})){
	    print STDERR "Trigger $t has no function $tfname in $pTriggerFile{$t}\n";
	}
    }
}

$nptrig = 0;
$onlyptrig = 0;
foreach $t (sort(keys(%pTriggerFile))) {
    ++$nptrig;
    if(!defined($oTriggerFile{$t})){
	printf "Postgres trigger %-50s %s\n", $t, $pTriggerFile{$t};
	++$onlyptrig;
	push @onlyptrig, $t;
	$tfname = $t;
	$tfname =~ s/[.]/.TF_/g;
	if(!defined($pFunctionFile{"$tfname"})){
	    print STDERR "Trigger $t has no function $tfname in $pTriggerFile{$t}\n";
	}
    }
}

# Compare functions + procedures Oracle + Postgres

$nofunc = 0;
$onlyofunc = 0;
foreach $t (sort(keys(%oFunctionFile))) {
    ++$nofunc;
    if(!defined($pFunctionFile{$t})){
	printf "Oracle function %-50s %s\n", $t, $oFunctionFile{$t};
	++$onlyofunc;
	push @onlyofunc, $t;
    }
    else {
	$pfile = $pFunctionFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $oFunctionFile{$t}) {$pfile = "   (same)"}
	else{$pfile = "   $pFunctionFile{$t}"}
	if($showSame) {
	    printf "Both   function %-50s %s%s\n", $t, $oFunctionFile{$t}, $pfile;
	}
    }
}

$npfunc = 0;
$onlypfunc = 0;
foreach $t (sort(keys(%pFunctionFile))) {
    my $tt = $t;
    if($tt =~ /[.]TF_/) {
    $tt =~ s/[.]TF_/./;
if(defined($pTriggerFile{$tt})) {next}
}
    ++$npfunc;
    if(!defined($oFunctionFile{$t}) &&
       !defined($oProcFile{$t})){
	printf "Postgres function %-50s %s\n", $t, $pFunctionFile{$t};
	++$onlypfunc;
	push @onlypfunc, $t;
    }
}

$noproc = 0;
$onlyoproc = 0;
foreach $t (sort(keys(%oProcFile))) {
    ++$noproc;
    if(!defined($pFunctionFile{$t})){
	printf "Oracle procedure %-50s %s\n", $t, $oProcFile{$t};
	++$onlyoproc;
	push @onlyoproc, $t;
    }
    else {
	$pfile = $pFunctionFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $oProcFile{$t}) {$pfile = "   (same)"}
	else{$pfile = "   $pFunctionFile{$t}"}
	if($showSame) {
	    printf "Both   procedure %-50s %s%s\n", $t, $oProcFile{$t}, $pfile;
	}
    }
}

# Compare views Oracle + Postgres

$noview = 0;
$onlyoview = 0;
foreach $t (sort(keys(%oViewFile))) {
    ++$noview;
    if(!defined($pViewFile{$t})){
	printf "Oracle view %-50s %s\n", $t, $oViewFile{$t};
	++$onlyoview;
	push @onlyoview, $t;
    }
    else {
	$pfile = $pViewFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $oViewFile{$t}) {$pfile = "   (same)"}
	else{$pfile = "   $pViewFile{$t}"}
	if($showSame){
	    printf "Both   view %-50s %s%s\n", $t, $oViewFile{$t}, $pfile;
	}
    }
}

$npview = 0;
$onlypview = 0;
foreach $t (sort(keys(%pViewFile))) {
    ++$npview;
    if(!defined($oViewFile{$t})){
	printf "Postgres view %-50s %s\n", $t, $pViewFile{$t};
	++$onlypview;
	push @onlypview, $t;
    }
}


# Compare Primary keys

$noindexprim = 0;
$onlyoindexprim = 0;
$diffindexprim = 0;
foreach $t (sort(keys(%oTablePrikey))) {
    ++$noindexprim;
    if(!defined($pTablePrikey{$t})){
	printf "Oracle primary index %-50s (%s) %s\n", $t, $oTablePrikeyName{$t}, $oTablePrikey{$t};
	++$onlyoindexprim;
	push @onlyoindexprim, $t;
    }
    else {
	if($oTablePrikey{$t} eq $pTablePrikey{$t}) {$pindex = "   (same)"}
	else {
	    $pindex .= " $pTablePrikey{$t})";
	    ++$diffindexprim;
	    push @diffindexprim, "$t ($oTablePrikeyName{$t}) $oTablePrikey{$t} <=> $pindex";
	}
	if($showSame){
	    printf "Both   primary index %-50s %s%s\n", $t, $oTablePrikey{$t}, $pindex;
	}
    }
}

$npindexprim = 0;
$onlypindexprim = 0;
foreach $t (sort(keys(%pTablePrikey))) {
    ++$npindexprim;
    if(!defined($oTablePrikey{$t})){
	printf "Postgres primary index %-50s %s\n", $t, $pTablePrikey{$t};
	++$onlypindexprim;
	push @onlypindexprim, $t;
    }
}


# Compare Unique keys

$noindexuni = 0;
$onlyoindexuni = 0;
$diffindexuni = 0;
foreach $t (sort(keys(%oTableUnikey))) {
    ++$noindexuni;
    if(!defined($pTableUnikey{$t})){
	printf "Oracle unique index %-50s %s\n", $t, $oTableUnikey{$t};
	++$onlyoindexuni;
	push @onlyoindexuni, $t;
    }
    else {
	if($oTableUnikey{$t} eq $pTableUnikey{$t}) {$pindex = "   (same)"}
	else {
	    $pindex = "($pTableUnikey{$t})";
	    ++$diffindexuni;
	    push @diffindexuni, "$t ($oTableUnikey{$t}) <=> $pindex";
	}
	if($showSame){
	    printf "Both   unique index %-50s %s%s\n", $t, $oTableUnikey{$t}, $pindex;
	}
    }
}

$npindexuni = 0;
$onlypindexuni = 0;
foreach $t (sort(keys(%pTableUnikey))) {
    ++$npindexuni;
    if(!defined($oTableUnikey{$t})){
	printf "Postgres unique index %-50s %s\n", $t, $pTableUnikey{$t};
	++$onlypindexuni;
	push @onlypindexuni, $t;
    }
}

# Compare Foreign keys

$noindexfor = 0;
$onlyoindexfor = 0;
$diffindexfor = 0;
foreach $t (sort(keys(%oTableForkey))) {
    ++$npindexfor;
    if(!defined($pTableForkey{$t})){
	printf "Oracle foreign index %-50s %s\n", $t, $oTableForkey{$t};
	++$onlyoindexfor;
	push @onlyoindexfor, $t;
    }
    else {
	if(compareForkey($oTableForkey{$t},$pTableForkey{$t},$t,'transmart')) {$pindex = "   (same)"}
	else{
	    print STDERR "Transmart foreign keys differ: $compareForkey\n";
	    $pindex = "($pTableForkey{$t})";
	    ++$diffindexfor;
	    push @diffindexfor, "$t ($oTableForkey{$t}) <=> $pindex";
	}
	if($showSame){
	    printf "Both   foreign index %-50s %s%s\n", $t, $oTableForkey{$t}, $pindex;
	}
    }
}

$npindexfor = 0;
$onlypindexfor = 0;
foreach $t (sort(keys(%pTableForkey))) {
    ++$noindexfor;
    if(!defined($oTableForkey{$t})){
	printf "Postgres foreign index %-50s %s\n", $t, $pTableForkey{$t};
	++$onlypindexfor;
	push @onlypindexfor, $t;
    }
}

# Compare Indexes

$noindex = 0;
$onlyoindex = 0;
$diffindex = 0;
foreach $t (sort(keys(%oIndex))) {
    ++$noindex;
    if(!defined($pIndex{$t})){
	printf "Oracle index %-50s %s\n", $t, $oIndex{$t};
	++$onlyoindex;
	push @onlyoindex, $t;
    }
    else {
	if($oIndex{$t} eq $pIndex{$t}) {$pindex = "   (same)"}
	else {
	    $pindex = "$pIndex{$t}";
	    ++$diffindex;
	    push @diffindex, "$t $oIndex{$t} <=> $pindex";
	}
	if($showSame){
	    printf "Both          index %-50s %s%s\n", $t, $oIndex{$t}, $pindex;
	}
    }
}

$npindex = 0;
$onlypindex = 0;
foreach $t (sort(keys(%pIndex))) {
    ++$npindex;
    if(!defined($oIndex{$t})){
	printf "Postgres        index %-50s %s\n", $t, $pIndex{$t};
	++$onlypindex;
	push @onlypindex, $t;
    }
}


# Check I2b2 Oracle + Postgres + Sqlserver
# Also check against Oracle + Postgres



# Print results

print "\n";
print "----------- transmart oracle <=> postgres ------------------------------\n";
print "\n";
print "  Oracle files: ".(scalar keys %orsql)."\n";
print "Postgres files: ".(scalar keys %pgsql)."\n";
print "  Oracle loads: ".(scalar keys %orload)."\n";
print "Postgres loads: ".(scalar keys %pgload)."\n";
print "  Oracle skips: ".(scalar keys %orskip)."\n";
print "Postgres skips: ".(scalar keys %pgskip)."\n";
print "\n";
print "  Oracle tables: $notable\n";
print "Postgres tables: $nptable\n";
print "  Oracle new tables: $onlyotable\n";
print "Postgres new tables: $onlyptable\n";
print "  Oracle-only columns:  $onewcol\n";
print "Postgres-only columns:  $pnewcol\n";
print "\n";

print "  Oracle sequences: $noseq\n";
print "Postgres sequences: $npseq\n";
print "  Oracle new sequences: $onlyoseq\n";
print "Postgres new sequences: $onlypseq\n";
print "\n";

print "  Oracle triggers: $notrig\n";
print "Postgres triggers: $nptrig\n";
print "  Oracle new triggers: $onlyotrig\n";
print "Postgres new triggers: $onlyptrig\n";
print "\n";

print "  Oracle functions: $nofunc\n";
print "Postgres functions: $npfunc\n";
print "  Oracle new functions: $onlyofunc\n";
print "Postgres new functions: $onlypfunc\n";
print "\n";

print "  Oracle procedures: $noproc\n";
print "  Oracle new procedures: $onlyoproc\n";
print "\n";

print "  Oracle views: $noview\n";
print "Postgres views: $npview\n";
print "  Oracle new views: $onlyoview\n";
print "Postgres new views: $onlypview\n";
print "\n";
print "-----------------------------------------------------------\n";
print "\n";

foreach $os (sort(keys(%orsql))){
    if($orsql{$os} != 1) {
	print STDERR "$os found $orsql{$os} times\n";
    }
    if(!defined($orload{$os})) {
	print STDERR "Oracle $os not loaded\n";
    }
#    elsif($orload{$os} != 1) {
#	print STDERR "Oracle $os loaded $orload{$os} times\n";
#    }
}

foreach $os (sort(keys(%orload))){
    if(!defined($orsql{$os}) && !defined($orskip{"$oplus/$os"})) {
	print STDERR "Oracle $os unknown\n";
#	if($orload{$os} != 1) {
#	    print STDERR "Oracle $os loaded $orload{$os} times\n";
#	}
    }
}

foreach $ps (sort(keys(%pgsql))){
    if($pgsql{$ps} != 1) {
	print STDERR "Postgres $ps found $pgsql{$ps} times\n";
    }
    if(!defined($pgload{$ps})) {
	print STDERR "Postgres $ps not loaded\n";
    }
    elsif($pgload{$ps} != 1) {
	print STDERR "Postgres $ps loaded $pgload{$ps} times\n";
    }
}

foreach $ps (sort(keys(%pgload))){
    if(!defined($pgsql{$ps}) && !defined($pgskip{"$pplus/$ps"})) {
	print STDERR "Postgres $ps unknown\n";
	if($pgload{$ps} != 1) {
	    print STDERR "Postgres $ps loaded $pgload{$ps} times\n";
	}
    }
}

# ==============================
# Test I2b2
# ==============================

$i = 0;
$j = 0;
foreach $io (sort(keys(%iosql))){
    $i++;
    if($iosql{$io} != 1) {
	print STDERR "I2b2Oracle $io found $iosql{$io} times\n";
    }
    if(!defined($ioload{$io})) {
	$j++;
	print STDERR "I2b2Oracle $io not loaded\n";
    }
    elsif($ioload{$io} != 1) {
	print STDERR "I2b2Oracle $io loaded $ioload{$io} times\n";
    }
}
print STDERR "Tested $i I2b2Oracle files, $j not loaded\n";

$i = 0;
$j = 0;
foreach $io (sort(keys(%ioload))){
    $i++;
    if(!defined($iosql{$io}) && !defined($iskip{"$iplus/$io"})) {
	$j++;
##	print STDERR "I2b2Oracle $io unknown for target $iotarget{$io}\n";
	if($ioload{$io} != 1) {
	    print STDERR "I2b2Oracle $io loaded $ioload{$io} times\n";
	}
    }
}
print STDERR "Tested $i in I2b2Oracle data_build.xml $j unknown\n";

$i = 0;
$j = 0;
foreach $ip (sort(keys(%ipsql))){
    $i++;
    if($ipsql{$ip} != 1) {
	print STDERR "I2b2Postgres $ip found $ipsql{$ip} times\n";
    }
    if(!defined($ipload{$ip})) {
	$j++;
	print STDERR "I2b2Postgres $ip not loaded\n";
    }
    elsif($ipload{$ip} != 1) {
	print STDERR "I2b2Postgres $ip loaded $ipload{$ip} times\n";
    }
}
print STDERR "Tested $i I2b2Postgres files, $j not loaded\n";

$i=0;
$j=0;
foreach $ip (sort(keys(%ipload))){
    $i++;
    if(!defined($ipsql{$ip}) && !defined($iskip{"$iplus/$ip"})) {
	$j++;
##	print STDERR "I2b2Postgres $ip unknown for target $iptarget{$ip}\n";
	if($ipload{$ip} != 1) {
	    print STDERR "I2b2Postgres $ip loaded $ipload{$ip} times\n";
	}
    }
}
print STDERR "Tested $i in I2b2Postgres data_build.xml $j unknown\n";

$i = 0;
$j = 0;
foreach $is (sort(keys(%issql))){
    $i++;
    if($issql{$is} != 1) {
	print STDERR "I2b2Sqlserver $is found $issql{$is} times\n";
    }
    if(!defined($isload{$is})) {
	$j++;
	print STDERR "I2b2Sqlserver $is not loaded\n";
    }
    elsif($isload{$is} != 1) {
	print STDERR "I2b2Sqlserver $is loaded $isload{$is} times\n";
    }
}
print STDERR "Tested $i I2b2Sqlserver files, $j not loaded\n";

$i = 0;
$j = 0;
foreach $is (sort(keys(%isload))){
    $i++;
    if(!defined($issql{$is}) && !defined($iskip{"$iplus/$is"})) {
	$j++;
##	print STDERR "I2b2Sqlserver $is unknown for target $istarget{$is}\n";
	if($isload{$is} != 1) {
	    print STDERR "I2b2Sqlserver $is loaded $isload{$is} times\n";
	}
    }
}
print STDERR "Tested $i in I2b2Sqlserver data_build.xml $j unknown\n";


# Compare i2b2 oracle and postgres
# ================================

# I2b2 Compare tables Oracle + Postgres
# =====================================

$inotable = 0;
$ionlyotable = 0;
foreach $t (sort(keys(%ioTableFile))) {
    if(defined($iskip{$ioTableFile{$t}})){next}
    ++$inotable;
    @ocols = split(/;/,$ioTableColumn{$t});
    $nocol = 1 + $#ocols;
    if(!defined($ipTableFile{$t})){
	printf "I2b2Oracle table %3d %-50s %s\n", $nocol, $t, $ioTableFile{$t};
	++$ionlyotable;
	push @ionlyotable, $t;
    }
    else {
	$compstr = compareI2b2Columns($ioTableColumn{$t},$ipTableColumn{$t});
	@pcols = split(/;/,$ipTableColumn{$t});
	$npcol = 1 + $#pcols;
	if($nocol != $npcol) {$diff = "MOD"}
	elsif($compstr ne ""){$diff = "CMP"}
	else {$diff = "   "}
	$pfile = $ipTableFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $ioTableFile{$t}) {$pfile = "  (same)"}
	else{$pfile = "   $ipTableFile{$t}"}
	if($showSame || $compstr ne "") {
	    printf "I2b2Both %3s %3d %3d %-50s %s%s\n",
		$diff, $npcol, $nocol, $t, $ioTableFile{$t}, $pfile;
	    print $compstr;
	}
    }
}

$inptable = 0;
$ionlyptable = 0;
foreach $t (sort(keys(%ipTableFile))) {
    if(defined($iskip{$ipTableFile{$t}})){next}
    ++$inptable;
    if(!defined($ioTableFile{$t})){
	printf "I2b2Postgres table %-50s %s\n", $t, $ipTableFile{$t};
	++$ionlyptable;
	push @ionlyptable, $t;
    }
}

# I2b2 Compare sequences Oracle + Postgres
# ========================================

$inoseq = 0;
$ionlyoseq = 0;
foreach $t (sort(keys(%ioSequenceFile))) {
    ++$inoseq;
    if(!defined($ipSequenceFile{$t})){
	printf "I2b2Oracle sequence %-50s %s\n", $t, $ioSequenceFile{$t};
	++$ionlyoseq;
	push @ionlyoseq, $t;
    }
    else {
	$compstr = compareSequence($ioSequenceText{$t},$ipSequenceText{$t});
	$pfile = $ipSequenceFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $ioSequenceFile{$t}) {$pfile = "   (same)"}
	else{$pfile = "   $ipSequenceFile{$t}"}
	if($compstr eq "") {$diff = "   "}
	else {$diff = "CMP"}
	if($showSame || $compstr ne "") {
	    printf "I2b2Both %s sequence %-50s %s%s\n", $diff, $t, $ioSequenceFile{$t}, $pfile;
	    print $compstr;
	}
    }
}

$inpseq = 0;
$ionlypseq = 0;
foreach $t (sort(keys(%ipSequenceFile))) {
    ++$inpseq;
    if(!defined($ioSequenceFile{$t})){
	printf "I2b2Postgres sequence %-50s %s\n", $t, $ipSequenceFile{$t};
	++$ionlypseq;
	push @ionlypseq, $t;
    }
}

# I2b2 Compare triggers Oracle + Postgres
# =======================================

$inotrig = 0;
$ionlyotrig = 0;
foreach $t (sort(keys(%ioTriggerFile))) {
    if(defined($ioSkipTrigger{$t})){next}
    ++$inotrig;
    if(!defined($ipTriggerFile{$t})){
# check for Postgres nextval default
# implemented as a trigger in Oracle
	$pnext=0;
	if(defined($ioNexttrig{$t})){
#	    ($tn) = ($t =~ /[^.]+[.](.*)/);
	    $st = $ioNexttrig{$t};
	    $nvo = $ioNextval{$st};
	    if(defined($ipNextval{$st})){
		$nvp = $ipNextval{$st};
		if($nvo eq $nvp) {
		    $pnext=1;
		}
		else {
#		    print STDERR "Triggers mismatch '$t' '$tn' '$nvo' '$nvp'\n";
		}
	    }
	    else {
#		print STDERR "Triggers unknown '$t' '$tn' '$nvo'\n";
	    }
	}
	if(!$pnext){
	    printf "I2b2Oracle trigger %-50s %s\n", $t, $ioTriggerFile{$t};
	    ++$ionlyotrig;
	    push @ionlyotrig, $t;
	}
    }
    else {
	$pfile = $ipTriggerFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $ioTriggerFile{$t}) {$pfile = "   (same)"}
	else{$pfile = "   $ipTriggerFile{$t}"}
	printf "I2b2Both   trigger %-50s %s%s\n", $t, $ioTriggerFile{$t}, $pfile;
	$tfname = $t;
	$tfname =~ s/[.]/.TF_/g;
	if(!defined($ipFunctionFile{"$tfname"})){
	    print STDERR "I2b2Trigger $t has no function $tfname in $ipTriggerFile{$t}\n";
	}
    }
}

$inptrig = 0;
$ionlyptrig = 0;
foreach $t (sort(keys(%ipTriggerFile))) {
    ++$inptrig;
    if(!defined($ioTriggerFile{$t})){
	printf "I2b2Postgres trigger %-50s %s\n", $t, $ipTriggerFile{$t};
	++$ionlyptrig;
	push @ionlyptrig, $t;
	$tfname = $t;
	$tfname =~ s/[.]/.TF_/g;
	if(!defined($ipFunctionFile{"$tfname"})){
	    print STDERR "I2b2Trigger $t has no function $tfname in $ipTriggerFile{$t}\n";
	}
    }
}

# Compare I2b2 functions + procedures Oracle + Postgres
# =====================================================

$inofunc = 0;
$ionlyofunc = 0;
foreach $t (sort(keys(%ioFunctionFile))) {
    ++$inofunc;
    if(!defined($ipFunctionFile{$t})){
	printf "I2b2Oracle function %-50s %s\n", $t, $ioFunctionFile{$t};
	++$ionlyofunc;
	push @ionlyofunc, $t;
    }
    else {
	$pfile = $ipFunctionFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $ioFunctionFile{$t}) {$pfile = "   (same)"}
	else{$pfile = "   $ipFunctionFile{$t}"}
	printf "I2b2Both   function %-50s %s%s\n", $t, $ioFunctionFile{$t}, $pfile;
    }
}

$inpfunc = 0;
$ionlypfunc = 0;
foreach $t (sort(keys(%ipFunctionFile))) {
    my $tt = $t;
    if($tt =~ /[.]TF_/) {
	$tt =~ s/[.]TF_/./;
	if(defined($ipTriggerFile{$tt})) {next}
    }
    ++$inpfunc;
    if(!defined($ioFunctionFile{$t}) &&
       !defined($ioProcFile{$t})){
	printf "I2b2Postgres function %-50s %s\n", $t, $ipFunctionFile{$t};
	++$ionlypfunc;
	push @ionlypfunc, $t;
    }
}

$inoproc = 0;
$ionlyoproc = 0;
foreach $t (sort(keys(%ioProcFile))) {
    ++$inoproc;
    if(!defined($ipFunctionFile{$t})){
	printf "I2b2Oracle procedure %-50s %s\n", $t, $ioProcFile{$t};
	++$ionlyoproc;
	push @ionlyoproc, $t;
    }
    else {
	$pfile = $ipFunctionFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $ioProcFile{$t}) {$pfile = "   (same)"}
	else{$pfile = "   i$pFunctionFile{$t}"}
	printf "I2b2Both   procedure %-50s %s%s\n", $t, $ioProcFile{$t}, $pfile;
    }
}

# Compare I2b2 views Oracle + Postgres
# ====================================

$inoview = 0;
$ionlyoview = 0;
foreach $t (sort(keys(%ioViewFile))) {
    ++$inoview;
    if(!defined($ipViewFile{$t})){
	printf "I2b2Oracle view %-50s %s\n", $t, $ioViewFile{$t};
	++$ionlyoview;
	push @ionlyoview, $t;
    }
    else {
	$pfile = $ipViewFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $ioViewFile{$t}) {$pfile = "   (same)"}
	else{$pfile = "   $ipViewFile{$t}"}
	printf "I2b2Both   view %-50s %s%s\n", $t, $ioViewFile{$t}, $pfile;
    }
}

$inpview = 0;
$ionlypview = 0;
foreach $t (sort(keys(%ipViewFile))) {
    ++$inpview;
    if(!defined($ioViewFile{$t})){
	printf "I2b2Postgres view %-50s %s\n", $t, $ipViewFile{$t};
	++$ionlypview;
	push @ionlypview, $t;
    }
}




# Compare Primary keys

$inoindexprim = 0;
$ionlyoindexprim = 0;
$idiffindexprim = 0;
foreach $t (sort(keys(%ioTablePrikey))) {
    ++$inoindexprim;
    if(!defined($ipTablePrikey{$t})){
	printf "I2b2Oracle primary index %-50s (%s) %s\n", $t, $ioTablePrikeyName{$t}, $ioTablePrikey{$t};
	++$ionlyoindexprim;
	push @ionlyoindexprim, $t;
    }
    else {
	if($ioTablePrikey{$t} eq $ipTablePrikey{$t}) {$pindex = "   (same)"}
	else {
	    $pindex .= " $pTablePrikey{$t})";
	    ++$idiffindexprim;
	    push @idiffindexprim, "$t ($oTablePrikeyName{$t}) $oTablePrikey{$t} <=> $pindex";
	}
	if($showSame){
	    printf "I2b2Both   primary index %-50s %s%s\n", $t, $oTablePrikey{$t}, $pindex;
	}
    }
}

$inpindexprim = 0;
$ionlypindexprim = 0;
foreach $t (sort(keys(%ipTablePrikey))) {
    ++$inpindexprim;
    if(!defined($ioTablePrikey{$t})){
	printf "I2b2Postgres primary index %-50s %s\n", $t, $ipTablePrikey{$t};
	++$ionlypindexprim;
	push @ionlypindexprim, $t;
    }
}


# Compare Unique keys

$inoindexuni = 0;
$ionlyoindexuni = 0;
$idiffindexuni = 0;
foreach $t (sort(keys(%ioTableUnikey))) {
    ++$inoindexuni;
    if(!defined($ipTableUnikey{$t})){
	printf "I2b2Oracle unique index %-50s %s\n", $t, $ioTableUnikey{$t};
	++$ionlyoindexuni;
	push @ionlyoindexuni, $t;
    }
    else {
	if($ioTableUnikey{$t} eq $ipTableUnikey{$t}) {$pindex = "   (same)"}
	else {
	    $pindex = "($ipTableUnikey{$t})";
	    ++$idiffindexuni;
	    push @idiffindexuni, "$t ($ioTableUnikey{$t}) <=> $pindex";
	}
	if($showSame){
	    printf "I2b2Both   unique index %-50s %s%s\n", $t, $ioTableUnikey{$t}, $pindex;
	}
    }
}

$inpindexuni = 0;
$ionlypindexuni = 0;
foreach $t (sort(keys(%ipTableUnikey))) {
    ++$inpindexuni;
    if(!defined($ioTableUnikey{$t})){
	printf "I2b2Postgres unique index %-50s %s\n", $t, $ipTableUnikey{$t};
	++$ionlypindexuni;
	push @ionlypindexuni, $t;
    }
}

# Compare Foreign keys

$inoindexfor = 0;
$ionlyoindexfor = 0;
$idiffindexfor = 0;
foreach $t (sort(keys(%ioTableForkey))) {
    ++$inpindexfor;
    if(!defined($ipTableForkey{$t})){
	printf "I2b2Oracle foreign index %-50s %s\n", $t, $ioTableForkey{$t};
	++$ionlyoindexfor;
	push @ionlyoindexfor, $t;
    }
    else {
	if(compareForkey($ioTableForkey{$t},$ipTableForkey{$t},$t,'i2b2')) {$pindex = "   (same)"}
	else{
	    print STDERR "I2b2 foreign keys differ: $compareForkey\n";
	    $pindex = "($ipTableForkey{$t})";
	    ++$idiffindexfor;
	    push @idiffindexfor, "$t ($ioTableForkey{$t}) <=> $pindex";
	}
	if($showSame){
	    printf "I2b2Both   foreign index %-50s %s%s\n", $t, $ioTableForkey{$t}, $pindex;
	}
    }
}

$inpindexfor = 0;
$ionlypindexfor = 0;
foreach $t (sort(keys(%ipTableForkey))) {
    ++$inoindexfor;
    if(!defined($ioTableForkey{$t})){
	printf "I2b2Postgres foreign index %-50s %s\n", $t, $ipTableForkey{$t};
	++$ionlypindexfor;
	push @ionlypindexfor, $t;
    }
}


# Compare Indexes

$inoindex = 0;
$ionlyoindex = 0;
$idiffindex = 0;
foreach $t (sort(keys(%ioIndex))) {
    ++$inoindex;
    if(!defined($ipIndex{$t})){
	printf "Oracle index %-50s %s\n", $t, $ioIndex{$t};
	++$ionlyoindex;
	push @ionlyoindex, $t;
    }
    else {
	if($ioIndex{$t} eq $ipIndex{$t}) {$ipindex = "   (same)"}
	else {
	    $pindex = "$ipIndex{$t}";
	    ++$idiffindex;
	    push @idiffindex, "$t $ioIndex{$t} <=> $pindex";
	}
	if($showSame){
	    printf "Both          index %-50s %s%s\n", $t, $ioIndex{$t}, $pindex;
	}
    }
}

$inpindex = 0;
$ionlypindex = 0;
foreach $t (sort(keys(%ipIndex))) {
    ++$inpindex;
    if(!defined($ioIndex{$t})){
	printf "Postgres        index %-50s %s\n", $t, $ipIndex{$t};
	++$ionlypindex;
	push @ionlypindex, $t;
    }
}

# Check I2b2 Oracle + Postgres + Sqlserver
# Also check against Oracle + Postgres



# Print results

print "\n";
print "------------------------- i2b2 postgres <=> i2b2 oracle -----------------------------\n";
print "\n";
print "  Oracle I2b2 tables: $inotable\n";
print "Postgres I2b2 tables: $inptable\n";
print "  Oracle I2b2 new tables: $ionlyotable\n";
print "Postgres I2b2 new tables: $ionlyptable\n";
print "  Oracle-only I2b2 columns:  $ionewcol\n";
print "Postgres-only I2b2 columns:  $ipnewcol\n";
print "\n";

print "  Oracle I2b2 sequences: $inoseq\n";
print "Postgres I2b2 sequences: $inpseq\n";
print "  Oracle I2b2 new sequences: $ionlyoseq\n";
print "Postgres I2b2 new sequences: $ionlypseq\n";
print "\n";

print "  Oracle I2b2 triggers: $inotrig\n";
print "Postgres I2b2 triggers: $inptrig\n";
print "  Oracle I2b2 new triggers: $ionlyotrig\n";
print "Postgres I2b2 new triggers: $ionlyptrig\n";
print "\n";

print "  Oracle I2b2 functions: $inofunc\n";
print "Postgres I2b2 functions: $inpfunc\n";
print "  Oracle I2b2 new functions: $ionlyofunc\n";
print "Postgres I2b2 new functions: $ionlypfunc\n";
print "\n";

print "  Oracle I2b2 procedures: $inoproc\n";
print "  Oracle I2b2 new procedures: $ionlyoproc\n";
print "\n";

print "  Oracle I2b2 views: $inoview\n";
print "Postgres I2b2 views: $inpview\n";
print "  Oracle I2b2 new views: $ionlyoview\n";
print "Postgres I2b2 new views: $ionlypview\n";
print "\n";
print "--------------------------------------------------------------\n";
print "\n";


# =========================================
# Compare Oracle i2b2 with Oracle transmart
# =========================================

# Compare tables Oracle I2b2 + Transmart
# ======================================

$nibtable = 0;
$onlyibtable = 0;
foreach $t (sort(keys(%ioTableFile))) {
    if(defined($iskip{$ioTableFile{$t}})){next}
    ++$nibtable;
    @icols = split(/;/,$ioTableColumn{$t});
    $nicol = 1 + $#icols;
    if(!defined($oTableFile{$t})){
	printf "I-T I2b2 table %3d %-50s %s\n", $nicol, $t, $ioTableFile{$t};
	++$onlyibtable;
	push @onlyibtable, $t;
    }
    else {
	$compstr = compareOracleColumns($ioTableColumn{$t},$oTableColumn{$t});
	@ocols = split(/;/,$oTableColumn{$t});
	$nocol = 1 + $#ocols;
	if($nicol != $nocol) {$diff = "MOD"}
	elsif($compstr ne ""){$diff = "CMP"}
	else {$diff = "   "}
	$ofile = $oTableFile{$t};
	if($ofile eq $ioTableFile{$t}) {$ofile = ""}
	else{$ofile = "   $oTableFile{$t}"}
	if($showSame || $compstr ne "") {
	    printf "I-T Both %3s %3d %3d %-50s %s%s\n",
		$diff, $nocol, $nicol, $t, $ioTableFile{$t}, $ofile;
	    print $compstr;
	}
    }
}

$ntmtable = 0;
$onlytmtable = 0;
foreach $t (sort(keys(%oTableFile))) {
    if(defined($iskip{$oTableFile{$t}})){next}
    if($t !~ /^I2B2/) {next}
    ++$ntmtable;
    if(!defined($ioTableFile{$t})){
	printf "I-T Transmart table %-50s %s\n", $t, $oTableFile{$t};
	++$onlytmtable;
	push @onlytmtable, $t;
    }
}

# Compare sequences Oracle I2B2 + Transmart
# =========================================

$nibseq = 0;
$onlyibseq = 0;
foreach $t (sort(keys(%ioSequenceFile))) {
    ++$nibseq;
    if(!defined($oSequenceFile{$t})){
	printf "I-T I2b2 sequence %-50s %s\n", $t, $ioSequenceFile{$t};
	++$onlyibseq;
	push @onlyibseq, $t;
    }
    else {
	$compstr = compareSequence($ioSequenceText{$t},$oSequenceText{$t});
	$ofile = $oSequenceFile{$t};
	if($ofile eq $ioSequenceFile{$t}) {$ofile = ""}
	else{$ofile = "   $oSequenceFile{$t}"}
	if($compstr eq "") {$diff = "   "}
	else {$diff = "CMP"}
	if($showSame || $compstr ne "") {
	    printf "I-T Both %s sequence %-50s %s%s\n", $diff, $t, $ioSequenceFile{$t}, $ofile;
	    print $compstr;
	}
    }
}

$ntmseq = 0;
$onlytmseq = 0;
foreach $t (sort(keys(%oSequenceFile))) {
    if($t !~ /^I2B2/) {next}
    ++$ntmseq;
    if(!defined($ioSequenceFile{$t})){
	printf "I-T Transmart sequence %-50s %s\n", $t, $oSequenceFile{$t};
	++$onlytmseq;
	push @onlytmseq, $t;
    }
}

# Compare triggers Oracle I2b2 + Transmart
# ========================================

$nibtrig = 0;
$onlyibtrig = 0;
foreach $t (sort(keys(%ioTriggerFile))) {
    if(defined($ioSkipTrigger{$t})){next}
    ++$nibtrig;
    if(!defined($oTriggerFile{$t})){
# check for Postgres nextval default
# implemented as a trigger in Oracle
	$pnext=0;
	if(defined($ioNexttrig{$t})){
#	    ($tn) = ($t =~ /[^.]+[.](.*)/);
	    $st = $ioNexttrig{$t};
	    $nvo = $ioNextval{$st};
	    if(defined($ipNextval{$st})){
		$nvp = $ipNextval{$st};
		if($nvo eq $nvp) {
		    $pnext=1;
		}
		else {
#		    print STDERR "Triggers mismatch '$t' '$tn' '$nvo' '$nvp'\n";
		}
	    }
	    else {
#		print STDERR "Triggers unknown '$t' '$tn' '$nvo'\n";
	    }
	}
	if(!$pnext){
	    printf "I-T I2b2 trigger %-50s %s\n", $t, $ioTriggerFile{$t};
	    ++$onlyibtrig;
	    push @onlyibtrig, $t;
	}
    }
    else {
	$ofile = $oTriggerFile{$t};
	if($ofile eq $ioTriggerFile{$t}) {$ofile = ""}
	else{$ofile = "   $oTriggerFile{$t}"}
	if($showSame) {
	    printf "I-T Both   trigger %-50s %s%s\n", $t, $ioTriggerFile{$t}, $pfile;
	}
#	$tfname = $t;
#	$tfname =~ s/[.]/.TF_/g;
#	if(!defined($oFunctionFile{"$tfname"})){
#	    print STDERR "I2b2Trigger $t has no function $tfname in $oTriggerFile{$t}\n";
#	}
    }
}

$ntmtrig = 0;
$onlytmtrig = 0;
foreach $t (sort(keys(%oTriggerFile))) {
    if($t !~ /^I2B2/){next}
    ++$ntmtrig;
    if(!defined($ioTriggerFile{$t})){
	printf "I-T Transmart trigger %-50s %s\n", $t, $oTriggerFile{$t};
	++$onlytmtrig;
	push @onlytmtrig, $t;
#	$tfname = $t;
#	$tfname =~ s/[.]/.TF_/g;
#	if(!defined($oFunctionFile{"$tfname"})){
#	    print STDERR "I-T I2b2Trigger $t has no function $tfname in $oTriggerFile{$t}\n";
#	}
    }
}

# Compare functions Oracle I2b2 + Transmart
# =========================================

$nibfunc = 0;
$onlyibfunc = 0;
foreach $t (sort(keys(%ioFunctionFile))) {
    ++$nibfunc;
    if(!defined($oFunctionFile{$t})){
	printf "I-T I2b2 function %-50s %s\n", $t, $ioFunctionFile{$t};
	++$onlyibfunc;
	push @onlyibfunc, $t;
    }
    else {
	$ofile = $oFunctionFile{$t};
	if($ofile eq $ioFunctionFile{$t}) {$ofile = ""}
	else{$ofile = "   $oFunctionFile{$t}"}
	if($showSame) {
	    printf "I-T Both   function %-50s %s%s\n", $t, $ioFunctionFile{$t}, $ofile;
	}
    }
}

$ntmfunc = 0;
$onlytmfunc = 0;
foreach $t (sort(keys(%oFunctionFile))) {
    if($t !~ /^I2B2/) {next}
    ++$ntmfunc;
    if(!defined($ioFunctionFile{$t})){
	printf "I-T Transmart function %-50s %s\n", $t, $oFunctionFile{$t};
	++$onlytmfunc;
	push @onlytmfunc, $t;
    }
}

# Compare procedures Oracle I2b2 + Transmart
# ==========================================

$nibproc = 0;
$onlyibproc = 0;
foreach $t (sort(keys(%ioProcFile))) {
    ++$nibproc;
    if(!defined($oProcFile{$t})){
	printf "I-T I2b2 procedure %-50s %s\n", $t, $ioProcFile{$t};
	++$onlyibproc;
	push @onlyibproc, $t;
    }
    else {
	$ofile = $oProcFile{$t};
	if($ofile eq $ioProcFile{$t}) {$ofile = ""}
	else{$ofile = "   $oProcFile{$t}"}
	if($showSame) {
	    printf "I-T Both   procedure %-50s %s%s\n", $t, $ioProcFile{$t}, $ofile;
	}
    }
}

$ntmproc = 0;
$onlytmproc = 0;
foreach $t (sort(keys(%oProcFile))) {
    if($t !~ /^I2B2/) {next}
    ++$ntmproc;
    if(!defined($ioProcFile{$t})){
	printf "I-T Transmart procedure %-50s %s\n", $t, $oProcFile{$t};
	++$onlytmproc;
	push @onlytmproc, $t;
    }
}


# Compare views Oracle I2b2 + Postgres
# ====================================

$nibview = 0;
$onlyibview = 0;
foreach $t (sort(keys(%ioViewFile))) {
    ++$nibview;
    if(!defined($oViewFile{$t})){
	printf "I-T I2b2 view %-50s %s\n", $t, $ioViewFile{$t};
	++$onlyibview;
	push @onlyibview, $t;
    }
    else {
	$ofile = $oViewFile{$t};
	if($ofile eq $ioViewFile{$t}) {$ofile = ""}
	else{$ofile = "   $oViewFile{$t}"}
	if($showSame) {
	    printf "I-T Both   view %-50s %s%s\n", $t, $ioViewFile{$t}, $ofile;
	}
    }
}

$ntmview = 0;
$onlytmview = 0;
foreach $t (sort(keys(%oViewFile))) {
    if($t !~ /^I2B2/) {next}
    ++$ntmview;
    if(!defined($ioViewFile{$t})){
	printf "I-T Transmart view %-50s %s\n", $t, $oViewFile{$t};
	++$onlytmview;
	push @onlytmview, $t;
    }
}




# Compare Primary keys

$nibindexprim = 0;
$onlyibindexprim = 0;
$tmdiffindexprim = 0;
foreach $t (sort(keys(%ioTablePrikey))) {
    ++$nibindexprim;
    if(!defined($oTablePrikey{$t})){
	printf "I-T I2b2 primary index %-50s %s\n", $t, $ioTablePrikey{$t};
	++$onlyibindexprim;
	push @onlyibindexprim, $t;
    }
    else {
	if($ioTablePrikey{$t} eq $oTablePrikey{$t}) {$pindex = "   (same)"}
	else {
	    $pindex .= " $oTablePrikey{$t})";
	    ++$tmdiffindexprim;
	    push @tmdiffindexprim, "$t $oTablePrikey{$t} <=> $pindex";
	}
	if($showSame){
	    printf "I-T Both   primary index %-50s %s%s\n", $t, $oTablePrikey{$t}, $pindex;
	}
    }
}

$ntmindexprim = 0;
$onlytmindexprim = 0;
foreach $t (sort(keys(%oTablePrikey))) {
    if($t !~ /^I2B2/) {next}
    ++$ntmindexprim;
    if(!defined($ioTablePrikey{$t})){
	printf "I-T Transmart primary index %-50s %s\n", $t, $oTablePrikey{$t};
	++$onlytmindexprim;
	push @onlytmindexprim, $t;
    }
}


# Compare Unique keys

$nibindexuni = 0;
$onlyibindexuni = 0;
$tmdiffindexuni = 0;
foreach $t (sort(keys(%ioTableUnikey))) {
    ++$nibindexuni;
    if(!defined($oTableUnikey{$t})){
	printf "I-T I2b2 unique index %-50s %s\n", $t, $ioTableUnikey{$t};
	++$onlyibindexuni;
	push @onlyibindexuni, $t;
    }
    else {
	if($ioTableUnikey{$t} eq $oTableUnikey{$t}) {$pindex = "   (same)"}
	else {
	    $pindex = "($oTableUnikey{$t})";
	    ++$tmdiffindexuni;
	    push @tmdiffindexuni, "$t ($ioTableUnikey{$t}) <=> $pindex";
	}
	if($showSame){
	    printf "I-T Both   unique index %-50s %s%s\n", $t, $ioTableUnikey{$t}, $pindex;
	}
    }
}

$ntmindexuni = 0;
$onlytmindexuni = 0;
foreach $t (sort(keys(%oTableUnikey))) {
    if($t !~ /^I2B2/) {next}
    ++$ntmindexuni;
    if(!defined($ioTableUnikey{$t})){
	printf "I-T Transmart unique index %-50s %s\n", $t, $oTableUnikey{$t};
	++$onlytmindexuni;
	push @onlytmindexuni, $t;
    }
}

# Compare Foreign keys

$nibindexfor = 0;
$onlyibindexfor = 0;
$tmdiffindexfor = 0;
foreach $t (sort(keys(%ioTableForkey))) {
    ++$nibindexfor;
    if(!defined($oTableForkey{$t})){
	printf "I-T I2b2 foreign index %-50s %s\n", $t, $ioTableForkey{$t};
	++$onlyibindexfor;
	push @onlyibindexfor, $t;
    }
    else {
	if(compareForkey($ioTableForkey{$t},$oTableForkey{$t},$t,'oracle')) {$pindex = "   (same)"}
	else{
	    print STDERR "Oracle foreign keys differ: $compareForkey\n";
	    $pindex = "($oTableForkey{$t})";
	    ++$tmdiffindexfor;
	    push @tmdiffindexfor, "$t\n($ioTableForkey{$t}) <=>\n$pindex\n$compareForkey";
	}
	if($showSame){
	    printf "I-T Both   foreign index %-50s %s%s\n", $t, $ioTableForkey{$t}, $pindex;
	}
    }
}

$ntmindexfor = 0;
$onlytmindexfor = 0;
foreach $t (sort(keys(%oTableForkey))) {
    if($t !~ /^I2B2/) {next}
    ++$ntmindexfor;
    if(!defined($ioTableForkey{$t})){
	printf "I-T Transmart foreign index %-50s %s\n", $t, $oTableForkey{$t};
	++$onlytmindexfor;
	push @onlytmindexfor, $t;
    }
}

# Compare Indexes

$nibindex = 0;
$onlyibindex = 0;
$tmdiffindex = 0;
foreach $t (sort(keys(%ioIndex))) {
    ++$noindex;
    if(!defined($oIndex{$t})){
	printf "I-T I2b2   index %-50s %s\n", $t, $ioIndex{$t};
	++$onlyibindex;
	push @onlyibindex, $t;
    }
    else {
	if($ioIndex{$t} eq $oIndex{$t}) {$pindex = "   (same)"}
	else {
	    $pindex = "$oIndex{$t}";
	    ++$tmdiffindex;
	    push @tmdiffindex, "$t $ioIndex{$t} <=> $pindex";
	}
	if($showSame){
	    printf "I-T Both          index %-50s %s%s\n", $t, $ioIndex{$t}, $pindex;
	}
    }
}

$ntmindex = 0;
$onlytmindex = 0;
foreach $t (sort(keys(%oIndex))) {
    if($t !~ /^I2B2/) {next}
    ++$ntmindex;
    if(!defined($ioIndex{$t})){
	printf "I-T Transmart       index %-50s %s\n", $t, $oIndex{$t};
	++$onlytmindex;
	push @onlytmindex, $t;
    }
}

# Check I2b2 Oracle + Postgres + Sqlserver
# Also check against Oracle + Postgres



# Print results

print "\n";
print "------------------------- transmart oracle <=> i2b2 oracle -----------------------------\n";
print "\n";
print "      I2B2 tables: $inotable\n";
print " tranSMART tables: $notable\n";
print "      I2B2 new tables: $onlyibtable\n";
print " tranSMART new tables: $onlytmtable\n";
print "      I2B2-only columns:  $iibnewcol\n"; # from compareOracle
print " tranSMART-only columns:  $itmnewcol\n"; # from compareOracle
print "\n";

print "       I2b2 sequences: $inoseq\n";
print "  tranSMART sequences: $noseq\n";
print "       I2b2 new sequences: $onlyibseq\n";
print "  tranSMART new sequences: $onlytmseq\n";
print "\n";

print "     I2b2 triggers: $inotrig\n";
print "tranSMART triggers: $notrig\n";
print "     I2b2 new triggers: $onlyibtrig\n";
print "tranSMART new triggers: $onlytmtrig\n";
print "\n";

print "     I2b2 functions: $inofunc\n";
print "tranSMART functions: $nofunc\n";
print "     I2b2 new functions: $onlyibfunc\n";
print "tranSMART new functions: $onlytmfunc\n";
print "\n";

print "     I2b2 procedures: $inoproc\n";
print "tranSMART procedures: $noproc\n";
print "     I2b2 new procedures: $onlyibproc\n";
print "tranSMART new procedures: $onlytmproc\n";
print "\n";

print "     I2b2 views: $inoview\n";
print "tranSMART views: $noview\n";
print "     I2b2 new views: $onlyibview\n";
print "tranSMART new views: $onlytmview\n";
print "\n";
print "--------------------------------------------------------------\n";
print "\n";
print "SUMMARY OF DIFFERENCES\n";
print "----------------------\n";
print "\n";

# Summaries of differences
# ========================

# Postgres v Oracle (how consistent is transmart)
# ===============================================

if($onlyptable) {
    print "\nTransmart Postgres <=> Oracle $onlyptable new tables\n";
    $i = 0;
    foreach $t (@onlyptable) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyotable) {
    print "\nTransmart Oracle $onlyotable new tables\n";
    $i = 0;
    foreach $t (@onlyotable) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlypseq) {
    print "\nTransmart Postgres <=> Oracle $onlypseq new sequences\n";
    $i = 0;
    foreach $t (@onlypseq) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyoseq) {
    print "\nTransmart Oracle $onlyoseq new sequences\n";
    $i = 0;
    foreach $t (@onlyoseq) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyptrig) {
    print "\nTransmart Postgres <=> Oracle $onlyptrig new triggers\n";
    $i = 0;
    foreach $t (@onlyptrig) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyotrig) {
    print "\nTransmart Oracle <=> Postgres $onlyotrig new triggers\n";
    $i = 0;
    foreach $t (@onlyotrig) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlypview) {
    print "\nTransmart Postgres <=> Oracle $onlypview new views\n";
    $i = 0;
    foreach $t (@onlypview) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyoview) {
    print "\nTransmart Oracle <=> Postgres $onlyoview new views\n";
    $i = 0;
    foreach $t (@onlyoview) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlypfunc) {
    print "\nTransmart Postgres <=> Oracle $onlypfunc new functions\n";
    $i = 0;
    foreach $t (@onlypfunc) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyofunc) {
    print "\nTransmart Oracle <=> Postgres $onlyofunc new functions\n";
    $i = 0;
    foreach $t (@onlyofunc) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyoproc) {
    print "\nTransmart Oracle <=> Postgres $onlyoproc new procedures\n";
    $i = 0;
    foreach $t (@onlyoproc) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlypindexprim) {
    print "\nTransmart Postgres <=> Oracle $onlypindexprim new primary index\n";
    $i = 0;
    foreach $t (@onlypindexprim) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyoindexprim) {
    print "\nTransmart Oracle <=> Postgres $onlyoindexprim new primary index\n";
    $i = 0;
    foreach $t (@onlyoindexprim) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($diffindexprim) {
    print "\nTransmart Oracle <=> Postgres $diffindexprim changed primary index\n";
    $i = 0;
    foreach $t (@diffindexprim) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlypindexuni) {
    print "\nTransmart Postgres <=> Oracle $onlypindexuni new unique index\n";
    $i = 0;
    foreach $t (@onlypindexuni) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyoindexuni) {
    print "\nTransmart Oracle <=> Postgres $onlyoindexuni new unique index\n";
    $i = 0;
    foreach $t (@onlyoindexuni) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($diffindexuni) {
    print "\nTransmart Oracle <=> Postgres $diffindexuni changed unique index\n";
    $i = 0;
    foreach $t (@diffindexuni) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlypindexfor) {
    print "\nTransmart Postgres <=> Oracle $onlypindexfor new foreign index\n";
    $i = 0;
    foreach $t (@onlypindexfor) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyoindexfor) {
    print "\nTransmart Oracle <=> Postgres $onlyoindexfor new foreign index\n";
    $i = 0;
    foreach $t (@onlyoindexfor) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($diffindexfor) {
    print "\nTransmart Oracle <=> Postgres $diffindexfor changed foreign index\n";
    $i = 0;
    foreach $t (@diffindexfor) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlypindex) {
    print "\nTransmart Postgres <=> Oracle $onlypindex new index\n";
    $i = 0;
    foreach $t (@onlypindex) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyoindex) {
    print "\nTransmart Oracle <=> Postgres $onlyoindex new index\n";
    $i = 0;
    foreach $t (@onlyoindex) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($diffindex) {
    print "\nTransmart Oracle <=> Postgres $diffindex changed index\n";
    $i = 0;
    foreach $t (@diffindex) {
	printf "%3d %s\n", ++$i, $t;
    }
}

# Postgres <=> Oracle I2b2 (i2b2 comparison)
# ============================================

if($ionlyptable) {
    print "\nI2b2 Postgres <=> Oracle $ionlyptable new tables\n";
    $i = 0;
    foreach $t (@ionlyptable) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlyotable) {
    print "\nI2b2 Oracle <=> Postgres $ionlyotable new tables\n";
    $i = 0;
    foreach $t (@ionlyotable) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlypseq) {
    print "\nI2b2 Postgres <=> Oracle $ionlypseq new sequences\n";
    $i = 0;
    foreach $t (@ionlypseq) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlyoseq) {
    print "\nI2b2 Oracle <=> Postgres $ionlyoseq new sequences\n";
    $i = 0;
    foreach $t (@ionlyoseq) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlyptrig) {
    print "\nI2b2 Postgres <=> Oracle $ionlyptrig new triggers\n";
    $i = 0;
    foreach $t (@ionlyptrig) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlyotrig) {
    print "\nI2b2 Oracle <=> Postgres $ionlyotrig new triggers\n";
    $i = 0;
    foreach $t (@ionlyotrig) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlypview) {
    print "\nI2b2 Postgres <=> Oracle $ionlypview new views\n";
    $i = 0;
    foreach $t (@ionlypview) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlyoview) {
    print "\nI2b2 Oracle <=> Postgres $ionlyoview new views\n";
    $i = 0;
    foreach $t (@ionlyoview) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlypfunc) {
    print "\nI2b2 Postgres <=> Oracle $ionlypfunc new functions\n";
    $i = 0;
    foreach $t (@ionlypfunc) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlyofunc) {
    print "\nI2b2 Oracle <=> Postgres $ionlyofunc new functions\n";
    $i = 0;
    foreach $t (@ionlyofunc) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlyoproc) {
    print "\nI2b2 Oracle <=> Postgres $ionlyoproc new procedures\n";
    $i = 0;
    foreach $t (@ionlyoproc) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlypindexprim) {
    print "\nI2b2 Postgres <=> Oracle $ionlypindexprim new primary index\n";
    $i = 0;
    foreach $t (@ionlypindexprim) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlyoindexprim) {
    print "\nI2b2 Oracle <=> Postgres $ionlyoindexprim new primary index\n";
    $i = 0;
    foreach $t (@ionlyoindexprim) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($idiffindexprim) {
    print "\nI2b2 Oracle <=> Postgres $idiffindexprim changed primary index\n";
    $i = 0;
    foreach $t (@idiffindexprim) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlypindexuni) {
    print "\nI2b2 Postgres <=> Oracle $ionlypindexuni new unique index\n";
    $i = 0;
    foreach $t (@ionlypindexuni) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlyoindexuni) {
    print "\nI2b2 Oracle <=> Postgres $ionlyoindexuni new unique index\n";
    $i = 0;
    foreach $t (@ionlyoindexuni) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($idiffindexuni) {
    print "\nI2b2 Oracle <=> Postgres $idiffindexuni changed unique index\n";
    $i = 0;
    foreach $t (@idiffindexuni) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlypindexfor) {
    print "\nI2b2 Postgres <=> Oracle $ionlypindexfor new foreign index\n";
    $i = 0;
    foreach $t (@ionlypindexfor) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlyoindexfor) {
    print "\nI2b2 Oracle <=> Postgres $ionlyoindexfor new foreign index\n";
    $i = 0;
    foreach $t (@ionlyoindexfor) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($idiffindexfor) {
    print "\nI2b2 Oracle <=> Postgres $idiffindexfor changed foreign index\n";
    $i = 0;
    foreach $t (@idiffindexfor) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlypindex) {
    print "\nI2b2 Postgres <=> Oracle $ionlypindex new index\n";
    $i = 0;
    foreach $t (@ionlypindex) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($ionlyoindex) {
    print "\nI2b2 Oracle <=> Postgres $ionlyoindex new index\n";
    $i = 0;
    foreach $t (@ionlyoindex) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($idiffindex) {
    print "\nI2b2 Oracle <=> Postgres $idiffindex changed index\n";
    $i = 0;
    foreach $t (@idiffindex) {
	printf "%3d %s\n", ++$i, $t;
    }
}

# Oracle I2b2 <=> Oracle tranSMART (compare platforms)
# =====================================================

if($onlytmtable) {
    print "\nOracle Transmart <=> I2b2 $onlytmtable new tables\n";
    $i = 0;
    foreach $t (@onlytmtable) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyibtable) {
    print "\nOracle I2b2 <=> Transmart $onlyibtable new tables\n";
    $i = 0;
    foreach $t (@onlyibtable) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlytmseq) {
    print "\nOracle Transmart <=> I2b2 $onlytmseq new sequences\n";
    $i = 0;
    foreach $t (@onlytmseq) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyibseq) {
    print "\nOracle I2b2 <=> Transmart $onlyibseq new sequences\n";
    $i = 0;
    foreach $t (@onlyibseq) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlytmtrig) {
    print "\nOracle Transmart <=> I2b2 $onlytmtrig new triggers\n";
    $i = 0;
    foreach $t (@onlytmtrig) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyibtrig) {
    print "\nOracle I2b2 <=> Transmart $onlyibtrig new triggers\n";
    $i = 0;
    foreach $t (@onlyibtrig) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlytmview) {
    print "\nOracle Transmart <=> I2b2 $onlytmview new views\n";
    $i = 0;
    foreach $t (@onlytmview) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyibview) {
    print "\nOracle I2b2 <=> Transmart $onlyibview new views\n";
    $i = 0;
    foreach $t (@onlyibview) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlytmfunc) {
    print "\nOracle Transmart <=> I2b2 $onlytmfunc new functions\n";
    $i = 0;
    foreach $t (@onlytmfunc) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyibfunc) {
    print "\nOracle I2b2 <=> Transmart $onlyibfunc new functions\n";
    $i = 0;
    foreach $t (@onlyibfunc) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlytmproc) {
    print "\nOracle Transmart <=> I2b2 $onlytmproc new procedures\n";
    $i = 0;
    foreach $t (@onlytmproc) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyibproc) {
    print "\nOracle I2b2 <=> Transmart $onlyibproc new procedures\n";
    $i = 0;
    foreach $t (@onlyibproc) {
	printf "%3d %s\n", ++$i, $t;
    }
}
if($onlyibindexprim) {
    print "\nOracle I2b2 <=> Transmart $onlyibindexprim new primary index\n";
    $i = 0;
    foreach $t (@onlyibindexprim) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlytmindexprim) {
    print "\nOracle Transmart <=> I2b2 $onlytmindexprim new primary index\n";
    $i = 0;
    foreach $t (@onlytmindexprim) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($tmdiffindexprim) {
    print "\nOracle I2b2 <=> Transmart $tmdiffindexprim changed primary index\n";
    $i = 0;
    foreach $t (@tmdiffindexprim) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyibindexuni) {
    print "\nOracle I2b2 <=> Transmart $onlyibindexuni new unique index\n";
    $i = 0;
    foreach $t (@onlyibindexuni) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlytmindexuni) {
    print "\nOracle Transmart <=> I2b2 $onlytmindexuni new unique index\n";
    $i = 0;
    foreach $t (@onlytmindexuni) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($tmdiffindexuni) {
    print "\nOracle I2b2 <=> Transmart $tmdiffindexuni changed unique index\n";
    $i = 0;
    foreach $t (@tmdiffindexuni) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyibindexfor) {
    print "\nOracle I2b2 <=> Transmart $onlyibindexfor new foreign index\n";
    $i = 0;
    foreach $t (@onlyibindexfor) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlytmindexfor) {
    print "\nOracle Transmart <=> I2b2 $onlytmindexfor new foreign index\n";
    $i = 0;
    foreach $t (@onlytmindexfor) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($tmdiffindexfor) {
    print "\nOracle I2b2 <=> Transmart $tmdiffindexfor changed foreign index\n";
    $i = 0;
    foreach $t (@tmdiffindexfor) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlyibindex) {
    print "\nOracle I2b2 <=> Transmart $onlyibindex new index\n";
    $i = 0;
    foreach $t (@onlyibindex) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($onlytmindex) {
    print "\nOracle Transmart <=> I2b2 $onlytmindex new index\n";
    $i = 0;
    foreach $t (@onlytmindex) {
	printf "%3d %s\n", ++$i, $t;
    }
}

if($tmdiffindex) {
    print "\nOracle I2b2 <=> Transmart $tmdiffindex changed index\n";
    $i = 0;
    foreach $t (@tmdiffindex) {
	printf "%3d %s\n", ++$i, $t;
    }
}

