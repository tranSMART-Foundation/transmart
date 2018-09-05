#!/bin/sh

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_RanchoGSE20690
make -C samples/postgres load_ref_annotation_RanchoGSE20690
make -C samples/postgres load_expression_RanchoGSE20690
