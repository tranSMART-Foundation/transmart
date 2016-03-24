#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_SanofiRbmAdni
make -C samples/postgres load_ref_annotation_SanofiRbmAdni
make -C samples/postgres load_rbm_SanofiRbmAdni

