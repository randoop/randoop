package org.apache.commons.jelly.tags.velocity;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
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

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

import org.apache.commons.jelly.JellyContext;
import org.apache.velocity.context.Context;

/**
 * Adapts a JellyContext for use as a Velocity Context.  This context
 * can be used in either read-only or read-write mode.  When used as a
 * read-only adapter, items <tt>put</tt> or <tt>remove</tt>ed from the
 * Velocity context are not permitted to propogate to the JellyContext,
 * which is the default behavior.  The adapter can also be used in a
 * read-write mode.  This permits changes made by Velocity to propogate
 * to the JellyContext.
 *
 * @author <a href="mailto:pete-apache-dev@kazmier.com">Pete Kazmier</a>
 * @version $Id: JellyContextAdapter.java,v 1.4 2004/09/09 12:23:16 dion Exp $
 */
public class JellyContextAdapter implements Context
{
    /** Flag to indicate read-only or read-write mode */
    private boolean readOnly = true;

    /** The JellyContext being adapted */
    private JellyContext jellyContext;

    /** The store for Velocity in the event the adpater is read-only */
    private HashMap privateContext = new HashMap();

    /**
     * Constructor.
     *
     * @param jellyContext The JellyContext to adapt
     */
    public JellyContextAdapter( JellyContext jellyContext )
    {
        this.jellyContext = jellyContext;
    }

    /**
     * Sets the read-only flag for this adapter.  If the read-only flag
     * is set, changes to the Velocity Context will not be propogated to
     * the JellyContext.  Turning the read-only flag off enables changes
     * to propogate.
     *
     * @param readOnly If this parameter is <tt>true</tt>, the adapter
     * becomes read-only.  Setting the parameter to <tt>false</tt> the
     * adapter becomes read-write.
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    /**
     * Tests if the adapter is read-only.
     *
     * @return <tt>true</tt> if the adpater is read-only; otherwise
     * returns <tt>false</tt>.
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }

    public boolean containsKey( Object key )
    {
        if ( key == null )
        {
            return false;
        }

        if ( readOnly && privateContext.containsKey( key ) )
        {
            return true;
        }

        return jellyContext.getVariable( key.toString() ) != null ? true : false;
    }

    public Object get( String key )
    {
        if ( key == null )
        {
            return null;
        }

        if ( readOnly && privateContext.containsKey( key ) )
        {
            return privateContext.get( key );
        }

        return jellyContext.getVariable( key );
    }

    public Object[] getKeys()
    {
        Set keys = jellyContext.getVariables().keySet();

        if ( readOnly )
        {
            HashSet combinedKeys = new HashSet( keys );
            combinedKeys.addAll( privateContext.keySet() );
            keys = combinedKeys;
        }

        return keys.toArray();
    }

    public Object put( String key, Object value )
    {
        Object oldValue;

        if ( key == null || value == null )
        {
            return null;
        }

        if ( readOnly )
        {
            oldValue = privateContext.put( key, value );
        }
        else
        {
            oldValue = jellyContext.getVariable( key );
            jellyContext.setVariable( key, value );
        }

        return oldValue;
    }

    public Object remove( Object key )
    {
        Object oldValue;

        if ( key == null )
        {
            return null;
        }

        if ( readOnly )
        {
            oldValue = privateContext.remove( key );
        }
        else
        {
            oldValue = jellyContext.getVariable( key.toString() );
            jellyContext.removeVariable( key.toString() );
        }

        return oldValue;
    }
}

