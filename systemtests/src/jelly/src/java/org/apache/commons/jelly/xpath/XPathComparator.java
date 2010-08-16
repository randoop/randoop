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
package org.apache.commons.jelly.xpath;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.jelly.util.NestedRuntimeException;
import org.dom4j.Node;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

/**
 * Compares xml nodes by extracting the value at xpath and
 * comparing it.
 *
 * @author <a href="mailto:jason@jhorman.org">Jason Horman</a>
 * @version $Id: XPathComparator.java,v 1.4 2004/09/09 12:29:36 dion Exp $
 */

public class XPathComparator implements Comparator {

    /** The xpath to use to extract value from nodes to compare */
    private XPath xpath = null;

    /** Sort descending or ascending */
    private boolean descending = false;

    public XPathComparator() {

    }

    public XPathComparator(XPath xpath, boolean descending) {
        this.xpath = xpath;
        this.descending = descending;
    }

    public void setXpath(XPath xpath) {
        this.xpath = xpath;
    }

    public XPath getXpath() {
        return xpath;
    }

    public void setDescending(boolean descending) {
        this.descending = descending;
    }

    public int compare(Object o1, Object o2) {
        return compare((Node)o1, (Node)o2);
    }

    public int compare(Node n1, Node n2) {
        try {

            // apply the xpaths. not using stringValueOf since I don't
            // want all of the child nodes appended to the strings
            Object val1 = xpath.evaluate(n1);
            Object val2 = xpath.evaluate(n2);

            // return if null
            if (val1 == null || val2 == null) {
                return val1 == null ? (val2 == null ? 1 : -1) : 1;
            }

            Comparable c1 = getComparableValue(val1);
            Comparable c2 = getComparableValue(val2);

            // compare descending or ascending
            if (!descending) {
                return c1.compareTo(c2);
            } else {
                return c2.compareTo(c1);
            }

        } catch (JaxenException e) {

            throw new XPathSortException("error sorting nodes", e);

        }
    }

    /**
     * Turns the XPath result value into a Comparable object.
     */
    protected Comparable getComparableValue(Object value) {
        if (value instanceof List) {
            List list = (List) value;
            if (list.isEmpty()) {
                value = "";
            }
            value = list.get(0);
            if (value == null) {
                value = "";
            }
        }
        if (value instanceof Comparable) {
            return (Comparable) value;
        }
        else if (value instanceof Node) {
            Node node = (Node) value;
            return node.getStringValue();
        }
        return value.toString();
    }

    /**
     * My own runtime exception in case something goes wrong with sort.
     */
    public static class XPathSortException extends NestedRuntimeException {
        public XPathSortException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
