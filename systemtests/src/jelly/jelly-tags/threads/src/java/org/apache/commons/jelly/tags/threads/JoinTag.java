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

import org.apache.commons.jelly.XMLOutput;

import java.util.List;

/**
 * A thread join waits until a thread or threadGroup is complete.
 *
 * @author <a href="mailto:jason@jhorman.org">Jason Horman</a>
 */

public class JoinTag extends UseThreadTag {
    /** how long to wait */
    private long timeout = -1;

    /** Perform the thread join */
    protected void useThread(Thread thread, XMLOutput output) throws InterruptedException {
        joinThread(thread);
    }

    /** Join all of the threads in a thread group */
    protected void useThreadGroup(List threadGroup, XMLOutput output) throws InterruptedException {
        for (int i = 0; i < threadGroup.size(); i++) {
            joinThread((Thread) threadGroup.get(i));
        }
    }

    /** Join a thread */
    private void joinThread(Thread thread) throws InterruptedException {
        if (timeout > 0) {
            thread.join(timeout);
        } else {
            thread.join();
        }
    }

    /**
     * How long should the join wait. If <= 0 the join waits until the
     * thread is dead.
     * @param timeout in millis
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
