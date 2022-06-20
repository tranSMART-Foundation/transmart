PGHOST=localhost
PGPORT=5432
PGDATABASE=transmart
PGUSER=transmartadmin
PGPASSWORD=transmart

PGSQL_BIN="/usr/bin/"
TABLESPACES=/var/lib/postgresql/tablespaces/
TRANSMARTDATA=<?= realpath("../"), "\n" ?>
KETTLE_JOBS_PSQL=<?= realpath(__DIR__), "/transmart-etl/Kettle/postgres/Kettle-ETL/", "\n" ?>
R_JOBS_PSQL=<?= realpath(__DIR__), "/transmart-etl/Kettle/postgres/R/", "\n" ?>
KITCHEN=<?= realpath(__DIR__), "/data-integration/kitchen.sh", "\n" ?>

PATH=<?= realpath(__DIR__) ?>:$PATH

export PGHOST PGPORT PGDATABASE PGUSER PGPASSWORD TABLESPACES PGSQL_BIN \
	R_JOBS_PSQL KETTLE_JOBS KETTLE_JOBS_PSQL KETTLE_JOBS_ORA KITCHEN TSUSER_HOME ORAHOST ORAPORT \
	ORASID ORASVC ORAUSER ORAPASSWORD ORACLE_MANAGE_TABLESPACES TRANSMARTDATA \
	ORACLE_TABLESPACES_DIR ORACLE
