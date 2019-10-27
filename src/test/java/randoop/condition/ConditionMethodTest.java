package randoop.condition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import org.junit.Rule;
import org.junit.Test;
import randoop.compile.SequenceCompiler;
import randoop.main.GenInputsAbstract;
import randoop.reflection.RawSignature;

public class ConditionMethodTest {

  @SuppressWarnings("deprecation") // ExpectedException deprecated in JUnit 4.12, replaced in 4.13.
  @Rule
  public org.junit.rules.ExpectedException thrown = org.junit.rules.ExpectedException.none();

  @Test
  public void testSimpleConditionMethod() {
    RawSignature signature =
        new RawSignature(null, "SimpleCondition", "test", new Class<?>[] {String.class});
    ExecutableBooleanExpression simple =
        createCondition(signature, "(String s)", "true", "// always true");
    Object[] values = new Object[] {"dummy"};
    assertTrue("condition is always true", simple.check(values));
  }

  @Test
  public void testSingleArgumentMethod() {
    RawSignature signature =
        new RawSignature(null, "SingleArgumentCondition", "test", new Class<?>[] {String.class});
    ExecutableBooleanExpression simple =
        createCondition(signature, "(String s)", "s.length() > 2", "// has two characters");
    assertTrue("string has more than two characters", simple.check(new Object[] {"dummy"}));
    assertFalse("string has two characters", simple.check(new Object[] {"01"}));
  }

  @Test
  public void testWrongIdentifier() {
    thrown.expect(RandoopSpecificationError.class);
    RawSignature signature =
        new RawSignature(null, "WrongIdentifierCondition", "test", new Class<?>[] {String.class});
    createCondition(signature, "(String s)", "t.length() > 2", "// condition has wrong identifier");
  }

  @Test
  public void testWrongType() {
    thrown.expect(RandoopSpecificationError.class);
    RawSignature signature =
        new RawSignature(null, "WrongTypeCondition", "test", new Class<?>[] {String.class});
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
    ExecutableBooleanExpression error =
        createCondition(
            signature,
            "(randoop.condition.ConditionWithException r)",
            "r.errorPredicate()",
            "throws an Error");

    boolean old_ignore_condition_exception = GenInputsAbstract.ignore_condition_exception;
    GenInputsAbstract.ignore_condition_exception = true;
    try {
      assertFalse(
          "should be false when error thrown",
          error.check(new Object[] {new ConditionWithException()}));
    } finally {
      GenInputsAbstract.ignore_condition_exception = old_ignore_condition_exception;
    }
  }

  @Test
  public void testThrowableThrown() {
    RawSignature signature =
        new RawSignature(
            "randoop.condition",
            "ThrowableThrownCondition",
            "test",
            new Class<?>[] {ConditionWithException.class});
    ExecutableBooleanExpression throwable =
        createCondition(
            signature,
            "(randoop.condition.ConditionWithException r)",
            "r.throwablePredicate()",
            "throws a Throwable");

    boolean old_ignore_condition_exception = GenInputsAbstract.ignore_condition_exception;
    GenInputsAbstract.ignore_condition_exception = true;
    try {
      assertFalse(
          "should be false when exception thrown",
          throwable.check(new Object[] {new ConditionWithException()}));
    } finally {
      GenInputsAbstract.ignore_condition_exception = old_ignore_condition_exception;
    }
  }

  private ExecutableBooleanExpression createCondition(
      RawSignature signature, String declarations, String conditionText, String comment) {
    Method method =
        ExecutableBooleanExpression.createMethod(
            signature, declarations, conditionText, getCompiler());
    return new ExecutableBooleanExpression(method, comment, conditionText);
  }

  private SequenceCompiler getCompiler() {
    return new SequenceCompiler();
  }
}
