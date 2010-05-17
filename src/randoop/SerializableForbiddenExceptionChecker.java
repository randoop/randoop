package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.util.Reflection;

public class SerializableForbiddenExceptionChecker implements Serializable {

  private static final long serialVersionUID = 20100429; 

  private String exceptionClass;

  public SerializableForbiddenExceptionChecker(Class<? extends Throwable> exceptionClass) {
    this.exceptionClass = exceptionClass.getName();
  }

  private Object readResolve() throws ObjectStreamException {
    @SuppressWarnings("unchecked")
    Class<? extends Throwable> c = (Class<? extends Throwable>) Reflection.classForName(exceptionClass);
    return new ForbiddenExceptionChecker (c);
  }

}
