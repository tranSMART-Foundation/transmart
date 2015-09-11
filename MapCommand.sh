#!/bin/tcsh

set out = "~/Desktop/Process.txt"

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
# transmartApp \
transmart-data \
tranSMART-ETL \
transmart-batch \
transmart-ICE \
transmart-rest-api \
RInterface \
GWAVA \
transmart-test \
)

echo "Mapping Command . . ." > $out

foreach dir ($dirs)
	pushd $dir >> $out
		echo "" >> $out
		echo "" >> $out
		echo "++ $dir ++" >> $out
		echo "++ $dir ++"
		git fetch transmart
		git diff --name-only transmart/master >> $out
		echo "-- $dir --" >> $out
		echo "" >> $out
	popd >> $out
end

echo "DONE!"

# git log --pretty=oneline v1.2.3...HEAD >> $out
# git log --pretty=full v1.2.3...HEAD >> $out
# git shortlog -s -n --all v1.2.3...HEAD >> $out
# git clone git@github.com:tranSMART-Foundation/{$dir}.git >> $out
# git remote add transmart git@github.com:transmart/{$dir}.git >> $out
# git fetch transmart
# git diff --name-only transmart/master >> $out
