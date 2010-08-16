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
package org.apache.commons.jelly.tags.swing;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.tags.core.UseBeanTag;
import org.apache.commons.jelly.tags.swing.model.ExpressionTableColumn;

/**
 * Creates a default TableColumnModel.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class TableModelColumnTag extends UseBeanTag {

    public ExpressionTableColumn getColumn() {
        return (ExpressionTableColumn) getBean();
    }

    public Class getAttributeType(String name) throws JellyTagException {
        if (name.equals("value")) {
            return Expression.class;
        }
        return super.getAttributeType(name);
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void processBean(String var, Object bean) throws JellyTagException {
        super.processBean(var, bean);

        TableModelTag tag = (TableModelTag) findAncestorWithClass( TableModelTag.class );
        if ( tag == null ) {
            throw new JellyTagException( "This tag must be nested within a <tableModel> tag" );
        }
        tag.getTableModel().addColumn( getColumn() );
    }

    protected Class getDefaultClass() {
        return ExpressionTableColumn.class;
    }
}

