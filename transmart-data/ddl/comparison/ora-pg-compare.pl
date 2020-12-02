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
%ioTableKey = ();
%ipTableKey = ();
%isTableKey = ();
%oTableKey = ();
%pTableKey = ();


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
		elsif($f eq "_misc.sql"){
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
		if(/(\S+)\s+(TABLE|table)\s+([^.]+)[.](\S+)/) {
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
			if(/CONSTRAINT (\S+ )FOREIGN KEY (\([^\)]+\))/){
			    $pc = $1;
			    $pk = $2;
			    $pc =~ s/\"//g;
			    if(length($pc) > 31){print STDERR "Oracle constraint length ".length($pc)." '$pc'\n"}
			    $pfk = uc($pc).uc($pk);
			    $pfk =~ s/\"//g;
			    $oTableForkey{"$schema.$table"} .= $pfk;
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
			    $oTableKeycon{"$schema.$table"} = $pkc;
			}
			$oTableKey{"$schema.$table"} = $pk;
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
		    if(/^\s*(CONSTRAINT (\S+\s+))?FOREIGN KEY (\([^\)]+\))/){
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
	    }
	    close IN;
	}
	elsif($f eq "items.json"){
#	    print "Oracle parse json $d/$f\n";
	    open(IN,"$dir$d/$f") || die "Failed to read $d/$f";
	    while(<IN>) {
		if(/^\s+\"file\" : \"(\S+)\"/){
		    $ofile = $1;
		    if($ofile !~ /\/_cross[.]sql$/){
			$orload{"$ofile"}++;
		    }
		}
	    }
	    close IN;
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
		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s*\(([^\)]*)\)\s*RETURNS (.*)/) {
		    $func = $1;
		    $ret = $2;
		    $func =~ s/\(\)$//g;
		    ($schema) = ($d =~ /\/([^\/]+)\/functions$/);
		    $func = uc($func);
		    $schema = uc($schema);
		    if($ret ne "trigger"){
			$pFunctionFile{"$schema.$func"} = "$d/$f";
			$pFunctionReturn{"$schema.$func"} = "$ret";
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
			$pFunctionFile{"$schema.$func"} = "$d/$f";
			$pFunctionReturn{"$schema.$func"} = "$ret";
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
		    if(/^\s*ADD CONSTRAINT (\S+) PRIMARY KEY \(([^\)]+)\)/) {
			$pkc = uc($1);
			$pk = uc($2);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			$pTableKey{$altertable} = $pk;
			if(defined($pkc)){$pTableKeycon{"$schema.$table"} = $pkc}
		    }
		    if(/^\s*ADD CONSTRAINT (\S+) UNIQUE \(([^\)]+)\)/) {
			$pkc = uc($1);
			$pk = uc($2);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){$pTableUnikey{$altertable} .= "$pkc $pk;"}
			else {$pTableUnikey{$altertable} .= ". $pk;"}
		    }
		    if(/^\s*ADD CONSTRAINT (\S+ )FOREIGN KEY (\(\S+\) )REFERENCES ([^\(]+\([^\)]+\))/){
			$pk = uc($1).uc($2);
			$pk .= uc($schema);
			$pk .= ".";
			$pk .= uc($3);
			$pk .= ";";
			$pTableForkey{"$schema.$table"} .= $pk;
		    }
		    if(/;/) {$alterctable = 0}
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
			if($cdef =~ / DEFAULT nextval\(\'([^\']+)\'::regclass\) NOT NULL$/){
			    $cid = $1;
			    $cid = uc($1);
			    $pNextval{"$schema.$table"} = "$col.$cid";
			}
			elsif($cdef =~ /DEFAULT nextval/){print STDERR "$d/$f DEFAULT nextval not recognized: '$cdef'\n"}
		    }
		    if(/^\s*(CONSTRAINT (\S+)\s+)?PRIMARY KEY \(([^\)]+)\)/){
			$pkc = $2;
			$pk = uc($3);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			$pTableKey{"$schema.$table"} = $pk;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $pTableKeycon{"$schema.$table"} = $pkc;
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
			    $pTableUnikey{"$schema.$table"} .= "$pkc $pk;";
			}
			else {$pTableUnikey{"$schema.$table"} .= ". $pk;"}
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
		if(/^\s*CREATE\s+FUNCTION\s+(\S+)\s*\(([^\)]*)\)\s*RETURNS (.*)/) {
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
    if($ot eq $pt) {return 0}

#    if($pt =~ /DEFAULT NEXTVAL\S+/ && $ot =~ /\/\* POSTGRES NEXTVAL NEEDS TRIGGER \*\//){
#	$pt =~ s/DEFAULT NEXTVAL\S+ //g;
#	$ot =~ s/\/\*[^*]+\*\/ //g;
#    }

    $ot =~ s/  +/ /g;
    $pt =~ s/  +/ /g;

# clean up matching NULL and NOT NULL with optional ENABLE
   
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

    if($ot =~ /DATE NULL$/ && $pt =~ /TIMESTAMP NULL$/) {
	$ot =~ s/ NULL$/\)/;
	$pt =~ s/ NULL$/\)/;
    }

    if($ot =~ / NULL$/ && $ot !~ / NOT NULL$/ && $pt !~ / NULL/) {
	$ot =~ s/ NULL$//;
    }

    $ot =~ s/ WITH LOCAL TIME ZONE//g; # only allows local time display, storage unchanged

    if($pt =~ /^BIGINT/) {
	if($ot =~ /^NUMBER\((\d+),0\)/){
	    if($1 >= 9 && $1 <= 18) { # FIX size of bigint
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+/matched/;
	    }
	}
	elsif($ot =~ /^NUMBER/) {
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^BIGSERIAL/) {	# used for unique identifiers in i2b2 postgres
	if($ot =~ /^NUMBER\((\d+),0\)/){
	    if($1 >= 9 && $1 <= 18) { # fix: size of bigserial
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+/matched/;
	    }
	}
	elsif($ot =~ /^NUMBER/) {
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
	    if($1 >= 5 && $1 <= 18) { # fix: size of INT
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+/matched/;
	    }
	}
	elsif($ot =~ /^NUMBER/) {
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^SERIAL/) {	# used for unique identifiers in i2b2 postgres
	if($ot =~ /^NUMBER\((\d+),0\)/){ # fix: size of serial
	    if($1 >= 5 && $1 <= 8) {
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+/matched/;
	    }
	}
	elsif($ot =~ /^NUMBER/) {
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	else {return 1}
    }

    elsif($pt =~ /^SMALLINT/) {
	if($ot =~ /^NUMBER\((\d+),0\)/){
	    if($1 >= 1 && $1 <= 4) { # fix: size of smallint
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+/matched/;
	    }
	}
	elsif($ot =~ /^NUMBER/) {
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

    elsif($pt =~ /^DOUBLE PRECISION/) {
	if($ot =~ /^NUMBER\((\d+),(\d+)\)/){
	    if($1 >= 9 && $1 <= 38 && $2 > 0) {
		$ot =~ s/^\S+/matched/;
		$pt =~ s/^\S+ \S+/matched/;
	    }
	}
	elsif($ot =~ /^NUMBER/){
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

    elsif($pt =~ /^VARCHAR\((\d+)\)/) {
	$size = $1;
	if($ot =~ /N?VARCHAR2\($size BYTE\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	if($ot =~ /N?VARCHAR2\($size CHAR\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+/matched/;
	}
	if($ot =~ /N?VARCHAR2\($size\)/){
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

    elsif($pt =~ /^VARCHAR2\((\d+) BYTE\)/) {
	$size = $1;
	if($ot =~ /N?VARCHAR2\($size BYTE\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /N?VARCHAR2\($size CHAR\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /N?VARCHAR2\($size\)/){
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

    elsif($pt =~ /^VARCHAR \((\d+)\)/) {
	$size = $1;
	if($ot =~ /N?VARCHAR2\($size BYTE\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /N?VARCHAR2\($size CHAR\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /N?VARCHAR2\($size\)/){
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
	if($ot =~ /N?VARCHAR2?\($size BYTE\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /N?VARCHAR2\($size CHAR\)/){
	    $ot =~ s/^\S+ \S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /N?VARCHAR2\($size\)/){
	    $ot =~ s/^\S+/matched/;
	    $pt =~ s/^\S+ \S+/matched/;
	}
	if($ot =~ /CLOB/ && $size >= 2000){
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
	if($pt =~ / DEFAULT NOW\(\)$/ && $ot =~ / DEFAULT SYSDATE$/) {
	    $pt =~ s/ \S+ \S+$//;
	    $ot =~ s/ \S+ \S+$//;
	}
	if($pt =~ / DEFAULT CURRENT TIMESTAMP$/ && $ot =~ / DEFAULT SYSDATE$/) {
	    $pt =~ s/ \S+ \S+ \S+$//;
	    $ot =~ s/ \S+ \S+$//;
	}
    }
    elsif($pt =~ /^TIMESTAMP (\(6\))/){
	if($ot =~ /^DATE$/) {
	    $pt =~ s/\S+ \S+$//;
	    $ot =~ s/\S+$//;
	}
    }
    elsif($pt =~ /^TIMESTAMP(\(\d\))/){
	if($ot =~ /^TIMESTAMP \(\d\)/) {
	    $ot =~ s/^(\S+) (\S+)/$1$2/;
	}
    }
    elsif($pt =~ /^TIMESTAMP$/){
	if($ot =~ /^DATE$/) {
	    $pt =~ s/\S+$//;
	    $ot =~ s/\S+$//;
	}
	if($ot =~ /^TIMESTAMP \(6\)$/) {
	    $pt =~ s/\S+$//;
	    $ot =~ s/\S+ \S+$//;
	}
	if($ot =~ /^TIMESTAMP \(9\)$/) {
	    $pt =~ s/\S+$//;
	    $ot =~ s/\S+ \S+$//;
	}
	if($pt =~ / DEFAULT NOW\(\)$/ && $ot =~ / DEFAULT SYSDATE$/) {
	    $pt =~ s/ \S+ \S+$//;
	    $ot =~ s/ \S+ \S+$//;
	}
    }
    elsif($pt =~ /^DATE\b/){
	if($ot =~ /^DATE\b/) {
	    $ot =~ s/^(\S+)/matched/;
	    $pt =~ s/^(\S+)/matched/;
	}
	if($pt =~ / DEFAULT NOW\(\)$/ && $ot =~ / DEFAULT SYSDATE$/) {
	    $pt =~ s/ \S+ \S+$//;
	    $ot =~ s/ \S+ \S+$//;
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

    $otrigger = "";

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

    return 1;
}

sub compareColumns($$){
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

    my $okey = "undefined";
    my $pkey = "undefined";
    my @okey = ();
    my @pkey = ();

    if(defined($oTableKey{$t})){$okey = $oTableKey{$t}}
    if(defined($pTableKey{$t})){$pkey = $pTableKey{$t}}
    if($okey ne $pkey) {
	$compstr .= "PRIMARY KEY     $okey    $pkey\n";
	if($okey eq "undefined") {
	    if(!defined($pTableKeycon{$t})) {
		$compstr .= "PRIMARY KEY (\"$pTableKey{$t}\")\n";
	    }else{
		$compstr .= "CONSTRAINT \"$pTableKeycon{$t}\" PRIMARY KEY (\"$pTableKey{$t}\")\n";
	    }
	}
	if($pkey eq "undefined") {
	    $lk = lc($oTableKey{$t});
	    if(!defined($oTableKeycon{$t})) {
		$compstr .= "PRIMARY KEY (lk)\n";
	    }else{
		($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
		$lc = lc($oTableKeycon{$t});
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
	    $compstr = "UNIQUE      count $okey    $pkey\n";
	    for ($i = 0; $i < $okey; $i++) {
		$compstr .= "            oracle: $okey[$i]\n";
	    }
	    for ($i = 0; $i < $pkey; $i++) {
		$compstr .= "          postgres: $pkey[$i]\n";
	    }
	}
    }
    elsif($#okey >= 0) {
	$compstr = "UNIQUE oracle only $okey\n";
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
	$compstr = "UNIQUE postgres only $pkey\n";
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
	    $compstr = "FOREIGN KEY count $okey    $pkey\n";
	    for ($i = 0; $i < $okey; $i++) {
		$compstr .= "            oracle: $okey[$i]\n";
	    }
	    for ($i = 0; $i < $pkey; $i++) {
		$compstr .= "          postgres: $pkey[$i]\n";
	    }
	}
    }
    elsif($#okey >= 0) {
	$compstr = "FOREIGN KEY oracle only $okey\n";
	    for ($i = 0; $i < $okey; $i++) {
		$pk = lc($okey[$i]);
		$compstr .= "            oracle: $pk\n";
	    }
    }
    elsif($#pkey >= 0) {
	$compstr = "FOREIGN KEY postgres only $pkey\n";
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
	    $ionewcol++;
	    $compstr .= sprintf "$head"."column not in OracleI2b2:  %-32s %s\n", $c, $pcol{$c};
	    $head="";
	}
    }

    my $okey = "undefined";
    my $pkey = "undefined";
    my @okey = ();
    my @pkey = ();

    if(defined($ioTableKey{$t})){$okey = $ioTableKey{$t}}
    if(defined($ipTableKey{$t})){$pkey = $ipTableKey{$t}}
    if($okey ne $pkey) {
	$compstr .= "PRIMARY KEY     $okey    $pkey\n";
	if($okey eq "undefined") {
	    if(!defined($ipTableKeycon{$t})) {
		$compstr .= "PRIMARY KEY (\"$pTableKey{$t}\")\n";
	    }else{
		$compstr .= "CONSTRAINT \"$pTableKeycon{$t}\" PRIMARY KEY (\"$pTableKey{$t}\")\n";
	    }
	}
	if($pkey eq "undefined") {
	    $lk = lc($ioTableKey{$t});
	    if(!defined($ioTableKeycon{$t})) {
		$compstr .= "PRIMARY KEY (lk)\n";
	    }else{
		($ts,$tt) = ($t =~ /([^.]+)[.](.*)/);
		$lc = lc($ioTableKeycon{$t});
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
		    $compstr .= "UNIQUE      oracle: $okey[$i]\n";
		    $compstr .= "          postgres: $pkey[$i]\n";
		}
	    }
	}
	else {
	    $compstr = "UNIQUE      count $okey    $pkey\n";
	    for ($i = 0; $i < $okey; $i++) {
		$compstr .= "            oracle: $okey[$i]\n";
	    }
	    for ($i = 0; $i < $pkey; $i++) {
		$compstr .= "          postgres: $pkey[$i]\n";
	    }
	}
    }
    elsif($#okey >= 0) {
	$compstr = "UNIQUE oracle i2b2 only $okey\n";
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
	$compstr = "UNIQUE postgres i2b2 only $pkey\n";
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
	    $compstr = "FOREIGN KEY count $okey    $pkey\n";
	    for ($i = 0; $i < $okey; $i++) {
		$compstr .= "            oracle: $okey[$i]\n";
	    }
	    for ($i = 0; $i < $pkey; $i++) {
		$compstr .= "          postgres: $pkey[$i]\n";
	    }
	}
    }
    elsif($#okey >= 0) {
	$compstr = "FOREIGN KEY oracle i2b2 only $okey\n";
	    for ($i = 0; $i < $okey; $i++) {
		$pk = lc($okey[$i]);
		$compstr .= "            oracle: $pk\n";
	    }
    }
    elsif($#pkey >= 0) {
	$compstr = "FOREIGN KEY postgres i2b2 only $pkey\n";
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

    $otxt =~ s/\s\s+/ /g;
    $ptxt =~ s/\s\s+/ /g;

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
	    if($f eq "oracle") {
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
	    if($f eq "oracle") {
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
		if(/^\s*(.*\S)\s+(PROCEDURE|procedure)\s+(\S+)/) {
		    $puse = $1;
		    $schema = $ischema;
		    $proc = $3;
		    $puse = uc($puse);
		    $schema = uc($schema);
		    $proc = uc($proc);
		    $schema =~ s/\"//g;
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
		    if(/^\s+REFERENCES \"([^\"]+)\"[.]\"([^\"]+)\" \(\"([^\"]+)\"\) (ON DELETE CASCADE )?(EN|DIS)ABLE;/) {
			$pk = " ";
			$pk .= uc($1);
			$pk .= ".";
			$pk .= uc($2);
			$pk .= "(";
			$pk .= uc($3);
			$pk .= ");";
			$ioTableForkey{"$schema.$table"} .= $pk;
		    }
		    else {
			print STDERR "$d/$f $line I2b2 Unexpected foreign key format $d/$f: $_";
		    }
		    $forkey = 0;
		}
		if(/(\S+)\s+(TABLE|table)\s+(\S+)/) {
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
			if(/CONSTRAINT (\S+ )FOREIGN KEY (\([^\)]+\))/){
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
		    if(/^\s*(\S+)\s+(.*?),?$/) {
			$col = $1;
			$cdef = $2;
			$cdef =~ s/,\s+$//g;
			if($cdef =~ / PRIMARY KEY/g) {
			    $cdef =~ s/ PRIMARY KEY//g;
			    $ioTableKeycon{"$schema.$table"} = $col;
			}
			$ioTableColumn{"$schema.$table"} .= "$col $cdef;";
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
			    $ioTableKeycon{"$schema.$table"} = $pkc;
			}
			$ioTableKey{"$schema.$table"} = $pk;
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
			    $ioTableUnikey{"$schema.$table"} .= "$pkc $pk;";
			}
			else {$ioTableUnikey{"$schema.$table"} .= ". $pk;"}
		    }
		    if(/^\s*(CONSTRAINT (\S+\s+))?FOREIGN KEY (\([^\)]+\))/){
			if(defined($1)) {$pk = uc($2).uc($3)}
			else{$pk = "unnamed ".uc($3)}
			$pk =~ s/\"//g;
			$ioTableForkey{"$schema.$table"} .= $pk;
		    }
		}

		if($cseq == 1 && /([^;]*)(;?)/) {
		    $tseq .= $1;
		    if(defined($2)) {$cseq = 2}
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
	    if($f eq "postgresql") {
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
		    if(/^\s*ADD CONSTRAINT (\S+) PRIMARY KEY \(([^\)]+)\)/) {
			$pkc = uc($1);
			$pk = uc($2);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			$ipTableKey{$altertable} = $pk;
			if(defined($pkc)){$ipTableKeycon{"$schema.$table"} = $pkc}
		    }
		    if(/^\s*ADD CONSTRAINT (\S+) UNIQUE \(([^\)]+)\)/) {
			$pkc = uc($1);
			$pk = uc($2);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			if(defined($pkc)){$ipTableUnikey{$altertable} .= "$pkc $pk;"}
			else {$ipTableUnikey{$altertable} .= ". $pk;"}
		    }
		    if(/^\s*ADD CONSTRAINT (\S+ )FOREIGN KEY (\(\S+\) )REFERENCES ([^\(]+\([^\)]+\))/){
			$pk = uc($1).uc($2);
			$pk .= uc($schema);
			$pk .= ".";
			$pk .= uc($3);
			$pk .= ";";
			$ipTableForkey{"$schema.$table"} .= $pk;
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
		    if(/^\s*([A-Z]\S+)\s+(.*?),?$/) {
			$col = $1;
			$cdef = $2;
			$col = uc($col);
			$cdef =~ s/,\s+$//g;
			if($cdef =~ / PRIMARY KEY/g) {
			    $cdef =~ s/ PRIMARY KEY//g;
			    $ipTableKeycon{"$schema.$table"} = $col;
			}
			$ipTableColumn{"$schema.$table"} .= "$col $cdef;";
			if($cdef =~ / DEFAULT nextval\(\'([^\']+)\'::regclass\) NOT NULL$/){
			    $cid = $1;
			    $cid = uc($1);
			    $ipNextval{"$schema.$table"} = "$col.$cid";
			}
			elsif($cdef =~ /DEFAULT nextval/){print STDERR "$d/$f DEFAULT nextval not recognized: '$cdef'\n"}
		    }
		    if(/^\s*(CONSTRAINT (\S+)\s+)?PRIMARY KEY \(([^\)]+)\)/){
			$pkc = $2;
			$pk = uc($3);
			$pk =~ s/\s//g;
			$pk =~ s/\"//g;
			$ipTableKey{"$schema.$table"} = $pk;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $ipTableKeycon{"$schema.$table"} = $pkc;
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
			    $ipTableUnikey{"$schema.$table"} .= "$pkc $pk;";
			}
			else {$ipTableUnikey{"$schema.$table"} .= ". $pk;"}
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
			$isTableKey{$altertable} = $pk;
			if(defined($pkc)){$isTableKeycon{"$schema.$table"} = $pkc}
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
			$isTableKey{"$schema.$table"} = $pk;
			if(defined($pkc)){
			    $pkc = uc($pkc);
			    $pkc =~ s/\s//g;
			    $pkc =~ s/\"//g;
			    $isTableKeycon{"$schema.$table"} = $pkc;
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
	if($pfile eq $oTableFile{$t}) {$pfile = ""}
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
    }
    else {
	$compstr = compareSequence($oSequenceText{$t},$pSequenceText{$t});
	$pfile = $pSequenceFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $oSequenceFile{$t}) {$pfile = ""}
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
	}
    }
    else {
	$pfile = $pTriggerFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $oTriggerFile{$t}) {$pfile = ""}
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
    }
    else {
	$pfile = $pFunctionFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $oFunctionFile{$t}) {$pfile = ""}
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
    }
}

$noproc = 0;
$onlyoproc = 0;
foreach $t (sort(keys(%oProcFile))) {
    ++$noproc;
    if(!defined($pFunctionFile{$t})){
	printf "Oracle procedure %-50s %s\n", $t, $oProcFile{$t};
	++$onlyoproc;
    }
    else {
	$pfile = $pFunctionFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $oProcFile{$t}) {$pfile = ""}
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
    }
    else {
	$pfile = $pViewFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $oViewFile{$t}) {$pfile = ""}
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
    }
}


# Check I2b2 Oracle + Postgres + Sqlserver
# Also check against Oracle + Postgres



# Print results

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
	print STDERR "I2b2Oracle $io unknown for target $iotarget{$io}\n";
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
	print STDERR "I2b2Postgres $ip unknown for target $iptarget{$ip}\n";
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
	print STDERR "I2b2Sqlserver $is unknown for target $istarget{$is}\n";
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
	if($pfile eq $ioTableFile{$t}) {$pfile = ""}
	else{$pfile = "   $ipTableFile{$t}"}
	printf "I2b2Both %3s %3d %3d %-50s %s%s\n",
		  $diff, $npcol, $nocol, $t, $ioTableFile{$t}, $pfile;
	print $compstr;
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
    }
    else {
	$compstr = compareSequence($ioSequenceText{$t},$ipSequenceText{$t});
	$pfile = $ipSequenceFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $ioSequenceFile{$t}) {$pfile = ""}
	else{$pfile = "   $ipSequenceFile{$t}"}
	if($compstr eq "") {$diff = "   "}
	else {$diff = "CMP"}
	printf "I2b2Both %s sequence %-50s %s%s\n", $diff, $t, $ioSequenceFile{$t}, $pfile;
	print $compstr;
    }
}

$inpseq = 0;
$ionlypseq = 0;
foreach $t (sort(keys(%ipSequenceFile))) {
    ++$npseq;
    if(!defined($ioSequenceFile{$t})){
	printf "I2b2Postgres sequence %-50s %s\n", $t, $ipSequenceFile{$t};
	++$ionlypseq;
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
	}
    }
    else {
	$pfile = $ipTriggerFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $ioTriggerFile{$t}) {$pfile = ""}
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
    ++$nptrig;
    if(!defined($ioTriggerFile{$t})){
	printf "I2b2Postgres trigger %-50s %s\n", $t, $ipTriggerFile{$t};
	++$ionlyptrig;
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
    }
    else {
	$pfile = $ipFunctionFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $ioFunctionFile{$t}) {$pfile = ""}
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
    }
}


$inoproc = 0;
$ionlyoproc = 0;
foreach $t (sort(keys(%ioProcFile))) {
    ++$inoproc;
    if(!defined($ipFunctionFile{$t})){
	printf "I2b2Oracle procedure %-50s %s\n", $t, $ioProcFile{$t};
	++$ionlyoproc;
    }
    else {
	$pfile = $ipFunctionFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $ioProcFile{$t}) {$pfile = ""}
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
    }
    else {
	$pfile = $ipViewFile{$t};
	$pfile =~ s/\/postgres\//\/oracle\//g;
	if($pfile eq $ioViewFile{$t}) {$pfile = ""}
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
    }
}


# Check I2b2 Oracle + Postgres + Sqlserver
# Also check against Oracle + Postgres



# Print results

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



# =====================================
# Compare Oracle i2b2 with transmart
# =====================================

# I2b2 Compare tables I2b2 + Oracle
# =====================================

$initable = 0;
$ionlyitable = 0;
foreach $t (sort(keys(%ioTableFile))) {
    if(defined($iskip{$ioTableFile{$t}})){next}
    ++$initable;
    @icols = split(/;/,$ioTableColumn{$t});
    $nicol = 1 + $#icols;
    if(!defined($oTableFile{$t})){
	printf "I-O I2b2 table %3d %-50s %s\n", $nicol, $t, $ioTableFile{$t};
	++$ionlyitable;
    }
    else {
	$compstr = compareI2b2Columns($ioTableColumn{$t},$oTableColumn{$t});
	@ocols = split(/;/,$oTableColumn{$t});
	$nocol = 1 + $#ocols;
	if($nicol != $nocol) {$diff = "MOD"}
	elsif($compstr ne ""){$diff = "CMP"}
	else {$diff = "   "}
	$ofile = $oTableFile{$t};
	if($ofile eq $ioTableFile{$t}) {$ofile = ""}
	else{$ofile = "   $oTableFile{$t}"}
	printf "I-O Both %3s %3d %3d %-50s %s%s\n",
		  $diff, $nocol, $nicol, $t, $ioTableFile{$t}, $ofile;
	print $compstr;
    }
}

$inotable = 0;
$ionlyotable = 0;
foreach $t (sort(keys(%oTableFile))) {
    if(defined($iskip{$oTableFile{$t}})){next}
    if($t !~ /^I2B2[.]/) {next}
    ++$inotable;
    if(!defined($ioTableFile{$t})){
	printf "I-O Oracle table %-50s %s\n", $t, $oTableFile{$t};
	++$ionlyotable;
    }
}

# I2b2 Compare sequences I2B2 + Oracle
# ========================================

$iniseq = 0;
$ionlyiseq = 0;
foreach $t (sort(keys(%ioSequenceFile))) {
    ++$iniseq;
    if(!defined($oSequenceFile{$t})){
	printf "I-O I2b2 sequence %-50s %s\n", $t, $ioSequenceFile{$t};
	++$ionlyiseq;
    }
    else {
	$compstr = compareSequence($ioSequenceText{$t},$oSequenceText{$t});
	$ofile = $oSequenceFile{$t};
	if($ofile eq $ioSequenceFile{$t}) {$ofile = ""}
	else{$ofile = "   $oSequenceFile{$t}"}
	if($compstr eq "") {$diff = "   "}
	else {$diff = "CMP"}
	printf "I-O Both %s sequence %-50s %s%s\n", $diff, $t, $ioSequenceFile{$t}, $ofile;
	print $compstr;
    }
}

$inoseq = 0;
$ionlyo = 0;
foreach $t (sort(keys(%oSequenceFile))) {
    if($t !~ /^I2B2[.]/) {next}
    ++$noseq;
    if(!defined($ioSequenceFile{$t})){
	printf "I-O Oracle sequence %-50s %s\n", $t, $oSequenceFile{$t};
	++$ionlyoseq;
    }
}

# I2b2 Compare triggers Oracle + Postgres
# =======================================

$initrig = 0;
$ionlyitrig = 0;
foreach $t (sort(keys(%ioTriggerFile))) {
    if(defined($ioSkipTrigger{$t})){next}
    ++$initrig;
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
	    printf "I2b2Oracle trigger %-50s %s\n", $t, $ioTriggerFile{$t};
	    ++$ionlyotrig;
	}
    }
    else {
	$ofile = $oTriggerFile{$t};
	if($ofile eq $ioTriggerFile{$t}) {$ofile = ""}
	else{$ofile = "   $oTriggerFile{$t}"}
	printf "I-O Both   trigger %-50s %s%s\n", $t, $ioTriggerFile{$t}, $pfile;
#	$tfname = $t;
#	$tfname =~ s/[.]/.TF_/g;
#	if(!defined($oFunctionFile{"$tfname"})){
#	    print STDERR "I2b2Trigger $t has no function $tfname in $oTriggerFile{$t}\n";
#	}
    }
}

$inotrig = 0;
$ionlyotrig = 0;
foreach $t (sort(keys(%oTriggerFile))) {
    if($t !~ /^I2B2[.]/){next}
    ++$inotrig;
    if(!defined($ioTriggerFile{$t})){
	printf "I-O Oracle trigger %-50s %s\n", $t, $oTriggerFile{$t};
	++$ionlyotrig;
#	$tfname = $t;
#	$tfname =~ s/[.]/.TF_/g;
#	if(!defined($oFunctionFile{"$tfname"})){
#	    print STDERR "I-O I2b2Trigger $t has no function $tfname in $oTriggerFile{$t}\n";
#	}
    }
}

# Compare I2b2 functions + procedures Oracle + Postgres
# =====================================================

$inifunc = 0;
$ionlyifunc = 0;
foreach $t (sort(keys(%ioFunctionFile))) {
    ++$inifunc;
    if(!defined($oFunctionFile{$t})){
	printf "I-O I2b2 function %-50s %s\n", $t, $ioFunctionFile{$t};
	++$ionlyifunc;
    }
    else {
	$ofile = $oFunctionFile{$t};
	if($ofile eq $ioFunctionFile{$t}) {$ofile = ""}
	else{$ofile = "   $oFunctionFile{$t}"}
	printf "I-O Both   function %-50s %s%s\n", $t, $ioFunctionFile{$t}, $ofile;
    }
}

$inofunc = 0;
$ionlyofunc = 0;
foreach $t (sort(keys(%oFunctionFile))) {
    if($t !~ /^I2B2[.]/) {next}
    ++$inofunc;
    if(!defined($ioFunctionFile{$t})){
	printf "I-O Oracle function %-50s %s\n", $t, $oFunctionFile{$t};
	++$ionlyofunc;
    }
}


$iniproc = 0;
$ionlyiproc = 0;
foreach $t (sort(keys(%ioProcFile))) {
    ++$iniproc;
    if(!defined($oFunctionFile{$t})){
	printf "I2b2Oracle procedure %-50s %s\n", $t, $ioProcFile{$t};
	++$ionlyoproc;
    }
    else {
	$ofile = $oFunctionFile{$t};
	if($ofile eq $ioProcFile{$t}) {$ofile = ""}
	else{$ofile = "   $oFunctionFile{$t}"}
	printf "I2b2Both   procedure %-50s %s%s\n", $t, $ioProcFile{$t}, $ofile;
    }
}

$inoproc = 0;
$ionlyoproc = 0;
foreach $t (sort(keys(%oProcFile))) {
    if($t !~ /^I2B2[.]/) {next}
    ++$inoproc;
    if(!defined($ioProcFile{$t})){
	printf "I-O Oracle procedure %-50s %s\n", $t, $oProcFile{$t};
	++$ionlyoproc;
    }
}


# Compare I2b2 views Oracle + Postgres
# ====================================

$iniview = 0;
$ionlyiview = 0;
foreach $t (sort(keys(%ioViewFile))) {
    ++$iniview;
    if(!defined($oViewFile{$t})){
	printf "I-O I2b2 view %-50s %s\n", $t, $ioViewFile{$t};
	++$ionlyiview;
    }
    else {
	$ofile = $oViewFile{$t};
	if($ofile eq $ioViewFile{$t}) {$ofile = ""}
	else{$ofile = "   $oViewFile{$t}"}
	printf "I-O Both   view %-50s %s%s\n", $t, $ioViewFile{$t}, $ofile;
    }
}

$inoview = 0;
$ionlyoview = 0;
foreach $t (sort(keys(%oViewFile))) {
    if($t !~ /^I2B2[.]/) {next}
    ++$inoview;
    if(!defined($ioViewFile{$t})){
	printf "I-O Oracle view %-50s %s\n", $t, $oViewFile{$t};
	++$ionlyoview;
    }
}


# Check I2b2 Oracle + Postgres + Sqlserver
# Also check against Oracle + Postgres



# Print results

print "\n";
print "    I2b2 tables: $initable\n";
print "  Oracle tables: $inotable\n";
print "    I2b2 new tables: $ionlyitable\n";
print "  Oracle new tables: $ionlyotable\n";
print "    I2b2-only columns:  $ionewcol\n"; # from compareI2b2
print "  Oracle-only columns:  $ipnewcol\n"; # from compareI2b2
print "\n";

print "    I2b2 sequences: $iniseq\n";
print "  Oracle sequences: $inoseq\n";
print "    I2b2 new sequences: $ionlyiseq\n";
print "  Oracle new sequences: $ionlyoseq\n";
print "\n";

print "  I2b2 triggers: $initrig\n";
print "Oracle triggers: $inotrig\n";
print "  I2b2 new triggers: $ionlyitrig\n";
print "Oracle new triggers: $ionlyotrig\n";
print "\n";

print "  I2b2 functions: $inifunc\n";
print "Oracle functions: $inofunc\n";
print "  I2b2 new functions: $ionlyifunc\n";
print "Oracle new functions: $ionlyofunc\n";
print "\n";

print "  I2b2 procedures: $iniproc\n";
print "  Oracle procedures: $inoproc\n";
print "  I2b2 new procedures: $ionlyiproc\n";
print "  Oracle new procedures: $ionlyoproc\n";
print "\n";

print "  I2b2 views: $iniview\n";
print "Oracle views: $inoview\n";
print "  I2b2 new views: $ionlyiview\n";
print "Oracle new views: $ionlyoview\n";
print "\n";



