package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.util.Reflection;

public class SerializableExpectedExceptionChecker implements Serializable {

  private static final long serialVersionUID = 20100429; 

  private String exceptionClass;
  private int statementIdx;

  public SerializableExpectedExceptionChecker(Class<? extends Throwable> exceptionClass, int statementIdx) {
    this.exceptionClass = exceptionClass.getName();
    this.statementIdx = statementIdx;
  }

  private Object readResolve() throws ObjectStreamException {
    @SuppressWarnings("unchecked")
    Class<? extends Throwable> c = (Class<? extends Throwable>) Reflection.classForName(exceptionClass);
    return new ExpectedExceptionCheck (c, statementIdx);
  }

}
