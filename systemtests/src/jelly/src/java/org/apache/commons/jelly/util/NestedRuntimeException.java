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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A {@link RuntimeException} which is nested to preserve stack traces.
 *
 * This class allows the following code to be written to convert a regular
 * Exception into a {@link RuntimeException} without losing the stack trace.
 *
 * <pre>
 *    try {
 *        ...
 *    } catch (Exception e) {
 *        throw new RuntimeException(e);
 *    }
 * </pre>
 *
 * @author James Strachan
 * @version $Revision: 1.2 $
 */

public class NestedRuntimeException extends RuntimeException {

    /**
     * Holds the reference to the exception or error that caused
     * this exception to be thrown.
     */
    private Throwable cause = null;

    /**
     * Constructs a new <code>NestedRuntimeException</code> with specified
     * nested <code>Throwable</code>.
     *
     * @param cause the exception or error that caused this exception to be
     * thrown
     */
    public NestedRuntimeException(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    /**
     * Constructs a new <code>NestedRuntimeException</code> with specified
     * detail message and nested <code>Throwable</code>.
     *
     * @param msg    the error message
     * @param cause  the exception or error that caused this exception to be
     * thrown
     */
    public NestedRuntimeException(String msg, Throwable cause) {
        super(msg);
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

    public void printStackTrace() {
        cause.printStackTrace();
    }

    public void printStackTrace(PrintStream out) {
        cause.printStackTrace(out);
    }

    public void printStackTrace(PrintWriter out) {
        cause.printStackTrace(out);
    }

}
