THIS_SCHEMA := i2b2imdata
include $(DEMOCOMMON_DIR)/makefile_schemas.inc

load: load_im_mpi_demographics load_im_mpi_mapping load_im_project_patients load_im_project_sites
.PHONY: load
