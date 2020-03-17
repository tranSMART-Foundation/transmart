--
-- Name: fract_error_dist(double precision); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.fract_error_dist(norminput double precision) RETURNS double precision
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

    -- temp var
    t1 double precision;

    -- exponent input to next equation
    exponent_input double precision;

    ans double precision;

    fractError double precision;

begin
    t1 := 1.0 / (1.0 + 0.5 * ABS(normInput));
    exponent_input := -normInput*normInput - 1.26551223 +
        t1*(1.00002368 + t1*(0.37409196 + t1*(0.09678418 + t1*(-0.18628806 + t1*(0.27886807 + t1*(-1.13520398 + t1*(1.48851587 + t1*(-0.82215223 + t1*(0.17087277)))))))));

    -- Horner's method
    begin
	ans := 1 - t1 * EXP(exponent_input);
    exception
	when numeric_value_out_of_range then --underflow or overflow
	    if exponent_input < 0 then
		ans := 1 - t1 * 0::double precision;
	    else
		ans := 1 - t1 * 'Infinity'::double precision;
	    end if;
    end;

    -- handle sign
    if normInput>0 then
	fractError:= ans;
    else fractError:= -ans;
    end if;

    return fractError;

end
$$;

