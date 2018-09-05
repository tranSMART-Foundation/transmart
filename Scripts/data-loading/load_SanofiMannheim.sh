#!/bin/bash

cd $TRANSMART_DATA

. ./vars

make -C samples/postgres load_clinical_SanofiMannheim

