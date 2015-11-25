package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.types.TypeNames;

/**
 * Serialized form of {@link ExpectedExceptionChecker} allowing tests to
 * be serialized.
 * 
 * Also see <code>ExpectedExceptionChecker.writeReplace</code>.
 */
public class SerializableExpectedExceptionChecker implements Serializable {

  private static final long serialVersionUID = 20100429; 

  private String exceptionClass;
  private int statementIdx;

  public SerializableExpectedExceptionChecker(Class<? extends Throwable> exceptionClass, int statementIdx) {
    this.exceptionClass = exceptionClass.getName();
    this.statementIdx = statementIdx;
  }

  @SuppressWarnings("unchecked")
  private Object readResolve() throws ObjectStreamException, ClassNotFoundException {
    Class<? extends Throwable> c = (Class<? extends Throwable>) TypeNames.getTypeForName(exceptionClass);
    return new ExpectedExceptionCheck (c, statementIdx);
  }

}
