/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.commons.logging.simple;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Tests custom date time format configuration
 */
public class DateTimeCustomConfigTestCase extends CustomConfigTestCase {
    
    // ----------------------------------------------------------- Constructors

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(DateTimeCustomConfigTestCase.class));
    }


    /**
     * <p>Construct a new instance of this test case.</p>
     *
     * @param name Name of the test case
     */
    public DateTimeCustomConfigTestCase(String name) {
        super(name);
    }
    
    // ----------------------------------------------------------- Methods

    /** Checks that the date time format has been successfully set */
    protected void checkDecoratedDateTime() {
        assertEquals("Expected date format to be set", "dd.mm.yyyy",
                     ((DecoratedSimpleLog) log).getDateTimeFormat());
        
        // try the formatter
        Date now = new Date();
        DateFormat formatter = ((DecoratedSimpleLog) log).getDateTimeFormatter(); 
        SimpleDateFormat sampleFormatter = new SimpleDateFormat("dd.mm.yyyy");
        assertEquals("Date should be formatters to pattern dd.mm.yyyy", sampleFormatter.format(now), formatter.format(now));
    }
    
        /** Hook for subclassses */
    protected void checkShowDateTime() {
        assertTrue(((DecoratedSimpleLog) log).getShowDateTime());
    }
    
}