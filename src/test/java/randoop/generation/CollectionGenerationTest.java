package randoop.generation;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Collections;
import randoop.operation.EnumConstant;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;
import randoop.types.JDKTypes;
import randoop.types.ParameterizedType;
import randoop.types.ReferenceType;
import randoop.types.TypeTuple;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

/**
 * Tests to check Collection generation.
 */
public class CollectionGenerationTest {

  private static ComponentManager componentManager;

  @BeforeClass
  public static void setupComponentManager() {
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
    componentManager = new ComponentManager(components);
  }

  @Test
  public void testConcreteCollection() {
    ReferenceType elementType = ConcreteTypes.STRING_TYPE;
    ArrayType arrayType = ArrayType.ofElementType(elementType);
    ParameterizedType collectionType = JDKTypes.ARRAY_DEQUE_TYPE.instantiate(elementType);
    Sequence sequence = HelperSequenceCreator.createCollection(componentManager, collectionType);
    assert sequence != null : "sequence should not be null";

    Set<GeneralType> outputTypeSet = new HashSet<>();
    for (int i = 0; i < sequence.size(); i++) {
      GeneralType outputType = sequence.getStatement(i).getOutputType();
      outputTypeSet.add(outputType);
      assertTrue(
          "statement type should",
          outputType.equals(collectionType)
              || outputType.equals(elementType)
              || outputType.equals(arrayType)
              || outputType.equals(ConcreteTypes.BOOLEAN_TYPE));
    }
    assertThat("should only be four output types", outputTypeSet.size(), is(equalTo(4)));
  }

  @Test
  public void testAbstractCollection() {
    ReferenceType elementType = ConcreteTypes.STRING_TYPE;
    ArrayType arrayType = ArrayType.ofElementType(elementType);
    ParameterizedType collectionType = JDKTypes.SET_TYPE.instantiate(elementType);
    Sequence sequence = HelperSequenceCreator.createCollection(componentManager, collectionType);
    assert sequence != null : "sequence should not be null";

    Set<GeneralType> outputTypeSet = new HashSet<>();
    for (int i = 0; i < sequence.size(); i++) {
      GeneralType outputType = sequence.getStatement(i).getOutputType();
      outputTypeSet.add(outputType);
      assertTrue(
          "statement type should be one of four types, got " + outputType,
          outputType.isSubtypeOf(collectionType)
              || outputType.equals(elementType)
              || outputType.equals(arrayType)
              || outputType.equals(ConcreteTypes.BOOLEAN_TYPE));
    }
    assertThat("should only be four output types", outputTypeSet.size(), is(equalTo(4)));
  }

  @Test
  public void testEnumSetCollection() {
    EnumSet<Day> set = EnumSet.noneOf(Day.class);
    Collections.addAll(set, new Object[] {Day.FRIDAY});
    ReferenceType enumType = ClassOrInterfaceType.forClass(Day.class);
    ParameterizedType collectionType = JDKTypes.ENUM_SET_TYPE.instantiate(enumType);
    Sequence sequence = HelperSequenceCreator.createCollection(componentManager, collectionType);

    assert sequence == null : "enumset not working yet";
    //fail("not yet implemented");
  }
}
