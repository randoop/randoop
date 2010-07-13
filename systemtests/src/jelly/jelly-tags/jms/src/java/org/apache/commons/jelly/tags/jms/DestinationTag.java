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

import javax.jms.Destination;
import javax.jms.JMSException;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.messenger.Messenger;

/** Creates a Destination object from a String name.
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.5 $
  */
public class DestinationTag extends TagSupport {

    /** The variable name to create */
    private String var;

    /** Stores the name of the map entry */
    private String name;

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        ConnectionContext messengerTag = (ConnectionContext) findAncestorWithClass( ConnectionContext.class );
        if ( messengerTag == null ) {
            throw new JellyTagException("<jms:destination> tag must be within a <jms:connection> or <jms:send> or <jms:receive> tag");
        }

        Destination destination = null;
        try {
            Messenger messenger = messengerTag.getConnection();
            if (messenger == null) {
                throw new JellyTagException("No JMS Connection could be found!" );
            }
            String subject = (name != null) ? name : getBodyText();
            destination = messenger.getDestination( subject );
        }
        catch (JMSException e) {
            throw new JellyTagException(e);
        }

        if ( var != null ) {
            context.setVariable( var, destination );
        }
        else {
            MessageOperationTag tag = (MessageOperationTag) findAncestorWithClass( MessageOperationTag.class );
            if ( tag == null ) {
                throw new JellyTagException("<jms:destination> tag must be within a <jms:send> or <jms:receive> tag or the 'var' attribute should be specified");
            }
            tag.setDestination( destination );
        }
    }


    // Properties
    //-------------------------------------------------------------------------

    /** Sets the name of the Destination
      */
    public void setName(String name) {
        this.name = name;
    }

    /** Sets the variable name to use for the Destination
      */
    public void setVar(String var) {
        this.var = var;
    }
}
