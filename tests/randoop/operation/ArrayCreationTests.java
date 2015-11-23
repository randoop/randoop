package randoop.operation;

import java.util.ArrayList;

import randoop.Globals;
import randoop.operation.ArrayCreation;
import randoop.operation.NonreceiverTerm;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;

import junit.framework.TestCase;

public class ArrayCreationTests extends TestCase{
  public void test1() throws Exception {
    ArrayCreation ad= new ArrayCreation(String.class, 1);
    StringBuilder b= new StringBuilder();
    Sequence seq = new Sequence().extend(new NonreceiverTerm(String.class, "mystring")); 
    Variable var0 = new Variable(seq, 0);
    Variable var1 = new Variable(seq, 1);
    ArrayList<Variable> input = new ArrayList<Variable>();
    input.add(var0);
    ad.appendCode(var1, input, b);
    assertEquals("java.lang.String[] str_array1 = new java.lang.String[] { \"mystring\"};" + Globals.lineSep + "", b.toString());
  }

  public void test2() throws Exception {
    ArrayCreation ad= new ArrayCreation(char.class, 1);
    StringBuilder b= new StringBuilder();
    Sequence seq = new Sequence().extend(new NonreceiverTerm(char.class, 'c')); 
    Variable var0 = new Variable(seq, 0);
    Variable var1 = new Variable(seq, 1);
    ArrayList<Variable> input = new ArrayList<Variable>();
    input.add(var0);
    ad.appendCode(var1, input, b);
    assertEquals("char[] char_array1 = new char[] { 'c'};" + Globals.lineSep + "", b.toString());
  }

  public void test3() throws Exception {
    ArrayCreation ad= new ArrayCreation(char[].class, 1);
    StringBuilder b= new StringBuilder();
    Sequence seq = new Sequence().extend(new ArrayCreation(char[].class, 0)); 
    Variable var0 = new Variable(seq, 0);
    Variable var1 = new Variable(seq, 1);
    ArrayList<Variable> input = new ArrayList<Variable>();
    input.add(var0);
    ad.appendCode(var1, input, b);
    assertEquals("char[][] char_array_array1 = new char[][] { char_array0};" + Globals.lineSep + "", b.toString());
  }

}
