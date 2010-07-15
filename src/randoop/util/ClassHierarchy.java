package randoop.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * For a given set of classes, this object can answer queries about sub/superclass dependencies.
 */
public final class ClassHierarchy {
  private final Set<Class<?>> classes;
  private final MultiMap<Class<?>, Class<?>> classToSubclasses;
  private final MultiMap<Class<?>, Class<?>> classToSuperclasses;

  public ClassHierarchy(Collection<Class<?>> classes) {
    if (classes == null)
      throw new IllegalArgumentException("null argument");
    this.classes= Collections.unmodifiableSet(new LinkedHashSet<Class<?>>(classes));// copy
    this.classToSubclasses= new MultiMap<Class<?>, Class<?>>();
    this.classToSuperclasses= new MultiMap<Class<?>, Class<?>>();
    compute();
  }

  private void compute() {
    for (Class<?> clazz : classes) {
//    System.out.println("Hierarchy for: " + clazz.getName());
      internalSuperClasses(clazz);
    }
  }

  public Set<Class<?>> getClasses() {
    return this.classes;
  }

  private Collection<Class<?>> internalSuperClasses(Class<?> clazz) {
    if (classToSuperclasses.contains(clazz))           // already computed
      return classToSuperclasses.getValues(clazz);

    Set<Class<?>> result= new LinkedHashSet<Class<?>>();
    for (Class<?> directSuper : Reflection.getDirectSuperTypes(clazz)) {
      if (classes.contains(directSuper)) {
        result.addAll(internalSuperClasses(directSuper));
        result.add(directSuper);
      }
    }
    classToSuperclasses.addAll(clazz, result);
    return result;
  }

  public Set<Class<?>> superClasses(Class<?> c) {
    return Collections.unmodifiableSet(classToSuperclasses.getValues(c));
  }

  public Set<Class<?>> subClasses(Class<?> c) {
    if (classToSubclasses.contains(c)) {
//    System.out.println("subClasses:" + c.getName() + " precomputed");
      return Collections.unmodifiableSet(classToSubclasses.getValues(c));
    }
    Set<Class<?>> result= new LinkedHashSet<Class<?>>();
    for (Class<?> eachClass : classes) {
      if (superClasses(eachClass).contains(c))
        result.add(eachClass);
    }
    classToSubclasses.put(c, result);
    if (!classToSubclasses.contains(c)) {
      throw new IllegalStateException(c.getName() + " : " + result.toString() + " all:" + CollectionsExt.toStringInSortedLines(getClasses()));
    }
    return Collections.unmodifiableSet(result);
  }

  // Should this go to Reflection?
  /**
   * Returns the set of all direct and indirect superclasses and superinterfaces of the given class.
   * The returned set includes also the class itself.
   */
  public static Set<Class<?>> superClassClosure(Set<Class<?>> classes) {
    Set<Class<?>> result= new LinkedHashSet<Class<?>>();
    for (Class<?> c : classes) {
      result.addAll(superClassClosure(c));
    }
    return result;
  }

  public static Set<Class<?>> superClassClosure(Class<?> c) {
    Set<Class<?>> result= new LinkedHashSet<Class<?>>();
    result.add(c);
    for (Class<?> directSuper : Reflection.getDirectSuperTypes(c)) {
      result.addAll(superClassClosure(directSuper));
    }
    return result;
  }
}
