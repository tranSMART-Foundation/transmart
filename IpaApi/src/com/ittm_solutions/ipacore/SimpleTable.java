/*
 * Copyright (C) 2016 ITTM S.A.
 *
 * Written by Nils Christian <nils.christian@ittm-solutions.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ittm_solutions.ipacore;

import java.util.ArrayList;
import java.util.List;

/**
 * A very simple class for holding tabular data.
 */
public class SimpleTable {
    /**
     * The header, denoting what is contained in the columns.
     */
    private List<String> header = null;
    /**
     * The types of the columns. Note that the data itself stay Strings, this is
     * just a hint for end users (eg. numeric sorting in a web interface).
     */
    private List<SimpleColumnType> columnTypes;
    /**
     * The main data: a list of rows, each containing a list of values.
     */
    private List<List<String>> rows = new ArrayList<List<String>>();
    /**
     * Name of the table.
     */
    private String name;

    /**
     * Constructor.
     */
    public SimpleTable() {
    }

    /**
     * Constructor initialising the header.
     *
     * @param header
     *          column names of the table
     */
    public SimpleTable(List<String> header) {
        this.header = new ArrayList<String>(header);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the column names of the table.
     *
     * @return column names
     */
    public List<String> header() {
        return header;
    }

    /**
     * Sets the column names of the table.
     *
     * @param header
     *          column names
     */
    public void setHeader(List<String> header) {
        if (this.header != null) {
            throw new IllegalStateException("header already set");
        }

        this.header = new ArrayList<String>(header);
    }

    /**
     * Get the types of the columns.
     *
     * @return column types
     */
    public List<SimpleColumnType> getColumnTypes() {
        return columnTypes;
    }

    /**
     * Set the types of the columns.
     *
     * @param columnTypes
     */
    public void setColumnTypes(List<SimpleColumnType> columnTypes) {
        if (this.header == null) {
            throw new IllegalStateException("Cannot set column types before header");
        }
        if (this.header.size() != columnTypes.size()) {
            throw new IllegalStateException("Column types must be of the same size as header");
        }
        this.columnTypes = columnTypes;
    }

    /**
     * Adds a row to the table.
     *
     * @param row
     *          list of values
     */
    public void addRow(List<String> row) {
        rows.add(row);
    }

    /**
     * Returns true if the table is initialised (header has been set).
     *
     * @return true if header has been initialised
     */
    public boolean isInitialised() {
        return header != null;
    }

    /**
     * Deletes first column with the given column name.
     *
     * @param columnName
     *          name of column to be deleted
     * @return true if a column was removed
     */
    public boolean deleteColumn(String columnName) {
        int i = header.indexOf(columnName);
        if (i < 0) {
            return false;
        }
        header.remove(i);
        if (columnTypes != null) {
            columnTypes.remove(i);
        }
        for (List<String> row : rows) {
            row.remove(i);
        }
        return true;
    }

    @Override
    public String toString() {
        String out = getName() + "\n\n";
        for (String he : header) {
            out += he + "\t";
        }
        out += "\n";
        for (List<String> row : rows) {
            out += "\n";
            for (String e : row) {
                out += e + "\t";
            }
        }
        return out;
    }
}
