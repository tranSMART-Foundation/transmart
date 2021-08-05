THIS_SCHEMA := i2b2pm
include $(COMMON_DIR)/makefile_schemas.inc

load: load_pm_hive_data load_pm_project_user_roles load_pm_role_requirement load_pm_user_data
.PHONY: load

dump: dump_pm_hive_data dump_pm_project_user_roles dump_pm_role_requirement dump_pm_user_data
.PHONY: dump
