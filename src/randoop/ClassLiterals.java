package randoop;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.util.ListOfLists;
import randoop.util.SimpleList;

/**
 * For a given class C, ClassLiterals maps C (if present) to a
 * collection of literals (represented as single-element sequences)
 * that can be used as inputs to members of the given class.
 */
public class ClassLiterals extends MappedSequences<Class<?>> {
  
  @Override
  public void addSequence(Class<?> key, Sequence seq) {
    if (seq == null) throw new IllegalArgumentException("seq is null");
    if (!seq.isPrimitive()) {
      throw new IllegalArgumentException("seq is not a primitive sequence");
    }
    super.addSequence(key, seq);
  }
  
  private static final Map<Class<?>, Set<Class<?>>> hashedSuperClasses =
    new LinkedHashMap<Class<?>, Set<Class<?>>>();
  
  @Override
  public SimpleList<Sequence> getSequences(Class<?> key, Class<?> desiredType) {
    
    Set<Class<?>> superClasses = hashedSuperClasses.get(key);
    if (superClasses == null) {
      superClasses = getSuperClasses(key);
      hashedSuperClasses.put(key, superClasses);
    }
    List<SimpleList<Sequence>> listOfLists = new ArrayList<SimpleList<Sequence>>(); 
    listOfLists.add(super.getSequences(key, desiredType));
    for (Class<?> c : superClasses) {
      listOfLists.add(super.getSequences(c, desiredType));
    }
    return new ListOfLists<Sequence>(listOfLists);
  }

  // Gets superclasses for the given class. Stops at null or Object (excludes Object from result).
  private Set<Class<?>> getSuperClasses(Class<?> cls) {
    Set<Class<?>> ret = new LinkedHashSet<Class<?>>();
    Class<?> sup = cls.getSuperclass();
    while (sup != null && !sup.equals(Object.class)) {
      ret.add(sup);
      sup = sup.getSuperclass();
    }
    return ret;
  }

}
