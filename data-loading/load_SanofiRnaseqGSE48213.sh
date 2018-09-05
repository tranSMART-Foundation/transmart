#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_SanofiRnaseqGSE48213
make -C samples/postgres load_ref_annotation_SanofiRnaseqGSE48213
make -C samples/postgres load_rnaseq_SanofiRnaseqGSE48213

