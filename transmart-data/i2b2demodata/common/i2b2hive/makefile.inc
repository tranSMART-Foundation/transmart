THIS_SCHEMA := i2b2hive
include $(DEMOCOMMON_DIR)/makefile_schemas.inc

load: load_crc_db_lookup load_im_db_lookup load_ont_db_lookup load_work_db_lookup
.PHONY: load
