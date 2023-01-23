package randoop.instrument;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import randoop.ExecutionVisitor;
import randoop.sequence.ExecutableSequence;

/**
 * A {@link ExecutionVisitor} that polls a set of coverage instrumented classes and adds each
 * covered class to an {@link ExecutableSequence} after it is executed.
 */
public class CoveredClassVisitor implements ExecutionVisitor {

  /** The classes to be polled. */
  private Set<Class<?>> classes;

  /**
   * Creates a visitor to poll the given classes for coverage by sequence executions.
   *
   * @param classes the set of classes to poll for coverage by a sequence
   */
  public CoveredClassVisitor(Set<Class<?>> classes) {
    this.classes = classes;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Registers each class covered with the sequence execution results.
   */
  @Override
  public void visitAfterSequence(ExecutableSequence eseq) {
    for (Class<?> c : classes) {
      if (checkAndReset(c)) {
        eseq.addCoveredClass(c);
      }
    }
  }

  /**
   * Calls the coverage instrumentation method.
   *
   * @param c the class for which method is to be called
   * @return true if the instrumentation method is true, false otherwise
   */
  private boolean checkAndReset(Class<?> c) {
    try {
      Method m = c.getMethod("randoop_checkAndReset");
      m.setAccessible(true);
      return (boolean) m.invoke(null);
    } catch (NoSuchMethodException e) {
      throw new Error("Cannot find instrumentation method: " + e);
    } catch (SecurityException e) {
      throw new Error("Security error when accessing instrumentation method: " + e);
    } catch (IllegalAccessException e) {
      throw new Error("Cannot access instrumentation method: " + e);
    } catch (IllegalArgumentException e) {
      throw new Error("Bad argument to instrumentation method: " + e);
    } catch (InvocationTargetException e) {
      throw new Error("Bad invocation of instrumentation method: " + e);
    }
  }

  // unimplemented visitor methods
  @Override
  public void visitBeforeStatement(ExecutableSequence eseq, int i) {
    // Not doing anything before
  }

  @Override
  public void visitAfterStatement(ExecutableSequence eseq, int i) {
    // Not doing anything after
  }

  @Override
  public void initialize(ExecutableSequence eseq) {
    // No initialization
  }
}
