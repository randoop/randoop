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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
  * A tag which calls a method in an object instantied by core:new
  *
  *
  * @author Rodney Waldhoff
  * @version $Revision: 1.10 $
  */
public class InvokeTag extends TagSupport implements ArgTagParent {

    /** the variable exported */
    private String var;

    /** the variable where the method's exception is exported */
    private String exceptionVar;

    /** the method to invoke */
    private String methodName;

    /** the object to invoke the method on */
    private Object onInstance;

    private List paramTypes = new ArrayList();
    private List paramValues = new ArrayList();

    public InvokeTag() {
    }

    /** Sets the name of the variable exported by this tag */
    public void setVar(String var) {
        this.var = var;
    }

    /** Sets the name of a variable that exports the exception thrown by
     * the method's invocation (if any)
     */
    public void setExceptionVar(String var) {
        this.exceptionVar = var;
    }

    public void setMethod(String method) {
        this.methodName = method;
    }

    public void setOn(Object instance) {
        this.onInstance = instance;
    }

    public void addArgument(Class type, Object value) {
        paramTypes.add(type);
        paramValues.add(value);
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if ( null == methodName) {
            throw new MissingAttributeException( "method" );
        }
        if ( null == onInstance ) {
            throw new MissingAttributeException( "on" );
        }

        invokeBody(output);

        Object[] values = paramValues.toArray();
        Class[] types = (Class[])(paramTypes.toArray(new Class[paramTypes.size()]));

        Object result = null;
        try {
            result = MethodUtils.invokeMethod(onInstance,methodName,values,types);
        }
        catch (NoSuchMethodException e) {
            throw new JellyTagException(e);
        }
        catch (IllegalAccessException e) {
            throw new JellyTagException(e);
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

        ArgTag parentArg = (ArgTag)(findAncestorWithClass(ArgTag.class));
        if(null != parentArg) {
            parentArg.setValue(result);
        }
        if(null != var) {
            context.setVariable(var, result);
        }
    }
}
