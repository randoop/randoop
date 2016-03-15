package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.types.TypeNames;

/**
 * Serializable version of {@link ArrayCreation} allowing tests to be
 * serialized.
 *
 * @see ArrayCreation#writeReplace
 */
public class SerializableArrayCreation implements Serializable {

  private static final long serialVersionUID = 4091673456327607771L;

  private final int length;
  private final String elementType;

  public SerializableArrayCreation(Class<?> elementType, int length) {
    this.elementType = elementType.getName();
    this.length = length;
  }

  private Object readResolve() throws ObjectStreamException, ClassNotFoundException {
    Class<?> c = TypeNames.getTypeForName(elementType);
    return new ArrayCreation(c, length);
  }
}
