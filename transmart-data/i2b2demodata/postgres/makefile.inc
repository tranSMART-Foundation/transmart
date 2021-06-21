# to be included from <schema>/Makefile

BASE_FOLDER = ../../..
include $(BASE_FOLDER)/makefile.inc
include $(BASE_FOLDER)/lib/makefile.inc
OUTDIR := ../../common/$(THIS_SCHEMA)

export PSQL_COMMAND

load_%:
	@if [ `$(PSQL_COMMAND) -c "SELECT EXISTS (SELECT * FROM $(THIS_SCHEMA).$* LIMIT 1)" -tA` = 't' ]; then \
		echo 'WARNING: The table $(THIS_SCHEMA).$* already has data; skipped' >&2; \
	else \
		echo 'Loading $(THIS_SCHEMA).$*'; \
		$(PSQL_COMMAND) -c "COPY $(THIS_SCHEMA).$* FROM STDIN \
			CSV DELIMITER E'\t'" < $(OUTDIR)/$*.tsv; \
	fi

append_%:
	@if [ `$(PSQL_COMMAND) -c "SELECT NOT EXISTS (SELECT * FROM $(THIS_SCHEMA).$* LIMIT 1)" -tA` = 't' ]; then \
		echo 'WARNING: The table $(THIS_SCHEMA).$* has no data; skipped' >&2; \
	else \
		echo 'Appending $(THIS_SCHEMA).$*'; \
		$(PSQL_COMMAND) -c "COPY $(THIS_SCHEMA).$* FROM STDIN \
			CSV DELIMITER E'\t'" < $(OUTDIR)/$*.tsv_append; \
	fi

# vim: set ft=make:
