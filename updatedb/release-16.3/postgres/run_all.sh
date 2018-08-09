#!/bin/sh -f

./update-00-check163.sh

# 1 AMAPP and FMAPP schemas for Browse tab

#psql -f ./update-11a-fmapp-tabperm-ffa.sql

# 2 BIOMART schema

#psql -f ./update-21a-biomart-adhocprop.sql

# 3 BIOMART_USER schema

psql -f ./update-31a-bmu-funcmod-metazscore.sql
psql -f ./update-31b-bmu-viewmod-browseassays.sql

# 4 DEAPP schema

psql -f ./update-43g-deapp-funcmod-procrna-seq.sql

# 5 I2B2DEMODATA schema

psql -f ./update-51a-demodata-obsfact-nval.sql


# 6 I2B2METADATA schema

psql -f update-61a-metadata-funcmod-addontnode.sql
psql -f update-62a-metadata-inx-secure.sql

# 7 SEARCHAPP schema

#psql -f ./update-71a-searchapp-newtable-impxnatconfig.sql

# 8.1 TM_CZ schema

psql -f ./update-81a-tmcz-funcmod-metazscore.sql

psql -f ./update-82a-tmcz-funcmod-extendclin.sql
psql -f ./update-82b-tmcz-funcmod-loadclin.sql
psql -f ./update-82c-tmcz-funcmod-loadclininc.sql
psql -f ./update-82d-tmcz-funcmod-loadrbm.sql

psql -f ./update-83a-tmcz-funcmod-procacgh.sql
psql -f ./update-83b-tmcz-funcmod-procmetab.sql
psql -f ./update-83c-tmcz-funcmod-procmrna.sql
psql -f ./update-83d-tmcz-funcmod-procprot.sql
psql -f ./update-83e-tmcz-funcmod-procqpcr.sql
psql -f ./update-83f-tmcz-funcmod-procrnaseq.sql
psql -f ./update-83g-tmcz-funcmod-procrna-seq.sql

psql -f ./update-84a-tmcz-funcmod-loadsampcat.sql

# 8.2 TM_LZ schema

psql -f ./update-88e-tmlz-funcmod-procqpcr.sql

# 8.3 TM_WZ schema

#psql -f ./update-89a-tmwz-modtable-clin.sql

# 9 GWAS_PLINK schema

#psql -f ./update-91a-plink-role.sql

# 10 other schemas
