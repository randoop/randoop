package randoop.test.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.test.predicate.ExceptionBehaviorPredicate.IS_ERROR;
import static randoop.test.predicate.ExceptionBehaviorPredicate.IS_EXPECTED;
import static randoop.test.predicate.ExceptionBehaviorPredicate.IS_INVALID;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.ExceptionalExecution;
import randoop.main.GenInputsAbstract;
import randoop.main.GenInputsAbstract.BehaviorType;
import randoop.main.OptionsCache;
import randoop.operation.ConstructorCall;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.test.DummyCheckGenerator;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.NonParameterizedType;
import randoop.types.Type;
import randoop.types.TypeTuple;

/** Tests to check whether exception predicates are acting as expected. */
public class ExceptionPredicateTest {

  private static OptionsCache optionsCache;

  @AfterClass
  public static void restore() {
    optionsCache.restoreState();
  }

  /*
   * Make sure that command-line arguments are set in expected way.
   */
  @BeforeClass
  public static void setupBeforeClass() {
    optionsCache = new OptionsCache();
    optionsCache.saveState();
    GenInputsAbstract.deterministic = true;
    GenInputsAbstract.timeLimit = 0;
    GenInputsAbstract.minimize_error_test = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_null_input = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.sof_exception = BehaviorType.INVALID;
  }

  /*
   * Predicates for behavior types
   */
  private ExceptionPredicate alwaysFalse = new AlwaysFalseExceptionPredicate();

  /*
   * Faked occurrence of NullPointerException should not satisfy the
   * NPEContractPredicate, which is looking for an NPE when null is input.
   */
  @Test
  public void testNoNullNPE() {
    ExceptionalExecution exec = new ExceptionalExecution(new NullPointerException(), 0);
    ExecutableSequence s = new ExecutableSequence(new Sequence());

    assertTrue("non-null input NPE is error", IS_ERROR.test(exec, s));
    assertFalse("non-null input NPE is not invalid", IS_INVALID.test(exec, s));
    assertFalse("non-null input NPE is expected", IS_EXPECTED.test(exec, s));
    assertFalse("no exception satisfies this predicate", alwaysFalse.test(exec, s));
  }

  @Test
  public void testNullNPE() {
    ExceptionalExecution exec = new ExceptionalExecution(new NullPointerException(), 0);
    Class<?> c = CUTForExceptionPredicate.class;
    ClassOrInterfaceType classType = new NonParameterizedType(c);
    Constructor<?> con = null;
    try {
      con = c.getDeclaredConstructor(Object.class);
    } catch (Exception e) {
      fail("test not setup correctly: " + e);
    }
    List<Type> paramTypes = new ArrayList<>();
    paramTypes.add(JavaTypes.OBJECT_TYPE);
    TypedOperation conOp =
        new TypedClassOperation(
            new ConstructorCall(con), classType, new TypeTuple(paramTypes), classType);
    Sequence seq =
        new Sequence()
            .extend(TypedOperation.createNullOrZeroInitializationForType(JavaTypes.OBJECT_TYPE));
    List<Variable> inputVariables = new ArrayList<>();
    inputVariables.add(new Variable(seq, 0));

    seq = seq.extend(conOp, inputVariables);
    ExecutableSequence s = new ExecutableSequence(seq);
    s.execute(new DummyVisitor(), new DummyCheckGenerator());

    assertFalse("null input NPE is not error", IS_ERROR.test(exec, s));
    assertFalse("null input NPE is not invalid", IS_INVALID.test(exec, s));
    assertTrue("null input NPE is expected", IS_EXPECTED.test(exec, s));
    assertFalse("no exception satisfies this predicate", alwaysFalse.test(exec, s));
  }

  @Test
  public void testOOM() {
    ExceptionalExecution exec = new ExceptionalExecution(new OutOfMemoryError(), 0);
    ExecutableSequence s = new ExecutableSequence(new Sequence());

    assertFalse("OOM is not error", IS_ERROR.test(exec, s));
    assertTrue("OOM is invalid", IS_INVALID.test(exec, s));
    assertFalse("OOM is not expected", IS_EXPECTED.test(exec, s));
    assertFalse("no exception satisfies this predicate", alwaysFalse.test(exec, s));
  }

  @Test
  public void testFailures() {
    ExceptionalExecution assertionExec = new ExceptionalExecution(new AssertionError(), 0);
    ExceptionalExecution overflowExec = new ExceptionalExecution(new StackOverflowError(), 0);
    ExecutableSequence s = new ExecutableSequence(new Sequence());

    assertTrue("AE is error", IS_ERROR.test(assertionExec, s));
    assertFalse("AE is not invalid", IS_INVALID.test(assertionExec, s));
    assertFalse("AE is not expected", IS_EXPECTED.test(assertionExec, s));
    assertFalse("no exception satisfies this predicate", alwaysFalse.test(assertionExec, s));

    assertFalse("SOE is not error", IS_ERROR.test(overflowExec, s));
    assertTrue("SOE is invalid", IS_INVALID.test(overflowExec, s));
    assertFalse("SOE is not expected", IS_EXPECTED.test(overflowExec, s));
    assertFalse("no exception satisfies this predicate", alwaysFalse.test(overflowExec, s));
  }
}
