# Kettle (Pentaho data integration community edition) 07-sep-20
KETTLE_VERSION=9.1
KETTLE_ZIP=9.1.0.0-324

TRANSMART_RELEASE_BRANCH=release-19.1

UBUNTU_PACKAGES=postgresql make git rsync libcairo-dev curl \
				tar gfortran g++ unzip libreadline-dev \
				libxt-dev libpango1.0-dev libprotoc-dev \
				texlive-fonts-recommended tex-gyre liblz4-tool pv zip

CENTOS7_PACKAGES=postgresql tomcat make git rsync \
			gcc gcc-gfortran cpp gcc-c++ \
			readline-devel libXt-devel zlib-devel bzip2-devel \
			xz-devel pcre2-devel libcurl-devel cairo-devel libxml2-devel \
			openssl-devel

UBUNTU14_PACKAGES=php5-cli php5-json openjdk-8-jdk
UBUNTU16_PACKAGES=php7.0-cli php7.0-json
UBUNTU18_PACKAGES=php7.2-cli php7.2-json openjdk-8-jdk openjdk-8-jre \
		libbz2-dev liblzma-dev libcurl4-openssl-dev libjpeg-dev libxml2-dev libssl-dev libpcre2-dev
UBUNTU20_PACKAGES=php7.4-cli php7.4-json openjdk-8-jdk openjdk-8-jre \
		libbz2-dev liblzma-dev libcurl4-openssl-dev libjpeg-dev libxml2-dev libssl-dev libpcre2-dev

# Latest version of groovy-binary for Java 8+ 

GROOVY_VERSION=3.0.8

ONDREJ_KEY=E5267A6C

GROOVY_ZIP=apache-groovy-binary-$(GROOVY_VERSION).zip
GROOVY_URL=https://groovy.jfrog.io/artifactory/dist-release-local/groovy-zips/$(GROOVY_ZIP)
GROOVY_BIN=groovy-$(GROOVY_VERSION)/bin/groovy

PHP_VERSION=5.5.13
PHP_TARBALL=php-$(PHP_VERSION).tar.gz
PHP_SRCDIR=php-$(PHP_VERSION)
PHP_URL=http://nl1.php.net/distributions/php-$(PHP_VERSION).tar.gz
PHP_BIN=php5/bin/php

TRANSMART_LOADER=transmart-loader

TRANSMART_BATCH_FILE=transmart-batch-capsule.jar
TRANSMART_BATCH_URL=https://repo.thehyve.nl/service/local/artifact/maven/content?r=snapshots&g=org.transmartproject&a=transmart-batch&c=capsule&v=LATEST

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
		curl -L -f $(LATEST_TRANSMART_LOADER_SNAPSHOT_URL) > $@/$(TRANSMART_LOADER_SNAPSHOT_NAME); \
	fi

# do a conditional request here.
# We could also skip if we checked in the last hour or so (see ts-loader) below
$(TRANSMART_BATCH_FILE):
	curl -s -L -f -R -z '$@' -o '$@' '$(TRANSMART_BATCH_URL)'
	@chmod +x '$@'

batchdb-psql.properties: batchdb-psql.properties.php
	php -d variables_order=E '$<' > '$@'

batchdb-oracle.properties: batchdb-oracle.properties.php
	php -d variables_order=E '$<' > '$@'

# for redownload
.PHONY: $(TRANSMART_BATCH_FILE) batchdb-psql.properties batchdb-oracle.properties

$(GROOVY_ZIP):
	curl -L "$(GROOVY_URL)" > $@

$(GROOVY_BIN): $(GROOVY_ZIP)
	unzip -q $<
	touch $@

groovy: $(GROOVY_BIN)
	ln -sf $< $@

$(PHP_TARBALL):
	curl -f '$(PHP_URL)' > $@

$(PHP_SRCDIR)/.keep: $(PHP_TARBALL)
	tar -xf '$<'
	touch '$@'

$(PHP_BIN): $(PHP_SRCDIR)/.keep
	cd $(PHP_SRCDIR) && ./configure --prefix='$(realpath .)/php5' --disable-all --enable-json
	$(MAKE) -j8 -C '$(PHP_SRCDIR)' install

php: $(PHP_BIN)
	ln -s $< $@

install_ubuntu_packages14:
	. /etc/lsb-release; \
		test $$DISTRIB_RELEASE == '14.04' || \
	    grep ondrej/php5 /etc/apt/sources.list || ( \
				gpg --recv-keys --keyserver keyserver.ubuntu.com $(ONDREJ_KEY) && \
				(gpg -a --export $(ONDREJ_KEY) | apt-key add -) && \
				echo 'deb http://ppa.launchpad.net/ondrej/php5/ubuntu precise main' >> /etc/apt/sources.list)
	apt-get update
	apt-get install -y $(UBUNTU14_PACKAGES)

install_ubuntu_packages16:
	apt-get update
	apt-get install -y $(UBUNTU16_PACKAGES)

install_ubuntu_packages18:
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

update_etl_git: transmart-etl-release-19.1.zip
	test -d transmart-etl || unzip -qu transmart-etl-release-19.1.zip
	test -d transmart-etl || mv transmart-etl-release-19.1 transmart-etl

transmart-etl-release-19.1.zip:
	curl -L -f "http://library.transmartfoundation.org/beta/beta19_1_0_artifacts/transmart-etl-release-19.1.zip" > transmart-etl-release-19.1.zip
#	curl -L -f "http://library.transmartfoundation.org/release/release19_1_0_artifacts/transmart-etl-release-19.1.zip" > transmart-etl-release-19.1.zip

.PHONY: update_repos, $(TRANSMART_LOADER)-from-file-server, $(TRANSMART_LOADER)-latest-bamboo-snapshot

KETTLE_ARCHIVE=pdi-ce-$(KETTLE_ZIP).zip 
$(KETTLE_ARCHIVE):
	curl -L -f "http://downloads.sourceforge.net/project/pentaho/Pentaho%20$(KETTLE_VERSION)/client-tools/$@" > $@

## Updated for 9.1: Pentaho no longer has libext directory
KETTLE_ORACLE_DRIVER=data-integration/lib/$(JDBC_DRIVER_ORA)
$(KETTLE_ORACLE_DRIVER): $(JDBC_DRIVER_ORA_PATH) data-integration/.keep
	cp --reflink=auto "$<" "$@"

data-integration-oracle: data-integration/.keep $(KETTLE_ORACLE_DRIVER)
.PHONY: data-integration-oracle
data-integration-oracle/.keep: $(KETTLE_ARCHIVE)
	unzip -q $<
	touch '$@'

data-integration: data-integration/.keep
.PHONY: data-integration
data-integration/.keep: $(KETTLE_ARCHIVE)
	unzip -q $<
	touch '$@'

/var/lib/postgresql/tablespaces:

	/bin/bash -c 'mkdir -p $@/{indx,transmart,i2b2,i2b2_index}'
	chown -R postgres:postgres $@

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
