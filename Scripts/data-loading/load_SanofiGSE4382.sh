#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_SanofiGSE4382Inc
make -C samples/postgres load_clinical_SanofiGSE4382Inc2
make -C samples/postgres load_clinical_SanofiGSE4382Inc2
make -C samples/postgres load_clinical_SanofiGSE4382Inc4
