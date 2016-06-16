package randoop.test;

import java.util.List;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NotExecuted;
import randoop.contract.ObjectContract;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ReferenceValue;
import randoop.test.predicate.ExceptionPredicate;
import randoop.util.TupleSet;

/**
 * An execution visitor that generates checks for error-revealing tests. If
 * execution of the visited sequence is normal, it will generate checks for
 * unary and binary object contracts over the values from the execution.
 * Contracts will be checked on all values except for boxed primitives or
 * Strings. If the execution throws an exception considered to be an error, the
 * visitor generates a {@code NoExceptionCheck} indicating that the statement
 * threw an exception in error. For each contract violation, the visitor adds a
 * {@code Check} to the {@code TestChecks} object that is returned.
 */
public final class ContractCheckingVisitor implements TestCheckGenerator {

  private ContractSet contracts;
  private ExceptionPredicate exceptionPredicate;

  /**
   * Create a new visitor that checks the given contracts after the last
   * statement in a sequence is executed.
   *
   * @param contracts
   *          Expected to be unary contracts, i.e. for each contract
   *          <code>c</code>, <code>c.getArity() == 1</code>.
   * @param exceptionPredicate
   *          the predicate to test for exceptions that are errors
   *
   */
  public ContractCheckingVisitor(ContractSet contracts, ExceptionPredicate exceptionPredicate) {
    this.contracts = contracts;
    this.exceptionPredicate = exceptionPredicate;
  }

  /**
   * {@inheritDoc} Adds checks to final statement of sequence. Looks for failure
   * exceptions, and violations of contracts in {@code contracts}.
   */
  @Override
  public TestChecks visit(ExecutableSequence s) {
    ErrorRevealingChecks checks = new ErrorRevealingChecks();

    int finalIndex = s.sequence.size() - 1;
    ExecutionOutcome finalResult = s.getResult(finalIndex);

    // If statement not executed, then something flaky
    if (finalResult instanceof NotExecuted) {
      throw new Error("Un-executed final statement in sequence: " + s);
    }

    if (finalResult instanceof ExceptionalExecution) {
      // If there is an exception, check whether it is considered a failure
      ExceptionalExecution exec = (ExceptionalExecution) finalResult;

      if (exceptionPredicate.test(exec, s)) {
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
        List<ReferenceValue> statementValues = s.getLastStatementValues();
        List<ObjectContract> unaryContracts = contracts.getArity(1);
        if (!unaryContracts.isEmpty()) {
          TupleSet<ReferenceValue> statementTuples = new TupleSet<>();
          statementTuples = statementTuples.extend(statementValues);
          check = statementTuples.findAndTransform(new ContractChecker(s, unaryContracts));
          if (check != null) {
            checks.add(check);
            return checks;
          }
        }

        // 2. check binary over all other values
        List<ReferenceValue> inputValues = s.getInputValues();
        TupleSet<ReferenceValue> inputTuples = new TupleSet<>();
        inputTuples = inputTuples.extend(inputValues).extend(inputValues);
        List<ObjectContract> binaryContracts = contracts.getArity(2);
        if (!binaryContracts.isEmpty()) {

          check = inputTuples.findAndTransform(new ContractChecker(s, binaryContracts));
          if (check != null) {
            checks.add(check);
            return checks;
          }
        }

        // 3. check ternary over statement x pair of input values
        TupleSet<ReferenceValue> ternaryTuples = inputTuples.exhaustivelyExtend(statementValues);
        List<ObjectContract> ternaryContracts = contracts.getArity(3);
        if (!ternaryContracts.isEmpty()) {
          check = ternaryTuples.findAndTransform(new ContractChecker(s, ternaryContracts));
          if (check != null) {
            checks.add(check);
            return checks;
          }
        }
      }
    }
    return checks;
  }

}
