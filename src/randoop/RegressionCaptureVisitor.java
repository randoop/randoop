package randoop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import randoop.util.PrimitiveTypes;


/**
 * An execution visitor that records regression observations on the values
 * created by the sequence. It does this only after the last statement has been
 * executed.
 *
 * NOTES:
 *
 * <ul>
 *
 * <li> Should follow a contrat-checking visitor, if the latter is also
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

      if (s.getResult(i) instanceof NormalExecution) {

        NormalExecution e = (NormalExecution)s.getResult(i);
        // If value is like x in "int x = 3" don't capture observations (nothing interesting).
        if (s.sequence.getStatementKind(i) instanceof PrimitiveOrStringOrNullDecl)
          continue;

        StatementKind st = s.sequence.getStatementKind(i);

        // If value's type is void (i.e. its statement is a void-return method call), don't
        // capture observations (nothing interesting).
        Class<?> tc = st.getOutputType();
        if (void.class.equals(tc))
          continue; // no return value.

        // If value is the result of Object.toString() or Object.hashCode(), don't
        // capture observations (value is likely to be non-deterministic across runs).
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

        if (o == null || PrimitiveTypes.isBoxedPrimitiveTypeOrString(o.getClass())) {

          // If value is a String that contains ";@" we guess it might come
          // from a call of Object.toString() and don't print it either.
          // This may happen if some method internally calls Object.toString().
          if (o != null && o instanceof String) {
            String str = (String)o;
            if (str.indexOf(";@") != -1)
              continue;
          }

          s.addObservation(idx, new ExpressionEqValue(ValueExpression.class, vars, o));
        } else {
          continue;
        }

      } else if (s.getResult(i) instanceof ExceptionalExecution) {

        ExceptionalExecution e = (ExceptionalExecution)s.getResult(idx);
        s.addObservation(idx, new StatementThrowsException(e.getException()));

        for (int j = i + 1 ; j < s.sequence.size() ; j++) {
          assert s.getResult(j) instanceof NotExecuted
            : "i=" + i + ",sequence=" + s.sequence.toString();
        }
        return true;

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
