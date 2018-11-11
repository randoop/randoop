package randoop.types;

import java.lang.reflect.WildcardType;
import org.checkerframework.checker.signature.qual.ClassGetName;

/**
 * The superclass of a class hierarchy representing Java types defined in JLS Section 4.1. This
 * class corresponds directly to <i>Type</i> defined in the JLS, which is defined by
 *
 * <pre>
 *   Type:
 *     ReferenceType
 *     PrimitiveType
 * </pre>
 *
 * <p>The subclasses of this {@link Type} class should be used to represent types in Randoop test
 * generation rather than the reflection types. Using reflection, each Java type has a {@code
 * Class<?>} object, including primitive types. But, things get a little complicated for generic and
 * parameterized types, where the {@code Class} object represents the raw type of the generic class,
 * but also carries the type parameters of the generic class. More information about types is
 * available through the subinterfaces of <a
 * href="https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Type.html">{@code
 * java.lang.reflect.Type}</a>, but working with generic and parameterized types is still awkward.
 * This is in part because the correspondence to the JLS is unclear, but also because the provided
 * methods do not implement all of the algorithms needed to work with types and type hierarchies as
 * needed in Randoop. Effectively, the concrete subclasses of this class are facades for these
 * reflective types, but they are identified with the definitions in the JLS, and provide the
 * methods needed to test for assignability and test for subtypes.
 *
 * <p>{@link Type} objects are constructed using the methods {@link
 * #forType(java.lang.reflect.Type)}, {@link #forClass(Class)}, or {@link #forName(String)}. These
 * methods translate the reflection types into objects of subclasses of this type.
 */
public abstract class Type implements Comparable<Type> {

  /**
   * Translates a {@code Class} into a {@link Type} object. For primitive types, creates a {@link
   * PrimitiveType} object. For reference types, delegates to {@link ReferenceType#forClass(Class)}.
   *
   * @param classType the {@code Class} object for the type
   * @return the {@code Type} object for the given reflection type
   */
  public static Type forClass(Class<?> classType) {
    if (classType.equals(void.class)) {
      return VoidType.getVoidType();
    }
    if (classType.isPrimitive()) {
      return new PrimitiveType(classType);
    }

    return ReferenceType.forClass(classType);
  }

  /**
   * Returns a {@code Type} object for the given type name in <a
   * href="https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html#getName--">{@code
   * Class.getName}</a> format. Uses reflection to find the corresponding type.
   *
   * <p>Note that {@link Type#getName()} does not return the type name in this format. To get the
   * name in this format from a {@link Type} object {@code t}, use {@code
   * t.getRuntimeClass().getName()}.
   *
   * @param typeName the name of a type
   * @return the type object for the type with the name, null if none is found
   * @throws ClassNotFoundException if name is not a recognized type
   */
  public static Type forName(@ClassGetName String typeName) throws ClassNotFoundException {
    Class<?> c = PrimitiveTypes.classForName(typeName);
    if (c == null) {
      c = Class.forName(typeName);
    }
    return Type.forClass(c);
  }

  /**
   * Returns the type for the given Object reference.
   *
   * @param value the Object value
   * @return the {@link Type} for the given value
   */
  public static Type forValue(Object value) {
    return Type.forClass(value.getClass());
  }

  /**
   * Returns a type constructed from the object referenced by a {@code java.lang.reflect.Type}
   * reference.
   *
   * <p>Note that when the type corresponds to a generic class type, this method returns the type
   * variables from the {@link java.lang.reflect.ParameterizedType#getActualTypeArguments()
   * getActualTypeArguments()} method to maintain the guarantees needed for {@link
   * ParameterizedType#isSubtypeOf(Type)}.
   *
   * @param type the type to interpret
   * @return a {@link Type} object corresponding to the given type
   * @throws IllegalArgumentException if the type is a {@code java.lang.reflect.WildcardType}
   */
  public static Type forType(java.lang.reflect.Type type) {

    if (type instanceof WildcardType) {
      throw new IllegalArgumentException("Cannot construct type for wildcard " + type);
    }

    if ((type instanceof Class) && ((Class<?>) type).isPrimitive()) {
      return Type.forClass((Class<?>) type);
    }

    return ReferenceType.forType(type);
  }

  /**
   * Returns the runtime {@code Class} object for this type. For use when reflection is needed.
   *
   * <p>Note that type variables and the null reference type do not have a runtime class, and this
   * method will return null in those cases.
   *
   * <p>This method should not be confused with the inherited {@code Object.getClass()} method,
   * which returns the {@code Class<?>} for the {@link Type} object, and not of the represented
   * type. For instance, if a {@link Type} object {@code t} represented the Java type {@code int},
   * then {@code t.getRuntimeClass()} would return {@code int.class} while {@code t.getClass()}
   * would return {@code Type.class}.
   *
   * @return the {@link Class} that is the runtime representation of the type, or null if this type
   *     is a type variable or null reference type
   */
  public abstract Class<?> getRuntimeClass();

  /**
   * Returns the fully-qualified name of this type, including type arguments if this is a
   * parameterized type. For {@code java.util.List<T>} return {@code "java.util.List<T>"}.
   *
   * @return the fully-qualified type name for this type
   */
  public abstract String getName();

  /**
   * Returns the name of this type without type arguments or package qualifiers. For {@code
   * java.util.List<T>}, returns {@code "List"}.
   *
   * @return the name of this type without type arguments
   */
  public abstract String getSimpleName();

  /**
   * Returns the name of this type as the "canonical name" of the underlying runtime class.
   * Identical to {@link #getName()} except for types with type arguments. For {@code
   * java.util.List<T>} returns {@code "java.util.List"}. Returns {@code null} when {@code
   * Class<?>.getCanonicalName()} does for the underlying {@code Class<?>} object (e.g., the type is
   * a local or anonymous class, or array type where the component type that has no canonical name).
   *
   * @return the fully-qualified canonical name of this type
   */
  public String getCanonicalName() {
    return getRuntimeClass().getCanonicalName();
  }

  /**
   * Returns the name of this type without package name, but with type arguments if parameterized,
   * and enclosing class if a member class. For instance, for {@code java.util.List<T>} would return
   * {@code "List<T>"}.
   *
   * @return the unqualified name of this type
   */
  public String getUnqualifiedName() {
    return this.getSimpleName();
  }

  /**
   * Returns the raw type for this type, which is this type except for generic types.
   *
   * @return the raw type corresponding to this type
   */
  public Type getRawtype() {
    return this;
  }

  /**
   * Indicates whether the given {@code Class<?>} object is the runtime class of this type.
   *
   * @param c the {@code Class<?>} to check
   * @return true if {@code c} is the runtime {@code Class<?>} of this type, false otherwise
   */
  public boolean runtimeClassIs(Class<?> c) {
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
   * Indicates whether this is a boxed primitive type.
   *
   * @return true if this type is a boxed primitive, false otherwise
   */
  public boolean isBoxedPrimitive() {
    return false;
  }

  /**
   * Indicates whether this type is the Class type.
   *
   * @return true if this type is the Class type, and false otherwise
   */
  public boolean isClass() {
    return this.equals(JavaTypes.CLASS_TYPE);
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
   * Indicate whether this type is generic. A type is <i>generic</i> if it has one or more type
   * variables.
   *
   * @return true if this type is generic, false otherwise
   */
  public boolean isGeneric() {
    return false;
  }

  /**
   * Indicates whether this object is an interface type.
   *
   * @return true if this object is an interface type, false otherwise
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
    return this.equals(JavaTypes.OBJECT_TYPE);
  }

  /**
   * Indicates whether this type is the String type.
   *
   * @return true if this type is the String type, and false otherwise
   */
  public boolean isString() {
    return this.equals(JavaTypes.STRING_TYPE);
  }

  /**
   * Indicate whether this type is void.
   *
   * @return true if this type is void, false otherwise
   */
  public boolean isVoid() {
    return this.equals(JavaTypes.VOID_TYPE);
  }

  /**
   * Indicate whether this type is a parameterized type. (A <i>parameterized type</i> is a type
   * {@code C<T1,...,Tk>} that instantiates a generic class {@code C<F1,...,Fk>}.
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
   * Indicates whether this is a primitive type.
   *
   * @return true if this type is primitive, false otherwise
   * @see randoop.operation.NonreceiverTerm
   * @see randoop.operation.NonreceiverTerm#isNonreceiverType
   */
  public boolean isNonreceiverType() {
    return isPrimitive()
        || isBoxedPrimitive()
        || getRuntimeClass() == String.class
        || getRuntimeClass() == Class.class;
  }

  /**
   * Indicate whether this type is a rawtype of a generic class. The rawtype is the runtime type of
   * the class that has type parameters erased.
   *
   * @return true if this type is a rawtype of a generic class, false otherwise
   */
  public boolean isRawtype() {
    return false;
  }

  /**
   * Indicates whether this is a reference type. Note: implementing classes should ensure that this
   * is equivalent to !(this.isPrimitive())
   *
   * @return true if this type is a reference type, and false otherwise
   */
  public boolean isReferenceType() {
    return false;
  }

  /**
   * Indicates whether this type is a type variable.
   *
   * @return true if this type is a type variable, false otherwise
   */
  public boolean isVariable() {
    return false;
  }

  /**
   * Returns the type created by instantiating the type parameters of this type with {@link
   * ReferenceType} objects. Simply returns this type if it has no type parameters. In otherwords,
   * this type is not a {@link ParameterizedType}, which includes {@link GenericClassType}.
   *
   * <p>There are contexts in which it is necessary to apply a substitution to a {@link Type} and it
   * is not clear whether the type is parameterized. In particular, this method is defined here
   * because {@link ArrayType} can hold arbitrary types, including type variables and parameterized
   * types.
   *
   * @param substitution the type substitution
   * @return the {@link Type} constructed by substituting for type parameters in this type, or this
   *     type if this is not a generic class type
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
   * Indicates whether there is an assignment conversion from a source {@code Type} to this type.
   * (In other words, a value of the source type can be assigned to an l-value of this type.)
   * Returns true if this is a legal assignment conversion: <code>
   * Variable<sub>this</sub> = Expression<sub>sourcetype</sub>.
   * </code>
   *
   * <p>Based on the definition of <i>assignment context</i> in <a
   * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.2">section 5.2 of the
   * JDK 8 Java Language Specification</a>, a value of one type is assignable to a variable of
   * another type if the first type can be converted to the second by
   *
   * <ul>
   *   <li>an identity conversion (section 5.1.1),
   *   <li>a widening primitive conversion (section 5.1.2),
   *   <li>a widening reference conversion (section 5.1.5),
   *   <li>a boxing conversion (5.1.7), or
   *   <li>an unboxing conversion (section 5.1.8) possibly followed by a widening conversion.
   * </ul>
   *
   * And, if after all those conversions, the type is a raw type, an unchecked conversion may occur.
   *
   * @param sourceType the type to test for assignability
   * @return true if this type can be assigned from the source type, and false otherwise
   */
  public boolean isAssignableFrom(Type sourceType) {
    // default behavior, refined by overrides in subclasses
    if (sourceType.isVoid()) {
      return false;
    }
    // identity conversion
    return this.equals(sourceType);
  }

  /**
   * Indicates whether there is an assignment conversion from the type of {@code value} to this
   * type. (Note this is equivalent to determining whether {@code value} can be assigned to an
   * l-value of this type.) If the reference is null, then returns true only if this type is not
   * primitive.
   *
   * @param value the element to check
   * @param <T> the type of the value
   * @return true if the type of {@code value} is assignable to this type, false otherwise
   */
  public <T> boolean isAssignableFromTypeOf(T value) {
    if (value == null) {
      return !this.isPrimitive();
    }
    Type type = Type.forClass(value.getClass());
    return this.isAssignableFrom(type);
  }

  /**
   * Test whether this type is a subtype of the given type according to transitive closure of
   * definition of the <i>direct supertype</i> relation in <a
   * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.10">section 4.10 of
   * JLS for Java SE 8</a>.
   *
   * @param otherType the possible supertype
   * @return true if this type is a subtype of the given type, false otherwise
   */
  public boolean isSubtypeOf(Type otherType) {
    // default behavior, refined by overrides in subclasses
    return this.equals(otherType);
  }

  /**
   * Indicate whether this type is a class or interface type.
   *
   * @return true if this type is a class or interface type; false, otherwise
   */
  public boolean isClassOrInterfaceType() {
    return false;
  }

  /**
   * Compare this {@link Type} to another. Uses the names of the underlying {@code Class<?>}
   * objects. Uses canonical names if both have them, otherwise uses {@code Class<?>.getName()}.
   *
   * @param type the type to compare against
   * @return -1 if this type precedes {@code type}, 1 if this type succeeds {@code type}, and 0 if
   *     they are equal.
   */
  @Override
  public int compareTo(Type type) {
    String name1 = this.getCanonicalName();
    String name2 = this.getCanonicalName();
    if (name1 != null && name2 != null) {
      return this.getCanonicalName().compareTo(type.getCanonicalName());
    }
    return this.getRuntimeClass().getName().compareTo(this.getRuntimeClass().getName());
  }
}
