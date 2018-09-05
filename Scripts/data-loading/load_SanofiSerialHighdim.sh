#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_ref_annotation_SanofiSerialHighdim
make -C samples/postgres load_expression_SanofiSerialHighdim

