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
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * Base class for tags that will "use" mutexes.
 *
 * @author <a href="mailto:jason@jhorman.org">Jason Horman</a>
 */

public abstract class UseMutexTag extends TagSupport {
    /** The mutex to use in some way. */
    private Object mutex = null;

    /** Calls useMutex after checking to make sure that <i>setMutex</i> was called */
    public void doTag(XMLOutput output) throws JellyTagException {
        // either use the set thread or search for a parent thread to use
        if (mutex == null) {
            throw new JellyTagException("no mutex set");
        }

        useMutex(mutex, output);
    }

    /** Implement this method to do something with the mutex */
    protected abstract void useMutex(Object mutex, XMLOutput output) throws JellyTagException;

    /** Get the mutex */
    public Object getMutex() {
        return mutex;
    }

    /** Set the mutex. Any object can be used as a mutex. */
    public void setMutex(Object mutex) {
        this.mutex = mutex;
    }
}
