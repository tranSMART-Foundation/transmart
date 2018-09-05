#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_SanofiProteomics
make -C samples/postgres load_ref_annotation_SanofiProteomics
make -C samples/postgres load_msproteomics_SanofiProteomics

