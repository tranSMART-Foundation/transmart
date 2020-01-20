THIS_SCHEMA := biomart_user
include $(COMMON_DIR)/makefile_schemas.inc

load: load_application_settings load_cms_file load_cms_section load_gene_ontology load_gene_ontology_data
.PHONY: load

dump: dump_application_settings dump_cms_file dump_cms_section dump_gene_ontology dump_gene_ontology_data
.PHONY: dump
