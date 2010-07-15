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


package org.apache.commons.logging.impl;


import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;


/**
 * <p>Implementation of the <code>org.apache.commons.logging.Log</code>
 * interface that wraps the standard JDK logging mechanisms that were
 * introduced in the Merlin release (JDK 1.4).</p>
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:donaldp@apache.org">Peter Donald</a>
 * @version $Revision: 1.13 $ $Date: 2004/06/06 21:10:21 $
 */

public class Jdk14Logger implements Log, Serializable {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a named instance of this Logger.
     *
     * @param name Name of the logger to be constructed
     */
    public Jdk14Logger(String name) {

        this.name = name;
        logger = getLogger();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The underlying Logger implementation we are using.
     */
    protected transient Logger logger = null;


    /**
     * The name of the logger we are wrapping.
     */
    protected String name = null;


    // --------------------------------------------------------- Public Methods

    private void log( Level level, String msg, Throwable ex ) {

        Logger logger = getLogger();
        if (logger.isLoggable(level)) {
            // Hack (?) to get the stack trace.
            Throwable dummyException=new Throwable();
            StackTraceElement locations[]=dummyException.getStackTrace();
            // Caller will be the third element
            String cname="unknown";
            String method="unknown";
            if( locations!=null && locations.length >2 ) {
                StackTraceElement caller=locations[2];
                cname=caller.getClassName();
                method=caller.getMethodName();
            }
            if( ex==null ) {
                logger.logp( level, cname, method, msg );
            } else {
                logger.logp( level, cname, method, msg, ex );
            }
        }

    }

    /**
     * Log a message with debug log level.
     */
    public void debug(Object message) {
        log(Level.FINE, String.valueOf(message), null);
    }


    /**
     * Log a message and exception with debug log level.
     */
    public void debug(Object message, Throwable exception) {
        log(Level.FINE, String.valueOf(message), exception);
    }


    /**
     * Log a message with error log level.
     */
    public void error(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }


    /**
     * Log a message and exception with error log level.
     */
    public void error(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }


    /**
     * Log a message with fatal log level.
     */
    public void fatal(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }


    /**
     * Log a message and exception with fatal log level.
     */
    public void fatal(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }


    /**
     * Return the native Logger instance we are using.
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(name);
        }
        return (logger);
    }


    /**
     * Log a message with info log level.
     */
    public void info(Object message) {
        log(Level.INFO, String.valueOf(message), null);
    }


    /**
     * Log a message and exception with info log level.
     */
    public void info(Object message, Throwable exception) {
        log(Level.INFO, String.valueOf(message), exception);
    }


    /**
     * Is debug logging currently enabled?
     */
    public boolean isDebugEnabled() {
        return (getLogger().isLoggable(Level.FINE));
    }


    /**
     * Is error logging currently enabled?
     */
    public boolean isErrorEnabled() {
        return (getLogger().isLoggable(Level.SEVERE));
    }


    /**
     * Is fatal logging currently enabled?
     */
    public boolean isFatalEnabled() {
        return (getLogger().isLoggable(Level.SEVERE));
    }


    /**
     * Is info logging currently enabled?
     */
    public boolean isInfoEnabled() {
        return (getLogger().isLoggable(Level.INFO));
    }


    /**
     * Is trace logging currently enabled?
     */
    public boolean isTraceEnabled() {
        return (getLogger().isLoggable(Level.FINEST));
    }


    /**
     * Is warn logging currently enabled?
     */
    public boolean isWarnEnabled() {
        return (getLogger().isLoggable(Level.WARNING));
    }


    /**
     * Log a message with trace log level.
     */
    public void trace(Object message) {
        log(Level.FINEST, String.valueOf(message), null);
    }


    /**
     * Log a message and exception with trace log level.
     */
    public void trace(Object message, Throwable exception) {
        log(Level.FINEST, String.valueOf(message), exception);
    }


    /**
     * Log a message with warn log level.
     */
    public void warn(Object message) {
        log(Level.WARNING, String.valueOf(message), null);
    }


    /**
     * Log a message and exception with warn log level.
     */
    public void warn(Object message, Throwable exception) {
        log(Level.WARNING, String.valueOf(message), exception);
    }


}
