/*
 * Copyright 1999-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.chain.generic;


import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.Filter;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

/**
 * <p>This command combines elements of the {@link LookupCommand} with the 
 * {@link DispatchCommand}.  Look up a specified {@link Command} (which could 
 * also be a {@link Chain}) in a {@link Catalog}, and delegate execution to 
 * it.  Introspection is used to lookup the appropriate method to delegate 
 * execution to.  If the delegated-to {@link Command} is also a 
 * {@link Filter}, its <code>postprocess()</code> method will also be invoked 
 * at the appropriate time.</p>
 *
 * <p>The name of the {@link Command} can be specified either directly (via
 * the <code>name</code> property) or indirectly (via the <code>nameKey</code>
 * property).  Exactly one of these must be set.</p>
 *
 * <p>The name of the method to be called can be specified either directly 
 * (via the <code>method</code> property) or indirectly (via the <code>
 * methodKey</code> property).  Exactly one of these must be set.</p>
 * 
 * <p>If the <code>optional</code> property is set to <code>true</code>,
 * failure to find the specified command in the specified catalog will be
 * silently ignored.  Otherwise, a lookup failure will trigger an
 * <code>IllegalArgumentException</code>.</p>
 *
 * @author Sean Schofield
 * @version $Revision: 1.1 $
 */

public class DispatchLookupCommand extends LookupCommand implements Filter {

    // ------------------------------------------------------- Static Variables
    
    /**
     * The base implementation expects dispatch methods to take a <code>
     * Context</code> as their only argument.
     */
    private static final Class[] DEFAULT_SIGNATURE = 
        new Class[] { Context.class };


    // ----------------------------------------------------- Instance Variables
    
    private WeakHashMap methods = new WeakHashMap();


    // ------------------------------------------------------------- Properties

    private String method = null;
    private String methodKey = null;

    public String getMethod() {
        return method;
    }
    public String getMethodKey() {
        return methodKey;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public void setMethodKey(String methodKey) {
        this.methodKey = methodKey;
    }


    // --------------------------------------------------------- Public Methods

    /**
     * <p>Look up the specified command, and (if found) execute it.</p>
     *
     * @param context The context for this request
     *
     * @throws Exception if no such {@link Command} can be found and the 
     *  <code>optional</code> property is set to <code>false</code>
     */
    public boolean execute(Context context) throws Exception {

        if (this.getMethod() == null && this.getMethodKey() == null) {
            throw new IllegalStateException(
                "Neither 'method' nor 'methodKey' properties are defined "
            );
        }

        Command command = getCommand(context);
        
        if (command != null) {
            Method methodObject = extractMethod(command, context);
            Object obj = methodObject.invoke(command, getArguments(context));
            Boolean result = (Boolean)obj;
            
            return (result != null && result.booleanValue());
        } else {
            return false;
        }

    }


    // ------------------------------------------------------ Protected Methods
    
    /**
     * <p>Return a <code>Class[]</code> describing the expected signature of 
     * the method.  The default is a signature that just accepts the command's  
     * {@link Context}.  The method can be overidden to provide a different 
     * method signature.<p>
     * 
     * @return the expected method signature
     */
    protected Class[] getSignature() {
        return DEFAULT_SIGNATURE;
    }

    /**
     * Get the arguments to be passed into the dispatch method.  
     * Default implementation simply returns the context which was passed in, 
     * but subclasses could use this to wrap the context in some other type, 
     * or extract key values from the context to pass in.  The length and types 
     * of values returned by this must coordinate with the return value of 
     * <code>getSignature()</code>
     * 
     * @param context The context associated with the request
     * @return the method arguments to be used
     */
    protected Object[] getArguments(Context context) {
        return new Object[] { context };
    }


    // -------------------------------------------------------- Private Methods


    /**
     * Extract the dispatch method.  The base implementation uses the 
     * command's <code>method</code> property at the name of a method 
     * to look up, or, if that is not defined, uses the <code>
     * methodKey</code> to lookup the method name in the context.
     * 
     * @param command The commmand that contains the method to be 
     *    executed.
     * @param context The context associated with this request
     * @return the dispatch method
     * 
     * @throws NoSuchMethodException if no method can be found under the 
     *    specified name.
     * @throws NullPointerException if no methodName can be determined
     */
    private Method extractMethod(Command command, Context context) 
        throws NoSuchMethodException {

        String methodName = this.getMethod();

        if (methodName == null) {
            Object methodContextObj = context.get(getMethodKey());
            if (methodContextObj == null) {
                throw new NullPointerException("No value found in context under " + 
                                               getMethodKey());
            }
            methodName = methodContextObj.toString();
        }


        Method theMethod = null;

        synchronized (methods) {
            theMethod = (Method) methods.get(methodName);

            if (theMethod == null) {
                theMethod = command.getClass().getMethod(methodName, 
                                                         getSignature());
                methods.put(methodName, theMethod);
            }
        }

        return theMethod;
    }

}
