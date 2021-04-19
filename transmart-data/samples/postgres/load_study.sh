#!/bin/bash

$TRANSMARTDATA/samples/postgres/load_study.pl $1 | tee $TRANSMARTDATA/$1_loadstudy.log

