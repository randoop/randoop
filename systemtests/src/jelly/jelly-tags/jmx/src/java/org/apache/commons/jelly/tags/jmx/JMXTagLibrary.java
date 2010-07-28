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
package org.apache.commons.jelly.tags.jmx;

import javax.management.ObjectName;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.jelly.tags.bean.BeanTag;
import org.apache.commons.jelly.tags.bean.BeanTagLibrary;


/**
 * A Tag library for creating an instantiating Java Beans and MBeans
 * and registering them with JMX. Support for setting JMX attributes
 * and invoking JMX operations is also supported.
 *
 * @author
 * @version $Revision: 1.4 $
 */
public class JMXTagLibrary extends BeanTagLibrary {

    static {
        // register the various beanutils Converters from Strings to various JMX types
        ConvertUtils.register( new ObjectNameConverter(), ObjectName.class );
    }

    public JMXTagLibrary() {
        registerTag("mbean", BeanTag.class);
        registerTag("operation", OperationTag.class);
        registerTag("register", RegisterTag.class);
        registerTag("server", ServerTag.class);
    }
}
