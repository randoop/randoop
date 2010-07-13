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
 * Synchronize a block inside of a thread using the passed in mutex. The
 * mutex object passed in does not have to have been created using the
 * mutex tag, it can be any object at all.
 *
 * @author <a href="mailto:jason@jhorman.org">Jason Horman</a>
 */

public class SynchronizeTag extends UseMutexTag {
    /** Synchronize on the mutex */
    protected void useMutex(Object mutex, XMLOutput output) throws JellyTagException {
        synchronized (mutex) {
            invokeBody(output);
        }
    }
}

