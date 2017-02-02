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
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;
import randoop.sequence.Value;
import randoop.sequence.Variable;
import randoop.types.Type;
import randoop.types.PrimitiveTypes;
import randoop.util.Log;
import randoop.util.MultiMap;

/**
 * An {@code ExecutionVisitor} that records regression checks on the values
 * created by the sequence.
 *
 * NOTES:
 *
 * <ul>
 * <li>Only creates checks over variables whose type is primitive or String.
 * <li>Does not create checks for the return values of Object.toString() and
 * Object.hashCode() as their values can vary from run to run.
 * <li>Does not create checks for Strings that contain the string ";@" as this
 * is a good indication that at least part of the String came from a call of
 * Object.toString() (e.g. "[[Ljava.lang.Object;@5780d9]" is the string
 * representation of a list containing one Object).
 * </ul>
 */
public final class RegressionCaptureVisitor implements TestCheckGenerator {

  private ExpectedExceptionCheckGen exceptionExpectation;
  private MultiMap<Type, TypedOperation> observerMap;
  private final Set<TypedOperation> excludeSet;
  private boolean includeAssertions;

  public RegressionCaptureVisitor(
      ExpectedExceptionCheckGen exceptionExpectation,
      MultiMap<Type, TypedOperation> observerMap,
      Set<TypedOperation> excludeSet,
      boolean includeAssertions) {
    this.exceptionExpectation = exceptionExpectation;
    this.observerMap = observerMap;
    this.excludeSet = excludeSet;
    this.includeAssertions = includeAssertions;
  }

  /**
   * {@inheritDoc} Iterates over all statements of the sequence to create
   * regression assertions. If visitor is set to include assertions, then
   * assertions are generated for both normal execution and exceptions. A
   * try-catch block is always generated for exceptions, but whether assertions
   * are included is determined by the {@link ExpectedExceptionCheckGen} given
   * when creating this visitor.
   *
   * @throws Error
   *           if any statement is not executed, or exception occurs before last
   *           statement
   */
  @Override
  public TestChecks visit(ExecutableSequence s) {

    RegressionChecks checks = new RegressionChecks();

    int finalIndex = s.sequence.size() - 1;

    // Capture checks for each value created.
    // Recall there are as many values as statements in the sequence.
    for (int i = 0; i < s.sequence.size(); i++) {

      Statement st = s.sequence.getStatement(i);
      ExecutionOutcome result = s.getResult(i);
      if (result instanceof NotExecuted) {
        throw new Error("Abnormal execution in sequence: " + s);
      } else if (result instanceof NormalExecution) {
        if (includeAssertions) {
          NormalExecution e = (NormalExecution) result;
          // If value is like x in "int x = 3" don't capture
          // checks (nothing interesting).
          if (st.isPrimitiveInitialization()) {
            continue;
          }

          // If value's type is void (i.e. its statement is a
          // void-return method call), don't capture checks
          // (nothing interesting).
          Type tc = st.getOutputType();
          if (tc.isVoid()) continue; // no return value.

          // If value is the result of Object.toString() or
          // Object.hashCode(), don't capture checks (value is
          // likely to be non-deterministic across runs).
          if (excludeSet.contains(st.getOperation())) {
            continue;
          }

          Object o = e.getRuntimeValue();

          Variable var = s.sequence.getVariable(i);

          if (o == null) {

            // Add observer test for null
            checks.add(new ObjectCheck(new IsNull(), var));

          } else if (PrimitiveTypes.isBoxedPrimitive(o.getClass())
              || (o.getClass().equals(String.class))) {

            if (o instanceof String) {
              // System.out.printf ("considering String check for seq %08X\n",
              // s.seq_id());
              String str = (String) o;
              // Don't create assertions over strings that look like raw object
              // references.
              if (Value.looksLikeObjectToString(str)) {
                // System.out.printf ("ignoring Object.toString obs %s%n", str);
                continue;
              }
              // Don't create assertions over strings that are really
              // long, as this can cause the generate unit tests to be
              // unreadable and/or non-compilable due to Java
              // restrictions on String constants.
              if (!Value.stringLengthOK(str)) {
                if (Log.isLoggingOn()) {
                  Log.logLine(
                      "Ignoring a string that exceeds the maximum length of "
                          + GenInputsAbstract.string_maxlen);
                }
                continue;
              }
            }

            // If the value is returned from a Date that we created,
            // don't use it as it's just going to have today's date in it.
            if (!s.sequence.getInputs(i).isEmpty()) {
              Variable var0 = s.sequence.getInputs(i).get(0);
              if (var0.getType().hasRuntimeClass(java.util.Date.class)) {
                Statement sk = s.sequence.getCreatingStatement(var0);
                if ((sk.isConstructorCall()) && (s.sequence.getInputs(i).size() == 1)) continue;
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
            ObjectCheck oc = new ObjectCheck(new PrimValue(o, printMode), var);
            checks.add(oc);
            // System.out.printf ("Adding objectcheck %s to seq %08X\n",
            // oc, s.seq_id());

          } else if (o.getClass().isEnum()) {
            ObjectCheck oc = new ObjectCheck(new EnumValue((Enum<?>) o), var);
            checks.add(oc);
          } else { // its a more complex type with a non-null value

            // Assert that the value is not null.
            // Exception: if the value comes directly from a constructor call,
            // not interesting that it's non-null; omit the check.
            if (!(st.isConstructorCall())) {
              checks.add(new ObjectCheck(new IsNotNull(), var));
            }

            // Put out any observers that exist for this type
            Variable var0 = s.sequence.getVariable(i);
            Set<TypedOperation> observers = observerMap.getValues(var0.getType());
            if (observers != null) {
              for (TypedOperation m : observers) {

                ExecutionOutcome outcome = m.execute(new Object[] {o}, null);
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
                          + o
                          + " ["
                          + o.getClass()
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

                if (Log.isLoggingOn()) {
                  Log.logLine(String.format("Adding observer %s%n", observerCheck));
                }

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
        checks.add(exceptionExpectation.getExceptionCheck(e, s, i));

      } else { // statement not executed
        throw new Error("Unexecuted statement in sequence");
      }
    }
    return checks;
  }
}
