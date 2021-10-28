COMMON_DIR := /home/transmart/transmart/transmart-data/data/common
THIS_SCHEMA := biomart_user
include ../makefile.inc
include ../../common/biomart_user/makefile.inc


read_cms_file: $(JDBC_DRIVER_PSQL_PATH)
		groovy -cp '$(CP_PSQL)' read_cms_file.groovy
