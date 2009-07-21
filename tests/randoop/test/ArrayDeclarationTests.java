package randoop.test;

import java.util.ArrayList;

import junit.framework.TestCase;
import randoop.ArrayDeclaration;
import randoop.Globals;
import randoop.Sequence;
import randoop.Variable;

public class ArrayDeclarationTests extends TestCase{
  public void test1() throws Exception {
    ArrayDeclaration ad= new ArrayDeclaration(String.class, 1);
    StringBuilder b= new StringBuilder();
    Sequence dummySeq = new Sequence(); 
    Variable var0 = new Variable(dummySeq, 0);
    Variable var1 = new Variable(dummySeq, 1);
    ArrayList<Variable> input = new ArrayList<Variable>();
    input.add(var0);
    ad.appendCode(var1, input, b);
    assertEquals("java.lang.String[] var1 = new java.lang.String[] { var0};" + Globals.lineSep + "", b.toString());
  }

  public void test2() throws Exception {
    ArrayDeclaration ad= new ArrayDeclaration(char.class, 1);
    StringBuilder b= new StringBuilder();
    Sequence dummySeq = new Sequence(); 
    Variable var0 = new Variable(dummySeq, 0);
    Variable var1 = new Variable(dummySeq, 1);
    ArrayList<Variable> input = new ArrayList<Variable>();
    input.add(var0);
    ad.appendCode(var1, input, b);
    assertEquals("char[] var1 = new char[] { var0};" + Globals.lineSep + "", b.toString());
  }

  public void test3() throws Exception {
    ArrayDeclaration ad= new ArrayDeclaration(char[].class, 1);
    StringBuilder b= new StringBuilder();
    Sequence dummySeq = new Sequence(); 
    Variable var0 = new Variable(dummySeq, 0);
    Variable var1 = new Variable(dummySeq, 1);
    ArrayList<Variable> input = new ArrayList<Variable>();
    input.add(var0);
    ad.appendCode(var1, input, b);
    assertEquals("char[][] var1 = new char[][] { var0};" + Globals.lineSep + "", b.toString());
  }

}
