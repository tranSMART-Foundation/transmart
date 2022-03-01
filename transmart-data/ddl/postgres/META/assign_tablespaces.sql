DO $$
DECLARE
    table_name text;
    cur_ts     text;
    index_name text;
    command    text;
    spec       text[][];
    pair       text[];
BEGIN
    RAISE NOTICE 'Started assigning tablespaces';

    spec := ARRAY[
        ['tm_cz',        'transmart', 'indx'      ],
        ['tm_lz',        'transmart', 'indx'      ],
        ['tm_wz',        'transmart', 'indx'      ],
        ['i2b2demodata', 'i2b2',      'i2b2_index'],
        ['i2b2metadata', 'i2b2',      'i2b2_index'],
        ['i2b2hive',     'i2b2',      'i2b2_index'],
        ['i2b2imdata',   'i2b2',      'i2b2_index'],
        ['i2b2pm',       'i2b2',      'i2b2_index'],
        ['i2b2workdata', 'i2b2',      'i2b2_index'],
        ['deapp',        'transmart', 'indx'      ],
        ['searchapp',    'transmart', 'indx'      ],
        ['biomart',      'transmart', 'indx'      ],
        ['biomart_user', 'transmart', 'indx'      ],
        ['biomart_stage','transmart', 'indx'      ],
        ['galaxy',       'transmart', 'indx'      ],
        ['gwas_plink',   'transmart', 'indx'      ],
        ['fmapp',        'transmart', 'indx'      ],
        ['amapp',        'transmart', 'indx'      ],
        ['ts_batch',     'transmart', 'indx'      ]
    ];
    FOREACH pair SLICE 1 IN ARRAY spec LOOP
        -- Assign tables' tablespaces
        FOR table_name, cur_ts IN
                SELECT tablename, tablespace FROM pg_tables WHERE schemaname = pair[1] LOOP
            IF cur_ts = pair[2] THEN
                CONTINUE;
            END IF;

            RAISE NOTICE 'Current tablespace for %.% is %; changing to %',
                    pair[1], table_name, cur_ts, pair[2];

            command = 'ALTER TABLE ' || pair[1] || '.' || quote_ident(table_name) ||
                    ' SET TABLESPACE ' || pair[2];
            EXECUTE(command);
        END LOOP;

        -- Assign indexes' tablespaces
        FOR index_name, cur_ts IN
                SELECT indexname, tablespace FROM pg_indexes WHERE schemaname = pair[1] LOOP
            IF cur_ts = pair[3] THEN
                CONTINUE;
            END IF;

            RAISE NOTICE 'Current tablespace for index % is %; changing to %',
                    index_name, cur_ts, pair[3];

            command = 'ALTER INDEX ' || pair[1] || '.' || quote_ident(index_name) ||
                    ' SET TABLESPACE '|| pair[3];
            EXECUTE(command);
        END LOOP;
    END LOOP;

    RAISE NOTICE 'Finished assigning tablespaces';

END;
$$ LANGUAGE plpgsql;

-- vim: ft=plsql ts=4 sw=4 et:
