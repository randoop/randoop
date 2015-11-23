package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

import randoop.operation.MethodSignatures;
import randoop.operation.OperationParseException;

/**
 * Serialized form of {@link CheckRep} allowing tests to be serialized.
 * 
 * Also see <code>CheckRep.writeReplace</code>.
 */
public class SerializableCheckRepContract implements Serializable {

  private static final long serialVersionUID = -7030830526035671668L;
  private final String checkRepMethod;

  public SerializableCheckRepContract(Method checkRepMethod) {
    this.checkRepMethod = MethodSignatures.getSignature(checkRepMethod);
  }

  private Object readResolve() throws ObjectStreamException, OperationParseException {
    Method m = MethodSignatures.getMethodForSignature(checkRepMethod);
    return new CheckRepContract(m);
  }
}
