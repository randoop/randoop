package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.util.Reflection;

public class SerializableExceptionObservation implements Serializable {

  private String exceptionClass;

  public SerializableExceptionObservation(Class<? extends Throwable> exceptionClass) {
    this.exceptionClass = exceptionClass.getName();
  }

  private Object readResolve() throws ObjectStreamException {
    Class<?> c = Reflection.classForName(exceptionClass);
    return new StatementThrowsException (c);
  }

}
