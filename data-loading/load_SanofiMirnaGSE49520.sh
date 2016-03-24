#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_ref_annotation_SanofiMirnaGSE49520
make -C samples/postgres load_mirnaqpcr_SanofiMirnaGSE49520

