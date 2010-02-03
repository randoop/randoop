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

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/** Implementation of a quart <code>Job</code> to execute jellyscript.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class JellyJob implements Job
{
    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /** Construct.
     */
    public JellyJob()
    {
        // intentionally left blank.
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //     org.quartz.Job
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /** Execute this job.
     *
     *  @param jobContext Job context data.
     *
     *  @throws JobExecutionException If an error occurs during job execution.
     */
    public void execute(JobExecutionContext jobContext) throws JobExecutionException
    {

        JobDetail  detail = jobContext.getJobDetail();

        JobDataMap data   = detail.getJobDataMap();

        Script script = (Script) data.get( "jelly.script" );

        JellyContext jellyContext = (JellyContext) data.get( "jelly.context" );

        XMLOutput    output       = (XMLOutput) data.get( "jelly.output" );

        try
        {
            script.run( jellyContext,
                        output );
            output.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new JobExecutionException( e,
                                             false );
        }
    }
}
