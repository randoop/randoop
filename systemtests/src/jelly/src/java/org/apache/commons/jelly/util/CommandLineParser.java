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

package org.apache.commons.jelly.util;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.jelly.Jelly;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;

/**
 * Utility class to parse command line options using CLI.
 * Using a separate class allows us to run Jelly without
 * CLI in the classpath when the command line interface
 * is not in use.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author Morgan Delagrange
 * @version $Revision: 1.11 $
 */
public class CommandLineParser {

    protected static CommandLineParser _instance = new CommandLineParser();
    
    private Options cmdLineOptions = null;

    public static CommandLineParser getInstance() {
        return _instance;
    }

    /**
     * Parse out the command line options and configure
     * the give Jelly instance.
     *
     * @param args   options from the command line
     * @exception JellyException
     *                   if the command line could not be parsed
     */
    public void invokeCommandLineJelly(String[] args) throws JellyException {
        CommandLine cmdLine = null;
        try {
            cmdLine = parseCommandLineOptions(args);
        } catch (ParseException e) {
            throw new JellyException(e);
        }
        
        // check for -h or -v
        if (cmdLine.hasOption("h")) {
            new HelpFormatter().printHelp("jelly [scriptFile] [-script scriptFile] [-o outputFile] [-Dsysprop=syspropval] [-awt]",
                cmdLineOptions);
            System.exit(1);
        }
        if (cmdLine.hasOption("v")) {
            System.err.println("Jelly " + Jelly.getJellyVersion());
            System.err.println(" compiled: " + Jelly.getJellyBuildDate());
            System.err.println("");
            System.exit(1);
        }

        // get the -script option. If there isn't one then use args[0]
        String scriptFile = null;
        if (cmdLine.hasOption("script")) {
            scriptFile = cmdLine.getOptionValue("script");
        } else {
            scriptFile = args[0];
        }
        
        // check the -awt option.
        boolean runInSwingThread = cmdLine.hasOption("awt") || cmdLine.hasOption("swing");

        //
        // Use classloader to find file
        //
        URL url = ClassLoaderUtils.getClassLoader(getClass()).getResource(scriptFile);
        // check if the script file exists
        if (url == null && !(new File(scriptFile)).exists()) {
            throw new JellyException("Script file " + scriptFile + " not found");
        }

        try {
            // extract the -o option for the output file to use
            final XMLOutput output = cmdLine.hasOption("o") ?
                    XMLOutput.createXMLOutput(new FileWriter(cmdLine.getOptionValue("o"))) :
                    XMLOutput.createXMLOutput(System.out);

            Jelly jelly = new Jelly();
            jelly.setScript(scriptFile);

            final Script script = jelly.compileScript();

            // add the system properties and the command line arguments
            final JellyContext context = jelly.getJellyContext();
            context.setVariable("args", args);
            context.setVariable("commandLine", cmdLine);
            if (runInSwingThread) {
                javax.swing.SwingUtilities.invokeAndWait(new Runnable() { public void run() {
                    try {
                        script.run(context, output);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
            } } ); } else {
                script.run(context, output);
            }

            // now lets wait for all threads to close
            Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            output.close();
                        }
                        catch (Exception e) {
                            // ignore errors
                        }
                    }
                }
            );

        } catch (Exception e) {
            throw new JellyException(e);
        }

    }

    /**
     * Parse the command line using CLI. -o and -script are reserved for Jelly.
     * -Dsysprop=sysval is support on the command line as well.
     */
    public CommandLine parseCommandLineOptions(String[] args) throws ParseException {
        // create the expected options
        cmdLineOptions = new Options();
        cmdLineOptions.addOption("o", true, "Output file");
        cmdLineOptions.addOption("script", true, "Jelly script to run");
        cmdLineOptions.addOption("h","help", false, "Give this help message");
        cmdLineOptions.addOption("v","version", false, "prints Jelly's version and exits");
        cmdLineOptions.addOption("script", true, "Jelly script to run");
        cmdLineOptions.addOption("awt", false, "Wether to run in the AWT thread.");
        cmdLineOptions.addOption("swing", false, "Synonym of \"-awt\".");
        List builtinOptionNames = Arrays.asList(new String[]{
            "-o","-script","-h","--help","-v","--version","-awt","-swing"});
        
        // -D options will be added to the system properties
        Properties sysProps = System.getProperties();

        // filter the system property setting from the arg list
        // before passing it to the CLI parser
        ArrayList filteredArgList = new ArrayList();

        for (int i=0;i<args.length;i++) {
            String arg = args[i];

            // if this is a -D property parse it and add it to the system properties.
            // -D args will not be copied into the filteredArgList.
            if (arg.startsWith("-D") && (arg.length() > 2)) {
                arg = arg.substring(2);
                int ePos = arg.indexOf("=");
                if(ePos==-1 || ePos==0 || ePos==arg.length()-1)
                    System.err.println("Invalid system property: \"" + arg + "\".");
                sysProps.setProperty(arg.substring(0,ePos), arg.substring(ePos+1));
            } else {
                // add this to the filtered list of arguments
                filteredArgList.add(arg);

                // add additional "-?" options to the options object. if this is not done
                // the only options allowed would be the builtin-ones.
                if (arg.startsWith("-") && arg.length() > 1) {
                    if (!(builtinOptionNames.contains(arg))) {
                        cmdLineOptions.addOption(arg.substring(1, arg.length()), true, "dynamic option");
                    }
                }
            }
        }

        // make the filteredArgList into an array
        String[] filterArgs = new String[filteredArgList.size()];
        filteredArgList.toArray(filterArgs);

        // parse the command line
        Parser parser = new org.apache.commons.cli.GnuParser();
        return parser.parse(cmdLineOptions, filterArgs);
    }

}
