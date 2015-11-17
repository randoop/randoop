package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SerializableExpectedExceptionChecker implements Serializable {

  private static final long serialVersionUID = 20100429; 

  private String exceptionClass;
  private int statementIdx;

  public SerializableExpectedExceptionChecker(Class<? extends Throwable> exceptionClass, int statementIdx) {
    this.exceptionClass = exceptionClass.getName();
    this.statementIdx = statementIdx;
  }

  @SuppressWarnings("unchecked")
  private Object readResolve() throws ObjectStreamException {
    Class<? extends Throwable> c = null;
    try {
      c = (Class<? extends Throwable>) Class.forName(exceptionClass);
    } catch (ClassNotFoundException e) {
      //ignore
    }
    return new ExpectedExceptionCheck (c, statementIdx);
  }

}
