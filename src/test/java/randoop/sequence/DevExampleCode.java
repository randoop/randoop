package randoop.sequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import randoop.Globals;
import randoop.operation.TypedOperation;
import randoop.types.InstantiatedType;
import randoop.types.JDKTypes;
import randoop.types.JavaTypes;
import randoop.types.ReferenceType;
import randoop.types.Substitution;

/**
 * This is the code for building a sequence by extension from the developer document. Yeah, it
 * works. No, it doesn't look like the example b/c of variable naming and short form substitutions.
 */
public class DevExampleCode {

  @Test
  public void devDocExampleTest() {
    try {
      // Want constructor for LinkedList<String>
      InstantiatedType linkedListType =
          JDKTypes.LINKED_LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
      Substitution substLL = linkedListType.getTypeSubstitution();
      TypedOperation newLL =
          TypedOperation.forConstructor(LinkedList.class.getConstructor()).substitute(substLL);

      // operations for string constant, and list method calls
      TypedOperation newOb =
          TypedOperation.createPrimitiveInitialization(JavaTypes.STRING_TYPE, "hi!");
      TypedOperation addFirst =
          TypedOperation.forMethod(LinkedList.class.getMethod("addFirst", Object.class))
              .substitute(substLL);
      TypedOperation size =
          TypedOperation.forMethod(LinkedList.class.getMethod("size")).substitute(substLL);

      // Call to operation with wildcard in TreeSet<String>
      InstantiatedType treeSetType = JDKTypes.TREE_SET_TYPE.instantiate(JavaTypes.STRING_TYPE);
      Substitution substTS = treeSetType.getTypeSubstitution();
      TypedOperation wcTS =
          TypedOperation.forConstructor(TreeSet.class.getConstructor(Collection.class))
              .substitute(substTS)
              .applyCaptureConversion();
      Substitution substWC =
          new Substitution(wcTS.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
      TypedOperation newTS = wcTS.substitute(substWC);

      // call to generic operation
      TypedOperation syncA =
          TypedOperation.forMethod(Collections.class.getMethod("synchronizedSet", Set.class));
      Substitution substA =
          new Substitution(syncA.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
      TypedOperation syncS = syncA.substitute(substA);

      // Now, create the sequence by repeated extension.
      Sequence s = new Sequence();
      s = s.extend(newLL);
      s = s.extend(newOb);
      s = s.extend(addFirst, s.getVariable(0), s.getVariable(1));
      s = s.extend(size, s.getVariable(0));
      s = s.extend(newTS, s.getVariable(0));
      s = s.extend(syncS, s.getVariable(4));

      assertEquals(
          "java.util.LinkedList<java.lang.String> strList0 = new java.util.LinkedList<java.lang.String>();"
              + Globals.lineSep
              + "strList0.addFirst(\"hi!\");"
              + Globals.lineSep
              + "int int3 = strList0.size();"
              + Globals.lineSep
              + "java.util.TreeSet<java.lang.String> strSet4 = new java.util.TreeSet<java.lang.String>((java.util.Collection<java.lang.String>)strList0);"
              + Globals.lineSep
              + "java.util.Set<java.lang.String> strSet5 = java.util.Collections.synchronizedSet((java.util.Set<java.lang.String>)strSet4);"
              + Globals.lineSep,
          s.toCodeString());
    } catch (NoSuchMethodException e) {
      fail("didn't find method: " + e.getMessage());
    }
  }
}
