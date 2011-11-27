package randoop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import randoop.main.GenInputsAbstract;
import randoop.util.Files;
import randoop.util.PrimitiveTypes;
import randoop.util.Reflection;
import randoop.util.TimeoutExceededException;

/**
 * An execution visitor that records regression checks on the values
 * created by the sequence. It does this only after the last statement has been
 * executed.
 *
 * NOTES:
 *
 * <ul>
 *
 * <li> Should follow a contract-checking visitor, if the latter is also
 * present in a MultiVisitor. If there is a contract-checking violationg
 * in the sequence, this visitor adds no checks.
 *
 * <li> We only create checks over variables whose type is primitive or
 * String.
 *
 * <li> We do not create checks for the return values of Object.toString()
 * and Object.hashCode() as their values can vary from run to run.
 *
 * <li> We do not create checks for Strings that contain the string ";@"
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
  
  @Override
  public void initialize(ExecutableSequence s) {
    s.checks.clear();
    s.checksResults.clear();
    for (int i = 0 ; i < s.sequence.size() ; i++) {
      s.checks.add(new ArrayList<Check>(1));
      s.checksResults.add(new ArrayList<Boolean>(1));
    }
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

  public void visitAfter(ExecutableSequence s, int idx) {

    // We're only interested in statements at the end.
    if (idx < (s.sequence.size()-1))
      return;

    if (s.hasFailure(idx)) {
      return;
    }

    if (s.hasNonExecutedStatements()) {
      return;
    }

    // Capture checks for each value created.
    // Recall there are as many values as statements in the sequence.
    for (int i = 0; i < s.sequence.size() ; i++) {

      StatementKind st = s.sequence.getStatementKind(i);
      ExecutionOutcome result = s.getResult(i);

      if (result instanceof NormalExecution) {

        NormalExecution e = (NormalExecution)result;
        // If value is like x in "int x = 3" don't capture
        // checks (nothing interesting).
        if (st instanceof PrimitiveOrStringOrNullDecl)
          continue;


        // If value's type is void (i.e. its statement is a
        // void-return method call), don't capture checks
        // (nothing interesting).
        Class<?> tc = st.getOutputType();
        if (void.class.equals(tc))
          continue; // no return value.

        // If value is the result of Object.toString() or
        // Object.hashCode(), don't capture checks (value is
        // likely to be non-deterministic across runs).
        if (st instanceof RMethod) {
          Method method = ((RMethod)st).getMethod();
          if (method.equals(objectHashCode))
            continue;
          if (method.equals(objectToString))
            continue;
        }

        Object o = e.getRuntimeValue();
    
        Variable var = s.sequence.getVariable(i);

        if (o == null) {

          // Add observer test for null
          s.addCheck(idx,new ObjectCheck(new IsNull(), i, var), true);

        } else if (PrimitiveTypes.isBoxedPrimitiveTypeOrString(o.getClass())) {

          
          if (o instanceof String) {
            // System.out.printf ("considering String check for seq %08X\n",
            //                   s.seq_id());
            String str = (String)o;
            // Don't create assertions over strings that look like raw object references.
            if (PrimitiveTypes.looksLikeObjectToString(str)) {
              // System.out.printf ("ignoring Object.toString obs %s%n", str);
              continue;
            }
            // Don't create assertions over strings that are really
            // long, as this can cause the generate unit tests to be
            // unreadable and/or non-compilable due to Java
            // restrictions on String constants.
            if (str.length() > GenInputsAbstract.string_maxlen) {
              // System.out.printf ("Ignoring too long string%n");
              continue;
            }
          }

          // If the value is returned from a Date that we created,
          // don't use it as it's just going to have today's date in it.
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
          PrimValue.PrintMode printMode;
          if (var.getType().isPrimitive()) {
            printMode = PrimValue.PrintMode.EQUALSEQUALS;
          } else {
            printMode = PrimValue.PrintMode.EQUALSMETHOD;
          }
          ObjectCheck oc = new ObjectCheck(new PrimValue(o, printMode), i, var);
          s.addCheck(idx,oc, true);
          // System.out.printf ("Adding objectcheck %s to seq %08X\n",
          //                   oc, s.seq_id());

        } else { // its a more complex type with a non-null value

          // Assert that the value is not null.
          // Exception: if the value comes directly from a constructor call, 
          // not interesting that it's non-null; omit the check.
          if (!(st instanceof RConstructor)) {
            s.addCheck(idx, new ObjectCheck(new IsNotNull(), i, var), true);
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
                throw new RuntimeException("unexpected error invoking observer " + m + " on " + var + "[" + var.getType() + "]" + " with value " + o + " ["
                    + o.getClass() + "]", e2);
              }
              // Don't create assertions over string that look like raw object references.
              if ((value instanceof String) && PrimitiveTypes.looksLikeObjectToString((String)value)) {
                continue;
              }

              ObjectContract observerEqValue = new ObserverEqValue(m, value);
              ObjectCheck observerCheck = new ObjectCheck(observerEqValue, i, 
                                                          var);
              // System.out.printf ("Adding observer %s%n", observerCheck);
              s.addCheck(idx, observerCheck, true);
            }
          }
        }

      } else if (result instanceof ExceptionalExecution) {

        // The code threw an exception.  Require that the test throw the
        // same exception in the future as it did this time.

        ExceptionalExecution e = (ExceptionalExecution)result;
        
        Throwable exception = e.getException();

        s.addCheck(i, new ExpectedExceptionCheck(exception, i), true);
      } else {
        assert s.getResult(i) instanceof NotExecuted;
        assert false : "Randoop should not have gotten here (bug in Randoop)";
      }
    }
    return;
  }

  public void visitBefore(ExecutableSequence sequence, int i) {
    // Empty body.
  }
}
