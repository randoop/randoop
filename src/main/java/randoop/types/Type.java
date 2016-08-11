package randoop.types;

import java.lang.reflect.WildcardType;

/**
 * The superclass of a class hierarchy representing Java types defined in JLS Section 4.1.
 * This class corresponds directly to <i>Type</i> defined in the JLS, which is defined by
 * <pre>
 *   Type:
 *     ReferenceType
 *     PrimitiveType
 * </pre>
 * <p>
 * The subclasses of this {@link Type} class should be used to represent types in Randoop test generation
 * rather than the reflection types.
 * Using reflection, each Java type has a {@code Class<?>} object, including primitive types.
 * But, things get a little complicated for generic and parameterized types, where the {@code Class}
 * object represents the raw type of the generic class, but also carries the type parameters of the
 * generic class.
 * More information about types is available through the subinterfaces of
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Type.html"><code>java.lang.reflect.Type</code></a>,
 * but working with generic and parameterized types is still awkward.
 * This is in part because the correspondence to the JLS is unclear, but also because the provided
 * methods do not implement all of the algorithms needed to work with types and type hierarchies as
 * needed in Randoop.
 * Effectively, the concrete subclasses of this class are facades for these reflective types, but
 * they are identified with the definitions in the JLS, and provide the methods needed to test for
 * assignability and test for subtypes.
 * <p>
 * {@link Type} objects
 * are constructed using the methods
 * {@link #forType(java.lang.reflect.Type)},
 * {@link #forClass(Class)}, or
 * {@link #forName(String)}.
 * These methods translate the reflection types into objects of subclasses of this type.
 */
public abstract class Type {

  /**
   * Translates a {@code Class} into a {@link Type} object.
   * For primitive types, creates a {@link PrimitiveType} object.
   * For reference types, delegates to {@link ReferenceType#forClass(Class)}.
   *
   * @param classType  the {@code Class} object for the type
   * @return the {@code Type} object for the given reflection type
   */
  public static Type forClass(Class<?> classType) {
    if (classType.isPrimitive()) {
      return new PrimitiveType(classType);
    }

    return ReferenceType.forClass(classType);
  }

  /**
   * Returns a {@code Type} object for the given type name in
   * <a href="http://docs.oracle.com/javase/8/docs/api/java/lang/Class.html#getName--"><code>Class.getName</code></a>
   * format.
   * Uses reflection to find the corresponding type.
   * <p>
   * Note that {@link Type#getName()} does not return the type name in this format.
   * To get the name in this format from a {@link Type} object {@code t}, use
   * {@code t.getRuntimeClass().getName()}.
   *
   * @param typeName  the name of a type
   * @return the type object for the type with the name, null if none is found
   * @throws ClassNotFoundException if name is not a recognized type
   */
  public static Type forName(String typeName) throws ClassNotFoundException {
    Class<?> c = PrimitiveTypes.classForName(typeName);
    if (c == null) {
      c = Class.forName(typeName);
    }
    return Type.forClass(c);
  }

  /**
   * Returns a type constructed from the object referenced by a
   * {@code java.lang.reflect.Type} reference.
   * <p>
   * Note that when the type corresponds to a generic class type, this method
   * returns the type variables from the
   * {@link java.lang.reflect.ParameterizedType#getActualTypeArguments() getActualTypeArguments()}
   * method to maintain the guarantees needed for
   * {@link ParameterizedType#isSubtypeOf(Type)}.
   *
   * @param type  the type to interpret
   * @return a {@link Type} object corresponding to the given type
   * @throws IllegalArgumentException if the type is a {@code java.lang.reflect.WildcardType}
   */
  public static Type forType(java.lang.reflect.Type type) {

    if (type instanceof WildcardType) {
      throw new IllegalArgumentException("Cannot construct type for wildcard " + type);
    }

    if (type instanceof Class<?>) {
      return Type.forClass((Class<?>) type);
    }

    return ReferenceType.forType(type);
  }

  /**
   * Returns the runtime {@code Class} object for this type.
   * For use when reflection is needed.
   * <p>
   * This method should not be confused with the inherited {@code Object.getClass()} method,
   * which returns the {@code Class<?>} for the {@link Type} object, and not of the represented type.
   * For instance, if a {@link Type} object {@code t} represented the Java type {@code int}, then
   * {@code t.getRuntimeClass()} would return {@code int.class} while {@code t.getClass()} would return
   * {@code Type.class}.
   *
   * @return the {@link Class} that is the runtime representation of the type
   */
  public Class<?> getRuntimeClass() {
    return null;
  }

  /**
   * Returns the fully-qualified name of this type, including type arguments if
   * this is a parameterized type.
   *
   * @return the fully-qualified type name for this type
   */
  public abstract String getName();

  /**
   * Returns the package of the runtime class of this type.
   *
   * @return the package of the runtime class of this type
   */
  public Package getPackage() {
    Class<?> c = getRuntimeClass();
    if (c != null) {
      return c.getPackage();
    }
    return null;
  }

  /**
   * Indicates whether the given {@code Class<?>} object is the runtime class of this type.
   *
   * @param c  the {@code Class<?>} to check
   * @return true if {@code c} is the runtime {@code Class<?>} of this type, false otherwise
   */
  public boolean hasRuntimeClass(Class<?> c) {
    return this.getRuntimeClass().equals(c);
  }

  /**
   * Indicates whether this object represents an array type.
   *
   * @return true if this object represents an array type, false otherwise
   */
  public boolean isArray() {
    return false;
  }

  /**
   * Indicates whether a value of a {@code Type} can be assigned to a
   * variable of this type.  Returns true if this is a legal assignment:
   * <code>
   * Variable<sub>this</sub> = Expression<sub>sourcetype</sub>.
   * </code>
   * <p>
   * Based on the definition of <i>assignment context</i> in
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.2">
   * section 5.2 of the JDK 8 Java Language Specification</a>,
   * a value of one type is assignable to a variable of another type if the
   * first type can be converted to the second by
   * <ul>
   * <li> an identity conversion (section 5.1.1),
   * <li> a widening primitive conversion (section 5.1.2),
   * <li> a widening reference conversion (section 5.1.5),
   * <li> a boxing conversion (5.1.7), or
   * <li> an unboxing conversion (section 5.1.8) possibly followed by a widening conversion.
   * </ul>
   * And, if after all those conversions, the type is a raw type, an
   * unchecked conversion may occur.
   *
   * @param sourceType  the type to test for assignability
   * @return true if this type can be assigned from the source type, and false otherwise
   */
  public boolean isAssignableFrom(Type sourceType) {
    if (sourceType.isVoid()) {
      return false;
    }
    // identity conversion
    return this.equals(sourceType);
  }

  /**
   * Indicates whether the given object represents a value that is assignable to this type.
   * If the reference is null, then returns true only if this type is not primitive.
   *
   * @param e  the element to check
   * @param <T> the type of the value
   * @return true if the type of {@code e} is assignable to this type, false otherwise
   */
  public <T> boolean isAssignableFromTypeOf(T e) {
    if (e == null) {
      return !this.isPrimitive();
    }
    Type type = Type.forClass(e.getClass());
    return this.isAssignableFrom(type);
  }

  /**
   * Indicates whether this is a boxed primitive type.
   *
   * @return true if this type is a boxed primitive, false otherwise
   */
  public boolean isBoxedPrimitive() {
    return false;
  }

  /**
   * Indicates whether this is an enum type.
   *
   * @return true if this is an enum type, false otherwise
   */
  public boolean isEnum() {
    return false;
  }

  /**
   * Indicate whether this type is generic.
   * A type is <i>generic</i> if it has one or more type variables.
   *
   * @return true if this type is generic, false otherwise
   */
  public boolean isGeneric() {
    return false;
  }

  /**
   * Indicates whether this object represents a type defined by an interface.
   *
   * @return true if this object represents an interface type, false otherwise
   */
  public boolean isInterface() {
    return false;
  }

  /**
   * Indicate whether this is the {@code Object} type.
   *
   * @return true if this is the {@code Object} type, false otherwise
   */
  public boolean isObject() {
    return this.equals(ConcreteTypes.OBJECT_TYPE);
  }

  /**
   * Indicate whether this type is a parameterized type.
   * (A <i>parameterized type</i> is a type <code>C&lt;T<sub>1</sub>,&hellip;,T<sub>k</sub>&gt;</code>
   * where <code>C&lt;F<sub>1</sub>,&hellip;,F<sub>k</sub>&gt;</code> is a generic class
   * instantiated by a substitution <code>[F<sub>i</sub>:=T<sub>i</sub>]</code>, and
   * <code>T<sub>i</sub></code> is a subtype of the upper bound <code>B<sub>i</sub></code> of
   * the type parameter <code>F<sub>i</sub></code>.)
   *
   * @return true if this type is a parameterized type, false otherwise
   */
  public boolean isParameterized() {
    return false;
  }

  /**
   * Indicates whether this is a primitive type.
   *
   * @return true if this type is primitive, false otherwise
   */
  public boolean isPrimitive() {
    return false;
  }

  /**
   * Indicate whether this type is a rawtype of a generic class. The rawtype is
   * the runtime type of the class with type parameters erased.
   *
   * @return true if this type is a rawtype of a generic class, false otherwise
   */
  public boolean isRawtype() {
    return false;
  }

  /**
   * Indicates whether this is a reference type.
   * Note: implementing classes should ensure that this is equivalent to !(this.isPrimitive())
   *
   * @return true if this type is a reference type, and false otherwise.
   */
  public boolean isReferenceType() {
    return false;
  }

  /**
   * Indicates whether this type is a type variable.
   *
   * @return true if this type is a type variable, false otherwise
   */
  boolean isVariable() {
    return false;
  }

  /**
   * Indicates whether this type is the String type.
   *
   * @return true if this type is the String type, and false otherwise
   */
  public boolean isString() {
    return false;
  }

  /**
   * Test whether this type is a subtype of the given type according to
   * transitive closure of definition of the <i>direct supertype</i> relation in
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.10">
   * section 4.10 of JLS for JavaSE 8</a>.
   *
   * @param otherType  the possible supertype
   * @return true if this type is a subtype of the given type, false otherwise
   */
  public boolean isSubtypeOf(Type otherType) {
    return this.equals(otherType);
  }

  /**
   * Indicate whether this type is void.
   *
   * @return true if this type is void, false otherwise
   */
  public boolean isVoid() {
    return this.equals(ConcreteTypes.VOID_TYPE);
  }

  /**
   * Returns the type for this created by instantiating the type parameters of this type
   * with {@link ReferenceType} objects.
   *
   * @param substitution  the type substitution
   * @return the {@code Type} constructed by substituting for type
   * parameters in this type, or this type if there are no type parameters
   */
  public Type apply(Substitution<ReferenceType> substitution) {
    return this;
  }

  /**
   * Applies a capture conversion to this type.
   *
   * @return a copy of this type with wildcards replaced by type conversion
   */
  public Type applyCaptureConversion() {
    return this;
  }

  /**
   * Unbox a boxed primitive type.
   * Acts as the identity on primitive types, but is not legal for other types.
   *
   * @return the primitive type corresponding to this (boxed primitive) type
   */
  public PrimitiveType toPrimitive() {
    throw new IllegalArgumentException("Type must be boxed primitive");
  }

  /**
   * Box a primitive type.
   * Acts as the identity on boxed primitive types, but is not legal for other types.
   *
   * @return the boxed primitive type corresponding to this type.
   */
  public ClassOrInterfaceType toBoxedPrimitive() {
    throw new IllegalArgumentException("type must be primitive");
  }

  /**
   * Returns the type for the given Object reference.
   *
   * @param value  the Object value
   * @return the {@link Type} for the given value
   */
  public static Type forValue(Object value) {
    return Type.forClass(value.getClass());
  }
}
