package randoop.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utilities for working with Java primitive and boxed primitive types as {@code Class<?>} objects.
 * Provides conversion from primitive type names to {@code Class} objects, boxing and unboxing, as
 * well as primitive subtype and assignment tests.
 * <p>
 * Note that {@code void} is considered a primitive type by Java reflection
 * ({@code (void.class).isPrimitive()} returns true).
 *
 */
public final class PrimitiveTypes {
  private PrimitiveTypes() {
    throw new IllegalStateException("no instances");
  }

  /** Map from boxed primitive to primitive {@code Class<?>} objects. */
  private static final Map<Class<?>, Class<?>> boxedToPrimitive = new LinkedHashMap<>();

  static {
    boxedToPrimitive.put(Integer.class, int.class);
    boxedToPrimitive.put(Boolean.class, boolean.class);
    boxedToPrimitive.put(Float.class, float.class);
    boxedToPrimitive.put(Character.class, char.class);
    boxedToPrimitive.put(Double.class, double.class);
    boxedToPrimitive.put(Long.class, long.class);
    boxedToPrimitive.put(Short.class, short.class);
    boxedToPrimitive.put(Byte.class, byte.class);
  }

  /** Map from primitive to boxed primitive {@code Class<?>} objects. */
  private static final Map<Class<?>, Class<?>> primitiveToBoxed = new LinkedHashMap<>(8);

  static {
    primitiveToBoxed.put(boolean.class, Boolean.class);
    primitiveToBoxed.put(byte.class, Byte.class);
    primitiveToBoxed.put(char.class, Character.class);
    primitiveToBoxed.put(double.class, Double.class);
    primitiveToBoxed.put(float.class, Float.class);
    primitiveToBoxed.put(int.class, Integer.class);
    primitiveToBoxed.put(long.class, Long.class);
    primitiveToBoxed.put(short.class, Short.class);
  }

  /** Map from primitive type name to {@code Class<?>} objects. */
  private static final Map<String, Class<?>> nameToClass = new LinkedHashMap<>();

  static {
    nameToClass.put("void", void.class); // reflection considers void a primitive
    nameToClass.put("int", int.class);
    nameToClass.put("boolean", boolean.class);
    nameToClass.put("float", float.class);
    nameToClass.put("char", char.class);
    nameToClass.put("double", double.class);
    nameToClass.put("long", long.class);
    nameToClass.put("short", short.class);
    nameToClass.put("byte", byte.class);
  }

  /**
   * Primitive widening map.
   * Maps a primitive type to the set of primitive types to which it may be converted by widening
   * as defined in
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.2">JLS section 5.1.2</a>.
   */
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

  /**
   * Return the {@code Class<?>} object for the given primitive type name
   * ({@code boolean}, {@code void}, and numeric types).
   *
   * @param typeName  the name of the type
   * @return the {@code Class<?>} object for the type, or null
   */
  public static Class<?> classForName(String typeName) {
    return nameToClass.get(typeName);
  }

  /**
   * Tests assignability from source to target type via identity conversion
   * and widening primitive conversion.
   *
   * @param target  the target type for assignment; that is, the lvalue or left-hand side.
   *    Must be primitive.
   * @param source  the source type for assignment; that is, the rvalue or right-hand side
   *    Must be primitive.
   * @return true if the source type can be assigned to the target type, false otherwise
   */
  static boolean isAssignable(Class<?> target, Class<?> source) {
    if (target == null || source == null) {
      throw new IllegalArgumentException("types must be non null");
    }
    if (!target.isPrimitive() || !source.isPrimitive()) {
      throw new IllegalArgumentException("types must be primitive");
    }

    if (source.equals(target)) { // check identity widening
      return true;
    }
    Set<Class<?>> targets = wideningTable.get(source);
    return targets != null && targets.contains(target);
  }

  /**
   * Determine if the given {@code Class<?>} is a boxed primitive type.
   *
   * @param c  the {@code Class<?>}
   * @return true if the {@code Class<?>} is a boxed primitive, false otherwise
   */
  public static boolean isBoxedPrimitive(Class<?> c) {
    return boxedToPrimitive.containsKey(c);
  }

  /**
   * Determines whether the first primitive type is a subtype of the second primitive as determined
   * by widening.
   *
   * @param first  the first primitive type
   * @param second the second primitive type
   * @return true if the first type is a subtype of the second type
   */
  static boolean isSubtype(Class<?> first, Class<?> second) {
    if (!first.isPrimitive() && !second.isPrimitive()) {
      throw new IllegalArgumentException("types must be primitive");
    }
    Set<Class<?>> superTypes = wideningTable.get(first);
    return superTypes != null && superTypes.contains(second);
  }

  /**
   * Return boxed type for a primitive type
   *
   * @param cls  the {@code Class} object for the primitive type
   * @return the boxed type for the primitive type, or null if the given type is not primitive
   */
  static Class<?> toBoxedType(Class<?> cls) {
    return primitiveToBoxed.get(cls);
  }

  /**
   * Returns the primitive {@code Class<?>} type for a boxed primitive type.
   *
   * @param c  the {@code Class<?>} type
   * @return the primitive type for the boxed type, or null if given type is not a boxed primitive
   */
  static Class<?> toUnboxedType(Class<?> c) {
    return boxedToPrimitive.get(c);
  }
}
