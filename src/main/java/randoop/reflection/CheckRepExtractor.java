package randoop.reflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import randoop.CheckRep;
import randoop.contract.CheckRepContract;
import randoop.main.GenInputsAbstract;
import randoop.test.ContractSet;

/**
 * {@code CheckRepExtractor} is a {@link ClassVisitor} that inspects the methods passed to it to see
 * if they are annotated with {@link randoop.CheckRep}, are non-static, and have either a {@code
 * boolean} or {@code void} return type.
 */
class CheckRepExtractor extends DefaultClassVisitor {

  /** The set of accumulated {@link randoop.contract.ObjectContract} objects. */
  private ContractSet contracts;

  /**
   * Creates a visitor with an empty contracts set.
   *
   * @param contracts the set of contracts
   */
  CheckRepExtractor(ContractSet contracts) {
    this.contracts = contracts;
  }

  /**
   * {@inheritDoc}
   *
   * <p>If the method is annotated with {@link randoop.CheckRep}, non-static, and with boolean or
   * void return type, then a {@link randoop.contract.CheckRepContract} for the method is added to
   * the contracts set.
   *
   * @param m the method
   */
  @Override
  public void visit(Method m) {
    if (m.getAnnotation(CheckRep.class) != null) {
      if (Modifier.isStatic(m.getModifiers())) {
        String msg =
            "RANDOOP ANNOTATION ERROR: Expected @CheckRep-annotated method "
                + m.getName()
                + " in class "
                + m.getDeclaringClass()
                + " to be an instance method, but it is declared static.";
        throw new RuntimeException(msg);
      }

      if (m.getParameterTypes().length > 0) {
        String msg =
            "RANDOOP ANNOTATION ERROR: Expected @CheckRep-annotated method "
                + m.getName()
                + " in class "
                + m.getDeclaringClass()
                + " to declare no parameters but it does (method signature:"
                + m.toString()
                + ").";
        throw new RuntimeException(msg);
      }

      // Check that method's return type is void.
      if (!(m.getReturnType().equals(boolean.class) || m.getReturnType().equals(void.class))) {
        String msg =
            "RANDOOP ANNOTATION ERROR: Expected @CheckRep-annotated method "
                + m.getName()
                + " in class "
                + m.getDeclaringClass()
                + " to have void or boolean return type but it does not (method signature:"
                + m.toString()
                + ").";
        throw new RuntimeException(msg);
      }

      if (GenInputsAbstract.progressdisplay) {
        printDetectedAnnotatedCheckRepMethod(m);
      }
      contracts.add(new CheckRepContract(m));
    }
  }

  /**
   * Prints the log message indicating that a CheckRep method has been found.
   *
   * @param m the method
   */
  private static void printDetectedAnnotatedCheckRepMethod(Method m) {
    String msg =
        "ANNOTATION: Detected @CheckRep-annotated method \""
            + m.toString()
            + "\". Will use it to check rep invariant of class "
            + m.getDeclaringClass().getCanonicalName()
            + " during generation.";
    System.out.println(msg);
  }
}
