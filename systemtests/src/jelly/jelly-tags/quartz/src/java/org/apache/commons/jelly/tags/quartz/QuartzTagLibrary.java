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

import org.apache.commons.jelly.TagLibrary;

/** Tag library for the Quartz enterprise job scheduler.
 *
 *  <p>
 *  <a href="http://quartz.sf.net/">quartz @ sourceforge</a>
 *  </p>
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class QuartzTagLibrary extends TagLibrary
{
    /** Construct and register tags.
     */
    public QuartzTagLibrary()
    {
        registerTag( "job",
                     JobTag.class );

        registerTag( "cron",
                     CronTriggerTag.class );

        registerTag( "wait-for-scheduler",
                     WaitForSchedulerTag.class );
    }
}
