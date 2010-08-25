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
package org.apache.commons.jelly.tags.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A sample bean that we can construct via Jelly tags
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.4 $
 */
public class Manager {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(Manager.class);

    private List customers = new ArrayList();

    boolean invoked = false;

    public Manager() {
    }

    public String toString() {
        return super.toString() + "[customers=" + customers + "]";
    }

    /**
     * The invoke method which is called when the bean is constructed
     */
    public void run() {
        invoked = true;

        log.info("Invoked the run() method with customers: " + customers);
    }


    public List getCustomers() {
        return customers;
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void removeCustomer(Customer customer) {
        customers.remove(customer);
    }

    /**
     * @return boolean
     */
    public boolean isInvoked() {
        return invoked;
    }

    /**
     * Sets the invoked.
     * @param invoked The invoked to set
     */
    public void setInvoked(boolean invoked) {
        this.invoked = invoked;
    }

}
