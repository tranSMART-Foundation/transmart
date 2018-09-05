--
-- Name: jnj_init_cap(text); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION jnj_init_cap(text_to_parse text) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE

   start_pos 		bigint;
   end_pos   		bigint;
   string_length 	integer;
   string_tokens 	character varying(32676);
   counter 			integer;
   token_value 		character varying(1000);
   text_delimiter 	character varying(1);
   noInitCap 		boolean;

   --	create array to hold strings that will not be initcapped

   cat_path_exc_rec record;

   excludedText tm_cz.category_path_excluded_words[] = array(select row(excluded_text) from tm_cz.category_path_excluded_words);	

   exclCt integer;
   exclSize integer;

   --	text to return

   initcap_text character varying(1000);

BEGIN
  -------------------------------------------------------------------------------
   -- Performs custom initcap for category paths where specific text strings are
   -- excluded from the process.  Strings are delimited by a space.  The \ in
   -- the category path are converted to ' \ ' before parsing.

   -- JEA@20091001 - First rev.
   -- Copyright ? 2009 Recombinant Data Corp.
   -------------------------------------------------------------------------------

	--	Add a delimiter to the end of the string so we don't lose last value and
	--	surround \ with spaces

	exclSize := array_length(excludedText, 1);

	text_delimiter := ' ';
	string_tokens := replace(text_to_parse,'\',' \ ') || text_delimiter;

	--get length of string
	string_length := length(string_tokens);

	--set start and end for first token
	start_pos := 1;
	end_pos   := tm_cz.instr(string_tokens,text_delimiter,1,1);
	counter := 1;

	LOOP
		--	Get substring
		token_value := substr(string_tokens, start_pos, end_pos - start_pos);

		--	check if token_value is in excludedText, if yes, set indicator

		noInitCap := false;
		exclCt := 0;

		while ((exclCt < exclSize) and not noInitCap)
		loop
			if token_value = excludedText[exclCt].excluded_text then
				noInitCap := true;
			end if;
			exclCt := exclCt + 1;
		end loop;

		if noInitCap then
			initcap_text := initcap_text || token_value || ' ';
		else
			initcap_text := initcap_text || initcap(token_value) || ' ';
		end if;

		--Check to see if we are done
		IF end_pos = string_length
		THEN
			initcap_text := replace(rtrim(initcap_text,' '),' \ ','\');
			EXIT;
		ELSE
			-- Increment Start Pos and End Pos
			start_pos := end_pos + 1;
			--	increment counter
			counter := counter + 1;
			end_pos := tm_cz.instr(string_tokens, text_delimiter,1, counter);
     
		END IF;
  END LOOP;

  return initcap_text;

END;

$$;

