package randoop.types;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents Java types defined in JLS Section 4.1
 * <pre>
 *   Type:
 *     ReferenceType
 *     PrimitiveType
 * </pre>
 * <p>
 * This class should be used to represent types in Randoop test generation
 * rather than the reflection type {@code Class<?>}.
 * <p>
 * {@link GeneralType} objects are not constructed directly, and instead
 * are constructed using the methods
 * {@link #forType(Type)},
 * {@link #forClass(Class)}, or
 * {@link #forName(String)}.
 * These methods translate the reflection types into objects of subclasses of this type.
 *
 * @see ReferenceType
 * @see PrimitiveType
 */
public abstract class GeneralType {

  /**
   * Returns the runtime {@code Class} object for this type.
   * For use when reflection is needed.
   *
   * @return the {@link Class} that is the runtime representation of the type
   */
  public Class<?> getRuntimeClass() {
    return null;
  }

  /**
   * Indicates whether the given {@code Class} object corresponds to the runtime
   * class of this type.
   *
   * @param c  the class to check
   * @return true if {@code c} is the raw type of this type, false otherwise
   */
  public boolean hasRuntimeClass(Class<?> c) {
    return this.getRuntimeClass().equals(c);
  }

  /**
   * Returns the fully-qualified name of this type, including type arguments if
   * this is a parameterized type.
   *
   * @return the fully-qualified type name for this type
   */
  public abstract String getName();

  /**
   * Indicate whether this object has a wildcard as a type parameter.
   *
   * @return true if this type has a wildcard parameter, and false otherwise
   */
  public boolean hasWildcard() {
    return false;
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
   * Indicates whether a value of a {@code GeneralType} can be assigned to a
   * variable of this type:
   * <code>
   * Variable<sub>this</sub> = Expression<sub>sourcetype</sub>.
   * </code>
   * In other words, whether this is a legal assignment.
   * <p>
   * Based on the definition of <i>assignment context</i> in
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.2">
   * section 5.2 of the JDK 8 Java Language Specification</a>
   * a value of one type is assignable to a variable of another type if the
   * first type can be converted to the second by
   * <ul>
   * <li> an identity conversion (section 5.1.1),
   * <li> a widening primitive conversion (section 5.1.2),
   * <li> a widening reference conversion (section 5.1.5),
   * <li> a boxing conversion (5.1.7), and
   * <li> an unboxing conversion (section 5.1.8) possibly followed by a widening conversion.
   * </ul>
   * And, if after all those conversions, the type is a raw type, an
   * unchecked conversion may occur.
   *<p>
   * When working on this method, be aware that the method
   * {@link Class#isAssignableFrom(Class)} checks identity and
   * reference conversions, and does a comparison of raw types for parameterized
   * types. The method {@link PrimitiveTypes#isAssignable(Class, Class)} checks
   * identity and primitive widening for primitive types.
   *
   * @param sourceType  the type to test for assignability
   * @return true if this type can be assigned from the source type, and false otherwise
   */
  public boolean isAssignableFrom(GeneralType sourceType) {
    if (sourceType == null) {
      throw new IllegalArgumentException("source type may not be null");
    }

    if (sourceType.isVoid()) {
      return false;
    }
    // identity conversion
    return this.equals(sourceType);
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
   * Indicates whether this type is a type variable.
   *
   * @return true if this type is a type variable, false otherwise
   */
  boolean isVariable() {
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
  public boolean isSubtypeOf(GeneralType otherType) {
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
   * @return the {@code GeneralType} constructed by substituting for type
   * parameters in this type, or this type if there are no type parameters
   */
  public GeneralType apply(Substitution<ReferenceType> substitution) {
    return this;
  }

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
   * Indicates whether the given object represents a value that is assignable to this type.
   * If the reference is null, then returns true only if this type is not primitive.
   *
   * @param e  the element to check
   * @param <T> the type of the value
   * @return true if the type of {@code e} is assignable to this type, false otherwise
   */
  public <T> boolean isInstance(T e) {
    if (e == null) {
      return !this.isPrimitive();
    }
    GeneralType type = GeneralType.forClass(e.getClass());
    return this.isAssignableFrom(type);
  }

  /**
   * Return the type for the superclass for this type.
   * Returns null for types that do not have a superclass.
   *
   * @return superclass of this type, or null if this type has no superclass
   */
  public GeneralType getSuperclass() {
    return null;
  }

  /**
   * Translates a reflective {@code Class} into a {@link GeneralType} object.
   * For primitive types, creates a {@link PrimitiveType} object.
   * For reference types, delegates to {@link ReferenceType#forClass(Class)}.
   *
   * @param classType  the {@code Class} object for the type
   * @return the {@code GeneralType} object for the given type class
   */
  public static GeneralType forClass(Class<?> classType) {
    if (classType.isPrimitive()) {
      return new PrimitiveType(classType);
    }

    return ReferenceType.forClass(classType);
  }

  /**
   * Returns a {@code GeneralType} object for the given type name.
   * Uses reflection to find the corresponding type.
   *
   * @param typeName  the name of a type
   * @return the type object for the type with the name, null if none is found
   * @throws ClassNotFoundException if name is not a recognized type
   */
  public static GeneralType forName(String typeName) throws ClassNotFoundException {
    Class<?> c = PrimitiveTypes.getClassForName(typeName);
    if (c == null) {
      c = Class.forName(typeName);
    }
    return GeneralType.forClass(c);
  }

  /**
   * Returns a type constructed from the object referenced by a
   * {@code java.lang.reflect.Type} reference.
   * <p>
   * Note that when the type corresponds to a generic class type, this method
   * returns the type variables from the
   * {@link java.lang.reflect.ParameterizedType#getActualTypeArguments() getActualTypeArguments()}
   * method to maintain the guarantees needed for
   * {@link ParameterizedType#isSubtypeOf(GeneralType)}.
   *
   * @param type  the type to interpret
   * @return a {@link GeneralType} object corresponding to the given type
   * @throws IllegalArgumentException if the type is a {@code java.lang.reflect.WildcardType}
   */
  public static GeneralType forType(Type type) {

    if (type instanceof WildcardType) {
      throw new IllegalArgumentException("Cannot construct type for wildcard " + type);
    }

    if (type instanceof Class<?>) {
      return GeneralType.forClass((Class<?>) type);
    }

    return ReferenceType.forType(type);
  }

  /**
   * Applies a capture conversion to this type.
   *
   * @return a copy of this type with wildcards replaced by type conversion
   */
  public GeneralType applyCaptureConversion() {
    return this;
  }

  /**
   * Returns the list of type parameters for this type.
   *
   * @return the type parameters for this type
   */
  public List<TypeVariable> getTypeParameters() {
    return new ArrayList<>();
  }
}
