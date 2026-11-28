package randoop.sequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.util.Collections;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.condition.RandoopSpecificationError;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.OmitMethodsPredicate;
import randoop.test.ContractSet;
import randoop.test.TestCheckGenerator;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.JavaTypes;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.util.MultiMap;

/** Tests {@link ExecutableSequence#castToRunTimeType} when the last statement calls getClass(). */
public class GetClassCastTest {

  /**
   * A generic class whose type parameter has a bound that {@code Object} is not within.
   *
   * @param <T> the type parameter
   */
  public static class NumberHolder<T extends Number> {
    /** Creates a NumberHolder. */
    public NumberHolder() {}
  }

  /**
   * If {@code Object} is not within the bound of a type parameter of the receiver's generic class,
   * then the result of {@code getClass()} is cast to {@code Class} of the raw type.
   */
  @Test
  public void testGetClassOfBoundedGenericClass()
      throws NoSuchMethodException, RandoopSpecificationError {
    TypedClassOperation constructorOp =
        TypedOperation.forConstructor(NumberHolder.class.getConstructor());
    GenericClassType holderType = (GenericClassType) constructorOp.getOutputType();
    Sequence sequence = new Sequence().extend(constructorOp, Collections.emptyList());
    TypedOperation getClassOp = TypedOperation.forMethod(Object.class.getMethod("getClass"));
    sequence = sequence.extend(getClassOp, Collections.singletonList(sequence.getLastVariable()));

    ExecutableSequence es = new ExecutableSequence(sequence);
    TestCheckGenerator gen =
        GenTests.createTestCheckGenerator(
            IS_PUBLIC, new ContractSet(), new MultiMap<>(), OmitMethodsPredicate.NO_OMISSION);
    es.execute(new DummyVisitor(), gen);
    assertTrue(es.isNormalExecution());

    boolean savedCastToRunTimeType = GenInputsAbstract.cast_to_run_time_type;
    GenInputsAbstract.cast_to_run_time_type = true;
    try {
      assertTrue(es.castToRunTimeType());
    } finally {
      GenInputsAbstract.cast_to_run_time_type = savedCastToRunTimeType;
    }
    Statement cast = es.sequence.getStatement(es.sequence.size() - 1);
    assertEquals(
        JavaTypes.CLASS_TYPE.instantiate(Collections.singletonList(holderType.getRawtype())),
        cast.getOutputType());
  }

  /**
   * A generic class whose type parameter has a bound that {@code Object} is not within, with a
   * generic non-static member class.
   *
   * @param <T> the type parameter
   */
  public static class NumberOuter<T extends Number> {
    /** Creates a NumberOuter. */
    public NumberOuter() {}

    /**
     * A generic non-static member class.
     *
     * @param <U> the type parameter
     */
    public class Inner<U> {
      /** Creates an Inner. */
      public Inner() {}
    }
  }

  /**
   * If {@code Object} is not within the bound of a type parameter of an enclosing class of the
   * receiver's generic class, then the result of {@code getClass()} is cast to {@code Class} of the
   * raw type, whose enclosing type is also raw.
   */
  @Test
  public void testGetClassOfMemberOfBoundedGenericClass()
      throws NoSuchMethodException, RandoopSpecificationError {
    TypedClassOperation outerConstructorOp =
        TypedOperation.forConstructor(NumberOuter.class.getConstructor());
    TypedClassOperation innerConstructorOp =
        TypedOperation.forConstructor(NumberOuter.Inner.class.getConstructor(NumberOuter.class));
    Sequence sequence = new Sequence().extend(outerConstructorOp, Collections.emptyList());
    sequence =
        sequence.extend(innerConstructorOp, Collections.singletonList(sequence.getLastVariable()));
    TypedOperation getClassOp = TypedOperation.forMethod(Object.class.getMethod("getClass"));
    sequence = sequence.extend(getClassOp, Collections.singletonList(sequence.getLastVariable()));

    ExecutableSequence es = new ExecutableSequence(sequence);
    TestCheckGenerator gen =
        GenTests.createTestCheckGenerator(
            IS_PUBLIC, new ContractSet(), new MultiMap<>(), OmitMethodsPredicate.NO_OMISSION);
    es.execute(new DummyVisitor(), gen);
    assertTrue(es.isNormalExecution());

    boolean savedCastToRunTimeType = GenInputsAbstract.cast_to_run_time_type;
    GenInputsAbstract.cast_to_run_time_type = true;
    try {
      assertTrue(es.castToRunTimeType());
    } finally {
      GenInputsAbstract.cast_to_run_time_type = savedCastToRunTimeType;
    }
    Statement cast = es.sequence.getStatement(es.sequence.size() - 1);
    InstantiatedType castType = (InstantiatedType) cast.getOutputType();
    ReferenceType classArgument =
        ((ReferenceArgument) castType.getTypeArguments().get(0)).getReferenceType();
    assertTrue(classArgument.isRawtype());
    // The enclosing type is raw NumberOuter, not NumberOuter<T>, so T does not occur in the cast.
    assertFalse(classArgument.isGeneric());
  }
}
