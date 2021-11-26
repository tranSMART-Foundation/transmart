THIS_SCHEMA := biomart_user
include $(COMMON_DIR)/makefile_schemas.inc

load: load_cms_file load_cms_section
.PHONY: load

dump: dump_cms_file dump_cms_section
.PHONY: dump
