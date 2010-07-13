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

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.tags.Resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>A simple <code>DataSource</code> utility for the standard
 * <code>DriverManager</code> class.
 *
 * TO DO: need to cache DataSource
 *
 * @author Justyna Horwat
 */
public class DataSourceUtil {

    private static final String ESCAPE = "\\";
    private static final String TOKEN = ",";

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(DataSourceUtil.class);

    /**
     * If dataSource is a String first do JNDI lookup.
     * If lookup fails parse String like it was a set of JDBC parameters
     * Otherwise check to see if dataSource is a DataSource object and use as
     * is
     */
    static DataSource getDataSource(Object rawDataSource, JellyContext pc)
        throws JellyTagException {
        DataSource dataSource = null;

        if (rawDataSource == null) {
            rawDataSource = pc.getVariable("org.apache.commons.jelly.sql.DataSource");
        }

        if (rawDataSource == null) {
            return null;
        }

        /*
        * If the 'dataSource' attribute's value resolves to a String
        * after rtexpr/EL evaluation, use the string as JNDI path to
        * a DataSource
        */
        if (rawDataSource instanceof String) {
            try {
                Context ctx = new InitialContext();
                // relative to standard JNDI root for J2EE app
                Context envCtx = (Context) ctx.lookup("java:comp/env");
                dataSource = (DataSource) envCtx.lookup((String) rawDataSource);
            }
            catch (NamingException ex) {
                dataSource = getDataSource((String) rawDataSource);
            }
        }
        else if (rawDataSource instanceof DataSource) {
            dataSource = (DataSource) rawDataSource;
        }
        else {
            throw new JellyTagException(Resources.getMessage("SQL_DATASOURCE_INVALID_TYPE"));
        }

        return dataSource;
    }

    /**
     * Parse JDBC parameters and setup dataSource appropriately
     */
    private static DataSource getDataSource(String params) throws JellyTagException {
        DataSourceWrapper dataSource = new DataSourceWrapper();

        String[] paramString = new String[4];
        int escCount = 0;
        int aryCount = 0;
        int begin = 0;

        for (int index = 0; index < params.length(); index++) {
            char nextChar = params.charAt(index);
            if (TOKEN.indexOf(nextChar) != -1) {
                if (escCount == 0) {
                    paramString[aryCount] = params.substring(begin, index);
                    begin = index + 1;
                    if (++aryCount > 4) {
                        throw new JellyTagException(Resources.getMessage("JDBC_PARAM_COUNT"));
                    }
                }
            }
            if (ESCAPE.indexOf(nextChar) != -1) {
                escCount++;
            }
            else {
                escCount = 0;
            }
        }
        paramString[aryCount] = params.substring(begin);

        // use the JDBC URL from the parameter string
        dataSource.setJdbcURL(paramString[0]);

        // try to load a driver if it's present
        if (paramString[1] != null) {
            try {
                dataSource.setDriverClassName(paramString[1]);
            }
            catch (Exception ex) {
                throw new JellyTagException(
                    Resources.getMessage("DRIVER_INVALID_CLASS", ex.getMessage()));
            }
        }

        // set the username and password
        dataSource.setUserName(paramString[2]);
        dataSource.setPassword(paramString[3]);

        return dataSource;
    }

}
