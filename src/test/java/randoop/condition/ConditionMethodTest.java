package randoop.condition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import randoop.compile.SequenceClassLoader;
import randoop.compile.SequenceCompiler;

public class ConditionMethodTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testSimpleConditionMethod() {
    Condition simple = createCondition("", "(String s)", "true", "// always true");
    Object[] values = new Object[] {"dummy"};
    assertTrue("condition is always true", simple.check(values));
  }

  @Test
  public void testSingleArgumentMethod() {
    Condition simple = createCondition("", "(String s)", "s.length() > 2", "// has two characters");
    assertTrue("string has more than two characters", simple.check(new Object[] {"dummy"}));
    assertFalse("string has two characters", simple.check(new Object[] {"01"}));
  }

  @Test
  public void testWrongIdentifier() {
    thrown.expect(RandoopConditionError.class);

    Condition simple =
        createCondition("", "(String s)", "t.length() > 2", "// condition has wrong identifier");
  }

  @Test
  public void testWrongType() {
    thrown.expect(RandoopConditionError.class);
    Condition simple = createCondition("", "(String s)", "s.length()", "// int is not a boolean");
  }

  private Condition createCondition(
      String packageName, String signature, String conditionText, String comment) {
    Method method =
        ConditionMethodCreator.create(packageName, signature, conditionText, getCompiler());
    return new Condition(method, comment, conditionText);
  }

  private SequenceCompiler getCompiler() {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    SequenceClassLoader sequenceClassLoader = new SequenceClassLoader(getClass().getClassLoader());
    List<String> options = new ArrayList<>();
    return new SequenceCompiler(sequenceClassLoader, options, diagnostics);
  }
}
