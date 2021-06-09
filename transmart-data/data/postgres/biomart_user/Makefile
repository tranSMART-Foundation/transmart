COMMON_DIR := /home/pmr/git-master/transmart/transmart-data/data/common
THIS_SCHEMA := biomart_user
include ../makefile.inc
include ../../common/biomart_user/makefile.inc


read_cms_file:
		groovy -cp '$(CP_PSQL)' read_cms_file.groovy
