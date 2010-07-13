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

package org.apache.commons.jelly;

/** 
 * <p><code>LocationAware</code> represents a Tag or Exception which is location aware.
 * That is to say it is capable of recording where in a Jelly script a tag or exception
 * is used which can aid debugging and tracing.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.11 $
 */

public interface LocationAware {
    
    /** 
     * @return the line number of the tag 
     */
    int getLineNumber();
    
    /** 
     * Sets the line number of the tag 
     */
    void setLineNumber(int lineNumber);

    /** 
     * @return the column number of the tag 
     */
    int getColumnNumber();
    
    /** 
     * Sets the column number of the tag 
     */
    void setColumnNumber(int columnNumber);

    /** 
     * @return the Jelly file which caused the problem 
     */
    String getFileName();
    
    /** 
     * Sets the Jelly file which caused the problem 
     */
    void setFileName(String fileName);
    
    /** 
     * @return the element name which caused the problem
     */
    String getElementName();

    /** 
     * Sets the element name which caused the problem
     */
    void setElementName(String elementName);
}
