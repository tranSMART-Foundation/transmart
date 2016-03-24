#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_SanofiMetabolomics
make -C samples/postgres load_ref_annotation_SanofiMetabolomics
make -C samples/postgres load_metabolomics_SanofiMetabolomics

