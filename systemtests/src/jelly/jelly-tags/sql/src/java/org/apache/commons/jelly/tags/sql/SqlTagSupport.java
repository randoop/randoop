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

package org.apache.commons.jelly.tags.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.jstl.sql.SQLExecutionTag;
import javax.sql.DataSource;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.tags.Resources;

/**
 * <p>Abstract base class for any SQL related tag in JSTL.
 *
 * @author Hans Bergsten
 * @author Justyna Horwat
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 */

public abstract class SqlTagSupport extends TagSupport implements SQLExecutionTag {

    protected String var;
    protected String scope = "page";

    /*
     * The following properties take expression values, so the
     * setter methods are implemented by the expression type
     * specific subclasses.
     */
    protected Object rawDataSource;
    protected boolean dataSourceSpecified;
    protected String sql;

    /*
     * Instance variables that are not for attributes
     */
    private List parameters;
    protected boolean isPartOfTransaction;

    //*********************************************************************
    // Constructor and initialization

    public SqlTagSupport() {
		super.setEscapeText(false);
    }

    //*********************************************************************
    // Accessor methods

    /**
     * Sets the name of the variable to hold the
     * result.
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Sets the scope of the variable to hold the
     * result.
     */
    public void setScope(String scopeName) {
        this.scope = scopeName;
    }

    /**
     * Sets the SQL DataSource. DataSource can be
     * a String or a DataSource object.
     */
    public void setDataSource(Object dataSource) {
        this.rawDataSource = dataSource;
        this.dataSourceSpecified = true;
    }

    /**
     * Sets the SQL statement to use for the
     * query. The statement may contain parameter markers
     * (question marks, ?). If so, the parameter values must
     * be set using nested value elements.
     */
    public void setSql(String sql) {
        this.sql = sql;
    }


    //*********************************************************************
    // Public utility methods

    /**
     * Called by nested parameter elements to add PreparedStatement
     * parameter values.
     */
    public void addSQLParameter(Object o) {
        if (parameters == null) {
            parameters = new ArrayList();
        }
        parameters.add(o);
    }

    //*********************************************************************
    // Protected utility methods

    /**
     * @return true if there are SQL parameters
     */
    protected boolean hasParameters() {
        return parameters != null && parameters.size() > 0;
    }

    protected void clearParameters() {
        parameters = null;
    }

    protected Connection getConnection() throws JellyTagException, SQLException {
        // Fix: Add all other mechanisms
        Connection conn = null;
        isPartOfTransaction = false;

        TransactionTag parent =
            (TransactionTag) findAncestorWithClass(TransactionTag.class);
        if (parent != null) {
            if (dataSourceSpecified) {
                throw new JellyTagException(Resources.getMessage("ERROR_NESTED_DATASOURCE"));
            }
            conn = parent.getSharedConnection();
            isPartOfTransaction = true;
        }
        else {
            if ((rawDataSource == null) && dataSourceSpecified) {
                throw new JellyTagException(Resources.getMessage("SQL_DATASOURCE_NULL"));
            }
            DataSource dataSource = DataSourceUtil.getDataSource(rawDataSource, context);
            try {
                conn = dataSource.getConnection();
            }
            catch (Exception ex) {
                throw new JellyTagException(
                    Resources.getMessage("DATASOURCE_INVALID", ex.getMessage()));
            }
        }

        return conn;
    }

    protected void setParameters(PreparedStatement ps)
        throws SQLException {
        if (parameters != null) {
            for (int i = 0; i < parameters.size(); i++) {
                // The first parameter has index 1
                ps.setObject(i + 1, parameters.get(i));
            }
        }
    }
}
