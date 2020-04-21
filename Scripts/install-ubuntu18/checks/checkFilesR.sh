#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSMART install and data loading
# ********************************************************************************

echo "------------------------------------------------------------"
echo "|  Checking for required folders and files for the R install"
echo "------------------------------------------------------------"

base="$INSTALL_BASE/transmart-data"
baseR="$base/R"

returnValue=0
missingPackages=0
for filepath in "$baseR" "$baseR/root" "$baseR/root/bin" "$baseR/root/lib"
do
	if [ ! -d "$filepath" ]; then
		echo "The file at $filepath"
		echo "  is required and does not exist; this should have been created"
		echo "  in the 'buildR' step of the install; repeat that step"
		returnValue=1
	fi
done
if [ $returnValue != 0 ]; then
    exit $returnvalue
fi

baserootbin="$baseR/root/bin"
for rexec in R Rscript
do
	if [ ! -e "$baserootbin/$rexec" ]; then
		echo "The file at $baserootbin/$rexec"
		echo "  is required and does not exist; this should have been created"
		echo "  in the 'buildR' step of the install; repeat that step"
		returnValue=1
	fi
done


rlib="$baseR/root/lib/R/library"
for rpackage in reshape reshape2 ggplot2 data.table Cairo snowfall gplots foreach doParallel visreg pROC jsonlite RUnit shiny Rserve WGCNA CGHtest CGHtestpar
do
	if [ ! -d "$rlib/$rpackage" ]; then
		echo "The R package at $rlib/$rpackage"
		echo "  is required and does not exist; this should have been created"
		echo "  in the 'buildR' step of the install; repeat that step"
		((missingPackages++))
		returnValue=1
	fi

done

bioclib="$baseR/root/lib/R/library"
for biocpackage in impute multtest CGHbase edgeR DESeq2 limma snpStats preprocessCore GO.db AnnotationDbi QDNAseq
do
	if [ ! -d "$bioclib/$biocpackage" ]; then
		echo "The other R/BioConductor package at $bioclib/$biocpackage"
		echo "  is required and does not exist; this should have been created"
		echo "  in the 'buildR' step of the install; repeat that step"
		((missingPackages++))
		returnValue=1
	fi

done


if [ $missingPackages != 0 ]; then
    echo "There are $missingPackages R/BioConductor packages missing"
fi

exit $returnValue
