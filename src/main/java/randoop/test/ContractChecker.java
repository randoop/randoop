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
import randoop.types.TypeTuple;
import randoop.util.Log;
import randoop.util.Randomness;
import randoop.util.TupleVisitor;

/**
 * Perform checks over a {@link randoop.util.TupleSet}.
 */
class ContractChecker implements TupleVisitor<ReferenceValue,Check> {
  /** the executable sequence that is the source of values for checking contracts */
  private final ExecutableSequence s;

  /** the list of contracts to check */
  private final List<ObjectContract> contracts;

  /**
   * Creates a contract checker for value tuples.
   * All contracts must have the same arity.
   *
   * @param s  the executable sequence that produced values in tuples
   * @param contracts  the set of contracts
   */
  ContractChecker(ExecutableSequence s, List<ObjectContract> contracts) {
    this.s = s;
    this.contracts = contracts;
  }

  /**
   * Applies the contracts of this checker to the given tuple.
   *
   * @param tuple  the value tuple to use as input to the contracts
   * @return a {@link Check} of a contract that failed on the tuple
   */
  @Override
  public Check apply(List<ReferenceValue> tuple) {
    for (ObjectContract contract : contracts) {
      assert tuple.size() == contract.getArity() : "value tuple size " + tuple.size() + " must match contract arity " + contract.getArity();
      if (typesMatch(contract.getInputTypes(), tuple)) {
        if (Log.isLoggingOn()) {
          Log.logLine("Checking contract " + contract.getClass());
        }
        Object[] values = getValues(tuple);
        Check check = checkContract(contract, values);
        if (check != null) {
          return check;
        }
      }
    }
    return null;
  }

  /**
   * Checks a contract on a particular array of values.
   *
   * @param contract  the contract
   * @param values  the input values
   * @return a {@link ObjectCheck} if the contract fails, null otherwise
   */
  private Check checkContract(ObjectContract contract, Object[] values) {
    ExecutionOutcome outcome =  ObjectContractUtils.execute(contract, values);

    if (outcome instanceof NormalExecution) {
      if (((NormalExecution)outcome).getRuntimeValue().equals(true)) {
        return null;
      }
    } else if (outcome instanceof ExceptionalExecution) {
      Throwable e = ((ExceptionalExecution)outcome).getException();
      if (Log.isLoggingOn()) {
        Log.logLine("Contract threw exception: " + e.getMessage());
      }
      if (e instanceof BugInRandoopException) {
        throw (BugInRandoopException)e;
      }
      if (! contract.evalExceptionMeansFailure()) {
        return null;
      }
    } else {
      throw new BugInRandoopException("Contract failed to execute during evaluation");
    }

    // the contract failed
    Variable[] varArray = new Variable[values.length];
    for (int i = 0; i < varArray.length; i++) {
      List<Variable> variables = s.getVariables(values[i]);
      varArray[i] = Randomness.randomMember(variables);
    }

    return new ObjectCheck(contract, varArray);
  }


  /**
   * Indicates whether the given list of values matches the types in the type tuple.
   *
   * @param inputTypes  the expected types
   * @param valueTuple  the values
   * @return true if the types of the values match the expected types
   */
  private boolean typesMatch(TypeTuple inputTypes, List<ReferenceValue> valueTuple) {
    int i = 0;
    while(i < inputTypes.size()
            && valueTuple.get(i).getType().isSubtypeOf(inputTypes.get(i))) {
      i++;
    }
    return i == inputTypes.size();
  }

  /**
   * Creates an {@code Object} array for the given value list.
   *
   * @param tuple  the list of values
   * @return the Object array for the values
   */
  private Object[] getValues(List<ReferenceValue> tuple) {
    Object[] values = new Object[tuple.size()];
    for (int i = 0; i < tuple.size(); i++) {
      values[i] = tuple.get(i).getObjectValue();
    }
    return values;
  }
}
