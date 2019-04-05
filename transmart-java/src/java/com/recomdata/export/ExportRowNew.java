/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/  

package com.recomdata.export;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Chris Uhrich
 */
public class ExportRowNew {
	
    private Map<String, String> values = new LinkedHashMap<>();
	
    public Collection<String> getValues() {
        return values.values();
    }
	
    public boolean containsColumn(String columnname) {
        return values.containsKey(columnname);
    }

    public void put(String columnName, String value) {
        if (value == null) {
            value = "NULL";
        }
        values.put(columnName, value);
    }
	
    public String get(String columnname) {
        return values.get(columnname);
    }
	
    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        for (String column : values.keySet()) {
            json.put(column, values.get(column));
        }
        return json;
    }

    public JSONArray toJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (String column : values.keySet()) {
            jsonArray.put(values.get(column));
        }
        return jsonArray;
    }
}
