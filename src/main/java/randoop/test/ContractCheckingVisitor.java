package randoop.test;

import java.util.LinkedHashSet;
import java.util.Set;

import randoop.BugInRandoopException;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.contract.ObjectContract;
import randoop.contract.ObjectContractUtils;
import randoop.sequence.ExecutableSequence;
import randoop.test.predicate.ExceptionPredicate;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypes;
import randoop.util.Log;
import randoop.util.MultiMap;

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

  private Set<ObjectContract> contracts;
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
  public ContractCheckingVisitor(
      Set<ObjectContract> contracts, ExceptionPredicate exceptionPredicate) {
    this.contracts = new LinkedHashSet<>();
    for (ObjectContract c : contracts) {
      if (c.getArity() > 3)
        throw new IllegalArgumentException("Visitor accepts only unary or binary or ternary contracts.");
      this.contracts.add(c);
    }
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
      throw new Error("Unexecuted final statement in sequence: " + s);
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
      MultiMap<ConcreteType, Integer> idxmap = indicesToCheck(s);
      for (ConcreteType cls : idxmap.keySet()) {
        for (ObjectContract c : contracts) {
          if (c.getArity() == 1) {
            checkUnary(s, c, idxmap.getValues(cls), checks);
          } else if (c.getArity() == 2){
            checkBinary(s, c, idxmap.getValues(cls), checks);
          } else {
            checkTernary(s, c, idxmap.getValues(cls), checks);
          }
        }
      }
    }
    return checks;
  }

  /**
   * Checks a ternary contract over the set of values defined in the sequence,
   * and attaches failing checks at final statement of the sequence.
   *
   * @param s
   *          the executable sequence
   * @param c
   *          the contract to check
   * @param values
   *          the set of positions defining values to check
   * @param checks
   *          the {@code TestChecks} to which new checks are added
   */
  private void checkTernary(
          ExecutableSequence s, ObjectContract c, Set<Integer> values, ErrorRevealingChecks checks) {
    for (Integer i : values) {
      for (Integer j : values) {
        for (Integer k : values) {
          // Create three ExecutionOutcome objects from the ExecutableSequence
          ExecutionOutcome result1 = s.getResult(i);
          ExecutionOutcome result2 = s.getResult(j);
          ExecutionOutcome result3 = s.getResult(k);

          if (Log.isLoggingOn()) {
            Log.logLine("Checking contract " + c.getClass() + " on " + i + ", " + j + ", " + k);
          }

          ExecutionOutcome exprOutcome =
                  ObjectContractUtils.execute(
                          c,
                          ((NormalExecution) result1).getRuntimeValue(),
                          ((NormalExecution) result2).getRuntimeValue(),
                          ((NormalExecution) result3).getRuntimeValue());

          if (exprOutcome instanceof NormalExecution) {
            NormalExecution e = (NormalExecution) exprOutcome;
            if (e.getRuntimeValue().equals(true)) {
              if (Log.isLoggingOn()) Log.logLine("Contract returned true.");
              // Behavior ok.
            } else {
              if (Log.isLoggingOn())
                Log.logLine("Contract returned false. Will add ExpressionEqFalse check");
              // Create an check that records the actual value
              // returned by the expression, marking it as invalid
              // behavior.
              checks.add(new ObjectCheck(c, i, s.sequence.getVariable(i), s.sequence.getVariable(j), s.sequence.getVariable(k)));
            }
          } else if (exprOutcome instanceof ExceptionalExecution) {
            Throwable e = ((ExceptionalExecution) exprOutcome).getException();
            if (Log.isLoggingOn()) Log.logLine("Contract threw exception: " + e.getMessage());
            if (e instanceof BugInRandoopException) {
              throw new BugInRandoopException(e);
            }
            // Execution of contract resulted in exception. Do not create
            // a contract-violation decoration.
            // TODO are there cases where exception in contract check is a
            // failure?
          } else {
            throw new Error("Contract failed to execute during evaluation");
          }
        }
      }
    }
  }

  /**
   * Checks a binary contract over the set of values defined in the sequence,
   * and attaches failing checks at final statement of the sequence.
   *
   * @param s
   *          the executable sequence
   * @param c
   *          the contract to check
   * @param values
   *          the set of positions defining values to check
   * @param checks
   *          the {@code TestChecks} to which new checks are added
   */
  private void checkBinary(
      ExecutableSequence s, ObjectContract c, Set<Integer> values, ErrorRevealingChecks checks) {
    for (Integer i : values) {
      for (Integer j : values) {

        ExecutionOutcome result1 = s.getResult(i);
        ExecutionOutcome result2 = s.getResult(j);

        if (!((result1 instanceof NormalExecution) && (result2 instanceof NormalExecution))) {
          throw new Error("Abnormal execution in sequence: " + s);
        }

        if (Log.isLoggingOn())
          Log.logLine("Checking contract " + c.getClass() + " on " + i + ", " + j);

        ExecutionOutcome exprOutcome =
            ObjectContractUtils.execute(
                c,
                ((NormalExecution) result1).getRuntimeValue(),
                ((NormalExecution) result2).getRuntimeValue());

        if (exprOutcome instanceof NormalExecution) {
          NormalExecution e = (NormalExecution) exprOutcome;
          if (e.getRuntimeValue().equals(true)) {
            if (Log.isLoggingOn()) Log.logLine("Contract returned true.");
            // Behavior ok.
          } else {
            if (Log.isLoggingOn())
              Log.logLine("Contract returned false. Will add ExpressionEqFalse check");
            // Create an check that records the actual value
            // returned by the expression, marking it as invalid
            // behavior.
            checks.add(new ObjectCheck(c, i, s.sequence.getVariable(i), s.sequence.getVariable(j)));
          }
        } else if (exprOutcome instanceof ExceptionalExecution) {
          Throwable e = ((ExceptionalExecution) exprOutcome).getException();
          if (Log.isLoggingOn()) Log.logLine("Contract threw exception: " + e.getMessage());
          if (e instanceof BugInRandoopException) {
            throw new BugInRandoopException(e);
          }
          // Execution of contract resulted in exception. Do not create
          // a contract-violation decoration.
          // TODO are there cases where exception in contract check is a
          // failure?
        } else {
          throw new Error("Contract failed to execute during evaluation");
        }
      }
    }
  }

  /**
   * Checks a unary contract over the set of values defined in a sequence, and
   * attaches failing checks to the final statement of the sequence.
   *
   * @param s
   *          the executable sequence where values are defined
   * @param c
   *          the contract to check
   * @param values
   *          the set of positions with values to check
   * @param checks
   *          the {@code TestChecks} to which new checks are added
   */
  private void checkUnary(
      ExecutableSequence s, ObjectContract c, Set<Integer> values, ErrorRevealingChecks checks) {

    for (Integer i : values) {

      ExecutionOutcome result = s.getResult(i);
      if (!(result instanceof NormalExecution)) {
        throw new Error("Abnormal execution in sequence: " + s);
      }

      ExecutionOutcome exprOutcome =
          ObjectContractUtils.execute(c, ((NormalExecution) result).getRuntimeValue());

      if (exprOutcome instanceof NormalExecution) {
        NormalExecution e = (NormalExecution) exprOutcome;
        if (e.getRuntimeValue().equals(true)) {
          continue; // Behavior ok, move to next value
        }
      } else if (exprOutcome instanceof ExceptionalExecution) {
        // Execution of contract resulted in exception. Do not create
        // a contract-violation decoration.
        Throwable e = ((ExceptionalExecution) exprOutcome).getException();
        if (Log.isLoggingOn()) Log.logLine("Contract threw exception: " + e.getMessage());
        if (e instanceof BugInRandoopException) {
          throw new BugInRandoopException(e);
        }
        if (!c.evalExceptionMeansFailure()) {
          continue; // not violation, move to next value
        }
      } else {
        throw new Error("Contract failed to execute during evaluation");
      }

      // If we get here, either the contract returned false or resulted
      // in an exception that is considered a failure. Add
      // a contract violation check.
      // Create an check that records the actual value
      // returned by the expression, marking it as invalid
      // behavior.
      checks.add(new ObjectCheck(c, i, s.sequence.getVariable(i)));
    }
  }

  /**
   * Returns the indices for the objects to check contracts over as a map from
   * types to the position of the given type.
   * <p>
   * If an element is primitive, a String, or null, its index is not returned.
   *
   * @param s
   *          the code sequence
   * @return map indicating statement positions where variables of a type are
   *         assigned
   */
  private static MultiMap<ConcreteType, Integer> indicesToCheck(ExecutableSequence s) {
    MultiMap<ConcreteType, Integer> positionMap = new MultiMap<>();

    for (int i = 0; i < s.sequence.size(); i++) {

      ExecutionOutcome result = s.getResult(i);
      if (result instanceof NormalExecution) {

        ConcreteType outputType = s.sequence.getStatement(i).getOutputType();
        if (!outputType.equals(ConcreteTypes.VOID_TYPE)
            && !outputType.equals(ConcreteTypes.STRING_TYPE)
            && !outputType.isPrimitive()
            && ((NormalExecution) result).getRuntimeValue() != null) {
          positionMap.add(outputType, i);
        }

      } else {
        throw new Error("Abnormal execution in sequence: " + s);
      }
    }
    return positionMap;
  }
}
