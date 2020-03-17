--
-- Name: _final_median(anyarray); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz._final_median(anyarray) RETURNS numeric
    LANGUAGE sql IMMUTABLE
AS $$
    select avg(val)
    from (
	select val
	  from unnest($1) val
	 order by 1
	 limit  2 - mod(array_upper($1, 1), 2)
		offset ceil(array_upper($1, 1) / 2.0) - 1
    ) sub;
$$;

--
-- Name: _final_median(double precision[]); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz._final_median(double precision[]) RETURNS double precision
    LANGUAGE sql IMMUTABLE
AS $$
    select avg(val)
    from (
	select val
	  from unnest($1) val
	 order by 1
	 limit  2 - mod(array_upper($1, 1), 2)
		offset ceil(array_upper($1, 1) / 2.0) - 1
    ) sub;
$$;

