package randoop.test;

import java.util.List;
import randoop.contract.ObjectContract;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ReferenceValue;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeTuple;

/** Perform contract checks. */
class ContractChecker {
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
