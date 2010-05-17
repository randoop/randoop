package randoop;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import randoop.main.GenInputsAbstract;
import randoop.util.Log;
import randoop.util.MultiMap;
import randoop.util.PrimitiveTypes;


/**
 * An execution visitor that checks unary and binary object contracts on the
 * values created by the sequence. It does this only after the last statement
 * has been executed. For each contract violation, the visitor adds an
 * Check to the last index in the sequence.
 *
 * If the sequence throws an exception, the visitor does not check any contracts [[
 * TODO update]]. If it does not throw an exception, it checks all contracts on
 * each object returned by each statement, except objects that are boxed
 * primitives or Strings.
 */
public final class ContractCheckingVisitor implements ExecutionVisitor {

  private List<ObjectContract> contracts;
  private boolean checkAtEndOfExec;

  /**
   * Create a new visitor that checks the given contracts after the last
   * statement in a sequence is executed.
   *
   * @param contracts
   *          Expected to be unary contracts, i.e. for each contract
   *          <code>c</code>, <code>c.getArity() == 1</code>.
   *
   * @param checkAfterLast
   *          If true, checks contracts only when the last statement
   *          is visited. If false, checks contracts after each
   *          statement is visited.
   */
  public ContractCheckingVisitor(List<ObjectContract> contracts, boolean checkAfterLast) {
    this.contracts = new ArrayList<ObjectContract>();
    this.checkAtEndOfExec = checkAfterLast;
    for (ObjectContract c : contracts) {
      if (c.getArity() > 2)
        throw new IllegalArgumentException("Visitor accepts only unary or binary contracts.");
      this.contracts.add(c);
    }
  }

  public void visitBefore(ExecutableSequence sequence, int i) {
    // no body.
  }

  /**
   * If idx is the last index, checks contracts.
   */
  public boolean visitAfter(ExecutableSequence s, int idx) {

    for (int i = 0 ; i <= idx ; i++) {
      assert !(s.getResult(i) instanceof NotExecuted) : s;
      if (i < idx)
        assert !(s.getResult(i) instanceof ExceptionalExecution) : s;
    }

    if (checkAtEndOfExec && idx < s.sequence.size() - 1) {
      // Check contracts only after the last statement is executed.
      return true;
    }

     if (s.getResult(idx) instanceof ExceptionalExecution) {
       if (GenInputsAbstract.forbid_null) {
         ExceptionalExecution exec = (ExceptionalExecution)s.getResult(idx);
         if (exec.getException().getClass().equals(NullPointerException.class)) {
           ForbiddenExceptionChecker obs = new ForbiddenExceptionChecker(NullPointerException.class);
          s.addCheck(idx, obs, false);
         }
       }
       return true;
     }

    MultiMap<Class<?>, Integer> idxmap = objectIndicesToCheck(s, idx);

    for (Class<?> cls : idxmap.keySet()) {

      for (ObjectContract c : contracts) {

        if (c.getArity() == 1) {
          checkUnary(s, c, idxmap.getValues(cls), idx);
        } else {
          checkBinary(s, c, idxmap.getValues(cls), idx);
        }
      }
    }
    return true;
  }

  private void checkBinary(ExecutableSequence s, ObjectContract c, Set<Integer> values, int idx) {
    for (Integer i : values) {
      for (Integer j : values) {

        ExecutionOutcome result1 = s.getResult(i);
        assert result1 instanceof NormalExecution: s;

        ExecutionOutcome result2 = s.getResult(j);
        assert result2 instanceof NormalExecution: s;

        if (Log.isLoggingOn()) Log.logLine("Checking contract " + c.getClass() + " on " + i + ", " + j);

        ExecutionOutcome exprOutcome = ObjectContractUtils.execute(c,
            ((NormalExecution) result1).getRuntimeValue(),
            ((NormalExecution) result2).getRuntimeValue());

        Check obs = null;

        if (exprOutcome instanceof NormalExecution) {
          NormalExecution e = (NormalExecution)exprOutcome;
          if (e.getRuntimeValue().equals(true)) {
            if (Log.isLoggingOn()) Log.logLine("Contract returned true.");
            continue; // Behavior ok.
          } else {
            if (Log.isLoggingOn()) Log.logLine("Contract returned false. Will add ExpressionEqFalse check");
            // Create an check that records the actual value
            // returned by the expression, marking it as invalid
            // behavior.
            obs = new ObjectCheck(c, s.sequence.getVariable(i), s.sequence.getVariable(j));
            s.addCheck(idx, obs, false);
          }
        } else {
          if (Log.isLoggingOn()) Log.logLine("Contract threw exception.");
          // Execution of contract resulted in exception. Do not create
          // a contract-violation decoration.
          assert exprOutcome instanceof ExceptionalExecution;
        }
      }
    }
  }

  private void checkUnary(ExecutableSequence s, ObjectContract c, Set<Integer> values, int idx) {

    for (Integer i : values) {

      ExecutionOutcome result = s.getResult(i);
      assert result instanceof NormalExecution: s;

      ExecutionOutcome exprOutcome = ObjectContractUtils.execute(c,
          ((NormalExecution) result).getRuntimeValue());

      Object runtimeValue = null;
      if (exprOutcome instanceof NormalExecution) {
        NormalExecution e = (NormalExecution)exprOutcome;
        runtimeValue = e.getRuntimeValue();
        if (e.getRuntimeValue().equals(true)) {
          continue; // Behavior ok.
        }
      } else {
        // Execution of contract resulted in exception. Do not create
        // a contract-violation decoration.
        assert exprOutcome instanceof ExceptionalExecution;
        ExceptionalExecution e = (ExceptionalExecution)exprOutcome;
        if (e.getException().equals(BugInRandoopException.class)) {
          throw new BugInRandoopException(e.getException());
        }
        if (!c.evalExceptionMeansFailure()) {
          // Exception thrown, but not considered a failure.
          // Will not record behavior.
          continue; 
        }
      }

      // If we get here, either the contract returned false or resulted
      // in an exception that is considered a failure. Add
      // a contract violation check.
      // Create an check that records the actual value
      // returned by the expression, marking it as invalid
      // behavior.
      Check obs = new ObjectCheck(c, s.sequence.getVariable(i));
      s.addCheck(idx, obs, false);
    }
  }

  // Returns the indices for the objects to check contracts over.
  //
  // If an element is primitive, a String, or null, its index is not returned.
  //
  // The indices are returned as a map, from types to the indices of
  // the given type. Binary contracts are only checked for objects
  // of equal types, so themap is handy.
  private static MultiMap<Class<?>, Integer> objectIndicesToCheck(ExecutableSequence s, int maxIdx) {

    MultiMap<Class<?>, Integer> map = new MultiMap<Class<?>, Integer>();

    for (int i = 0 ; i <= maxIdx ; i++) {
      ExecutionOutcome result = s.getResult(i);

      assert result instanceof NormalExecution;

      Class<?> outputType = s.sequence.getStatementKind(i).getOutputType();

      if (outputType.equals(void.class))
        continue;
      if (outputType.equals(String.class))
        continue;
      if (PrimitiveTypes.isPrimitive(outputType))
        continue;

      Object runtimeValue = ((NormalExecution) result).getRuntimeValue();
      if (runtimeValue == null)
        continue;

      map.add(outputType, i);
    }
    return map;
  }

}
