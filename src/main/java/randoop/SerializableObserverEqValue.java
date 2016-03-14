package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

import randoop.operation.MethodSignatures;
import randoop.operation.OperationParseException;

/**
 * Serialized form of {@link ObserverEqValue} allowing tests to be serialized.
 *
 * Also see <code>ObserverEqValue.writeReplace</code>.
 */
public class SerializableObserverEqValue implements Serializable {

  private static final long serialVersionUID = 20090716L;
  private final String observer;
  private final Object value;

  public SerializableObserverEqValue(Method observer, Object value) {
    this.observer = MethodSignatures.getSignatureString(observer);
    this.value = value;
    // System.out.printf ("Serializing %s %s %s%n", this.observer, var, value);
  }

  private Object readResolve() throws ObjectStreamException, OperationParseException {
    Method m = MethodSignatures.getMethodForSignatureString(observer);
    return ObserverEqValue.getObserverEqValue(m, value);
  }
}
