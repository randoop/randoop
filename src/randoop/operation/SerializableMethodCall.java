package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

/** 
 * Serializable version of {@link MethodCall} allowing tests to be serialized.
 * 
 * Also see <code>MethodCall.writeReplace</code>
 */
public class SerializableMethodCall implements Serializable {

  private static final long serialVersionUID = -6481763909765960881L;
  private final String method;

  public SerializableMethodCall(Method method) {
    this.method = MethodSignatures.getSignature(method);
  }

  private Object readResolve() throws ObjectStreamException, OperationParseException {
    return MethodCall.getMethodCall(MethodSignatures.getMethodForSignature(method));
  }

}
