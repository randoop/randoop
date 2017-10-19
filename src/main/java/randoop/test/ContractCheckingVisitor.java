package randoop.test;

import java.util.List;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NotExecuted;
import randoop.contract.ObjectContract;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ReferenceValue;
import randoop.test.predicate.ExceptionPredicate;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeTuple;
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
   * @param tuples the value tuples to use as input to the contracts
   * @return a {@link Check} of the first contract that failed (for any tuple), or null if no
   *     contracts failed
   */
  Check checkContracts(
      List<ObjectContract> contracts, ExecutableSequence eseq, TupleSet<ReferenceValue> tuples) {
    for (List<ReferenceValue> tuple : tuples.tuples()) {

      for (ObjectContract contract : contracts) {
        assert tuple.size() == contract.getArity()
            : "value tuple size "
                + tuple.size()
                + " must match contract arity "
                + contract.getArity();
        if (typesMatch(contract.getInputTypes(), tuple)) {
          if (Log.isLoggingOn()) {
            Log.logLine("Checking contract " + contract.getClass());
          }
          Object[] values = getValues(tuple);
          Check check = contract.checkContract(eseq, values);
          if (check != null) {
            return check;
          }
        }
      }
    }

    return null;
  }

  /**
   * Indicates whether the given list of values matches the types in the type tuple. Contracts may
   * have generic input types, so this method checks for consistent substitutions across value
   * types.
   *
   * @param inputTypes the expected types for contract input
   * @param valueTuple the values to match against input types
   * @return true if the types of the values are assignable to the expected types, false otherwise
   */
  public static boolean typesMatch(TypeTuple inputTypes, List<ReferenceValue> valueTuple) {
    if (inputTypes.size() != valueTuple.size()) {
      return false;
    }

    Substitution<ReferenceType> substitution = new Substitution<>();
    int i = 0;
    while (i < inputTypes.size()) {
      Type inputType = inputTypes.get(i);
      ReferenceType valueType = valueTuple.get(i).getType();
      if (inputType.isGeneric()) { // check substitutions
        if (valueType instanceof ClassOrInterfaceType) {
          ClassOrInterfaceType classType = (ClassOrInterfaceType) valueType;
          InstantiatedType superType =
              classType.getMatchingSupertype((GenericClassType) inputTypes.get(i));
          if (superType == null) {
            return false;
          }
          Substitution<ReferenceType> subst = superType.getTypeSubstitution();
          if (!substitution.isConsistentWith(subst)) {
            return false;
          }
          substitution = substitution.extend(subst);
        } else { // have generic input type, and non-class value
          return false;
        }
      } else if (!inputType.isAssignableFrom(valueType)) {
        return false;
      }
      i++;
    }
    return true;
  }

  // TODO: Is this called a lot of times redundantly?
  /**
   * Creates an {@code Object} array for the given value list.
   *
   * @param tuple the list of values
   * @return the Object array for the values
   */
  public static Object[] getValues(List<ReferenceValue> tuple) {
    Object[] values = new Object[tuple.size()];
    for (int i = 0; i < tuple.size(); i++) {
      values[i] = tuple.get(i).getObjectValue();
    }
    return values;
  }
}
