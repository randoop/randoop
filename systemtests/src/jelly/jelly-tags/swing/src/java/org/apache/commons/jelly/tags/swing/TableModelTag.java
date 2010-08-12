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

import javax.swing.JTable;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.tags.core.UseBeanTag;
import org.apache.commons.jelly.tags.swing.model.ExpressionTableModel;

/**
 * Creates a default TableModel using nested tableColumn tags.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class TableModelTag extends UseBeanTag {

    public ExpressionTableModel getTableModel() {
        return (ExpressionTableModel) getBean();
    }


    // Implementation methods
    //-------------------------------------------------------------------------
    protected void processBean(String var, Object bean) throws JellyTagException {
        super.processBean(var, bean);

        ComponentTag tag = (ComponentTag) findAncestorWithClass( ComponentTag.class );
        if ( tag == null ) {
            throw new JellyTagException( "This tag must be nested within a JellySwing <table> tag" );
        }
        ExpressionTableModel model = getTableModel();
        model.setContext(context);

        Object component = tag.getComponent();
        if (component instanceof JTable) {
            JTable table = (JTable) component;
            table.setModel(model);
        }
        else {
            throw new JellyTagException( "This tag must be nested within a JellySwing <table> tag" );
        }
    }

    protected Class getDefaultClass() {
        return ExpressionTableModel.class;
    }
}

