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

import javax.jms.JMSException;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.messenger.Messenger;
import org.apache.commons.messenger.MessengerManager;

/** Defines a JMS connection for use by other JMS tags.
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.5 $
  */
public class ConnectionTag extends TagSupport implements ConnectionContext {

    /** The variable name to create */
    private String var;

    /** Stores the name of the map entry */
    private String name;

    /** The Messenger */
    private Messenger connection;

    // ConnectionContext interface
    //-------------------------------------------------------------------------
    public Messenger getConnection() {
        return connection;
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {

        try {
            connection = MessengerManager.get( name );
        }
        catch (JMSException e) {
            throw new JellyTagException(e);
        }

        if (connection == null) {
            throw new JellyTagException( "Could not find a JMS connection called: " + name );
        }

        if ( var != null ) {
            context.setVariable( var, connection );
        }

        invokeBody(output);
    }

    // Properties
    //-------------------------------------------------------------------------

    /** Sets the name of the Messenger (JMS connection pool) to use
      */
    public void setName(String name) {
        this.name = name;
    }

    /** Sets the variable name to use for the exported Messenger (JMS connection pool)
      */
    public void setVar(String var) {
        this.var = var;
    }
}
