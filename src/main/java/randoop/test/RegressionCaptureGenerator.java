package randoop.test;

import java.util.Set;
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
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.reflection.VisibilityPredicate;
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
 *   <li>Only creates checks over variables whose type is primitive or String.
 *   <li>Does not create checks for Strings that contain the string ";@" as this is a good
 *       indication that at least part of the String came from a call of Object.toString() (e.g.
 *       "[[Ljava.lang.Object;@5780d9]" is the string representation of a list containing one
 *       Object).
 * </ul>
 */
public final class RegressionCaptureGenerator extends TestCheckGenerator {

  /** The generator for expected exceptions. */
  private ExpectedExceptionCheckGen exceptionExpectation;

  /** The map from a type to the observer operations for the type. */
  private MultiMap<Type, TypedOperation> observerMap;

  /** The visibility predicate. */
  private final VisibilityPredicate isVisible;

  /** The flag whether to include regression assertions. */
  private boolean includeAssertions;

  public RegressionCaptureGenerator(
      ExpectedExceptionCheckGen exceptionExpectation,
      MultiMap<Type, TypedOperation> observerMap,
      VisibilityPredicate isVisible,
      boolean includeAssertions) {
    this.exceptionExpectation = exceptionExpectation;
    this.observerMap = observerMap;
    this.isVisible = isVisible;
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
  public RegressionChecks generateTestChecks(ExecutableSequence sequence) {

    RegressionChecks checks = new RegressionChecks();

    int finalIndex = sequence.sequence.size() - 1;

    // Capture checks for each value created.
    // Recall there are as many values as statements in the sequence.
    for (int i = 0; i < sequence.sequence.size(); i++) {

      Statement statement = sequence.sequence.getStatement(i);
      ExecutionOutcome result = sequence.getResult(i);
      if (result instanceof NotExecuted) {
        throw new Error("Abnormal execution in sequence: " + sequence);
      } else if (result instanceof NormalExecution) {
        if (includeAssertions) {
          NormalExecution execution = (NormalExecution) result;
          // If value is like x in "int x = 3" don't capture
          // checks (nothing interesting).
          if (statement.isNonreceivingInitialization()) {
            continue;
          }

          // If value's type is void (i.e. its statement is a void-return method call),
          // don't capture checks (nothing interesting).
          Type outputType = statement.getOutputType();
          if (outputType.isVoid()) continue; // no return value.

          Object runtimeValue = execution.getRuntimeValue();

          Variable var = sequence.sequence.getVariable(i);

          if (runtimeValue == null) {

            // Add observer test for null
            checks.add(new ObjectCheck(new IsNull(), var));

          } else if (PrimitiveTypes.isBoxedPrimitive(runtimeValue.getClass())
              || runtimeValue.getClass().equals(String.class)) {

            if (runtimeValue instanceof String) {
              // System.out.printf("considering String check for seq %08X%n",
              // s.seq_id());
              String str = (String) runtimeValue;
              // Don't create assertions over strings that look like raw object
              // references.
              if (Value.looksLikeObjectToString(str)) {
                // System.out.printf("ignoring Object.toString obs %s%n", str);
                continue;
              }
              // Don't create assertions over strings that are really
              // long, as this can cause the generate unit tests to be
              // unreadable and/or non-compilable due to Java
              // restrictions on String constants.
              if (!Value.stringLengthOK(str)) {
                Log.logPrintf(
                    "Ignoring a string that exceeds the maximum length of %d%n",
                    GenInputsAbstract.string_maxlen);
                continue;
              }
            }

            // If the value is returned from a Date that we created,
            // don't use it as it's just going to have today's date in it.
            if (!sequence.sequence.getInputs(i).isEmpty()) {
              Variable var0 = sequence.sequence.getInputs(i).get(0);
              if (var0.getType().runtimeClassIs(java.util.Date.class)) {
                Statement sk = sequence.sequence.getCreatingStatement(var0);
                if (sk.isConstructorCall() && sequence.sequence.getInputs(i).size() == 1) {
                  continue;
                }
                // System.out.printf ("var type %s comes from date %s / %s%n",
                // s.sequence.getVariable(i).getType(),
                // s.sequence.getOperation(i), sk);
              }
            }

            // Add observer test for the primitive
            PrimValue.PrintMode printMode;
            if (var.getType().isPrimitive()) {
              printMode = PrimValue.PrintMode.EQUALSEQUALS;
            } else {
              printMode = PrimValue.PrintMode.EQUALSMETHOD;
            }
            ObjectCheck oc = new ObjectCheck(new PrimValue(runtimeValue, printMode), var);
            checks.add(oc);
            // System.out.printf("Adding objectcheck %s to seq %08X%n",
            // oc, s.seq_id());

          } else if (runtimeValue.getClass().isEnum()
              && isVisible.isVisible(runtimeValue.getClass())) {
            // XXX Not clear why the visibility check is necessary
            ObjectCheck oc = new ObjectCheck(new EnumValue((Enum<?>) runtimeValue), var);
            checks.add(oc);
          } else { // its a more complex type with a non-null value

            // Assert that the value is not null.
            // Exception: if the value comes directly from a constructor call,
            // not interesting that it's non-null; omit the check.
            if (!statement.isConstructorCall()) {
              checks.add(new ObjectCheck(new IsNotNull(), var));
            }

            // Put out any observers that exist for this type
            Variable var0 = sequence.sequence.getVariable(i);
            Set<TypedOperation> observers = observerMap.getValues(var0.getType());
            if (observers != null) {
              for (TypedOperation m : observers) {

                // When outputting checks, ignore observers that don't take a single argument.
                if (m.getInputTypes().size() != 1) {
                  continue;
                }

                ExecutionOutcome outcome = m.execute(new Object[] {runtimeValue});
                if (outcome instanceof ExceptionalExecution) {
                  String msg =
                      "unexpected error invoking observer "
                          + m
                          + " on "
                          + var
                          + "["
                          + var.getType()
                          + "]"
                          + " with value "
                          + runtimeValue
                          + " ["
                          + runtimeValue.getClass()
                          + "]";
                  throw new RuntimeException(msg, ((ExceptionalExecution) outcome).getException());
                }

                Object value = ((NormalExecution) outcome).getRuntimeValue();

                // Don't create assertions over string that look like raw object
                // references.
                if ((value instanceof String) && Value.looksLikeObjectToString((String) value)) {
                  continue;
                }

                ObjectContract observerEqValue = new ObserverEqValue(m, value);
                ObjectCheck observerCheck = new ObjectCheck(observerEqValue, var);

                Log.logPrintf("Adding observer %s%n", observerCheck);

                checks.add(observerCheck);
              }
            }
          }
        }
      } else if (result instanceof ExceptionalExecution) {
        // The code threw an exception

        // if happens before last statement, sequence is malformed
        if (i != finalIndex) {
          throw new Error("Exception thrown before end of sequence");
        }

        // Otherwise, add the check determined by exceptionExpectation
        ExceptionalExecution e = (ExceptionalExecution) result;
        checks.add(exceptionExpectation.getExceptionCheck(e, sequence, i));

      } else { // statement not executed
        throw new Error("Unexecuted statement in sequence");
      }
    }
    return checks;
  }
}
