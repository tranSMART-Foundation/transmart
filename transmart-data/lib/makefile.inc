LIB_DIR          := $(dir $(lastword $(MAKEFILE_LIST)))

# PostgreSQL driver 19-apr-21
# https://jdbc.postgresql.org/
# ----------------------------

JDBC_DL_PSQL     := https://jdbc.postgresql.org/download
JDBC_DRIVER_PSQL := postgresql-42.2.20.jar

JDBC_DRIVER_PSQL_PATH := $(LIB_DIR)$(JDBC_DRIVER_PSQL)

# Oracle driver 16-jan-21
# https://mvnrepository.com/artifact/com.oracle.database.jdbc/ojdbc8
# -------------------------------------------------------------------

JDBC_DLO_ORA      := https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/21.1.0.0
JDBC_DLX_ORA      := https://repo1.maven.org/maven2/com/oracle/database/xml/xdb/21.1.0.0
JDBC_DRIVER_ORA  := ojdbc8-21.1.0.0.jar
JDBC_XDB_ORA     := xdb-21.1.0.0.jar

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

NETTY_JAR        := netty-3.5.13.Final.jar
NETTY_JAR_DL     := http://files.thehyve.net
NETTY_JAR_PATH   := $(LIB_DIR)$(NETTY_JAR)

$(NETTY_JAR_PATH):
	curl -f $(NETTY_JAR_DL)/$(NETTY_JAR) > $@

SCHEMA_SPY_VERSION=6.1.0
SCHEMA_SPY_JAR=schemaSpy_$(SCHEMA_SPY_VERSION).jar
SCHEMA_SPY_JAR_PATH  := $(LIB_DIR)$(SCHEMA_SPY_JAR)
$(SCHEMA_SPY_JAR_PATH):
	curl -L -f "https://github.com/schemaspy/schemaspy/releases/download/v6.1.0/schemaspy-6.1.0.jar" > $@

.DELETE_ON_ERROR:

# vim: ft=make list
