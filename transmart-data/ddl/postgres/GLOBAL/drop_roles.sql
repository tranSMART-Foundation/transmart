DO $$
DECLARE
    spec    text[];
    role    text;
    command text;
    dummy   record;
BEGIN
    spec := ARRAY[
        'amapp',
        'biomart',
        'biomart_stage',
        'biomart_user',
        'deapp',
        'fmapp',
        'galaxy',
        'gwas_plink',
        'i2b2demodata',
        'i2b2metadata',
        'i2b2hive',
        'i2b2imdata',
        'i2b2pm',
        'i2b2workdata',
        'searchapp',
        'tm_cz',
        'tm_lz',
        'tm_wz',
        'ts_batch'
    ];

    FOREACH role IN ARRAY spec LOOP

        SELECT rolname
        INTO dummy
        FROM pg_roles
        WHERE rolname = role;

        IF NOT FOUND THEN
            CONTINUE;
        END IF;

        command := 'DROP OWNED BY ' || role || ' CASCADE';
        EXECUTE(command);

        command := 'DROP ROLE ' || role;
        EXECUTE(command);

    END LOOP;
END;
$$ LANGUAGE plpgsql;
-- vim: et sw=4 ts=4 filetype=plsql:
