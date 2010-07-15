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

import java.util.List;

/**
 * Base class for tags that will "use" threads.
 *
 * @author <a href="mailto:jason@jhorman.org">Jason Horman</a>
 */

public abstract class UseThreadTag extends TagSupport {
    /** The thread to use in some way. */
    private Thread thread = null;
    /** Threads can be grouped and acted on as a set */
    private List threadGroup = null;
    /** If true doTag will search for a parent thread to use if setThread was not called */
    private boolean searchForParent = true;

    /**
     * The default behavior is to either use the set thread or to
     * search for a parent thread to use.
     */
    public void doTag(XMLOutput output) throws JellyTagException {
        try {
            // either use the set thread or search for a parent thread to use
            if (thread != null) {
                useThread(thread, output);
            } else if (threadGroup != null) {
                useThreadGroup(threadGroup, output);
            } else {
                // check if this tag is nested inside a thread. if so
                // use the parent thread.
                if (searchForParent) {
                    // first look for parent threads
                    ThreadTag tt = (ThreadTag) findAncestorWithClass(ThreadTag.class);

                    if (tt != null) {
                        useThread(tt.getThread(), output);
                    } else {
                        // then look for parent thread groups
                        GroupTag gt = (GroupTag) findAncestorWithClass(GroupTag.class);
                        if (gt != null) {
                            useThreadGroup(gt.getThreads(), output);
                        } else {
                            throw new JellyTagException("no thread or thread group found");
                        }
                    }
                } else {
                    throw new JellyTagException("no thread or thread group found");
                }
            }
        }
        catch (InterruptedException e) {
            throw new JellyTagException(e);
        }
    }

    /** Implement this method to do something with the thread */
    protected abstract void useThread(Thread thread, XMLOutput output) throws InterruptedException ;

    /** Implement this method to do something with the threadGroup */
    protected abstract void useThreadGroup(List threadGroup, XMLOutput output) throws InterruptedException ;

    /**
     * Set the thread to use in some way.
     */
    public void setThread(Thread thread) {
        this.thread = thread;
    }

    /**
     * Get a reference to the thread to use
     */
    public Thread getThread() {
        return thread;
    }

    /**
     * Set the thread group to "use".
     * @param threadGroup The threadGroup created with the <i>group</i> tag.
     */
    public void setThreadGroup(List threadGroup) {
        this.threadGroup = threadGroup;
    }

    /**
     * Get the thread group
     */
    public List getThreadGroup() {
        return threadGroup;
    }

    /**
     * If true the tag will search for a parent thread tag to "use" if
     * no thread was set via <i>setThread</i>. This is <i>true</i> by default.
     */
    public void setSearchForParentThread(boolean searchForParent) {
        this.searchForParent = searchForParent;
    }
}
