#!/bin/sh

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_RanchoGSE1456
make -C samples/postgres load_ref_annotation_RanchoGSE1456
make -C samples/postgres load_expression_RanchoGSE1456
