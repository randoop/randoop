package randoop.condition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import randoop.compile.SequenceClassLoader;
import randoop.compile.SequenceCompiler;
import randoop.reflection.RawSignature;

public class ConditionMethodTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testSimpleConditionMethod() {
    RawSignature signature =
        new RawSignature("", "SimpleCondition", "test", new Class<?>[] {String.class});
    Condition simple = createCondition(signature, "(String s)", "true", "// always true");
    Object[] values = new Object[] {"dummy"};
    assertTrue("condition is always true", simple.check(values));
  }

  @Test
  public void testSingleArgumentMethod() {
    RawSignature signature =
        new RawSignature("", "SingleArgumentCondition", "test", new Class<?>[] {String.class});
    Condition simple =
        createCondition(signature, "(String s)", "s.length() > 2", "// has two characters");
    assertTrue("string has more than two characters", simple.check(new Object[] {"dummy"}));
    assertFalse("string has two characters", simple.check(new Object[] {"01"}));
  }

  @Test
  public void testWrongIdentifier() {
    thrown.expect(RandoopConditionError.class);
    RawSignature signature =
        new RawSignature("", "WrongIdentifierCondition", "test", new Class<?>[] {String.class});
    Condition simple =
        createCondition(
            signature, "(String s)", "t.length() > 2", "// condition has wrong identifier");
  }

  @Test
  public void testWrongType() {
    thrown.expect(RandoopConditionError.class);
    RawSignature signature =
        new RawSignature("", "WrongTypeCondition", "test", new Class<?>[] {String.class});
    Condition simple =
        createCondition(signature, "(String s)", "s.length()", "// int is not a boolean");
  }

  @Test
  public void testErrorThrown() {
    RawSignature signature =
        new RawSignature(
            "randoop.condition",
            "ErrorThrownCondition",
            "test",
            new Class<?>[] {ConditionWithException.class});
    Condition error =
        createCondition(
            signature,
            "(randoop.condition.ConditionWithException r)",
            "r.errorPredicate()",
            "throws an Error");
    assertFalse(
        "should be false when error thrown",
        error.check(new Object[] {new ConditionWithException()}));
  }

  @Test
  public void testThrowableThrown() {
    RawSignature signature =
        new RawSignature(
            "randoop.condition",
            "ThrowableThrownCondition",
            "test",
            new Class<?>[] {ConditionWithException.class});
    Condition throwable =
        createCondition(
            signature,
            "(randoop.condition.ConditionWithException r)",
            "r.throwablePredicate()",
            "throws a Throwable");
    assertFalse(
        "should be false when exception thrown",
        throwable.check(new Object[] {new ConditionWithException()}));
  }

  private Condition createCondition(
      RawSignature signature, String declarations, String conditionText, String comment) {
    Method method =
        ConditionMethodCreator.create(signature, declarations, conditionText, getCompiler());
    return new Condition(method, comment, conditionText);
  }

  private SequenceCompiler getCompiler() {
    SequenceClassLoader sequenceClassLoader = new SequenceClassLoader(getClass().getClassLoader());
    List<String> options = new ArrayList<>();
    return new SequenceCompiler(sequenceClassLoader, options);
  }
}
