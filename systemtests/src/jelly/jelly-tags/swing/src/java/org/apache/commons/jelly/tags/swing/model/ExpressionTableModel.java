/*
 * Copyright 2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jelly.tags.swing.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;

import org.apache.commons.jelly.JellyContext;

/**
 * A Swing TableModel that uses a List of rows with pluggable Expressions
 * to evaluate the value of the cells
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class ExpressionTableModel extends AbstractTableModel {

    private JellyContext context;
    private List rows = new ArrayList();
    private MyTableColumnModel columnModel = new MyTableColumnModel();

    public ExpressionTableModel() {
    }

    /**
     * Returns the column definitions.
     * @return List
     */
    public List getColumnList() {
        return columnModel.getColumnList();
    }

    /**
     * @return the TableColumnModel
     */
    public TableColumnModel getColumnModel() {
        return columnModel;
    }

    /**
     * Adds a new column definition to the table
     */
    public void addColumn(ExpressionTableColumn column) {
        columnModel.addColumn(column);
    }

    /**
     * Removes a column definition from the table
     */
    public void removeColumn(ExpressionTableColumn column) {
        columnModel.removeColumn(column);
    }


    // TableModel interface
    //-------------------------------------------------------------------------
    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return columnModel.getColumnCount();
    }

    public String getColumnName(int columnIndex) {
        String answer = null;
        if (columnIndex < 0 || columnIndex >= columnModel.getColumnCount()) {
            return answer;
        }
        Object value = columnModel.getColumn(columnIndex).getHeaderValue();
        if (value != null) {
            return value.toString();
        }
        return answer;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Object answer = null;
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return answer;
        }
        if (columnIndex < 0 || columnIndex >= columnModel.getColumnCount()) {
            return answer;
        }
        Object row = rows.get(rowIndex);;
        ExpressionTableColumn column = (ExpressionTableColumn) columnModel.getColumn(columnIndex);
        if (row == null || column == null) {
            return answer;
        }
        return column.evaluateValue(this, row, rowIndex, columnIndex);
    }


    // Properties
    //-------------------------------------------------------------------------


    /**
     * Returns the list of rows.
     * @return List
     */
    public List getRows() {
        return rows;
    }

    /**
     * Sets the list of rows.
     * @param rows The rows to set
     */
    public void setRows(List rows) {
        this.rows = rows;
    }

    /**
     * Returns the context.
     * @return JellyContext
     */
    public JellyContext getContext() {
        return context;
    }

    /**
     * Sets the context.
     * @param context The context to set
     */
    public void setContext(JellyContext context) {
        this.context = context;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected static class MyTableColumnModel extends DefaultTableColumnModel {
        public List getColumnList() {
            return tableColumns;
        }
    };


}
