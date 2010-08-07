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

import java.sql.Date;

import javax.servlet.jsp.jstl.sql.SQLExecutionTag;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.Resources;

/**
 * <p>Tag handler for &lt;Param&gt; in JSTL, used to set
 * parameter values for a SQL statement.</p>
 *
 * @author Justyna Horwat
 */

public class DateParamTag extends TagSupport {

    //*********************************************************************
    // Private constants

    private static final String TIMESTAMP_TYPE = "timestamp";
    private static final String TIME_TYPE = "time";
    private static final String DATE_TYPE = "date";

    //*********************************************************************
    // Protected state

    protected String type;
    protected java.util.Date value;

    //*********************************************************************
    // Constructor

    public DateParamTag() {
    }

    //*********************************************************************
    // Properties

    public void setValue(Date value) {
        this.value = value;
    }

    public void setType(String type) {
        this.type = type;
    }

    //*********************************************************************
    // Tag logic

    public void doTag(XMLOutput output) throws JellyTagException {
        SQLExecutionTag parent =
            (SQLExecutionTag) findAncestorWithClass(this, SQLExecutionTag.class);
        if (parent == null) {
            throw new JellyTagException(Resources.getMessage("SQL_PARAM_OUTSIDE_PARENT"));
        }

        if (value != null) {
            convertValue();
        }

        parent.addSQLParameter(value);
    }

    //*********************************************************************
    // Private utility methods

    private void convertValue() throws JellyTagException {

        if ((type == null) || (type.equalsIgnoreCase(TIMESTAMP_TYPE))) {
            if (!(value instanceof java.sql.Timestamp)) {
                value = new java.sql.Timestamp(value.getTime());
            }
        }
        else if (type.equalsIgnoreCase(TIME_TYPE)) {
            if (!(value instanceof java.sql.Time)) {
                value = new java.sql.Time(value.getTime());
            }
        }
        else if (type.equalsIgnoreCase(DATE_TYPE)) {
            if (!(value instanceof java.sql.Date)) {
                value = new java.sql.Date(value.getTime());
            }
        }
        else {
            throw new JellyTagException(
                Resources.getMessage("SQL_DATE_PARAM_INVALID_TYPE", type));
        }
    }
}
