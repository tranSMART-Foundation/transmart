#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_SanofiMirnaseqWitten
make -C samples/postgres load_ref_annotation_SanofiMirnaseqWitten
make -C samples/postgres load_mirnaseq_SanofiMirnaseqWitten

