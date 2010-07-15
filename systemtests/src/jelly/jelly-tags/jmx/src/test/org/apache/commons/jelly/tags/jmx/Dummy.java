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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A dummy MBean used for the demo
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.4 $
 */
public class Dummy implements DummyMBean {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(Dummy.class);

    private String name = "James";
    private int count;

    public void doSomething() {
        ++count;
        log.info("Do something! on: " + this);
    }

    public String toString() {
        return super.toString() + "[name=" + name + "]";
    }


    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return int
     */
    public int getCount() {
        return count;
    }

    /**
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the count.
     * @param count The count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }


}
