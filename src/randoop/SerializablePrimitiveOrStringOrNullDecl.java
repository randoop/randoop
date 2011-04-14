package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.util.Reflection;

public class SerializablePrimitiveOrStringOrNullDecl implements Serializable {

  private static final long serialVersionUID = 6934946167409672994L;

  private final String type;
  private final Object value;

  public SerializablePrimitiveOrStringOrNullDecl(Class<?> type, Object value) {
    this.type = type.getName();
    this.value = value;
  }

  private Object readResolve() throws ObjectStreamException {
    return new PrimitiveOrStringOrNullDecl(Reflection.classForName(type), value);
  }
}
