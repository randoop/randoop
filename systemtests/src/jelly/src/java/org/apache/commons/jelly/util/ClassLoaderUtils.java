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

package org.apache.commons.jelly.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class to centralize the class loader management code.
 */
public class ClassLoaderUtils {

    /** log for debug etc output */
    private static final Log log = LogFactory.getLog(ClassLoaderUtils.class);
    
    /**
     * Return the class loader to be used for instantiating application objects
     * when required.  This is determined based upon the following rules:
     * <ul>
     * <li>The specified class loader, if any</li>
     * <li>The thread context class loader, if it exists and <code>useContextClassLoader</code> is true</li>
     * <li>The class loader used to load the calling class.
     * <li>The System class loader.
     * </ul>
     */
    public static ClassLoader getClassLoader(ClassLoader specifiedLoader, boolean useContextClassLoader, Class callingClass) {
        if (specifiedLoader != null) {
            return specifiedLoader;
        }
        if (useContextClassLoader) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                return classLoader;
            }
        }
        return getClassLoader(callingClass);
    }

    /**
     * Return the class loader to be used for instantiating application objects
     * when a context class loader is not specified.  This is determined based upon the following rules:
     * <ul>
     * <li>The specified class loader, if any</li>
     * <li>The class loader used to load the calling class.
     * <li>The System class loader.
     * </ul>
     */
    public static ClassLoader getClassLoader(ClassLoader specifiedLoader, Class callingClass) {
        if (specifiedLoader != null) {
            return specifiedLoader;
        }
        return getClassLoader(callingClass);
    }

    /**
     * Get the loader for the given class. 
     * @param clazz the class to retrieve the loader for
     * @return the class loader that loaded the provided class
     */
    public static ClassLoader getClassLoader(Class clazz) {
        ClassLoader callersLoader = clazz.getClassLoader();
        if (callersLoader == null) {
            callersLoader = ClassLoader.getSystemClassLoader();
        }
        return callersLoader;
    }

    /**
     * Loads the given class using the current Thread's context class loader first
     * otherwise use the class loader which loaded this class.
     */
    public static Class loadClass(String className, Class callingClass) throws ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            return getClassLoader(callingClass).loadClass(className);
        } else {
            return loader.loadClass(className);
        }
    }

    /**
     * Loads the given class using:
     * <ol>
     * <li>the specified classloader,</li>
     * <li>the current Thread's context class loader first, if asked</li>
     * <li>otherwise use the class loader which loaded this class.</li>
     * </ol>
     */
    public static Class loadClass(String className, ClassLoader specifiedLoader, boolean useContextLoader, Class callingClass) throws ClassNotFoundException {
        Class clazz = null;
        if (specifiedLoader != null) {
            try {
                clazz = specifiedLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                log.debug("couldn't find class in specified loader", e);
            }
        }
        if (clazz == null && useContextLoader) {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            if (contextLoader != null) {
                try {
                    clazz = contextLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    log.debug("couldn't find class in specified loader", e);
                }
            }
        }
        if (clazz == null) {
            ClassLoader loader = getClassLoader(callingClass);
            clazz = loader.loadClass(className);
        }
        return clazz;
    }
}
