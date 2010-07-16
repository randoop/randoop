package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.util.Reflection;

public class SerializableArrayDeclaration implements Serializable {

  private static final long serialVersionUID = 4091673456327607771L;

  private final int length;
  private final String elementType;

  public SerializableArrayDeclaration(Class<?> elementType, int length) {
    this.elementType = elementType.getName();
    this.length = length;
  }

  private Object readResolve() throws ObjectStreamException {
    return new ArrayDeclaration(Reflection.classForName(elementType), length);
  }

}
