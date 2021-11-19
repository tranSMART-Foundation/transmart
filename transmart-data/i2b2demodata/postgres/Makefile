include ../common/makefile.inc

row_counts:
	$(PSQL_COMMAND) -c "SELECT nspname, relname, reltuples \
		FROM pg_class C \
		INNER JOIN pg_namespace N ON (N.oid = C.relnamespace) \
		WHERE relkind = 'r' \
			AND N.nspname ${SCHEMA_COMP} \
		ORDER BY nspname, reltuples DESC"

analyze:
	$(PSQL_COMMAND) -c 'ANALYZE VERBOSE'

.PHONY: row_counts analyze

SHELL := /bin/bash

load_psql = \
	@if [[ "$$($(PSQL_COMMAND) -c "SELECT EXISTS (SELECT * FROM $(1).$(2) LIMIT 1)" -tA)" = 't' ]]; then \
		echo 'WARNING: The table $(1).$(2) already has data; skipped' >&2; \
	else \
		echo 'Loading $(1).$(2)'; \
		pv -pet '$(3)' | lz4c -d | \
		$(PSQL_COMMAND) -c "COPY $(1).$(2)$(4) FROM STDIN \
			CSV DELIMITER E'\t'"; \
	fi

export PSQL_COMMAND

# vim: set filetype=make:
