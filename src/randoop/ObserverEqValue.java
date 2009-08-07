package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

import randoop.util.PrimitiveTypes;

/**
 * An observation recording the value of an observer call on a
 * variable evaluated during execution. For example:
 *
 *     var0.getfoo() == value
 *
 * where value is a primitive value or a String.
 */
public class ObserverEqValue implements Observation, Serializable {

  /* The observer method */
  public Method observer;

  /* The instance variable */
  Variable var;

  /* The runtime value of the observer */
  public Object value;

  public String toString() {
    return String.format ("<ObserverEqValue %s %s, value = '%s'", observer,
                          var, value);
  }

  /** Returns the value of this observation as a string **/
  public String get_value() {
    return String.format ("%s", this.value);
  }

  /**
   * Creates the observer.  Gets the current value of the observer and
   * stores it away for later comparison.
   *
   * @param observer - The observer method to call
   * @param var     - The instance variable for the observer (observers with
   *                  arguments are not yet supported)
   * @param obj     - The object from the sequence execution that corresponds
   *                  to var
   */
  public ObserverEqValue(Method observer, Variable var, Object obj) {

    this.observer = observer;
    this.var = var;
    try {
      this.value = observer.invoke (obj);
    } catch (Exception e) {
      throw new RuntimeException ("unexpected error invoking observer "
                                  + observer + " on " + var + "[" +
                                  var.getType() + "]" + " with value "
                                  + obj + " [" + obj.getClass() + "]",
                                  e);
    }
    assert (this.value == null)
      || PrimitiveTypes.isBoxedPrimitiveTypeOrString (this.value.getClass())
      : "obs value/class = " + this.value +"/" + this.value.getClass()
      + " observer = " + observer;
  }

  /**
   * Constructor used when reading as serialized file
   */
  private ObserverEqValue (Variable var, Object value, Method observer) {
    this.observer = observer;
    this.var = var;
    this.value = value;
  }

  /**
   * Serialize with a String version of Method
   */
  private Object writeReplace() throws ObjectStreamException {
    return new SerializableObserverEqValue(observer, var, value);
  }

  public String toCodeStringPreStatement() {
    return "";
  }

  public String toCodeStringPostStatement() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Regression assertion (captures the current behavior of the code)" + Globals.lineSep);
    b.append("assertTrue(");

    String methodname = observer.getName();
    if (value == null) {
      b.append(String.format ("%s.%s() == null", var.getName(), methodname));
    } else if (observer.getReturnType().isPrimitive()) {
      b.append(String.format ("%s.%s()  == %s", var.getName(), methodname,
                              PrimitiveTypes.toCodeString(value)));
    } else { // string
      // System.out.printf ("value = %s - %s\n", value, value.getClass());
      b.append(String.format ("%s.%s().equals(%s)", var.getName(), methodname,
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
                                                    Variable var, Object val) {
    return new ObserverEqValue (var, val, method);
  }

}
