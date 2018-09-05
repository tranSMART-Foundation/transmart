#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_SanofiMirnaGSE42468
make -C samples/postgres load_ref_annotation_SanofiMirnaGSE42468
make -C samples/postgres load_mirnaqpcr_SanofiMirnaGSE42468

