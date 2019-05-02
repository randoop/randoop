package randoop.operation;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.ArrayType;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

public class ArrayCreationTests {
  @Test
  public void test1() throws Exception {
    Type elementType = JavaTypes.STRING_TYPE;
    Type arrayType = ArrayType.ofComponentType(elementType);
    List<Type> paramTypes = Collections.singletonList(elementType);
    TypeTuple inputTypes = new TypeTuple(paramTypes);
    InitializedArrayCreation ad = new InitializedArrayCreation((ArrayType) arrayType, 1);
    TypedOperation acOp = new TypedTermOperation(ad, inputTypes, arrayType);
    StringBuilder b = new StringBuilder();
    CallableOperation initOp = new NonreceiverTerm(elementType, "mystring");

    Sequence seq =
        new Sequence().extend(new TypedTermOperation(initOp, new TypeTuple(), elementType));
    Variable var0 = new Variable(seq, 0);
    Variable var1 = new Variable(seq, 1);
    List<Variable> input = Collections.singletonList(var0);
    Statement st_ad = new Statement(acOp);
    st_ad.appendCode(var1, input, b);
    assertEquals(
        "java.lang.String[] strArray1 = new java.lang.String[] { \"mystring\" };", b.toString());
  }

  @Test
  public void test2() throws Exception {
    Type elementType = JavaTypes.CHAR_TYPE;
    Type arrayType = ArrayType.ofComponentType(elementType);
    InitializedArrayCreation ad = new InitializedArrayCreation((ArrayType) arrayType, 1);
    List<Type> paramTypes = Collections.singletonList(elementType);
    TypeTuple inputTypes = new TypeTuple(paramTypes);
    TypedOperation acOp = new TypedTermOperation(ad, inputTypes, arrayType);
    StringBuilder b = new StringBuilder();
    Sequence seq =
        new Sequence()
            .extend(
                new TypedTermOperation(
                    new NonreceiverTerm(elementType, 'c'), new TypeTuple(), elementType));
    Variable var0 = new Variable(seq, 0);
    Variable var1 = new Variable(seq, 1);
    List<Variable> input = Collections.singletonList(var0);
    Statement st_ad = new Statement(acOp);
    st_ad.appendCode(var1, input, b);
    assertEquals("char[] charArray1 = new char[] { 'c' };", b.toString());
  }

  @Test
  public void test3() throws Exception {
    Type elementType = ArrayType.ofComponentType(JavaTypes.CHAR_TYPE);
    Type arrayType = ArrayType.ofComponentType(elementType);
    InitializedArrayCreation arrayCreation = new InitializedArrayCreation((ArrayType) arrayType, 1);
    List<Type> paramTypes = Collections.singletonList(elementType);
    TypeTuple inputTypes = new TypeTuple(paramTypes);
    TypedOperation acOp = new TypedTermOperation(arrayCreation, inputTypes, arrayType);
    StringBuilder b = new StringBuilder();
    Sequence seq =
        new Sequence()
            .extend(
                new TypedTermOperation(
                    new InitializedArrayCreation((ArrayType) arrayType, 0),
                    new TypeTuple(),
                    elementType));
    Variable var0 = new Variable(seq, 0);
    Variable var1 = new Variable(seq, 1);
    List<Variable> input = Collections.singletonList(var0);
    Statement st_ad = new Statement(acOp);
    st_ad.appendCode(var1, input, b);
    assertEquals("char[][] charArray1 = new char[][] { charArray0 };", b.toString());
  }
}
