include ../../makefile.inc

# This file is to included from:
#   ../oracle/Makefile
#   ../postgres/Makefile
#
# It still has some PostgreSQL specific stuff; that should be moved into
# PostgreSQL's Makefile and only the common stuff should be here.
# To be done when more analyses are ported to Oracle

showdblog: $(JDBC_DRIVER)
	groovy -cp '$(LIB_CLASSPATH)' ../common/dump_audit.groovy $(RDBMS) `tput cols`

showdblog_%: $(JDBC_DRIVER)
	groovy -cp '$(LIB_CLASSPATH)' ../common/dump_audit.groovy $(RDBMS) `tput cols` $*

jdbcdriver: $(JDBC_DRIVER)

.PHONY: showdblog

export KETTLE_JOBS
export JDBC_DRIVER
export LIB_CLASSPATH

ifndef KITCHEN
$(error KITCHEN is not set)
endif

DATASETS_FILE := $(wildcard ../studies/datasets)
ifndef DATASETS_FILE
$(info Fetching datasets...)
DUMMY := $(shell $(MAKE) -C ../studies datasets)
$(info Finished fetching datasets)
endif

LOAD_ALL_TARGETS := $(shell perl ../studies/find_studies.pl)

LOAD_STUDY_TARGETS := $(shell php ../studies/enumerate.php load_targets study)
LOAD_PROGRAM_TARGETS := $(shell php ../studies/enumerate.php load_targets program)
LOAD_BROWSE_TARGETS := $(shell php ../studies/enumerate.php load_targets browse)

LOAD_CLINICAL_TARGETS := $(shell php ../studies/enumerate.php load_targets clinical)

LOAD_ANNOTATION_TARGETS := $(shell php ../studies/enumerate.php load_targets annotation)
LOAD_SAMPLES_TARGETS := $(shell php ../studies/enumerate.php load_targets samples)

LOAD_REF_ANNOTATION_TARGETS := $(shell php ../studies/enumerate.php load_targets ref_annotation)

LOAD_ACGH_PARAMS_TARGETS := $(shell php ../studies/enumerate.php load_targets acgh)
LOAD_EXPRESSION_PARAMS_TARGETS := $(shell php ../studies/enumerate.php load_targets expression)
LOAD_METABOLOMICS_PARAMS_TARGETS := $(shell php ../studies/enumerate.php load_targets metabolomics)
LOAD_MIRNA_PARAMS_TARGETS := $(shell php ../studies/enumerate.php load_targets mirna)
LOAD_MIRNAQPCR_PARAMS_TARGETS := $(shell php ../studies/enumerate.php load_targets mirnaqpcr)
LOAD_MIRNASEQ_PARAMS_TARGETS := $(shell php ../studies/enumerate.php load_targets mirnaseq)
LOAD_MSPROTEOMICS_PARAMS_TARGETS := $(shell php ../studies/enumerate.php load_targets msproteomics)
LOAD_PROTEOMICS_PARAMS_TARGETS := $(shell php ../studies/enumerate.php load_targets proteomics)
LOAD_RBM_PARAMS_TARGETS := $(shell php ../studies/enumerate.php load_targets rbm)
LOAD_RNASEQ_PARAMS_TARGETS := $(shell php ../studies/enumerate.php load_targets rnaseq)
LOAD_VCF_PARAMS_TARGETS := $(patsubst ../studies/%/vcf.params,load_vcf_%, $(VCF_PARAMS_FILES))

KETTLE_TARGETS := $(LOAD_CLINICAL_TARGETS) $(LOAD_EXPRESSION_PARAMS_TARGETS) $(LOAD_ACGH_PARAMS_TARGETS) $(LOAD_PROTEOMICS_PARAMS_TARGETS) $(LOAD_MSPROTEOMICS_PARAMS_TARGETS) $(LOAD_MIRNA_PARAMS_TARGETS) $(LOAD_MIRNAQPCR_PARAMS_TARGETS) $(LOAD_MIRNASEQ_PARAMS_TARGETS) $(LOAD_METABOLOMICS_PARAMS_TARGETS) $(LOAD_RBM_PARAMS_TARGETS) $(LOAD_RNASEQ_PARAMS_TARGETS)

$(KETTLE_TARGETS): kettle-home/kettle.properties


#these ones are here just for autocompletion:
$(LOAD_ANNOTATION_TARGETS):
$(LOAD_PROGRAM_TARGETS):
$(LOAD_BROWSE_TARGETS):
$(LOAD_SAMPLES_TARGETS):
$(LOAD_STUDY_TARGETS):
$(LOAD_REF_ANNOTATION_TARGETS):
$(LOAD_SAMPLE_TARGETS):
$(LOAD_VCF_PARAMS_TARGETS):
$(LOAD_ALL_TARGETS):

kettle-home/kettle.properties: gen_kettle_properties.php
	php -d variables_order=E $< > $@

.PHONY: kettle-home/kettle.properties

../studies/%:
	$(MAKE) -C ../studies $*

load_clinical_%: ../studies/%/clinical.params ../studies/%/clinical
	STUDY_ID=$* DATA_LOCATION="$(realpath ../studies/$*/clinical)" \
			KETTLE_HOME="$(realpath kettle-home)" \
			./load_clinical.sh $<

load_annotation_%: ../studies/%/annotation.params ../studies/%/annotation
	DATA_LOCATION="$(realpath ../studies/$*/annotation)" \
		KETTLE_HOME="$(realpath kettle-home)" \
		PSQL_COMMAND="$(PSQL_COMMAND)" \
		./load_annotation.sh $<

load_ref_annotation_%: ../studies/%/ref_annotation.params
	$(foreach  refplatform, $(shell ./load_ref_annotation.sh $<), \
	    KETTLE_HOME="$(realpath kettle-home)" $(MAKE) load_annotation_$(refplatform); $(shell echo "refplatform=$(refplatform)" ); )

# Load all data for a study

load_all_%: ../studies/%/loadall.params
	    ./load_study.sh $*

# Browse tab study - check and load program, then load study

load_browse_%: ../studies/%/browse.params
	$(foreach  program, $(shell ./show_browse_program.sh $<), \
	    KETTLE_HOME="$(realpath kettle-home)" $(MAKE) load_program_$(program); $(shell echo "program=$(program)" ); )
	    STUDY_ID=$* DATA_LOCATION="$(realpath ../studies/$*/browse)" \
			./load_browse_study.sh $<

# Browse tab program - picked up from browse.params and loaded if not found

load_program_%: ../studies/%/program.params
	STUDY_ID=$* DATA_LOCATION="$(realpath ../studies/$*/program)" \
			KETTLE_HOME="$(realpath kettle-home)" \
			./load_browse_program.sh $<

# load targets for each data type identified by params file

load_acgh_%: ../studies/%/acgh.params ../studies/%/acgh
	STUDY_ID=$* DATA_LOCATION="$(realpath ../studies/$*/acgh)" \
			PSQL_COMMAND="$(PSQL_COMMAND)" \
			KETTLE_HOME="$(realpath kettle-home)" \
			./load_acgh.sh $<

load_expression_%: ../studies/%/expression.params ../studies/%/expression
	STUDY_ID=$* DATA_LOCATION="$(realpath ../studies/$*/expression)" \
			KETTLE_HOME="$(realpath kettle-home)" \
			./load_expression.sh $<

load_metabolomics_%: ../studies/%/metabolomics.params ../studies/%/metabolomics
	STUDY_ID=$* DATA_LOCATION="$(realpath ../studies/$*/metabolomics)" \
			KETTLE_HOME="$(realpath kettle-home)" \
			./load_metabolomics.sh $<

load_mirnaqpcr_%: ../studies/%/mirnaqpcr.params ../studies/%/mirnaqpcr
	STUDY_ID=$* DATA_LOCATION="$(realpath ../studies/$*/mirnaqpcr)" \
			KETTLE_HOME="$(realpath kettle-home)" \
			./load_mirnaqpcr.sh $<

load_mirnaseq_%: ../studies/%/mirnaseq.params ../studies/%/mirnaseq
	STUDY_ID=$* DATA_LOCATION="$(realpath ../studies/$*/mirnaseq)" \
			KETTLE_HOME="$(realpath kettle-home)" \
			./load_mirnaseq.sh $<

load_msproteomics_%: ../studies/%/msproteomics.params ../studies/%/msproteomics
	STUDY_ID=$* DATA_LOCATION="$(realpath ../studies/$*/msproteomics)" \
			KETTLE_HOME="$(realpath kettle-home)" \
			./load_msproteomics.sh $<

load_rbm_%: ../studies/%/rbm.params ../studies/%/rbm
	STUDY_ID=$* DATA_LOCATION="$(realpath ../studies/$*/rbm)" \
			KETTLE_HOME="$(realpath kettle-home)" \
			./load_rbm.sh $<

load_rnaseq_%: ../studies/%/rnaseq.params ../studies/%/rnaseq
	STUDY_ID=$* DATA_LOCATION="$(realpath ../studies/$*/rnaseq)" \
			KETTLE_HOME="$(realpath kettle-home)" \
			./load_rnaseq.sh $<

PROTEOMICS_SSM_FILE     ?= proteomics_subject_sample_mapping.txt
PROTEOMICS_CM_FILE      ?= proteomics_column_mapping.txt
PROTEOMICS_DATA_PREFIX  ?= proteomics_data
PROTEOMICS_DATA_FILE    ?= $(PROTEOMICS_DATA_PREFIX).txt
PROTEOMICS_ANNOT_PREFIX ?= proteomics
PROTEOMICS_ANNOT_FILE   ?= $(PROTEOMICS_ANNOT_PREFIX)_annotation.txt

load_proteomics_annotation_%: GPL_ID ?= $*_proteomics
load_proteomics_annotation_%: ../studies/%/proteomics_annotation/$(PROTEOMICS_ANNOT_FILE)
	DATA_LOCATION="$(realpath ../studies/$*/proteomics_annotation)" \
	KETTLE_HOME="$(realpath kettle-home)" \
	./load_proteomics_annotation.sh $< \

load_proteomics_%: ../studies/%/proteomics.params ../studies/%/proteomics/$(PROTEOMICS_SSM_FILE) ../studies/%/proteomics/$(PROTEOMICS_CM_FILE)
	MAP_FILENAME=$(PROTEOMICS_SSM_FILE) \
	COLUMN_MAPPING_FILE=$(PROTEOMICS_CM_FILE) \
	DATA_FILE_PREFIX=$(PROTEOMICS_DATA_PREFIX) \
	STUDY_ID=$* \
	DATA_LOCATION="$(realpath ../studies/$*/proteomics)" \
	KETTLE_HOME="$(realpath kettle-home)" \
	./load_proteomics.sh $<

../studies/%/proteomics/$(PROTEOMICS_DATA_FILE):
	$(error Copy proteomics data into $@)

../studies/%/proteomics/$(PROTEOMICS_ANNOT_FILE): ../studies/%/proteomics/$(PROTEOMICS_DATA_FILE) ../common/generate_proteomics_annotation.groovy
	groovy ../common/generate_proteomics_annotation.groovy -i $< -o $@ -p $*_proteomics

../studies/%/proteomics/$(PROTEOMICS_SSM_FILE): ../studies/%/proteomics/$(PROTEOMICS_DATA_FILE) ../common/generate_proteomics_subject_sample_mapping.groovy
	groovy ../common/generate_proteomics_subject_sample_mapping.groovy -i $< -o $@ -p $*_proteomics -t $*

../studies/%/proteomics/$(PROTEOMICS_CM_FILE): ../studies/%/proteomics/$(PROTEOMICS_DATA_FILE) ../common/generate_proteomics_column_mapping.groovy
	groovy ../common/generate_proteomics_column_mapping.groovy -i $< -o $@

.PHONY: load_proteomics_annotation_% load_proteomics_%
.PRECIOUS: ../studies/%/proteomics/$(PROTEOMICS_DATA_FILE) ../studies/%/proteomics/$(PROTEOMICS_ANNOT_FILE) ../studies/%/proteomics/$(PROTEOMICS_SSM_FILE) ../studies/%/proteomics/$(PROTEOMICS_CM_FILE)

MIRNA_SSM_FILE     ?= mirna_subject_sample_mapping.txt
MIRNA_DATA_PREFIX  ?= mirna_data
MIRNA_DATA_FILE    ?= $(MIRNA_DATA_PREFIX).txt
MIRNA_ANNOT_PREFIX ?= mirna
MIRNA_ANNOT_FILE   ?= $(MIRNA_ANNOT_PREFIX)_annotation.txt

load_mirna_annotation_%: GPL_ID ?= $*_mirna
load_mirna_annotation_%: ../studies/%/mirna.params ../studies/%/mirna ../studies/%/mirna/$(MIRNA_ANNOT_FILE)
	DATA_LOCATION="$(realpath ../studies/$*/mirna)" \
	KETTLE_HOME="$(realpath kettle-home)" \
	./load_mirna_annotation.sh $< \

load_mirna_%: ../studies/%/mirna.params ../studies/%/mirna load_mirna_annotation_% ../studies/%/mirna/$(MIRNA_SSM_FILE)
	MAP_FILENAME=$(MIRNA_SSM_FILE) \
	DATA_FILE_PREFIX=$(MIRNA_DATA_PREFIX) \
	DATA_FILE=$(MIRNA_DATA_FILE) \
	STUDY_ID=$* \
	DATA_LOCATION="$(realpath ../studies/$*/mirna)" \
        KETTLE_HOME="$(realpath kettle-home)" \
	./load_mirna.sh $<

../studies/%/mirna:
	$(info Copy mirna data files into $*)

../studies/%/mirna/$(MIRNA_ANNOT_FILE): ../studies/%/mirna ../common/generate_mirna_data_annot_files.groovy
	groovy ../common/generate_mirna_data_annot_files.groovy -i $< -a $@ -d $</$(MIRNA_DATA_FILE) -p $*_mirna

../studies/%/mirna/$(MIRNA_SSM_FILE): ../studies/%/mirna ../common/generate_mirna_subject_sample_mapping.groovy
	groovy ../common/generate_mirna_subject_sample_mapping.groovy -i $< -o $@ -p $*_mirna -t $*

.PHONY: load_mirna_annotation_% load_mirna_%
.PRECIOUS: ../studies/%/mirna/$(MIRNA_DATA_FILE) ../studies/%/mirna/$(MIRNA_ANNOT_FILE) ../studies/%/mirna/$(MIRNA_SSM_FILE)

parse_vcf_%: ../studies/%/vcf.params ../studies/%/vcf
	STUDY_ID=$* DATA_LOCATION="$(realpath ../studies/$*/vcf)" \
			../common/_scripts/vcf/parse_vcf.sh $<

load_vcf_%: ../studies/%/vcf.params parse_vcf_%
	STUDY_ID=$* PSQL='$(PSQL_COMMAND) -d $(PGDATABASE)'\
			DATA_LOCATION="$(realpath ../studies/$*/vcf)" \
			./load_vcf.sh $<

load_samples_%: ../studies/%/samples.params
	STUDY_ID=$* PSQL='$(PSQL_COMMAND) -d $(PGDATABASE)' \
	DATA_LOCATION="$(realpath ../studies/$*/samples) \"
	./load_samples.sh $<

load_study_%: ../studies/%/study.params ../studies/%/load.sh
	STUDY_ID=$* PSQL='$(PSQL_COMMAND) -d $(PGDATABASE)' \
	cd "$(realpath ../studies/$*) \"
	./load.sh

clean:
	rm -f logs/*.log kettle-home/kettle.properties

.PHONY: clean

.PHONY: parse_vcf
../common/parse_vcf:
	perl ../common/_scripts/vcf/parseVCFintoTextFiles.pl "$(VCF_FILE)" "$(VCF_TEMP_DIR)" "$(DATASOURCE)" \
			"$(DATASET_ID)" "$(GPL_ID)" "$(GENOME_BUILD)" "$(ETL_USER)"
	perl ../common/_scripts/vcf/convertMappingIntoSQLFiles.pl "$(SUBJECT_SAMPLE_MAPPING_FILE)" "$(VCF_TEMP_DIR)" \
			"$(STUDY_ID)" "$(DATASET_ID)" "$(CONCEPT_PATH)"

# transmart-batch:
ifdef USE_TRANSMART_BATCH

EXTRA_TSBATCH_ARGS ?=

define TSBATCH_TEMPLATE
 load_$(1)_%: ../studies/%/$(1).params ../studies/%/$(1)
	$$(MAKE) -C ../../env transmart-batch-capsule.jar $$(BATCH_PROPS)
	../../env/transmart-batch-capsule.jar \
		-p '$$(realpath ../studies)/$$*/$(1).params' \
		-c '$$(realpath ../../env)/$$(BATCH_PROPS)' \
		-d 'STUDY_ID=$$*' \
		$$(EXTRA_TSBATCH_ARGS)
endef

else

TSBATCH_TEMPLATE =

endif

$(eval $(call TSBATCH_TEMPLATE,clinical))
$(eval $(call TSBATCH_TEMPLATE,annotation))
$(eval $(call TSBATCH_TEMPLATE,tags))
$(eval $(call TSBATCH_TEMPLATE,expression))
$(eval $(call TSBATCH_TEMPLATE,metabolomics_annotation))
$(eval $(call TSBATCH_TEMPLATE,metabolomics))
$(eval $(call TSBATCH_TEMPLATE,proteomics_annotation))
$(eval $(call TSBATCH_TEMPLATE,proteomics))
$(eval $(call TSBATCH_TEMPLATE,gwas))
$(eval $(call TSBATCH_TEMPLATE,backout))

# vim: ft=make :
