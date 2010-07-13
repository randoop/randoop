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

package javax.servlet.jsp.jstl.sql;

import java.sql.*;
import java.util.*;

/**
 * <p>This class creates a cached version of a <tt>ResultSet</tt>.
 * It's represented as a <tt>Result</tt> implementation, capable of
 * returing an array of <tt>Row</tt> objects containing a <tt>Column</tt>
 * instance for each column in the row.   It is not part of the JSTL
 * API; it serves merely as a back-end to ResultSupport's static methods.
 * Thus, we scope its access to the package.
 *
 * @author Hans Bergsten
 * @author Justyna Horwat
 */

class ResultImpl implements Result {
    private List rowMap;
    private List rowByIndex;
    private String[] columnNames;
    private boolean isLimited;

    /**
     * This constructor reads the ResultSet and saves a cached
     * copy.
     *
     * @param rs an open <tt>ResultSet</tt>, positioned before the first
     * row
     * @param startRow, beginning row to be cached
     * @param maxRows, query maximum rows limit
     * @exception if a database error occurs
     */
    public ResultImpl(ResultSet rs, int startRow, int maxRows)
        throws SQLException
    {
        rowMap = new ArrayList();
        rowByIndex = new ArrayList();

        ResultSetMetaData rsmd = rs.getMetaData();
        int noOfColumns = rsmd.getColumnCount();

        // Create the column name array
        columnNames = new String[noOfColumns];
        for (int i = 1; i <= noOfColumns; i++) {
            columnNames[i-1] = rsmd.getColumnName(i);
        }

        // Throw away all rows upto startRow
        for (int i = 0; i < startRow; i++) {
            rs.next();
        }

        // Process the remaining rows upto maxRows
        int processedRows = 0;
        while (rs.next()) {
            if ((maxRows != -1) && (processedRows == maxRows)) {
                isLimited = true;
                break;
            }
            Object[] columns = new Object[noOfColumns];
            SortedMap columnMap =
                new TreeMap(String.CASE_INSENSITIVE_ORDER);

            // JDBC uses 1 as the lowest index!
            for (int i = 1; i <= noOfColumns; i++) {
                Object value =  rs.getObject(i);
                if (rs.wasNull()) {
                    value = null;
                }
                columns[i-1] = value;
                columnMap.put(columnNames[i-1], value);
            }
            rowMap.add(columnMap);
            rowByIndex.add(columns);
            processedRows++;
        }
    }

    /**
     * Returns an array of SortedMap objects. The SortedMap
     * object key is the ColumnName and the value is the ColumnValue.
     * SortedMap was created using the CASE_INSENSITIVE_ORDER
     * Comparator so the key is the case insensitive representation
     * of the ColumnName.
     *
     * @return an array of Map, or null if there are no rows
     */
    public SortedMap[] getRows() {
        if (rowMap == null) {
            return null;
        }

        //should just be able to return SortedMap[] object
        return (SortedMap []) rowMap.toArray(new SortedMap[0]);
    }


    /**
     * Returns an array of Object[] objects. The first index
     * designates the Row, the second the Column. The array
     * stores the value at the specified row and column.
     *
     * @return an array of Object[], or null if there are no rows
     */
    public Object[][] getRowsByIndex() {
        if (rowByIndex == null) {
            return null;
        }

        //should just be able to return Object[][] object
        return (Object [][])rowByIndex.toArray(new Object[0][0]);
    }

    /**
     * Returns an array of String objects. The array represents
     * the names of the columns arranged in the same order as in
     * the getRowsByIndex() method.
     *
     * @return an array of String[]
     */
    public String[] getColumnNames() {
        return columnNames;
    }

    /**
     * Returns the number of rows in the cached ResultSet
     *
     * @return the number of cached rows, or -1 if the Result could
     *    not be initialized due to SQLExceptions
     */
    public int getRowCount() {
        if (rowMap == null) {
            return -1;
        }
        return rowMap.size();
    }

    /**
     * Returns true of the query was limited by a maximum row setting
     *
     * @return true if the query was limited by a MaxRows attribute
     */
    public boolean isLimitedByMaxRows() {
        return isLimited;
    }

}
