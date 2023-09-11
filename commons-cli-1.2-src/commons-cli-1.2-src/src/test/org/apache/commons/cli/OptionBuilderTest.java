/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.cli;

import junit.framework.TestCase;

public class OptionBuilderTest extends TestCase {

    public void testCompleteOption( ) {
        Option simple = OptionBuilder.withLongOpt( "simple option")
                                     .hasArg( )
                                     .isRequired( )
                                     .hasArgs( )
                                     .withType( new Float( 10 ) )
                                     .withDescription( "this is a simple option" )
                                     .create( 's' );

        assertEquals( "s", simple.getOpt() );
        assertEquals( "simple option", simple.getLongOpt() );
        assertEquals( "this is a simple option", simple.getDescription() );
        assertEquals( simple.getType().getClass(), Float.class );
        assertTrue( simple.hasArg() );
        assertTrue( simple.isRequired() );
        assertTrue( simple.hasArgs() );
    }

    public void testTwoCompleteOptions( ) {
        Option simple = OptionBuilder.withLongOpt( "simple option")
                                     .hasArg( )
                                     .isRequired( )
                                     .hasArgs( )
                                     .withType( new Float( 10 ) )
                                     .withDescription( "this is a simple option" )
                                     .create( 's' );

        assertEquals( "s", simple.getOpt() );
        assertEquals( "simple option", simple.getLongOpt() );
        assertEquals( "this is a simple option", simple.getDescription() );
        assertEquals( simple.getType().getClass(), Float.class );
        assertTrue( simple.hasArg() );
        assertTrue( simple.isRequired() );
        assertTrue( simple.hasArgs() );

        simple = OptionBuilder.withLongOpt( "dimple option")
                              .hasArg( )
                              .withDescription( "this is a dimple option" )
                              .create( 'd' );

        assertEquals( "d", simple.getOpt() );
        assertEquals( "dimple option", simple.getLongOpt() );
        assertEquals( "this is a dimple option", simple.getDescription() );
        assertNull( simple.getType() );
        assertTrue( simple.hasArg() );
        assertTrue( !simple.isRequired() );
        assertTrue( !simple.hasArgs() );
    }

    public void testBaseOptionCharOpt() {
        Option base = OptionBuilder.withDescription( "option description")
                                   .create( 'o' );

        assertEquals( "o", base.getOpt() );
        assertEquals( "option description", base.getDescription() );
        assertTrue( !base.hasArg() );
    }

    public void testBaseOptionStringOpt() {
        Option base = OptionBuilder.withDescription( "option description")
                                   .create( "o" );

        assertEquals( "o", base.getOpt() );
        assertEquals( "option description", base.getDescription() );
        assertTrue( !base.hasArg() );
    }

    public void testSpecialOptChars() throws Exception
    {
        // '?'
        Option opt1 = OptionBuilder.withDescription("help options").create('?');
        assertEquals("?", opt1.getOpt());

        // '@'
        Option opt2 = OptionBuilder.withDescription("read from stdin").create('@');
        assertEquals("@", opt2.getOpt());
    }

    public void testOptionArgNumbers()
    {
        Option opt = OptionBuilder.withDescription( "option description" )
                                  .hasArgs( 2 )
                                  .create( 'o' );
        assertEquals( 2, opt.getArgs() );
    }

    public void testIllegalOptions() {
        // bad single character option
        try {
            OptionBuilder.withDescription( "option description" ).create( '"' );
            fail( "IllegalArgumentException not caught" );
        }
        catch( IllegalArgumentException exp ) {
            // success
        }

        // bad character in option string
        try {
            Option opt = OptionBuilder.create( "opt`" );
            fail( "IllegalArgumentException not caught" );
        }
        catch( IllegalArgumentException exp ) {
            // success
        }

        // valid option 
        try {
            Option opt = OptionBuilder.create( "opt" );
            // success
        }
        catch( IllegalArgumentException exp ) {
            fail( "IllegalArgumentException caught" );
        }
    }

    public void testCreateIncompleteOption() {
        try
        {
            OptionBuilder.hasArg().create();
            fail("Incomplete option should be rejected");
        }
        catch (IllegalArgumentException e)
        {
            // expected
            
            // implicitly reset the builder
            OptionBuilder.create( "opt" );
        }
    }

    public void testBuilderIsResettedAlways() {
        try
        {
            OptionBuilder.withDescription("JUnit").create('"');
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        assertNull("we inherited a description", OptionBuilder.create('x').getDescription());

        try
        {
            OptionBuilder.withDescription("JUnit").create();
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        assertNull("we inherited a description", OptionBuilder.create('x').getDescription());
    }
}
