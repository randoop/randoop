package randoop;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FailureAnalyzer {
  
  private Set<Failure> failures = new LinkedHashSet<Failure>();
  
  public static class Failure {
    public final StatementKind st;
    public final Class<?> viocls;
    public final int reachablesize;
    public Failure(StatementKind st, Class<?> viocls, int reachablesize) {
      this.st = st;
      this.viocls = viocls;
      this.reachablesize = reachablesize;
    }
    public boolean equals(Object o) {
      if (o == null) return false;
      if (o == this) return true;
      Failure other = (Failure)o;
      if (!st.equals(other.st)) return false;
      if (!viocls.equals(other.viocls)) return false;
      if (reachablesize != other.reachablesize) return false;
      return true;
    }
    public int hashCode() {
      int hash = 7;
      hash = hash*31 + st.hashCode();
      hash = hash*31 + viocls.hashCode();
      hash = hash*31 + new Integer(reachablesize).hashCode();
      return hash;
    }
  }

  public FailureAnalyzer(ExecutableSequence es) {
    int idx = es.getObservationIndex(ContractViolation.class);
    
    if (idx < 0) {
      return;
    }
    
    for (Observation obs : es.getObservations(idx, ContractViolation.class)) {
      Class<?> vioCls = obs.getClass();
      StatementKind st = null;
      int numInf = -1;
      
      if (obs instanceof ExpressionEqFalse) {
        ExpressionEqFalse ex = (ExpressionEqFalse)obs;
        assert ex.objcontract.equals(EqualsToItself.class) || ex.objcontract.equals(EqualsToNull.class)
          || ex.objcontract.equals(EqualsHashcode.class) || ex.objcontract.equals(EqualsSymmetric.class);


        MSequence mseq = es.sequence.toModifiableSequence();
        List<MVariable> vars = new ArrayList<MVariable>();
        for (Variable v : ex.vars) {
          vars.add(mseq.getVariable(v.index));
        }
        numInf = mseq.numInfluencingStatements(idx, vars);

        int equalsReceiver = ex.vars.get(0).index;
        ExecutionOutcome res = es.getResult(equalsReceiver);
        assert res instanceof NormalExecution;
        Object runtimeval = ((NormalExecution)res).getRuntimeValue();
        assert runtimeval != null;

        Class<?> cls = runtimeval.getClass();
        // We record this as an error in the equals method.
        try {
          st = RMethod.getRMethod(cls.getMethod("equals", Object.class));
        } catch (Exception e) {
          throw new Error(e);
        }

        // We record this as the class of the specific objecvarst contract.
        vioCls = ex.objcontract;

      } else {
        assert obs instanceof StatementThrowsNPE;
        st = es.sequence.getStatementKind(idx);

        MSequence mseq = es.sequence.toModifiableSequence();
        List<MVariable> vars = new ArrayList<MVariable>();
        for (Variable v : es.sequence.getInputs(idx)) {
          vars.add(mseq.getVariable(v.index));
        }
        numInf = mseq.numInfluencingStatements(idx, vars);
      }
      assert st != null;
      assert numInf >= 0;
      
      failures.add(new Failure(st, vioCls, numInf));
    }
  }

  public Set<Failure> getFailures() {
    return failures;
  }

}
