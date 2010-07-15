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
package org.apache.commons.jelly.tags.log;

import org.apache.commons.logging.Log;

/**
 * A Mock Object useful for unit testing of commons-logging. (Maybe this should
 * be contributed back to commons-logging?)
 *
 * @author James Strachan
 * @version 1.1 2003/01/22 10:22:30
 */
public class MockLog implements Log {

    private Object debug;
    private Object trace;
    private Object info;
    private Object warn;
    private Object error;
    private Object fatal;
    private Throwable lastThrowable;

    public MockLog() {
    }

    /**
     * Resets all the last logging messages received
     */
    public void clear() {
        this.debug = null;
        this.trace = null;
        this.info = null;
        this.warn = null;
        this.error = null;
        this.fatal = null;
        this.lastThrowable = null;
    }


    // Log interface
    //-------------------------------------------------------------------------

    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
     */
    public void debug(Object message, Throwable exception) {
        this.debug = message;
        this.lastThrowable = exception;
    }

    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object)
     */
    public void debug(Object message) {
        this.debug = message;
    }

    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
     */
    public void error(Object message, Throwable exception) {
        this.error = message;
        this.lastThrowable = exception;
    }

    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object)
     */
    public void error(Object message) {
        this.error = message;
    }

    /**
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
     */
    public void fatal(Object message, Throwable exception) {
        this.fatal = message;
        this.lastThrowable = exception;
    }

    /**
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
     */
    public void fatal(Object message) {
        this.fatal = message;
    }

    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
     */
    public void info(Object message, Throwable exception) {
        this.info = message;
        this.lastThrowable = exception;
    }

    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object)
     */
    public void info(Object message) {
        this.info = message;
    }

    /**
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#isFatalEnabled()
     */
    public boolean isFatalEnabled() {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#isTraceEnabled()
     */
    public boolean isTraceEnabled() {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#trace(java.lang.Object, java.lang.Throwable)
     */
    public void trace(Object message, Throwable exception) {
        this.trace = message;
        this.lastThrowable = exception;
    }

    /**
     * @see org.apache.commons.logging.Log#trace(java.lang.Object)
     */
    public void trace(Object message) {
        this.trace = message;
    }

    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
     */
    public void warn(Object message, Throwable exception) {
        this.warn = message;
    }

    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object)
     */
    public void warn(Object message) {
        this.warn = message;
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Returns the error.
     * @return Object
     */
    public Object getError() {
        return error;
    }

    /**
     * Returns the fatal.
     * @return Object
     */
    public Object getFatal() {
        return fatal;
    }

    /**
     * Returns the info.
     * @return Object
     */
    public Object getInfo() {
        return info;
    }

    /**
     * Returns the lastThrowable.
     * @return Throwable
     */
    public Throwable getLastThrowable() {
        return lastThrowable;
    }

    /**
     * Returns the warn.
     * @return Object
     */
    public Object getWarn() {
        return warn;
    }

    /**
     * Returns the trace.
     * @return Object
     */
    public Object getTrace() {
        return trace;
    }

    /**
     * Returns the debug.
     * @return Object
     */
    public Object getDebug() {
        return debug;
    }

}
