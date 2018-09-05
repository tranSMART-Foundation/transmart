#!/bin/sh

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_EtriksGSE10024
make -C samples/postgres load_ref_annotation_EtriksGSE10024
make -C samples/postgres load_expression_EtriksGSE10024
