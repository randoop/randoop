package randoop.sequence.mutators;

import junit.framework.TestCase;
import randoop.MutableSequence;
import randoop.Sequence;
import randoop.experiments.BranchDirMutator;

public class BranchDirMutatorTests extends TestCase {
  

  private static Sequence getSeq1() {
    StringBuilder b = new StringBuilder();

    b.append("var0 = cons java.util.ArrayList.<init>();");
    b.append("var1 = cons java.lang.Object.<init>();");
    b.append("var2 = method java.util.ArrayList.add(java.lang.Object) var0 var1;");
    b.append("var3 = method java.util.ArrayList.iterator() var0 ;");
    b.append("var4 = method java.util.ArrayList.trimToSize() var0;");
    b.append("var5 = prim short 0;");
    b.append("var6 = method java.util.ArrayList.equals(java.lang.Object) var0 var5;");
    b.append("var7 = cons java.util.ArrayList.<init>();");
    b.append("var8 = cons java.lang.Object.<init>();");
    b.append("var9 = method java.util.ArrayList.add(java.lang.Object) var7 var8;");
    b.append("var10 = prim int 0;");
    b.append("var11 = prim long -1;");
    b.append("var12 = method java.util.ArrayList.add(int,java.lang.Object) var7 var10 var11;");
    b.append("var13 = method java.util.ArrayList.listIterator() var7;");
    b.append("var14 = method java.util.ArrayList.remove(java.lang.Object) var0 var13;");


    
    try {
      return Sequence.parse(b.toString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public static void test1() {
    
    Sequence s = getSeq1();
    
    MutableSequence ss = s.toModifiableSequence();
    BranchDirMutator.replace(ss, ss.getVariable(13), ss.getVariable(1));

    System.out.println(ss.toCodeString());
    
    ss = s.toModifiableSequence();
    BranchDirMutator.replace(ss, ss.getVariable(1), ss.getVariable(13));
    System.out.println(ss.toCodeString());
    
  }

}
