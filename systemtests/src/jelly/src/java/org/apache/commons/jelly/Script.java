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

package org.apache.commons.jelly;

/**
 * <p><code>Script</code> represents a Jelly script.
 * A Script <b>must</b> be thread safe so care should be taken on the
 * implementations of Scripts. However Tags are only used in a single thread
 * (each thread will have create its own Tags for the Script it is running)
 * so multi threading is not a concern for Tag developers.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.12 $
 */
public interface Script {

    /** Called by the parser to allow a more efficient
     * representation of the script to be used.
     */
    public Script compile() throws JellyException;

    /** Evaluates the body of a tag */
    public void run(JellyContext context, XMLOutput output) throws JellyTagException;

}
