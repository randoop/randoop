package randoop.test;

import static randoop.main.GenInputsAbstract.BehaviorType.ERROR;

import java.util.List;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.contract.ObjectContract;
import randoop.main.ExceptionBehaviorClassifier;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ReferenceValue;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.TupleSet;

/**
 * An execution visitor that generates checks for error-revealing tests.
 *
 * <p>If execution of the visited sequence is normal, it will generate checks for contracts over the
 * values from the execution. Contracts will be checked on all values except for boxed primitives or
 * Strings. For each contract violation, the visitor adds a {@code Check} to the {@code TestChecks}
 * object that is returned.
 *
 * <p>If the execution throws an exception considered to be an error, the visitor generates a {@code
 * NoExceptionCheck} indicating that the statement should not throw the exception.
 */
public final class ContractCheckingGenerator extends TestCheckGenerator {

  private ContractSet contracts;

  /**
   * Create a new visitor that checks the given contracts after the last statement in a sequence is
   * executed.
   *
   * @param contracts expected to be unary contracts, i.e. for each contract {@code c}, {@code
   *     c.getArity() == 1}.
   */
  public ContractCheckingGenerator(ContractSet contracts) {
    this.contracts = contracts;
  }

  // TODO: what is a "failure exception"?
  // TODO: in what sense does this "Adds checks to final statement of sequence"?
  /**
   * {@inheritDoc}
   *
   * <p>Adds checks to final statement of sequence. Looks for failure exceptions, and violations of
   * contracts in {@code contracts}.
   */
  @Override
  public TestChecks<?> generateTestChecks(ExecutableSequence eseq) {

    int finalIndex = eseq.sequence.size() - 1;
    ExecutionOutcome finalResult = eseq.getResult(finalIndex);

    if (finalResult instanceof NotExecuted) {
      // If statement not executed, then something is flaky
      throw new Error("Un-executed final statement in sequence: " + eseq);
    } else if (finalResult instanceof ExceptionalExecution) {
      // If there is an exception, check whether it is considered a failure
      ExceptionalExecution exec = (ExceptionalExecution) finalResult;

      if (ExceptionBehaviorClassifier.classify(exec, eseq) == ERROR) {
        String exceptionName = exec.getException().getClass().getName();
        NoExceptionCheck obs = new NoExceptionCheck(finalIndex, exceptionName);
        return new ErrorRevealingChecks(obs);
      }

      // TODO: The classification might be EXPECTED or INVALID.
      // This does the same thing for both possibilities.  Is that correct behavior?

      // If exception not considered a failure, don't include checks
      return ErrorRevealingChecks.EMPTY;

    } else {
      // Otherwise, normal execution, check contracts
      assert finalResult instanceof NormalExecution;
      if (!contracts.isEmpty()) {
        // 1. check unary over values in last statement
        // TODO: Why aren't unary contracts checked over all values like binary contracts are?
        List<ReferenceValue> statementValues = eseq.getLastStatementValues();
        List<ObjectContract> unaryContracts = contracts.getWithArity(1);
        if (!unaryContracts.isEmpty()) {
          TupleSet<ReferenceValue> statementTuples = new TupleSet<>();
          statementTuples = statementTuples.extend(statementValues);
          Check check = checkContracts(unaryContracts, eseq, statementTuples);
          if (check != null) {
            return singletonTestCheck(check);
          }
        }

        // 2. check binary over all pairs of values.
        // Rationale:  this call might have side-effected some previously-existing value.
        List<ReferenceValue> inputValues = eseq.getInputValues();
        TupleSet<ReferenceValue> inputTuples = new TupleSet<>();
        inputTuples = inputTuples.extend(inputValues).extend(inputValues);
        List<ObjectContract> binaryContracts = contracts.getWithArity(2);
        if (!binaryContracts.isEmpty()) {
          Check check = checkContracts(binaryContracts, eseq, inputTuples);
          if (check != null) {
            return singletonTestCheck(check);
          }
        }

        // 3. check ternary over statement x pair of input values
        TupleSet<ReferenceValue> ternaryTuples = inputTuples.exhaustivelyExtend(statementValues);
        List<ObjectContract> ternaryContracts = contracts.getWithArity(3);
        if (!ternaryContracts.isEmpty()) {
          Check check = checkContracts(ternaryContracts, eseq, ternaryTuples);
          if (check != null) {
            return singletonTestCheck(check);
          }
        }
      }
    }
    return ErrorRevealingChecks.EMPTY;
  }

  /**
   * Return a TestChecks that contains only the given check.
   *
   * @param check the sole member of the singleton TestChecks
   * @return a TestChecks that contains only the given check
   */
  private TestChecks<?> singletonTestCheck(Check check) {
    // System.out.printf("singletonTestCheck([class %s] %s)%n", check.getClass(), check);
    // new Error().printStackTrace();
    if (check instanceof InvalidExceptionCheck) {
      return new InvalidChecks((InvalidExceptionCheck) check);
    } else {
      return new ErrorRevealingChecks(check);
    }
  }

  /**
   * If a contract fails for some tuple, returns some such failing check.
   *
   * @param contracts the contracts to check
   * @param eseq the executable sequence that is the source of values for checking contracts
   * @param tuples the value tuples to use as input to the contracts
   * @return a {@link Check} of the first contract+tuple that did not succeed, or null if all
   *     contracts succeeded. More specifically, returns a {@link ObjectCheck} if a contract fails,
   *     an {@link InvalidExceptionCheck} if a contract throws an exception indicating that the
   *     sequence is invalid, null otherwise.
   */
  Check checkContracts(
      List<ObjectContract> contracts, ExecutableSequence eseq, TupleSet<ReferenceValue> tuples) {
    for (List<ReferenceValue> tuple : tuples.tuples()) {
      Object[] values = getValues(tuple);

      for (ObjectContract contract : contracts) {
        assert tuple.size() == contract.getArity()
            : "value tuple size "
                + tuple.size()
                + " must match contract arity "
                + contract.getArity();
        if (typesMatch(contract.getInputTypes(), tuple)) {
          // Commented out because it makes the logs too big.  Uncomment when debugging this code.
          // Log.logPrintf("Checking contract %s%n", contract.getClass());
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

  /**
   * Creates an {@code Object} array for the given value list.
   *
   * @param tuple the list of values
   * @return the Object array for the values
   */
  private static Object[] getValues(List<ReferenceValue> tuple) {
    Object[] values = new Object[tuple.size()];
    for (int i = 0; i < tuple.size(); i++) {
      values[i] = tuple.get(i).getObjectValue();
    }
    return values;
  }
}
