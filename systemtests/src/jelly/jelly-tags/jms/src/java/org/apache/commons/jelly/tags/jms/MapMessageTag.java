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

import java.util.Iterator;
import java.util.Map;

import javax.jms.Message;
import javax.jms.MapMessage;
import javax.jms.JMSException;

import org.apache.commons.jelly.JellyTagException;

/** Creates a JMS MapMessage
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.5 $
  */
public class MapMessageTag extends MessageTag {

    public MapMessageTag() {
    }

    public void addEntry(String name, Object value) throws JellyTagException {
        MapMessage message = (MapMessage) getMessage();
        try {
            message.setObject(name, value);
        }
        catch (JMSException e) {
            throw new JellyTagException(e);
        }
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the Map of entries to be used for this Map Message
     */
    public void setMap(Map map) throws JellyTagException {
        MapMessage message = (MapMessage) getMessage();
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = entry.getKey().toString();
            Object value = entry.getValue();

            try {
                message.setObject(name, value);
            }
            catch (JMSException e) {
                throw new JellyTagException(e);
            }
        }
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected Message createMessage() throws JellyTagException {
        try {
            return getConnection().createMapMessage();
        } catch (JMSException e) {
            throw new JellyTagException(e);
        }
    }
}
