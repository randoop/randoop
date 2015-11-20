package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

import randoop.operation.MethodCall;
import randoop.operation.MethodSignatures;
import randoop.operation.OperationParseException;

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
