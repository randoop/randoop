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
import javax.jms.Message;
import javax.jms.JMSException;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

import org.apache.commons.messenger.Messenger;

/** A tag which creates a JMS message
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.5 $
  */
public class MessageTag extends TagSupport {

    /** The name of the Message variable that is created */
    private String var;

    /** The JMS Message created */
    private Message message;

    /** The Messenger used to access the JMS connection */
    private Messenger connection;

    public MessageTag() {
    }

    /** Adds a JMS property to the message */
    public void addProperty(String name, Object value) throws JellyTagException {
        Message message = getMessage();

        try {
            message.setObjectProperty(name, value);
        } catch (JMSException e) {
            throw new JellyTagException(e);
        }
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        if ( var == null ) {
            // expose message to parent message consumer
            SendTag tag = (SendTag) findAncestorWithClass( SendTag.class );
            if ( tag == null ) {
                throw new JellyTagException("<jms:message> tags must either have the 'var' attribute specified or be used inside a <jms:send> tag");
            }

            tag.setMessage( getMessage() );
        }
        else {

            context.setVariable( var, getMessage() );

        }
    }

    // Properties
    //-------------------------------------------------------------------------

    /** Sets the name of the variable that the message will be exported to */
    public void setVar(String var) {
        this.var = var;
    }

    public Messenger getConnection() throws JellyTagException {
        if ( connection == null ) {
            return findConnection();
        }
        return connection;
    }

    /**
     * Sets the Messenger (the JMS connection pool) that will be used to send the message
     */
    public void setConnection(Messenger connection) {
        this.connection = connection;
    }

    public Message getMessage() throws JellyTagException {
        if ( message == null ) {
            message = createMessage();
        }
        return message;
    }


    // JMS related properties

    /**
     * Sets the JMS Correlation ID to be used on the message
     */
    public void setCorrelationID(String correlationID) throws JellyTagException {
        try {
            getMessage().setJMSCorrelationID(correlationID);
        }
        catch (JMSException e) {
            throw new JellyTagException(e);
        }
    }

    /**
     * Sets the reply-to destination to add to the message
     */
    public void setReplyTo(Destination destination) throws JellyTagException {
        try {
            getMessage().setJMSReplyTo(destination);
        }
        catch (JMSException e) {
            throw new JellyTagException(e);
        }
    }

    /**
     * Sets the type name of the message
     */
    public void setType(String type) throws JellyTagException {
        try {
            getMessage().setJMSType(type);
        }
        catch (JMSException e) {
            throw new JellyTagException(e);
        }
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected Messenger findConnection() throws JellyTagException {
        ConnectionContext messengerTag = (ConnectionContext) findAncestorWithClass( ConnectionContext.class );
        if ( messengerTag == null ) {
            throw new JellyTagException("This tag must be within a <jms:connection> tag or the 'connection' attribute should be specified");
        }

        try {
            return messengerTag.getConnection();
        }
        catch (JMSException e) {
            throw new JellyTagException(e);
        }
    }

    protected Message createMessage() throws JellyTagException {
        try {
            return getConnection().createMessage();
        } catch (JMSException e) {
            throw new JellyTagException(e);
        }
    }
}
