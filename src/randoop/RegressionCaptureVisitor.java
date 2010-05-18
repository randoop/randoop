package randoop;

import java.lang.reflect.Method;
import java.util.ArrayList;

import randoop.util.PrimitiveTypes;

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

  public boolean visitAfter(ExecutableSequence s, int idx) {

    // We're only interested in statements at the end.
    if (idx < (s.sequence.size()-1))
      return true;

    if (s.hasFailure(idx)) {
      return true;
    }

    if (s.hasNonExecutedStatements()) {
      return true;
    }

    // Capture checks for each value created.
    // Recall there are as many values as statements in the sequence.
    for (int i = 0; i < s.sequence.size() ; i++) {

      StatementKind st = s.sequence.getStatementKind(i);

      if (s.getResult(i) instanceof NormalExecution) {

        NormalExecution e = (NormalExecution)s.getResult(i);
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
          s.addCheck(idx,new ObjectCheck(new IsNull(), var), true);

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
          s.addCheck(idx,new ObjectCheck(new PrimValue(o, printMode), var), true);

        } else { // its a more complex type with a non-null value

          // Assert that the value is not null.
          // Exception: if the value comes directly from a constructor call, 
          // not interesting that it's non-null; omit the check.
          if (!(st instanceof RConstructor)) {
            s.addCheck(idx, new ObjectCheck(new IsNotNull(), var), true);
          }
        }

      } else if (s.getResult(i) instanceof ExceptionalExecution) {

        ExceptionalExecution e = (ExceptionalExecution)s.getResult(i);
        s.addCheck(i, new ExpectedExceptionCheck(e.getException(), i), true);

      } else {
        assert s.getResult(i) instanceof NotExecuted;
        assert false : "Randoop should not have gotten here (bug in Randoop)";
      }
    }
    return true;
  }

  public void visitBefore(ExecutableSequence sequence, int i) {
    // Empty body.
  }
}
