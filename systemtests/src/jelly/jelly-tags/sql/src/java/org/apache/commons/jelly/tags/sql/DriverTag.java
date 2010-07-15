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

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * <p>Tag handler for &lt;Driver&gt; in JSTL, used to create
 * a simple DataSource for prototyping.</p>
 *
 * @author Hans Bergsten
 */
public class DriverTag extends TagSupport {
    private static final String DRIVER_CLASS_NAME =
        "javax.servlet.jsp.jstl.sql.driver";
    private static final String JDBC_URL = "javax.servlet.jsp.jstl.sql.jdbcURL";
    private static final String USER_NAME = "javax.servlet.jsp.jstl.sql.userName";
    private static final String PASSWORD = "javax.servlet.jsp.jstl.sql.password";

    private String driverClassName;
    private String jdbcURL;
    private String scope = "page";
    private String userName;
    private String var;

    //*********************************************************************
    // Accessor methods

    public void setDriver(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public void setJdbcURL(String jdbcURL) {
        this.jdbcURL = jdbcURL;
    }

    /**
     * Sets the scope of the variable to hold the
     * result.
     *
     */
    public void setScope(String scopeName) {
        this.scope = scopeName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setVar(String var) {
        this.var = var;
    }

    //*********************************************************************
    // Tag logic

    public void doTag(XMLOutput output) throws JellyTagException {
        DataSourceWrapper ds = new DataSourceWrapper();
        try {
            ds.setDriverClassName(getDriverClassName());
        }
        catch (Exception e) {
            throw new JellyTagException("Invalid driver class name: " + e.getMessage());
        }
        ds.setJdbcURL(getJdbcURL());
        ds.setUserName(getUserName());
        ds.setPassword(getPassword());
        context.setVariable(var, ds);
    }

    //*********************************************************************
    // Private utility methods

    private String getDriverClassName() {
        if (driverClassName != null) {
            return driverClassName;
        }
        return getInitParameter(DRIVER_CLASS_NAME);
    }

    private String getJdbcURL() {
        if (jdbcURL != null) {
            return jdbcURL;
        }
        return getInitParameter(JDBC_URL);
    }

    private String getUserName() {
        if (userName != null) {
            return userName;
        }
        return getInitParameter(USER_NAME);
    }

    private String getPassword() {
        return getInitParameter(PASSWORD);
    }

    protected String getInitParameter(String key) {
        return "";
    }
}
