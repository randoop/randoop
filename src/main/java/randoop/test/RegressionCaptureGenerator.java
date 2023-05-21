package randoop.test;

import static randoop.contract.PrimValue.EqualityMode.EQUALSEQUALS;
import static randoop.contract.PrimValue.EqualityMode.EQUALSMETHOD;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import org.plumelib.util.StringsPlume;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.contract.EnumValue;
import randoop.contract.IsNotNull;
import randoop.contract.IsNull;
import randoop.contract.ObjectContract;
import randoop.contract.ObserverEqValue;
import randoop.contract.PrimValue;
import randoop.operation.TypedClassOperation;
import randoop.reflection.AccessibilityPredicate;
import randoop.reflection.OmitMethodsPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;
import randoop.sequence.Value;
import randoop.sequence.Variable;
import randoop.types.PrimitiveTypes;
import randoop.types.Type;
import randoop.util.Log;
import randoop.util.MultiMap;

/**
 * A {@code TestCheckGenerator} that records regression checks on the values created by the
 * sequence.
 *
 * <p>NOTES:
 *
 * <ul>
 *   <li>Only creates checks over values whose type is primitive or String.
 *   <li>Does not create checks for Strings that contain the string ";@" as this is a good
 *       indication that at least part of the String came from a call of Object.toString() (e.g.
 *       "[[Ljava.lang.Object;@5780d9]" is the string representation of a list containing one
 *       Object).
 * </ul>
 */
public final class RegressionCaptureGenerator extends TestCheckGenerator {

  /** The generator for expected exceptions. */
  private ExpectedExceptionCheckGen exceptionExpectation;

  /** The map from a type to the set of side-effect-free operations for the type. */
  private MultiMap<Type, TypedClassOperation> sideEffectFreeMethodsByType;

  /** The accessibility predicate. */
  private final AccessibilityPredicate isAccessible;

  /** The user-supplied predicate for methods that should not be called. */
  private OmitMethodsPredicate omitMethodsPredicate;

  /**
   * Whether to include regression assertions. If false, no assertions are added for sequences whose
   * execution is NormalExecution.
   */
  private boolean includeAssertions;

  /**
   * Create a RegressionCaptureGenerator.
   *
   * @param exceptionExpectation the generator for expected exceptions
   * @param sideEffectFreeMethodsByType the map from a type to the side-effect-free operations for
   *     the type
   * @param isAccessible the accessibility predicate
   * @param omitMethodsPredicate the user-supplied predicate for methods that should not be called
   * @param includeAssertions whether to include regression assertions
   */
  public RegressionCaptureGenerator(
      ExpectedExceptionCheckGen exceptionExpectation,
      MultiMap<Type, TypedClassOperation> sideEffectFreeMethodsByType,
      AccessibilityPredicate isAccessible,
      OmitMethodsPredicate omitMethodsPredicate,
      boolean includeAssertions) {
    this.exceptionExpectation = exceptionExpectation;
    this.sideEffectFreeMethodsByType = sideEffectFreeMethodsByType;
    this.isAccessible = isAccessible;
    this.omitMethodsPredicate = omitMethodsPredicate;
    this.includeAssertions = includeAssertions;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Iterates over all statements of the sequence to create regression assertions. If visitor is
   * set to include assertions, then assertions are generated for both normal execution and
   * exceptions. A try-catch block is always generated for exceptions, but whether assertions are
   * included is determined by the {@link ExpectedExceptionCheckGen} given when creating this
   * visitor.
   *
   * @throws Error if any statement is not executed, or exception occurs before last statement
   */
  @Override
  public RegressionChecks generateTestChecks(ExecutableSequence eseq) {

    RegressionChecks checks = new RegressionChecks();

    int finalIndex = eseq.sequence.size() - 1;

    // Capture checks for each value created/returned by a statement.
    // Does not currently capture checks for values side-effected by a statement.
    for (int i = 0; i < eseq.sequence.size(); i++) {

      Statement statement = eseq.sequence.getStatement(i);
      ExecutionOutcome result = eseq.getResult(i);
      if (result instanceof NotExecuted) {
        throw new Error("Abnormal execution in sequence: " + eseq);
      } else if (result instanceof NormalExecution) {
        if (includeAssertions) {
          NormalExecution execution = (NormalExecution) result;
          // If value is like x in "int x = 3" don't capture checks.  There is nothing interesting
          // to assert, and the statement might not even appear in the output because the RHS might
          // get inlined at uses.
          if (statement.isNonreceivingInitialization()) {
            continue;
          }

          // If value's type is void (i.e. its statement is a void-return method call),
          // don't capture checks (nothing interesting).
          Type outputType = statement.getOutputType();
          if (outputType.isVoid()) {
            continue;
          }

          Object runtimeValue = execution.getRuntimeValue();

          Variable var = eseq.sequence.getVariable(i);

          if (runtimeValue == null) {
            checks.add(new ObjectCheck(new IsNull(), var));
          } else if (PrimitiveTypes.isBoxedPrimitive(runtimeValue.getClass())
              || runtimeValue.getClass().equals(String.class)) {
            if (Value.isUnassertableString(runtimeValue)) {
              continue;
            }
            // System.out.printf("Adding objectcheck %s to seq %08X%n", poc, s.seq_id());
            PrimValue.EqualityMode equalityMode =
                var.getType().isPrimitive() ? EQUALSEQUALS : EQUALSMETHOD;
            ObjectCheck oc = new ObjectCheck(new PrimValue(runtimeValue, equalityMode), var);
            checks.add(oc);
          } else if (runtimeValue.getClass().isEnum()
              // The assertion will be "foo == EnumClass.ENUM" and the rhs must be accessible.
              && isAccessible.isAccessible(runtimeValue.getClass())) {
            ObjectCheck oc = new ObjectCheck(new EnumValue((Enum<?>) runtimeValue), var);
            checks.add(oc);
          } else { // It's a more complex type with a non-null value.

            // Assert that the value is not null.
            // Exception: if the value comes directly from a constructor call,
            // not interesting that it's non-null; omit the check.
            if (!statement.isConstructorCall()) {
              checks.add(new ObjectCheck(new IsNotNull(), var));
            }

            // Put out any side-effect-free methods that exist for this type.
            Variable var0 = eseq.sequence.getVariable(i);
            Set<TypedClassOperation> sideEffectFreeMethods =
                sideEffectFreeMethodsByType.getValues(var0.getType());
            if (sideEffectFreeMethods != null) {
              for (TypedClassOperation m : sideEffectFreeMethods) {
                if (!isAssertableMethod(m, omitMethodsPredicate, isAccessible)) {
                  continue;
                }

                // Avoid making a call that will fail looksLikeObjectToString.
                if (isObjectToString(m) && runtimeValue.getClass() == Object.class) {
                  continue;
                }

                ExecutionOutcome outcome = m.execute(new Object[] {runtimeValue});
                if (outcome instanceof ExceptionalExecution) {
                  // The program under test threw an exception.  Don't call this method in the test.
                  continue;
                }

                Object value = ((NormalExecution) outcome).getRuntimeValue();

                if (Value.isUnassertableString(value)) {
                  continue;
                }

                ObjectContract observerEqValue = new ObserverEqValue(m, value);
                ObjectCheck observerCheck = new ObjectCheck(observerEqValue, var);
                Log.logPrintf("Adding observer check %s%n", observerCheck);
                checks.add(observerCheck);
              }
            }
          }
        }
      } else if (result instanceof ExceptionalExecution) {
        // The code threw an exception.

        // if happens before last statement, sequence is malformed
        if (i != finalIndex) {
          throw new Error("Exception thrown before end of sequence");
        }

        // Otherwise, add the check determined by exceptionExpectation
        ExceptionalExecution e = (ExceptionalExecution) result;
        checks.add(exceptionExpectation.getExceptionCheck(e, eseq, i));

      } else { // statement not executed
        throw new Error("Unexpected result type: " + StringsPlume.toStringAndClass(result));
      }
    }
    return checks;
  }

  /**
   * Return true if the method is Object.toString (which is nondeterministic for classes that have
   * not overridden it).
   *
   * @param m the method to test
   * @return true if the method is Object.toString
   */
  private static boolean isObjectToString(TypedClassOperation m) {
    Class<?> declaringClass = m.getDeclaringType().getRuntimeClass();
    if (declaringClass == Object.class || declaringClass == Objects.class) {
      return m.getUnqualifiedBinaryName().equals("toString");
    }
    if (declaringClass == String.class) {
      return m.getUnqualifiedBinaryName().equals("valueOf");
    }
    return false;
  }

  /**
   * Returns true if the given side-effect-free method or constructor can be used in an assertion in
   * Randoop.
   *
   * @param m a method or constructor, which must be side-effect-free
   * @param omitMethodsPredicate the user-supplied predicate for methods and constructors that
   *     should not be called
   * @param accessibility the predicate used to check whether a method or constructor is accessible
   *     to call
   * @return whether we can use this method or constructor in a side-effect-free assertion
   * @throws IllegalArgumentException if m is not either a Method or a Constructor
   */
  public static boolean isAssertableMethod(
      TypedClassOperation m,
      OmitMethodsPredicate omitMethodsPredicate,
      AccessibilityPredicate accessibility) {

    if (omitMethodsPredicate.shouldOmit(m)) {
      return false;
    }

    AccessibleObject executable = m.getOperation().getReflectionObject();
    if (executable instanceof Method) {
      if (!accessibility.isAccessible((Method) executable)) {
        return false;
      }
    } else if (executable instanceof Constructor) {
      if (!accessibility.isAccessible((Constructor) executable)) {
        return false;
      }
    } else {
      throw new IllegalArgumentException(
          "TypedClassOperation " + m + " must be either of type Constructor or Method.");
    }

    // Must have a single formal parameter.
    if (m.getInputTypes().size() != 1) {
      return false;
    }

    // Must return non-void.
    if (m.getOutputType().isVoid()) {
      return false;
    }
    Class<?> outputClass = m.getOutputType().getRuntimeClass();
    // Ignore the null reference type.
    if (outputClass == null) {
      return false;
    }
    // Don't create assertions over types that are not primitives,  strings, or enums.
    if (!PrimitiveTypes.isBoxedPrimitive(outputClass)
        && !outputClass.equals(String.class)
        && !outputClass.isEnum()) {
      return false;
    }

    return true;
  }
}
