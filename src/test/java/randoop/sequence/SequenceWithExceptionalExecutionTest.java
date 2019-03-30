package randoop.sequence;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.main.GenTests;
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.test.ContractSet;
import randoop.test.TestCheckGenerator;
import randoop.types.ArrayType;
import randoop.types.GenericClassType;
import randoop.types.JDKTypes;
import randoop.types.JavaTypes;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.util.MultiMap;

/**
 * This test is to check behavior of sequence predicates on sequence that has an ArrayStoreException
 * due to attempting to assign an array element of the wrong type to an array. A minimal example
 * would be
 *
 * <pre>{@code
 * Collection<String>[] a = (Collection<String>[])new ArrayList[4];
 * a[0] = new LinkedHashSet<>();
 * }</pre>
 */
public class SequenceWithExceptionalExecutionTest {

  @Test
  public void testArrayStoreException() {
    ArrayType arrayType =
        ArrayType.ofComponentType(JDKTypes.COLLECTION_TYPE.instantiate(JavaTypes.STRING_TYPE));
    ArrayType rawArrayType = ArrayType.ofComponentType(JDKTypes.ARRAY_LIST_TYPE.getRawtype());
    Sequence sequence = new Sequence();
    TypedOperation lengthTerm =
        TypedOperation.createNonreceiverInitialization(new NonreceiverTerm(JavaTypes.INT_TYPE, 4));
    sequence = sequence.extend(lengthTerm, new ArrayList<Variable>());
    List<Variable> input = Collections.singletonList(sequence.getLastVariable());
    sequence = sequence.extend(TypedOperation.createArrayCreation(rawArrayType), input);
    input = Collections.singletonList(sequence.getLastVariable());
    sequence = sequence.extend(TypedOperation.createCast(rawArrayType, arrayType), input);
    int arrayValueIndex = sequence.getLastVariable().index;

    Constructor<?> constructor;
    try {
      constructor = LinkedHashSet.class.getConstructor();
    } catch (NoSuchMethodException e) {
      fail("couldn't get default constructor for LinkedHashSet: " + e.getMessage());
      throw new Error("Unreachable");
    }
    TypedClassOperation constructorOp = TypedOperation.forConstructor(constructor);
    Substitution<ReferenceType> substitution =
        ((GenericClassType) constructorOp.getDeclaringType())
            .instantiate(JavaTypes.STRING_TYPE)
            .getTypeSubstitution();
    input = new ArrayList<>();
    sequence = sequence.extend(constructorOp.apply(substitution), input);
    int linkedHashSetIndex = sequence.getLastVariable().index;

    sequence = sequence.extend(TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, 0));
    input = new ArrayList<>();
    input.add(sequence.getVariable(arrayValueIndex));
    input.add(sequence.getLastVariable());
    input.add(sequence.getVariable(linkedHashSetIndex));
    sequence = sequence.extend(TypedOperation.createArrayElementAssignment(arrayType), input);

    ExecutableSequence es = new ExecutableSequence(sequence);
    TestCheckGenerator gen =
        GenTests.createTestCheckGenerator(
            IS_PUBLIC, new ContractSet(), new MultiMap<Type, TypedOperation>());
    es.execute(new DummyVisitor(), gen);

    assertFalse("sequence should not have unexecuted statements", es.hasNonExecutedStatements());
    assertFalse("sequence should not have failure", es.hasFailure());
    assertFalse("sequence should not have invalid behavior", es.hasInvalidBehavior());
    assertFalse("sequence should not have normal execution", es.isNormalExecution());

    assertThat(
        "exception in last statement",
        es.getNonNormalExecutionIndex(),
        is(equalTo(sequence.size() - 1)));
  }
}
