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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Chris Uhrich
 */
public class HeatMapTable {

    private static final String RATIO_PATTERN = "##0.######";

    private Map<String, String> ids1 = new HashMap<>();
    private Map<String, String> ids2 = new HashMap<>();
    private Map<String, HeatMapColumn> columns = new LinkedHashMap<>();
    private Map<String, HeatMapRow> rows = new LinkedHashMap<>();
    private Map<String, String> descriptions = new HashMap<>();
    private boolean missingValues = false;

    public HeatMapTable(List<ExportResult> rs1, String patientIds1, List<ExportResult> rs2, String patientIds2) {
        setColumns(rs1, rs2);

        for (ExportResult e : rs1) {
            String gene = e.getConcept();
            String description = e.getDescription();
            String patientId = e.getSubject();
            String value = e.getValue();
            String columnName = ids1.get(patientId);

            HeatMapRow row;
            if (!rows.containsKey(gene)) {
                row = new HeatMapRow(gene, columns.keySet());
                rows.put(gene, row);
            }
            else {
                row = rows.get(gene);
            }
            row.put(columnName, value);

            if (!descriptions.containsKey(gene)) {
                descriptions.put(gene, description);
            }
            if (value == null || value.equals("null") || value.equals("")) {
                missingValues = true;
            }
        }

        for (ExportResult e : rs2) {
            String gene = e.getConcept();
            String description = e.getDescription();
            String patientId = e.getSubject();
            String value = e.getValue();
            String columnName = ids2.get(patientId);

            HeatMapRow row;
            if (!rows.containsKey(gene)) {
                row = new HeatMapRow(gene, columns.keySet());
                rows.put(gene, row);
            }
            else {
                row = rows.get(gene);
            }
            row.put(columnName, value);

            if (!descriptions.containsKey(gene)) {
                descriptions.put(gene, description);
            }

            if (value == null || value.equals("null") || value.equals("")) {
                missingValues = true;
            }
        }

        HeatMapRow row;
        List<String> ids = new ArrayList<>();
        ids.addAll(ids1.values());
        ids.addAll(ids2.values());

        for (String gene : rows.keySet()) {
            row = rows.get(gene);
            for (String id : ids) {
                String value = row.get(id);
                if (value == null || value.equals("null") || value.equals("")) {
                    // causes problems in genepattern
                    missingValues = true;
                    return;
                }
            }
        }		
    }

    public void writeToFile(String delimiter, PrintStream ps) {
        if (missingValues) {
            writeToFile(delimiter, ps, true);
        }
        else {
            writeToFile(delimiter, ps, false);
        }
    }
	
    public void writeToFile(String delimiter, PrintStream ps, Boolean addMeans) {
        // outputs a data table, suitable for export to genepattern

        ps.println("#1.2");

        if (addMeans) {
            ps.println(rows.size() + delimiter + columns.size() );
        }
        else {
            ps.println(rows.size() + delimiter + (columns.size() - 1 ) );
        }

        ps.print("NAME"+delimiter+"Description");

        // to assure that columns are returned in the right order,
        // build a list of subject ids, then sort the list
        List<String> ids = new ArrayList<>();
        ids.addAll(ids1.values());
        ids.addAll(ids2.values());

        // ids should be numerically sorted already...

        Collections.sort(ids, new subjectComparator());

        for (String id : ids) {
            ps.print(delimiter + id);
        }

        if (addMeans) {
            ps.print(delimiter + "Mean");
        }

        ps.print("\n");

        HeatMapRow row;
        double sumOfValues = 0.0;
        double countOfValues = 0.0;
        for (String gene : rows.keySet()) {
            ps.print(gene + delimiter + descriptions.get(gene));
            row = rows.get(gene);
            for (String id : ids) {
                String value = row.get(id);
                if (value.equals("null") || value.equals("")) {
                    // causes problems in genepattern
                    value = "";
                }
                else if (addMeans) {
                    sumOfValues = sumOfValues + Double.valueOf(value);
                    countOfValues = countOfValues + 1;
                }
                ps.print(delimiter + value);
            }
            if (addMeans) {
                double mean = 0.0;
                if (countOfValues > 0) {
                    mean = sumOfValues / countOfValues;
                }
                ps.print(delimiter + mean);
            }
            ps.print("\n");			
        }
    }
	
    public void setColumns(String patientIds1, String patientIds2) {

        columns.put("Gene", new HeatMapColumn("Gene", "Gene", "", "t"));

        StringTokenizer st1 = new StringTokenizer(patientIds1, ",");
        while (st1.hasMoreTokens()) {
            String id = st1.nextToken();
            String label = "S1_" + id;
            ids1.put(id, label);
            columns.put(label, new HeatMapColumn(id, label, RATIO_PATTERN, "n"));
        }

        StringTokenizer st2 = new StringTokenizer(patientIds2, ",");
        while (st2.hasMoreTokens()) {
            String id = st2.nextToken();
            String label = "S2_" + id;
            ids2.put(id, label);
            columns.put(label, new HeatMapColumn(id, label, RATIO_PATTERN, "n"));
        }
    }

    public void setColumns(List<ExportResult> rs1, List<ExportResult> rs2) {
        columns.put("Gene", new HeatMapColumn("Gene", "Gene", "", "t"));

        for (ExportResult e : rs1) {
            String patientId = e.getSubject();
            if(!ids1.containsKey(patientId)){
                String label = "S1_" + patientId;
                ids1.put(patientId, label);
                columns.put(label, new HeatMapColumn(patientId, label, RATIO_PATTERN, "n"));
            }
        }

        for (ExportResult e : rs2) {
            String patientId = e.getSubject();
            if(!ids2.containsKey(patientId)){
                String label = "S2_" + patientId;
                ids2.put(patientId, label);
                columns.put(label, new HeatMapColumn(patientId, label, RATIO_PATTERN, "n"));
            }
        }
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("status", "ok");
        jsonObj.put("requestId", "0");

        JSONObject jsonTable = new JSONObject();

        JSONArray jsonColumns = new JSONArray();
        for (HeatMapColumn heatMapColumn : columns.values()) {
            jsonColumns.put(heatMapColumn.toJSONObject());
        }
        jsonTable.put("cols", jsonColumns);

        JSONArray jsonRows = new JSONArray();
        for (HeatMapRow heatMapRow : rows.values()) {
            jsonRows.put(heatMapRow.toJSONArray());
        }
        jsonTable.put("rows", jsonRows);

        jsonObj.put("table", jsonTable);
        jsonObj.put("signature", "1");

        return jsonObj;
    }
}
