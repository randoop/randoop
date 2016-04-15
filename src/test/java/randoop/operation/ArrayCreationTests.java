package randoop.operation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import randoop.Globals;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.ConcreteArrayType;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeTuple;
import randoop.types.ConcreteTypes;

import static org.junit.Assert.assertEquals;

public class ArrayCreationTests {
  @Test
  public void test1() throws Exception {
    ConcreteType elementType = ConcreteTypes.STRING_TYPE;
    ConcreteType arrayType = ConcreteType.forArrayOf(elementType);
    List<ConcreteType> paramTypes = new ArrayList<>();
    paramTypes.add(elementType);
    ConcreteTypeTuple inputTypes = new ConcreteTypeTuple(paramTypes);
    ArrayCreation ad = new ArrayCreation((ConcreteArrayType)arrayType, 1);
    ConcreteOperation acOp = new ConcreteOperation(ad, arrayType, inputTypes, arrayType);
    StringBuilder b = new StringBuilder();
    CallableOperation initOp = new NonreceiverTerm(elementType, "mystring");

    Sequence seq = new Sequence().extend(new ConcreteOperation(initOp, elementType, new ConcreteTypeTuple(), elementType));
    Variable var0 = new Variable(seq, 0);
    Variable var1 = new Variable(seq, 1);
    ArrayList<Variable> input = new ArrayList<>();
    input.add(var0);
    Statement st_ad = new Statement(acOp);
    st_ad.appendCode(var1, input, b);
    assertEquals(
        "java.lang.String[] str_array1 = new java.lang.String[] { \"mystring\" };"
            + Globals.lineSep
            + "",
        b.toString());
  }

  @Test
  public void test2() throws Exception {
    ConcreteType elementType = ConcreteTypes.CHAR_TYPE;
    ConcreteType arrayType = ConcreteType.forArrayOf(elementType);
    ArrayCreation ad = new ArrayCreation((ConcreteArrayType)arrayType, 1);
    List<ConcreteType> paramTypes = new ArrayList<>();
    paramTypes.add(elementType);
    ConcreteTypeTuple inputTypes = new ConcreteTypeTuple(paramTypes);
    ConcreteOperation acOp = new ConcreteOperation(ad, arrayType, inputTypes, arrayType);
    StringBuilder b = new StringBuilder();
    Sequence seq = new Sequence().extend(new ConcreteOperation(new NonreceiverTerm(elementType, 'c'), elementType, new ConcreteTypeTuple(), elementType));
    Variable var0 = new Variable(seq, 0);
    Variable var1 = new Variable(seq, 1);
    ArrayList<Variable> input = new ArrayList<>();
    input.add(var0);
    Statement st_ad = new Statement(acOp);
    st_ad.appendCode(var1, input, b);
    assertEquals("char[] char_array1 = new char[] { 'c' };" + Globals.lineSep + "", b.toString());
  }

  @Test
  public void test3() throws Exception {
    ConcreteType elementType = ConcreteType.forArrayOf(ConcreteTypes.CHAR_TYPE);
    ConcreteType arrayType = ConcreteType.forArrayOf(elementType);
    ArrayCreation arrayCreation = new ArrayCreation((ConcreteArrayType)arrayType, 1);
    List<ConcreteType> paramTypes = new ArrayList<>();
    paramTypes.add(elementType);
    ConcreteTypeTuple inputTypes = new ConcreteTypeTuple(paramTypes);
    ConcreteOperation acOp = new ConcreteOperation(arrayCreation, arrayType, inputTypes, arrayType);
    StringBuilder b = new StringBuilder();
    Sequence seq = new Sequence().extend(new ConcreteOperation(new ArrayCreation((ConcreteArrayType)arrayType, 0), elementType, new ConcreteTypeTuple(), elementType));
    Variable var0 = new Variable(seq, 0);
    Variable var1 = new Variable(seq, 1);
    ArrayList<Variable> input = new ArrayList<>();
    input.add(var0);
    Statement st_ad = new Statement(acOp);
    st_ad.appendCode(var1, input, b);
    assertEquals(
        "char[][] char_array_array1 = new char[][] { char_array0 };" + Globals.lineSep + "",
        b.toString());
  }
}
