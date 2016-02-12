package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.reflection.visibilitytest.PackageSubclass;

/**
 * A test to ensure that reflective collection of methods includes the 
 * visibility bridge, which is a bridge method that fakes the "inheritance" of
 * a public method from a package private class.
 */
public class VisibilityBridgeTest {
  
  //can't compare method of superclass directly to method of subclass
  //so need to convert to abstraction to allow list search
  private class FormalMethod {
    private Class<?> returnType;
    private String name;
    private Class<?>[] parameterTypes;

    public FormalMethod(Method m) {
      this.returnType = m.getReturnType();
      this.name = m.getName();
      this.parameterTypes = m.getParameterTypes();
    }
    
    public FormalMethod(MethodCall call) {
      this(call.getMethod());
    }
    
    public boolean equals(FormalMethod m) {
      if (! this.returnType.equals(m.returnType)) return false;
      if (! this.name.equals(m.name)) return false;
      if (this.parameterTypes.length != m.parameterTypes.length) return false;
      for (int i = 0; i < this.parameterTypes.length; i++) {
        if (! this.parameterTypes[i].equals(m.parameterTypes[i])) return false;
      }
      return true;
    }
    
    @Override 
    public boolean equals(Object obj) {
      if (! (obj instanceof FormalMethod)) return false;
      FormalMethod m = (FormalMethod)obj;
      return this.equals(m);
    }
    public String getName() { return name;}
  }
  
  /**
   * This test is simply to ensure that reflective collection of methods
   * includes the visibility bridge, which looks like an "inherited" public 
   * method of package private class.
   */
  @Test
  public void testVisibilityBridge() {
    ArrayList<Class<?>> classes = new ArrayList<>();
    Class<?> sub = PackageSubclass.class;
    classes.add(sub);
    
    //should only inherit public non-synthetic methods of package private superclass
    List<FormalMethod> include = new ArrayList<>();
    try {
      Class<?> sup = Class.forName("randoop.reflection.visibilitytest.PackagePrivateBase");
      for (Method m : sup.getDeclaredMethods()) {
        if (Modifier.isPublic(m.getModifiers()) && ! m.isBridge() && ! m.isSynthetic()) {
          include.add(new FormalMethod(m));
        }
      }
    } catch (ClassNotFoundException e) {
      fail("test failed because unable to find base class");
    }
    
    List<Operation> actualOps = OperationExtractor.getOperations(classes, null);
    assertEquals("expect operations count to be inherited methods plus constructor", include.size() + 1, actualOps.size());
    
    List<FormalMethod> actual = new ArrayList<>();
    for (Operation op : actualOps) {
      if (op instanceof MethodCall) {
        actual.add(new FormalMethod((MethodCall)op));
      }
    }
    
    for (FormalMethod m : include) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(m));
    }
  }

}
