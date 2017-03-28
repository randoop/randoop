package randoop.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.condition.specification.Guard;
import randoop.condition.specification.Identifiers;
import randoop.condition.specification.Operation;
import randoop.condition.specification.OperationSpecification;
import randoop.condition.specification.PostSpecification;
import randoop.condition.specification.PreSpecification;
import randoop.condition.specification.Property;
import randoop.condition.specification.ThrowsSpecification;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.test.Check;
import randoop.test.DummyCheckGenerator;
import randoop.test.TestCheckGenerator;
import randoop.types.JavaTypes;

public class OperationConditionTest {

  @Test
  public void conditionTest() {
    OperationConditions conditions = getConditions();

    ClassWithConditions receiver = new ClassWithConditions(5);

    Object[] preValues;
    preValues = new Object[] {receiver, -1};
    assertFalse("should fail param condition", conditions.checkPreconditions(preValues));

    preValues = new Object[] {receiver, 1};
    assertTrue("should pass param condition", conditions.checkPreconditions(preValues));
    List<TestCheckGenerator> throwsGen = conditions.getThrowsCheckGenerator(preValues);
    assertTrue("should not be a throws generator", throwsGen.isEmpty());
    List<TestCheckGenerator> retGen = conditions.getReturnCheckGenerator(preValues);
    assertFalse("should be a return generator", retGen.isEmpty());

    preValues = new Object[] {receiver, 6};
    assertTrue("should pass param condition", conditions.checkPreconditions(preValues));
    throwsGen = conditions.getThrowsCheckGenerator(preValues);
    assertTrue("should not be a throws generator", throwsGen.isEmpty());
    retGen = conditions.getReturnCheckGenerator(preValues);
    assertTrue("should be a return generator", !retGen.isEmpty());

    preValues = new Object[] {receiver, 11};
    assertTrue("should pass param condition", conditions.checkPreconditions(preValues));
    throwsGen = conditions.getThrowsCheckGenerator(preValues);
    assertTrue("should not be a throws generator", throwsGen.isEmpty());
    retGen = conditions.getReturnCheckGenerator(preValues);
    assertTrue("should be a return generator", !retGen.isEmpty());

    preValues = new Object[] {receiver, 16};
    assertTrue("should pass param condition", conditions.checkPreconditions(preValues));
    throwsGen = conditions.getThrowsCheckGenerator(preValues);
    assertTrue("should not be a throws generator", throwsGen.isEmpty());
    retGen = conditions.getReturnCheckGenerator(preValues);
    assertTrue("should be a return generator", !retGen.isEmpty());

    preValues = new Object[] {receiver, 21};
    assertTrue("should pass param condition", conditions.checkPreconditions(preValues));
    throwsGen = conditions.getThrowsCheckGenerator(preValues);
    assertTrue("should be a throws generator", throwsGen != null);
  }

  @Test
  public void sequenceTest() {
    ExecutableSequence es = createSequence(-1);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertTrue("should be invalid sequence", es.hasInvalidBehavior());

    es = createSequence(1);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertFalse("should be valid sequence", es.hasInvalidBehavior());
    assertFalse("should not have failure", es.hasFailure());

    for (Check check : es.getChecks().get().keySet()) {
      assertEquals(
          "should check for ONE",
          "randoop.condition.PostCondition(x2.equals(ClassWithConditions.Range.ONE))",
          check.getValue());
    }

    es = createSequence(6);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertFalse("should be valid sequence", es.hasInvalidBehavior());
    assertFalse("should not have failure", es.hasFailure());

    for (Check check : es.getChecks().get().keySet()) {
      assertEquals(
          "should check for TWO",
          "randoop.condition.PostCondition(x2.equals(ClassWithConditions.Range.TWO))",
          check.getValue());
    }

    es = createSequence(11);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertFalse("should be valid sequence", es.hasInvalidBehavior());
    assertTrue("should have failure", es.hasFailure());

    for (Check check : es.getChecks().get().keySet()) {
      assertEquals(
          "should check for THREE",
          "randoop.condition.PostCondition(x2.equals(ClassWithConditions.Range.THREE))",
          check.getValue());
    }

    es = createSequence(16);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertFalse("should be valid sequence", es.hasInvalidBehavior());
    assertFalse("should not have failure", es.hasFailure());

    for (Check check : es.getChecks().get().keySet()) {
      assertEquals(
          "should check for FOUR",
          "randoop.condition.PostCondition(x2.equals(ClassWithConditions.Range.FOUR))",
          check.getValue());
    }

    es = createSequence(21);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertFalse("should be valid sequence", es.hasInvalidBehavior());
    assertFalse("should not have failure", es.hasFailure());
    assertEquals(
        "should throw exception ",
        "java.lang.IllegalArgumentException",
        es.getChecks().getExceptionCheck().getExceptionName());
  }

  private ExecutableSequence createSequence(int value) {
    Class<?> c = ClassWithConditions.class;
    Constructor<?> reflectionConstructor = null;
    try {
      reflectionConstructor = c.getConstructor(int.class);
    } catch (NoSuchMethodException e) {
      fail("could not load constructor");
    }
    TypedClassOperation constructorOp = TypedOperation.forConstructor(reflectionConstructor);
    Method method = null;
    try {
      method = c.getDeclaredMethod("category", int.class);
    } catch (NoSuchMethodException e) {
      fail("couldn't load method");
    }
    TypedClassOperation methodOp = TypedOperation.forMethod(method);
    methodOp.addConditions(getConditions());

    Sequence sequence = new Sequence();
    sequence = sequence.extend(TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, 5));
    sequence = sequence.extend(constructorOp, sequence.getLastVariable());
    sequence =
        sequence.extend(TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, value));
    List<Variable> variables = new ArrayList<>();
    variables.add(sequence.getVariable(sequence.size() - 2));
    variables.add(sequence.getVariable(sequence.size() - 1));
    sequence = sequence.extend(methodOp, variables);

    return new ExecutableSequence(sequence);
  }

  /**
   * Creates an {@link OperationSpecification}, places it in a {@link SpecificationCollection}, and
   * gets the {@link OperationConditions}. Effectively, translating the specifications to
   * conditions.
   *
   * @return the {@link OperationConditions} object for {@link ClassWithConditions#category(int)}
   */
  private OperationConditions getConditions() {
    Class<?> c = ClassWithConditions.class;
    Method method = null;
    try {
      method = c.getDeclaredMethod("category", int.class);
    } catch (NoSuchMethodException e) {
      fail("could not load method");
    }

    List<String> paramNames = new ArrayList<>();
    paramNames.add("value");
    OperationSpecification spec =
        new OperationSpecification(Operation.getOperation(method), new Identifiers(paramNames));

    List<PreSpecification> preSpecifications = new ArrayList<>();
    Guard paramGuard = new Guard("positive", "value > 0");
    PreSpecification paramSpec = new PreSpecification("must be positive", paramGuard);
    preSpecifications.add(paramSpec);
    spec.addParamSpecifications(preSpecifications);

    List<ThrowsSpecification> throwsSpecifications = new ArrayList<>();
    Guard throwsGuard = new Guard("greater than 4*getValue()", "value >= 4*receiver.getValue()");
    ThrowsSpecification throwsSpec =
        new ThrowsSpecification(
            "should be less than 4*getValue", throwsGuard, "java.lang.IllegalArgumentException");
    throwsSpecifications.add(throwsSpec);
    spec.addThrowsSpecifications(throwsSpecifications);

    List<PostSpecification> postSpecifications = new ArrayList<>();
    Guard retGuard;
    Property retProperty;
    PostSpecification returnSpec;

    retGuard = new Guard("value in first range", "value < receiver.getValue()");
    retProperty = new Property("return ONE", "result.equals(ClassWithConditions.Range.ONE)");
    returnSpec = new PostSpecification("value in first range", retGuard, retProperty);
    postSpecifications.add(returnSpec);

    retGuard = new Guard("value in second range", "value < 2*receiver.getValue()");
    retProperty = new Property("return TWO", "result.equals(ClassWithConditions.Range.TWO)");
    returnSpec = new PostSpecification("value in second range", retGuard, retProperty);
    postSpecifications.add(returnSpec);

    retGuard = new Guard("value in third range", "value < 3*receiver.getValue()");
    retProperty = new Property("return THREE", "result.equals(ClassWithConditions.Range.THREE)");
    returnSpec = new PostSpecification("value in third range", retGuard, retProperty);
    postSpecifications.add(returnSpec);

    retGuard = new Guard("otherwise", "true");
    retProperty = new Property("return FOUR", "result.equals(ClassWithConditions.Range.FOUR)");
    returnSpec = new PostSpecification("otherwise, return FOUR", retGuard, retProperty);
    postSpecifications.add(returnSpec);
    spec.addReturnSpecifications(postSpecifications);

    Map<AccessibleObject, OperationSpecification> specMap = new HashMap<>();
    specMap.put(method, spec);
    SpecificationCollection collection = new SpecificationCollection(specMap);
    return collection.getOperationConditions(method);
  }
}
