package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.types.TypeNames;

/**
 * Serialized form of {@link ExpectedExceptionCheck} allowing tests to
 * be serialized.
 * 
 * Also see <code>ExpectedExceptionCheck.writeReplace</code>.
 */
public class SerializableExpectedExceptionCheck implements Serializable {

  private static final long serialVersionUID = 20100429; 

  private String exceptionClass;
  private int statementIdx;

  public SerializableExpectedExceptionCheck(Class<? extends Throwable> exceptionClass, int statementIdx) {
    this.exceptionClass = exceptionClass.getName();
    this.statementIdx = statementIdx;
  }

  private Object readResolve() throws ObjectStreamException, ClassNotFoundException {
    @SuppressWarnings("unchecked")
    Class<? extends Throwable> c = (Class<? extends Throwable>) TypeNames.getTypeForName(exceptionClass);
    return new ExpectedExceptionCheck (c, statementIdx);
  }

}
