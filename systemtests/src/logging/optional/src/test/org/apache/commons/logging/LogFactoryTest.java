/*
 * Copyright 2004 The Apache Software Foundation.
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


package org.apache.commons.logging;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import junit.framework.TestCase;

import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.commons.logging.impl.WeakHashtable;

public class LogFactoryTest extends TestCase {

    
    /** Maximum number of iterations before our test fails */
    private static final int MAX_GC_ITERATIONS = 50;

    private ClassLoader origLoader          = null;
    private String      origFactoryProperty = null;

    public LogFactoryTest(String testName) {
        super(testName);
    }
    
    public void testLogFactoryType() {
        assertTrue(LogFactory.factories instanceof WeakHashtable);
    }
    
    /**
     * Tests that LogFactories are not removed from the map
     * if their creating ClassLoader is still alive.
     */ 
    public void testHoldFactories() throws Exception
    {
        // 1) Basic test
        
        // Get a weak reference to the factory using the classloader.
        // When this reference is cleared we know the factory has been 
        // cleared from LogFactory.factories as well
        WeakReference weakFactory = loadFactoryFromContextClassLoader();
        // Run the gc, confirming that the factory
        // is not dropped from the map even though there are 
        // no other references to it        
        checkRelease(weakFactory, true);
        
        // 2) Test using an isolated classloader a la a web app
        
        // Create a classloader that isolates commons-logging
        ClassLoader childLoader = new IsolatedClassLoader(origLoader);
        Thread.currentThread().setContextClassLoader(childLoader);
        weakFactory = loadFactoryFromContextClassLoader();
        Thread.currentThread().setContextClassLoader(origLoader);  
        // At this point we still have a reference to childLoader,
        // so the factory should not be cleared
        
        checkRelease(weakFactory, true);
    }

    /**
     * Tests that a ClassLoader is eventually removed from the map
     * after all hard references to it are removed. 
     */
    public void testReleaseClassLoader() throws Exception
    {
        // 1) Test of a child classloader that follows the Java2
        //    delegation model (e.g. an EJB module classloader)
                
        // Create a classloader that delegates to its parent        
        ClassLoader childLoader = new ClassLoader() {};   
        // Get a weak reference to the factory using the classloader.
        // When this reference is cleared we know the factory has been 
        // cleared from LogFactory.factories as well
        Thread.currentThread().setContextClassLoader(childLoader);
        loadFactoryFromContextClassLoader();
        Thread.currentThread().setContextClassLoader(origLoader);    
        
        // Get a WeakReference to the child loader so we know when it
        // has been gc'ed
        WeakReference weakLoader = new WeakReference(childLoader);     
        // Remove any hard reference to the childLoader or the factory creator
        childLoader   = null;

        // Run the gc, confirming that childLoader is dropped from the map
        checkRelease(weakLoader, false);
        
        // 2) Test using an isolated classloader a la a web app
        
        childLoader = new IsolatedClassLoader(origLoader);
        Thread.currentThread().setContextClassLoader(childLoader);
        loadFactoryFromContextClassLoader();
        Thread.currentThread().setContextClassLoader(origLoader);
        weakLoader  = new WeakReference(childLoader);
        childLoader = null;  // somewhat equivalent to undeploying a webapp  
        
        checkRelease(weakLoader, false);
        
    }
    
    /**
     * Repeatedly run the gc, checking whether the given WeakReference 
     * is not cleared and failing or succeeding based on
     * parameter <code>failOnRelease</code>.
     */
    private void checkRelease(WeakReference reference, boolean failOnRelease) {
        
        int iterations = 0;
        int bytz = 2;
        while(true) {
            System.gc();
            if(iterations++ > MAX_GC_ITERATIONS){
                if (failOnRelease) {
                    break;
                }
                fail("Max iterations reached before reference released.");
            }
            
            if(reference.get() == null) {
                if (failOnRelease) {
                    fail("reference released");
                }
                else {
                    break;
                }
            } else {
                // create garbage:
                byte[] b;
                try {
                    b =  new byte[bytz];
                    bytz = bytz * 2;
                }
                catch (OutOfMemoryError oom) {
                    // Doing this is probably a no-no, but it seems to work ;-)
                    b = null;
                    System.gc();
                    if (failOnRelease) {
                        break;
                    }
                    fail("OutOfMemory before reference released.");
                }
            }
        }
    }

    protected void setUp() throws Exception {
        // Preserve the original classloader and factory implementation
        // class so we can restore them when we are done
        origLoader          = Thread.currentThread().getContextClassLoader();
        origFactoryProperty = System.getProperty(LogFactory.FACTORY_PROPERTY);
        
        // Ensure we use LogFactoryImpl as our factory
        System.setProperty(LogFactory.FACTORY_PROPERTY, 
                           LogFactoryImpl.class.getName());
        
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        // Set the  classloader back to whatever it originally was
        Thread.currentThread().setContextClassLoader(origLoader);
        
        // Set the factory implementation class back to 
        // whatever it originally was
        if (origFactoryProperty != null) {
            System.setProperty(LogFactory.FACTORY_PROPERTY, 
                               origFactoryProperty);
        }
        else {
            System.getProperties().remove(LogFactory.FACTORY_PROPERTY);
        }
        
        super.tearDown();
    }
    
    private static WeakReference loadFactoryFromContextClassLoader() 
            throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class clazz = loader.loadClass(SubDeploymentClass.class.getName());
        IFactoryCreator creator = (IFactoryCreator) clazz.newInstance();
        return creator.getWeakFactory();
    }
    
    /**
     * A ClassLoader that mimics the operation of a web app classloader
     * by not delegating some calls to its parent.  
     * 
     * In this case it does not delegate loading commons-logging classes, 
     * acting as if commons-logging were in WEB-INF/lib.  However, it does 
     * delegate loading of IFactoryCreator, thus allowing this class to 
     * interact with SubDeploymentClass via IFactoryCreator.
     */
    private static final class IsolatedClassLoader extends ClassLoader {
        
        private IsolatedClassLoader(ClassLoader parent) {
            super(parent);
        }
        
        protected synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
        {
            if (name != null && name.startsWith("org.apache.commons.logging")
                    && "org.apache.commons.logging.IFactoryCreator".equals(name) == false) {
                // First, check if the class has already been loaded
                Class c = findClass(name);
            
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
            else {
                return super.loadClass(name, resolve);
            }
        }
        
        protected Class findClass(String name) throws ClassNotFoundException {
            if (name != null && name.startsWith("org.apache.commons.logging")
                    && "org.apache.commons.logging.IFactoryCreator".equals(name) == false) {
                try {
                    InputStream is = getResourceAsStream( name.replace('.','/').concat(".class"));
                    byte[] bytes = new byte[1024];                
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                    int read;
                    while ((read = is.read(bytes)) > -1) {
                        baos.write(bytes, 0, read);
                    }
                    bytes = baos.toByteArray();
                    return this.defineClass(name, bytes, 0, bytes.length);
                } catch (FileNotFoundException e) {
                    throw new ClassNotFoundException("cannot find " + name, e);
                } catch (IOException e) {
                    throw new ClassNotFoundException("cannot read " + name, e);
                }
            }
            else {
                return super.findClass(name);
            }
        }
        
    }
    
}
