/*
 * Copyright 2004 The Apache Software Foundation.
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
 
package org.apache.commons.logging;

import junit.framework.TestCase;

/**
 * Tests the basic logging operations to ensure that they all function 
 * without exception failure. In other words, that they do no fail by
 * throwing exceptions.
 * This is the minimum requirement for any well behaved logger 
 * and so this test should be run for each kind.
 */
public class BasicOperationsTest extends TestCase
{
    public BasicOperationsTest(String testName)
    {
        super(testName);
    }
    
    public void testIsEnabledClassLog()
    {
        Log log = LogFactory.getLog(BasicOperationsTest.class);
        executeIsEnabledTest(log);
    }
    
    public void testIsEnabledNamedLog()
    {
        Log log = LogFactory.getLog(BasicOperationsTest.class.getName());
        executeIsEnabledTest(log);
    }    
    
    public void executeIsEnabledTest(Log log)
    {
        try
        {
            log.isTraceEnabled();
            log.isDebugEnabled();
            log.isInfoEnabled();
            log.isWarnEnabled();
            log.isErrorEnabled();
            log.isFatalEnabled();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            fail("Exception thrown: " + t);
        }
    }
    
    
    public void testMessageWithoutExceptionClassLog()
    {
        Log log = LogFactory.getLog(BasicOperationsTest.class);
        executeMessageWithoutExceptionTest(log);
    }
    
    public void testMessageWithoutExceptionNamedLog()
    {
        Log log = LogFactory.getLog(BasicOperationsTest.class.getName());
        executeMessageWithoutExceptionTest(log);
    }    
    
    public void executeMessageWithoutExceptionTest(Log log)
    {
        try
        {
            log.trace("Hello, Mum");
            log.debug("Hello, Mum");
            log.info("Hello, Mum");
            log.warn("Hello, Mum");
            log.error("Hello, Mum");
            log.fatal("Hello, Mum");
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            fail("Exception thrown: " + t);
        }
    }
    
    public void testMessageWithExceptionClassLog()
    {
        Log log = LogFactory.getLog(BasicOperationsTest.class);
        executeMessageWithExceptionTest(log);
    }
    
    public void testMessageWithExceptionNamedLog()
    {
        Log log = LogFactory.getLog(BasicOperationsTest.class.getName());
        executeMessageWithExceptionTest(log);
    }    
    
    public void executeMessageWithExceptionTest(Log log)
    {
        try
        {
            log.trace("Hello, Mum", new ArithmeticException());
            log.debug("Hello, Mum", new ArithmeticException());
            log.info("Hello, Mum", new ArithmeticException());
            log.warn("Hello, Mum", new ArithmeticException());
            log.error("Hello, Mum", new ArithmeticException());
            log.fatal("Hello, Mum", new ArithmeticException());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            fail("Exception thrown: " + t);
        }
    }
}
