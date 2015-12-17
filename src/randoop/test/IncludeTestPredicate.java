package randoop.test;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import randoop.sequence.ExecutableSequence;
import randoop.sequence.Variable;
import randoop.util.predicate.DefaultPredicate;

public class IncludeTestPredicate extends DefaultPredicate<ExecutableSequence> {
  
  private Pattern testClasses;

  public IncludeTestPredicate(Pattern testClasses) {
    this.testClasses = testClasses;
  }

  @Override
  public boolean test(ExecutableSequence s) {
    List<Variable> vars = s.sequence.getAllVariables();
    Iterator<Variable> v_i = vars.iterator();
    if (v_i.hasNext()) {
      Variable v = v_i.next();
      while (v_i.hasNext() && ! (testClasses.matcher(v.getType().getName()).matches())) {
        v = v_i.next();
      }
      return testClasses.matcher(v.getType().getName()).matches();
    }
    return true;
  }

}
