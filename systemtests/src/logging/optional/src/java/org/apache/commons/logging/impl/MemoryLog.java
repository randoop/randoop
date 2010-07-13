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


package org.apache.commons.logging.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * <p>Memory implementation of Log that keeps all log message as
 * entries in memory. The class is designed to be used in unit tests. 
 * The default log level is TRACE.</p>
 * <p>The code borrows heavily from the SimpleLog class.</p>
 * @author J&ouml;rg Schaible
 * @version $Id: MemoryLog.java,v 1.1 2004/11/04 23:01:39 rdonkin Exp $
 */
public class MemoryLog implements Log {

    // ------------------------------------------------------- Class Attributes
    
    /**
     * A class for a log entry.
     */
    public static class Entry {
        
        private final Date date;
        private final String name;
        private final int level;
        private final Object message;
        private final Throwable throwable;

        /**
         * Construct a log entry.
         * @param name the logger's name
         * @param level the log level
         * @param message the message to log
         * @param t the throwable attending the log
         */
        private Entry(String name, int level, Object message, Throwable t) {
            this.date = new Date();
            this.name = name;
            this.level = level;
            this.message = message;
            this.throwable = t;
        }
        
        /**
         * @return Returns the logging date.
         */
        public Date getDate() {
            return date;
        }
        
        /**
         * @return Returns the logger's name.
         */
        public String getLogName() {
            return name;
        }
        
        /**
         * @return Returns the log message.
         */
        public Object getMessage() {
            return message;
        }
        /**
         * @return Returns the attendent {@link java.lang.Throwable} of the log or null.
         */
        public Throwable getThrowable() {
            return throwable;
        }
        
        /**
         * @return Returns the log level.
         */
        public int getLevel() {
            return level;
        }
    }

    /** The list with all log entries. */
    private static final List logEntries = Collections.synchronizedList(new ArrayList());

    
    // ---------------------------------------------------- Log Level Constants

    /** "Trace" level logging. */
    public static final int LOG_LEVEL_TRACE  = 1;
    /** "Debug" level logging. */
    public static final int LOG_LEVEL_DEBUG  = 2;
    /** "Info" level logging. */
    public static final int LOG_LEVEL_INFO   = 3;
    /** "Warn" level logging. */
    public static final int LOG_LEVEL_WARN   = 4;
    /** "Error" level logging. */
    public static final int LOG_LEVEL_ERROR  = 5;
    /** "Fatal" level logging. */
    public static final int LOG_LEVEL_FATAL  = 6;

    /** Enable all logging levels */
    public static final int LOG_LEVEL_ALL    = (LOG_LEVEL_TRACE - 1);

    /** Enable no logging levels */
    public static final int LOG_LEVEL_OFF    = (LOG_LEVEL_FATAL + 1);

    
    // ------------------------------------------------------------- Attributes

    /** The name of this simple log instance */
    protected String logName = null;
    /** The current log level */
    protected int currentLogLevel;

    
    // ------------------------------------------------------------ Constructor

    /**
     * Construct a simple log with given name.
     *
     * @param name log name
     */
    public MemoryLog(String name) {

        logName = name;

        // Set initial log level
        setLevel(MemoryLog.LOG_LEVEL_TRACE);
    }


    // -------------------------------------------------------- Properties

    /**
     * <p> Set logging level. </p>
     *
     * @param currentLogLevel new logging level
     */
    public void setLevel(int currentLogLevel) {

        this.currentLogLevel = currentLogLevel;

    }


    /**
     * @return Returns the logging level.
     */
    public int getLevel() {

        return currentLogLevel;
    }


    // -------------------------------------------------------- Logging Methods


    /**
     * <p> Do the actual logging.
     * This method assembles the message
     * and then calls <code>write()</code> to cause it to be written.</p>
     *
     * @param type One of the LOG_LEVEL_XXX constants defining the log level
     * @param message The message itself (typically a String)
     * @param t The exception whose stack trace should be logged
     */
    protected void log(int type, Object message, Throwable t) {
        
        if(isLevelEnabled(type)) {
            Entry entry = new Entry(logName, type, message, t);
            logEntries.add(entry);
        }
    }

    
    /**
     * @param logLevel is this level enabled?
     * @return Returns true if the current level is enabled.
     */
    protected boolean isLevelEnabled(int logLevel) {
        // log level are numerically ordered so can use simple numeric
        // comparison
        return (logLevel >= currentLogLevel);
    }
    
    
    /**
     * @return Returns the log entries.
     */
    public static List getLogEntries() {
        return Collections.unmodifiableList(logEntries);
    }


    /**
     * Reset the MemoryLog and clear the log entries. 
     */
    public static void reset() {
        logEntries.clear();
    }


    // -------------------------------------------------------- Log Implementation


    /**
     * <p> Log a message with debug log level.</p>
     */
    public final void debug(Object message) {

        if (isLevelEnabled(MemoryLog.LOG_LEVEL_DEBUG)) {
            log(MemoryLog.LOG_LEVEL_DEBUG, message, null);
        }
    }


    /**
     * <p> Log an error with debug log level.</p>
     */
    public final void debug(Object message, Throwable t) {

        if (isLevelEnabled(MemoryLog.LOG_LEVEL_DEBUG)) {
            log(MemoryLog.LOG_LEVEL_DEBUG, message, t);
        }
    }


    /**
     * <p> Log a message with trace log level.</p>
     */
    public final void trace(Object message) {

        if (isLevelEnabled(MemoryLog.LOG_LEVEL_TRACE)) {
            log(MemoryLog.LOG_LEVEL_TRACE, message, null);
        }
    }


    /**
     * <p> Log an error with trace log level.</p>
     */
    public final void trace(Object message, Throwable t) {

        if (isLevelEnabled(MemoryLog.LOG_LEVEL_TRACE)) {
            log(MemoryLog.LOG_LEVEL_TRACE, message, t);
        }
    }


    /**
     * <p> Log a message with info log level.</p>
     */
    public final void info(Object message) {

        if (isLevelEnabled(MemoryLog.LOG_LEVEL_INFO)) {
            log(MemoryLog.LOG_LEVEL_INFO,message,null);
        }
    }


    /**
     * <p> Log an error with info log level.</p>
     */
    public final void info(Object message, Throwable t) {

        if (isLevelEnabled(MemoryLog.LOG_LEVEL_INFO)) {
            log(MemoryLog.LOG_LEVEL_INFO, message, t);
        }
    }


    /**
     * <p> Log a message with warn log level.</p>
     */
    public final void warn(Object message) {

        if (isLevelEnabled(MemoryLog.LOG_LEVEL_WARN)) {
            log(MemoryLog.LOG_LEVEL_WARN, message, null);
        }
    }


    /**
     * <p> Log an error with warn log level.</p>
     */
    public final void warn(Object message, Throwable t) {

        if (isLevelEnabled(MemoryLog.LOG_LEVEL_WARN)) {
            log(MemoryLog.LOG_LEVEL_WARN, message, t);
        }
    }


    /**
     * <p> Log a message with error log level.</p>
     */
    public final void error(Object message) {

        if (isLevelEnabled(MemoryLog.LOG_LEVEL_ERROR)) {
            log(MemoryLog.LOG_LEVEL_ERROR, message, null);
        }
    }


    /**
     * <p> Log an error with error log level.</p>
     */
    public final void error(Object message, Throwable t) {

        if (isLevelEnabled(MemoryLog.LOG_LEVEL_ERROR)) {
            log(MemoryLog.LOG_LEVEL_ERROR, message, t);
        }
    }


    /**
     * <p> Log a message with fatal log level.</p>
     */
    public final void fatal(Object message) {

        if (isLevelEnabled(MemoryLog.LOG_LEVEL_FATAL)) {
            log(MemoryLog.LOG_LEVEL_FATAL, message, null);
        }
    }


    /**
     * <p> Log an error with fatal log level.</p>
     */
    public final void fatal(Object message, Throwable t) {

        if (isLevelEnabled(MemoryLog.LOG_LEVEL_FATAL)) {
            log(MemoryLog.LOG_LEVEL_FATAL, message, t);
        }
    }


    /**
     * <p> Are debug messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isDebugEnabled() {

        return isLevelEnabled(MemoryLog.LOG_LEVEL_DEBUG);
    }


    /**
     * <p> Are error messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isErrorEnabled() {

        return isLevelEnabled(MemoryLog.LOG_LEVEL_ERROR);
    }


    /**
     * <p> Are fatal messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isFatalEnabled() {

        return isLevelEnabled(MemoryLog.LOG_LEVEL_FATAL);
    }


    /**
     * <p> Are info messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isInfoEnabled() {

        return isLevelEnabled(MemoryLog.LOG_LEVEL_INFO);
    }


    /**
     * <p> Are trace messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isTraceEnabled() {

        return isLevelEnabled(MemoryLog.LOG_LEVEL_TRACE);
    }


    /**
     * <p> Are warn messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isWarnEnabled() {

        return isLevelEnabled(MemoryLog.LOG_LEVEL_WARN);
    }
}
