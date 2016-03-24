#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_SanofiGSE38642
make -C samples/postgres load_ref_annotation_SanofiGSE38642
make -C samples/postgres load_expression_SanofiGSE38642
