#!/bin/sh

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_RanchoGSE34466
