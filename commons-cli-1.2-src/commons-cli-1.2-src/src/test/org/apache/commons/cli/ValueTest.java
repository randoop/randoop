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

import java.util.Arrays;
import java.util.Properties;

import junit.framework.TestCase;

public class ValueTest extends TestCase
{
    private CommandLine _cl = null;
    private Options opts = new Options();

    public void setUp() throws Exception
    {
        opts.addOption("a", false, "toggle -a");
        opts.addOption("b", true, "set -b");
        opts.addOption("c", "c", false, "toggle -c");
        opts.addOption("d", "d", true, "set -d");

        opts.addOption(OptionBuilder.hasOptionalArg().create('e'));
        opts.addOption(OptionBuilder.hasOptionalArg().withLongOpt("fish").create());
        opts.addOption(OptionBuilder.hasOptionalArgs().withLongOpt("gravy").create());
        opts.addOption(OptionBuilder.hasOptionalArgs(2).withLongOpt("hide").create());
        opts.addOption(OptionBuilder.hasOptionalArgs(2).create('i'));
        opts.addOption(OptionBuilder.hasOptionalArgs().create('j'));
        opts.addOption(OptionBuilder.hasArgs().withValueSeparator(',').create('k'));

        String[] args = new String[] { "-a",
            "-b", "foo",
            "--c",
            "--d", "bar" 
        };

        Parser parser = new PosixParser();
        _cl = parser.parse(opts,args);
    }

    public void testShortNoArg()
    {
        assertTrue( _cl.hasOption("a") );
        assertNull( _cl.getOptionValue("a") );
    }

    public void testShortWithArg()
    {
        assertTrue( _cl.hasOption("b") );
        assertNotNull( _cl.getOptionValue("b") );
        assertEquals( _cl.getOptionValue("b"), "foo");
    }

    public void testLongNoArg()
    {
        assertTrue( _cl.hasOption("c") );
        assertNull( _cl.getOptionValue("c") );
    }

    public void testLongWithArg()
    {
        assertTrue( _cl.hasOption("d") );
        assertNotNull( _cl.getOptionValue("d") );
        assertEquals( _cl.getOptionValue("d"), "bar");
    }

    public void testShortOptionalArgNoValue() throws Exception
    {
        String[] args = new String[] { "-e" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("e") );
        assertNull( cmd.getOptionValue("e") );
    }

    public void testShortOptionalArgValue() throws Exception
    {
        String[] args = new String[] { "-e", "everything" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("e") );
        assertEquals( "everything", cmd.getOptionValue("e") );
    }

    public void testLongOptionalNoValue() throws Exception
    {
        String[] args = new String[] { "--fish" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("fish") );
        assertNull( cmd.getOptionValue("fish") );
    }

    public void testLongOptionalArgValue() throws Exception
    {
        String[] args = new String[] { "--fish", "face" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("fish") );
        assertEquals( "face", cmd.getOptionValue("fish") );
    }

    public void testShortOptionalArgValues() throws Exception
    {
        String[] args = new String[] { "-j", "ink", "idea" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("j") );
        assertEquals( "ink", cmd.getOptionValue("j") );
        assertEquals( "ink", cmd.getOptionValues("j")[0] );
        assertEquals( "idea", cmd.getOptionValues("j")[1] );
        assertEquals( cmd.getArgs().length, 0 );
    }

    public void testLongOptionalArgValues() throws Exception
    {
        String[] args = new String[] { "--gravy", "gold", "garden" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("gravy") );
        assertEquals( "gold", cmd.getOptionValue("gravy") );
        assertEquals( "gold", cmd.getOptionValues("gravy")[0] );
        assertEquals( "garden", cmd.getOptionValues("gravy")[1] );
        assertEquals( cmd.getArgs().length, 0 );
    }

    public void testShortOptionalNArgValues() throws Exception
    {
        String[] args = new String[] { "-i", "ink", "idea", "isotope", "ice" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("i") );
        assertEquals( "ink", cmd.getOptionValue("i") );
        assertEquals( "ink", cmd.getOptionValues("i")[0] );
        assertEquals( "idea", cmd.getOptionValues("i")[1] );
        assertEquals( cmd.getArgs().length, 2 );
        assertEquals( "isotope", cmd.getArgs()[0] );
        assertEquals( "ice", cmd.getArgs()[1] );
    }

    public void testLongOptionalNArgValues() throws Exception
    {
        String[] args = new String[] { 
            "--hide", "house", "hair", "head"
        };

        Parser parser = new PosixParser();

        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("hide") );
        assertEquals( "house", cmd.getOptionValue("hide") );
        assertEquals( "house", cmd.getOptionValues("hide")[0] );
        assertEquals( "hair", cmd.getOptionValues("hide")[1] );
        assertEquals( cmd.getArgs().length, 1 );
        assertEquals( "head", cmd.getArgs()[0] );
    }

    public void testPropertyOptionSingularValue() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty( "hide", "seek" );

        Parser parser = new PosixParser();
        
        CommandLine cmd = parser.parse(opts, null, properties);
        assertTrue( cmd.hasOption("hide") );
        assertEquals( "seek", cmd.getOptionValue("hide") );
        assertTrue( !cmd.hasOption("fake") );
    }

    public void testPropertyOptionFlags() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty( "a", "true" );
        properties.setProperty( "c", "yes" );
        properties.setProperty( "e", "1" );

        Parser parser = new PosixParser();

        CommandLine cmd = parser.parse(opts, null, properties);
        assertTrue( cmd.hasOption("a") );
        assertTrue( cmd.hasOption("c") );
        assertTrue( cmd.hasOption("e") );


        properties = new Properties();
        properties.setProperty( "a", "false" );
        properties.setProperty( "c", "no" );
        properties.setProperty( "e", "0" );

        cmd = parser.parse(opts, null, properties);
        assertTrue( !cmd.hasOption("a") );
        assertTrue( !cmd.hasOption("c") );
        assertTrue( !cmd.hasOption("e") );


        properties = new Properties();
        properties.setProperty( "a", "TRUE" );
        properties.setProperty( "c", "nO" );
        properties.setProperty( "e", "TrUe" );

        cmd = parser.parse(opts, null, properties);
        assertTrue( cmd.hasOption("a") );
        assertTrue( !cmd.hasOption("c") );
        assertTrue( cmd.hasOption("e") );

        
        properties = new Properties();
        properties.setProperty( "a", "just a string" );
        properties.setProperty( "e", "" );

        cmd = parser.parse(opts, null, properties);
        assertTrue( !cmd.hasOption("a") );
        assertTrue( !cmd.hasOption("c") );
        assertTrue( !cmd.hasOption("e") );
    } 

    public void testPropertyOptionMultipleValues() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty( "k", "one,two" );

        Parser parser = new PosixParser();
        
        String[] values = new String[] {
            "one", "two"
        };

        CommandLine cmd = parser.parse(opts, null, properties);
        assertTrue( cmd.hasOption("k") );
        assertTrue( Arrays.equals( values, cmd.getOptionValues('k') ) );
    }

    public void testPropertyOverrideValues() throws Exception
    {
        String[] args = new String[] { 
            "-j",
            "found",
            "-i",
            "ink"
        };

        Properties properties = new Properties();
        properties.setProperty( "j", "seek" );

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts, args, properties);
        assertTrue( cmd.hasOption("j") );
        assertEquals( "found", cmd.getOptionValue("j") );
        assertTrue( cmd.hasOption("i") );
        assertEquals( "ink", cmd.getOptionValue("i") );
        assertTrue( !cmd.hasOption("fake") );
    }

}
