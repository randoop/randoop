package randoop.generation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.operation.EnumConstant;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.DummyCheckGenerator;
import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.InstantiatedType;
import randoop.types.JDKTypes;
import randoop.types.JavaTypes;
import randoop.types.ParameterizedType;
import randoop.types.ReferenceType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.Randomness;
import randoop.util.SimpleList;

/** Tests to check Collection generation. */
public class CollectionGenerationTest {

  private ComponentManager setupComponentManager() {
    Set<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    Class<?> enumClass = Day.class;
    ClassOrInterfaceType enumType = ClassOrInterfaceType.forClass(enumClass);
    for (Object obj : enumClass.getEnumConstants()) {
      Enum<?> e = (Enum<?>) obj;
      TypedOperation op =
          new TypedClassOperation(new EnumConstant(e), enumType, new TypeTuple(), enumType);
      components.add(new Sequence().extend(op));
    }
    return new ComponentManager(components);
  }

  @Test
  public void testConcreteCollection() {
    ComponentManager componentManager = setupComponentManager();
    ReferenceType elementType = JavaTypes.STRING_TYPE;
    ArrayType arrayType = ArrayType.ofComponentType(elementType);
    InstantiatedType collectionType = JDKTypes.ARRAY_DEQUE_TYPE.instantiate(elementType);
    Sequence sequence = HelperSequenceCreator.createCollection(componentManager, collectionType);
    assertNotNull(sequence);

    Set<Type> outputTypeSet = new HashSet<>();
    for (int i = 0; i < sequence.size(); i++) {
      Type outputType = sequence.getStatement(i).getOutputType();
      outputTypeSet.add(outputType);
      assertTrue(
          "statement type should",
          outputType.equals(collectionType)
              || outputType.equals(elementType)
              || outputType.equals(arrayType)
              || outputType.equals(JavaTypes.BOOLEAN_TYPE));
    }
    assertThat("should only be four output types", outputTypeSet.size(), is(equalTo(4)));

    ExecutableSequence es = new ExecutableSequence(sequence);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
  }

  @Test
  public void testAbstractCollection() {
    ComponentManager componentManager = setupComponentManager();
    ReferenceType elementType = JavaTypes.STRING_TYPE;
    ArrayType arrayType = ArrayType.ofComponentType(elementType);
    InstantiatedType collectionType = JDKTypes.SET_TYPE.instantiate(elementType);
    Sequence sequence = HelperSequenceCreator.createCollection(componentManager, collectionType);
    assertNotNull(sequence);

    Set<Type> outputTypeSet = new HashSet<>();
    for (int i = 0; i < sequence.size(); i++) {
      Type outputType = sequence.getStatement(i).getOutputType();
      outputTypeSet.add(outputType);
      assertTrue(
          "statement type should be one of four types, got " + outputType,
          outputType.isSubtypeOf(collectionType)
              || outputType.equals(elementType)
              || outputType.equals(arrayType)
              || outputType.equals(JavaTypes.BOOLEAN_TYPE));
    }
    assertThat("should only be four output types", outputTypeSet.size(), is(equalTo(4)));
  }

  @Test
  public void testEnumSetCollection() {
    ComponentManager componentManager = setupComponentManager();
    ReferenceType enumType = ClassOrInterfaceType.forClass(Day.class);
    ArrayType arrayType = ArrayType.ofComponentType(enumType);
    InstantiatedType collectionType = JDKTypes.ENUM_SET_TYPE.instantiate(enumType);
    Sequence sequence = HelperSequenceCreator.createCollection(componentManager, collectionType);
    assertNotNull(sequence);

    Set<Type> outputTypeSet = new HashSet<>();
    for (int i = 0; i < sequence.size(); i++) {
      Type outputType = sequence.getStatement(i).getOutputType();
      outputTypeSet.add(outputType);
      assertTrue(
          "statement type should be one of five types, got " + outputType,
          outputType.isSubtypeOf(collectionType)
              || outputType.equals(enumType)
              || outputType.equals(arrayType)
              || outputType.equals(JavaTypes.BOOLEAN_TYPE)
              || outputType.equals(JavaTypes.CLASS_TYPE));
    }
    assertThat("should only be five output types", outputTypeSet.size(), is(equalTo(5)));
  }

  @Test
  public void testParameterizedElementCollection() {
    ComponentManager componentManager = setupComponentManager();
    InstantiatedType elementType = JDKTypes.LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    componentManager.addGeneratedSequence(
        HelperSequenceCreator.createCollection(componentManager, elementType));
    ParameterizedType concreteElementType =
        JDKTypes.ARRAY_LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    ArrayType arrayType = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);

    InstantiatedType collectionType = JDKTypes.LIST_TYPE.instantiate(elementType);
    Sequence sequence = HelperSequenceCreator.createCollection(componentManager, collectionType);
    assertNotNull(sequence);

    Set<Type> outputTypeSet = new HashSet<>();
    for (int i = 0; i < sequence.size(); i++) {
      Type outputType = sequence.getStatement(i).getOutputType();
      outputTypeSet.add(outputType);
      assertTrue(
          "statement type should be one of five types, got " + outputType,
          outputType.isSubtypeOf(collectionType)
              || outputType.equals(concreteElementType)
              || outputType.equals(arrayType)
              || outputType.equals(JavaTypes.STRING_TYPE)
              || outputType.equals(JavaTypes.BOOLEAN_TYPE)
              || outputType.equals(JavaTypes.CLASS_TYPE));
    }
    assertThat("should only be five output types", outputTypeSet.size(), is(equalTo(5)));
  }

  @Test
  public void testParameterizedArray() {
    Randomness.setSeed(999997);
    ComponentManager componentManager = setupComponentManager();
    InstantiatedType elementType = JDKTypes.LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    componentManager.addGeneratedSequence(
        HelperSequenceCreator.createCollection(componentManager, elementType));
    componentManager.addGeneratedSequence(
        HelperSequenceCreator.createCollection(componentManager, elementType));
    InstantiatedType concreteElementType =
        JDKTypes.ARRAY_LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    ArrayType arrayType = ArrayType.ofComponentType(elementType);
    ArrayType rawArrayType = ArrayType.ofComponentType(JDKTypes.LIST_TYPE.getRawtype());
    ArrayType strArrayType = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);
    SimpleList<Sequence> sequenceList =
        HelperSequenceCreator.createArraySequence(componentManager, arrayType);
    Sequence sequence = sequenceList.get(0);
    assertNotNull(sequence);

    Set<Type> outputTypeSet = new HashSet<>();
    for (int i = 0; i < sequence.size(); i++) {
      Type outputType = sequence.getStatement(i).getOutputType();
      outputTypeSet.add(outputType);
      assertTrue(
          "statement type should be one of 8 types, got " + outputType,
          outputType.equals(concreteElementType)
              || outputType.equals(arrayType)
              || outputType.equals(rawArrayType)
              || outputType.equals(JavaTypes.INT_TYPE)
              || outputType.isString()
              || outputType.equals(strArrayType)
              || outputType.equals(JavaTypes.BOOLEAN_TYPE)
              || outputType.isVoid());
    }
    assertThat("should be eight output types", outputTypeSet.size(), is(equalTo(8)));
  }

  @Test
  public void testGenericArrayOfArray() {
    Randomness.setSeed(104729);
    ComponentManager componentManager = setupComponentManager();
    InstantiatedType elementType = JDKTypes.LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    componentManager.addGeneratedSequence(
        HelperSequenceCreator.createCollection(componentManager, elementType));
    ArrayType arrayType = ArrayType.ofComponentType(elementType);
    componentManager.addGeneratedSequence(
        HelperSequenceCreator.createArraySequence(componentManager, arrayType).get(0));

    InstantiatedType concreteElementType =
        JDKTypes.ARRAY_LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);

    ArrayType arrayOfArrayType = ArrayType.ofComponentType(arrayType);

    assertTrue("array type parameterized", arrayType.hasParameterizedElementType());
    assertTrue("array of array type parameterized", arrayOfArrayType.hasParameterizedElementType());

    ArrayType rawArrayType = arrayType.getRawTypeArray();
    ArrayType rawArrayOfArrayType = arrayOfArrayType.getRawTypeArray();
    ArrayType strArrayType = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);

    // Returns a list containing a single sequence
    SimpleList<Sequence> sequenceList =
        HelperSequenceCreator.createArraySequence(componentManager, arrayOfArrayType);
    Sequence firstSequence = sequenceList.get(0);
    assertNotNull(firstSequence);

    Set<Type> outputTypeSet = new HashSet<>();
    for (int i = 0; i < firstSequence.size(); i++) {
      Type outputType = firstSequence.getStatement(i).getOutputType();
      outputTypeSet.add(outputType);
      assertTrue(
          "statement type should be one of ten types, got " + outputType,
          outputType.equals(concreteElementType)
              || outputType.equals(arrayType)
              || outputType.equals(arrayOfArrayType)
              || outputType.equals(rawArrayType)
              || outputType.equals(rawArrayOfArrayType)
              || outputType.equals(JavaTypes.INT_TYPE)
              || outputType.isString()
              || outputType.equals(strArrayType)
              || outputType.equals(JavaTypes.BOOLEAN_TYPE)
              || outputType.isVoid());
    }

    if (outputTypeSet.size() != 6 && outputTypeSet.size() != 10) {
      System.out.println("outputTypeSet: " + outputTypeSet);
      System.out.println("sequenceList.size(): " + sequenceList.size());
      for (int i = 0; i < sequenceList.size(); i++) {
        Sequence sequence = sequenceList.get(i);
        System.out.println("");
        System.out.println("TEST:");
        System.out.println(sequence);
      }
    }
    // TODO: This is brittle because it creates a single sequence and hard-codes expectations
    // for that.  This should create a full test suite and check the types in it.
    // assertThat("should be ten output types", outputTypeSet.size(), anyOf(is(equalTo(10)),
    // is(equalTo(6))));
  }

  /**
   * inspired by jfreechart case in Defects4J where Randoop trying to create an array of type
   * Comparable<org.jfree.chart.plot.PlotOrientation>[]. Tests that Comparable<String>[] is replaced
   * with String[]
   */
  @Test
  public void testInterfaceArray() {
    ComponentManager componentManager = setupComponentManager();
    ParameterizedType elementType = JavaTypes.COMPARABLE_TYPE.instantiate(JavaTypes.STRING_TYPE);
    ArrayType arrayType = ArrayType.ofComponentType(elementType);
    ArrayType strArrayType = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);
    SimpleList<Sequence> sequenceList =
        HelperSequenceCreator.createArraySequence(componentManager, arrayType);
    Sequence firstSequence = sequenceList.get(0);
    assertNotNull(firstSequence);

    Set<Type> outputTypeSet = new HashSet<>();
    for (int i = 0; i < firstSequence.size(); i++) {
      Type outputType = firstSequence.getStatement(i).getOutputType();
      outputTypeSet.add(outputType);
      assertTrue(
          "statement type should be one of two types, got " + outputType,
          !outputType.equals(elementType)
              || outputType.equals(JavaTypes.STRING_TYPE)
              || outputType.equals(strArrayType));
    }
    assertThat("should be two output types", outputTypeSet.size(), is(equalTo(2)));
  }
}
