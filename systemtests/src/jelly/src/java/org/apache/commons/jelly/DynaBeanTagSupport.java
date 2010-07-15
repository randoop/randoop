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

package org.apache.commons.jelly;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;

/** 
 * <p><code>DynaBeanTag</code> is a DynaTag implementation which uses a DynaBean
 * to store its attribute values in. Derived tags can then process this
 * DynaBean in any way it wishes.
 * </p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.3 $
 */

public abstract class DynaBeanTagSupport extends DynaTagSupport {

    /** the DynaBean which is used to store the attributes of this tag. */
    private DynaBean dynaBean;

    public DynaBeanTagSupport() {
    }
    
    public DynaBeanTagSupport(DynaBean dynaBean) {
        this.dynaBean = dynaBean;
    }
     
    /** Sets the context in which the tag will be run. */
    public void setContext(JellyContext context) throws JellyTagException {
        this.context = context;
        beforeSetAttributes();
    }
    
    /** Sets an attribute value of this tag before the tag is invoked
     */
    public void setAttribute(String name, Object value) throws JellyTagException {
        getDynaBean().set(name, value);
    }

    /**
     * @return the type of the given attribute. By default just return
     * Object.class if this is not known.
     */
    public Class getAttributeType(String name) throws JellyTagException {
        DynaProperty property = getDynaBean().getDynaClass().getDynaProperty(name);
        if (property != null) {
            return property.getType();
        }
        return Object.class;
    }
    
    /** 
     * @return the DynaBean which is used to store the
     *  attributes of this tag
     */
    public DynaBean getDynaBean() {
        return dynaBean;
    }
    
    /**
     * Sets the DynaBean which is used to store the
     *  attributes of this tag
     */
    public void setDynaBean(DynaBean dynaBean) {
        this.dynaBean = dynaBean;
    }

    /**
     * Callback to allow processing to occur before the attributes are about to be set
     */
    public void beforeSetAttributes() throws JellyTagException {
    }
    
}
