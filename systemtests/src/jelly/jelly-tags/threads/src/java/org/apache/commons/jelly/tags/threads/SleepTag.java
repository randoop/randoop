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
 * Puts the current thread to sleep for some amount of time.
 *
 * @author <a href="mailto:jason@jhorman.org">Jason Horman</a>
 */

public class SleepTag extends TagSupport {
    /** How long to sleep for */
    private long howLong = 0;

    /** Put the thread to sleep */
    public void doTag(XMLOutput output) throws JellyTagException {
        try {
            Thread.sleep(howLong);
        }
        catch (InterruptedException e) {
            throw new JellyTagException(e);
        }
    }

    /**
     * How long to put the thread to sleep for
     * @param howLong in millis
     */
    public void setFor(long howLong) {
        this.howLong = howLong;
    }
}
