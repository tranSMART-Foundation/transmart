LIB_DIR          := $(dir $(lastword $(MAKEFILE_LIST)))

# PostgreSQL driver 21-mar-23
# https://jdbc.postgresql.org/
# ----------------------------

JDBC_DL_PSQL     := https://jdbc.postgresql.org/download
JDBC_DRIVER_PSQL := postgresql-42.7.2.jar

JDBC_DRIVER_PSQL_PATH := $(LIB_DIR)$(JDBC_DRIVER_PSQL)

# Oracle driver 07-apr-23
# https:/repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/
# https:/repo1.maven.org/maven2/com/oracle/database/xml/xdb/
# -------------------------------------------------------------------

JDBC_DLO_ORA      := https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/23.2.0.0
JDBC_DLX_ORA      := https://repo1.maven.org/maven2/com/oracle/database/xml/xdb/23.2.0.0
JDBC_DRIVER_ORA  := ojdbc8-23.2.0.0.jar
JDBC_XDB_ORA     := xdb-23.2.0.0.jar

JDBC_DRIVER_ORA_PATH  := $(LIB_DIR)$(JDBC_DRIVER_ORA)
JDBC_XDB_ORA_PATH     := $(LIB_DIR)$(JDBC_XDB_ORA)

CP_ORA           := $(LIB_DIR)$(JDBC_DRIVER_ORA):$(LIB_DIR)$(JDBC_XDB_ORA):$(LIB_DIR)
CP_PSQL          := $(LIB_DIR)$(JDBC_DRIVER_PSQL):$(LIB_DIR)

$(JDBC_DRIVER_PSQL_PATH):
	curl -f $(JDBC_DL_PSQL)/$(JDBC_DRIVER_PSQL) > $@

# For correctness, the dependency on XDB should be split off since some uses of this rule don't need XDB, but it doesn't matter a lot.
$(JDBC_DRIVER_ORA_PATH): $(JDBC_XDB_ORA_PATH)
	curl -f $(JDBC_DLO_ORA)/$(JDBC_DRIVER_ORA) > $@

$(JDBC_XDB_ORA_PATH):
	curl -f $(JDBC_DLX_ORA)/$(JDBC_XDB_ORA) > $@

# Used by The Hyve (maybe for development). This version has log4j and protobuf vulnerabilities.
# Version 4 is alpha on maven
# Version 5 on netty project github
# =================================

# Not used by current codebase.
# NETTY_JAR        := netty-3.10.6.Final.jar
# NETTY_JAR_DL     := https://mvnrepository.com/artifact/io.netty/netty/
# NETTY_JAR_PATH   := $(LIB_DIR)$(NETTY_JAR)

# $(NETTY_JAR_PATH):
#	curl -f $(NETTY_JAR_DL)/$(NETTY_JAR) > $@

# SchemaSpy 21-jul-23
# No sign of active development
# =============================

SCHEMA_SPY_VERSION=6.2.4
SCHEMA_SPY_JAR=schemaSpy_$(SCHEMA_SPY_VERSION).jar
SCHEMA_SPY_JAR_PATH  := $(LIB_DIR)$(SCHEMA_SPY_JAR)
$(SCHEMA_SPY_JAR_PATH):
	curl -L -f "https://github.com/schemaspy/schemaspy/releases/download/v6.2.4/schemaspy-6.2.4.jar" > $@

.DELETE_ON_ERROR:

# vim: ft=make list
