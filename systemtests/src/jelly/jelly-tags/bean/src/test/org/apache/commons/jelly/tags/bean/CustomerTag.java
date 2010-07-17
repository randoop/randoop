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

import org.apache.commons.jelly.tags.core.UseBeanTag;

/**
 * A first-class tag which is used to demonstrate using the bean tag library
 * in conjunction with an existing tag library. The only requirement is that
 * we implement the BeanSource interface, and that the bean returned from the
 * getBean() method supplies the appropriate create[nested tag name] and/or
 * add[nested tag name] methods.
 *
 * @author Christian Sell
 * @version CustomerTag.java,v 1.1 2003/01/21 15:16:32 jstrachan Exp
 */
public class CustomerTag extends UseBeanTag {
    public CustomerTag() {
        super(Customer.class);
    }
}
