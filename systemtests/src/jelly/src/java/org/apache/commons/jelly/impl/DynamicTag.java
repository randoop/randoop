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
package org.apache.commons.jelly.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.apache.commons.jelly.DynaTagSupport;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p><code>DynamicTag</code> is a tag that is created from
 * inside a Jelly script as a Jelly template and will invoke a
 * given script, passing in its instantiation attributes
 * as variables and will allow the template to invoke its instance body.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.10 $
 */
public class DynamicTag extends DynaTagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(DynamicTag.class);

    /** The template script */
    private Script template;

    /** The instance attributes */
    private Map attributes = new HashMap();

    public DynamicTag() {
    }

    public DynamicTag(Script template) {
        this.template = template;
    }


    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        if ( log.isDebugEnabled() ) {
            log.debug("Invoking dynamic tag with attributes: " + attributes);
        }
        attributes.put("org.apache.commons.jelly.body", getBody());

        // create new context based on current attributes
        JellyContext newJellyContext = context.newJellyContext(attributes);
        Map attrMap = new HashMap();
        for ( Iterator keyIter = this.attributes.keySet().iterator();
              keyIter.hasNext();) {
            String key = (String) keyIter.next();
            if ( key.endsWith( "Attr" ) ) {
                Object value = this.attributes.get( key );
                attrMap.put( key, value );
                attrMap.put( key.substring( 0, key.length()-4 ), value );
            }
        }
        newJellyContext.setVariable( "attrs", attrMap );
        getTemplate().run(newJellyContext, output);
    }

    // DynaTag interface
    //-------------------------------------------------------------------------
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
        attributes.put(name + "Attr", value);
    }

    // Properties
    //-------------------------------------------------------------------------
    /** The template to be executed by this tag which may well
     * invoke this instances body from inside the template
     */
    public Script getTemplate() {
        return template;
    }

    public void setTemplate(Script template) {
        this.template = template;
    }
}
