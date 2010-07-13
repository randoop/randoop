package randoop.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import randoop.Globals;


/**
 * For each class, it can calculate the length of the shortest sequence of method calls that creates an object 
 * of the class (for abstract classes and interfaces: any concrete subclass), using only the classes in the provided set.
 */
public final class ClassComplexityCalculator {

  private final Map<Class<?>, Integer> counts;
  private final ClassHierarchy h;

  public ClassComplexityCalculator(Collection<Class<?>> classes) {
    counts= new LinkedHashMap<Class<?>, Integer>();
    h= new ClassHierarchy(ClassHierarchy.superClassClosure(new LinkedHashSet<Class<?>>(Reflection.relatedClasses(classes, 1))));
    computeForAll();
  }

  private Set<Class<?>> computingNow= new LinkedHashSet<Class<?>>();
  private Map<Class<?>, Integer> minComplexityOfSubclass= new LinkedHashMap<Class<?>, Integer>();
  private void computeForAll() {
    for (Class<?> c : h.getClasses()) {
//    System.out.println("Complexity for: " + c.getName());
      counts.put(c, internalClassComplexity(c));
    }
  }

  public int classComplexity(Class<?> c) {
    return counts.get(c);
  }

  private int internalClassComplexity(Class<?> c) {
    if (counts.containsKey(c))
      return counts.get(c);
    if (c.isPrimitive()) {
      counts.put(c, 1);
      return 1;            
    }
    if (c.isArray()) {
      int r= internalClassComplexity(c.getComponentType());
      counts.put(c, r);
      return r;
    }
    if (! Modifier.isPublic(c.getModifiers())) {
      counts.put(c, Integer.MAX_VALUE);
      return Integer.MAX_VALUE;
    }
    if (c.isAnonymousClass()) {
      counts.put(c, Integer.MAX_VALUE);
      return Integer.MAX_VALUE;
    }

    if (! isConcreteClass(c)) {
      int r= minComplexityOfSubclass(c);
      counts.put(c, r);
      return r;
    }
    if (computingNow.contains(c))
      throw new CircularityException(c);
    computingNow.add(c);
    int complexity= Integer.MAX_VALUE;
    for (Constructor<?> ctor : c.getConstructors()) {
      int ctorComplexity= ctorComplexity(ctor);
      complexity= Math.min(complexity, ctorComplexity); 
    }
    computingNow.remove(c);
    counts.put(c, complexity);
    return complexity;
  }

  private boolean isConcreteClass(Class<?> c) {
    return ! c.isInterface() && ! Modifier.isAbstract(c.getModifiers());
  }

  private int ctorComplexity(Constructor<?> ctor) {
    try {
      int result= 0;
      for (Class<?> param : ctor.getParameterTypes()) {
        result= Math.max(result, internalClassComplexity(param));
      }
      if (result == Integer.MAX_VALUE)
        return result;
      return result + 1;
    } catch (CircularityException e) {
      return Integer.MAX_VALUE;
    }    
  }

  private int minComplexityOfSubclass(Class<?> c) {
    if (minComplexityOfSubclass.containsKey(c))
      return minComplexityOfSubclass.get(c);
//  System.out.println("minComplexityOfSubclass:" + c);
int result= Integer.MAX_VALUE;
for (Class<?> clazz : h.subClasses(c)) {
  if (isConcreteClass(clazz))
    result= Math.min(result, internalClassComplexity(clazz));
}
minComplexityOfSubclass.put(c, result);
return result;
  }

  class CircularityException extends RuntimeException{

    public Class<?> c;

    public CircularityException(Class<?> c) {
      super(c.getName());
      this.c= c;
    }

    private static final long serialVersionUID = 2081919221361409565L;
  }

  public static void main(String[] args) throws ClassNotFoundException {
    Set<Class<?>> classes= new LinkedHashSet<Class<?>>();
    args= args[0].split(Globals.lineSep);
    int classCount= 0;
    for (String className : args) {
      String classNa = className.substring(0, className.length() - ".class".length());
      System.out.println("loading " +classNa + " " + (classCount++) + " of " + args.length);
      try{
        classes.add(Class.forName(classNa));
      } catch (NoClassDefFoundError e) {
        System.out.println("Not found:" + classNa);
        // keep going
      }
    }
    System.out.println("loaded all");
    ClassComplexityCalculator ccc= new ClassComplexityCalculator(classes);
    // int count= 0;
    Histogram<Class<?>> h= new Histogram<Class<?>>();
    for (Class<?> c : classes) {
      // System.out.println((count++) + " of " + classes.size());
      h.addToCount(c, ccc.classComplexity(c));
    }
    System.out.println(h.toStringSortedByNumbers());
  }
}
