package randoop.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.condition.specification.Guard;
import randoop.condition.specification.Identifiers;
import randoop.condition.specification.OperationSignature;
import randoop.condition.specification.OperationSpecification;
import randoop.condition.specification.Postcondition;
import randoop.condition.specification.Precondition;
import randoop.condition.specification.Property;
import randoop.condition.specification.ThrowsCondition;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.test.Check;
import randoop.test.DummyCheckGenerator;
import randoop.test.ExpectedExceptionGenerator;
import randoop.test.PostConditionCheck;
import randoop.test.PostConditionCheckGenerator;
import randoop.test.TestCheckGenerator;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.util.MultiMap;

public class OperationSpecificationTest {
  @Test
  public void conditionTest() throws NoSuchMethodException {
    Class<?> c = ClassWithConditions.class;
    Method method = c.getDeclaredMethod("category", int.class);
    ExecutableSpecification execSpec = getMethodSpecification(method);

    ClassWithConditions receiver = new ClassWithConditions(5);

    Object[] preValues = new Object[] {receiver, -1};
    ExpectedOutcomeTable table = execSpec.checkPrestate(preValues);
    assertTrue("should fail param condition", table.isInvalidCall());

    preValues = new Object[] {receiver, 1};
    table = execSpec.checkPrestate(preValues);
    assertFalse("should pass param condition", table.isInvalidCall());

    TestCheckGenerator gen = table.addPostCheckGenerator(new DummyCheckGenerator());
    assertFalse(
        "should not be a exception check generator",
        gen.hasGenerator(ExpectedExceptionGenerator.class));
    assertTrue(
        "should be a post-condition check generator",
        gen.hasGenerator(PostConditionCheckGenerator.class));

    preValues = new Object[] {receiver, 6};
    table = execSpec.checkPrestate(preValues);
    gen = table.addPostCheckGenerator(new DummyCheckGenerator());
    assertFalse("should pass param condition", table.isInvalidCall());
    assertFalse(
        "should not be a throws generator", gen.hasGenerator(ExpectedExceptionGenerator.class));
    assertTrue("should be a return generator", gen.hasGenerator(PostConditionCheckGenerator.class));

    preValues = new Object[] {receiver, 11};
    table = execSpec.checkPrestate(preValues);
    gen = table.addPostCheckGenerator(new DummyCheckGenerator());
    assertTrue("should pass param condition", !table.isInvalidCall());
    assertFalse(
        "should not be a throws generator", gen.hasGenerator(ExpectedExceptionGenerator.class));
    assertTrue("should be a return generator", gen.hasGenerator(PostConditionCheckGenerator.class));

    preValues = new Object[] {receiver, 16};
    table = execSpec.checkPrestate(preValues);
    gen = table.addPostCheckGenerator(new DummyCheckGenerator());
    assertTrue("should pass param condition", !table.isInvalidCall());
    assertFalse(
        "should not be a throws generator", gen.hasGenerator(ExpectedExceptionGenerator.class));
    assertTrue("should be a return generator", gen.hasGenerator(PostConditionCheckGenerator.class));

    preValues = new Object[] {receiver, 21};
    table = execSpec.checkPrestate(preValues);
    gen = table.addPostCheckGenerator(new DummyCheckGenerator());
    assertTrue("should pass param condition", !table.isInvalidCall());
    assertFalse(
        "should be a return generator", gen.hasGenerator(PostConditionCheckGenerator.class));
    assertTrue("should be a throws generator", gen.hasGenerator(ExpectedExceptionGenerator.class));
  }

  @Test
  public void constructorSequenceTest() throws NoSuchMethodException {
    ExecutableSequence es = createConstructorSequence(-1);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertTrue("should be invalid sequence", es.hasInvalidBehavior());

    es = createConstructorSequence(5);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertTrue("should have failure", es.hasFailure());
  }

  @Test
  public void methodSequenceTest() throws NoSuchMethodException {
    ExecutableSequence es = createCategorySequence(-1);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertTrue("should be invalid sequence", es.hasInvalidBehavior());

    es = createCategorySequence(1);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertFalse("should be valid sequence", es.hasInvalidBehavior());
    assertFalse("should not have failure", es.hasFailure());

    for (Check check : es.getChecks().checks()) {
      assertTrue("should be post-condition check", check instanceof PostConditionCheck);
      PostConditionCheck postConditionCheck = (PostConditionCheck) check;
      for (ExecutableBooleanExpression condition : postConditionCheck.getPostConditions()) {
        assertEquals(
            "should check for ONE",
            "x2.equals(ClassWithConditions.Range.ONE)",
            condition.getContractSource());
      }
    }

    es = createCategorySequence(6);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertFalse("should be valid sequence", es.hasInvalidBehavior());
    assertFalse("should not have failure", es.hasFailure());

    for (Check check : es.getChecks().checks()) {
      assertTrue("should be post-condition check", check instanceof PostConditionCheck);
      PostConditionCheck postConditionCheck = (PostConditionCheck) check;
      for (ExecutableBooleanExpression condition : postConditionCheck.getPostConditions()) {
        assertEquals(
            "should check for TWO",
            "x2.equals(ClassWithConditions.Range.TWO)",
            condition.getContractSource());
      }
    }

    es = createCategorySequence(11);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertFalse("should be valid sequence", es.hasInvalidBehavior());
    assertTrue("should have failure", es.hasFailure());

    for (Check check : es.getChecks().checks()) {
      assertTrue("should be post-condition check", check instanceof PostConditionCheck);
      PostConditionCheck postConditionCheck = (PostConditionCheck) check;
      for (ExecutableBooleanExpression condition : postConditionCheck.getPostConditions()) {
        assertEquals(
            "should check for THREE",
            "x2.equals(ClassWithConditions.Range.THREE)",
            condition.getContractSource());
      }
    }

    es = createCategorySequence(16);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertFalse("should be valid sequence", es.hasInvalidBehavior());
    assertFalse("should not have failure", es.hasFailure());

    for (Check check : es.getChecks().checks()) {
      assertTrue("should be post-condition check", check instanceof PostConditionCheck);
      PostConditionCheck postConditionCheck = (PostConditionCheck) check;
      for (ExecutableBooleanExpression condition : postConditionCheck.getPostConditions()) {
        assertEquals(
            "should check for FOUR",
            "x2.equals(ClassWithConditions.Range.FOUR)",
            condition.getContractSource());
      }
    }

    es = createCategorySequence(21);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertFalse("should be valid sequence", es.hasInvalidBehavior());
    assertFalse("should not have failure", es.hasFailure());
    assertEquals(
        "should throw exception ",
        "java.lang.IllegalArgumentException",
        es.getChecks().getExceptionCheck().getExceptionName());
  }

  @Test
  public void testMultipleThrows() throws NoSuchMethodException {
    ExecutableSequence es = createBadnessSequence();
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    assertFalse("should be valid sequence", es.hasInvalidBehavior());
    assertFalse("should not have failures", es.hasFailure());
  }

  private ExecutableSequence createConstructorSequence(int initValue) throws NoSuchMethodException {
    Class<?> c = ClassWithConditions.class;
    Constructor<?> reflectionConstructor = c.getConstructor(int.class);
    TypedClassOperation constructorOp = TypedOperation.forConstructor(reflectionConstructor);
    constructorOp.setExecutableSpecification(getConstructorConditions(reflectionConstructor));
    Sequence sequence = new Sequence();
    sequence =
        sequence.extend(
            TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, initValue));
    sequence = sequence.extend(constructorOp, sequence.getLastVariable());
    return new ExecutableSequence(sequence);
  }

  private ExecutableSequence createCategorySequence(int value) throws NoSuchMethodException {
    Class<?> c = ClassWithConditions.class;
    Constructor<?> reflectionConstructor = c.getConstructor(int.class);
    TypedClassOperation constructorOp = TypedOperation.forConstructor(reflectionConstructor);
    Method method = c.getDeclaredMethod("category", int.class);
    TypedClassOperation methodOp = TypedOperation.forMethod(method);
    methodOp.setExecutableSpecification(getMethodSpecification(method));

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

  private ExecutableSequence createBadnessSequence() throws NoSuchMethodException {
    Class<?> c = ClassWithConditions.class;
    Method method = c.getDeclaredMethod("badness", ClassWithConditions.Range.class, int.class);
    TypedClassOperation methodOp = TypedOperation.forMethod(method);
    methodOp.setExecutableSpecification(getBadnessConditions(method));

    Sequence sequence = new Sequence();
    sequence =
        sequence.extend(
            TypedOperation.createNullOrZeroInitializationForType(
                Type.forClass(ClassWithConditions.Range.class)));
    sequence =
        sequence.extend(TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, -1));
    List<Variable> variables = new ArrayList<>();
    variables.add(sequence.getVariable(sequence.size() - 2));
    variables.add(sequence.getVariable(sequence.size() - 1));
    sequence = sequence.extend(methodOp, variables);
    return new ExecutableSequence(sequence);
  }

  /**
   * Creates an {@link OperationSpecification}, places it in a {@link SpecificationCollection}, and
   * gets the {@link ExecutableSpecification}. Effectively, translating the specifications to
   * executable specifications.
   *
   * @return the {@link ExecutableSpecification} object for {@link
   *     ClassWithConditions#category(int)}
   * @param method the method for which to get the specification
   */
  private ExecutableSpecification getMethodSpecification(Method method) {
    List<String> paramNames = new ArrayList<>();
    paramNames.add("value");
    OperationSpecification spec =
        new OperationSpecification(OperationSignature.of(method), new Identifiers(paramNames));

    List<Precondition> preSpecifications = new ArrayList<>();
    Guard paramGuard = new Guard("positive", "value > 0");
    Precondition paramSpec = new Precondition("must be positive", paramGuard);
    preSpecifications.add(paramSpec);
    spec.addParamSpecifications(preSpecifications);

    List<ThrowsCondition> throwsSpecifications = new ArrayList<>();
    Guard throwsGuard = new Guard("greater than 4*getValue()", "value > 4*receiver.getValue()");
    ThrowsCondition throwsSpec =
        new ThrowsCondition(
            "should be less than 4*getValue", throwsGuard, "java.lang.IllegalArgumentException");
    throwsSpecifications.add(throwsSpec);
    spec.addThrowsConditions(throwsSpecifications);

    List<Postcondition> postSpecifications = new ArrayList<>();
    Guard retGuard;
    Property retProperty;
    Postcondition returnSpec;

    retGuard = new Guard("value in first range", "value < receiver.getValue()");
    retProperty = new Property("return ONE", "result.equals(ClassWithConditions.Range.ONE)");
    returnSpec = new Postcondition("value in first range", retGuard, retProperty);
    postSpecifications.add(returnSpec);

    retGuard = new Guard("value in second range", "value < 2*receiver.getValue()");
    retProperty = new Property("return TWO", "result.equals(ClassWithConditions.Range.TWO)");
    returnSpec = new Postcondition("value in second range", retGuard, retProperty);
    postSpecifications.add(returnSpec);

    retGuard = new Guard("value in third range", "value < 3*receiver.getValue()");
    retProperty = new Property("return THREE", "result.equals(ClassWithConditions.Range.THREE)");
    returnSpec = new Postcondition("value in third range", retGuard, retProperty);
    postSpecifications.add(returnSpec);

    retGuard = new Guard("otherwise", "true");
    retProperty = new Property("return FOUR", "result.equals(ClassWithConditions.Range.FOUR)");
    returnSpec = new Postcondition("otherwise, return FOUR", retGuard, retProperty);
    postSpecifications.add(returnSpec);
    spec.addReturnSpecifications(postSpecifications);

    Map<AccessibleObject, OperationSpecification> specMap = new HashMap<>();
    specMap.put(method, spec);

    Map<AccessibleObject, Set<Method>> parentMap = new HashMap<>();
    MultiMap<OperationSignature, Method> signatureMap = new MultiMap<>();
    SpecificationCollection collection =
        new SpecificationCollection(specMap, signatureMap, parentMap);
    return collection.getExecutableSpecification(method);
  }

  /** Creates ExecutableSpecification including post-condition for constructor that will fail. */
  private ExecutableSpecification getConstructorConditions(Constructor<?> constructor) {

    List<String> paramNames = new ArrayList<>();
    paramNames.add("value");
    OperationSpecification spec =
        new OperationSpecification(OperationSignature.of(constructor), new Identifiers(paramNames));
    List<Precondition> preSpecifications = new ArrayList<>();
    Guard paramGuard = new Guard("non-negative value", "value >= 0");
    Precondition paramSpec = new Precondition("must be non-negative", paramGuard);
    preSpecifications.add(paramSpec);
    spec.addParamSpecifications(preSpecifications);

    List<Postcondition> postSpecifications = new ArrayList<>();
    Guard retGuard = new Guard("always", "true");
    Property retProperty =
        new Property("should have value of argument", "value == 2*result.getValue()");
    Postcondition returnSpec = new Postcondition("value should be argument", retGuard, retProperty);
    postSpecifications.add(returnSpec);
    spec.addReturnSpecifications(postSpecifications);

    Map<AccessibleObject, OperationSpecification> specMap = new HashMap<>();
    specMap.put(constructor, spec);
    Map<AccessibleObject, Set<Method>> parentMap = new HashMap<>();
    MultiMap<OperationSignature, Method> signatureMap = new MultiMap<>();
    SpecificationCollection collection =
        new SpecificationCollection(specMap, signatureMap, parentMap);
    return collection.getExecutableSpecification(constructor);
  }

  private ExecutableSpecification getBadnessConditions(Method method) {
    List<String> paramNames = new ArrayList<>();
    paramNames.add("range");
    paramNames.add("value");
    OperationSpecification spec =
        new OperationSpecification(OperationSignature.of(method), new Identifiers(paramNames));
    List<ThrowsCondition> throwsSpecifications = new ArrayList<>();
    Guard throwsGuard = new Guard("non null", "range == null");
    ThrowsCondition throwsSpecification =
        new ThrowsCondition("non null", throwsGuard, "java.lang.NullPointerException");
    throwsSpecifications.add(throwsSpecification);

    throwsGuard = new Guard("positive value", "value <= 0");
    throwsSpecification =
        new ThrowsCondition(
            "value should be positive integer", throwsGuard, "java.lang.IllegalArgumentException");
    throwsSpecifications.add(throwsSpecification);

    spec.addThrowsConditions(throwsSpecifications);

    Map<AccessibleObject, OperationSpecification> specMap = new HashMap<>();
    specMap.put(method, spec);
    Map<AccessibleObject, Set<Method>> parentMap = new HashMap<>();
    MultiMap<OperationSignature, Method> signatureMap = new MultiMap<>();
    SpecificationCollection collection =
        new SpecificationCollection(specMap, signatureMap, parentMap);
    return collection.getExecutableSpecification(method);
  }
}
