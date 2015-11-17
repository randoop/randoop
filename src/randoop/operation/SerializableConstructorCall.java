package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializableConstructorCall implements Serializable {

  private static final long serialVersionUID = -4118503748722441553L;
  private final String constructor;

  public SerializableConstructorCall(ConstructorCall constructor) {
    this.constructor = constructor.getSignature();
  }

  private Object readResolve() throws ObjectStreamException {
    return ConstructorCall.getRConstructor(ConstructorParser.getConstructorForSignature(constructor));
  }

}
