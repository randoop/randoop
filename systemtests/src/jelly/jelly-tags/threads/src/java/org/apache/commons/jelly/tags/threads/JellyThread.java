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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Adds some functionality to the jdk thread class.
 *
 * @author <a href="mailto:jason@jhorman.org">Jason Horman</a>
 */

public class JellyThread extends Thread {
    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(ThreadTag.class);

    /** While this thread is still running it owns this mutex */
    private Mutex runningMutex = new Mutex();
    /** The Runnable target */
    private Runnable target = null;

    /** Tracks the status of this thread */
    RunnableStatus status = new RunnableStatus();

    public JellyThread() {
        // aquire my still running lock immediately
        while (true) {
            try {
                runningMutex.acquire();
                break;
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Set the Runnable target that will be run
     */
    public void setTarget(Runnable target) {
        this.target = target;
    }

    /**
     * Run the thread
     */
    public void run() {
        log.debug("Starting thread \"" + getName() + "\"");

        // run the runnable item
        try {

            log.debug("Thread \"" + getName() + "\" running");
            target.run();

            // as long as there were no runtime exceptions set SUCCESS
            status.set(RunnableStatus.SUCCESS);

        } catch(RequirementException e) {

            status.set(RunnableStatus.AVOIDED);
            log.warn("Thread \"" + getName() + "\" avoided, " + e.getMessage());

        } catch(TimeoutException e) {

            status.set(RunnableStatus.AVOIDED);
            log.warn("Thread \"" + getName() + "\" avoided, " + e.getMessage());

        } catch (Exception e) {

            // runtime exceptions will cause a status of FAILURE
            status.set(RunnableStatus.FAILURE, e);
            log.error("Thread \"" + getName() + "\" failure, " + e.getMessage());
            log.debug(e);

        }

        // release the i'm still running mutex
        runningMutex.release();

        log.debug("Thread \"" + getName() + "\" finished");
    }

    /**
     * Call this method from a different thread to wait until this thread is done. This
     * is used by the {@link WaitForTag} class.
     */
    public void waitUntilDone(long howLong) throws TimeoutException {
        if (Thread.currentThread() == this) {
            throw new RuntimeException("This method should be called from a different thread than itself");
        }

        // wait until the calling thread can aquire the lock
        while (true) {
            try {
                if (howLong == -1) {
                    runningMutex.acquire();
                    break;
                } else if (!runningMutex.attempt(howLong)) {
                    throw new TimeoutException("max wait time exceeded");
                }
            } catch (InterruptedException e) {
            }
        }

        // release the lock, just needed it to get started
        runningMutex.release();
    }

    /** Get the status of this thread */
    public RunnableStatus getStatus() {
        return status;
    }
}
