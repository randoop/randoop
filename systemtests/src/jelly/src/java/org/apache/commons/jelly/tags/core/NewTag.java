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

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;

/** A tag which creates a new object of the given type
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision:$
  */
public class NewTag extends BaseClassLoaderTag implements ArgTagParent {

    /** the variable exported */
    private String var;

    /** the class name of the object to instantiate */
    private String className;

    private List paramTypes = new ArrayList();
    private List paramValues = new ArrayList();

    public NewTag() {
    }

    /** Sets the name of the variable exported by this tag */
    public void setVar(String var) {
        this.var = var;
    }

    /** Sets the class name of the object to instantiate */
    public void setClassName(String className) {
        this.className = className;
    }

    public void addArgument(Class type, Object value) {
        paramTypes.add(type);
        paramValues.add(value);
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        ArgTag parentArg = null;
        if ( var == null ) {
            parentArg = (ArgTag)(findAncestorWithClass(ArgTag.class));
            if(null == parentArg) {
                throw new MissingAttributeException( "var" );
            }
        }
        if ( className == null ) {
            throw new MissingAttributeException( "className" );
        }
        invokeBody(output);

        try {
            Class theClass = getClassLoader().loadClass( className );
            Object object = null;
            if(paramTypes.size() == 0) {
                object = theClass.newInstance();
            } else {
                Object[] values = paramValues.toArray();
                Class[] types = (Class[])(paramTypes.toArray(new Class[paramTypes.size()]));
                object = ConstructorUtils.invokeConstructor(theClass,values,types);
                paramTypes.clear();
                paramValues.clear();
            }
            if(null != var) {
                context.setVariable(var, object);
            } else {
                parentArg.setValue(object);
            }
        }
        catch (ClassNotFoundException e) {
            throw new JellyTagException(e);
        }
        catch (InstantiationException e) {
            throw new JellyTagException(e);
        }
        catch (NoSuchMethodException e) {
            throw new JellyTagException(e);
        }
        catch (IllegalAccessException e) {
            throw new JellyTagException(e);
        }
        catch (InvocationTargetException e) {
            throw new JellyTagException(e);
        }
    }
}
