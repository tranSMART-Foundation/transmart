#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_SanofiSerialRicerca
make -C samples/postgres load_ref_annotation_SanofiSerialRicerca
make -C samples/postgres load_expression_SanofiSerialRicerca

