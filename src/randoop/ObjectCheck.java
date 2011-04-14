package randoop;

import java.util.Arrays;

/**
 * A check that checks for expected properties of one or more objects
 * generated during the execution of a {@link Sequence}, for example:
 * <p>
 * <ul>
 * <li> Checking that the objects created during execution of a sequence 
 *      respect reflexivity, transitivity and symmetry of equality.
 * <li> Checking that calling <code>toString()</code> on the objects
 *      created during execution of a sequence does not throw an exception.
 * </ul>  
 * <p>
 * An <code>ObjectCheck</code> has two parts:
 * <p>
 * <ul>
 * <li>A {@link ObjectContract} responsible for performing
 *     the actual check on a set of runtime values. For example.
 *     the class {@link EqualsReflexive} is a checker code class that,
 *     given an object <i>o</i>, calls <i>o.equals(o)</i> and checks that
 *     it returns <code>true</code>.
 *     
 * <li>A list of {@link Variable}s, which describe the specific
 *     objects in the sequence that the check is over.  </ul> <p>
 */
public class ObjectCheck implements Check {

  private static final long serialVersionUID = 7794896690777599374L;
  public final ObjectContract contract;
  public final Variable[] vars;
  public final int stmt_no;

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (!(o instanceof ObjectCheck)) {
      return false;
    }
    ObjectCheck other = (ObjectCheck) o;
    return contract.equals(other.contract) && Arrays.equals(vars, other.vars);
  }

  @Override
  public int hashCode() {
    int h = 7;
    h = h * 31 + contract.hashCode();
    h = h * 31 + Arrays.hashCode(vars);
    return h;
  }

  public ObjectCheck(ObjectContract cc, int stmt_no, Variable... vars) {
    if (cc == null) {
      throw new IllegalArgumentException("first argument cannot be null.");
    }
    if (vars.length != cc.getArity()) {
      throw new IllegalArgumentException("vars.size() != template.getArity().");
    }
    this.stmt_no = stmt_no;
    this.contract = cc;
    this.vars = new Variable[vars.length];
    int count = 0;
    for (Variable v : vars) {
      this.vars[count++] = v;
    }

  }

  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("<");
    b.append(contract.toString());
    b.append(" " + Arrays.toString(vars) + " ");
    return b.toString();
  }

  @Override
  public String toCodeStringPreStatement() {
    return "";
  }

  public String toCodeStringPostStatement() {
    return ObjectContractUtils.localizeContractCode(contract.toCodeString(), vars);
  }

  /**
   * For checks involving a primitive-lie value (primitive, String or null)
   * returns a string representation of the value. Otherwise returns
   * the name of the contract class.
   */
  @Override
  public String get_value() {
    if (contract instanceof IsNotNull) {
      return "!null";
    } else if (contract instanceof IsNull) {
      return "null";
    } else if (contract instanceof ObserverEqValue) {
      return String.format ("%s", ((ObserverEqValue)contract).value);
    } else if (contract instanceof PrimValue) {
      return ((PrimValue)contract).value.toString();
    } else {
      return contract.getClass().getName();
    }
  }

  @Override
  public boolean evaluate(Execution execution) {
    Object[] obs = ExecutableSequence.getRuntimeValuesForVars(Arrays.asList(vars), execution.theList);
    try {
      return contract.evaluate(obs);
    } catch (ThreadDeath t) {
      throw t;
    } catch (Throwable t) {
      return contract.evalExceptionMeansFailure();
    }
  }

  @Override
  public int get_stmt_no() {
    return (stmt_no);
  }

  @Override
  public String get_id() {
    return contract.get_observer_str() + " " + Arrays.toString(vars);
  }
}
