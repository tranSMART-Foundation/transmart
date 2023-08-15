#!/bin/bash -e

# loadtable.sh <schema> <table>
# finds the .tsv file for th etabel
# (saves with savetable.sh)
#
# loads data into table

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

if [ `psql -c "SELECT EXISTS (SELECT * FROM $1.$2 LIMIT 1)" -tA` = 't' ]; then \
		echo "WARNING: The table $1.$2 already has data; skipped" >&2; \
	else \
		echo "Loading $1.$2"; \
		psql -c "COPY $1.$2 FROM STDIN \
			CSV DELIMITER E'\t'" < $SCRIPT_DIR/$2.tsv; \
	fi

