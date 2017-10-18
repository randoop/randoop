package randoop.test;

import java.util.List;
import randoop.BugInRandoopException;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.contract.ObjectContract;
import randoop.contract.ObjectContractUtils;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ReferenceValue;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.Log;
import randoop.util.Randomness;
import randoop.util.TupleVisitor;

/** Perform checks over a {@link randoop.util.TupleSet}. */
class ContractChecker implements TupleVisitor<ReferenceValue, Check> {
  /** the executable sequence that is the source of values for checking contracts */
  private final ExecutableSequence eseq;

  /** the list of contracts to check */
  private final List<ObjectContract> contracts;

  /**
   * Creates a contract checker for value tuples. All contracts must have the same arity.
   *
   * @param eseq the executable sequence that produced values in tuples
   * @param contracts the set of contracts
   */
  ContractChecker(ExecutableSequence eseq, List<ObjectContract> contracts) {
    this.eseq = eseq;
    this.contracts = contracts;
  }

  /**
   * Applies the contracts of this checker to the given tuple.
   *
   * @param tuple the value tuple to use as input to the contracts
   * @return a {@link Check} of the first contract that failed on the tuple, or null if no contracts
   *     failed
   */
  @Override
  public Check apply(List<ReferenceValue> tuple) {
    return checkContracts(tuple);
  }

  /**
   * Applies the contracts of this checker to the given tuple.
   *
   * @param tuple the value tuple to use as input to the contracts
   * @return a {@link Check} of the first contract that failed on the tuple, or null if no contracts
   *     failed
   */
  public Check checkContracts(List<ReferenceValue> tuple) {
    for (ObjectContract contract : contracts) {
      assert tuple.size() == contract.getArity()
          : "value tuple size "
              + tuple.size()
              + " must match contract arity "
              + contract.getArity();
      // if (Randomness.selectionLog.enabled() && Randomness.verbosity > 0) {
      //   Randomness.selectionLog.log("ContractChecker.apply: considering contract=%s%n", contract);
      // }
      if (typesMatch(contract.getInputTypes(), tuple)) {
        if (Log.isLoggingOn()) {
          Log.logLine("Checking contract " + contract.getClass());
        }
        Object[] values = getValues(tuple);
        Check check = checkContract(contract, eseq, values);
        if (check != null) {
          return check;
        }
      }
    }
    return null;
  }

  // TODO: should this be in ObjectContract instead?
  /**
   * Checks a contract on a particular array of values.
   *
   * @param eseq the executable sequence that is the source of values for checking contracts
   * @param contract the contract
   * @param values the input values
   * @return a {@link ObjectCheck} if the contract fails, null otherwise
   */
  public static Check checkContract(
      ObjectContract contract, ExecutableSequence eseq, Object[] values) {
    // if (Randomness.selectionLog.enabled() && Randomness.verbosity > 0) {
    //   Randomness.selectionLog.log("ContractChecker.checkContract: contract=%s%n", contract);
    //   Randomness.selectionLog.log("  values (%d) =%n", values.length);
    //   for (Object value : values) {
    //     Randomness.selectionLog.log(
    //         "  %s @%s%n", toStringHandleExceptions(value), System.identityHashCode(value));
    //   }
    // }

    ExecutionOutcome outcome = ObjectContractUtils.execute(contract, values);

    if (Log.isLoggingOn()) {
      Log.logLine("Executed contract " + contract.getClass());
      Log.logLine(" Contract outcome " + outcome);
    }

    if (outcome instanceof NormalExecution) {
      if (((NormalExecution) outcome).getRuntimeValue().equals(true)) {
        return null;
      }
    } else if (outcome instanceof ExceptionalExecution) {
      Throwable e = ((ExceptionalExecution) outcome).getException();
      if (Log.isLoggingOn()) {
        Log.logLine(
            String.format(
                "ContractChecker.checkContract(): Contract %s threw exception of class %s with message %s",
                contract, e.getClass(), e.getMessage()));
      }
      if (e instanceof BugInRandoopException) {
        throw (BugInRandoopException) e;
      }
      // ***** TODO: determine what the exception is
    } else {
      throw new BugInRandoopException(
          "Contract " + contract + " failed to execute during evaluation");
    }

    // the contract failed
    Variable[] varArray = new Variable[values.length];
    for (int i = 0; i < varArray.length; i++) {
      List<Variable> variables = eseq.getVariables(values[i]);
      varArray[i] = Randomness.randomMember(variables);
      // if (Randomness.selectionLog.enabled() && Randomness.verbosity > 0) {
      //   Randomness.selectionLog.log(
      //       "values[%d] = %s @%s%n",
      //       i, toStringHandleExceptions(values[i]), System.identityHashCode(values[i]));
      //   Randomness.selectionLog.log("  candidate variables = %s%n", variables);
      //   Randomness.selectionLog.log(
      //       "  varArray[%d] = %s @%s%n", i, varArray[i], System.identityHashCode(varArray[i]));
      // }
    }

    return new ObjectCheck(contract, varArray);
  }

  // The toString() of class Buggy throws an exception.
  String toStringHandleExceptions(Object o) {
    try {
      return o.toString();
    } catch (Throwable t) {
      return "of " + o.getClass() + " with identityHashCode=@" + System.identityHashCode(o);
    }
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
