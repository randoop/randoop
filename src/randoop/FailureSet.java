package randoop;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.operation.MethodCall;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.MutableSequence;
import randoop.sequence.MutableVariable;
import randoop.sequence.Statement;
import randoop.sequence.Variable;

/**
 * FailureSet represents a set of failures that occurred in the execution of a particular
 * {@link ExecutableSequence}. 
 *
 */
public class FailureSet {
  
  private Set<Failure> failures = new LinkedHashSet<Failure>();
  
  /**
   * Represents an execution failure, and holds the
   * statement where the failure occurred, and the type of contract that 
   * was violated.
   *
   */
  public static class Failure {
    
    /**
     * The statement in the {@link ExecutableSequence} where the failure occurred.
     */
    public final Statement statement;
    
    /**
     * The type of the {@link ObjectContract} that was violated.
     */
    public final Class<?> violationClass;
    
    public Failure(Statement st, Class<?> viocls) {
      this.statement = st;
      this.violationClass = viocls;
    }
    
    public boolean equals(Object o) {
      if (o instanceof Failure) {    
        Failure other = (Failure)o;
        return statement.equals(other.statement) && violationClass.equals(other.violationClass);
      }
      return false;
    }
    
    public int hashCode() {
      int hash = 7;
      hash = hash*31 + statement.hashCode();
      hash = hash*31 + violationClass.hashCode();
      return hash;
    }
  }

  /**
   * Constructs the failure set for the given executable sequence.
   * 
   * @param es  the executable sequence
   */
  public FailureSet(ExecutableSequence es) {
    int idx = es.getFailureIndex();
    
    if (idx < 0) {
      return;
    }
    
    for (Check obs : es.getFailures(idx)) {
      Class<?> vioCls = obs.getClass();
      Statement st = null;

      if (obs instanceof ObjectCheck && ((ObjectCheck)obs).contract instanceof ObjectContract) {

        ObjectContract ex = ((ObjectCheck)obs).contract;
        int equalsReceiver = ((ObjectCheck)obs).vars[0].index;

        if (ex instanceof EqualsReflexive 
            || ex instanceof EqualsToNullRetFalse
            || ex instanceof EqualsHashcode
            || ex instanceof EqualsSymmetric) {

          ExecutionOutcome res = es.getResult(equalsReceiver);
          assert res instanceof NormalExecution;
          Object runtimeval = ((NormalExecution) res).getRuntimeValue();
          assert runtimeval != null;

          Class<?> cls = runtimeval.getClass();
          // We record this as an error in the equals method.
          try {
            st = new Statement(MethodCall.createMethodCall(cls.getMethod("equals", Object.class)));
          } catch (Exception e) {
            throw new Error(e);
          }

          // We record this as the class of the specific objecvarst contract.
          vioCls = ex.getClass();
          
        } else {
          st = es.sequence.getStatement(idx);
        }

      } else {
        st = es.sequence.getStatement(idx);

        MutableSequence mseq = es.sequence.toModifiableSequence();
        List<MutableVariable> vars = new ArrayList<MutableVariable>();
        for (Variable v : es.sequence.getInputs(idx)) {
          vars.add(mseq.getVariable(v.index));
        }
      }
      
      
      assert st != null;
      
      failures.add(new Failure(st, vioCls));
    }
  }

  public Set<Failure> getFailures() {
    return failures;
  }

}
