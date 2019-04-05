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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Chris Uhrich
 */
public class ExportRow {
	
    private Map<String, ExportValue> values = new LinkedHashMap<>();
	
    public ExportRow(String gene, Set<String> columns) {
        for (String columnName : columns) {
            if (columnName.equals("Gene")) {
                values.put(columnName, new ExportValue(gene));
            }
            else {
                values.put(columnName, new ExportValue(""));
            }
        }
    }

    public Collection<ExportValue> getValues() {
        return values.values();
    }

    public void put(String columnName, String value) {
        values.get(columnName).setValue(value);
    }
	
    public JSONArray toJSONArray() throws JSONException {
        JSONArray json = new JSONArray();
        for (ExportValue exportValue : values.values()) {
            json.put(exportValue.toJSONObject());
        }
        return json;
    }
}
