package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

import randoop.util.Reflection;

public class SerializableCheckRepContract implements Serializable {

  private static final long serialVersionUID = -7030830526035671668L;
  private final String checkRepMethod;

  public SerializableCheckRepContract(Method checkRepMethod) {
    this.checkRepMethod = Reflection.getSignature(checkRepMethod);
  }

  private Object readResolve() throws ObjectStreamException {
    Method m = Reflection.getMethodForSignature(checkRepMethod);
    return new CheckRepContract(m);
  }
}
