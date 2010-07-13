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

import javax.swing.table.TableColumn;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.expression.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a column in an ExpressionTable
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class ExpressionTableColumn extends TableColumn {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( ExpressionTableColumn.class );

    private Expression value;
    private Class type = Object.class;

    public ExpressionTableColumn() {
    }

    public String toString() {
        return super.toString() + "[value:" + value + "]";
    }

    /**
     * Evaluates the value of a cell
     */
    public Object evaluateValue(ExpressionTableModel model, Object row, int rowIndex, int columnIndex) {
        if (value == null) {
            return null;
        }
        // lets put the values in the context
        JellyContext context = model.getContext();
        context.setVariable("rows", model.getRows());
        context.setVariable("columns", model.getColumnList());
        context.setVariable("row", row);
        context.setVariable("rowIndex", new Integer(rowIndex));
        context.setVariable("columnIndex", new Integer(columnIndex));

        // now lets invoke the expression
        try {
            return value.evaluateRecurse(context);
        }
        catch (RuntimeException e) {
            log.warn( "Caught exception: " + e + " evaluating: " + value, e );
            throw e;
        }
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Returns the column type.
     * @return Class
     */
    public Class getType() {
        return type;
    }

    /**
     * Returns the expression used to extract the value.
     * @return Expression
     */
    public Expression getValue() {
        return value;
    }

    /**
     * Sets the expression used to extract the value.
     * @param type The type to set
     */
    public void setType(Class type) {
        this.type = type;
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValue(Expression value) {
        this.value = value;
    }

}
