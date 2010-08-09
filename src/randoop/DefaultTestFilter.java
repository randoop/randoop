package randoop;

import java.util.LinkedHashSet;
import java.util.Set;

import plume.Pair;
import randoop.main.GenInputsAbstract;

public class DefaultTestFilter implements ITestFilter {

  public Set<Pair<StatementKind,Class<?>>> errors = new LinkedHashSet<Pair<StatementKind,Class<?>>>();
  // public Set<FailureAnalyzer.Failure> errors = new LinkedHashSet<FailureAnalyzer.Failure>();

  @Override
  public boolean outputSequence(ExecutableSequence s, FailureAnalyzer f) {
    
    if ((GenInputsAbstract.output_nonexec || !s.hasNonExecutedStatements())
        && (GenInputsAbstract.output_tests.equals(GenInputsAbstract.pass)
            || GenInputsAbstract.output_tests.equals(GenInputsAbstract.all))) {
      
      return true;
    }
    
    for (FailureAnalyzer.Failure failure : f.getFailures()) {
      
      Pair<StatementKind, Class<?>> p = new Pair<StatementKind, Class<?>>(failure.st, failure.viocls);
      if (errors.add(p)) {
        if ((GenInputsAbstract.output_nonexec || !s.hasNonExecutedStatements())
            && (GenInputsAbstract.output_tests.equals(GenInputsAbstract.fail) || GenInputsAbstract.output_tests.equals(GenInputsAbstract.all))) {
          return true;
        }
      }
    }
    return false;
  }

}
