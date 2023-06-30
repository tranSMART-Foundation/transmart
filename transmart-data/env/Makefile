# Kettle (Pentaho data integration community edition) 9.3 07-may-22
# No longer on SourceForge servers 
#KETTLE_HOST ?= http://downloads.sourceforge.net/project/pentaho

#KETTLE_VERSION=9.2
#KETTLE_ZIP=9.2.0.0-290

# verson 9.3 is the latest supported version
KETTLE_VERSION=9.3
KETTLE_ZIP=9.3.0.0-428
KETTLE_HOST ?= https://privatefilesbucket-community-edition.s3.us-west-2.amazonaws.com

# version 9.4 has completely different configuration and is not yet working with transmart ETL
#KETTLE_VERSION=9.4
#KETTLE_ZIP=9.4.0.0-343

TRANSMART_LIBRARY ?= http://library.transmartfoundation.org/release

TRANSMART_RELEASE_BRANCH ?= release-19.1
TRANSMART_RELEASE_DIR ?= release19_1_0

UBUNTU_PACKAGES=make git rsync libcairo-dev curl gnupg \
				tar gfortran g++ unzip libreadline-dev \
				libxt-dev libpango1.0-dev libprotoc-dev \
				texlive-fonts-recommended tex-gyre texlive-latex-extra liblz4-tool pv zip

CENTOS7_PACKAGES=postgresql tomcat make git rsync \
			gcc gcc-gfortran cpp gcc-c++ \
			readline-devel libXt-devel zlib-devel bzip2-devel \
			xz-devel pcre2-devel libcurl-devel cairo-devel libxml2-devel \
			openssl-devel

UBUNTU18_PACKAGES=postgresql-14 php7.2-cli php7.2-json openjdk-8-jdk openjdk-8-jre tomcat8 \
		libbz2-dev liblzma-dev libcurl4-openssl-dev libjpeg-dev libxml2-dev libssl-dev libpcre2-dev

UBUNTU20_PACKAGES=php7.4-cli php7.4-json openjdk-8-jdk openjdk-8-jre \
		libbz2-dev liblzma-dev libcurl4-openssl-dev libjpeg-dev libxml2-dev libssl-dev libpcre2-dev

# Latest version of groovy-binary for Java 8+ 

GROOVY_VERSION=3.0.9

APTPSQL_KEY=ACCC4CF8.asc

GROOVY_ZIP=apache-groovy-binary-$(GROOVY_VERSION).zip
GROOVY_URL=https://groovy.jfrog.io/artifactory/dist-release-local/groovy-zips/$(GROOVY_ZIP)
GROOVY_BIN=groovy-$(GROOVY_VERSION)/bin/groovy

PHP_VERSION=5.5.13
PHP_TARBALL=php-$(PHP_VERSION).tar.gz
PHP_SRCDIR=php-$(PHP_VERSION)
PHP_URL=http://nl1.php.net/distributions/php-$(PHP_VERSION).tar.gz
PHP_BIN=php5/bin/php

TRANSMART_LOADER=transmart-loader

# 1.1 is the latest tranSMART version for transmart-batch
# Others are 17.1 for the tranSMART-Pro project

TRANSMART_BATCH_FILE=transmart-batch-capsule.jar
TRANSMART_BATCH_URL=https://repo.thehyve.nl/service/local/artifact/maven/content?r=snapshots&g=org.transmartproject&a=transmart-batch&c=capsule&v=1.1-SNAPSHOT

include ../lib/makefile.inc

%/conf/log4j.properties:
	mkdir -p $*/conf \
	&& echo "log4j.rootLogger=INFO, console" > $@ \
	&& echo "log4j.appender.console=org.apache.log4j.ConsoleAppender" >> $@ \
	&& echo "log4j.appender.console.layout=org.apache.log4j.PatternLayout" >> $@ \
	&& echo "log4j.appender.console.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n" >> $@

TRANSMART_LOADER_SNAPSHOT_NAME=loader-jar-with-dependencies.jar
LATEST_TRANSMART_LOADER_SNAPSHOT_URL=https://ci.ctmmtrait.nl/artifact/TM-TLOAD/shared/build-latest/transmart-loader/target/$(TRANSMART_LOADER_SNAPSHOT_NAME)

# only load the snapshot if the current one is 24 hours old (or older)
# use FORCE_TS_LOADER_DOWNLOAD=1 to override
$(TRANSMART_LOADER)-latest-bamboo-snapshot: $(TRANSMART_LOADER)-latest-bamboo-snapshot/conf/log4j.properties
	if test "$$(find $@/$(TRANSMART_LOADER_SNAPSHOT_NAME) -mtime +1 2> /dev/null)" \
		-o "$$FORCE_TS_LOADER_DOWNLOAD" = 1 \
		-o ! -f $@/$(TRANSMART_LOADER_SNAPSHOT_NAME); then \
		curl --location --silent --show-error $(LATEST_TRANSMART_LOADER_SNAPSHOT_URL) > $@/$(TRANSMART_LOADER_SNAPSHOT_NAME); \
	fi

# do a conditional request here.
# We could also skip if we checked in the last hour or so (see ts-loader) below
$(TRANSMART_BATCH_FILE):
	curl --silent --location --show-error --fail --remote-time --time-cond '$@' -o '$@' '$(TRANSMART_BATCH_URL)'
	@chmod +x '$@'

batchdb-psql.properties: batchdb-psql.properties.php
	php -d variables_order=E '$<' > '$@'

batchdb-oracle.properties: batchdb-oracle.properties.php
	php -d variables_order=E '$<' > '$@'

# for redownload
.PHONY: $(TRANSMART_BATCH_FILE) batchdb-psql.properties batchdb-oracle.properties

$(GROOVY_ZIP):
	curl --location --silent --show-error "$(GROOVY_URL)" > $@

$(GROOVY_BIN): $(GROOVY_ZIP)
	unzip -q $<
	touch $@

groovy: $(GROOVY_BIN)
	ln -sf $< $@

$(PHP_TARBALL):
	curl --location --silent --show-error '$(PHP_URL)' > $@

$(PHP_SRCDIR)/.keep: $(PHP_TARBALL)
	tar -xf '$<'
	touch '$@'

$(PHP_BIN): $(PHP_SRCDIR)/.keep
	cd $(PHP_SRCDIR) && ./configure --prefix='$(realpath .)/php5' --disable-all --enable-json
	$(MAKE) -j8 -C '$(PHP_SRCDIR)' install

php: $(PHP_BIN)
	ln -s $< $@

install_ubuntu_packages18:
	. /etc/os-release; \
	echo "deb http://apt.postgresql.org/pub/repos/apt $${UBUNTU_CODENAME}-pgdg main" > /etc/apt/sources.list.d/pgdg.list
	curl --silent --show-error https://www.postgresql.org/media/keys/$(APTPSQL_KEY) | apt-key add -
	apt-get update
	apt-get install -y $(UBUNTU18_PACKAGES)

install_ubuntu_packages20:
	apt-get update
	apt-get install -y $(UBUNTU20_PACKAGES)

install_ubuntu_packages:
	apt-get update
	apt-get install -y $(UBUNTU_PACKAGES)

install_centos_package7:
	yum update
	yum -y $(CENTOS7_PACKAGES)

# <directory> <repos> <branch>
update_repos = if [ ! -d $(1) ]; then \
	git clone -b $(3) '$(2)' $(1); \
	else cd $(1) && git pull; fi

# Note: in the release case - we are making a strong assumption that transmart-etl
#   already exists, is NOT a git archive, and can not be updated
update_etl:
	echo "for release build transmart-etl is not automatically updated from git, because it is assumed to be already loaded from a zip file"

update_etl_git: transmart-etl-$(TRANSMART_RELEASE_BRANCH).zip
	test -d transmart-etl || unzip -qu transmart-etl-$(TRANSMART_RELEASE_BRANCH).zip
	test -d transmart-etl || mv transmart-etl-$(TRANSMART_RELEASE_BRANCH) transmart-etl

transmart-etl-$(TRANSMART_RELEASE_BRANCH).zip:
	curl --location --silent --show-error --fail "$(TRANSMART_LIBRARY)/$(TRANSMART_RELEASE_DIR)_artifacts/transmart-etl-$(TRANSMART_RELEASE_BRANCH).zip" > transmart-etl-$(TRANSMART_RELEASE_BRANCH).zip

.PHONY: update_repos, $(TRANSMART_LOADER)-from-file-server, $(TRANSMART_LOADER)-latest-bamboo-snapshot

# Note kettle to 9.0 had a space in the directory, 9.1 onward has a dash
KETTLE_ARCHIVE=pdi-ce-$(KETTLE_ZIP).zip 
$(KETTLE_ARCHIVE):
	curl --location --silent --show-error --fail "$(KETTLE_HOST)/$(KETTLE_ZIP)/ce/client-tools/$@" > $@
#	curl --location --silent --show-error --fail "$(KETTLE_HOST)/Pentaho%20$(KETTLE_VERSION)/client-tools/$@" > $@

## Updated for 9.1+: Pentaho no longer has libext directory
KETTLE_ORACLE_DRIVER=data-integration/lib/$(JDBC_DRIVER_ORA)
$(KETTLE_ORACLE_DRIVER): $(JDBC_DRIVER_ORA_PATH) data-integration/.keep
	cp --reflink=auto "$<" "$@"

# Unzipping. 9.4+ no longer has configuration files to raise log4j2.rootLogger.level

data-integration-oracle: data-integration/.keep $(KETTLE_ORACLE_DRIVER)
.PHONY: data-integration-oracle
data-integration-oracle/.keep: $(KETTLE_ARCHIVE)
	unzip -q $<
# 9.4 version
#	sed -i'' 's/<ThresholdFilter level="INFO"\/>/<ThresholdFilter level="WARN"\/> <!-- updated for tranSMART -->/g' data-integration/classes/log4j2.xml
	sed -i'' 's/log4j2.rootLogger.level = INFO/log4j2.rootLogger.level = WARN # updated for tranSMART/g' data-integration/system/karaf/etc/org.ops4j.pax.logging.cfg
	touch '$@'

data-integration: data-integration/.keep
.PHONY: data-integration
data-integration/.keep: $(KETTLE_ARCHIVE)
	unzip -q $<
# 9.4 version
#	sed -i'' 's/<ThresholdFilter level="INFO"\/>/<ThresholdFilter level="WARN"\/> <!-- updated for tranSMART -->/g' data-integration/classes/log4j2.xml
	sed -i'' 's/log4j2.rootLogger.level.*INFO/log4j2.rootLogger.level = WARN # updated for tranSMART/g' data-integration/system/karaf/etc/org.ops4j.pax.logging.cfg
	touch '$@'

/var/lib/postgresql/tablespaces:

	/bin/bash -c 'mkdir -p $@/{indx,transmart,i2b2,i2b2_index}'
	chown -R postgres:postgres $@
	chmod -R 700 $@
	chmod go+rx $@

../vars: vars-ubuntu.php
	php -d variables_order=E $< > $@

ubuntu_deps_root: install_ubuntu_packages install_ubuntu_packages /var/lib/postgresql/tablespaces

ubuntu_deps_root14: install_ubuntu_packages install_ubuntu_packages14 /var/lib/postgresql/tablespaces

ubuntu_deps_root16: install_ubuntu_packages install_ubuntu_packages16 /var/lib/postgresql/tablespaces

ubuntu_deps_root18: install_ubuntu_packages install_ubuntu_packages18 /var/lib/postgresql/tablespaces

# Additional dependencies: KETTLE, groovy, create initial transmart-data/vars file

ubuntu_deps_regular: data-integration ../vars groovy

ubuntu_deps_regular14: data-integration ../vars groovy

ubuntu_deps_regular16: data-integration ../vars groovy

ubuntu_deps_regular18: data-integration ../vars groovy

ubuntu_deps_regular20: data-integration ../vars groovy

.DELETE_ON_ERROR:

# vim: ai ts=4 sw=4 noet:
