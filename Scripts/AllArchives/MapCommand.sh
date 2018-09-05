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
transmartApp \
transmart-data \
tranSMART-ETL \
transmart-batch \
transmart-ICE \
transmart-rest-api \
RInterface \
GWAVA \
transmart-test \
Scripts \
)

echo "Mapping Command . . ." > $out

foreach dir ($dirs)
	pushd $dir
		echo "" >> $out
		echo "++ $dir ++" >> $out
		echo "++ $dir ++"
# -------------- actual commands
		git checkout master >> $out
#		git checkout release-16.2 >> $out
#		git checkout for-testing-changes >> $out
#		git fetch -p >> $out
#		git fetch -p transmart >> $out
#		git log --since="Jan 4, 2016" --pretty=fuller >> $out
#		git merge master >> $out
#		git merge transmart/master >> $out
#		git merge transmart >> $out
#		git branch -D trial-merge >> $out
#		git remote -v >> $out
#		git diff --name-only transmart/master >> $out
#		git branch -a >> $out
#		git checkout -b release-16.2 >> $out
#		git push -u origin release-16.2 >> $out
#		git branch >> $out
#		git push >> $out
#		git pull >> $out
#		git log -1 >> $out
		git status >> $out
# -------------- actual commands		
		echo "-- $dir --" >> $out
	popd
end

echo "DONE!" >> $out
echo "DONE!"

# checkout "protection" branch
# git checkout for-testing-changes >> $out

# various log options
# git log --pretty=oneline release-16.2...HEAD >> $out
# git log --pretty=full release-16.2...HEAD >> $out
# git shortlog -s -n --all release-16.2...HEAD >> $out

# various checking commands
# git status >> $out

# initial clone
# git clone git@github.com:tranSMART-Foundation/{$dir}.git >> $out

# to set remote = transmart
# git remote add transmart git@github.com:transmart/{$dir}.git >> $out

# to check diff with remote = transmart - already defined
#		git fetch transmart
#		git diff --name-only transmart/master >> $out

# create release branch (original)
#		git checkout -b release-16.2
#		git push -u origin release-16.2

# commit/push version update changes
#		git commit -a -m "update version number to 16.2" >> $out
#		git push >> $out
#		git status >> $out

# create release branch (incremental)
#		git fetch origin
#		git checkout -b release-16.2 origin/release-16.2

# update refs 
#        git checkout release-16.2 >> $out
#		git commit -a -m "created tags for release" >> $out
#        git push >> $out
#		git status >> $out

# search for SNAPSHOT
#		find . -name "pom.xml" -exec grep -H "SNAPSHOT" \{\} \; >> $out
#		find . -name "*Plugin.xml" -exec grep -H "SNAPSHOT" \{\} \; >> $out
#		find . -name "VERSION.txt" -exec grep -H "SNAPSHOT" \{\} \; >> $out
#		find . -name "*Plugin.groovy" -exec grep -H "SNAPSHOT" \{\} \; >> $out
#		find . -name "BuildConfig.groovy" -exec grep -H "SNAPSHOT" \{\} \; >> $out
