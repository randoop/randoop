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

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;

/**
 * This calls mutex.wait() on the mutex passed in via the "mutex" attribute.
 *
 * @author <a href="mailto:jason@jhorman.org">Jason Horman</a>
 */

public class WaitTag extends UseMutexTag {
    /** How long should the wait last. If <=0 it lasts until a notify. */
    private long timeout = -1;

    /** Start waiting */
    public void useMutex(Object mutex, XMLOutput output) throws JellyTagException {
        try {
            if (timeout > 0) {
                mutex.wait(timeout);
            } else {
                mutex.wait();
            }
        }
        catch (InterruptedException e) {
            throw new JellyTagException(e);
        }
    }

    /**
     * Set how long the wait should last. If <= 0 the wait will last
     * until a notify occurs.
     * @param timeout in millis
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
