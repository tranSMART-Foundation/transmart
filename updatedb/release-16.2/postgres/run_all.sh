#!/bin/sh -f

./update-00-check.sh

# 1 AMAPP and FMAPP schemas for Browse tab

psql -f ./update-11a-fmapp-tabperm-ffa.sql

# 2 BIOMART schema

psql -f ./update-21a-biomart-adhocprop.sql
psql -f ./update-21b-biomart-assaneqtl.sql
psql -f ./update-21c-biomart-assangwas.sql
psql -f ./update-21d-biomart-index-asyanadataext.sql
psql -f ./update-21e-biomart-bdobs.sql
psql -f ./update-21f-biomart-bdplat.sql

psql -f ./update-22a-biomart-delindex-bac.sql 
psql -f ./update-22b-biomart-blid.sql 
psql -f ./update-22c-biomart-modtab-hmr.sql 

psql -f ./update-23a-biomart-drop-meshcopy.sql
psql -f ./update-23b-biomart-drop-meshpath.sql
psql -f ./update-23c-biomart-drop-meshtemp.sql

# 3 BIOMART_USER schema

psql -f ./update-31a-bmu-drop-tempioe.sql
psql -f ./update-31b-bmu-drop-tempkde.sql
psql -f ./update-32a-bmu-funcmod-metzc.sql
psql -f ./update-32b-bmu-funcmod-bulkadd.sql

# 4 DEAPP schema

psql -f ./update-41a-deapp-modtable-geneinfo.sql
psql -f ./update-41b-deapp-modtable-desubacgh.sql
psql -f ./update-41c-deapp-modtable-desubmicdata.sql
psql -f ./update-41d-deapp-modtable-desubmiclog.sql
psql -f ./update-41e-deapp-modtable-desubmicmed.sql
psql -f ./update-41f-deapp-modtable-desubprot.sql
psql -f ./update-41g-deapp-modtable-desubproteo.sql
psql -f ./update-41h-deapp-modtable-tworeg.sql
psql -f ./update-41i-deapp-modtable-tworje.sql
psql -f ./update-41j-deapp-modtable-dvsi.sql
psql -f ./update-41k-deapp-modtable-dvss.sql
psql -f ./update-41l-deapp-modtable-ricerca.sql

psql -f ./update-42a-deapp-funcmod-procrnaseq.sql

psql -f ./update-43a-deapp-newview-hg19.sql

# 5 I2B2DEMODATA schema

psql -f update-51a-demodata-funcmod-syncclrmod.sql
psql -f update-52a-demodata-modtable-patdim.sql
psql -f update-53a-demodata-modtable-qtpsc.sql

# 6 I2B2METADATA schema

psql -f update-61a-metadata-funcmod-addontnode.sql
psql -f update-62a-metadata-modtable-i2b2.sql
psql -f update-63a-metadata-modtable-i2b2sec.sql

# 7 SEARCHAPP schema

psql -f ./update-71a-searchapp-newtable-impxnatconfig.sql
psql -f ./update-71b-searchapp-newtable-impxnatvar.sql
psql -f ./update-72a-searchapp-newtable-xnatsubject.sql

# 8.1 TM_CZ schema

psql -f ./update-80a-tmcz-funcmod-czaudex.sql
psql -f ./update-80b-tmcz-funcmod-czendaud.sql
psql -f ./update-80c-tmcz-funcmod-czerrhand.sql
psql -f ./update-80d-tmcz-funcmod-czwriterr.sql
psql -f ./update-80e-tmcz-funcmod-czwritinfo.sql
psql -f ./update-80f-tmcz-funcmod-czxarrsort.sql
psql -f ./update-80g-tmcz-funcmod-czxerrhand.sql
psql -f ./update-80h-tmcz-funcmod-czxinfohand.sql
psql -f ./update-80i-tmcz-funcmod-czxpctcont.sql
psql -f ./update-80j-tmcz-funcmod-czxstartaud.sql
psql -f ./update-80k-tmcz-funcmod-czxtabindmaint.sql
psql -f ./update-80l-tmcz-funcmod-czxwriteaud.sql
psql -f ./update-80m-tmcz-funcmod-czxwriteerr.sql
psql -f ./update-80n-tmcz-funcmod-finduser.sql
psql -f ./update-80o-tmcz-funcmod-addnode.sql
psql -f ./update-80p-tmcz-funcmod-addroot.sql
psql -f ./update-80q-tmcz-funcmod-addsnpmark.sql
psql -f ./update-80r-tmcz-funcmod-addschtrm.sql

psql -f ./update-81a-tmcz-funcmod-creconcnt.sql
psql -f ./update-81b-tmcz-funcmod-cresectrial.sql
psql -f ./update-81c-tmcz-funcmod-cresecinctrial.sql
psql -f ./update-81d-tmcz-funcmod-delnode.sql
psql -f ./update-81d-tmcz-funcmod-delnodes.sql
psql -f ./update-81e-tmcz-funcmod-extclin.sql
psql -f ./update-81f-tmcz-funcmod-filltree.sql
psql -f ./update-81g-tmcz-funcmod-hidenode.sql
psql -f ./update-81h-tmcz-funcmod-loadanndeapp.sql
psql -f ./update-81j-tmcz-funcmod-loadclin.sql
psql -f ./update-81k-tmcz-funcmod-loadclininc.sql

psql -f ./update-82a-tmcz-funcmod-loadomicsann.sql
psql -f ./update-82b-tmcz-funcmod-loadprotann.sql
psql -f ./update-82c-tmcz-funcmod-loadrbmdata.sql
psql -f ./update-82d-tmcz-funcmod-loadsamcat.sql
psql -f ./update-82e-tmcz-funcmod-loadsecdat.sql
psql -f ./update-82f-tmcz-funcmod-loadstudmet.sql
psql -f ./update-82g-tmcz-funcmod-metzcalc.sql
psql -f ./update-82h-tmcz-funcmod-mirnazcalc.sql
psql -f ./update-83a-tmcz-funcmod-movenode.sql
psql -f ./update-83b-tmcz-funcmod-movestudy.sql
psql -f ./update-83c-tmcz-funcmod-mrnainxmaint.sql
psql -f ./update-83d-tmcz-funcmod-mrnazcalc.sql
psql -f ./update-83e-tmcz-funcmod-procacgh.sql
psql -f ./update-83f-tmcz-funcmod-procmet.sql
psql -f ./update-83g-tmcz-funcmod-procmrna.sql
psql -f ./update-83h-tmcz-funcmod-procprot.sql
psql -f ./update-83i-tmcz-funcmod-procqpcr.sql
psql -f ./update-83j-tmcz-funcmod-procrna-seq.sql
psql -f ./update-83k-tmcz-funcmod-procrnaseq.sql
psql -f ./update-83l-tmcz-funcmod-protzcalc.sql
psql -f ./update-83m-tmcz-funcmod-rbmzcalc.sql
psql -f ./update-83n-tmcz-funcmod-renamenode.sql
psql -f ./update-83o-tmcz-funcmod-rnaseqzcalc.sql
psql -f ./update-83p-tmcz-funcmod-secstudy.sql
psql -f ./update-83q-tmcz-funcmod-instr.sql
psql -f ./update-83r-tmcz-funcmod-isnum.sql
psql -f ./update-83s-tmcz-funcmod-parsenth.sql
psql -f ./update-83t-tmcz-funcmod-rdcreload.sql
psql -f ./update-83u-tmcz-funcmod-upgrademrna.sql

psql -f ./update-84a-tmcz-funcmod-baauid.sql
psql -f ./update-84b-tmcz-funcmod-bapuid.sql
psql -f ./update-84c-tmcz-funcmod-baapuid.sql
psql -f ./update-84d-tmcz-funcmod-biocompuid.sql
psql -f ./update-84e-tmcz-funcmod-biocuruid.sql
psql -f ./update-84f-tmcz-funcmod-biodisuid.sql
psql -f ./update-84g-tmcz-funcmod-bioexpuid.sql
psql -f ./update-84h-tmcz-funcmod-bjoduid.sql
psql -f ./update-84i-tmcz-funcmod-bjosduid.sql
psql -f ./update-84j-tmcz-funcmod-biomgeneuid.sql
psql -f ./update-84k-tmcz-funcmod-biompathuid.sql
psql -f ./update-84l-tmcz-funcmod-trunreltab.sql
psql -f ./update-84m-tmcz-funcmod-sbduidd.sql
psql -f ./update-84n-tmcz-funcmod-sbduidp.sql

psql -f ./update-85a-tmcz-funcmod-czfinit.sql
psql -f ./update-85b-tmcz-funcmod-dropsyn.sql
psql -f ./update-85c-tmcz-funcmod-droptable.sql
psql -f ./update-85d-tmcz-funcmod-jnjinit.sql
psql -f ./update-85e-tmcz-funcmod-rdcinit.sql
psql -f ./update-85f-tmcz-funcmod-loadkegg.sql
psql -f ./update-85g-tmcz-funcmod-numparse.sql
psql -f ./update-85h-tmcz-funcmod-sfxtab.sql
psql -f ./update-85i-tmcz-funcmod-textparser.sql
psql -f ./update-85j-tmcz-funcmod-trunctab.sql

psql -f ./update-86a-tmcz-deltable-anndeapp.sql
psql -f ./update-86b-tmcz-modtable-desubprot.sql
psql -f ./update-86c-tmcz-modtable-desubmrna.sql
psql -f ./update-86d-tmcz-modtable-desubrbm.sql
psql -f ./update-86e-tmcz-deltable-dk1.sql
psql -f ./update-86f-tmcz-deltable-gplid.sql
psql -f ./update-86g-tmcz-modtable-patdim.sql
psql -f ./update-86h-tmcz-modtable-tmpsubinf.sql

# 8.2 TM_LZ schema

psql -f ./update-88a-tmlz-funcmod-procqpcr.sql
psql -f ./update-88b-tmlz-modtable-snpgm.sql
psql -f ./update-88c-tmlz-modtable-ssenrol.sql

# 8.3 TM_WZ schema

psql -f ./update-89a-tmwz-modtable-clin.sql
psql -f ./update-89b-tmwz-modtable-subinfo.sql
psql -f ./update-89c-tmwz-modtable-submbprob.sql
psql -f ./update-89d-tmwz-modtable-mbcalc.sql
psql -f ./update-89e-tmwz-modtable-submblog.sql
psql -f ./update-89f-tmwz-modtable-submbmed.sql
psql -f ./update-89g-tmwz-modtable-submiclog.sql
psql -f ./update-89h-tmwz-modtable-submirlog.sql
psql -f ./update-89i-tmwz-modtable-submirmed.sql
psql -f ./update-89j-tmwz-modtable-subprotlog.sql
psql -f ./update-89k-tmwz-modtable-subprotmed.sql
psql -f ./update-89l-tmwz-modtable-subrbmcalc.sql
psql -f ./update-89m-tmwz-modtable-subrbmlog.sql
psql -f ./update-89n-tmwz-modtable-subrbmmed.sql
psql -f ./update-89o-tmwz-modtable-subrnacalc.sql
psql -f ./update-89p-tmwz-modtable-subrnalog.sql
psql -f ./update-89q-tmwz-modtable-subrnamed.sql
psql -f ./update-89r-tmwz-modtable-submicmed.sql

# 9 GWAS_PLINK schema

psql -f ./update-91a-plink-role.sql
psql -f ./update-91b-plink-modrole.sql

./update-92-plink-make.sh

psql -f ./update-93-plink-tablespace.sql
psql -f ./update-94-plink-defperm.sql


# 10 other schemas
