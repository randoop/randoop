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
import org.apache.log.Logger;
import org.apache.log.Hierarchy;
import org.apache.commons.logging.Log;

/**
 * <p>Implementation of <code>org.apache.commons.logging.Log</code>
 * that wraps the <a href="http://avalon.apache.org/logkit/">avalon-logkit</a>
 * logging system. Configuration of <code>LogKit</code> is left to the user.
 * </p>
 *
 * <p><code>LogKit</code> accepts only <code>String</code> messages.
 * Therefore, this implementation converts object messages into strings
 * by called their <code>toString()</code> method before logging them.</p>
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Robert Burrell Donkin
 * @version $Id: LogKitLogger.java,v 1.9 2004/06/01 19:56:46 rdonkin Exp $
 */

public class LogKitLogger implements Log, Serializable {


    // ------------------------------------------------------------- Attributes


    /** Logging goes to this <code>LogKit</code> logger */
    protected transient Logger logger = null;

    /** Name of this logger */
    protected String name = null;


    // ------------------------------------------------------------ Constructor


    /**
     * Construct <code>LogKitLogger</code> which wraps the <code>LogKit</code>
     * logger with given name.
     *
     * @param name log name
     */
    public LogKitLogger(String name) {
        this.name = name;
        this.logger = getLogger();
    }


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Return the underlying Logger we are using.</p>
     */
    public Logger getLogger() {

        if (logger == null) {
            logger = Hierarchy.getDefaultHierarchy().getLoggerFor(name);
        }
        return (logger);

    }


    // ----------------------------------------------------- Log Implementation


    /**
     * Log message to <code>LogKit</code> logger with <code>DEBUG</code> priority.
     */
    public void trace(Object message) {
        debug(message);
    }


    /**
     * Log error to <code>LogKit</code> logger with <code>DEBUG</code> priority.
     */
    public void trace(Object message, Throwable t) {
        debug(message, t);
    }


    /**
     * Log message to <code>LogKit</code> logger with <code>DEBUG</code> priority.
     */
    public void debug(Object message) {
        if (message != null) {
            getLogger().debug(String.valueOf(message));
        }
    }


    /**
     * Log error to <code>LogKit</code> logger with <code>DEBUG</code> priority.
     */
    public void debug(Object message, Throwable t) {
        if (message != null) {
            getLogger().debug(String.valueOf(message), t);
        }
    }


    /**
     * Log message to <code>LogKit</code> logger with <code>INFO</code> priority.
     */
    public void info(Object message) {
        if (message != null) {
            getLogger().info(String.valueOf(message));
        }
    }


    /**
     * Log error to <code>LogKit</code> logger with <code>INFO</code> priority.
     */
    public void info(Object message, Throwable t) {
        if (message != null) {
            getLogger().info(String.valueOf(message), t);
        }
    }


    /**
     * Log message to <code>LogKit</code> logger with <code>WARN</code> priority.
     */
    public void warn(Object message) {
        if (message != null) {
            getLogger().warn(String.valueOf(message));
        }
    }


    /**
     * Log error to <code>LogKit</code> logger with <code>WARN</code> priority.
     */
    public void warn(Object message, Throwable t) {
        if (message != null) {
            getLogger().warn(String.valueOf(message), t);
        }
    }


    /**
     * Log message to <code>LogKit</code> logger with <code>ERROR</code> priority.
     */
    public void error(Object message) {
        if (message != null) {
            getLogger().error(String.valueOf(message));
        }
    }


    /**
     * Log error to <code>LogKit</code> logger with <code>ERROR</code> priority.
     */
    public void error(Object message, Throwable t) {
        if (message != null) {
            getLogger().error(String.valueOf(message), t);
        }
    }


    /**
     * Log message to <code>LogKit</code> logger with <code>FATAL_ERROR</code> priority.
     */
    public void fatal(Object message) {
        if (message != null) {
            getLogger().fatalError(String.valueOf(message));
        }
    }


    /**
     * Log error to <code>LogKit</code> logger with <code>FATAL_ERROR</code> priority.
     */
    public void fatal(Object message, Throwable t) {
        if (message != null) {
            getLogger().fatalError(String.valueOf(message), t);
        }
    }


    /**
     * Check whether the <code>LogKit</code> logger will log messages of priority <code>DEBUG</code>.
     */
    public boolean isDebugEnabled() {
        return getLogger().isDebugEnabled();
    }


    /**
     * Check whether the <code>LogKit</code> logger will log messages of priority <code>ERROR</code>.
     */
    public boolean isErrorEnabled() {
        return getLogger().isErrorEnabled();
    }


    /**
     * Check whether the <code>LogKit</code> logger will log messages of priority <code>FATAL_ERROR</code>.
     */
    public boolean isFatalEnabled() {
        return getLogger().isFatalErrorEnabled();
    }


    /**
     * Check whether the <code>LogKit</code> logger will log messages of priority <code>INFO</code>.
     */
    public boolean isInfoEnabled() {
        return getLogger().isInfoEnabled();
    }


    /**
     * Check whether the <code>LogKit</code> logger will log messages of priority <code>DEBUG</code>.
     */
    public boolean isTraceEnabled() {
        return getLogger().isDebugEnabled();
    }


    /**
     * Check whether the <code>LogKit</code> logger will log messages of priority <code>WARN</code>.
     */
    public boolean isWarnEnabled() {
        return getLogger().isWarnEnabled();
    }


}
