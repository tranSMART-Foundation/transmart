#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_SanofiMirnaGSE37425
make -C samples/postgres load_ref_annotation_SanofiMirnaGSE37425
make -C samples/postgres load_mirnaqpcr_SanofiMirnaGSE37425

