package org.apache.commons.chain.generic;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import java.lang.reflect.Method;
import java.util.WeakHashMap;
import java.lang.reflect.InvocationTargetException;

/**
 * An abstract base command which uses introspection to look up a method to execute.  
 * For use by developers who prefer to group related functionality into a single class
 * rather than an inheritance family.
 */
public abstract class DispatchCommand implements Command {

    protected WeakHashMap methods = new WeakHashMap();

    protected String method = null;

    protected String methodKey = null;

    /**
     * The base implementation expects dispatch methods to take a <code>Context</code>
     * as their only argument.
     */
    protected static final Class[] DEFAULT_SIGNATURE = new Class[] { Context.class };


    /**
     * Look up the method specified by either "method" or "methodKey" and invoke it,
     * returning a boolean value as interpreted by <code>evaluateResult</code>.
     * @param context
     * @return
     * @throws Exception
     */
    public boolean execute(Context context) throws Exception {

        if (this.getMethod() == null && this.getMethodKey() == null) {
            throw new IllegalStateException("Neither 'method' nor 'methodKey' properties are defined ");
        }

        Method methodObject = extractMethod(context);

        return evaluateResult(methodObject.invoke(this, getArguments(context)));
    }

    /**
     * Extract the dispatch method.  The base implementation uses the command's 
     * <code>method</code> property at the name of a method to look up, or, if that is not defined,
     * 
     * and <code>methodKey</code>
     * @param context
     * @return
     * @throws NoSuchMethodException if no method can be found under the specified name.
     * @throws NullPointerException if no methodName can be determined
     */
    protected Method extractMethod(Context context) throws NoSuchMethodException {

        String methodName = this.getMethod();

        if (methodName == null) {
            Object methodContextObj = context.get(this.getMethodKey());
            if (methodContextObj == null) {
                throw new NullPointerException("No value found in context under " + this.getMethodKey());
            }
            methodName = methodContextObj.toString();
        }


        Method theMethod = null;

        synchronized (methods) {
            theMethod = (Method) methods.get(methodName);

            if (theMethod == null) {
                theMethod = getClass().getMethod(methodName, getSignature());
                methods.put(methodName, theMethod);
            }
        }

        return theMethod;
    }

    /**
     * Evaluate the result of the method invocation as a boolean value.  Base implementation
     * expects that the invoked method returns boolean true/false, but subclasses might
     * implement other interpretations.
     * @param o
     * @return
     */
    protected boolean evaluateResult(Object o) {
        
        Boolean result = (Boolean) o;
        return (result != null && result.booleanValue());
        
    }

    /**
     * Return a <code>Class[]</code> describing the expected signature of the method 
     * @return
     */
    protected Class[] getSignature() {
        return DEFAULT_SIGNATURE;
    }
    
    /**
     * Get the arguments to be passed into the dispatch method.  
     * Default implementation simply returns the context which was passed in, but subclasses
     * could use this to wrap the context in some other type, or extract key values from the 
     * context to pass in.  The length and types of values returned by this must coordinate
     * with the return value of <code>getSignature()</code>
     * @param context
     * @return
     */
    protected Object[] getArguments(Context context) {
        return new Object[] { context };
    }

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

    

}