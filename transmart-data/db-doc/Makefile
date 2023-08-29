include ../lib/makefile.inc

# need graphviz installed for older versions
# added -vizjs to use javascript to generate diagrams

# use -t pgsql11 to allow old reference to proisagg for postgres 10
# and earlier in ddl/postgres/META/assign_support.sql
# A simple -t pgsql complains and refuses to document functions/procedures

java11 ?= java

generate_postgresql: $(SCHEMA_SPY_JAR_PATH) $(JDBC_DRIVER_PSQL_PATH)
	$(java11) -jar $(SCHEMA_SPY_JAR_PATH) -t pgsql11 -dp $(JDBC_DRIVER_PSQL_PATH) -db $(PGDATABASE) -host \
		$(if $(PGHOST),$(if $(findstring /tmp,$(PGHOST)),localhost,$(PGHOST)),localhost)$(if $(PGPORT),:$(PGPORT),) -u $(PGUSER) $(if $(PGPASSWORD),-p $(PGPASSWORD),) \
		-hq -norows -o postgres -all -schemaSpec "(biomart.*)|(.*app)|(i2b2.*)|(gwas.*)|(tm_.*)|(galaxy.*)|(ts_.*)" -ahic -vizjs

# was -t ora

generate_oracle: $(SCHEMA_SPY_JAR_PATH) $(JDBC_DRIVER_ORA_PATH)
	$(java11) -jar $(SCHEMA_SPY_JAR_PATH) -t orathin-service -dp $(JDBC_DRIVER_ORA_PATH) -db $(ORASID) -host $(ORAHOST) -u $(ORAUSER) --port $(ORAPORT) $(if $(ORAPASSWORD),-p $(ORAPASSWORD),) \
		-norows -o oracle -all  -ahic -vizjs -cat %
