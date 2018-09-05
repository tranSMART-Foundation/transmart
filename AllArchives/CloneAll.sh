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

echo "Mapping Clone Command . . ."

for dir in $dirs; do
	echo "++ ${dir} ++"
    git clone git@github.com:tranSMART-Foundation/${dir}.git
	echo "-- $dir --"
	echo ""
done

echo "DONE!"
