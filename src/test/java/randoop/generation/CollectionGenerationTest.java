package randoop.generation;

import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import randoop.operation.EnumConstant;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.InstantiatedType;
import randoop.types.JDKTypes;
import randoop.types.ParameterizedType;
import randoop.types.ReferenceType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.Randomness;
import randoop.util.SimpleList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests to check Collection generation.
 */
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
    ArrayType arrayType = ArrayType.ofElementType(elementType);
    ParameterizedType collectionType = JDKTypes.ARRAY_DEQUE_TYPE.instantiate(elementType);
    Sequence sequence = HelperSequenceCreator.createCollection(componentManager, collectionType);
    assert sequence != null : "sequence should not be null";

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
  }

  @Test
  public void testAbstractCollection() {
    ComponentManager componentManager = setupComponentManager();
    ReferenceType elementType = JavaTypes.STRING_TYPE;
    ArrayType arrayType = ArrayType.ofElementType(elementType);
    ParameterizedType collectionType = JDKTypes.SET_TYPE.instantiate(elementType);
    Sequence sequence = HelperSequenceCreator.createCollection(componentManager, collectionType);
    assert sequence != null : "sequence should not be null";

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
    ArrayType arrayType = ArrayType.ofElementType(enumType);
    ParameterizedType collectionType = JDKTypes.ENUM_SET_TYPE.instantiate(enumType);
    Sequence sequence = HelperSequenceCreator.createCollection(componentManager, collectionType);
    assert sequence != null : "sequence should not be null";

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
    ParameterizedType elementType = JDKTypes.LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    componentManager.addGeneratedSequence(
        HelperSequenceCreator.createCollection(componentManager, elementType));
    ParameterizedType concreteElementType =
        JDKTypes.ARRAY_LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    ArrayType arrayType = ArrayType.ofElementType(JavaTypes.STRING_TYPE);

    ParameterizedType collectionType = JDKTypes.LIST_TYPE.instantiate(elementType);
    Sequence sequence = HelperSequenceCreator.createCollection(componentManager, collectionType);
    assert sequence != null : "sequence should not be null";
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
    Randomness.reset(999997);
    ComponentManager componentManager = setupComponentManager();
    ParameterizedType elementType = JDKTypes.LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    componentManager.addGeneratedSequence(
        HelperSequenceCreator.createCollection(componentManager, elementType));
    componentManager.addGeneratedSequence(
        HelperSequenceCreator.createCollection(componentManager, elementType));
    ParameterizedType concreteElementType =
        JDKTypes.ARRAY_LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    ArrayType arrayType = ArrayType.ofElementType(elementType);
    ArrayType strArrayType = ArrayType.ofElementType(JavaTypes.STRING_TYPE);
    SimpleList<Sequence> sequenceList =
        HelperSequenceCreator.createArraySequence(componentManager, arrayType);
    Sequence sequence = sequenceList.get(0);
    assert sequence != null : "sequence should not be null";
    Set<Type> outputTypeSet = new HashSet<>();
    for (int i = 0; i < sequence.size(); i++) {
      Type outputType = sequence.getStatement(i).getOutputType();
      outputTypeSet.add(outputType);
      assertTrue(
          "statement type should be one of nine types, got " + outputType,
          outputType.equals(elementType)
              || outputType.equals(concreteElementType)
              || outputType.equals(arrayType)
              || outputType.equals(JavaTypes.OBJECT_TYPE)
              || outputType.equals(JavaTypes.INT_TYPE)
              || (outputType.isParameterized()
                  && ((InstantiatedType) outputType).isInstantiationOf(JavaTypes.CLASS_TYPE))
              || outputType.isString()
              || outputType.equals(strArrayType)
              || outputType.equals(JavaTypes.BOOLEAN_TYPE)
              || outputType.isVoid());
    }
    assertThat("should be nine output types", outputTypeSet.size(), is(equalTo(9)));
  }

  /*
   * inspired by jfreechart case in Defects4J where Randoop trying to create an array of type
   * Comparable<org.jfree.chart.plot.PlotOrientation>[].
   * Tests that Comparable<String>[] is replaced with String[]
   */
  @Test
  public void testInterfaceArray() {
    ComponentManager componentManager = setupComponentManager();
    ParameterizedType elementType = JavaTypes.COMPARABLE_TYPE.instantiate(JavaTypes.STRING_TYPE);
    ArrayType arrayType = ArrayType.ofElementType(elementType);
    ArrayType strArrayType = ArrayType.ofElementType(JavaTypes.STRING_TYPE);
    SimpleList<Sequence> sequenceList =
        HelperSequenceCreator.createArraySequence(componentManager, arrayType);
    Sequence sequence = sequenceList.get(0);
    assert sequence != null : "sequence should not be null";
    Set<Type> outputTypeSet = new HashSet<>();
    for (int i = 0; i < sequence.size(); i++) {
      Type outputType = sequence.getStatement(i).getOutputType();
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
