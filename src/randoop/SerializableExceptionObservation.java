package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.util.Reflection;

public class SerializableExceptionObservation implements Serializable {

  private String exceptionClass;

  public SerializableExceptionObservation(Class<? extends Throwable> exceptionClass) {
    this.exceptionClass = exceptionClass.getName();
  }

  private Object writeReplace() throws ObjectStreamException {
    return Reflection.classForName(exceptionClass);
  }

}
