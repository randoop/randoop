package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;

import randoop.util.Reflection;

public class SerializableConstructorCall implements Serializable {

  private static final long serialVersionUID = -4118503748722441553L;
  private final String constructor;

  public SerializableConstructorCall(Constructor<?> constructor) {
    this.constructor = Reflection.getSignature(constructor);
  }

  private Object readResolve() throws ObjectStreamException {
    return ConstructorCall.getRConstructor(Reflection.getConstructorForSignature(constructor));
  }

}
