--
-- Name: cum_normal_dist(double precision, double precision, double precision); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.cum_normal_dist(foldchg double precision, mu double precision, sigma double precision) RETURNS double precision
    LANGUAGE plpgsql IMMUTABLE
AS $$
    declare
    /*************************************************************************
     * Copyright 2008-2012 Janssen Research & Development, LLC.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     ******************************************************************/

    -------------------------------------------------------------------------------
    -- param foldChg: fold change ratio from analysis_data table
    -- param mu: mean of all analsyis_data records for a given analysis
    -- param sigma: std dev of all analsyis_data records for a given analysis
    -------------------------------------------------------------------------------

    -- temporary vars
    t1 double PRECISION;

    -- fractional error dist input
    fract_error_input double PRECISION;

    -- return result (i.e. Prob [X<=x])
    ans double PRECISION;

begin
    select (foldChg-mu)/sigma into t1;
    select t1/sqrt(2) into fract_error_input;
    select 0.5 * (1.0 + tm_cz.fract_error_dist(fract_error_input)) into ans;
    return ans;
end
$$;

