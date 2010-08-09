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

import javax.servlet.jsp.jstl.sql.SQLExecutionTag;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.Resources;

/**
 * <p>Tag handler for &lt;Param&gt; in JSTL, used to set
 * parameter values for a SQL statement.</p>
 *
 * @author Hans Bergsten
 */

public class ParamTag extends TagSupport {
    protected Object value;


    public void setValue(Object value) {
        this.value = value;
    }

    //*********************************************************************
    // Tag logic

    public void doTag(XMLOutput output) throws JellyTagException {
        SQLExecutionTag parent =
            (SQLExecutionTag) findAncestorWithClass(this, SQLExecutionTag.class);
        if (parent == null) {
            throw new JellyTagException(Resources.getMessage("SQL_PARAM_OUTSIDE_PARENT"));
        }

        Object paramValue = value;
        if (value != null) {
            paramValue = value;
        }
        else {
            String bodyContent = getBodyText();
            if (bodyContent != null) {
                bodyContent = bodyContent.trim();
                if (bodyContent.length() > 0) {
                    paramValue = bodyContent;
                }
            }
        }

        parent.addSQLParameter(paramValue);
    }
}
