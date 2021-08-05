#!/bin/bash

$TRANSMARTDATA/samples/postgres/load_study.pl $1 $2 | tee $TRANSMARTDATA/samples/studies/$1_loadstudy.log

