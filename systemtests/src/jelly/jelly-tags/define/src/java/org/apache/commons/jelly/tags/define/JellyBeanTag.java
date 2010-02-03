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

package org.apache.commons.jelly.tags.define;

import java.lang.reflect.Method;

import org.apache.commons.beanutils.MethodUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Binds a Java bean to the given named Jelly tag so that the attributes of
 * the tag set the bean properties. After the body of this tag is invoked
 * then the beans invoke() method will be called, if the bean has one.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class JellyBeanTag extends BeanTag {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(JellyBeanTag.class);

    /** Empty parameter types for Method lookup */
    private static final Class[] emptyParamTypes = {};

    /** the name of the method to invoke on the bean */
    private String method;

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the method name to use, which defaults to 'run' for Runnable
     * objects
     */
    public String getMethod() {
        if ( method == null ) {
            return "run";
        }
        return method;
    }

    /**
     * Sets the name of the method to invoke on the bean.
     * This defaults to "run" so that Runnable objects can be
     * invoked, but this property can be set to whatever is required,
     * such as "execute" or "invoke"
     */
    public void setMethod(String method) {
        this.method = method;
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    protected Method getInvokeMethod( Class theClass ) {
        Method invokeMethod =
            MethodUtils.getAccessibleMethod(
                theClass,
                getMethod(),
                emptyParamTypes);

        if ( invokeMethod == null ) {
        }
        return invokeMethod;
    }
}
