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

package org.apache.commons.jelly.tags.threads;

/**
 * Represents the status of {@link JellyThread}.
 *
 * @author <a href="mailto:jason@jhorman.org">Jason Horman</a>
 */

public class RunnableStatus {
    public static final int NONE = 0;
    public static final int SUCCESS = 1;
    public static final int FAILURE = 2;
    public static final int AVOIDED = 3;
    public static final int TIMED_OUT = 4;
    public static final int KILLED = 5;

    private int status = NONE;

    /** On a status change to FAILURE an exception can be set */
    private Exception exception = null;

    public RunnableStatus() {

    }

    public RunnableStatus(int status) {
        set(status);
    }

    public synchronized void set(int status) {
        set(status, null);
    }

    public synchronized void set(int status, Exception e) {
        // this check is important since I may call setStatus(BLAH) again
        // to trigger the callback
        if (this.status != status) {
            this.status = status;

            // store the exception if one was set
            if (e != null)
                this.exception = e;
        }
    }

    public synchronized int get() {
        return status;
    }

    public synchronized boolean isSuccess() {
        return (status == SUCCESS);
    }

    public synchronized boolean isFailure() {
        return (status == FAILURE);
    }

    public synchronized boolean isAvoided() {
        return (status == AVOIDED);
    }

    public synchronized boolean isTimedOut() {
        return (status == TIMED_OUT);
    }

    public synchronized boolean isKilled() {
        return (status == KILLED);
    }

    public synchronized Exception getException() {
        return exception;
    }

    public synchronized boolean equals(RunnableStatus status) {
        return status.get() == this.status;
    }

    public synchronized boolean equals(int status) {
        return this.status == status;
    }

    /**
     * Used to get the status code from a string representation. Mainly used for
     * xml parsing.
     * @param status The status string rep.
     * @return The status enum value
     */
    public static int getStatusCode(String status) {
        if (status.equalsIgnoreCase("SUCCESS")) {
            return SUCCESS;
        } else if (status.equalsIgnoreCase("FAILURE")) {
            return FAILURE;
        } else if (status.equalsIgnoreCase("TIMED_OUT")) {
            return TIMED_OUT;
        } else if (status.equalsIgnoreCase("AVOIDED")) {
            return AVOIDED;
        } else if (status.equalsIgnoreCase("KILLED")) {
            return KILLED;
        } else {
            throw new IllegalArgumentException(status + " is invalid status");
        }
    }

    /**
     * The reverse of getStatusCode
     */
    public static String getStatusString(int status) {
        if (status == SUCCESS) {
            return "SUCCESS";
        } else if (status == FAILURE) {
            return "FAILURE";
        } else if (status == TIMED_OUT) {
            return "TIMED_OUT";
        } else if (status == AVOIDED) {
            return "AVOIDED";
        } else if (status == KILLED) {
            return "KILLED";
        } else {
            throw new IllegalArgumentException(status + " is invalid status");
        }
    }

    public static boolean isValidStatus(int status) {
        if (status == SUCCESS) {
            return true;
        } else if (status == FAILURE) {
            return true;
        } else if (status == TIMED_OUT) {
            return true;
        } else if (status == AVOIDED) {
            return true;
        } else if (status == KILLED) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        return getStatusString(status);
    }
}