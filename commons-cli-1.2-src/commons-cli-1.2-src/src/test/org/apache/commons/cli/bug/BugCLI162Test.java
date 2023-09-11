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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ParameterMetaData;
import java.sql.Types;

import junit.framework.TestCase;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class BugCLI162Test extends TestCase {
    /** Constant for the line separator.*/
    private static final String CR = System.getProperty("line.separator");

    public void testInfiniteLoop() {
        Options options = new Options();
        options.addOption("h", "help", false, "This is a looooong description");
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(20);
        formatter.printHelp("app", options); // used to hang & crash
    }

    public void testPrintHelpLongLines() throws ParseException, IOException {
        // Constants used for options
        final String OPT = "-";

        final String OPT_COLUMN_NAMES = "l";

        final String OPT_CONNECTION = "c";

        final String OPT_DESCRIPTION = "e";

        final String OPT_DRIVER = "d";

        final String OPT_DRIVER_INFO = "n";

        final String OPT_FILE_BINDING = "b";

        final String OPT_FILE_JDBC = "j";

        final String OPT_FILE_SFMD = "f";

        final String OPT_HELP = "h";

        final String OPT_HELP_ = "help";

        final String OPT_INTERACTIVE = "i";

        final String OPT_JDBC_TO_SFMD = "2";

        final String OPT_JDBC_TO_SFMD_L = "jdbc2sfmd";

        final String OPT_METADATA = "m";

        final String OPT_PARAM_MODES_INT = "o";

        final String OPT_PARAM_MODES_NAME = "O";

        final String OPT_PARAM_NAMES = "a";

        final String OPT_PARAM_TYPES_INT = "y";

        final String OPT_PARAM_TYPES_NAME = "Y";

        final String OPT_PASSWORD = "p";

        final String OPT_PASSWORD_L = "password";

        final String OPT_SQL = "s";

        final String OPT_SQL_L = "sql";

        final String OPT_SQL_SPLIT_DEFAULT = "###";

        final String OPT_SQL_SPLIT_L = "splitSql";

        final String OPT_STACK_TRACE = "t";

        final String OPT_TIMING = "g";

        final String OPT_TRIM_L = "trim";

        final String OPT_USER = "u";

        final String OPT_WRITE_TO_FILE = "w";

        final String _PMODE_IN = "IN";

        final String _PMODE_INOUT = "INOUT";

        final String _PMODE_OUT = "OUT";

        final String _PMODE_UNK = "Unknown";

        final String PMODES = _PMODE_IN + ", " + _PMODE_INOUT + ", " + _PMODE_OUT + ", " + _PMODE_UNK;

        // Options build
        Options commandLineOptions;
        commandLineOptions = new Options();
        commandLineOptions.addOption(OPT_HELP, OPT_HELP_, false, "Prints help and quits");
        commandLineOptions.addOption(OPT_DRIVER, "driver", true, "JDBC driver class name");
        commandLineOptions.addOption(OPT_DRIVER_INFO, "info", false, "Prints driver information and properties. If "
            + OPT
            + OPT_CONNECTION
            + " is not specified, all drivers on the classpath are displayed.");
        commandLineOptions.addOption(OPT_CONNECTION, "url", true, "Connection URL");
        commandLineOptions.addOption(OPT_USER, "user", true, "A database user name");
        commandLineOptions
                .addOption(
                        OPT_PASSWORD,
                        OPT_PASSWORD_L,
                        true,
                        "The database password for the user specified with the "
                            + OPT
                            + OPT_USER
                            + " option. You can obfuscate the password with org.mortbay.jetty.security.Password, see http://docs.codehaus.org/display/JETTY/Securing+Passwords");
        commandLineOptions.addOption(OPT_SQL, OPT_SQL_L, true, "Runs SQL or {call stored_procedure(?, ?)} or {?=call function(?, ?)}");
        commandLineOptions.addOption(OPT_FILE_SFMD, "sfmd", true, "Writes a SFMD file for the given SQL");
        commandLineOptions.addOption(OPT_FILE_BINDING, "jdbc", true, "Writes a JDBC binding node file for the given SQL");
        commandLineOptions.addOption(OPT_FILE_JDBC, "node", true, "Writes a JDBC node file for the given SQL (internal debugging)");
        commandLineOptions.addOption(OPT_WRITE_TO_FILE, "outfile", true, "Writes the SQL output to the given file");
        commandLineOptions.addOption(OPT_DESCRIPTION, "description", true,
                "SFMD description. A default description is used if omited. Example: " + OPT + OPT_DESCRIPTION + " \"Runs such and such\"");
        commandLineOptions.addOption(OPT_INTERACTIVE, "interactive", false,
                "Runs in interactive mode, reading and writing from the console, 'go' or '/' sends a statement");
        commandLineOptions.addOption(OPT_TIMING, "printTiming", false, "Prints timing information");
        commandLineOptions.addOption(OPT_METADATA, "printMetaData", false, "Prints metadata information");
        commandLineOptions.addOption(OPT_STACK_TRACE, "printStack", false, "Prints stack traces on errors");
        Option option = new Option(OPT_COLUMN_NAMES, "columnNames", true, "Column XML names; default names column labels. Example: "
            + OPT
            + OPT_COLUMN_NAMES
            + " \"cname1 cname2\"");
        commandLineOptions.addOption(option);
        option = new Option(OPT_PARAM_NAMES, "paramNames", true, "Parameter XML names; default names are param1, param2, etc. Example: "
            + OPT
            + OPT_PARAM_NAMES
            + " \"pname1 pname2\"");
        commandLineOptions.addOption(option);
        //
        OptionGroup pOutTypesOptionGroup = new OptionGroup();
        String pOutTypesOptionGroupDoc = OPT + OPT_PARAM_TYPES_INT + " and " + OPT + OPT_PARAM_TYPES_NAME + " are mutually exclusive.";
        final String typesClassName = Types.class.getName();
        option = new Option(OPT_PARAM_TYPES_INT, "paramTypes", true, "Parameter types from "
            + typesClassName
            + ". "
            + pOutTypesOptionGroupDoc
            + " Example: "
            + OPT
            + OPT_PARAM_TYPES_INT
            + " \"-10 12\"");
        commandLineOptions.addOption(option);
        option = new Option(OPT_PARAM_TYPES_NAME, "paramTypeNames", true, "Parameter "
            + typesClassName
            + " names. "
            + pOutTypesOptionGroupDoc
            + " Example: "
            + OPT
            + OPT_PARAM_TYPES_NAME
            + " \"CURSOR VARCHAR\"");
        commandLineOptions.addOption(option);
        commandLineOptions.addOptionGroup(pOutTypesOptionGroup);
        //
        OptionGroup modesOptionGroup = new OptionGroup();
        String modesOptionGroupDoc = OPT + OPT_PARAM_MODES_INT + " and " + OPT + OPT_PARAM_MODES_NAME + " are mutually exclusive.";
        option = new Option(OPT_PARAM_MODES_INT, "paramModes", true, "Parameters modes ("
            + ParameterMetaData.parameterModeIn
            + "=IN, "
            + ParameterMetaData.parameterModeInOut
            + "=INOUT, "
            + ParameterMetaData.parameterModeOut
            + "=OUT, "
            + ParameterMetaData.parameterModeUnknown
            + "=Unknown"
            + "). "
            + modesOptionGroupDoc
            + " Example for 2 parameters, OUT and IN: "
            + OPT
            + OPT_PARAM_MODES_INT
            + " \""
            + ParameterMetaData.parameterModeOut
            + " "
            + ParameterMetaData.parameterModeIn
            + "\"");
        modesOptionGroup.addOption(option);
        option = new Option(OPT_PARAM_MODES_NAME, "paramModeNames", true, "Parameters mode names ("
            + PMODES
            + "). "
            + modesOptionGroupDoc
            + " Example for 2 parameters, OUT and IN: "
            + OPT
            + OPT_PARAM_MODES_NAME
            + " \""
            + _PMODE_OUT
            + " "
            + _PMODE_IN
            + "\"");
        modesOptionGroup.addOption(option);
        commandLineOptions.addOptionGroup(modesOptionGroup);
        option = new Option(null, OPT_TRIM_L, true,
                "Trims leading and trailing spaces from all column values. Column XML names can be optionally specified to set which columns to trim.");
        option.setOptionalArg(true);
        commandLineOptions.addOption(option);
        option = new Option(OPT_JDBC_TO_SFMD, OPT_JDBC_TO_SFMD_L, true,
                "Converts the JDBC file in the first argument to an SMFD file specified in the second argument.");
        option.setArgs(2);
        commandLineOptions.addOption(option);
        new HelpFormatter().printHelp(this.getClass().getName(), commandLineOptions);
    }

    public void testLongLineChunking() throws ParseException, IOException {
        Options options = new Options();
        options.addOption("x", "extralongarg", false,
                                     "This description has ReallyLongValuesThatAreLongerThanTheWidthOfTheColumns " +
                                     "and also other ReallyLongValuesThatAreHugerAndBiggerThanTheWidthOfTheColumnsBob, " +
                                     "yes. ");
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        formatter.printHelp(new PrintWriter(sw), 35, this.getClass().getName(), "Header", options, 0, 5, "Footer");
        String expected = "usage:" + CR +
                          "       org.apache.commons.cli.bug.B" + CR +
                          "       ugCLI162Test" + CR +
                          "Header" + CR +
                          "-x,--extralongarg     This" + CR +
                          "                      description" + CR +
                          "                      has" + CR +
                          "                      ReallyLongVal" + CR +
                          "                      uesThatAreLon" + CR +
                          "                      gerThanTheWid" + CR +
                          "                      thOfTheColumn" + CR +
                          "                      s and also" + CR +
                          "                      other" + CR +
                          "                      ReallyLongVal" + CR +
                          "                      uesThatAreHug" + CR +
                          "                      erAndBiggerTh" + CR +
                          "                      anTheWidthOfT" + CR +
                          "                      heColumnsBob," + CR +
                          "                      yes." + CR +
                          "Footer" + CR;
        assertEquals( "Long arguments did not split as expected", expected, sw.toString() );
    }

    public void testLongLineChunkingIndentIgnored() throws ParseException, IOException {
        Options options = new Options();
        options.addOption("x", "extralongarg", false, "This description is Long." );
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        formatter.printHelp(new PrintWriter(sw), 22, this.getClass().getName(), "Header", options, 0, 5, "Footer");
        System.err.println(sw.toString());
        String expected = "usage:" + CR +
                          "       org.apache.comm" + CR +
                          "       ons.cli.bug.Bug" + CR +
                          "       CLI162Test" + CR +
                          "Header" + CR +
                          "-x,--extralongarg" + CR +
                          " This description is" + CR +
                          " Long." + CR +
                          "Footer" + CR;
        assertEquals( "Long arguments did not split as expected", expected, sw.toString() );
    }

}
