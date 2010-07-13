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
 * Set the response code in the request handler of a Jetty http server
 *
 * @author  rtl
 */
public class ResponseCodeTag extends TagSupport {

    /** parameter value */
    private int _value;

    /**
     * Perform the tag functionality. In this case, set the response code in the
     * http response found in the jelly context
     *
     * @param xmlOutput where to send output
     * @throws Exception when an error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {

        if (getValue() <= 100) {
            throw new JellyTagException("<responseCode> tag must have a value of at least 100");
        }

        // get the response from the context
        HttpResponse httpResponse = (HttpResponse) getContext().getVariable("response");
        if (null == httpResponse) {
            throw new JellyTagException("HttpResponse variable not available in Jelly context");
        }

        // set response code
        httpResponse.setStatus(getValue());

    }

    //--------------------------------------------------------------------------
    // Property accessors/mutators
    //--------------------------------------------------------------------------

    /**
     * Getter for property value.
     *
     * @return value of property value.
     */
    public int getValue() {
        return _value;
    }

    /**
     * Setter for property value.
     *
     * @param value New value of property value.
     */
    public void setValue(int value) {
        _value = value;
    }

}

