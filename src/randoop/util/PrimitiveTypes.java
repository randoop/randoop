package randoop.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import randoop.main.GenInputsAbstract;

public final class PrimitiveTypes {
  private PrimitiveTypes() {
    throw new IllegalStateException("no instances");
  }

  private static final Map<String, Class<?>> typeNameToBoxed= new LinkedHashMap<String, Class<?>>();
  static {
    typeNameToBoxed.put("int",     Integer.class);
    typeNameToBoxed.put("boolean", Boolean.class);
    typeNameToBoxed.put("float",   Float.class);
    typeNameToBoxed.put("char",    Character.class);
    typeNameToBoxed.put("double",  Double.class);
    typeNameToBoxed.put("long",    Long.class);
    typeNameToBoxed.put("short",   Short.class);
    typeNameToBoxed.put("byte",    Byte.class);
    typeNameToBoxed.put(String.class.getName(), String.class);
  }

  private static final Map<Class<?>, Class<?>> boxedToPrimitiveAndString= new LinkedHashMap<Class<?>, Class<?>>();
  static {
    boxedToPrimitiveAndString.put(Integer.class,   int.class);
    boxedToPrimitiveAndString.put(Boolean.class,   boolean.class);
    boxedToPrimitiveAndString.put(Float.class,     float.class);
    boxedToPrimitiveAndString.put(Character.class, char.class);
    boxedToPrimitiveAndString.put(Double.class,    double.class);
    boxedToPrimitiveAndString.put(Long.class,      long.class);
    boxedToPrimitiveAndString.put(Short.class,     short.class);
    boxedToPrimitiveAndString.put(Byte.class,      byte.class);
    boxedToPrimitiveAndString.put(String.class, String.class);
  }

  private static final Map<Class<?>, Class<?>> primitiveAndStringToBoxed
  = new LinkedHashMap<Class<?>, Class<?>>(8);

  static {
    primitiveAndStringToBoxed.put(boolean.class, Boolean.class);
    primitiveAndStringToBoxed.put(byte.class, Byte.class);
    primitiveAndStringToBoxed.put(char.class, Character.class);
    primitiveAndStringToBoxed.put(double.class, Double.class);
    primitiveAndStringToBoxed.put(float.class, Float.class);
    primitiveAndStringToBoxed.put(int.class, Integer.class);
    primitiveAndStringToBoxed.put(long.class, Long.class);
    primitiveAndStringToBoxed.put(short.class, Short.class);
    primitiveAndStringToBoxed.put(String.class, String.class); // TODO remove this hack!
  }

  protected static final Map<String, Class<?>> typeNameToPrimitiveOrString= new LinkedHashMap<String, Class<?>>();
  static {
    typeNameToPrimitiveOrString.put("void", void.class);
    typeNameToPrimitiveOrString.put("int",     int.class);
    typeNameToPrimitiveOrString.put("boolean", boolean.class);
    typeNameToPrimitiveOrString.put("float",   float.class);
    typeNameToPrimitiveOrString.put("char",    char.class);
    typeNameToPrimitiveOrString.put("double",  double.class);
    typeNameToPrimitiveOrString.put("long",    long.class);
    typeNameToPrimitiveOrString.put("short",   short.class);
    typeNameToPrimitiveOrString.put("byte",    byte.class);
    typeNameToPrimitiveOrString.put(String.class.getName(), String.class);
  }

  public static boolean isPrimitiveOrStringTypeName(String typeName) {
    return typeNameToBoxed.containsKey(typeName);
  }

  public static Class<?> getBoxedType(String typeName) {
    Class<?> boxed = typeNameToBoxed.get(typeName);
    if (boxed == null)
      throw new IllegalArgumentException("not a primitive type:" + typeName);
    return boxed;
  }

  public static Class<?> getPrimitiveTypeOrString(String typeName) {
    return typeNameToPrimitiveOrString.get(typeName);
  }

  public static Set<Class<?>> getPrimitiveTypesAndString() {
    return Collections.unmodifiableSet(primitiveAndStringToBoxed.keySet());
  }

  public static Set<Class<?>> getBoxedTypesAndString() {
    return Collections.unmodifiableSet(boxedToPrimitiveAndString.keySet());
  }

  public static Class<?> boxedType(Class<?> c1) {
    return primitiveAndStringToBoxed.get(c1);
  }

  public static boolean isBoxedPrimitiveTypeOrString(Class<?> c) {
    return boxedToPrimitiveAndString.containsKey(c);
  }

  public static boolean isPrimitiveOrStringType(Class<?> type) {
    return primitiveAndStringToBoxed.containsKey(type);
  }

  public static Map<Class<?>,Boolean> isPrimitiveCached =
    new LinkedHashMap<Class<?>, Boolean>();

  /**
   * Same as c.isPrimitive() but faster if this test is done very
   * frequently (as it is in Randoop).
   */
  public static boolean isPrimitive(Class<?> c) {
    if (c == null)
      throw new IllegalArgumentException("c cannot be null.");
    Boolean b = isPrimitiveCached.get(c);
    if (b == null) {
      b = c.isPrimitive();
      isPrimitiveCached.put(c, b);
    }
    return b;

  }

  public static boolean isBoxedOrPrimitiveOrStringType(Class<?> c) {
    if (isPrimitive(c))
      return true;
    if (isBoxedPrimitiveTypeOrString(c))
      return true;
    return false;
  }

  /** Returns null if c is not a primitive or a boxed type. */
  public static Class<?> primitiveType(Class<? extends Object> c) {
    if (c.isPrimitive())
      return c;
    return boxedToPrimitiveAndString.get(c);
  }

  /**
   * Given a primitive, boxed primitive, or String, returns a String that can
   * be uesd in Java source to represent it.
   *
   * @param value the value to create a String representation for.
   * The value's type must be a primitive type, a String, or null.
   */
  public static String toCodeString(Object value) {

    if (value == null) {
      return "null";
    }
    Class<?> valueClass = primitiveType(value.getClass());
    assert valueClass != null : value + " "  + value.getClass();

    if (String.class.equals(valueClass)) {
      String escaped = StringEscapeUtils.escapeJava(value.toString());
      if (escaped.length() > GenInputsAbstract.string_maxlen) {
        throw new Error("String too long, length = " + escaped.length());
      }
      return "\"" + escaped + "\""; // + "/*length=" + escaped.length() + "*/"
    } else if (char.class.equals(valueClass)) {
      // XXX This won't always work!
      if (value.equals(' '))
        return "' '";
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
      if (rep.charAt(0) == '-')
        rep = "(" + rep + ")";
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
      if (rep.charAt(0) == '-')
        rep = "(" + rep + ")";
      return rep;

    } else if (boolean.class.equals(valueClass)) {

      // true and false are explicit enough; don't need cast.
      return value.toString();

    } else if (long.class.equals(valueClass)) {

      String rep = value.toString() + "L";
      if (rep.charAt(0) == '-')
        rep = "(" + rep + ")";
      return rep;

    } else if (byte.class.equals(valueClass)) {

      String rep = value.toString();
      if (rep.charAt(0) == '-')
        rep = "(" + rep + ")";
      rep = "(byte)" + rep;
      return rep;

    } else if (short.class.equals(valueClass)) {

      String rep = value.toString();
      if (rep.charAt(0) == '-')
        rep = "(" + rep + ")";
      rep = "(short)" + rep;
      return rep;

    } else {
      assert int.class.equals(valueClass) : valueClass;

      // We don't need to cast an int.
      String rep = value.toString();
      if (rep.charAt(0) == '-')
        rep = "(" + rep + ")";
      return rep;

    }
  }

  public static Class<?> getUnboxType(Class<?> c) {
    return boxedToPrimitiveAndString.get(c);
  }

  // If you modify, update doc for looksLikeObjectToString method.
  private static final String OBJECT_REF_PATTERN = ".*@[0-9a-h]{1,8}.*";

  /**
   * Returns true if the given string looks like it came from a call of
   * Object.toString(); in other words, looks something like
   * "<classname>@<hex>". Such strings are rarely useful in generation because
   * they contain non-reproducible hash strings.
   * 
   * This method is actually more restrictive in what it determines to look like
   * it came from Object.toString(): it deems anything that matches the pattern
   * 
   * .*@[0-9a-h]{1,8}.*
   * 
   * Meaning, if it looks like the string contains the telltale "@<hex>"
   * pattern, the method returns false. This almost always works and is a
   * faster check.
   */
  public static boolean looksLikeObjectToString(String s) {
    if (s == null) {
      throw new IllegalArgumentException("s is null");
    }
    int len = s.length();

    // Object.toString() string must have at least one character for
    // the class name, plus '@', plus one character for hashCode().
    if (len < 3) {
      return false;
    }

    return s.matches(OBJECT_REF_PATTERN);
  }
  
  // Used to increase performance of stringLengthOK method.
  private static Map<String, Boolean> stringLengthOKCached =
    new LinkedHashMap<String, Boolean>();
  
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

}
