--
-- Name: i2b2_show_node(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_show_node(path character varying) RETURNS void
    LANGUAGE plpgsql
AS $$
begin

    -------------------------------------------------------------
    -- Shows a tree node in i2b2
    -- updates node c_visualattributes (sets to FA, LA)
    -- KCR@20090519 - First Rev
    -------------------------------------------------------------
    if path != ''  or path != '%'
    then

	--i2b2
	update i2b2metadata.i2b2
	set c_visualattributes = 'FA'
	where c_visualattributes like 'F%'
	and c_fullname like path || '%';

	update i2b2metadata.i2b2
	   set c_visualattributes = 'LA'
	 where c_visualattributes like 'L%'
	       and c_fullname like path || '%';
	commit;
	end if;

end;

$$;

