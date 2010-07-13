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

package org.apache.commons.jelly.tags.xml;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.xpath.XPathComparator;
import org.apache.commons.jelly.xpath.XPathTagSupport;
import org.jaxen.XPath;
import org.jaxen.JaxenException;

import java.util.List;
import java.util.Collections;

/** A tag that can sort a list of xml nodes via an xpath expression.
  *
  * @author <a href="mailto:jason@jhorman.org">Jason Horman</a>
  * @version $Id: SortTag.java,v 1.5 2004/09/09 12:24:40 dion Exp $
  */

public class SortTag extends XPathTagSupport {

    /** The list to sort */
    private List list = null;

    /** Xpath comparator for sorting */
    private XPathComparator xpCmp = null;

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if (xpCmp == null) {
            throw new MissingAttributeException( "xpCmp" );
        }
        if (list == null) {
            throw new MissingAttributeException( "list" );
        }

        Collections.sort(list, xpCmp);
    }

    /** Set the list to sort. */
    public void setList(List list) {
        this.list = list;
    }

    /** Sets the xpath expression to use to sort selected nodes.
     */
    public void setSort(XPath sortXPath) throws JaxenException {
        if (xpCmp == null) xpCmp = new XPathComparator();
        xpCmp.setXpath(sortXPath);
    }

    /**
     * Set whether to sort ascending or descending.
     */
    public void setDescending(boolean descending) {
        if (xpCmp == null) xpCmp = new XPathComparator();
        xpCmp.setDescending(descending);
    }
}
