<?php

if ($argc != 2) {
	fprintf(STDERR, "Syntax: %s <schema>\n", $argv[0]);
	exit(1);
}

$listFile = __DIR__ . "/$argv[1]_list";
$tableList = file($listFile, FILE_IGNORE_NEW_LINES);
if ($tableList === false) {
	fprintf(STDERR, "File %s does not exist. No tables to dump for this schema?\n",
			$listFile);
	exit(1);
}

echo "THIS_SCHEMA := ", $argv[1], "\n";
echo "include $(DEMOCOMMON_DIR)/makefile_schemas.inc\n";

foreach ($tableList as $table) {
	$table = trim($table);
	if ($table === '') {
		continue;
	}

	if (!key_exists($table, $dependencies)) {
		continue;
	}

	$theseDeps = array_filter($dependencies[$table],
			function ($d) use ($tableList, $table) {
				$ret = in_array($d, $tableList);
				if (!$ret) {
					fprintf(STDERR, "\e[0;31mWARNING\e[0m: Table %s is missing "
							. "data from dependency %s.\n", $table, $d);
				}
				return $ret;
			});
	if (empty($theseDeps)) {
		continue;
	}

	echo "load_" . $table, ": ", array_reduce($theseDeps,
		function (&$result, $item) {
			if ($result) $result .= " ";
			return $result . "load_" . $item;
		}, ''), "\n";
}

$all_load_targets = implode(" ", array_map(function ($t) { return "load_$t"; }, $tableList));
echo "\n";
echo "load: $all_load_targets\n";
echo ".PHONY: load\n";

$all_append_targets = implode(" ", array_map(function ($t) { return "load_$t"; }, $tableList));
echo "\n";
echo "append: $all_append_targets\n";
echo ".PHONY: append\n";

