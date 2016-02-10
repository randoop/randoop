package randoop.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.types.TypeNames;

/**
 * Serializable form of {@link NonreceiverTerm} allowing tests to be serialized.
 * 
 * @see NonreceiverTerm#writeReplace
 */
public class SerializableNonreceiverTerm implements Serializable {

  private static final long serialVersionUID = 6934946167409672994L;

  private final String type;
  private final Object value;

  public SerializableNonreceiverTerm(Class<?> type, Object value) {
    this.type = type.getName();
    this.value = value;
  }

  private Object readResolve() throws ObjectStreamException, ClassNotFoundException {
    Class<?> c = TypeNames.getTypeForName(type);
    return new NonreceiverTerm(c, value);
  }
}
