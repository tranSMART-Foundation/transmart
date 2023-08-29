#!/usr/bin/perl -w

$doupdate = 0;
foreach $arg (@ARGV) {
    if($arg eq "-update") {$doupdate = 1}
    else {die "Unknown option $arg"}
}

$topddl = "../../ddl/postgres";

opendir(SCHEMA, "$topddl") || die "Cannot open $topddl";
@schema = sort(readdir(SCHEMA));
closedir SCHEMA;

foreach $s (@schema) {
    if ($s =~ /^[.]/) {next}
    $sdir = "$topddl/$s";
    if(! -d "$sdir") {next}
    opendir(DDL, "$sdir") || die "Cannot read $sdir";
    print "Processing schema $s\n";
    @tables = sort(readdir(DDL));
    foreach $t (@tables) {
	if ($t =~ /(\S+)[.]sql/) {
	    $table = lc($1);
	    open(SQL, "$sdir/$t") || die "Cannot open $sdir/$t";
	    while(<SQL>){
		if(/^\s+select nextval[\(]'([^.]+).([A-Za-z0-9_]+)'[\)] into new.(\S+)\s*;/) {
		    $seqSchema = lc($1);
		    $seqName = lc($2);
		    $id = lc($3);
		    $psqlstr = "psql -A -t -c \"select greatest(start_value,last_value) from pg_sequences where schemaname = '$seqSchema' and sequencename = '$seqName'\"";
		    open(PSQL, "$psqlstr|") || die "Failed to start psql";
		    $pout = <PSQL>;
		    chomp $pout;
		    $sval = $pout;
		    close PSQL;
		    if(!defined($seqValue{"$seqSchema.$seqName"})){
			$seqValue{"$seqSchema.$seqName"} = $sval;
			$seqDepend{"$seqSchema.$seqName"} = "$s.$table.$id";
		    } else {
			$seqDepend{"$seqSchema.$seqName"} .= ";$s.$table.$id";
		    }
		}
		if(/^\s+(\S+) .* (DEFAULT|default) nextval[\(]'([A-Za-z0-9_]+)'(::regclass)?[\)] NOT NULL/) {
		    $seqSchema = lc($s);
		    $seqName = lc($3);
		    $id = lc($1);

		    $psqlstr = "psql -A -t -c \"select greatest(start_value,last_value) from pg_sequences where schemaname = '$seqSchema' and sequencename = '$seqName'\"";
		    open(PSQL, "$psqlstr|") || die "Failed to start psql";
		    $pout = <PSQL>;
		    chomp $pout;
		    $sval = $pout;
		    close PSQL;
		    if(!defined($seqValue{"$seqSchema.$seqName"})){
			$seqValue{"$seqSchema.$seqName"} = $sval;
			$seqDepend{"$seqSchema.$seqName"} = "$s.$table.$id";
		    } else {
			$seqDepend{"$seqSchema.$seqName"} .= ";$s.$table.$id";
		    }
		    print "Test default $seqSchema.$id $seqName $sval\n";
		}
	    }
	    close SQL;
	}
    }
    closedir DDL;
}


# Go through all sequences and check columns that depend on them

foreach $seq (sort(keys(%seqValue))){
    @ids = split(/;/,$seqDepend{$seq});
    $sval = int($seqValue{$seq});
    $maxival = 0;
    foreach $id (@ids) {
	($testSchema,$testTable,$testId) = split(/[.]/,$id);
	$psqlstr = "psql -A -t -c \"select count(*), max($testId) from $testSchema.$testTable\"";
	if($id eq " i2b2demodata.concept_id") {
	    $psqlstr = "psql -A -t -c \"select max(cast(substring(concept_cd from 3) as int)) from i2b2demodata.concept_dimension where concept_cd like 'TM\%'";
	}
	open(PSQL, "$psqlstr|") || die "Failed to start psql";
	$pout = <PSQL>;
	chomp $pout;
	close PSQL;
	($cnt,$ival) = split(/[\|]/,$pout);
	if($cnt eq "0") {
#	    print "\tNo values found";
	}
	elsif($ival eq "") {
#	    print "\tNo ID value";
	}
	elsif($ival !~ /^[0-9]+$/) {
#	    print "\tTEXT ID '$ival'";
}
	else {
	    # integer ival
	    if(int($ival) > $maxival) {$maxival = $ival}
#	    if(int($ival) > $sval) {print "\tFIX sequence $seq $sval $ival"}
#	    else {print "\tOK $ival"}
	}
#	print "\n";
    }


    if($maxival > $sval) {
	$newmax = $maxival+1;
	if($doupdate) {
	    print "\nUpdating: alter sequence $seq start with $newmax;\n";
	    $psqlstr = "psql -c \"alter sequence $seq start with $newmax\"";
	    open(PSQL, "$psqlstr|") || die "Failed to start psql";
	    while(<PSQL>){
		print;
	    }
	    close PSQL;
	    print "\nRestart:  alter sequence $seq restart;\n";
	    $psqlstr = "psql -c \"alter sequence $seq restart\"";
	    open(PSQL, "$psqlstr|") || die "Failed to start psql";
	    while(<PSQL>){
		print;
	    }
	    close PSQL;
	} else {
	    print "\nalter sequence $seq start with $newmax;\n";
	    print "alter sequence $seq restart;\n";
	}
    }
}
