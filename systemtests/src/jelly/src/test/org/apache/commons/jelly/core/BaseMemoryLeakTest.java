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
package org.apache.commons.jelly.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.parser.XMLParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Automates the basic process of testing a tag library for a memory leak.
 * <p>
 * To use it, extend it. Use the {@link runScriptManyTimes(String, int)}
 * method in your unit tests.
 * 
 * @author Hans Gilde
 *  
 */
public class BaseMemoryLeakTest extends TestCase {
    private final static Log log = LogFactory.getLog(BaseMemoryLeakTest.class);

    /**
     * The JUnit constructor
     * 
     * @param name
     */
    public BaseMemoryLeakTest(String name) {
        super(name);
    }

    /** Runs a script count times and reports the number of bytes "leaked".
     * Note that "leaked" means "not collected by the GC"
     * and can easily be different between JVM's. This is because all 
     * freed references may not be available for GC in the short time
     * between their freeing and the completion of this test.
     * <p/>
     * However, running a 
     * script 10,000 or 100,000 times should be a pretty good test
     * for a memory leak. If there's not too much memory "leaked",
     * you're probably OK.
     * @param scriptName The path to the script, from the classloader of the current class.
     * @param count The number of times to run the script.
     * @return The number of bytes "leaked"
     * @throws IOException
     * @throws SAXException
     * @throws JellyException
     */
    public long runScriptManyTimes(String scriptName, int count)
            throws IOException, SAXException, JellyException {
        Runtime rt = Runtime.getRuntime();
        JellyContext jc = new JellyContext();
        jc.setClassLoader(getClass().getClassLoader());

        XMLOutput output = XMLOutput.createDummyXMLOutput();
        
        URL url = this.getClass().getResource(scriptName);

        String exturl = url.toExternalForm();
        int lastSlash = exturl.lastIndexOf("/");
        String extBase = exturl.substring(0,lastSlash+1);
        URL baseurl = new URL(extBase);
        jc.setCurrentURL(baseurl);
        
        InputStream is = url.openStream();
        byte[] bytes = new byte[is.available()];
        is.read(bytes);

        InputStream scriptIStream = new ByteArrayInputStream(bytes);
        InputSource scriptISource = new InputSource(scriptIStream);

        is.close();
        is = null;
        bytes = null;

        rt.runFinalization();
        rt.gc();

        long start = rt.totalMemory() - rt.freeMemory();
        log.info("Starting memory test with used memory of " + start);

        XMLParser parser;
        Script script;

        int outputEveryXIterations = outputEveryXIterations();

        for (int i = 0; i < count; i++) {
            scriptIStream.reset();
            parser = new XMLParser();

            script = parser.parse(scriptISource);
            script.run(jc, output);
            // PL: I don't see why but removing the clear here 
            //     does make the test fail!
            //     As if the WeakHashMap wasn't weak enough...
            
            //Hans: The structure of the relationship
            //  between TagScript and Tag prevents WeakHashMap
            //  from working in this case, which is why I removed it.
            jc.clear();

            if (outputEveryXIterations != 0 && i % outputEveryXIterations == 0) {
                parser = null;
                script = null;
                
                rt.runFinalization();
                rt.gc();
                long middle = rt.totalMemory() - rt.freeMemory();
                log.info("TagHolderMap has " + jc.getThreadScriptDataMap().size() + " entries.");
                log.info("Memory test after " + i + " runs: "
                        + (middle - start));
            }
        }
        
        rt.gc();

        jc = null;
        output = null;
        parser = null;
        script = null;

        scriptIStream = null;
        scriptISource = null;

        rt.runFinalization();
        rt.gc();
        
        long nullsDone = rt.totalMemory() - rt.freeMemory();
        log.info("Memory test completed, memory \"leaked\": " + (nullsDone - start));
        
        return nullsDone - start;
    }

    protected int outputEveryXIterations() {
        return 1000;
    }

}
