package randoop.test.predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static randoop.main.GenInputsAbstract.BehaviorType.ERROR;
import static randoop.main.GenInputsAbstract.BehaviorType.EXPECTED;
import static randoop.main.GenInputsAbstract.BehaviorType.INVALID;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.ExceptionalExecution;
import randoop.main.ExceptionBehaviorClassifier;
import randoop.main.GenInputsAbstract.BehaviorType;
import randoop.main.GenInputsAbstract;
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

/** Tests to check whether exception classifiers are acting as expected. */
public class ExceptionClassifierTest {

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
    GenInputsAbstract.time_limit = 0;
    GenInputsAbstract.minimize_error_test = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_null_input = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.sof_exception = BehaviorType.INVALID;
  }

  /*
   * Faked occurrence of NullPointerException should not satisfy the
   * NPEContractPredicate, which is looking for an NPE when null is input.
   */
  @Test
  public void testNoNullNPE() {
    ExceptionalExecution exec = new ExceptionalExecution(new NullPointerException(), 0);
    ExecutableSequence s = new ExecutableSequence(new Sequence());

    assertEquals(ERROR, ExceptionBehaviorClassifier.classify(exec, s));
  }

  @Test
  public void testNullNPE() {
    ExceptionalExecution exec = new ExceptionalExecution(new NullPointerException(), 0);
    Class<?> c = CUTForExceptionPredicate.class;
    ClassOrInterfaceType classType = NonParameterizedType.forClass(c);
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

    assertEquals(EXPECTED, ExceptionBehaviorClassifier.classify(exec, s));
  }

  @Test
  public void testOOM() {
    ExceptionalExecution exec = new ExceptionalExecution(new OutOfMemoryError(), 0);
    ExecutableSequence s = new ExecutableSequence(new Sequence());

    assertEquals(INVALID, ExceptionBehaviorClassifier.classify(exec, s));
  }

  @Test
  public void testFailures() {
    ExceptionalExecution assertionExec = new ExceptionalExecution(new AssertionError(), 0);
    ExceptionalExecution overflowExec = new ExceptionalExecution(new StackOverflowError(), 0);
    ExecutableSequence s = new ExecutableSequence(new Sequence());

    assertEquals(ERROR, ExceptionBehaviorClassifier.classify(assertionExec, s));

    assertEquals(INVALID, ExceptionBehaviorClassifier.classify(overflowExec, s));
  }
}
