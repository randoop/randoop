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
package org.apache.commons.jelly.tags.jms;

import javax.jms.Message;
import javax.jms.JMSException;

import org.apache.commons.jelly.JellyTagException;

/** Creates a JMS TextMessage
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.5 $
  */
public class TextMessageTag extends MessageTag {

    private String text;

    public TextMessageTag() {
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the body of the message, a String. If this value is not set or
     * the value is null then the content of the tag will be used instead.
     */
    public void setText(String text) {
        this.text = text;
    }


    // Implementation methods
    //-------------------------------------------------------------------------
    protected Message createMessage() throws JellyTagException {
        String value = (text != null) ? text : getBodyText();
        try {
            return getConnection().createTextMessage(value);
        } catch (JMSException e) {
            throw new JellyTagException(e);
        }
    }
}
