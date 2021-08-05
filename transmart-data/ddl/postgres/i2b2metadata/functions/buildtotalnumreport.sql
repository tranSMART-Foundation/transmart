--
-- Name: BuildTotalnumReport(int, float); Type: FUNCTION; Schema: i2b2metadata; Owner: -
--
CREATE OR REPLACE FUNCTION i2b2metadata.BuildTotalnumReport(threshold int, sigma float) RETURNS void
 LANGUAGE plpgsql
 AS $BODY$
BEGIN

-- Build the i2b2metadata.totalnum report in the i2b2metadata.totalnum_report table
-- Uses the most recent value for each path name in the i2b2metadata.totalnum table,
-- and obfuscates with the specified censoring threshold and Gaussian sigma
-- e.g., to censor counts under ten and add Gaussian noise with a sigma of 2.8 - exec BuildTotalnumReport 9, 2.8
-- Postgres version, dependent on normal-curve random helper function in the same directory
-- Run by runtotalnum, but example usage:
--     select BuildTotalnumReport(9, 2.8);
-- By Jeff Klann, PhD

     -- Implements SHRINE's obfuscation (with user-specified threshold and sigma)
    -- If the result is less than 10
    --                10 or fewer
    -- else  
     -- Generate some gaussian noise with standard deviation of 6.5
     -- If the absolute value of the noise is greater than 10, use 10
     -- Round the result to the nearest 5
     -- Add the noise to the rounded result
     --  Round the noised result
     -- If the rounded result is less than 10
     --     10 or fewer
     --  Else
     -- The rounded result

    truncate table i2b2metadata.totalnum_report;

    insert into i2b2metadata.totalnum_report(c_fullname, agg_count, agg_date)
     select c_fullname, case sign(agg_count - threshold + 1 ) when 1 then (round(agg_count/5.0,0)*5)+round(random_normal(0,sigma,threshold)) else -1 end agg_count, 
       to_char(agg_date,'YYYY-MM-DD') agg_date from 
       (select * from 
           (select row_number() over (partition by c_fullname order by agg_date desc) rn,c_fullname, agg_count,agg_date from i2b2metadata.totalnum where typeflag_cd like 'P%') x where rn=1) y;

    update i2b2metadata.totalnum_report set agg_count=-1 where agg_count<threshold;

END;
$BODY$
 VOLATILE SECURITY DEFINER;
