package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.util.Reflection;

public class SerializableExceptionObservation implements Serializable {

  private static final long serialVersionUID = 20100429; 

  private String exceptionClass;

  public SerializableExceptionObservation(Class<? extends Throwable> exceptionClass) {
    this.exceptionClass = exceptionClass.getName();
  }

  private Object readResolve() throws ObjectStreamException {
    @SuppressWarnings("unchecked")
    Class<? extends Throwable> c = (Class<? extends Throwable>) Reflection.classForName(exceptionClass);
    return new StatementThrowsException (c);
  }

}
