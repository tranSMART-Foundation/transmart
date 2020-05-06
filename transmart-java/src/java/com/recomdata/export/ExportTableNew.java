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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mmcduffie
 */
public class ExportTableNew {
    
    private Map<String, ExportColumn> columns = new LinkedHashMap<>();
    private Map<String, ExportRowNew> rows = new LinkedHashMap<>();
	
    public ExportColumn getColumn(String columnname) {
        return columns.get(columnname);
    }

    public ExportColumn getColumnByBasename(String basename) {
        for (Iterator<ExportColumn> i = columns.values().iterator(); i.hasNext(); ) {
            ExportColumn col = i.next();
            if(basename.equals(col.getBasename())) {
                return col;
            }
        }
        return null;
    }

    public String setColumnUnique(String columnname) {
        ExportColumn newcol = columns.get(columnname);
        if (newcol == null) return null;
        String basename = newcol.getBasename();
        String tooltip = newcol.getTooltip();
        List<ExportColumn> list = new ArrayList<ExportColumn>();

        for (Iterator<ExportColumn> i = columns.values().iterator(); i.hasNext(); ) {
            ExportColumn col = i.next();
            if(basename.equals(col.getBasename()) && !columnname.equals(col.getId())) {
                list.add(col);
            }
        }
        if(list.size() < 1) {
            return newcol.getLabel();
        }

        // Check tooltip for backslash with optional underscore(s) from rejected characters

        String[] newNodes = tooltip.split("_?\\\\_?");
        int newSize = newNodes.length;
        for (int k = 0; k < list.size(); k++) {
            ExportColumn listcol = list.get(k);
            if(tooltip.equals(listcol.getTooltip())) {
                newcol.setLabel(listcol.getLabel());
            }
            else {
                String[] colNodes = listcol.getTooltip().split("_?\\\\_?");
                int colSize = colNodes.length;
                int knew = newSize - 1;
                int kcol = colSize - 1;
                String newLabel = newNodes[knew];
                String colLabel = colNodes[kcol];
                while (newLabel.equals(colLabel) && knew > 0 && kcol > 0) {
                    newLabel = newNodes[--knew] + " " + newLabel;
                    colLabel = colNodes[--kcol] + " " + colLabel;
                }

                if(newLabel.equals(colLabel)) {
                    newcol.setLabel(listcol.getLabel());
                }
                else {
                    if(colLabel.length() > listcol.getLabel().length()) {
                        listcol.setLabel(colLabel);
                    }
                    if(newLabel.length() > newcol.getLabel().length()) {
                        newcol.setLabel(newLabel);
                    }
                }
            }
        }

        return newcol.getLabel();
    }

    public void putColumn(String columnname, ExportColumn column) {
        columns.put(columnname, column);
    }

    public boolean containsColumn(String columnname) {
        return columns.containsKey(columnname);
    }

    public ExportRowNew getRow(String rowname) {
        return rows.get(rowname);
    }

    public void putRow(String rowname, ExportRowNew row) {
        rows.put(rowname, row);
    }

    public boolean containsRow(String rowname) {
        return rows.containsKey(rowname);
    }

    public Collection<ExportColumn> getColumns() {
        return columns.values();
    }

    public Collection<ExportRowNew> getRows() {
        return rows.values();
    }

    public Map<String, ExportColumn> getColumnMap() {
        return columns;
    }

    public Map<String, ExportRowNew> getRowMap() {
        return rows;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject metadata = new JSONObject();		
        JSONObject jsonTable = new JSONObject();	
        JSONArray fields = new JSONArray();
		
        for (ExportColumn exportColumn : columns.values()) {
            fields.put(exportColumn.toJSONObject());
        }
		
        metadata.put("fields", fields);
        metadata.put("totalProperty","results");
        metadata.put("root", "rows");
        metadata.put("id", "subject");
		
        jsonTable.put("metaData",metadata);
        jsonTable.put("results",rows.size());
        JSONArray jsonRows = new JSONArray();

        for (ExportRowNew exportRowNew : rows.values()) {
            jsonRows.put(exportRowNew.toJSONObject());
        }
        jsonTable.put("rows", jsonRows);
        return jsonTable;
    }

    //Supports jQuery datatables object
    public JSONObject toJSON_DataTables(String echo, String title) throws JSONException {
        JSONObject jsonTable = toJSON_DataTables(echo);
        jsonTable.put("iTitle", title);
        return jsonTable;
    }

    //Supports jQuery datatables object
    public JSONObject toJSON_DataTables(String echo) throws JSONException {

        JSONObject jsonTable = new JSONObject();
        JSONArray aoColumns = new JSONArray();
        JSONArray headerToolTips = new JSONArray();

        for (ExportColumn col : columns.values()) {
            aoColumns.put(col.toJSON_DataTables());
            headerToolTips.put(col.getId());
        }

        JSONArray jsonRows = new JSONArray();
        for (ExportRowNew exportRowNew : rows.values()) {
            jsonRows.put(exportRowNew.toJSONArray());
        }

        if (echo != null) {
            jsonTable.put("sEcho", echo);
        }
        jsonTable.put("iTitle", "Title");
        jsonTable.put("iTotalRecords", rows.size());
        jsonTable.put("iTotalDisplayRecords", rows.size());
        jsonTable.put("aoColumns", aoColumns);
        jsonTable.put("headerToolTips", headerToolTips);

        jsonTable.put("aaData", jsonRows);

        return jsonTable;
    }

    public JSONObject getJSONColumns() throws JSONException {

        JSONObject jsonColumns = new JSONObject();
        JSONArray columnsAry = new JSONArray();

        for (ExportColumn exportColumn : columns.values()) {
            columnsAry.put(exportColumn.toJSON_DataTables());
        }

        jsonColumns.put("aoColumns", columnsAry);
        return jsonColumns;
    }

    public byte[] toCSVbytes() {
        //copy from map of objects to list of strings
        List<String> newheaders = new ArrayList<>();
        for (ExportColumn exportColumn : columns.values()) {
            newheaders.add(exportColumn.getId());
        }

        //copy from map of rows into list of list of rows
        List<List> newrows = new ArrayList<>();
        for (ExportRowNew exportRowNew : rows.values()) {
            newrows.add(new ArrayList<>(exportRowNew.getValues()));
        }

        return CSVGenerator.generateCSV(newheaders, newrows);
    }
}
