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

/**
 * Test case for the PosixParser.
 *
 * @version $Revision: 695410 $, $Date: 2008-09-15 03:25:38 -0700 (Mon, 15 Sep 2008) $
 */
public class PosixParserTest extends ParserTestCase
{
    public void setUp()
    {
        super.setUp();
        parser = new PosixParser();
    }

    public void testBursting() throws Exception
    {
        String[] args = new String[] { "-acbtoast",
                                       "foo", "bar" };

        CommandLine cl = parser.parse(options, args);

        assertTrue( "Confirm -a is set", cl.hasOption("a") );
        assertTrue( "Confirm -b is set", cl.hasOption("b") );
        assertTrue( "Confirm -c is set", cl.hasOption("c") );
        assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("toast") );
        assertTrue( "Confirm size of extra args", cl.getArgList().size() == 2);
    }

    public void testUnrecognizedOptionWithBursting() throws Exception
    {
        String[] args = new String[] { "-adbtoast", "foo", "bar" };

        try
        {
            parser.parse(options, args);
            fail("UnrecognizedOptionException wasn't thrown");
        }
        catch (UnrecognizedOptionException e)
        {
            assertEquals("-adbtoast", e.getOption());
        }
    }

    public void testMissingArgWithBursting() throws Exception
    {
        String[] args = new String[] { "-acb" };

        boolean caught = false;

        try
        {
            parser.parse(options, args);
        }
        catch (MissingArgumentException e)
        {
            caught = true;
            assertEquals("option missing an argument", "b", e.getOption().getOpt());
        }

        assertTrue( "Confirm MissingArgumentException caught", caught );
    }

    public void testStopBursting() throws Exception
    {
        String[] args = new String[] { "-azc" };

        CommandLine cl = parser.parse(options, args, true);
        assertTrue( "Confirm -a is set", cl.hasOption("a") );
        assertFalse( "Confirm -c is not set", cl.hasOption("c") );

        assertTrue( "Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
        assertTrue(cl.getArgList().contains("zc"));
    }

    public void testStopBursting2() throws Exception
    {
        String[] args = new String[] { "-c",
                                       "foobar",
                                       "-btoast" };

        CommandLine cl = parser.parse(options, args, true);
        assertTrue("Confirm -c is set", cl.hasOption("c"));
        assertTrue("Confirm  2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);

        cl = parser.parse(options, cl.getArgs());

        assertTrue("Confirm -c is not set", !cl.hasOption("c"));
        assertTrue("Confirm -b is set", cl.hasOption("b"));
        assertTrue("Confirm arg of -b", cl.getOptionValue("b").equals("toast"));
        assertTrue("Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
        assertTrue("Confirm  value of extra arg: " + cl.getArgList().get(0), cl.getArgList().get(0).equals("foobar"));
    }

    /**
     * Real world test with long and short options.
     */
    public void testLongOptionWithShort() throws Exception {
        Option help = new Option("h", "help", false, "print this message");
        Option version = new Option("v", "version", false, "print version information");
        Option newRun = new Option("n", "new", false, "Create NLT cache entries only for new items");
        Option trackerRun = new Option("t", "tracker", false, "Create NLT cache entries only for tracker items");

        Option timeLimit = OptionBuilder.withLongOpt("limit").hasArg()
                                        .withValueSeparator()
                                        .withDescription("Set time limit for execution, in minutes")
                                        .create("l");

        Option age = OptionBuilder.withLongOpt("age").hasArg()
                                  .withValueSeparator()
                                  .withDescription("Age (in days) of cache item before being recomputed")
                                  .create("a");

        Option server = OptionBuilder.withLongOpt("server").hasArg()
                                     .withValueSeparator()
                                     .withDescription("The NLT server address")
                                     .create("s");

        Option numResults = OptionBuilder.withLongOpt("results").hasArg()
                                         .withValueSeparator()
                                         .withDescription("Number of results per item")
                                         .create("r");

        Option configFile = OptionBuilder.withLongOpt("file").hasArg()
                                         .withValueSeparator()
                                         .withDescription("Use the specified configuration file")
                                         .create();

        Options options = new Options();
        options.addOption(help);
        options.addOption(version);
        options.addOption(newRun);
        options.addOption(trackerRun);
        options.addOption(timeLimit);
        options.addOption(age);
        options.addOption(server);
        options.addOption(numResults);
        options.addOption(configFile);

        // create the command line parser
        CommandLineParser parser = new PosixParser();

        String[] args = new String[] {
                "-v",
                "-l",
                "10",
                "-age",
                "5",
                "-file",
                "filename"
            };

        CommandLine line = parser.parse(options, args);
        assertTrue(line.hasOption("v"));
        assertEquals(line.getOptionValue("l"), "10");
        assertEquals(line.getOptionValue("limit"), "10");
        assertEquals(line.getOptionValue("a"), "5");
        assertEquals(line.getOptionValue("age"), "5");
        assertEquals(line.getOptionValue("file"), "filename");
    }

    public void testLongWithEqualSingleDash() throws Exception
    {
        // not supported by the PosixParser
    }

    public void testShortWithEqual() throws Exception
    {
        // not supported by the PosixParser
    }
}
