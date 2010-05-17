package randoop;

import java.io.ObjectStreamException;
import java.lang.reflect.Method;

import randoop.util.PrimitiveTypes;
import randoop.util.Util;

/**
 * WARNING: This code is old and hasn't been tested recently.
 * 
 * An check recording the value of an observer call on a
 * variable evaluated during execution. For example:
 *
 *     x.F() == value
 *
 *
 * Obviously, this is not a property that must hold of all objects in a test.
 * Randoop creates an instance of this contract when, during execution of
 * a sequence, it determines that the above property holds. The property
 * thus represents a <i>regression</i> as it captures the behavior of the
 * code when it is executed.

 * where value is a primitive value or a String.
 */
public final class ObserverEqValue implements ObjectContract {

  // serial version id not actually used because this class
  // declares a writeReplace() method, but javac complains
  // if the field is missing.
  private static final long serialVersionUID = 20100429; 

  /* The observer method */
  public Method observer;

  /* The runtime value of the observer */
  public Object value;

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (!(o instanceof ObserverEqValue)) {
      return false;
    }
    ObserverEqValue other = (ObserverEqValue)o;
    return observer.equals(other.observer) && Util.equalsWithNull(value, other.value);
  }

  @Override
  public int hashCode() {
    int h = 7;
    h = h * 31 + observer.hashCode();
    h = h * 31 + (value == null ? 0 : value.hashCode());
    return h;
  }

  public ObserverEqValue(Method observer, Object value) {
    this.observer = observer;
    assert (this.value == null)
      || PrimitiveTypes.isBoxedPrimitiveTypeOrString (this.value.getClass())
      : "obs value/class = " + this.value +"/" + this.value.getClass()
      + " observer = " + observer;
  }

  /**
   * Serialize with a String version of Method
   */
  private Object writeReplace() throws ObjectStreamException {
    return new SerializableObserverEqValue(observer, value);
  }

  public String toCodeStringPreStatement() {
    return "";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Regression assertion (captures the current behavior of the code)" + Globals.lineSep);
    b.append("assertTrue(");

    String methodname = observer.getName();
    if (value == null) {
      b.append(String.format ("x0.%s() == null", methodname));
    } else if (observer.getReturnType().isPrimitive()) {
      b.append(String.format ("x0.%s()  == %s", methodname,
                              PrimitiveTypes.toCodeString(value)));
    } else { // string
      // System.out.printf ("value = %s - %s\n", value, value.getClass());
      b.append(String.format ("x0.%s().equals(%s)", methodname,
                              PrimitiveTypes.toCodeString(value)));
    }

    // Close assert.
    b.append(");");

    return b.toString();
  }

  /**
   * Create an ObserverEqValue from its basic parts (used when
   * reading from a serialized file).  Note that the 'val' parameter
   * is the actual value to check, not the object from the sequence
   * execution (as it is with the public constructor)
   */
  public static ObserverEqValue getObserverEqValue (Method method,
                                                    Object val) {
    return new ObserverEqValue (method, val);
  }

  @Override
  public boolean evaluate(Object... objects) throws Throwable {
    assert objects.length == 0;
    throw new RuntimeException("not implemented.");
//    try {
//      this.value = observer.invoke (obj);
//    } catch (Exception e) {
//      throw new RuntimeException ("unexpected error invoking observer "
//                                  + observer + " on " + var + "[" +
//                                  var.getType() + "]" + " with value "
//                                  + obj + " [" + obj.getClass() + "]",
//                                  e);
//    }
  }

  @Override
  public int getArity() {
    return 1;
  }

  @Override
  public String toCommentString() {
    return toCodeString();
  }

  @Override
  public boolean evalExceptionMeansFailure() {
    return true;
  }
  
}
