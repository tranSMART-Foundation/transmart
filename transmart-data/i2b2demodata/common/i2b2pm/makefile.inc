THIS_SCHEMA := i2b2pm
include $(DEMOCOMMON_DIR)/makefile_schemas.inc

load: append_pm_user_data load_pm_project_data load_pm_cell_data append_pm_project_user_roles
.PHONY: load
