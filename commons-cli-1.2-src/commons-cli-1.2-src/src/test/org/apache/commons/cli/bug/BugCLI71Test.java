/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.commons.cli.bug;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import junit.framework.TestCase;

public class BugCLI71Test extends TestCase {

    private Options options;
    private CommandLineParser parser;

    public void setUp() {
        options = new Options();

        Option algorithm = new Option("a" , "algo", true, "the algorithm which it to perform executing");
        algorithm.setArgName("algorithm name");
        options.addOption(algorithm);

        Option key = new Option("k" , "key", true, "the key the setted algorithm uses to process");
        algorithm.setArgName("value");
        options.addOption(key);

        parser = new PosixParser();
    }

    public void testBasic() throws Exception {
        String[] args = new String[] { "-a", "Caesar", "-k", "A" };
        CommandLine line = parser.parse( options, args);
        assertEquals( "Caesar", line.getOptionValue("a") );
        assertEquals( "A", line.getOptionValue("k") );
    }

    public void testMistakenArgument() throws Exception {
        String[] args = new String[] { "-a", "Caesar", "-k", "A" };
        CommandLine line = parser.parse( options, args);
        args = new String[] { "-a", "Caesar", "-k", "a" };
        line = parser.parse( options, args);
        assertEquals( "Caesar", line.getOptionValue("a") );
        assertEquals( "a", line.getOptionValue("k") );
    }

    public void testLackOfError() throws Exception {
        String[] args = new String[] { "-k", "-a",  "Caesar" };
        try {
            CommandLine line = parser.parse( options, args);
            fail("MissingArgumentException expected");
        } catch(MissingArgumentException e) {
            assertEquals("option missing an argument", "k", e.getOption().getOpt());
        }
    }

    public void testGetsDefaultIfOptional() throws Exception {
        String[] args = new String[] { "-k", "-a", "Caesar" };
        options.getOption("k").setOptionalArg(true);
        CommandLine line = parser.parse( options, args);
        
        assertEquals( "Caesar", line.getOptionValue("a") );
        assertEquals( "a", line.getOptionValue("k", "a") );
    }

}
