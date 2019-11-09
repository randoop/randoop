package randoop.test;

import java.util.Objects;
import randoop.Globals;

/**
 * An {@code ExceptionCheck} indicates that an exception is expected at a particular statement in a
 * sequence.
 *
 * <p>When test code is generated in {@link randoop.sequence.ExecutableSequence#toCodeString()}, the
 * methods {@link #toCodeStringPreStatement()} and {@link #toCodeStringPostStatement()} wrap the
 * statement in a try-catch block for the exception, while the implementing classes define {@link
 * #appendTryBehavior(StringBuilder)} and {@link #appendCatchBehavior(StringBuilder)} which handle
 * differences in whether assertions are generated to enforce the expectation of the exception.
 */
public abstract class ExceptionCheck implements Check {

  /** The thrown exception. */
  protected final Throwable exception;

  /** Indicates which statement results in the given exception. */
  final int statementIndex;

  /**
   * The name of exception to be caught. This might be a supertype of {@code exception}'s class C,
   * for instance because C is private or because the method is declared to throw a superclass of C.
   */
  private String catchClassName;

  /**
   * Creates an exception check for the statement at the statement index. The generated code for
   * this check will include a try-catch block with behaviors determined by implementing
   * sub-classes.
   *
   * @param exception the exception expected at the statement index
   * @param statementIndex the position of the statement in a sequence
   * @param catchClassName the name of exception to be caught
   */
  public ExceptionCheck(Throwable exception, int statementIndex, String catchClassName) {
    this.exception = exception;
    this.statementIndex = statementIndex;
    this.catchClassName = catchClassName;
  }

  /**
   * Determines if two {@code ExceptionCheck} objects are equal. Assumes that implementing classes
   * have no state.
   */
  @Override
  @SuppressWarnings("EqualsGetClass")
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (this.getClass() != o.getClass()) { // match implementing class
      return false;
    }
    ExceptionCheck other = (ExceptionCheck) o;
    return this.exception.equals(other.exception) && statementIndex == other.statementIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(exception, statementIndex);
  }

  @Override
  public String toString() {
    return "// throws exception of type "
        + exception.getClass().getCanonicalName()
        + Globals.lineSep;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The pre-statement prefix of the try-catch wrapper.
   */
  @Override
  public final String toCodeStringPreStatement() {
    return "// The following exception was thrown during execution in test generation"
        + Globals.lineSep
        + "try {"
        + Globals.lineSep;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the post-statement portion of the try-catch wrapper. Starts with post-statement
   * try-behavior as determined by a subclass implementation of {@link #appendTryBehavior}, and then
   * closes with the catch clause with the body determined by the sub-class implementation of {@link
   * #appendCatchBehavior(StringBuilder)}. Catches this exception or the closest public superclass
   * of the exception.
   *
   * @return the post-statement code text for the expected exception
   */
  @Override
  public final String toCodeStringPostStatement() {
    StringBuilder b = new StringBuilder();
    if (catchClassName == null) {
      catchClassName = "Exception";
    }
    appendTryBehavior(b);
    b.append("} catch (").append(catchClassName).append(" e) {").append(Globals.lineSep);
    b.append("  // Expected exception.").append(Globals.lineSep);
    appendCatchBehavior(b);
    b.append("}").append(Globals.lineSep);
    return b.toString();
  }

  /**
   * Appends code for catch block behavior corresponding to expected exception.
   *
   * @param b the string builder to which code text is to be added
   */
  protected abstract void appendCatchBehavior(StringBuilder b);

  /**
   * Appends code to follow the statement throwing expected exception in try block.
   *
   * @param b the string builder to which code text is added
   */
  protected abstract void appendTryBehavior(StringBuilder b);

  /**
   * Returns the name of the exception class.
   *
   * @return the canonical name of the exception class
   */
  public String getExceptionName() {
    return exception.getClass().getCanonicalName();
  }

  /**
   * Returns the exception.
   *
   * @return the exception in this check
   */
  public Throwable getException() {
    return exception;
  }
}
