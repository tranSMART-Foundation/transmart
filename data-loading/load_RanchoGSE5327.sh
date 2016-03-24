#!/bin/sh

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_RanchoGSE5327
make -C samples/postgres load_ref_annotation_RanchoGSE5327
make -C samples/postgres load_expression_RanchoGSE5327
