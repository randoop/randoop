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
import org.apache.commons.jelly.XMLOutput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Receives a JMS message.
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.5 $
  */
public class ReceiveTag extends MessageOperationTag {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(ReceiveTag.class);

    private String var;
    private long timeout = -1L;

    public ReceiveTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        // evaluate body as it may contain a <destination> tag
        invokeBody(output);

        Message message = null;
        try {
            Destination destination = getDestination();
            if ( destination == null ) {
                throw new JellyTagException( "No destination specified. Either specify a 'destination' attribute or use a nested <jms:destination> tag" );
            }
            if ( timeout > 0 ) {
                if ( log.isDebugEnabled() ) {
                    log.debug( "Receiving message on destination: " + destination + " with timeout: " + timeout );
                }

                message = getConnection().receive( destination, timeout );
            }
            else if ( timeout == 0 ) {
                if ( log.isDebugEnabled() ) {
                    log.debug( "Receiving message on destination: " + destination + " with No Wait" );
                }

                message = getConnection().receiveNoWait( destination );
            }
            else {
                if ( log.isDebugEnabled() ) {
                    log.debug( "Receiving message on destination: " + destination );
                }
                message = getConnection().receive( destination );
            }
        }
        catch (JMSException e) {
            throw new JellyTagException(e);
        }

        onMessage( message );
    }

    // Properties
    //-------------------------------------------------------------------------
    public String getVar() {
        return var;
    }

    /**
     * Sets the variable name to create for the received message, which will be null if no
     * message could be returned in the given time period.
     */
    public void setVar(String var) {
        this.var = var;
    }

    public long getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout period in milliseconds to wait for a message. A value
     * of -1 will wait forever for a message.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * A strategy method which processes the incoming message, allowing derived classes
     * to implement different processing methods
     */
    protected void onMessage( Message message ) {
        if ( message != null ) {
            context.setVariable( var, message );
        }
        else {
            context.removeVariable( var );
        }
    }
}
