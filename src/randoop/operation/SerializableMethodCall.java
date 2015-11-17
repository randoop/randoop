package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.util.Reflection;

public class SerializableMethodCall implements Serializable {

  private static final long serialVersionUID = -6481763909765960881L;
  private final String method;

  public SerializableMethodCall(MethodCall method) {
    this.method = method.getSignature();
  }

  private Object readResolve() throws ObjectStreamException {
    return MethodCall.getMethodCall(Reflection.getMethodForSignature(method));
  }

}
