package randoop;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import randoop.util.ArrayListSimpleList;
import randoop.util.Randomness;
import randoop.util.SimpleList;
import randoop.util.Reflection.Match;


public class HelperSequenceCreator {

  /**
   * Returns a sequence that creates an object of type compatible the given class.
   * Wraps the object in a list, and returns the list.
   *
   * CURRENTLY, will return a sequence (i.e. a non-empty list) only if cls is an array.
   */
  public static SimpleList<Sequence> createSequence(Class<?> cls, SequenceCollection components) {

    if (!cls.isArray()) {
      return new ArrayListSimpleList<Sequence>();
    }

    Sequence s = null;

    if (cls.getComponentType().isPrimitive()) {
      s = randPrimitiveArray(cls.getComponentType());
    } else {
      SimpleList<Sequence> candidates = components.getSequencesForType(cls.getComponentType(), false);
      ArrayDeclaration decl = new ArrayDeclaration(cls.getComponentType(), 1);
      if (candidates.isEmpty()) {
        // Return the array [ null ]
        s = new Sequence();
        s = s.extend(PrimitiveOrStringOrNullDecl.nullOrZeroDecl(cls.getComponentType()));
        List<Variable> ins = new ArrayList<Variable>();
        ins.add(s.getVariable(0));
        s = s.extend(decl, ins);
      } else {
        // Return the array [ x ] where x is the last value in the sequence.
        s = candidates.get(Randomness.nextRandomInt(candidates.size()));
        List<Variable> ins = new ArrayList<Variable>();
        // XXX this assumes that last statement will have such a var,
        // which I know is currently true because of SequenceCollection implementation.
        ins.add(s.randomVariableForTypeLastStatement(cls.getComponentType(), Match.COMPATIBLE_TYPE));
        s = s.extend(decl, ins);
      }
    }
    assert s != null;
    ArrayListSimpleList<Sequence> l = new ArrayListSimpleList<Sequence>();
    l.add(s);
    return l;
  }

  private static Sequence randPrimitiveArray(Class<?> componentType) {
    assert componentType.isPrimitive();
    Set<Object> potentialElts = SeedSequences.getSeeds(componentType);
    int length = Randomness.nextRandomInt(4);
    Sequence s = new Sequence();
    List<Variable> emptylist = new ArrayList<Variable>();
    for (int i = 0 ; i < length ; i++) {
      Object elt = Randomness.randomSetMember(potentialElts);
      s = s.extend(new PrimitiveOrStringOrNullDecl(componentType, elt), emptylist);
    }
    List<Variable> inputs = new ArrayList<Variable>();
    for (int i = 0 ; i < length ; i++) {
      inputs.add(s.getVariable(i));
    }
    s = s.extend(new ArrayDeclaration(componentType, length), inputs);
    return s;
  }

}
