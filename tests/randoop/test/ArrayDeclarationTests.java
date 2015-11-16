package randoop.test;

import java.util.ArrayList;

import junit.framework.TestCase;
import randoop.ArrayCreation;
import randoop.Globals;
import randoop.NonreceiverTerm;
import randoop.Sequence;
import randoop.Statement;
import randoop.Variable;

public class ArrayDeclarationTests extends TestCase{
  public void test1() throws Exception {
    ArrayCreation ad= new ArrayCreation(String.class, 1);
    StringBuilder b= new StringBuilder();
    Sequence seq = new Sequence().extend(new NonreceiverTerm(String.class, "mystring")); 
    Variable var0 = new Variable(seq, 0);
    Variable var1 = new Variable(seq, 1);
    ArrayList<Variable> input = new ArrayList<Variable>();
    input.add(var0);
    Statement st_ad = new Statement(ad);
    st_ad.appendCode(var1, input, b);
    assertEquals("java.lang.String[] str_array1 = new java.lang.String[] { \"mystring\" };" + Globals.lineSep + "", b.toString());
  }

  public void test2() throws Exception {
    ArrayCreation ad= new ArrayCreation(char.class, 1);
    StringBuilder b= new StringBuilder();
    Sequence seq = new Sequence().extend(new NonreceiverTerm(char.class, 'c')); 
    Variable var0 = new Variable(seq, 0);
    Variable var1 = new Variable(seq, 1);
    ArrayList<Variable> input = new ArrayList<Variable>();
    input.add(var0);
    Statement st_ad = new Statement(ad);
    st_ad.appendCode(var1, input, b);
    assertEquals("char[] char_array1 = new char[] { 'c' };" + Globals.lineSep + "", b.toString());
  }

  public void test3() throws Exception {
    ArrayCreation ad= new ArrayCreation(char[].class, 1);
    StringBuilder b= new StringBuilder();
    Sequence seq = new Sequence().extend(new ArrayCreation(char[].class, 0)); 
    Variable var0 = new Variable(seq, 0);
    Variable var1 = new Variable(seq, 1);
    ArrayList<Variable> input = new ArrayList<Variable>();
    input.add(var0);
    Statement st_ad = new Statement(ad);
    st_ad.appendCode(var1, input, b);
    assertEquals("char[][] char_array1 = new char[][] { char_array0 };" + Globals.lineSep + "", b.toString());
  }

}
