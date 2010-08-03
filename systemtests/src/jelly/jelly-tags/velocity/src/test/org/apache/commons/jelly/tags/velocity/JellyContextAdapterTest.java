package org.apache.commons.jelly.tags.velocity;

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

import java.util.Set;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.collections.CollectionUtils;

/**
 * Unit test for <code>JellyContextAdapter</code>.
 *
 * @author <a href="mailto:pete-apache-dev@kazmier.com">Pete Kazmier</a>
 * @version $Id: JellyContextAdapterTest.java,v 1.3 2004/09/09 12:23:17 dion Exp $
 */
public class JellyContextAdapterTest extends TestCase
{
    JellyContext jellyContext;
    JellyContextAdapter adapter;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public JellyContextAdapterTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( JellyContextAdapterTest.class );
    }

    public void setUp()
    {
        jellyContext = new JellyContext();
        adapter = new JellyContextAdapter( jellyContext );
    }

    /**
     * Test the behavior of null keys.
     */
    public void testNullKey()
    {
        adapter.setReadOnly( false );
        adapter.put( null, new Object() );

        assertTrue( adapter.get( null ) == null );
    }

    /**
     * Test the behavior of null values.
     */
    public void testNullValue()
    {
        adapter.setReadOnly( false );
        adapter.put( "key", null );

        assertTrue( adapter.get( "key" ) == null );
    }

    /**
     * Test that items can be added and retrieved from a read-write
     * adpater.  Also verify the key/value pair was actually inserted
     * into the JellyContext.
     */
    public void testReadWritePut()
    {
        Object value = new Object();

        adapter.setReadOnly( false );
        adapter.put( "key", value );

        assertTrue( "adapter: did not return the original value",
                adapter.get( "key" ) == value );

        assertTrue( "jellyContext: did not return the original value",
                jellyContext.getVariable( "key" ) == value );
    }

    /**
     * Test that items can be added and retrieved from a read-only
     * adapter.  Also verify the key/value pair was not inserted into
     * the JellyContext.
     */
    public void testReadOnlyPut()
    {
        Object value = new Object();

        adapter.setReadOnly( true );
        adapter.put( "key", value );

        assertTrue( "adapter: did not return the original value",
                adapter.get( "key" ) == value );

        assertTrue( "jellyContext: must return null when adapter is readonly",
                jellyContext.getVariable( "key" ) == null );
    }

    /**
     * Test that items can be removed from a read-write context.  Also
     * verify that the item is removed from the JellyContext.
     */
    public void testReadWriteRemove()
    {
        Object value = new Object();

        adapter.setReadOnly( false );
        adapter.put( "key", value );
        Object oldValue = adapter.remove( "key" );

        assertTrue( "Value returned from remove() is not the original",
                value == oldValue );

        assertTrue( "adapter: after removal of key, value should be null",
                adapter.get( "key" ) == null );

        assertTrue( "jellyContext: after removal of key, value should be null",
                jellyContext.getVariable( "key" ) == null );

        assertTrue( "Removal of non-existent key should return null",
                adapter.remove( "non-existent key" ) == null );
    }

    /**
     * Test that items can be removed from a read-only context.  Also
     * verify that the JellyContext is not impacted by removal of keys.
     */
    public void testReadOnlyRemove()
    {
        Object value = new Object();

        adapter.setReadOnly( true );
        adapter.put( "key", value );

        Object oldValue = adapter.remove( "key" );

        assertTrue( "Value returned from remove() is not the original",
                value == oldValue );

        assertTrue( "adapter: after removal of key, value should be null",
                adapter.get( "key" ) == null );

        assertTrue( "jellyContext: value should not be affected.",
                jellyContext.getVariable( "key" ) == null );

        assertTrue( "Removal of non-existent key should return null",
                adapter.remove( "non-existent key" ) == null );
    }

    /**
     * Test that items can shadow or hide items in the JellyContext.
     * Removal of a key in the private context will unveil the key in
     * the JellyContext if it exists.
     */
    public void testReadOnlyShadowingRemove()
    {
        Object value1 = new Object();
        Object value2 = new Object();

        adapter.setReadOnly( true );
        adapter.put( "key", value1 );
        jellyContext.setVariable( "key", value2 );

        assertTrue( "adapter: before removal of key, value should be 1",
                adapter.get( "key" ) == value1 );

        adapter.remove( "key" );

        assertTrue( "adapter: after removal of key, value should be 2",
                adapter.get( "key" ) == value2 );

        assertTrue( "jellyContext: value should not be affected.",
                jellyContext.getVariable( "key" ) == value2 );
    }

    /**
     * Test the containsKey method in a read-write adapter.
     */
    public void testReadWriteContainsKey()
    {
        Object value1 = new Object();
        Object value2 = new Object();

        adapter.setReadOnly( false );
        adapter.put( "key1", value1 );
        jellyContext.setVariable( "key2", value2 );

        assertTrue( "adapter: did not contain the key",
                adapter.containsKey( "key1" ) );

        assertTrue( "adapter: should contain the key",
                adapter.containsKey( "key2" ) );

        assertTrue( "jellyContext: did not contain the key",
                jellyContext.getVariable( "key1" ) != null );
    }

    /**
     * Test the containsKey method in a read-only adapter.
     */
    public void testReadOnlyContainsKey()
    {
        Object value1= new Object();
        Object value2 = new Object();

        adapter.setReadOnly( true );
        adapter.put( "key1", value1 );
        jellyContext.setVariable( "key2", value2 );

        assertTrue( "adapter: did not contain the key",
                adapter.containsKey( "key1" ) );

        assertTrue( "adapter: should not contain the key",
                adapter.containsKey( "key2" ) );

        assertTrue( "jellyContext: should not contain the key",
                jellyContext.getVariable( "key1" ) == null );
    }

    /**
     * Test the getKeys method of a read-write adapter.
     */
    public void testReadWriteGetKeys()
    {
        Object value1 = new Object();
        Object value2 = new Object();
        Object value3 = new Object();

        adapter.setReadOnly( false );
        adapter.put( "key1", value1 );
        adapter.put( "key2", value2 );
        jellyContext.setVariable( "key3", value3 );

        Set expectedKeys = new HashSet();
        expectedKeys.add( "key1" );
        expectedKeys.add( "key2" );
        expectedKeys.add( "key3" );

        Set actualKeys = new HashSet();
        CollectionUtils.addAll(actualKeys, adapter.getKeys());

        assertTrue( "adapter: does not contain the correct key set",
                actualKeys.containsAll( expectedKeys ) );
    }

    /**
     * Test the getKeys method of a read-only adapter.
     */
    public void testReadOnlyGetKeys()
    {
        Object value1 = new Object();
        Object value2 = new Object();
        Object value3 = new Object();

        adapter.setReadOnly( true );
        adapter.put( "key1", value1 );
        adapter.put( "key2", value2 );
        jellyContext.setVariable( "key3", value3 );

        Set expectedKeys = new HashSet();
        expectedKeys.add( "key1" );
        expectedKeys.add( "key2" );

        Set actualKeys = new HashSet();
        CollectionUtils.addAll(actualKeys, adapter.getKeys());

        assertTrue( "adapter: does not contain the correct key set",
                actualKeys.containsAll( expectedKeys ) );
    }
}
