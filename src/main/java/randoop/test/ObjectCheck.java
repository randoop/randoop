package randoop.test;

import java.util.Arrays;
import java.util.Objects;
import randoop.contract.ObjectContract;
import randoop.contract.ObjectContractUtils;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;

/**
 * A check that checks for expected properties of one or more objects generated during the execution
 * of a {@link Sequence}, for example:
 *
 * <ul>
 *   <li>Checking that the objects created during execution of a sequence respect reflexivity,
 *       transitivity and symmetry of equality.
 *   <li>Checking that calling {@code toString()} on the objects created during execution of a
 *       sequence does not throw an exception.
 * </ul>
 *
 * <p>An {@code ObjectCheck} has two parts:
 *
 * <ul>
 *   <li>A {@link randoop.contract.ObjectContract} responsible for performing the actual check on a
 *       set of runtime values. For example. the class {@link randoop.contract.EqualsReflexive} is a
 *       checker code class that, given an object <i>o</i>, calls <i>o.equals(o)</i> and checks that
 *       it returns {@code true}.
 *   <li>A list of {@link Variable}s, which describe the specific objects in the sequence that the
 *       check is over.
 * </ul>
 */
public class ObjectCheck implements Check {

  /** The contract that is checked. */
  private final ObjectContract contract;

  /** The variables for the contract. */
  private final Variable[] vars;

  /**
   * Creates an {@link ObjectCheck} for the given contract using the variables as input.
   *
   * @param contract the contract to check
   * @param vars the input variables for the created check
   */
  public ObjectCheck(ObjectContract contract, Variable... vars) {
    if (contract == null) {
      throw new IllegalArgumentException("first argument cannot be null.");
    }
    if (vars.length != contract.getArity()) {
      throw new IllegalArgumentException("vars.size() != template.getArity().");
    }
    this.contract = contract;
    this.vars = new Variable[vars.length];
    int count = 0;
    for (Variable v : vars) {
      this.vars[count++] = v;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof ObjectCheck)) {
      return false;
    }
    ObjectCheck other = (ObjectCheck) o;
    return contract.equals(other.contract) && Arrays.equals(vars, other.vars);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contract, Arrays.hashCode(vars));
  }

  @Override
  public String toString() {
    return "<" + contract.toString() + " " + Arrays.toString(vars) + ">";
  }

  @Override
  public String toCodeStringPreStatement() {
    return "";
  }

  @Override
  public String toCodeStringPostStatement() {
    return ObjectContractUtils.localizeContractCode(contract.toCodeString(), vars);
  }
}
