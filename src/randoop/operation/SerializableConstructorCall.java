package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;

/**
 *  Serializable version of {@link ConstructorCall} allowing tests to be
 *  serialized. In particular, uses string representation of the constructor based on
 *  {@link ConstructorSignatures#getSignature(Constructor)}.
 *  
 *  Also, see <code>ConstructorCall.writeReplace</code>.
  */
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
