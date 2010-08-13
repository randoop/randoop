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

/**
 * This calls mutex.notify() or mutex.notifyAll() on the mutex passed
 * in via the "mutex" attribute.
 *
 * @author <a href="mailto:jason@jhorman.org">Jason Horman</a>
 */

public class NotifyTag extends UseMutexTag {
    /** True means mutex.notifyAll() will be called */
    private boolean notifyAll = false;

    /** Perform the notify */
    public void useMutex(Object mutex, XMLOutput output) {
        if (notifyAll) {
            mutex.notifyAll();
        } else {
            mutex.notify();
        }
    }

    /**
     * If set to true the notify will notify all waiting threads
     */
    public void setNotifyAll(boolean notifyAll) {
        this.notifyAll = notifyAll;
    }
}
