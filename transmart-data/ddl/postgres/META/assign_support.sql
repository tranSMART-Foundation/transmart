\set ON_ERROR_STOP on
-- Support definitions for the assign_* scripts
DO $$
<<locals>>
    DECLARE
dummy        record;

BEGIN

    -- check for array_append version
    -- postgres 14+ incompatible with earlier versions
    
    SELECT pg_get_function_arguments(n.oid)
      INTO dummy
      FROM pg_proc p
               JOIN pg_namespace n
		       ON (p.pronamespace = n.oid)
     WHERE n.nspname = 'pg_catalog'
       AND proname = 'array_append'
       AND proargtypes = ARRAY[5078,5077]::oidvector; -- postgres 14 

    IF FOUND THEN
	-- Postgres 14
	-- Make sure we have array_accum(anycompatible) aggregate
	SELECT proname
	INTO dummy
	FROM pg_proc p
	JOIN pg_namespace n
	ON (p.pronamespace = n.oid)
	WHERE n.nspname = 'public'
	AND proname = 'array_accum'
	AND proargtypes = ARRAY[5077]::oidvector; --oid for anycompatible

	IF NOT FOUND THEN
	    CREATE AGGREGATE public.array_accum(anycompatible) (
		sfunc = array_append,
		stype = anycompatiblearray,
		initcond = '{}'
	    );
        END IF;
    ELSE
	-- Postgres 9-13
	-- Make sure we have array_accum(anyelement) aggregate
	SELECT proname
	INTO dummy
	FROM pg_proc p
	JOIN pg_namespace n
	ON (p.pronamespace = n.oid)
	WHERE n.nspname = 'public'
	AND proname = 'array_accum'
	AND proargtypes = ARRAY[2283]::oidvector; --oid for anyelement

	IF NOT FOUND THEN
	    CREATE AGGREGATE public.array_accum(anyelement) (
		sfunc = array_append,
		stype = anyarray,
		initcond = '{}'
	    );
	END IF;
    END IF;

    -- Ensure biomart_write_tables exists
    SELECT relname
      INTO dummy
      FROM pg_class c
     WHERE relname = 'biomart_write_tables';

    IF NOT FOUND THEN
        CREATE TABLE public.biomart_write_tables(
            nschema text,
            ntable text);
    END IF;

    -- Ensure ts_default_permissions exists
    SELECT relname
      INTO dummy
      FROM pg_class c
     WHERE relname = 'ts_default_permissions';

    IF NOT FOUND THEN
        CREATE TABLE public.ts_default_permissions(
            nschema text,
            nuser text,
            ntype text,
            nperm text);
    END IF;

    -- Ensure ts_misc_permissions exists
    SELECT relname
      INTO dummy
      FROM pg_class c
     WHERE relname = 'ts_misc_permissions';

    IF NOT FOUND THEN
        CREATE TABLE public.ts_misc_permissions(
            nschema text,
            nname text,
            ntype text,
            nuser text,
            nperm text);
    END IF;

    -- Make sure we have the schemas_tables_funcs view

    if(exists (select 1 from information_schema.columns where table_name = 'pg_proc' and column_name = 'proisagg')) then

	CREATE OR REPLACE VIEW public.schemas_tables_funcs(
            name,
            kind,
            owner,
            acl,
            nspname,
            change_owner_skip
	) AS (
	    -- tables, views and sequences
	    SELECT
		quote_ident(relname),
		relkind,
		rolname,
		relacl,
		nspname,
		--prevent error "cannot change owner; sequence is linked to table..."
		EXISTS(
                    SELECT 1
                      FROM pg_depend
                     WHERE refobjsubid <> 0 -- dependent is a table
                       AND deptype = 'a' -- dependency is automatic
                       AND pg_depend.objid = c.oid
                     LIMIT 1
		) as change_owner_skip
              FROM
		      pg_class c
		      JOIN pg_namespace n ON (c.relnamespace = n.oid)
		      JOIN pg_roles r ON (c.relowner = r.oid)
             WHERE
            c.relkind IN ('r','S','v')
             ORDER BY c.relkind = 'S'
	) UNION ( -- schemas
		 SELECT
		     nspname,
		     's',
		     rolname,
		     nspacl,
		     nspname,
		     FALSE as change_owner_skip
		   FROM
			   pg_namespace n
			   JOIN pg_roles r ON (n.nspowner = r.oid)
	) UNION (
	    -- tablespaces
            SELECT
		quote_ident(spcname),
		'T',
		NULL,
		spcacl,
		NULL,
		FALSE as change_owner_skip
              FROM
		      pg_tablespace
             WHERE
            spcname NOT LIKE 'pg\_%'
	) UNION (
	    -- functions (including aggregates)
            SELECT
		quote_ident(p.proname) || '(' || array_to_string(
                    (SELECT public.array_accum(typname)
                       FROM ( -- we need this subquery to force the args to the correct order
                             SELECT typname
                               FROM (
				   SELECT Z.id
					  ,p.proargtypes[Z.id]
                                     FROM generate_subscripts(p.proargtypes, 1) AS Z(id)
                               ) AS A(id, oid)
					JOIN pg_type T
						ON (T.oid = A.oid)
			      GROUP BY A.id
				       ,T.typname
			      ORDER BY A.id
                       ) AS Y
                    ),
		    ', ') || ')',
		case proisagg
                    when false then 'f'
                    else 'a'
		end::"char",
		rolname,
		proacl,
		nspname,
		FALSE as change_owner_skip
              FROM
		      pg_proc p
		      JOIN pg_namespace n ON (p.pronamespace = n.oid)
		      JOIN pg_roles r ON (p.proowner = r.oid)
	);

    else

	CREATE OR REPLACE VIEW public.schemas_tables_funcs(
            name,
            kind,
            owner,
            acl,
            nspname,
            change_owner_skip
	) AS (
	    -- tables, views and sequences
	    SELECT
		quote_ident(relname),
		relkind,
		rolname,
		relacl,
		nspname,
		--prevent error "cannot change owner; sequence is linked to table..."
		EXISTS(
                    SELECT 1
                      FROM pg_depend
                     WHERE refobjsubid <> 0 -- dependent is a table
                       AND deptype = 'a' -- dependency is automatic
                       AND pg_depend.objid = c.oid
                     LIMIT 1
		) as change_owner_skip
              FROM
		      pg_class c
		      JOIN pg_namespace n ON (c.relnamespace = n.oid)
		      JOIN pg_roles r ON (c.relowner = r.oid)
             WHERE
            c.relkind IN ('r','S','v')
             ORDER BY c.relkind = 'S'
	) UNION ( -- schemas
		 SELECT
		     nspname,
		     's',
		     rolname,
		     nspacl,
		     nspname,
		     FALSE as change_owner_skip
		   FROM
			   pg_namespace n
			   JOIN pg_roles r ON (n.nspowner = r.oid)
	) UNION (
	    -- tablespaces
            SELECT
		quote_ident(spcname),
		'T',
		NULL,
		spcacl,
		NULL,
		FALSE as change_owner_skip
              FROM
		      pg_tablespace
             WHERE
            spcname NOT LIKE 'pg\_%'
	) UNION (
	    -- functions (including aggregates)
            SELECT
		quote_ident(p.proname) || '(' || array_to_string(
                    (SELECT public.array_accum(typname)
                       FROM ( -- we need this subquery to force the args to the correct order
                             SELECT typname
                               FROM (
				   SELECT Z.id
					  ,p.proargtypes[Z.id]
                                     FROM generate_subscripts(p.proargtypes, 1) AS Z(id)
                               ) AS A(id, oid)
					JOIN pg_type T
						ON (T.oid = A.oid)
			      GROUP BY A.id
				       ,T.typname
			      ORDER BY A.id
                       ) AS Y
                    ),
		    ', ') || ')',
		case prokind
                    when 'a' then 'a'
                    else 'f'
		end::"char",
		rolname,
		proacl,
		nspname,
		FALSE as change_owner_skip
              FROM
		      pg_proc p
		      JOIN pg_namespace n ON (p.pronamespace = n.oid)
		      JOIN pg_roles r ON (p.proowner = r.oid)
	);
    end if;

END;
$$ LANGUAGE plpgsql;

TRUNCATE public.biomart_write_tables;

\COPY public.biomart_write_tables FROM 'biomart_user_write.tsv'

TRUNCATE public.ts_default_permissions;

\COPY public.ts_default_permissions FROM 'default_permissions.tsv'

TRUNCATE public.ts_misc_permissions;

\COPY public.ts_misc_permissions FROM 'misc_permissions.tsv';

-- vim: ft=plsql ts=4 sw=4 et:
