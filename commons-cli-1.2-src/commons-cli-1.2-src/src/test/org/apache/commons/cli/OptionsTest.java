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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

/**
 * @author Rob Oxspring roxspring@apache.org
 * @version $Revision: 680311 $
 */
public class OptionsTest extends TestCase
{
    public void testSimple()
    {
        Options opts = new Options();

        opts.addOption("a", false, "toggle -a");
        opts.addOption("b", true, "toggle -b");

        assertTrue(opts.hasOption("a"));
        assertTrue(opts.hasOption("b"));
    }

    public void testDuplicateSimple()
    {
        Options opts = new Options();
        opts.addOption("a", false, "toggle -a");
        opts.addOption("a", true, "toggle -a*");

        assertEquals("last one in wins", "toggle -a*", opts.getOption("a").getDescription());
    }

    public void testLong()
    {
        Options opts = new Options();

        opts.addOption("a", "--a", false, "toggle -a");
        opts.addOption("b", "--b", true, "set -b");

        assertTrue(opts.hasOption("a"));
        assertTrue(opts.hasOption("b"));
    }

    public void testDuplicateLong()
    {
        Options opts = new Options();
        opts.addOption("a", "--a", false, "toggle -a");
        opts.addOption("a", "--a", false, "toggle -a*");
        assertEquals("last one in wins", "toggle -a*", opts.getOption("a").getDescription());
    }

    public void testHelpOptions()
    {
        Option longOnly1 = OptionBuilder.withLongOpt("long-only1").create();
        Option longOnly2 = OptionBuilder.withLongOpt("long-only2").create();
        Option shortOnly1 = OptionBuilder.create("1");
        Option shortOnly2 = OptionBuilder.create("2");
        Option bothA = OptionBuilder.withLongOpt("bothA").create("a");
        Option bothB = OptionBuilder.withLongOpt("bothB").create("b");
        
        Options options = new Options();
        options.addOption(longOnly1);
        options.addOption(longOnly2);
        options.addOption(shortOnly1);
        options.addOption(shortOnly2);
        options.addOption(bothA);
        options.addOption(bothB);
        
        Collection allOptions = new ArrayList();
        allOptions.add(longOnly1);
        allOptions.add(longOnly2);
        allOptions.add(shortOnly1);
        allOptions.add(shortOnly2);
        allOptions.add(bothA);
        allOptions.add(bothB);
        
        Collection helpOptions = options.helpOptions();
        
        assertTrue("Everything in all should be in help", helpOptions.containsAll(allOptions));
        assertTrue("Everything in help should be in all", allOptions.containsAll(helpOptions));        
    }

    public void testMissingOptionException() throws ParseException
    {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired().create("f"));
        try
        {
            new PosixParser().parse(options, new String[0]);
            fail("Expected MissingOptionException to be thrown");
        }
        catch (MissingOptionException e)
        {
            assertEquals("Missing required option: f", e.getMessage());
        }
    }

    public void testMissingOptionsException() throws ParseException
    {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired().create("f"));
        options.addOption(OptionBuilder.isRequired().create("x"));
        try
        {
            new PosixParser().parse(options, new String[0]);
            fail("Expected MissingOptionException to be thrown");
        }
        catch (MissingOptionException e)
        {
            assertEquals("Missing required options: f, x", e.getMessage());
        }
    }

    public void testToString()
    {
        Options options = new Options();
        options.addOption("f", "foo", true, "Foo");
        options.addOption("b", "bar", false, "Bar");

        String s = options.toString();
        assertNotNull("null string returned", s);
        assertTrue("foo option missing", s.toLowerCase().indexOf("foo") != -1);
        assertTrue("bar option missing", s.toLowerCase().indexOf("bar") != -1);
    }

    public void testGetOptionsGroups()
    {
        Options options = new Options();

        OptionGroup group1 = new OptionGroup();
        group1.addOption(OptionBuilder.create('a'));
        group1.addOption(OptionBuilder.create('b'));

        OptionGroup group2 = new OptionGroup();
        group2.addOption(OptionBuilder.create('x'));
        group2.addOption(OptionBuilder.create('y'));

        options.addOptionGroup(group1);
        options.addOptionGroup(group2);

        assertNotNull(options.getOptionGroups());
        assertEquals(2, options.getOptionGroups().size());
    }
}
