package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;

/**
 *  Serializable version of {@link ConstructorCall} allowing tests to be
 *  serialized. In particular, uses string representation of the constructor based on
 *  {@link ConstructorSignatures#getSignatureString(Constructor)}.
 *  
 *  @see ConstructorCall#writeReplace
  */
public class SerializableConstructorCall implements Serializable {

  private static final long serialVersionUID = -4118503748722441553L;
  private final String constructor;

  public SerializableConstructorCall(Constructor<?> constructor) {
    this.constructor = ConstructorSignatures.getSignatureString(constructor);
  }

  private Object readResolve() throws ObjectStreamException, OperationParseException {
    return ConstructorCall.createConstructorCall(ConstructorSignatures.getConstructorForSignatureString(constructor));
  }

}
