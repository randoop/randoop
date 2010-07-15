package org.apache.commons.jelly.tags.quartz;

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

import org.apache.commons.jelly.XMLOutput;

import org.apache.commons.jelly.JellyTagException;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/** Block and wait for the Quartz scheduler to shutdown.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class WaitForSchedulerTag extends QuartzTagSupport
{
    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /** Construct.
     */
    public WaitForSchedulerTag()
    {
        // intentionally left blank.
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //     org.apache.commons.jelly.Tag
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /** Perform this tag.
     *
     *  @param output Output sink.
     *
     *  @throws Exception If an error occurs.
     */
    public void doTag(XMLOutput output) throws JellyTagException
    {
        try {
            Scheduler sched = getScheduler();

            while ( ! sched.isShutdown() )
            {
                try
                {
                    Thread.sleep( 500 );
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
        }
        catch (SchedulerException e) {
            throw new JellyTagException(e);
        }
    }
}

