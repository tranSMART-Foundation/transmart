#!/bin/bash

$TRANSMARTDATA/samples/oracle/load_study.pl $1 | tee $TRANSMARTDATA/samples/studies/$1_loadstudy.log

