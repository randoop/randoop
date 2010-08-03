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
package org.apache.commons.jelly.tags.ojb;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.ojb.broker.PersistenceBroker;

/**
 * <p>This Store tag will store the given object in ObjectBridge using
 * the given broker or it will use the parent broker tags broker instance.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.4 $
 */
public class StoreTag extends TagSupport {

    /** the value to persist */
    private Object value;

    /** The persistence broker instance */
    private PersistenceBroker broker;

    public StoreTag() {
    }


    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        if ( value == null ) {
            throw new JellyTagException( "No value is supplied!" );
        }
        getBroker().store( value );
    }

    // Properties
    //-------------------------------------------------------------------------

    /** Sets the value to be persisted */
    public void setValue(Object value) {
        this.value = value;
    }

    /** @return the persistence broker instance */
    public PersistenceBroker getBroker() {
        if (broker == null) {
            BrokerTag brokerTag = (BrokerTag) findAncestorWithClass( BrokerTag.class );
            if ( brokerTag != null ) {
                broker = brokerTag.getBroker();
            }
            else {
                broker = (PersistenceBroker) context.getVariable(
                    "org.apache.commons.jelly.ojb.Broker"
                );
            }
        }
        return broker;
    }

    /** Sets the persistence broker instance */
    public void setBroker(PersistenceBroker broker) {
        this.broker = broker;
    }
}

