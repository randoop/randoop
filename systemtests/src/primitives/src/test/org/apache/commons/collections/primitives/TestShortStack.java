/*
 *   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.apache.commons.collections.primitives ;


import java.util.EmptyStackException;

import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the ShortStack class.
 *
 * @author <a href="mailto:directory-dev@incubator.apache.org">Apache Directory Project</a>
 * @version $Revision: 1.3 $ $Date: 2004/07/13 19:57:02 $
 */
public class TestShortStack extends TestCase
{
    ShortStack stack = null ;
    
    
    /**
     * Runs the test. 
     * 
     * @param args nada
     */
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( TestShortStack.class ) ;
    }

    public static TestSuite suite() {
        return new TestSuite(TestShortStack.class);
    }

    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp() ;
        stack = new ShortStack() ;
    }
    
    
    /**
     * Constructor for IntStackTest.
     * @param arg0
     */
    public TestShortStack( String arg0 )
    {
        super( arg0 ) ;
    }

    
    public void testEmpty()
    {
        assertTrue( "Newly created stacks should be empty", stack.empty() ) ;
        stack.push( ( short ) 12342 ) ;
        assertFalse( "Stack with item should not be empty", stack.empty() ) ;
        stack.pop() ;
        assertTrue( "Stack last int popped should be empty", stack.empty() ) ;
    }

    
    public void testPeek()
    {
        try
        {
            stack.peek() ;
            throw new AssertionError( 
                    "Peek should have thrown an EmptyStackException" ) ;
        }
        catch( EmptyStackException e )
        {
            assertNotNull( "EmptyStackException should not be null", e ) ;
        }
        
        for( int ii = 0; ii < 10; ii++ )
        {    
            stack.push( ( short ) ii ) ;
            assertTrue( ii == stack.peek() ) ;
        }
    }

    
    public void testPop()
    {
        try
        {
            stack.pop() ;
            throw new AssertionError( 
                    "Pop should have thrown an EmptyStackException" ) ;
        }
        catch( EmptyStackException e )
        {
            assertNotNull( "EmptyStackException should not be null", e ) ;
        }
        
        for( short ii = 0; ii < 10; ii++ )
        {    
            stack.push( ii ) ;
            assertTrue( ii == stack.pop() ) ;
        }

        for( short ii = 0; ii < 10; ii++ )
        {    
            stack.push( ii ) ;
        }
        for( short ii = 10; ii < 0; ii-- )
        {    
            stack.push( ii ) ;
            assertTrue( ii == stack.pop() ) ;
        }
    }

    
    public void testPush()
    {
        stack.push( ( short ) 0 ) ;
        stack.push( ( short ) 0 ) ;
        assertFalse( stack.empty() ) ;
        assertTrue( 0 == stack.pop() ) ;
        assertTrue( 0 == stack.pop() ) ;
    }

    
    public void testSearch()
    {
        stack.push( ( short ) 0 ) ;
        stack.push( ( short ) 1 ) ;
        assertTrue( 2 == stack.search( ( short ) 0 ) ) ;
        stack.push( ( short ) 0 ) ;
        assertTrue( 1 == stack.search( ( short ) 0 ) ) ;
        stack.push( ( short ) 0 ) ;
        assertTrue( 3 == stack.search( ( short ) 1 ) ) ;
        assertTrue( -1 == stack.search( ( short ) 44 ) ) ;
    }

    public void testArrayConstructor() {
        short[] array = { 1, 2, 3, 4 };
        stack  = new ShortStack(array);
        assertEquals(array.length,stack.size());
        for(int i=array.length-1;i>=0;i--) {
            assertEquals(array[i],stack.pop());
        }
    }
    
    public void testPeekN() {
        short[] array = { 1, 2, 3, 4 };
        stack  = new ShortStack(array);
        for(int i=array.length-1;i>=0;i--) {
            assertEquals(array[i],stack.peek((array.length-1)-i));
        }
    }
}
