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
package org.apache.commons.jelly.tags.swing;

import org.apache.commons.jelly.JellyTagException;

import java.awt.Component;

/**
 * An interface which represents a Tag which is capable of containing AWT Components.
 * So tags such as ContainerTag and LayoutTagSupport implement this interface as they can have
 * nested child component tags.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public interface ContainerTag {

    /**
     * Adds a child component to this container with optional constraints.
     * If the constraints are null they are ignored
     */
    public void addChild(Component component, Object constraints) throws JellyTagException;
}
