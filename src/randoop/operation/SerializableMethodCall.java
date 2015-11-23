package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

import randoop.util.Reflection;

public class SerializableMethodCall implements Serializable {

  private static final long serialVersionUID = -6481763909765960881L;
  private final String method;

  public SerializableMethodCall(Method method) {
    this.method = Reflection.getSignature(method);
  }

  private Object readResolve() throws ObjectStreamException {
    return MethodCall.getRMethod(Reflection.getMethodForSignature(method));
  }

}
