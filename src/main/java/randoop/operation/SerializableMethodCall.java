package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Serializable version of {@link MethodCall} allowing tests to be serialized.
 *
 * @see MethodCall#writeReplace
 */
public class SerializableMethodCall implements Serializable {

  private static final long serialVersionUID = -6481763909765960881L;
  private final String method;

  public SerializableMethodCall(Method method) {
    this.method = MethodSignatures.getSignatureString(method);
  }

  private Object readResolve() throws ObjectStreamException, OperationParseException {
    return MethodCall.createMethodCall(MethodSignatures.getMethodForSignatureString(method));
  }
}
