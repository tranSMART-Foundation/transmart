ifeq ($(THIS_SCHEMA), searchapp)
#special case
load_plugin_module: load_plugin update_plugin_module
load: load_plugin_module

update_plugin_module:
	$(MAKE) -C ../../common/searchapp/plugin_modules_params ../plugin_module.tsv

.PHONY: update_plugin_module
endif

ifeq ($(THIS_SCHEMA), biomart_user)
#special case
ifdef ORACLE
  JDBC_DRIVER=$(JDBC_DRIVER_ORA)
  JDBC_DRIVER_PATH=$(JDBC_DRIVER_ORA_PATH)
  CP_PATH= $(LIB_DIR)$(JDBC_DRIVER_ORA):$(LIB_DIR)$(JDBC_XDB_ORA):$(LIB_DIR)
else
  JDBC_DRIVER=$(JDBC_DRIVER_PSQL)
  JDBC_DRIVER_PATH=$(JDBC_DRIVER_PSQL_PATH)
  CP_PATH=$(LIB_DIR)$(JDBC_DRIVER_PSQL):$(LIB_DIR)
endif

load: read_cms_file

read_cms_file: load_cms_file $(JDBC_DRIVER_PATH)
		groovy -cp '$(CP_PATH)' read_cms_file.groovy

endif

ifdef THIS_SCHEMA
SCHEMA_COMP := '$(THIS_SCHEMA)'
else
SCHEMA_COMP := IN ('amapp', 'biomart', 'biomart_user', 'deapp', 'searchapp', 'i2b2demodata', 'i2b2hive', 'i2b2metadata', 'i2b2pm')
endif

