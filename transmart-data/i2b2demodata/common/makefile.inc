# to be included from ../{oracle,postgres}/Makefile

SELF_DIR := $(dir $(lastword $(MAKEFILE_LIST)))
include $(SELF_DIR)/../../makefile.inc
DEMOCOMMON_DIR := $(realpath $(SELF_DIR)/../common)
ENV_DIR := $(realpath $(SELF_DIR)/../../env)

TABLE_FILES      := $(wildcard $(DEMOCOMMON_DIR)/*_list)
APPEND_DIRS      := $(patsubst %_list,%,$(APPEND_FILES))
SCHEMA_DIRS      := $(patsubst %_list,%,$(TABLE_FILES))
SCHEMA_DIRS_DBMS := $(notdir $(SCHEMA_DIRS))
DUMP_TARGETS     := $(foreach schema,$(notdir $(SCHEMA_DIRS)),dump_schema_$(schema))
LOAD_TARGETS     := $(foreach schema,$(notdir $(SCHEMA_DIRS)),load_schema_$(schema))
APPEND_TARGETS     := $(foreach schema,$(notdir $(APPEND_DIRS)),append_schema_$(schema))

# for autocompletion
$(DUMP_TARGETS) $(LOAD_TARGETS):

dump: $(DUMP_TARGETS)

load: $(LOAD_TARGETS)

append: $(APPEND_TARGETS)

$(foreach schema,$(SCHEMA_DIRS_DBMS),$(schema)/Makefile):

$(DEMOCOMMON_DIR)/%/makefile.inc:
	$(MAKE) -C $(DEMOCOMMON_DIR) $*/makefile.inc

.PRECIOUS: $(DEMOCOMMON_DIR)/%/makefile.inc

%/Makefile: $(DEMOCOMMON_DIR)/%/makefile.inc
	mkdir -p $*
	printf 'DEMOCOMMON_DIR := %s\nTHIS_SCHEMA := %s\ninclude ../makefile.inc\n' \
        "$(DEMOCOMMON_DIR)" $* > $@
	echo 'include ../../common/$*/makefile.inc' >> $@

dump_schema_%: %/Makefile
	$(MAKE) -C $* dump

load_schema_%: %/Makefile
	$(MAKE) -C $* load

append_schema_%: %/Makefile
	$(MAKE) -C $* append

.PHONY: dump_schema_% load_schema_% append_schema_%
.PRECIOUS: $(foreach d,$(TABLE_DIRS),$d)

