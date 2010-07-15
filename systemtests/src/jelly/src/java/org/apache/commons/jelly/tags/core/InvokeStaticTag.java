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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.util.ClassLoaderUtils;

/**
  * A Tag which can invoke a static method on a class, without an
  * instance of the class being needed.
  * <p>
  * Like the {@link InvokeTag}, this tag can take a set of
  * arguments using the {@link ArgTag}.
  * </p>
  * <p>
  *  The following attributes are required:<br />
  * <ul>
  *   <li>var - The variable to assign the return of the method call to</li>
  *   <li>method - The name of the static method to invoke</li>
  *   <li>className - The name of the class containing the static method</li>
  * </ul>
  * </p>
  *
  * @author <a href="mailto:robert@bull-enterprises.com>Robert McIntosh</a>
  * @version $Revision: 1.9 $
  */
public class InvokeStaticTag extends TagSupport implements ArgTagParent {

    /** the variable exported */
    private String var;

    /** the variable where the method's exception is exported */
    private String exceptionVar;

    /** the method to invoke */
    private String methodName;

    /** the object to invoke the method on */
    private String className;

    private List paramTypes = new ArrayList();
    private List paramValues = new ArrayList();

    public InvokeStaticTag() {
    }

    /**
     * Sets the name of the variable exported by this tag
     *
     * @param var The variable name
     */
    public void setVar(String var) {
        this.var = var;
    }

    /** Sets the name of a variable that exports the exception thrown by
     * the method's invocation (if any)
     */
    public void setExceptionVar(String var) {
        this.exceptionVar = var;
    }

    /**
     * Sets the name of the method to invoke
     *
     * @param method The method name
     */
    public void setMethod(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Sets the fully qualified class name containing the static method
     *
     * @param className The name of the class
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Adds an argument to supply to the method
     *
     * @param type The Class type of the argument
     * @param value The value of the argument
     */
    public void addArgument(Class type, Object value) {
        paramTypes.add(type);
        paramValues.add(value);
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        try {
            if ( null == methodName) {
                throw new MissingAttributeException( "method" );
            }
            invokeBody(output);

            Object[] values = paramValues.toArray();
            Class[] types = (Class[])(paramTypes.toArray(new Class[paramTypes.size()]));
            Method method = loadClass().getMethod( methodName, types );
            Object result = method.invoke( null, values );
            if(null != var) {
                context.setVariable(var, result);
            }

            ArgTag parentArg = (ArgTag)(findAncestorWithClass(ArgTag.class));
            if(null != parentArg) {
                parentArg.setValue(result);
            }
        }
        catch (ClassNotFoundException e) {
            throw createLoadClassFailedException(e);
        }
        catch (NoSuchMethodException e) {
            throw createLoadClassFailedException(e);
        }
        catch (IllegalAccessException e) {
            throw createLoadClassFailedException(e);
        }
        catch (InvocationTargetException e) {
            if(null != exceptionVar) {
                context.setVariable(exceptionVar,e.getTargetException());
            } else {
                throw new JellyTagException("method " + methodName +
                    " threw exception: "+ e.getTargetException().getMessage(),
                    e.getTargetException() );
            }
        }
        finally {
            paramTypes.clear();
            paramValues.clear();
        }
    }

    // Tag interface
    //-------------------------------------------------------------------------

    /**
     * Loads the class using either the class loader which loaded me or the
     * current threads context class loader
     */
    protected Class loadClass() throws ClassNotFoundException {
        return ClassLoaderUtils.loadClass(className, getClass());
    }

    /**
     * Factory method to create a new JellyTagException instance from a given
     * failure exception
     * @param e is the exception which occurred attempting to load the class
     * @return JellyTagException
     */
    protected JellyTagException createLoadClassFailedException(Exception e) {
        return new JellyTagException(
            "Could not load class: " + className + ". Reason: " + e, e
        );
    }
}

