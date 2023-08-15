#!/bin/bash -e

# savetable.sh <schema> <table>
# save the row values for a table
# tab separated, but with an integer conversion
#    so that we get 0 and 1 rather than 't' and 'f'
# This is so the result of COPY can be used in Oracle too.

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

psql -c "COPY (SELECT `$SCRIPT_DIR/colnames.sh $1 $2` FROM $1.$2) TO STDOUT CSV DELIMITER E'\\t' FORCE QUOTE *" > $2.tsv

if [ -s "$2.tsv" ]; then
    tsvlines=$(wc -l "$2.tsv" | awk '{print $1}')
    echo "Table $1.$2 saved to `pwd`/$2.tsv $tsvlines rows"
else
    echo "Table $1.$2 saved to `pwd`/$2.tsv EMPTY"
fi

