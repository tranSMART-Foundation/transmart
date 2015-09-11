#!/bin/tcsh

set dirs = (\
transmart-core-api \
transmart-core-db \
Rmodules \
transmart-legacy-db \
folder-management-plugin \
transmart-extensions \
transmart-gwas-plugin \
blend4j-plugin \
transmart-metacore-plugin \
transmartApp \
transmart-data \
tranSMART-ETL \
transmart-batch \
transmart-ICE \
transmart-rest-api \
RInterface \
GWAVA \
transmart-test \
transmart-docker \
Scripts
)

echo "Mapping Command . . ." 

foreach dir ($dirs)
	echo "++ $dir ++"
	git clone git@github.com:tranSMART-Foundation/{$dir}.git
	echo "-- $dir --"
	echo "" 
end

echo "DONE!"
