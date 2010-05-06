package randoop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import randoop.util.PrimitiveTypes;
import randoop.main.GenInputsAbstract;
import randoop.util.Files;
import randoop.util.Reflection;

/**
 * An execution visitor that records regression observations on the values
 * created by the sequence. It does this only after the last statement has been
 * executed.
 *
 * NOTES:
 *
 * <ul>
 *
 * <li> Should follow a contract-checking visitor, if the latter is also
 * present in a MultiVisitor. If there is a contract-checking violationg
 * in the sequence, this visitor adds no observations.
 *
 * <li> We only create observations over variables whose type is primitive or
 * String.
 *
 * <li> We do not create observations for the return values of Object.toString()
 * and Object.hashCode() as their values can vary from run to run.
 *
 * <li> We do not create observations for Strings that contain the string ";@"
 * as this is a good indication that at least part of the String came from a
 * call of Object.toString() (e.g. "[[Ljava.lang.Object;@5780d9]" is the string
 * representation of a list containing one Object).
 *
 * </ul>
 */
public final class RegressionCaptureVisitor implements ExecutionVisitor {

  public RegressionCaptureVisitor() {
    // Empty body.
  }

  // We don't create regression observations for these methods.
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
  private static final Map<Class<?>, List<Method>> observer_map
    = new LinkedHashMap<Class<?>, List<Method>>();
  static {
    if (GenInputsAbstract.observers != null) {
      List<String> lines  = null;
      try {
        lines = Files.readWhole (GenInputsAbstract.observers);
      } catch (Exception e) {
        throw new RuntimeException ("problem reading observer file "
                                    + GenInputsAbstract.observers, e);
      }
      for (String line : lines) {
        if (line.startsWith ("//"))
          continue;
        if (line.trim().length() == 0)
          continue;
        int lastdot = line.lastIndexOf(".");
        if (lastdot == -1)
          throw new RuntimeException (String.format ("invalid observer '%s'",
                                                     line));
        String classname = line.substring (0, lastdot);
        String methodname = line.substring (lastdot+1);
        methodname = methodname.replaceFirst ("[()]*$", "");
        Class<?> obs_class = null;
        try {
          obs_class = Class.forName (classname);
        } catch (Exception e) {
          throw new RuntimeException ("Can't load observer class " + classname,
                                      e);
        }
        Method obs_method = null;
        try {
          obs_method = Reflection.super_get_declared_method (obs_class,
                                                             methodname);
        } catch (Exception e) {
          throw new RuntimeException ("Can't find observer method "
                                      + methodname, e);
        }
        if (!PrimitiveTypes.isPrimitiveOrStringType(obs_method.getReturnType()))
          throw new RuntimeException
            (String.format ("Observer method %s does not return a primitive "
                            + "or string", obs_method));
        List<Method> methods = observer_map.get (obs_class);
        if (methods == null) {
          methods = new ArrayList<Method>();
          observer_map.put (obs_class, methods);
        }
        methods.add (obs_method);
      }
    }
  }

  public boolean visitAfter(ExecutableSequence s, int idx) {

    // We're only interested in statements at the end.
    if (idx < (s.sequence.size()-1))
      return true;

    if (s.hasObservation(idx, ContractViolation.class)) {
      return true;
    }

    if (s.hasNonExecutedStatements()) {
      return true;
    }

    // Capture observations for each value created.
    // Recall there are as many values as statements in the sequence.
    for (int i = 0; i < s.sequence.size() ; i++) {

      StatementKind st = s.sequence.getStatementKind(i);

      if (s.getResult(i) instanceof NormalExecution) {

        NormalExecution e = (NormalExecution)s.getResult(i);
        // If value is like x in "int x = 3" don't capture
        // observations (nothing interesting).
        if (st instanceof PrimitiveOrStringOrNullDecl)
          continue;


        // If value's type is void (i.e. its statement is a
        // void-return method call), don't capture observations
        // (nothing interesting).
        Class<?> tc = st.getOutputType();
        if (void.class.equals(tc))
          continue; // no return value.

        // If value is the result of Object.toString() or
        // Object.hashCode(), don't capture observations (value is
        // likely to be non-deterministic across runs).
        if (st instanceof RMethod) {
          Method method = ((RMethod)st).getMethod();
          if (method.equals(objectHashCode))
            continue;
          if (method.equals(objectToString))
            continue;
        }

        Object o = e.getRuntimeValue();
    
        List<Variable> vars = new ArrayList<Variable>();
        vars.add(s.sequence.getVariable(i));

        if (o == null) {

          // Add observer test for null
          s.addObservation(idx,
                         new ExpressionEqValue(ValueExpression.class, vars, o));

        } else if (PrimitiveTypes.isBoxedPrimitiveTypeOrString(o.getClass())) {

          // If value is a String that contains "<classname>@<hex>" we
          // guess it might come from a call of Object.toString() and
          // don't print it either.  This may happen if some method
          // internally calls Object.toString().  This used to check for
          // ;@, but that doesn't seem to be correct.
          if (o instanceof String) {
            String str = (String)o;
            if (str.matches (".*[a-zA-Z]{2,}[a-zA-Z0-9.]*@[0-9a-h]{4,}.*")) {
              // System.out.printf ("ignoring Object.toString obs %s%n", str);
              continue;
            }
          }

          // If the value is returned from a Date that we created,
          // don't use it as its just going to have today's date in it.
          if (s.sequence.getInputs(i).size() > 0) {
            Variable var0 = s.sequence.getInputs (i).get(0);
            if (var0.getType() == java.util.Date.class) {
              StatementKind sk = s.sequence.getCreatingStatement (var0);
              if ((sk instanceof RConstructor) &&
                  (s.sequence.getInputs(i).size() == 1))
                continue;
              // System.out.printf ("var type %s comes from date %s / %s%n",
              //                   s.sequence.getVariable(i).getType(),
              //                   s.sequence.getStatementKind(i), sk);
            }
          }

          // Add observer test for the primitive
          s.addObservation(idx,
                         new ExpressionEqValue(ValueExpression.class, vars, o));

        } else { // its a more complex type with a non-null value

          // Assert that the value is not null (just as interesting as is null)
          s.addObservation(idx, new ExpressionNotEqValue(ValueExpression.class,
                                                         vars, null));

          // Put out any observers that exist for this type
          Variable var0 = s.sequence.getVariable(i);
          List<Method> observers = observer_map.get (var0.getType());
          if (observers != null) {
            for (Method m : observers) {
              Observation observer = new ObserverEqValue (m, var0, o);
              // System.out.printf ("Adding observer %s%n", observer);
              s.addObservation (idx, observer);
            }
          }
        }

      } else if (s.getResult(i) instanceof ExceptionalExecution) {

        ExceptionalExecution e = (ExceptionalExecution)s.getResult(i);
        s.addObservation(i, new StatementThrowsException(e.getException()));

      } else {
        assert s.getResult(i) instanceof NotExecuted;
        // We should not get here because this should only happen after
        // an ExceptionalExecution statement, and when that type of statement
        // is processed, the method returns (see code above).
        assert false : "Randoop should not have gotten here (bug in Randoop)";
      }
    }
    return true;
  }

  public void visitBefore(ExecutableSequence sequence, int i) {
    // Empty body.
  }
}
