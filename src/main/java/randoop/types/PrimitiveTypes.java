package randoop.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import randoop.main.GenInputsAbstract;
import randoop.util.StringEscapeUtils;

/**
 * Implements utilities over Java primitive types.
 */
public final class PrimitiveTypes {
  private PrimitiveTypes() {
    throw new IllegalStateException("no instances");
  }

  private static final Map<String, Class<?>> typeNameToBoxed = new LinkedHashMap<>();

  static {
    typeNameToBoxed.put("int", Integer.class);
    typeNameToBoxed.put("boolean", Boolean.class);
    typeNameToBoxed.put("float", Float.class);
    typeNameToBoxed.put("char", Character.class);
    typeNameToBoxed.put("double", Double.class);
    typeNameToBoxed.put("long", Long.class);
    typeNameToBoxed.put("short", Short.class);
    typeNameToBoxed.put("byte", Byte.class);
    typeNameToBoxed.put(String.class.getName(), String.class);
  }

  private static final Map<Class<?>, Class<?>> boxedToPrimitiveAndString = new LinkedHashMap<>();

  static {
    boxedToPrimitiveAndString.put(Integer.class, int.class);
    boxedToPrimitiveAndString.put(Boolean.class, boolean.class);
    boxedToPrimitiveAndString.put(Float.class, float.class);
    boxedToPrimitiveAndString.put(Character.class, char.class);
    boxedToPrimitiveAndString.put(Double.class, double.class);
    boxedToPrimitiveAndString.put(Long.class, long.class);
    boxedToPrimitiveAndString.put(Short.class, short.class);
    boxedToPrimitiveAndString.put(Byte.class, byte.class);
    boxedToPrimitiveAndString.put(String.class, String.class);
  }

  private static final Map<Class<?>, Class<?>> primitiveAndStringToBoxed = new LinkedHashMap<>(8);

  static {
    primitiveAndStringToBoxed.put(boolean.class, Boolean.class);
    primitiveAndStringToBoxed.put(byte.class, Byte.class);
    primitiveAndStringToBoxed.put(char.class, Character.class);
    primitiveAndStringToBoxed.put(double.class, Double.class);
    primitiveAndStringToBoxed.put(float.class, Float.class);
    primitiveAndStringToBoxed.put(int.class, Integer.class);
    primitiveAndStringToBoxed.put(long.class, Long.class);
    primitiveAndStringToBoxed.put(short.class, Short.class);
    primitiveAndStringToBoxed.put(String.class, String.class);
  }

  private static final Map<String, Class<?>> typeNameToPrimitiveOrString = new LinkedHashMap<>();

  static {
    typeNameToPrimitiveOrString.put("void", void.class);
    typeNameToPrimitiveOrString.put("int", int.class);
    typeNameToPrimitiveOrString.put("boolean", boolean.class);
    typeNameToPrimitiveOrString.put("float", float.class);
    typeNameToPrimitiveOrString.put("char", char.class);
    typeNameToPrimitiveOrString.put("double", double.class);
    typeNameToPrimitiveOrString.put("long", long.class);
    typeNameToPrimitiveOrString.put("short", short.class);
    typeNameToPrimitiveOrString.put("byte", byte.class);
    typeNameToPrimitiveOrString.put(String.class.getName(), String.class);
  }

  private static final Map<Class<?>, Set<Class<?>>> wideningTable = new HashMap<>();

  static {
    Set<Class<?>> s = new HashSet<>();
    s.add(double.class);
    wideningTable.put(float.class, new HashSet<>(s));
    s.add(float.class);
    wideningTable.put(long.class, new HashSet<>(s));
    s.add(long.class);
    wideningTable.put(int.class, new HashSet<>(s));
    s.add(int.class);
    wideningTable.put(char.class, new HashSet<>(s));
    wideningTable.put(short.class, new HashSet<>(s));
    s.add(short.class);
    wideningTable.put(byte.class, new HashSet<>(s));
  }

  public static boolean isPrimitiveOrStringTypeName(String typeName) {
    return typeNameToBoxed.containsKey(typeName);
  }

  public static Class<?> getBoxedType(String typeName) {
    Class<?> boxed = typeNameToBoxed.get(typeName);
    if (boxed == null) throw new IllegalArgumentException("not a primitive type:" + typeName);
    return boxed;
  }

  public static Class<?> getPrimitiveOrStringType(String typeName) {
    return typeNameToPrimitiveOrString.get(typeName);
  }

  public static Set<Class<?>> getPrimitiveOrStringTypes() {
    Set<Class<?>> s = new LinkedHashSet<>();
    for (Class<?> c : primitiveAndStringToBoxed.keySet()) {
      s.add(c);
    }
    return Collections.unmodifiableSet(s);
  }

  public static Set<Class<?>> getBoxedTypesAndString() {
    Set<Class<?>> s = new LinkedHashSet<>();
    for (Class<?> c : boxedToPrimitiveAndString.keySet()) {
      s.add(c);
    }
    return Collections.unmodifiableSet(s);
  }

  public static Class<?> toBoxedType(Class<?> cls) {
    return primitiveAndStringToBoxed.get(cls);
  }

  public static boolean isBoxedPrimitiveTypeOrString(Class<?> c) {
    return boxedToPrimitiveAndString.containsKey(c);
  }

  public static boolean isPrimitiveOrStringType(Class<?> type) {
    return primitiveAndStringToBoxed.containsKey(type);
  }

  private static Map<Class<?>, Boolean> isPrimitiveCached = new LinkedHashMap<>();

  /**
   * Same as c.isPrimitive() but faster if this test is done very
   * frequently (as it is in Randoop).
   *
   * @param c  the type class
   * @return true if the type is primitive, false otherwise
   */
  public static boolean isPrimitive(Class<?> c) {
    if (c == null) throw new IllegalArgumentException("c cannot be null.");
    Boolean b = isPrimitiveCached.get(c);
    if (b == null) {
      b = c.isPrimitive();
      isPrimitiveCached.put(c, b);
    }
    return b;
  }

  /**
   * Indicates whether the given type is a boxed primitive, primitive or String type.
   *
   * @param c  the type class
   * @return true if the type is boxed primitive, primitive, or String
   */
  public static boolean isBoxedOrPrimitiveOrStringType(Class<?> c) {
    return isPrimitive(c) || isBoxedPrimitiveTypeOrString(c);
  }

  /**
   * Returns the primitive type for the given boxed type.
   * Returns null if c is not a primitive or a boxed type.
   *
   * @param c  the class type
   * @return the primitive type for the given type, or null if given type is not primitive or boxed
   */
  public static Class<?> primitiveType(Class<?> c) {
    if (c.isPrimitive()) return c;
    return boxedToPrimitiveAndString.get(c);
  }

  /**
   * Given a primitive, boxed primitive, or String, returns a String that can
   * be uesd in Java source to represent it.
   *
   * @param value the value to create a String representation for.
   * The value's type must be a primitive type, a String, or null.
   * @return a string representing code for the given value
   */
  public static String toCodeString(Object value) {

    if (value == null) {
      return "null";
    }
    Class<?> valueClass = primitiveType(value.getClass());
    assert valueClass != null : value + " " + value.getClass();

    if (String.class.equals(valueClass)) {
      String escaped = StringEscapeUtils.escapeJava(value.toString());
      if (escaped.length() > GenInputsAbstract.string_maxlen) {
        throw new Error("String too long, length = " + escaped.length());
      }
      return "\"" + escaped + "\""; // + "/*length=" + escaped.length() + "*/"
    } else if (char.class.equals(valueClass)) {
      // XXX This won't always work!
      if (value.equals(' ')) return "' '";
      return "\'" + StringEscapeUtils.escapeJavaStyleString(value.toString(), true) + "\'";

    } else if (double.class.equals(valueClass)) {
      Double d = (Double) value;
      if (d.isNaN()) {
        return "Double.NaN";
      } else if (d == Double.POSITIVE_INFINITY) {
        return "Double.POSITIVE_INFINITY";
      } else if (d == Double.NEGATIVE_INFINITY) {
        return "Double.NEGATIVE_INFINITY";
      }
      String rep = d.toString();
      assert rep != null;
      rep = rep + "d";
      if (rep.charAt(0) == '-') rep = "(" + rep + ")";
      return rep;

    } else if (float.class.equals(valueClass)) {
      Float d = (Float) value;
      if (d.isNaN()) {
        return "Float.NaN";
      } else if (d == Float.POSITIVE_INFINITY) {
        return "Float.POSITIVE_INFINITY";
      } else if (d == Float.NEGATIVE_INFINITY) {
        return "Float.NEGATIVE_INFINITY";
      }
      String rep = d.toString();
      assert rep != null;
      rep = rep + "f";
      if (rep.charAt(0) == '-') rep = "(" + rep + ")";
      return rep;

    } else if (boolean.class.equals(valueClass)) {

      // true and false are explicit enough; don't need cast.
      return value.toString();

    } else if (long.class.equals(valueClass)) {

      String rep = value.toString() + "L";
      if (rep.charAt(0) == '-') rep = "(" + rep + ")";
      return rep;

    } else if (byte.class.equals(valueClass)) {

      String rep = value.toString();
      if (rep.charAt(0) == '-') rep = "(" + rep + ")";
      rep = "(byte)" + rep;
      return rep;

    } else if (short.class.equals(valueClass)) {

      String rep = value.toString();
      if (rep.charAt(0) == '-') rep = "(" + rep + ")";
      rep = "(short)" + rep;
      return rep;

    } else {
      assert int.class.equals(valueClass) : valueClass;

      // We don't need to cast an int.
      String rep = value.toString();
      if (rep.charAt(0) == '-') rep = "(" + rep + ")";
      return rep;
    }
  }

  static Class<?> toUnboxedType(Class<?> c) {
    return boxedToPrimitiveAndString.get(c);
  }

  // If you modify, update doc for looksLikeObjectToString method.
  private static final String OBJECT_REF_PATTERN = ".*@[0-9a-h]{1,8}.*";

  /**
   * Returns true if the given string looks like it came from a call of
   * Object.toString(); in other words, looks something like
   * "&lt;classname&gt;@&lt;hex&gt;". Such strings are rarely useful in generation because
   * they contain non-reproducible hash strings.
   *
   * This method is actually more restrictive in what it determines to look like
   * it came from Object.toString(): it deems anything that matches the pattern
   *
   * .*@[0-9a-h]{1,8}.*
   *
   * Meaning, if it looks like the string contains the telltale "@&lt;hex&gt;"
   * pattern, the method returns false. This almost always works and is a
   * faster check.
   *
   * @param s  the string
   * @return true if string appears to be default toString output, false otherwise
   */
  public static boolean looksLikeObjectToString(String s) {
    if (s == null) {
      throw new IllegalArgumentException("s is null");
    }
    int len = s.length();

    // Object.toString() string must have at least one character for
    // the class name, plus '@', plus one character for hashCode().
    return len >= 3 && s.matches(OBJECT_REF_PATTERN);

  }

  // Used to increase performance of stringLengthOK method.
  private static Map<String, Boolean> stringLengthOKCached = new LinkedHashMap<>();

  /**
   * Returns true if the given string is deemed to be reasonable (i.e. not too long)
   * based on the --string-maxlen=N parameter.
   * <p>
   * If Randoop generates tests using strings that are too long, this can
   * result in non-compilable tests due to the JVM's limit on the length of a string.
   * <p>
   * A string S is too long if, when printed as code in a generated unit test,
   * it may result in a non-compilable test. In order to determine this, we have
   * to consider not the length of s, but the length of the string that would be
   * printed to obtain s, which may be different due to escaped and unicode characters.
   * This method takes this into account.
   *
   * @param s  the string
   * @return true if the string length is reasonable for generated tests, false otherwise
   *
   *  @see GenInputsAbstract
   */
  public static boolean stringLengthOK(String s) {
    if (s == null) {
      throw new IllegalArgumentException("s is null");
    }

    // Optimization: return cached value if available.
    Boolean b = stringLengthOKCached.get(s);
    if (b != null) {
      return b;
    }

    int length = s.length();

    // Optimization: if length greater than maxlen, return false right away.
    if (length > GenInputsAbstract.string_maxlen) {
      stringLengthOKCached.put(s, false);
      return false;
    }

    // Optimization: if the string is definitely short enough, return true right away.
    // If a string's length is less than 1/6 * maxlen, it's definitely short enough, since
    // the worst that could happen is that every character in s is unicode and is
    // expanded to "\u0000" format, blowing up the length to s.length() * 6.
    if (length * 6 < GenInputsAbstract.string_maxlen) {
      stringLengthOKCached.put(s, true);
      return true;
    }

    boolean retval = StringEscapeUtils.escapeJava(s).length() <= GenInputsAbstract.string_maxlen;
    stringLengthOKCached.put(s, retval);
    return retval;
  }

  static Class<?> getClassForName(String typeName) {
    return typeNameToPrimitiveOrString.get(typeName);
  }

  /**
   * Tests assignability from source to target type via identity conversion
   * and widening primitive conversion.
   *
   * @param target  the target type for assignment
   * @param source  the source type for assignment
   * @return true if the source type can be assigned to the target type, false otherwise
   */
  static boolean isAssignable(Class<?> target, Class<?> source) {
    if (target == null || source == null) {
      throw new IllegalArgumentException("types must be non null");
    }
    if ((!target.isPrimitive() && !target.equals(String.class)) || (!source.isPrimitive() && !source.equals(String.class))) {
      throw new IllegalArgumentException("types must be primitive or String");
    }

    if (source.equals(target)) { // check identity widening
      return true;
    }
    Set<Class<?>> targets = wideningTable.get(source);
    return targets != null && targets.contains(target);
  }

  static boolean isSubtype(Class<?> first, Class<?> second) {
    if (! first.isPrimitive() && ! second.isPrimitive()) {
      throw new IllegalArgumentException("types must be primitive");
    }
    Set<Class<?>> superTypes = wideningTable.get(first);
    return superTypes != null && superTypes.contains(second);
  }

  /**
   * Return boxed type for a primitive type
   *
   * @param cls  the {@code Class} object for the primitive type
   * @return the {@code Class} object for boxed primitive type
   */
  static Class<?> getBoxedType(Class<?> cls) {
    return primitiveAndStringToBoxed.get(cls);
  }
}
