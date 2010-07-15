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
import javax.jms.MessageListener;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Performs a subscription to some JMS connection to a destination maybe with a selector.
 * A JMS MessageListener can be specified, or a special child tag can explicitly set it on
 * its parent (so a special tag could construct a MessageListener object and register it with this tag).
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class SubscribeTag extends MessageOperationTag implements ConsumerTag {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(SubscribeTag.class);

    /** the JMS Selector for the subscription */
    private String selector;

    /** The JMS MessageListener used to create the subscription */
    private MessageListener messageListener;

    public SubscribeTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {

        // evaluate body as it may contain child tags to register a MessageListener
        invokeBody(output);

        MessageListener listener = getMessageListener();
        if (listener == null) {
            throw new JellyTagException( "No messageListener attribute is specified so could not subscribe" );
        }

        // clear the listener for the next tag invocation, if caching is employed
        setMessageListener(null);


        Destination destination = null;
        try {
            destination = getDestination();
        }
        catch (JMSException e) {
            throw new JellyTagException(e);
        }

        if ( destination == null ) {
            throw new JellyTagException( "No destination specified. Either specify a 'destination' attribute or use a nested <jms:destination> tag" );
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "About to consume to: " + destination + " with listener: " + listener );
        }

        log.info( "About to consume to: " + destination + " with listener: " + listener );

        try {
            if (selector == null ) {
                getConnection().addListener( destination, listener );
            }
            else {
                getConnection().addListener( destination, selector, listener );
            }
        }
        catch (JMSException e) {
            throw new JellyTagException(e);
        }
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the optional JMS Message selector for the subscription
     */
    public void setSelector(String selector) {
        this.selector = selector;
    }


    /**
     * Returns the messageListener.
     * @return MessageListener
     */
    public MessageListener getMessageListener() {
        return messageListener;
    }


    /**
     * Sets the JMS messageListener used ot consume JMS messages on the given destination
     */
    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

}
