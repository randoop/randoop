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

package org.apache.commons.jelly.tags.http;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.httpclient.methods.multipart.StringPart;

/**
 * A tag to hold a part of a multiPartPost
 *
 */
public class PartTag extends TagSupport {
    /** parameter name */
    private String _name;
    /** parameter value */
    private String _value;
    /** parameter type (like text/plain) */
    private String _contentType = "text/plain";

    /** Creates a new instance of PartTag */
    public PartTag() {
    }

    /**
     * Extend StringPart so that I can specify the content type (ex: text/plain)
     */
    private class MyStringPart extends StringPart {
      String _contentType;
      public MyStringPart(String name, String value, String contentType) {
        super(name, value, "utf-8");
        _contentType=contentType;
      }
      public String getContentType() { return _contentType; }
    }

    /**
     * Perform the tag functionality. In this case, store this parameter
     * in the &lt;mppost&gt; tag above me
     *
     * @param xmlOutput where to send output
     * @throws Exception when an error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {
        MultipartPostTag http = (MultipartPostTag) findAncestorWithClass(MultipartPostTag.class);
        StringPart sp = new MyStringPart(getName(), getValue(), getContentType());
        http.addPart(sp);
        invokeBody(xmlOutput);
    }

    //--------------------------------------------------------------------------
    // Property accessors/mutators
    //--------------------------------------------------------------------------
    /**
     * Getter for property name.
     *
     * @return Value of property name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Setter for property name.
     *
     * @param name New value of property name.
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Getter for property value.
     *
     * @return Value of property value.
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

    /**
     * Getter for property contentType.
     *
     * @return Value of contentType.
     */
    public String getContentType() {
        return _contentType;
    }

    /**
     * Setter for property contentType.
     *
     * @param value New value of contentType.
     */
    public void setContentType(String contentType) {
        _contentType = contentType;
    }

}
