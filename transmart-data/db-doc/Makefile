include ../lib/makefile.inc

# need graphviz installed for older versions
# added -vizjs to use javascript to generate diagrams

generate_postgresql: $(SCHEMA_SPY_JAR_PATH) $(JDBC_DRIVER_PSQL_PATH)
	java -jar $(SCHEMA_SPY_JAR_PATH) -t pgsql -dp $(JDBC_DRIVER_PSQL_PATH) -db $(PGDATABASE) -host $(if $(PGHOST),$(if $(findstring /tmp,$(PGHOST)),localhost,$(PGHOST)),localhost)$(if $(PGPORT),:$(PGPORT),) -u $(PGUSER) $(if $(PGPASSWORD),-p $(PGPASSWORD),) -hq -norows -o postgres -all -schemaSpec "(biomart.*)|(.*app)|(i2b2.*)|(gwas.*)|(tm_.*)|(galaxy.*)|(ts_.*)" -ahic -vizjs

generate_oracle: $(SCHEMA_SPY_JAR_PATH) $(JDBC_DRIVER_ORA_PATH)
	java -jar $(SCHEMA_SPY_JAR_PATH) -t ora -dp $(JDBC_DRIVER_ORA_PATH) -db $(ORASID) -host $(ORAHOST) -u $(ORAUSER) $(if $(ORAPASSWORD),-p $(ORAPASSWORD),) -norows -o oracle -all  -ahic -vizjs
