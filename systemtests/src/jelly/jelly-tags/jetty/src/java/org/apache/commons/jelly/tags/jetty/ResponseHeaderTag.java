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

package org.apache.commons.jelly.tags.jetty;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

import org.mortbay.http.HttpResponse;

/**
 * Set a response header in the request handler of a Jetty http server
 *
 * @author  rtl
 */
public class ResponseHeaderTag extends TagSupport {

    /** parameter name */
    private String _name;

    /** parameter value */
    private String _value;

    /**
     * Perform the tag functionality. In this case, set a header in the
     * http response found in the jelly context
     *
     * @param xmlOutput where to send output
     * @throws Exception when an error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {

        if (null == getName()) {
            throw new JellyTagException("<responseHeader> tag must have a name");
        }

        // get the response from the context
        HttpResponse httpResponse = (HttpResponse) getContext().getVariable("response");
        if (null == httpResponse) {
            throw new JellyTagException("HttpResponse variable not available in Jelly context");
        }

        // if value is valid then set it
        // otherwise remove the field
        if (null != getValue()) {
            httpResponse.setField(getName(), getValue());
        } else {
            httpResponse.removeField(getName());
        }

    }

    //--------------------------------------------------------------------------
    // Property accessors/mutators
    //--------------------------------------------------------------------------

    /**
     * Getter for property context path.
     *
     * @return value of property context path.
     */
    public String getName() {
        return _name;
    }

    /**
     * Setter for property context path.
     *
     * @param path New value of property context path.
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Getter for property value.
     *
     * @return value of property value.
     */
    public String getValue() {
        return _value;
    }

    /**
     * Setter for property value.
     *
     * @param value New value of property value.
     */
    public void setValue(String value) {
        _value = value;
    }

}

