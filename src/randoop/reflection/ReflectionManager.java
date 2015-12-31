package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import randoop.util.Log;

/**
 * ReflectionManager reflectively visits a {@link Class} instance to apply a set 
 * of {@link ClassVisitor} objects to the class members. 
 * Uses a {@link ReflectionPredicate} and heuristics to determine which classes 
 * and class members to visit.
 * May be restricted to visit only public members of a class, but, otherwise,
 * accessibility rules of the reflection predicate are used.
 * 
 * For a non-enum class visits:
 * <ul>
 * <li> all methods satisfying predicate.
 * <li> all constructors satisfying predicate.
 * <li> all fields that satisfy predicate and are not hidden. (A hidden field is a member of 
 *   superclass with field of same name in current class. These are accessible via reflection.).
 * <li> inner enums satisfying predicate.
 * </ul>
 * 
 * For an enum visits:
 * <ul>
 * <li> all enum constants.
 * <li> methods of the enum satisfying predicate other than <code>values</code> and <code>valueOf</code>.
 * <li> methods defined for enum constants that satisfy predicate.
 * </ul>
 */
public class ReflectionManager {

  private ReflectionPredicate predicate;
  private ArrayList<ClassVisitor> visitors;
  private boolean publicMembersOnly;

  /**
   * Creates a manager object that uses the given predicate to determine which 
   * classes, methods and constructors should be visited. 
   * The list of visitors is initially empty.
   * 
   * @param predicate is used to determine whether class and its members should 
   *        be visited.
   * @param publicMembersOnly  the flag to indicate whether only public members
   * of a class should be visited.
   */
  public ReflectionManager(ReflectionPredicate predicate, boolean publicMembersOnly) {
    this.predicate = predicate;
    this.visitors = new ArrayList<>();
    this.publicMembersOnly = publicMembersOnly;
  }

  /**
   * Registers a {@link ClassVisitor} for use by the 
   * {@link ReflectionManager#apply(Class)} method.
   * 
   * @param visitor a {@link ClassVisitor} object.
   */
  public void add(ClassVisitor visitor) {
    visitors.add(visitor);
  }

  /**
   * Applies the registered {@link ClassVisitor} objects of this object to the
   * given class.
   *  
   * @param c a {@link Class} object to be visited.
   */
  public void apply(Class<?> c) {
    
    if (predicate.test(c)) {

      if (Log.isLoggingOn()) Log.logLine("Applying visitors to class " + c.getName());

      visitBefore(c); //perform any previsit steps
      
      if (c.isEnum()) { //treat enum classes differently
        applyEnum(c);
      } else {

        applyMethods(c);

        for (Constructor<?> co : c.getDeclaredConstructors()) {
          if (predicate.test(co)) {
            visitConstructor(co);
          }   
        }

        for (Class<?> ic : c.getDeclaredClasses()) { //look for inner enums
          if (ic.isEnum() && predicate.test(ic)) {
            applyEnum(ic);
          }
        }

        applyFields(c);

      }
      
      visitAfter(c);
    }
    
  }

  /**
   * Applies the visitors to the methods of the class. 
   * Will visit all methods unless visiting only public members.
   * 
   * @param c  the class whose methods should be visited
   */
  private void applyMethods(Class<?> c) {
    Set<Method> methods = new HashSet<>();
    for (Method m : c.getMethods()) {
      if (predicate.test(m)) {
        visitMethod(m);
      }
    }
    if (! publicMembersOnly) {
      for (Method m : c.getDeclaredMethods()) {
        if ((! methods.contains(m)) && predicate.test(m)) {
          visitMethod(m);
        }
      }
    }
  }
  
  /**
   * Applies the visitors to the constants and methods of the given enum. 
   * A method is included if it satisfies the predicate, and either is declared
   * in the enum, or in the anonymous class of some constant.
   * Note that methods will either belong to the enum itself, or to an anonymous
   * class attached to a constant. Ordinarily, the type of the constant is the 
   * enum, but when there is an anonymous class for constant e, e.getClass() 
   * returns the anonymous class. This is used to check for method overrides 
   * (could include Object methods) within the constant.
   * 
   * Heuristically exclude methods <code>values</code> and <code>valueOf</code>
   * since their definition is implicit, and we aren't testing Java enum 
   * implementation.
   *
   * @param c enum class object from which constants and methods are extracted
   */
  private void applyEnum(Class<?> c) {
    Set<String> overrideMethods = new HashSet<String>();
    for (Object obj : c.getEnumConstants()) {
      Enum<?> e = (Enum<?>)obj;
      visitEnum(e);
      if (!e.getClass().equals(c)) { //does constant have an anonymous class?
        for (Method m : e.getClass().getDeclaredMethods()) {
          overrideMethods.add(m.getName()); //collect any potential overrides
        }
      }
    }
    //get methods that are explicitly declared in the enum
    for (Method m : c.getDeclaredMethods()) {
      if (predicate.test(m)) {
        if (!m.getName().equals("values") && !m.getName().equals("valueOf")) {
          visitMethod(m);
        }
      }
    }
    //get any inherited methods also declared in anonymous class of some constant
    for (Method m : c.getMethods()) { 
      if (predicate.test(m) && overrideMethods.contains(m.getName())) {
        visitMethod(m);
      }
    }
  }
  
  /**
   * Determines which fields of the given class the visitors will be applied to. 
   * Only excludes fields hidden by inheritance that are otherwise still 
   * accessible via reflection.
   * 
   * @param c
   */
  private void applyFields(Class<?> c) {
    //The set of fields declared in class c is needed to ensure we don't collect
    //inherited fields that are hidden by local declaration
    Set<String> declaredNames = new TreeSet<>(); //get names of fields declared
    for (Field f : c.getDeclaredFields()) {
      declaredNames.add(f.getName());
    }
    for (Field f : c.getFields()) { //for all public fields
      //keep a field that satisfies filter, and is not inherited and hidden by local declaration
      if (predicate.test(f) && 
          (!declaredNames.contains(f.getName()) || 
              c.equals(f.getDeclaringClass()))) {
        visitField(f);
      }
    }
  }

  /**
   * Apply all registered visitors to a field.
   * 
   * @param f  the field to be visited.
   */
  private void visitField(Field f) {
    if (Log.isLoggingOn()) {
      Log.logLine(String.format("Considering field %s", f));
    }
    for (ClassVisitor v : visitors) {
      v.visit(f);
    }
  }

  /**
   * Apply all registered visitors to the constructor.
   * 
   * @param co  the constructor to be visited.
   */
  private void visitConstructor(Constructor<?> co) {
    if (Log.isLoggingOn()) {
      Log.logLine(String.format("Considering constructor %s", co));
    }
    for (ClassVisitor v : visitors) {
      v.visit(co);
    }
  }

  /**
   * Apply all registered visitors to the method.
   * 
   * @param m  the method to be visited.
   */
  private void visitMethod(Method m) {
    if (Log.isLoggingOn()) {
      Log.logLine(String.format("Considering method %s", m));
    }
    for (ClassVisitor v : visitors) {
      v.visit(m);
    }
  }
  
  /**
   * Apply all registered visitors to the enum value.
   * 
   * @param e  the enum value to be visited.
   */
  private void visitEnum(Enum<?> e) {
    if (Log.isLoggingOn()) {
      Log.logLine(String.format("Considering enum %s", e));
    }
    for (ClassVisitor v : visitors) {
      v.visit(e);
    }
  }

  /**
   * Apply all registered visitors to the class.
   * Called at the end of {@link #apply(Class)}.
   * 
   * @param c  the class to be visited.
   */
  private void visitAfter(Class<?> c) {
    for (ClassVisitor v : visitors) {
      v.visitAfter(c);
    }
  }

  /**
   * Apply all registered visitors to the class.
   * Called at the beginning of {@link #apply(Class)}.
   * 
   * @param c  the class to be visited.
   */
  private void visitBefore(Class<?> c) {
    for (ClassVisitor v : visitors) {
      v.visitBefore(c);
    }
  }

}
