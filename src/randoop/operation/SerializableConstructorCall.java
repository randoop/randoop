package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;

public class SerializableConstructorCall implements Serializable {

  private static final long serialVersionUID = -4118503748722441553L;
  private final String constructor;

  public SerializableConstructorCall(Constructor<?> constructor) {
    this.constructor = ConstructorSignatures.getSignature(constructor);
  }

  private Object readResolve() throws ObjectStreamException, OperationParseException {
    return ConstructorCall.getConstructorCall(ConstructorSignatures.getConstructorForSignature(constructor));
  }

}
