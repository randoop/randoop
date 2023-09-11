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

/**
 * @author John Keyes (john at integralsource.com)
 * @version $Revision: 678662 $
 */
public class ParseRequiredTest extends TestCase
{
    private Options _options = null;
    private CommandLineParser parser = new PosixParser();

    public void setUp()
    {
        _options = new Options()
            .addOption("a",
                       "enable-a",
                       false,
                       "turn [a] on or off")
            .addOption( OptionBuilder.withLongOpt( "bfile" )
                                     .hasArg()
                                     .isRequired()
                                     .withDescription( "set the value of [b]" )
                                     .create( 'b' ) );
    }

    public void testWithRequiredOption() throws Exception
    {
        String[] args = new String[] {  "-b", "file" };

        CommandLine cl = parser.parse(_options,args);

        assertTrue( "Confirm -a is NOT set", !cl.hasOption("a") );
        assertTrue( "Confirm -b is set", cl.hasOption("b") );
        assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("file") );
        assertTrue( "Confirm NO of extra args", cl.getArgList().size() == 0);
    }

    public void testOptionAndRequiredOption() throws Exception
    {
        String[] args = new String[] {  "-a", "-b", "file" };

        CommandLine cl = parser.parse(_options,args);

        assertTrue( "Confirm -a is set", cl.hasOption("a") );
        assertTrue( "Confirm -b is set", cl.hasOption("b") );
        assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("file") );
        assertTrue( "Confirm NO of extra args", cl.getArgList().size() == 0);
    }

    public void testMissingRequiredOption()
    {
        String[] args = new String[] { "-a" };

        try
        {
            CommandLine cl = parser.parse(_options,args);
            fail( "exception should have been thrown" );
        }
        catch (MissingOptionException e)
        {
            assertEquals( "Incorrect exception message", "Missing required option: b", e.getMessage() );
            assertTrue(e.getMissingOptions().contains("b"));
        }
        catch (ParseException e)
        {
            fail( "expected to catch MissingOptionException" );
        }
    }

    public void testMissingRequiredOptions()
    {
        String[] args = new String[] { "-a" };

        _options.addOption( OptionBuilder.withLongOpt( "cfile" )
                                     .hasArg()
                                     .isRequired()
                                     .withDescription( "set the value of [c]" )
                                     .create( 'c' ) );

        try
        {
            CommandLine cl = parser.parse(_options,args);
            fail( "exception should have been thrown" );
        }
        catch (MissingOptionException e)
        {
            assertEquals( "Incorrect exception message", "Missing required options: b, c", e.getMessage() );
            assertTrue(e.getMissingOptions().contains("b"));
            assertTrue(e.getMissingOptions().contains("c"));
        }
        catch (ParseException e)
        {
            fail( "expected to catch MissingOptionException" );
        }
    }

    public void testReuseOptionsTwice() throws Exception
    {
        Options opts = new Options();
		opts.addOption(OptionBuilder.isRequired().create('v'));

		GnuParser parser = new GnuParser();

        // first parsing
        parser.parse(opts, new String[] { "-v" });

        try
        {
            // second parsing, with the same Options instance and an invalid command line
            parser.parse(opts, new String[0]);
            fail("MissingOptionException not thrown");
        }
        catch (MissingOptionException e)
        {
            // expected
        }
    }

}
