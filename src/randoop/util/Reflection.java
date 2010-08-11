package randoop.util;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.Globals;
import randoop.RConstructor;
import randoop.RMethod;
import randoop.StatementKind;
import randoop.StatementKindParseException;
import randoop.StatementKinds;
import randoop.main.GenInputsAbstract;
import plume.EntryReader;
import plume.Pair;
import plume.UtilMDE;

/** Utility methods that operate on reflection objects (classes, methods, etc.). */
public final class Reflection {

  /**
   * Used by methods that that a java.lang.Class<?> object as
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
   * We say that C1, C2 and C3 are related to C1 at depth >= 0.
   * We say that C4 and C5 are related to C1 at depth >= 1.
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
       Class<?> c = classForName(className, noerr);
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
     try{
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
       StatementKind stk;
      try {
        stk = StatementKinds.parse(line);
      } catch (StatementKindParseException e) {
        throw new Error(e);
      }
       if (stk instanceof RMethod) {
         result.add(((RMethod)stk).getMethod());
       } else {
         assert (stk instanceof RConstructor);
         result.add(((RConstructor)stk).getConstructor());
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

   public static boolean isAbstract(Class<?> c) {
     return Modifier.isAbstract (c.getModifiers());
   }

   public static void saveClassesToFile(List<Class<?>> classes, String file) throws IOException {
     FileWriter fw = new FileWriter(file);
     for(Class<?> s:classes) {
       fw.append(s.getName() + Globals.lineSep);
     }
     fw.close();

   }

   /**
    *
    * @param classListing
    * @param filter can be null.
    */
   public static List<StatementKind> getStatements(Collection<Class<?>> classListing, ReflectionFilter filter) {
     if (filter == null) filter = new DefaultReflectionFilter(null);
     Set<StatementKind> statements = new LinkedHashSet<StatementKind>();
     for (Class<?> c : classListing) {
       // System.out.printf ("Considering class %s, filter = %s%n", c, filter);
       if (filter.canUse(c)) {
         if (Log.isLoggingOn()) Log.logLine("Will add members for class " + c.getName());
         // System.out.printf ("using class %s%n", c);
         for (Method m : getMethodsOrdered(c)) {
           if (Log.isLoggingOn()) {
             Log.logLine(String.format("Considering method %s", m));
           }
           if (filter.canUse(m)) {
             RMethod mc = RMethod.getRMethod(m);
             statements.add(mc);
           }
         }
         for (Constructor<?> co : getDeclaredConstructorsOrdered(c)) {
           // System.out.printf ("Considering constructor %s%n", co);
           if (filter.canUse(co)) {
             RConstructor mc = RConstructor.getRConstructor(co);
             statements.add(mc);
           }
         }
       }
     }
     List<String> statementsAsString = new ArrayList<String>(); // For testing purposes.
     for (StatementKind st : statements)
       statementsAsString.add(st.toString());
     assert statementsAsString.size() == new LinkedHashSet<String>(statementsAsString).size();

     return new ArrayList<StatementKind>(statements);
   }

   /**
    * To deserialize a list serialized with this method, use the
    * method deserializeClassList.
    * @throws IOException
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
       retval = UtilMDE.classnameFromJvm(retval.replace('.', '/'));
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
