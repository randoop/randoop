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
package org.apache.commons.jelly.tags.log;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An abstract base class for any logging tag..
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public abstract class LogTagSupport extends TagSupport {

    private Log log;
    private boolean encode;

    public LogTagSupport() {
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the Log being used by this tag. If none is returned then a new one will be created.
     */
    public Log getLog() {
        if ( log == null ) {
            // use a default Log
            // ### we could inherit from a parent tag?
            log = LogFactory.getLog( getClass() );
        }
        return log;
    }

    /**
     * Sets the name of the logger to use
     */
    public void setName(String name) {
        setLog( LogFactory.getLog(name) );
    }

    /** Sets the Log instance to use for logging. */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Returns whether the body of this tag will be XML encoded or not.
     */
    public boolean isEncode() {
        return encode;
    }

    /**
     * Sets whether the body of the tag should be encoded as text (so that &lt; and &gt; are
     * encoded as &amp;lt; and &amp;gt;) or leave the text as XML which is the default.
     */
    public void setEncode(boolean encode) {
        this.encode = encode;
    }

}
