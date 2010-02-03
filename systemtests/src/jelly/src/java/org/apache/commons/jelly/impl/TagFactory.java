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
package org.apache.commons.jelly.impl;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Tag;

import org.xml.sax.Attributes;

/**
 * <p><code>TagFactory</code> represents a Factory of {@link Tag} instances.</p>
 *
 * <b>Note</b> that this class should be re-entrant and used
 * concurrently by multiple threads.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.16 $
 */
public interface TagFactory {

    /**
     * Creates a Tag for the given local name and the SAX attributes
     */
    public Tag createTag(String name, Attributes attributes) throws JellyException;
}
