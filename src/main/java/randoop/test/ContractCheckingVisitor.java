package randoop.test;

import java.util.List;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NotExecuted;
import randoop.contract.ObjectContract;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ReferenceValue;
import randoop.test.predicate.ExceptionPredicate;
import randoop.util.Log;
import randoop.util.TupleSet;

/**
 * An execution visitor that generates checks for error-revealing tests. If execution of the visited
 * sequence is normal, it will generate checks for unary and binary object contracts over the values
 * from the execution. Contracts will be checked on all values except for boxed primitives or
 * Strings. If the execution throws an exception considered to be an error, the visitor generates a
 * {@code NoExceptionCheck} indicating that the statement threw an exception in error. For each
 * contract violation, the visitor adds a {@code Check} to the {@code TestChecks} object that is
 * returned.
 */
public final class ContractCheckingVisitor implements TestCheckGenerator {

  private ContractSet contracts;
  private ExceptionPredicate exceptionPredicate;

  /**
   * Create a new visitor that checks the given contracts after the last statement in a sequence is
   * executed.
   *
   * @param contracts expected to be unary contracts, i.e. for each contract {@code c}, {@code
   *     c.getArity() == 1}.
   * @param exceptionPredicate the predicate to test for exceptions that are errors
   */
  public ContractCheckingVisitor(ContractSet contracts, ExceptionPredicate exceptionPredicate) {
    this.contracts = contracts;
    this.exceptionPredicate = exceptionPredicate;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Adds checks to final statement of sequence. Looks for failure exceptions, and violations of
   * contracts in {@code contracts}.
   */
  @Override
  public TestChecks visit(ExecutableSequence eseq) {
    ErrorRevealingChecks checks = new ErrorRevealingChecks();

    int finalIndex = eseq.sequence.size() - 1;
    ExecutionOutcome finalResult = eseq.getResult(finalIndex);

    // If statement not executed, then something flaky
    if (finalResult instanceof NotExecuted) {
      throw new Error("Un-executed final statement in sequence: " + eseq);
    }

    if (finalResult instanceof ExceptionalExecution) {
      // If there is an exception, check whether it is considered a failure
      ExceptionalExecution exec = (ExceptionalExecution) finalResult;

      if (exceptionPredicate.test(exec, eseq)) {
        String exceptionName = exec.getException().getClass().getName();
        NoExceptionCheck obs = new NoExceptionCheck(finalIndex, exceptionName);
        checks.add(obs);
      }

      // If exception not considered a failure, don't include checks

    } else {
      // Otherwise, normal execution, check contracts
      if (!contracts.isEmpty()) {
        Check check;

        // 1. check unary over values in last statement
        // TODO: Why aren't unary contracts checked over all values like binary contracts are?
        List<ReferenceValue> statementValues = eseq.getLastStatementValues();
        List<ObjectContract> unaryContracts = contracts.getWithArity(1);
        if (!unaryContracts.isEmpty()) {
          TupleSet<ReferenceValue> statementTuples = new TupleSet<>();
          statementTuples = statementTuples.extend(statementValues);
          check = checkContracts(unaryContracts, eseq, statementTuples);
          if (check != null) {
            checks.add(check);
            return checks;
          }
        }

        // 2. check binary over all pairs of values.
        // Rationale:  this call might have side-effected some previously-existing value.
        List<ReferenceValue> inputValues = eseq.getInputValues();
        TupleSet<ReferenceValue> inputTuples = new TupleSet<>();
        inputTuples = inputTuples.extend(inputValues).extend(inputValues);
        List<ObjectContract> binaryContracts = contracts.getWithArity(2);
        if (!binaryContracts.isEmpty()) {
          check = checkContracts(binaryContracts, eseq, inputTuples);
          if (check != null) {
            checks.add(check);
            return checks;
          }
        }

        // 3. check ternary over statement x pair of input values
        TupleSet<ReferenceValue> ternaryTuples = inputTuples.exhaustivelyExtend(statementValues);
        List<ObjectContract> ternaryContracts = contracts.getWithArity(3);
        if (!ternaryContracts.isEmpty()) {
          check = checkContracts(ternaryContracts, eseq, ternaryTuples);
          if (check != null) {
            checks.add(check);
            return checks;
          }
        }
      }
    }
    return checks;
  }

  /**
   * Finds the first tuple for which a contract fails, and returns the failing check.
   *
   * @return a failing Check, or null
   */
  Check checkContracts(
      List<ObjectContract> contracts, ExecutableSequence s, TupleSet<ReferenceValue> tuples) {
    for (List<ReferenceValue> tuple : tuples.tuples()) {

      for (ObjectContract contract : contracts) {
        assert tuple.size() == contract.getArity()
            : "value tuple size "
                + tuple.size()
                + " must match contract arity "
                + contract.getArity();
        // if (Randomness.selectionLog.enabled() && Randomness.verbosity > 0) {
        //   Randomness.selectionLog.log("ContractChecker.apply: considering contract=%s%n", contract);
        // }
        if (ContractChecker.typesMatch(contract.getInputTypes(), tuple)) {
          if (Log.isLoggingOn()) {
            Log.logLine("Checking contract " + contract.getClass());
          }
          Object[] values = ContractChecker.getValues(tuple);
          Check check = ContractChecker.checkContract(contract, s, values);
          if (check != null) {
            return check;
          }
        }
      }
    }

    return null;
  }
}
