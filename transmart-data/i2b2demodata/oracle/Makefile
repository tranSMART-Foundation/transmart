include ../common/makefile.inc
include ../../lib/makefile.inc

start_pool: $(NETTY_JAR_PATH) $(JDBC_DRIVER_ORA_PATH)
	NLS_DATE_FORMAT='yyyy-mm-dd hh24:mi:ss' \
		NLS_TIMESTAMP_FORMAT='yyyy-mm-dd hh24:mi:ss.ff' \
		groovy -cp '$(CP_ORA):$(NETTY_JAR_PATH)' start_conn_pool.groovy \
		-f pool-lock

.PHONY: start_pool

# vim: set filetype=make:
