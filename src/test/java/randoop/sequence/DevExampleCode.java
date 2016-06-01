package randoop.sequence;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import randoop.Globals;
import randoop.operation.TypedOperation;
import randoop.types.ConcreteTypes;
import randoop.types.InstantiatedType;
import randoop.types.JDKTypes;
import randoop.types.ReferenceType;
import randoop.types.Substitution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by bjkeller on 6/1/16.
 */
public class DevExampleCode {

  @Test
  public void devDocExampleTest() {
    try {
      InstantiatedType linkedListType = JDKTypes.LINKED_LIST_TYPE.instantiate(ConcreteTypes.STRING_TYPE);
      Substitution<ReferenceType> substLL = linkedListType.getTypeSubstitution();
      TypedOperation newLL = TypedOperation.forConstructor(LinkedList.class.getConstructor()).apply(substLL);
      TypedOperation newOb = TypedOperation.createPrimitiveInitialization(ConcreteTypes.STRING_TYPE, "hi!");
      TypedOperation addFirst = TypedOperation.forMethod(LinkedList.class.getMethod("addFirst", Object.class)).apply(substLL);
      TypedOperation size = TypedOperation.forMethod(LinkedList.class.getMethod("size")).apply(substLL);
      InstantiatedType treeSetType = JDKTypes.TREE_SET_TYPE.instantiate(ConcreteTypes.STRING_TYPE);
      Substitution<ReferenceType> substTS = treeSetType.getTypeSubstitution();
      TypedOperation wcTS = TypedOperation.forConstructor(TreeSet.class.getConstructor(Collection.class)).apply(substTS).applyCaptureConversion();
      Substitution<ReferenceType> substWC = Substitution.forArgs(wcTS.getTypeParameters(), (ReferenceType)ConcreteTypes.STRING_TYPE);
      TypedOperation newTS = wcTS.apply(substWC);
      TypedOperation syncA = TypedOperation.forMethod(Collections.class.getMethod("synchronizedSet", Set.class));
      Substitution<ReferenceType> substA = Substitution.forArgs(syncA.getTypeParameters(), (ReferenceType)ConcreteTypes.STRING_TYPE);
      TypedOperation syncS = syncA.apply(substA);

      // Now, create the sequence by repeated extension.
      Sequence s = new Sequence();
      s = s.extend(newLL);
      s = s.extend(newOb);
      s = s.extend(addFirst, s.getVariable(0), s.getVariable(1));
      s = s.extend(size,    s.getVariable(0));
      s = s.extend(newTS,   s.getVariable(0));
      s = s.extend(syncS,   s.getVariable(4));

      assertEquals("java.util.LinkedList<java.lang.String> linkedlist_str0 = new java.util.LinkedList<java.lang.String>();" + Globals.lineSep +
              "linkedlist_str0.addFirst(\"hi!\");" + Globals.lineSep +
              "int i3 = linkedlist_str0.size();" + Globals.lineSep +
              "java.util.TreeSet<java.lang.String> treeset_str4 = new java.util.TreeSet<java.lang.String>((java.util.Collection<java.lang.String>)linkedlist_str0);" + Globals.lineSep +
              "java.util.Set<java.lang.String> set_str5 = java.util.Collections.synchronizedSet((java.util.Set<java.lang.String>)treeset_str4);" + Globals.lineSep,
              s.toCodeString());
    } catch (NoSuchMethodException e) {
      fail("didn't find method: " + e.getMessage());
    }
  }
}
