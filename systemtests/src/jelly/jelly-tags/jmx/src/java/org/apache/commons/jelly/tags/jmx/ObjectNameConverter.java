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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;

/**
 * A Converter that turns Strings into JMX ObjectName objects
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.4 $
 */
public class ObjectNameConverter implements Converter {

    private static final ObjectNameConverter instance =
        new ObjectNameConverter();

    public static ObjectNameConverter getInstance() {
        return instance;
    }

    //-------------------------------------------------------------------------
    public Object convert(Class type, Object value) {
        try {
            if (value == null) {
                return new ObjectName("");
            }
            else {
                return new ObjectName(value.toString());
            }
        }
        catch (MalformedObjectNameException e) {
            throw new ConversionException(
                "Could not convert: "
                    + value
                    + " into ObjectName. Reason: "
                    + e,
                e);
        }
    }
}