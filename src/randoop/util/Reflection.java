package randoop.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
// For Java 8: import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import randoop.Globals;
import randoop.main.GenInputsAbstract;
import randoop.operation.ConstructorCall;
import randoop.operation.EnumConstant;
import randoop.operation.FieldGetter;
import randoop.operation.FieldSetter;
import randoop.operation.InstanceField;
import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.operation.OperationParseException;
import randoop.operation.OperationParser;
import randoop.operation.StaticField;
import randoop.operation.StaticFinalField;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.ReflectionPredicate;

import plume.EntryReader;
import plume.Pair;
import plume.UtilMDE;

/** Utility methods that operate on reflection objects (classes, methods, etc.). */
public final class Reflection {

  /**
   * Used by methods that that a java.lang.Class&lt;?&gt; object as
   * argument and use it to compute something based on it.
   */
  public static enum Match { EXACT_TYPE, COMPATIBLE_TYPE }

  static Map<String, Member> cached_deserializeMethodOrCtor =
      new LinkedHashMap<String, Member>();

  private Reflection() {
    // no instance
  }

  /**
   * Returns the set of classes that appear, recursively, in the interface of
   * the given class, to a given depth. For example, if class C1 declares
   * only method foo(C2)/C3,  and class C2 declares method bar(C4)/C5, then:
   *
   * We say that C1, C2 and C3 are related to C1 at depth &ge; 0.
   * We say that C4 and C5 are related to C1 at depth &ge; 1.
   *
   * We say that a class C2 appears in the interface of C iff:
   * (1) C2 is C
   * (2) C2 is a return value of some method in C.getMethods()
   * (2) C2 is a parameter of some method in C.getMethods() or some
   *     constructor in C.getConstructors().
   */
  public static Set<Class<?>> relatedClasses(Class<?> clazz, int depth) {
    if (clazz == null)
      throw new IllegalArgumentException("clazz cannot be null.");
    if (depth < 0)
      throw new IllegalArgumentException("depth must be non-negative.");
    return relatedClassesInternal(Collections.<Class<?>>singleton(clazz), depth);
  }

  public static Set<Class<?>> relatedClasses(Collection<Class<?>> classes, int i) {
    Set<Class<?>> result = new LinkedHashSet<Class<?>>();
    for (Class<?> c : classes) {
      result.addAll(relatedClasses(c, i));
    }
    return result;
  }

  private static Set<Class<?>> relatedClassesInternal(Set<Class<?>> classes, int depth) {
    if (depth < 0)
      return classes;
    Set<Class<?>> acc = new LinkedHashSet<Class<?>>();
    for (Class<?> c : classes) {
      acc.addAll(classesAppearingInInterface(c));
    }
    return relatedClassesInternal(acc, depth - 1);
  }

  private static Set<Class<?>> classesAppearingInInterface(Class<?> c) {
    Set<Class<?>> retval = new LinkedHashSet<Class<?>>();
    retval.add(c);
    for (Method m : c.getMethods()) {
      retval.add(m.getReturnType());
      retval.addAll(Arrays.asList(m.getParameterTypes()));
    }
    for (Constructor<?> cons : c.getConstructors()) {
      retval.addAll(Arrays.asList(cons.getParameterTypes()));
    }
    return Collections.unmodifiableSet(retval);
  }

  public static final Comparator<Member> SORT_MEMBERS_BY_NAME = new Comparator<Member>() {
    public int compare(Member o1, Member o2) {
      return o1.toString().compareTo(o2.toString());
    }
  };

  /**
   * Like Class.getMethods(), but guarantees always same order.
   */
  public static Method[] getMethodsOrdered(Class<?> c) {
    if (c == null) {
      throw new IllegalArgumentException("c cannot be null.");
    }
    List<Method> ms = new ArrayList<Method>();
    try {
      ms.addAll(Arrays.asList(c.getMethods()));
      // System.out.printf ("methods = %s%n", Arrays.toString(c.getMethods()));
      ms.addAll(Arrays.asList(c.getDeclaredMethods()));
    } catch (Exception e) {
      System.out.println("getMethodsOrdered(" + c + "): " + e.getMessage());
    } catch (Error e) {
      System.out.println("getMethodsOrdered(" + c + "): " + e.getMessage());
    }
    Method[] ret = ms.toArray(new Method[0]);
    Arrays.sort(ret, SORT_MEMBERS_BY_NAME);
    return ret;
  }

  /**
   * Like Class.getDeclaredMethods(), but guarantees always same order.
   */
  public static Method[] getDeclaredMethodsOrdered(Class<?> c) {
    if (c == null) {
      throw new IllegalArgumentException("c cannot be null.");
    }
    Method[] ret = c.getDeclaredMethods();
    Arrays.sort(ret, SORT_MEMBERS_BY_NAME);
    return ret;
  }

  /**
   * Like Class.getConstructors(), but guarantees always same order.
   */
  public static Constructor<?>[] getConstructorsOrdered(Class<?> c) {
    if (c == null) {
      throw new IllegalArgumentException("c cannot be null.");
    }
    Constructor<?>[] ret = c.getConstructors();
    Arrays.sort(ret, SORT_MEMBERS_BY_NAME);
    return ret;
  }

  /**
   * Like Class.getDeclaredConstructors(), but guarantees always same order.
   */
  public static Constructor<?>[] getDeclaredConstructorsOrdered(Class<?> c) {
    if (c == null) {
      throw new IllegalArgumentException("c cannot be null.");
    }
    Constructor<?>[] ret;
    try {
      ret = c.getDeclaredConstructors();
    } catch (Exception e) {
      System.out.println("getDeclaredConstructorsOrdered(" + c + "): " + e.getMessage());
      ret = new Constructor<?>[0];
    }
    Arrays.sort(ret, SORT_MEMBERS_BY_NAME);
    return ret;
  }

  /**
   * Gets the class corresponding to the given string. Assumes the string is
   * in the format output by the method java.lang.Class.toString().
   */
  public static Class<?> classForName(String classname) {
    return classForName(classname, false);
  }

  /**
   * Gets the class corresponding to the given string. Assumes the string is
   * in the format output by the method java.lang.Class.toString().
   * 
   * If noerr==true and Class.forName(classname) throws an exception, throws an Error.
   * 
   * If noerr==false and Class.forName(classname) throws an exception, returns null.
   */
  public static Class<?> classForName(String classname, boolean noerr) {

    Class<?> c = PrimitiveTypes.typeNameToPrimitiveOrString.get(classname);
    if (c != null)
      return c;

    try {
      c = Class.forName(classname);
    } catch (Throwable e) {
      if (noerr) {
        System.out.printf("WARNING: classForName(%s) yielded exception: %s%n",
            classname, e.getMessage());
        e.printStackTrace(System.out);
        return null;
      } else {
        throw new Error(String.format("classForName(%s)", classname), e);
      }
    }
    return c;
  }

  private static Set<Class<?>> getInterfacesTransitive(Class<?> c1) {

    Set<Class<?>> ret = new LinkedHashSet<Class<?>>();

    Class<?>[] c1Interfaces = c1.getInterfaces();
    for (int i = 0; i < c1Interfaces.length; i++) {
      ret.add(c1Interfaces[i]);
      ret.addAll(getInterfacesTransitive(c1Interfaces[i]));
    }

    Class<?> superClass = c1.getSuperclass();
    if (superClass != null)
      ret.addAll(getInterfacesTransitive(superClass));

    return ret;
  }

  public static Set<Class<?>> getDirectSuperTypes(Class<?> c) {
    Set<Class<?>> result= new LinkedHashSet<Class<?>>();
    Class<?> superclass = c.getSuperclass();
    if (superclass != null)
      result.add(superclass);
    result.addAll(Arrays.<Class<?>>asList(c.getInterfaces()));
    return result;
  }

  /**
   * Preconditions (established because this method is only called from
   * canBeUsedAs): params are non-null, are not Void.TYPE, and are not
   * isInterface().
   *
   * @param c1
   * @param c2
   */
  private static boolean isSubclass(Class<?> c1, Class<?> c2) {
    assert(c1 != null);
    assert(c2 != null);
    assert(!c1.equals(Void.TYPE));
    assert(!c2.equals(Void.TYPE));
    assert(!c1.isInterface());
    assert(!c2.isInterface());
    return c2.isAssignableFrom(c1);
  }


  private static Map<Pair<Class<?>, Class<?>>, Boolean> canBeUsedCache =
      new LinkedHashMap<Pair<Class<?>, Class<?>>, Boolean>();

  public static long num_times_canBeUsedAs_called = 0;

  /**
   * Checks if an object of class c1 can be used as an object of class c2.
   * This is more than subtyping: for example, int can be used as Integer,
   * but the latter is not a subtype of the former.
   */
  public static boolean canBeUsedAs(Class<?> c1, Class<?> c2) {
    if (c1 == null || c2 == null)
      throw new IllegalArgumentException("Parameters cannot be null.");
    if (c1.equals(c2))
      return true;
    if (c1.equals(void.class) && c2.equals(void.class))
      return true;
    if (c1.equals(void.class) || c2.equals(void.class))
      return false;
    Pair<Class<?>, Class<?>> classPair = new Pair<Class<?>, Class<?>>(c1, c2);
    Boolean cachedRetVal = canBeUsedCache.get(classPair);
    boolean retval;
    if (cachedRetVal == null) {
      retval = canBeUsedAs0(c1, c2);
      canBeUsedCache.put(classPair, retval);
    } else {
      retval = cachedRetVal;
    }
    return retval;
  }

  // TODO testclasses array code (third if clause)
  private static boolean canBeUsedAs0(Class<?> c1, Class<?> c2) {
    if (c1.isArray()) {
      if (c2.equals(Object.class))
        return true;
      if (!c2.isArray())
        return false;
      Class<?> c1SequenceType = c1.getComponentType();
      Class<?> c2componentType = c2.getComponentType();

      if (c1SequenceType.isPrimitive()) {
        if (c2componentType.isPrimitive()) {
          return (c1SequenceType.equals(c2componentType));
        } else {
          return false;
        }
      } else {
        if (c2componentType.isPrimitive()) {
          return false;
        } else {
          c1 = c1SequenceType;
          c2 = c2componentType;
        }
      }
    }

    if (c1.isPrimitive())
      c1 = PrimitiveTypes.boxedType(c1);
    if (c2.isPrimitive())
      c2 = PrimitiveTypes.boxedType(c2);

    boolean ret = false;

    if (c1.equals(c2)) { // XXX redundant (see canBeUsedAs(..)).
      ret = true;
    } else if (c2.isInterface()) {
      Set<Class<?>> c1Interfaces = getInterfacesTransitive(c1);
      if (c1Interfaces.contains(c2))
        ret = true;
      else
        ret = false;
    } else if (c1.isInterface()) {
      // c1 represents an interface and c2 a class.
      // The only safe possibility is when c2 is Object.
      if (c2.equals(Object.class))
        ret = true;
      else
        ret = false;
    } else {
      ret = isSubclass(c1, c2);
    }
    return ret;
  }

  /**
   * Checks whether the inputs can be used as arguments for the specified parameter types.
   * This method considers "null" as always being a valid argument.
   * errMsgContext is uninterpreted - just printed in error messages
   * Returns null if inputs are OK wrt paramTypes. Returns error message otherwise.
   */
  public static String checkArgumentTypes(Object[] inputs, Class<?>[] paramTypes, Object errMsgContext) {
    if (inputs.length != paramTypes.length)
      return "Bad number of parameters for " + errMsgContext + " was:" + inputs.length;

    for (int i = 0; i < paramTypes.length; i++) {
      Object input= inputs[i];
      Class<?> pType = paramTypes[i];
      if (! canBePassedAsArgument(input, pType))
        return "Invalid type of argument at pos " + i + " for:" + errMsgContext + " expected:" + pType + " was:"
        + (input == null ? "n/a(input was null)" : input.getClass());
    }
    return null;
  }

  /**
   * Returns whether the input can be used as argument for the specified parameter type.
   */
  public static boolean canBePassedAsArgument(Object inputObject, Class<?> parameterType) {
    if (parameterType == null || parameterType.equals(Void.TYPE))
      throw new IllegalStateException("Illegal type of parameter " + parameterType);
    if (inputObject == null) {
      return true;
    } else if (! Reflection.canBeUsedAs(inputObject.getClass(), parameterType)) {
      return false;
    } else
      return true;
  }

  /**
   * Returns a list of classes, given a list of class names.
   * 
   * if noerr=true, any classnames where Class.forName(classname) are ignored
   * and not added to the list. Otherwise, an exception is thrown in this situation.
   */
  public static List<Class<?>> loadClassesFromList(List<String> classNames, boolean noerr) {

    List<Class<?>> result = new ArrayList<Class<?>>(classNames.size());
    for (String className : classNames) {
      Class<?> c;
      try {
        c = classForName(className, noerr);
      } catch (Error e) {
        System.err.println("class not found: " + className);
        throw e;
      }
      if (c != null) {
        result.add(c);
      }
    }
    return result;
  }

  /** Blank lines and lines starting with "#" are ignored.
   *  Other lines must contain string such that Class.forName(s) returns a class.
   */
  public static List<Class<?>> loadClassesFromStream(InputStream in, String filename) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    return loadClassesFromReader(reader, filename, false);
  }

  /** Blank lines and lines starting with "#" are ignored.
   *  Other lines must contain string such that Class.forName(s) returns a class.
   */
  public static List<Class<?>> loadClassesFromReader(BufferedReader reader, String filename) {
    return loadClassesFromReader(reader, filename, false);
  }

  /** Blank lines and lines starting with "#" are ignored.
   *  Other lines must contain string such that Class.forName(s) returns a class.
   */
  public static List<Class<?>> loadClassesFromReader(BufferedReader reader, String filename, boolean noerr) {
    List<Class<?>> result = new ArrayList<Class<?>>();
    EntryReader er = new EntryReader(reader, filename, "^#.*", null);
    for (String line : er) {
      String trimmed = line.trim();
      Class<?> c = classForName(trimmed, noerr);
      if (c != null) {
        result.add(c);
      }
    }
    return result;
  }

  /** Blank lines and lines starting with "#" are ignored.
   *  Other lines must contain string such that Class.forName(s) returns a class.
   */
  public static List<Class<?>> loadClassesFromFile(File classListingFile) throws IOException {
    return loadClassesFromFile(classListingFile, false);
  }

  /** Blank lines and lines starting with "#" are ignored.
   *  Other lines must contain string such that Class.forName(s) returns a class.
   */
  public static List<Class<?>> loadClassesFromFile(File classListingFile, boolean noerr) throws IOException {
    BufferedReader reader = null;
    try {
      reader = Files.getFileReader(classListingFile);
      return loadClassesFromReader(reader, classListingFile.getPath(), noerr);
    } finally {
      if (reader != null)
        reader.close();
    }
  }

  /** Blank lines and lines starting with "#" are ignored.
   *  Other lines must contain string such that Class.forName(s) returns a class.
   */
  public static List<Member> loadMethodsAndCtorsFromStream(InputStream in) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    return loadMethodsAndCtorsFromReader(reader);
  }

  /** Blank lines and lines starting with "#" are ignored.
   *  Other lines must contain string such that Class.forName(s) returns a class.
   */
  public static List<Member> loadMethodsAndCtorsFromReader(BufferedReader reader) {
    try {
      List<String> lines= Files.readWhole(reader);
      return loadMethodsAndCtorsFromLines(lines);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Blank lines and lines starting with "#" are ignored.
   *  Other lines must contain string representing a method or constructor signature.
   */
  public static List<Member> loadMethodsAndCtorsFromLines(List<String> lines) {
    List<Member> result = new ArrayList<Member>(lines.size());
    for (String line : lines) {
      String trimmed = line.trim();
      if (trimmed.equals("") || trimmed.startsWith("#"))
        continue;
      Operation stk;
      try {
        stk = OperationParser.parse(line);
      } catch (OperationParseException e) {
        throw new Error(e);
      }
      if (stk instanceof MethodCall) {
        result.add(((MethodCall)stk).getMethod());
      } else {
        assert (stk instanceof ConstructorCall);
        result.add(((ConstructorCall)stk).getConstructor());
      }
    }
    return result;
  }

  /** Blank lines and lines starting with "#" are ignored.
   *  Other lines must contain string such that Class.forName(s) returns a class.
   */
  public static List<Member> loadMethodsAndCtorsFromFile(File classListingFile) throws IOException {
    BufferedReader reader = null;
    try {
      reader = Files.getFileReader(classListingFile);
      return loadMethodsAndCtorsFromReader(reader);
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } finally {
      if (reader != null)
        reader.close();
    }
  }

  private static Map<Class<?>, Boolean> cached_isVisible =
      new LinkedHashMap<Class<?>, Boolean>();

  public static boolean isVisible(Class<?> c) {

    Boolean cached = cached_isVisible.get(c);
    if (cached == null) {
      if (c.isAnonymousClass()) {
        cached = false;
      } else {
        int mods = c.getModifiers();
        boolean classVisible = isVisible (mods);
        if (c.isMemberClass())
          cached = classVisible && isVisible(c.getDeclaringClass());
        else
          cached = classVisible;
      }
      cached_isVisible.put(c, cached);
    }
    assert cached != null;
    return cached;
  }

  /**
   * isAbstract checks to see if class is abstract.
   * Note: an enum can look like an abstract class under certain circumstances,
   * but this returns false for enums.
   * 
   * @param c class to test.
   * @return true if non-enum class that is abstract, false otherwise.
   */
  public static boolean isAbstract(Class<?> c) {
    return !(c.isEnum()) && Modifier.isAbstract (c.getModifiers());
  }

  public static void saveClassesToFile(List<Class<?>> classes, String file) throws IOException {
    FileWriter fw = new FileWriter(file);
    for (Class<?> s:classes) {
      fw.append(s.getName() + Globals.lineSep);
    }
    fw.close();

  }

  /**
   * getStatements collects the methods, constructor and enum constants for a collection of classes.
   * Returns a filtered list of Operation objects.
   * 
   * @param classListing collection of class objects from which to extract.
   * @param filter filter object determines whether method/constructor/enum constant can be used.
   * @return list of Operation objects representing filtered set.
   */
  public static List<Operation> getStatements(Collection<Class<?>> classListing, ReflectionPredicate filter) {
    if (filter == null) filter = new DefaultReflectionPredicate();
    Set<Operation> statements = new LinkedHashSet<Operation>();
    for (Class<?> c : classListing) {
      // System.out.printf ("Considering class %s, filter = %s%n", c, filter);
      getStatementsForClass(filter, statements, c);
    }
    List<String> statementsAsString = new ArrayList<String>(); // For testing purposes.
    for (Operation st : statements)
      statementsAsString.add(st.toString());
    assert statementsAsString.size() == new LinkedHashSet<String>(statementsAsString).size();

    return new ArrayList<Operation>(statements);
  }

  /**
   * getStatementsForClass uses reflection to identify the methods, constructors and enum constants
   * of a particular class that meets the filter's canUse criteria, and returns the corresponding 
   * Operation objects. 
   * Note that it looks for inner enums, but not inner classes.
   *  
   * @param filter object that determines whether to extract from class, or to include members
   * @param statements collection of {@link Operation} objects constructed
   * @param c class object from which members are extracted
   */
  private static void getStatementsForClass(ReflectionPredicate filter, Set<Operation> statements, Class<?> c) {
    if (filter.canUse(c)) {
      
      if (Log.isLoggingOn()) Log.logLine("Will add members for class " + c.getName());

      if (c.isEnum()) {
        getEnumStatements(filter,statements,c);
      } else {
        
        for (Method m : getMethodsOrdered(c)) {
          if (Log.isLoggingOn()) {
            Log.logLine(String.format("Considering method %s", m));
          }
          if (filter.canUse(m)) {
            MethodCall mc = MethodCall.getRMethod(m);
            statements.add(mc);
          }
        }
        
        for (Constructor<?> co : getDeclaredConstructorsOrdered(c)) {
          // System.out.printf ("Considering constructor %s%n", co);
          if (filter.canUse(co)) {
            ConstructorCall mc = ConstructorCall.getRConstructor(co);
            statements.add(mc);
          }
        }
        
        for (Class<?> ic : c.getDeclaredClasses()) { //look for inner enums
          if (ic.isEnum() && filter.canUse(ic)) {
            getEnumStatements(filter,statements, ic);
          }
        }
        
        //The set of fields declared in class c is needed to ensure we don't collect
        //inherited fields that are hidden by local declaration
        Set<String> declaredNames = new TreeSet<>(); //get names of fields declared
        for (Field f : c.getDeclaredFields()) {
          declaredNames.add(f.getName());
        }
        for (Field f : c.getFields()) { //for all public fields
          //keep a field that satisfies filter, and is not inherited and hidden by local declaration
          if (filter.canUse(f) && (!declaredNames.contains(f.getName()) || c.equals(f.getDeclaringClass()))) {
            getFieldStatements(statements,f);
          }
        }
        
      }
    }
  }


  /**
   * getFieldStatements adds the {@link Operation} objects corresponding to 
   * getters and setters appropriate to the kind of field.
   * 
   * @param statements
   * @param f
   */
  private static void getFieldStatements(Set<Operation> statements, Field f) {
    int mods = f.getModifiers();
    if (Modifier.isPublic(mods)) {
      if (Modifier.isStatic(mods)) {
        if (Modifier.isFinal(mods)) {
          StaticFinalField s = new StaticFinalField(f);
          statements.add(new FieldGetter(s));
        } else {
          StaticField s = new StaticField(f);
          statements.add(new FieldGetter(s));
          statements.add(new FieldSetter(s));
        }
      } else {
        InstanceField i = new InstanceField(f);
        statements.add(new FieldGetter(i));
        statements.add(new FieldSetter(i));
      }
    }
    
  }

  /**
   * getEnumStatements gets and adds the enum constants and methods for the given class 
   * to the set of {@link Operation} objects. A method is included if it satisfies the filter,
   * and either is declared in the enum, or in the anonymous class of some constant.
   * If the class is not an enum, then nothing will be added to the statement set.
   * @param filter 
   * 
   * @param statements collection of {@link Operation} objects constructed
   * @param c class object from which enum constants are extracted
   */
  private static void getEnumStatements(ReflectionPredicate filter, Set<Operation> statements, Class<?> c) {
    //get enum constants and capture methods attached to them, if any
    Set<String> overrideMethods = new HashSet<String>();
    for (Object obj : c.getEnumConstants()) {
      Enum<?> e = (Enum<?>)obj;
      statements.add(new EnumConstant(e));
      if (!e.getClass().equals(c)) { //does constant have an anonymous class?
        for (Method m : e.getClass().getDeclaredMethods()) {
          overrideMethods.add(m.getName()); //collect any potential overrides
        }
      }
    }
    //get methods that are explicitly declared in the enum
    for (Method m : c.getDeclaredMethods()) {
      if (filter.canUse(m)) {
        if (!m.getName().equals("values") && !m.getName().equals("valueOf")) {
          statements.add(new MethodCall(m));
        }
      }
    }
    //get any inherited methods that seem to be overridden in anonymous class of some constant
    for (Method m : c.getMethods()) { 
      if (filter.canUse(m) && overrideMethods.contains(m.getName())) {
        statements.add(new MethodCall(m));
      }
    }
  }


  /** Sets the overloads field of each RMethod or RConstructor in the list. */
  public static void setOverloads(List<Operation> model) {
    for (Operation sk : model) {
      if (sk instanceof MethodCall) {
        MethodCall rm = (MethodCall) sk;
        rm.resetOverloads();
        Method m = rm.getMethod();

        List<Method> possibleOverloads;
        if (Modifier.isStatic(m.getModifiers())) {
          possibleOverloads = new ArrayList<Method>(Arrays.asList(m.getDeclaringClass().getDeclaredMethods()));
        } else {
          // Find the overloads in this and superclasses.
          possibleOverloads = new ArrayList<Method>(Arrays.asList(m.getDeclaringClass().getMethods()));
          // We should to find overloads in any subclass of a superclass
          // that declares the method.  For now, just look in the model.
          for (Operation possibleOverloadSk : model) {
            if (possibleOverloadSk instanceof MethodCall) {
              Method possibleOverload = ((MethodCall) possibleOverloadSk).getMethod();
              possibleOverloads.add(possibleOverload);
            }
          }
        }
        for (Method possibleOverload : possibleOverloads) {
          if (isOverload(m, possibleOverload)) {
            rm.addToOverloads(possibleOverload);
          }
        }
      } else if (sk instanceof ConstructorCall) {
        ConstructorCall rc = (ConstructorCall) sk;
        rc.resetOverloads();
        Constructor<?> c = rc.getConstructor();

        Constructor<?>[] possibleOverloads = c.getDeclaringClass().getDeclaredConstructors();

        for (Constructor<?> possibleOverload : possibleOverloads) {
          if (isOverload(c, possibleOverload)) {
            rc.addToOverloads(possibleOverload);
          }
        }
      }
    }
  }

  // It would be better to have one version of this routine with signature
  //   private static boolean isOverload(Executable m1, Executable m2) {
  // but the Executable class was introduced only in Java 8, so to retain
  // compatibility with earlier versions of Java, copy-and-paste two
  // identical versions.

  // Return true if the two methods or constructors overload one another
  // with the same number of arguments.
  private static boolean isOverload(Method m1, Method m2) {
    if (! m1.getClass().equals(m2.getClass()))
      // Require two Methods or two Constructors, no mismatch
      return false;
    if (! m1.getName().equals(m2.getName()))
      return false;
    // For Java 8: if (m1.getParameterCount() != m2.getParameterCount())
    if (m1.getParameterTypes().length != m2.getParameterTypes().length)
      return false;
    if (Modifier.isStatic(m1.getModifiers()) != Modifier.isStatic(m2.getModifiers()))
      return false;

    // Not needed; there is no harm to including this.
    // Class<?>[] paramTypes1 = m1.getParameterTypes();
    // Class<?>[] paramTypes2 = m2.getParameterTypes();
    // if (! Array.equals(paramTypes1, paramTypes2))
    //   return false;             // not an overload:  the identical signature!

    // Another test would be that some common subclass/subinterface of
    // their classes declares the method, to avoid methods of the same name
    // that have no ambiguity.

    return true;
  }

  // Return true if the two methods or constructors overload one another
  // with the same number of arguments.
  private static boolean isOverload(Constructor<?> m1, Constructor<?> m2) {
    if (! m1.getClass().equals(m2.getClass()))
      // Require two Methods or two Constructors, no mismatch
      return false;
    if (! m1.getName().equals(m2.getName()))
      return false;
    // For Java 8: if (m1.getParameterCount() != m2.getParameterCount())
    if (m1.getParameterTypes().length != m2.getParameterTypes().length)
      return false;
    if (Modifier.isStatic(m1.getModifiers()) != Modifier.isStatic(m2.getModifiers()))
      return false;

    // Not needed; there is no harm to including this.
    // Class<?>[] paramTypes1 = m1.getParameterTypes();
    // Class<?>[] paramTypes2 = m2.getParameterTypes();
    // if (! Array.equals(paramTypes1, paramTypes2))
    //   return false;             // not an overload:  the identical signature!

    // Another test would be that some common subclass/subinterface of
    // their classes declares the method, to avoid methods of the same name
    // that have no ambiguity.

    return true;
  }

  // Returns true if there exists a value that is of both types
  private static boolean typesCauseOverloadAmbiguity(Class<?> t1, Class<?> t2) {
    if (t1.isPrimitive())
      return false;
    if (t2.isPrimitive())
      return false;
    if (t1.isAssignableFrom(t2))
      return false;
    if (t2.isAssignableFrom(t1))
      return false;
    // An example value is null
    return true;
  }

  /**
   * To deserialize a list serialized with this method, use the
   * method deserializeClassList.
   */
  public static ArrayList<String> getNamesForClasses(ArrayList<Class<?>> cl) {
    if (cl == null) throw new IllegalArgumentException("cl should not be null.");
    // Create an ArrayList of Strings corresponding to the class names,
    // and serialize it.
    ArrayList<String> listToSerialize = new ArrayList<String>();
    for (Class<?> c : cl) {
      if (c == null) throw new IllegalArgumentException("classes in list should not be null.");
      listToSerialize.add(c.getName());
    }
    return listToSerialize;
  }

  @SuppressWarnings("unchecked")
  public static ArrayList<Class<?>> classesForNames(ArrayList<String> l) {
    if (l == null) throw new IllegalArgumentException("l should not be null.");
    ArrayList<Class<?>> ret = new ArrayList<Class<?>>();
    for (String className : l) {
      if (className == null) throw new IllegalArgumentException("class names in list should not be null.");
      ret.add(classForName(className));
    }
    return ret;
  }



  // XXX stolen from Constructor.toString - but we don't need modifiers or exceptions
  // and we need a slightly different format
  public static String getSignature(Constructor<?> c) {
    StringBuilder sb = new StringBuilder();
    sb.append(c.getName() + ".<init>(");
    Class<?>[] params = c.getParameterTypes();
    for (int j = 0; j < params.length; j++) {
      sb.append(params[j].getName());
      if (j < (params.length - 1))
        sb.append(",");
    }
    sb.append(")");
    return sb.toString();
  }

  // XXX stolen from Method.toString - but we don't need modifiers or exceptions
  public static String getSignature(Method m) {
    StringBuilder sb = new StringBuilder();
    sb.append(m.getDeclaringClass().getName() + ".");
    sb.append(m.getName() + "(");
    Class<?>[] params = m.getParameterTypes();
    for (int j = 0; j < params.length; j++) {
      sb.append(params[j].getName());
      if (j < (params.length - 1))
        sb.append(",");
    }
    sb.append(")");
    return sb.toString();
  }

  public static Method getMethodForSignature(String s) {
    return (Method) Reflection.getMemberForSignature(s, false);
  }

  public static Constructor<?> getConstructorForSignature(String s) {
    return (Constructor<?>) Reflection.getMemberForSignature(s, true);
  }

  private static Member getMemberForSignature(String s, boolean isCtor) {
    if (s == null) throw new IllegalArgumentException("s cannot be null.");
    Member m = cached_deserializeMethodOrCtor.get(s);
    if (m == null) {
      m = Reflection.memberForSignature2(s, isCtor);
    }
    cached_deserializeMethodOrCtor.put(s, m);
    return m;
  }

  private static Member memberForSignature2(String s, boolean isCtor) {
    int openPar = s.indexOf('(');
    int closePar = s.indexOf(')');
    // Verify only one open/close paren, and close paren is last char.
    assert openPar == s.lastIndexOf('(') : s;
    assert closePar == s.lastIndexOf(')') : s;
    assert closePar == s.length() - 1 : s;
    String clsAndMethod = s.substring(0, openPar);
    int lastDot = clsAndMethod.lastIndexOf('.');
    // There should be at least one dot, separating class/method name.
    assert lastDot >= 0;
    String clsName = clsAndMethod.substring(0, lastDot);
    String methodName = clsAndMethod.substring(lastDot + 1);
    if (isCtor)
      assert methodName.equals("<init>");
    String argsOneStr = s.substring(openPar + 1, closePar);

    // Extract parameter types.
    Class<?>[] argTypes = new Class<?>[0];
    if (argsOneStr.trim().length() > 0) {
      String[] argsStrs = argsOneStr.split(",");
      argTypes = new Class<?>[argsStrs.length];
      for (int i = 0 ; i < argsStrs.length ; i++) {
        argTypes[i] = classForName(argsStrs[i].trim());
      }
    }

    Class<?> cls = classForName(clsName);
    try {
      if (isCtor)
        return cls.getDeclaredConstructor(argTypes);
      else
        return cls.getDeclaredMethod(methodName, argTypes);
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  public static String throwPointToString(StackTraceElement throwPoint) {
    if (throwPoint == null)
      throw new IllegalArgumentException("throwPoint cannot be null.");
    StringBuilder b = new StringBuilder();
    b.append(throwPoint.getClassName());
    b.append(":");
    b.append(throwPoint.getMethodName());
    b.append(":");
    b.append(throwPoint.getFileName());
    b.append(":");
    b.append(throwPoint.getLineNumber());
    return b.toString();
  }

  public static StackTraceElement throwPointForName(String s) {
    if (s == null) throw new IllegalArgumentException("s cannot be null.");
    String[] split = s.split(":");
    assert split.length == 4;
    return new StackTraceElement(split[0], split[1], split[2], Integer.parseInt(split[3]));
  }


  /**
   * Returns a name that can be used in Java source for the given class.
   */
  public static String getCompilableName(Class<?> cls) {
    String retval = cls.getName();

    // If it's an array, it starts with "[".
    if (retval.charAt(0) == '[') {
      // Class.getName() returns a a string that is almost in JVML
      // format, except that it slashes are periods. So before calling
      // classnameFromJvm, we replace the period with slashes to
      // make the string true JVML.
      retval = UtilMDE.fieldDescriptorToBinaryName(retval.replace('.', '/'));
    }

    // If inner classes are involved, Class.getName() will return
    // a string with "$" characters. To make it compilable, must replace with
    // dots.
    retval = retval.replace('$', '.');

    return retval;
  }

  /**
   * Looks for the specified method name in the specified class and all of its
   * superclasses
   */
  public static Method super_get_declared_method (Class<?> c,
      String methodname, Class<?>... parameter_types) throws Exception {

    // Try and find the method in the base class
    Exception exception = null;
    Method method = null;
    try {
      method = c.getDeclaredMethod (methodname, parameter_types);
    } catch (Exception e) {
      exception = e;
    }
    if (method != null)
      return method;


    // Otherwise, look for the method in all superclasses for the method
    while (c.getSuperclass() != null) {
      c = c.getSuperclass();
      try {
        method = c.getDeclaredMethod (methodname, parameter_types);
      } catch (Exception e) {
      }
      if (method != null)
        return method;
    }

    // couldn't find the method anywhere
    throw exception;
  }

  /**
   * Returns true if the the specified modifier is visible to Randoop.
   * @see GenInputsAbstract#public_only
   */
  public static boolean isVisible (int modifiers) {
    // System.out.printf ("isVisible public_only=%b, modifiers = %s%n",
    //             GenInputsAbstract.public_only, Modifier.toString(modifiers));
    if (GenInputsAbstract.public_only) {
      return Modifier.isPublic (modifiers);
    } else {
      return !Modifier.isPrivate (modifiers);
    }
  }

}
