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

import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.logging.Log;

/**
 * <p>Implementation of commons-logging Log interface that delegates all
 * logging calls to the Avalon logging abstraction: the Logger interface.
 * </p>
 * <p>
 * There are two ways in which this class can be used:
 * </p>
 * <ul>
 * <li>the instance can be constructed with an Avalon logger 
 * (by calling {@link #AvalonLogger(Logger)}). In this case, it acts 
 * as a simple thin wrapping implementation over the logger. This is 
 * particularly useful when using a property setter.
 * </li>
 * <li>the {@link #setDefaultLogger} class property can be called which
 * sets the ancesteral Avalon logger for this class. Any <code>AvalonLogger</code> 
 * instances created through the <code>LogFactory</code> mechanisms will output
 * to child loggers of this <code>Logger</code>.
 * </li>
 * </ul>
 *
 * @author <a href="mailto:neeme@apache.org">Neeme Praks</a>
 * @version $Revision: 1.10 $ $Date: 2004/09/27 16:21:40 $
 */
public class AvalonLogger implements Log {

    /** Ancesteral avalon logger  */ 
    private static Logger defaultLogger = null;
    /** Avalon logger used to perform log */
    private transient Logger logger = null;

    /**
     * Constructs an <code>AvalonLogger</code> that outputs to the given
     * <code>Logger</code> instance.
     * @param logger the avalon logger implementation to delegate to
     */
    public AvalonLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Constructs an <code>AvalonLogger</code> that will log to a child
     * of the <code>Logger</code> set by calling {@link #setDefaultLogger}.
     * @param name the name of the avalon logger implementation to delegate to
     */
    public AvalonLogger(String name) {
        if (defaultLogger == null)
            throw new NullPointerException("default logger has to be specified if this constructor is used!");
        this.logger = defaultLogger.getChildLogger(name);
    }

    /**
     * Gets the Avalon logger implementation used to perform logging.
     * @return avalon logger implementation
     */
    private final Logger getLogger() {
        return logger;
    }

    /**
     * Sets the ancesteral Avalon logger from which the delegating loggers 
     * will descend.
     * @param logger the default avalon logger, 
     * in case there is no logger instance supplied in constructor
     */
    public static void setDefaultLogger(Logger logger) {
        defaultLogger = logger;
    }

    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
     */
    public void debug(Object o, Throwable t) {
        if (getLogger().isDebugEnabled()) getLogger().debug(String.valueOf(o), t);
    }

    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object)
     */
    public void debug(Object o) {
        if (getLogger().isDebugEnabled()) getLogger().debug(String.valueOf(o));
    }

    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
     */
    public void error(Object o, Throwable t) {
        if (getLogger().isErrorEnabled()) getLogger().error(String.valueOf(o), t);
    }

    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object)
     */
    public void error(Object o) {
        if (getLogger().isErrorEnabled()) getLogger().error(String.valueOf(o));
    }

    /**
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
     */
    public void fatal(Object o, Throwable t) {
        if (getLogger().isFatalErrorEnabled()) getLogger().fatalError(String.valueOf(o), t);
    }

    /**
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
     */
    public void fatal(Object o) {
        if (getLogger().isFatalErrorEnabled()) getLogger().fatalError(String.valueOf(o));
    }

    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
     */
    public void info(Object o, Throwable t) {
        if (getLogger().isInfoEnabled()) getLogger().info(String.valueOf(o), t);
    }

    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object)
     */
    public void info(Object o) {
        if (getLogger().isInfoEnabled()) getLogger().info(String.valueOf(o));
    }

    /**
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return getLogger().isDebugEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return getLogger().isErrorEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isFatalEnabled()
     */
    public boolean isFatalEnabled() {
        return getLogger().isFatalErrorEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return getLogger().isInfoEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isTraceEnabled()
     */
    public boolean isTraceEnabled() {
        return getLogger().isDebugEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return getLogger().isWarnEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#trace(java.lang.Object, java.lang.Throwable)
     */
    public void trace(Object o, Throwable t) {
        if (getLogger().isDebugEnabled()) getLogger().debug(String.valueOf(o), t);
    }

    /**
     * @see org.apache.commons.logging.Log#trace(java.lang.Object)
     */
    public void trace(Object o) {
        if (getLogger().isDebugEnabled()) getLogger().debug(String.valueOf(o));
    }

    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
     */
    public void warn(Object o, Throwable t) {
        if (getLogger().isWarnEnabled()) getLogger().warn(String.valueOf(o), t);
    }

    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object)
     */
    public void warn(Object o) {
        if (getLogger().isWarnEnabled()) getLogger().warn(String.valueOf(o));
    }

}
