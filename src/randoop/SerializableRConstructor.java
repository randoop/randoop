package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;

import randoop.util.Reflection;

public class SerializableRConstructor implements Serializable {

  private static final long serialVersionUID = -4118503748722441553L;
  private final String constructor;

  public SerializableRConstructor(Constructor<?> constructor) {
    this.constructor = Reflection.getSignature(constructor);
  }

  private Object readResolve() throws ObjectStreamException {
    return RConstructor.getRConstructor(Reflection.getConstructorForSignature(constructor));
  }

}
