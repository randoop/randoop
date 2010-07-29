package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

import randoop.util.Reflection;

public class SerializableRMethod implements Serializable {

  private static final long serialVersionUID = -6481763909765960881L;
  private final String method;

  public SerializableRMethod(Method method) {
    this.method = Reflection.getSignature(method);
  }

  private Object readResolve() throws ObjectStreamException {
    return RMethod.getRMethod(Reflection.getMethodForSignature(method));
  }

}
