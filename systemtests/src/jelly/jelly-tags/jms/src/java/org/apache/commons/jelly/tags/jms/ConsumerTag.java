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
package org.apache.commons.jelly.tags.jms;

import javax.jms.MessageListener;

/**
 * Represents an interface for a Tag which consumes JMS messages.
 * By default this is the &lt;subscribe&gt; tag but other tags could
 * implement this interface to enabled things like Message pipelining,
 * transactional message consumer tags, stopwatch wrappers etc.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.4 $
 */
public interface ConsumerTag {

    /**
     * Sets the JMS messageListener used to consume JMS messages
     */
    public void setMessageListener(MessageListener messageListener);

}
