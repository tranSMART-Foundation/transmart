PGHOST=
PGPORT=5432
PGDATABASE=transmart
PGUSER=postgres
PGPASSWORD=

PGSQL_BIN="/usr/bin/"
TABLESPACES=/var/lib/postgresql/tablespaces/
KETTLE_JOBS_PSQL=<?= realpath(__DIR__), "/tranSMART-ETL/Kettle/postgres/Kettle-ETL/", "\n" ?>
R_JOBS_PSQL=<?= realpath(__DIR__), "/tranSMART-ETL/Kettle/postgres/R/", "\n" ?>
KITCHEN=<?= realpath(__DIR__), "/data-integration/kitchen.sh", "\n" ?>

PATH=<?= realpath(__DIR__) ?>:$PATH

export PGHOST PGPORT PGDATABASE PGUSER PGPASSWORD TABLESPACES PGSQL_BIN \
	KETTLE_JOBS_PSQL KETTLE_JOBS R_JOBS_PSQL KITCHEN PATH
