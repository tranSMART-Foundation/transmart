#!/bin/sh

#todo check database path. Surely ~transmart/transmart?

# download dictionary loaders
# download latest dictionaries
# from dictionaries/

mkdir -p /data/ETL/dictionaries
cd /data/ETL/dictionaries

#entrez-dictionary.tar.xz
#geneontology-dictionary.tar.xz
#hmdb-dictionary.tar.xz
#kegg-dictionary.tar.xz
#mesh-dictionary.tar.xz
#mirna-dictionary.tar.xz
#taxonomy-dictionary.tar.xz
#uniprot-sprot-human-dictionary.tar.xz

#load-dictionaries.tar.xz


cd /data/ETL/load-dictionaries

./load_all.sh
