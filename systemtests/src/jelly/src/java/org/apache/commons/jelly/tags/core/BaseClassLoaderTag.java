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
package org.apache.commons.jelly.tags.core;

import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.util.ClassLoaderUtils;

/** Abstract base tag providing {@link ClassLoader} support.
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @author Rodney Waldhoff
  * @version $Revision: 1.5 $
  */
public abstract class BaseClassLoaderTag extends TagSupport {
    /**
     * The class loader to use for instantiating application objects.
     * If not specified, the context class loader, or the class loader
     * used to load XMLParser itself, is used, based on the value of the
     * <code>useContextClassLoader</code> variable.
     */
    protected ClassLoader classLoader = null;

    /**
     * Do we want to use the Context ClassLoader when loading classes
     * for instantiating new objects?  Default is <code>false</code>.
     */
    protected boolean useContextClassLoader = false;

    /**
     * Return the class loader to be used for instantiating application objects
     * when required.  This is determined based upon the following rules:
     * <ul>
     * <li>The class loader set by <code>setClassLoader()</code>, if any</li>
     * <li>The thread context class loader, if it exists and the
     *     <code>useContextClassLoader</code> property is set to true</li>
     * <li>The class loader used to load the XMLParser class itself.
     * </ul>
     */
    public ClassLoader getClassLoader() {
        return ClassLoaderUtils.getClassLoader(classLoader, useContextClassLoader, getClass());
    }

    /**
     * Set the class loader to be used for instantiating application objects
     * when required.
     *
     * @param classLoader The new class loader to use, or <code>null</code>
     *  to revert to the standard rules
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Return the boolean as to whether the context classloader should be used.
     */
    public boolean getUseContextClassLoader() {
        return useContextClassLoader;
    }

    /**
     * Determine whether to use the Context ClassLoader (the one found by
     * calling <code>Thread.currentThread().getContextClassLoader()</code>)
     * to resolve/load classes.  If not
     * using Context ClassLoader, then the class-loading defaults to
     * using the calling-class' ClassLoader.
     *
     * @param boolean determines whether to use JellyContext ClassLoader.
     */
    public void setUseContextClassLoader(boolean use) {
        useContextClassLoader = use;
    }
}
