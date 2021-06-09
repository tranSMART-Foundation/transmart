THIS_SCHEMA := i2b2hive
include $(COMMON_DIR)/makefile_schemas.inc

load: load_hive_cell_params
.PHONY: load

dump: dump_hive_cell_params
.PHONY: dump
