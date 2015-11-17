package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.util.Reflection;

public class SerializableNonreceiverTerm implements Serializable {

  private static final long serialVersionUID = 6934946167409672994L;

  private final String type;
  private final Object value;

  public SerializableNonreceiverTerm(Class<?> type, Object value) {
    this.type = type.getName();
    this.value = value;
  }

  private Object readResolve() throws ObjectStreamException {
    Class<?> c;
    try {
      c = Class.forName(type);
    } catch (ClassNotFoundException e) {
      c = null;
    }
    return new NonreceiverTerm(c, value);
  }
}
