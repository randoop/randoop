package randoop;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FailureSet {
  
  private Set<Failure> failures = new LinkedHashSet<Failure>();
  
  public static class Failure {
    public final StatementKind st;
    public final Class<?> viocls;
    public Failure(StatementKind st, Class<?> viocls) {
      this.st = st;
      this.viocls = viocls;
    }
    public boolean equals(Object o) {
      if (o == null) return false;
      if (o == this) return true;
      Failure other = (Failure)o;
      if (!st.equals(other.st)) return false;
      if (!viocls.equals(other.viocls)) return false;
      return true;
    }
    public int hashCode() {
      int hash = 7;
      hash = hash*31 + st.hashCode();
      hash = hash*31 + viocls.hashCode();
      return hash;
    }
  }

  public FailureSet(ExecutableSequence es) {
    int idx = es.getFailureIndex();
    
    if (idx < 0) {
      return;
    }
    
    for (Check obs : es.getFailures(idx)) {
      Class<?> vioCls = obs.getClass();
      StatementKind st = null;

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
            st = RMethod.getRMethod(cls.getMethod("equals", Object.class));
          } catch (Exception e) {
            throw new Error(e);
          }

          // We record this as the class of the specific objecvarst contract.
          vioCls = ex.getClass();
          
        } else {
          st = es.sequence.getStatementKind(idx);
        }

      } else {
        st = es.sequence.getStatementKind(idx);

        MSequence mseq = es.sequence.toModifiableSequence();
        List<MVariable> vars = new ArrayList<MVariable>();
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
