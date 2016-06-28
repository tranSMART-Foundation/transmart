out=~/Desktop/Process.txt

dirs="transmart-core-api
transmart-core-db
Rmodules
transmart-legacy-db
folder-management-plugin
transmart-extensions
transmart-gwas-plugin
blend4j-plugin
transmart-metacore-plugin
transmartApp
transmart-data
tranSMART-ETL
transmart-batch
transmart-ICE
transmart-rest-api
RInterface
SmartR
GWAVA
transmart-test
Scripts
"

echo "Mapping Command . . ." > $out

for dir in $dirs; do
	pushd $dir
		echo "" >> $out
		echo "++ $dir ++" >> $out
#		echo "++ $dir ++"
# -------------- actual commands
#		git checkout master >> $out
#		git checkout release-1.2.5-Beta >> $out
#		git checkout for-testing-changes >> $out
#		git fetch -p >> $out
#		git fetch transmart >> $out
#		git pull >> $out
#		git log --since="Jan 4, 2016" --pretty=fuller >> $out
#		git merge master >> $out
#		git merge release-1.2.5-Beta >> $out
#		git merge transmart/master >> $out
#		git merge transmart >> $out
#		git branch -D trial-merge >> $out
#		git remote -v >> $out
#		git diff --name-only transmart/master >> $out
#		git branch -a >> $out
#		git checkout -b release-16.1 >> $out
#		git push -u origin release-16.1 >> $out
#		git branch >> $out
#		git push >> $out
#		git pull >> $out
#		git log -1 >> $out
		git status >> $out
# -------------- actual commands		
#		echo "-- $dir --" >> $out
	popd
done

echo "DONE!" >> $out
echo "DONE!"

# checkout "protection" branch
# git checkout for-testing-changes >> $out

# various log options
# git log --pretty=oneline v1.2.4...HEAD >> $out
# git log --pretty=full v1.2.4...HEAD >> $out
# git shortlog -s -n --all v1.2.4...HEAD >> $out

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
#		git checkout -b release-1.2.5-Beta
#		git push -u origin release-1.2.5-Beta

# commit/push version update changes
#		git commit -a -m "update version number to 16.1" >> $out
#		git push >> $out
#		git status >> $out

# create release branch (incremental)
#		git fetch origin
#		git checkout -b release-1.2.5-Beta origin/release-1.2.5-Beta

# update refs 
#        git checkout release-1.2.5-Beta >> $out
#		git commit -a -m "created tags for release" >> $out
#        git push >> $out
#		git status >> $out

# search for SNAPSHOT
#		find . -name "pom.xml" -exec grep -H "SNAPSHOT" \{\} \; >> $out
#		find . -name "*Plugin.xml" -exec grep -H "SNAPSHOT" \{\} \; >> $out
#		find . -name "VERSION.txt" -exec grep -H "SNAPSHOT" \{\} \; >> $out
#		find . -name "*Plugin.groovy" -exec grep -H "SNAPSHOT" \{\} \; >> $out
#		find . -name "BuildConfig.groovy" -exec grep -H "SNAPSHOT" \{\} \; >> $out
