package randoop.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.IsNotNull;
import randoop.IsNull;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.ObjectCheck;
import randoop.ObjectContract;
import randoop.ObserverEqValue;
import randoop.PrimValue;
import randoop.main.GenInputsAbstract;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.TypeNames;
import randoop.util.Files;
import randoop.util.Log;
import randoop.util.PrimitiveTypes;
import randoop.util.Reflection;

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
  private boolean includeAssertions;

  public RegressionCaptureVisitor(
      ExpectedExceptionCheckGen exceptionExpectation, boolean includeAssertions) {
    this.exceptionExpectation = exceptionExpectation;
    this.includeAssertions = includeAssertions;
  }

  // We don't create regression checks for these methods.
  private static final Method objectToString;
  private static final Method objectHashCode;

  static {
    try {
      objectToString = Object.class.getDeclaredMethod("toString");
      objectHashCode = Object.class.getDeclaredMethod("hashCode");
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  /** Map from each class to the list of observer methods for that class */
  private static final Map<Class<?>, List<Method>> observer_map =
      new LinkedHashMap<Class<?>, List<Method>>();

  public static boolean isObserverInvocation(Statement statement) {
    if (observer_map.containsKey(statement.getDeclaringClass())) {
      return statement.isMethodIn(
          RegressionCaptureVisitor.observer_map.get(statement.getDeclaringClass()));
    }
    return false;
  }

  // TODO move this out of class
  // Populate observer_map from observers file.
  static {
    if (GenInputsAbstract.observers != null) {
      List<String> lines = null;
      try {
        lines = Files.readWhole(GenInputsAbstract.observers);
      } catch (Exception e) {
        throw new RuntimeException(
            "problem reading observer file " + GenInputsAbstract.observers, e);
      }
      for (String line : lines) {
        if (line.startsWith("//")) continue;
        if (line.trim().length() == 0) continue;
        int lastdot = line.lastIndexOf(".");
        if (lastdot == -1) throw new RuntimeException(String.format("invalid observer '%s'", line));
        String classname = line.substring(0, lastdot);
        String methodname = line.substring(lastdot + 1);
        methodname = methodname.replaceFirst("[()]*$", "");
        Class<?> obs_class = null;
        try {
          obs_class = TypeNames.getTypeForName(classname);
        } catch (Exception e) {
          throw new RuntimeException("Can't load observer class " + classname, e);
        }
        Method obs_method = null;
        try {
          obs_method = Reflection.super_get_declared_method(obs_class, methodname);
        } catch (Exception e) {
          throw new RuntimeException("Can't find observer method " + methodname, e);
        }
        if (!PrimitiveTypes.isPrimitiveOrStringType(obs_method.getReturnType()))
          throw new RuntimeException(
              String.format(
                  "Observer method %s does not return a primitive " + "or string", obs_method));
        List<Method> methods = observer_map.get(obs_class);
        if (methods == null) {
          methods = new ArrayList<Method>();
          observer_map.put(obs_class, methods);
        }
        methods.add(obs_method);
      }
    }
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
          if (st.isPrimitiveInitialization()) continue;

          // If value's type is void (i.e. its statement is a
          // void-return method call), don't capture checks
          // (nothing interesting).
          Class<?> tc = st.getOutputType();
          if (void.class.equals(tc)) continue; // no return value.

          // If value is the result of Object.toString() or
          // Object.hashCode(), don't capture checks (value is
          // likely to be non-deterministic across runs).
          if (st.callsTheMethod(objectHashCode) || st.callsTheMethod(objectToString)) {
            continue;
          }

          Object o = e.getRuntimeValue();

          Variable var = s.sequence.getVariable(i);

          if (o == null) {

            // Add observer test for null
            checks.add(new ObjectCheck(new IsNull(), i, var));

          } else if (PrimitiveTypes.isBoxedPrimitiveTypeOrString(o.getClass())) {

            if (o instanceof String) {
              // System.out.printf ("considering String check for seq %08X\n",
              // s.seq_id());
              String str = (String) o;
              // Don't create assertions over strings that look like raw object
              // references.
              if (PrimitiveTypes.looksLikeObjectToString(str)) {
                // System.out.printf ("ignoring Object.toString obs %s%n", str);
                continue;
              }
              // Don't create assertions over strings that are really
              // long, as this can cause the generate unit tests to be
              // unreadable and/or non-compilable due to Java
              // restrictions on String constants.
              if (!PrimitiveTypes.stringLengthOK(str)) {
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
            if (s.sequence.getInputs(i).size() > 0) {
              Variable var0 = s.sequence.getInputs(i).get(0);
              if (var0.getType() == java.util.Date.class) {
                Statement sk = s.sequence.getCreatingStatement(var0);
                if ((sk.isConstructorCall()) && (s.sequence.getInputs(i).size() == 1)) continue;
                // System.out.printf ("var type %s comes from date %s / %s%n",
                // s.sequence.getVariable(i).getType(),
                // s.sequence.getStatementKind(i), sk);
              }
            }

            // Add observer test for the primitive
            PrimValue.PrintMode printMode;
            if (var.getType().isPrimitive()) {
              printMode = PrimValue.PrintMode.EQUALSEQUALS;
            } else {
              printMode = PrimValue.PrintMode.EQUALSMETHOD;
            }
            ObjectCheck oc = new ObjectCheck(new PrimValue(o, printMode), i, var);
            checks.add(oc);
            // System.out.printf ("Adding objectcheck %s to seq %08X\n",
            // oc, s.seq_id());

          } else { // its a more complex type with a non-null value

            // Assert that the value is not null.
            // Exception: if the value comes directly from a constructor call,
            // not interesting that it's non-null; omit the check.
            if (!(st.isConstructorCall())) {
              checks.add(new ObjectCheck(new IsNotNull(), i, var));
            }

            // Put out any observers that exist for this type
            Variable var0 = s.sequence.getVariable(i);
            List<Method> observers = observer_map.get(var0.getType());
            if (observers != null) {
              for (Method m : observers) {

                Object value = null;
                try {
                  value = m.invoke(o);
                } catch (Exception e2) {
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
                  throw new RuntimeException(msg, e2);
                }
                // Don't create assertions over string that look like raw object
                // references.
                if ((value instanceof String)
                    && PrimitiveTypes.looksLikeObjectToString((String) value)) {
                  continue;
                }

                ObjectContract observerEqValue = new ObserverEqValue(m, value);
                ObjectCheck observerCheck = new ObjectCheck(observerEqValue, i, var);
                // System.out.printf ("Adding observer %s%n", observerCheck);
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
