package randoop.sequence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.types.ConcreteType;
import randoop.types.ConcreteTypes;
import randoop.types.RandoopTypeException;
import randoop.util.ListOfLists;
import randoop.util.SimpleList;

/**
 * For a given class C, ClassLiterals maps C (if present) to a collection of
 * literals (represented as single-element sequences) that can be used as inputs
 * to members of the given class.
 */
public class ClassLiterals extends MappedSequences<ConcreteType> {

  @Override
  public void addSequence(ConcreteType key, Sequence seq) {
    if (seq == null) throw new IllegalArgumentException("seq is null");
    if (!seq.isPrimitive()) {
      throw new IllegalArgumentException("seq is not a primitive sequence");
    }
    super.addSequence(key, seq);
  }

  private static final Map<ConcreteType, Set<ConcreteType>> hashedSuperClasses =
      new LinkedHashMap<>();

  @Override
  public SimpleList<Sequence> getSequences(ConcreteType key, ConcreteType desiredType) {

    Set<ConcreteType> superClasses = hashedSuperClasses.get(key);
    if (superClasses == null) {
      superClasses = getSuperClasses(key);
      hashedSuperClasses.put(key, superClasses);
    }
    List<SimpleList<Sequence>> listOfLists = new ArrayList<>();
    listOfLists.add(super.getSequences(key, desiredType));
    for (ConcreteType c : superClasses) {
      listOfLists.add(super.getSequences(c, desiredType));
    }
    return new ListOfLists<>(listOfLists);
  }

  // Gets superclasses for the given class. Stops at null or Object (excludes
  // Object from result).
  private Set<ConcreteType> getSuperClasses(ConcreteType cls) {
    Set<ConcreteType> ret = new LinkedHashSet<>();
    try {
      ConcreteType sup = cls.getSuperclass();
      while (sup != null && !sup.equals(ConcreteTypes.OBJECT_TYPE)) {
        ret.add(sup);
        sup = sup.getSuperclass();
      }
    } catch (RandoopTypeException e) {
      System.out.println("Type error for class " + cls.getName() + ": " + e);
    }
    return ret;
  }
}
