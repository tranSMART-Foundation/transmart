include ../makefile.inc

include ../lib/makefile.inc

SOLR_DL=https://archive.apache.org/dist/lucene/solr
SOLR_VERSION=4.5.0
SOLR_PREFIX ?= $(realpath .)
SOLR_LOG ?= $(realpath .)/solr.log

ifdef ORACLE
  JDBC_DRIVER=$(JDBC_DRIVER_ORA)
  JDBC_DRIVER_PATH=$(JDBC_DRIVER_ORA_PATH)
  DBSUBDIR=oracle
else
  JDBC_DRIVER=$(JDBC_DRIVER_PSQL)
  JDBC_DRIVER_PATH=$(JDBC_DRIVER_PSQL_PATH)
  DBSUBDIR=postgres
endif

solr-$(SOLR_VERSION).tgz:
	curl --silent --show-error -f '$(SOLR_DL)/$(SOLR_VERSION)/solr-$(SOLR_VERSION).tgz' -o $@

ROOT_FILES=contrib dist
EXAMPLE_FILES=etc contexts lib logs resources solr-webapp \
			  webapps README.txt start.jar

$(ROOT_FILES): solr-$(SOLR_VERSION).tgz
	$(TAR_COMMAND) xzf $< --touch --strip-components=1 solr-$(SOLR_VERSION)/$@

$(EXAMPLE_FILES): solr-$(SOLR_VERSION).tgz
	$(TAR_COMMAND) xzf $< --touch --strip-components=2 solr-$(SOLR_VERSION)/example/$@

DRIVER=contrib/dataimporthandler/lib/$(JDBC_DRIVER)
$(DRIVER): $(JDBC_DRIVER_PATH)
	test -d contrib || $(MAKE) contrib
	cp --reflink=auto "$<" "$@"

solr: solr-$(SOLR_VERSION).tgz
	$(TAR_COMMAND) xzf $< --touch --strip-components=2 \
		--exclude solr-$(SOLR_VERSION)/example/solr/collection1 \
		solr-$(SOLR_VERSION)/example/solr

solr/collection1:
	test -d solr || $(MAKE) solr #don't add dep because solr is touched on each run
	$(TAR_COMMAND) xzf solr-$(SOLR_VERSION).tgz --touch --strip-components=3 \
		-C solr solr-$(SOLR_VERSION)/example/solr/collection1
	rm $@/core.properties #so solr doesn't see the dir as core

CORE_TARGETS=solr/<core_name> solr/<core_name>/conf/schema.xml solr/<core_name>/conf/data-config.xml
FOUND_CORES=$(patsubst data-config/$(DBSUBDIR)/data-config_%.xml.php,\
            %,\
            $(wildcard data-config/$(DBSUBDIR)/data-config_*.xml.php))
FOUND_CORE_TARGETS=$(foreach corename,$(FOUND_CORES),\
        $(subst <core_name>,$(corename),$(CORE_TARGETS)))
#$(info targets: $(FOUND_CORE_TARGETS))
FOUND_CORE_DIRS=$(foreach corename,$(FOUND_CORES),solr/$(corename))
FULL_IMPORT_TARGETS=$(foreach corename,$(FOUND_CORES),$(corename)_full_import)
DELTA_IMPORT_TARGETS=$(foreach corename,$(FOUND_CORES),$(corename)_delta_import)

$(FOUND_CORE_DIRS): solr/collection1 solrconfig.patch
	rsync -a $</* $@
	echo "name=$(notdir $@)" > $@/core.properties
	rm $@/conf/schema.xml
	cd $@/conf && patch -p0 < $(realpath solrconfig.patch)

solr/%/conf/schema.xml: solr/% schemas/schema_%.xml
	test -d solr/$* || $(MAKE) solr/$* #solr/$* is touched on each solr run
	cp schemas/schema_$*.xml $@

solr/%/conf/data-config.xml: data-config/$(DBSUBDIR)/data-config_%.xml.php
	test -d solr/$* || $(MAKE) solr/$* #solr/$* is touched on each solr run
	php -d variables_order=E $< > $@

solr_home: $(EXAMPLE_FILES) $(DRIVER) $(FOUND_CORE_TARGETS) $(ROOT_FILES)

start: solr_home
	java -jar start.jar

$(FULL_IMPORT_TARGETS):
	curl -f "http://localhost:8983/solr/$(patsubst %_full_import,%,$@)/dataimport?command=full-import&clean=true"

$(DELTA_IMPORT_TARGETS):
	curl -f "http://localhost:8983/solr/$(patsubst %_delta_import,%,$@)/dataimport?command=delta-import"

# must be run as root
$(SOLR_LOG):
	mkdir -p '$(dir $@)'
	touch '$@'
	chown $(SOLR_USER).$(SOLR_USER) '$@'

write_default_solr:
	@test -n '$(SOLR_USER)' || { \
		echo 'Define $$SOLR_USER' 2>&1 ; false; }
	echo 'SOLR_USER=$(SOLR_USER)' > /etc/default/solr
	echo 'SOLR_LOG="$(SOLR_LOG)"' >> /etc/default/solr

install_solr_init: /etc/init.d/solr
/etc/init.d/solr: write_default_solr $(SOLR_LOG)
	SOLR_PREFIX='$(SOLR_PREFIX)' SOLR_LOG=$(SOLR_LOG) php \
		-d variables_order=E -d open_basedir=/ \
		solr.php > $@
	chmod +x '$@'

install_solr_unit: /etc/systemd/system/solr.service
/etc/systemd/system/solr.service: $(SOLR_LOG)
	SOLR_PREFIX='$(SOLR_PREFIX)' SOLR_LOG="file:$(SOLR_LOG)" \
		SOLR_USER=$(SOLR_USER) php \
		-d variables_order=E -d open_basedir=/ solr.service.php > $@
	systemctl daemon-reload


clean:
	rm -rf $(EXAMPLE_FILES) $(ROOT_FILES) solr solr-$(SOLR_VERSION).tgz

clean_cores:
	find solr -maxdepth 1 -mindepth 1 -type d ! -name bin ! -name collection1 -exec rm -r {} \;

.PHONY: start solr_home clean solr/%/conf/data-config.xml write_default_solr
