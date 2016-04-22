package randoop.types;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Represents Java types in Randoop.
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
   * Returns the fully-qualified name of the type, including type arguments if
   * this is a parameterized type.
   *
   * @return the fully-qualified type name for this type
   */
  public abstract String getName();

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
   * In other words, this is a legal assignment.
   * Based on the definition of <i>assignment context</i> in
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.2">
   * section 5.2 of the JDK 8 Java Language Specification</a>
   * a value of one type is assignable to a variable of another type if the
   * first type can be converted to the second by
   * <ul>
   * <li> an identity conversion,
   * <li> a widening primitive conversion,
   * <li> a widening reference conversion,
   * <li> a boxing conversion, and
   * <li> an unboxing conversion possibly followed by a widening conversion.
   * </ul>
   * And, if after all those conversions, the type is a raw type, an
   * unchecked conversion may occur.
   *<p>
   * When implementing this method, be aware that the method
   * {@link Class#isAssignableFrom(Class)} checks identity and
   * reference conversions, and does a comparison of raw types for parameterized
   * types. The method {@link PrimitiveTypes#isAssignable(Class, Class)} checks
   * identity and primitive widening for primitive types.
   *
   * @param sourceType  the type to test for assignability
   * @return true if this type can be assigned from the source type, and false otherwise
   */
  public abstract boolean isAssignableFrom(GeneralType sourceType);

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
   * If not, then type is concrete.
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
  public boolean isInterface() { return false; }

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
   * (A parameterized type is a generic class that has been instantiated with
   * concrete type arguments such as <code>List&lt;String&gt;</code>.)
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

  public boolean isString() {
    return false;
  }

  /**
   * Test whether this type is a subtype of the given type according to
   * transitive closure of definition of the <i>direct supertype</i> relation in
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.10.2">
   * section 4.10.2 of JLS for JavaSE 8</a>.
   * <i>Only</i> checks reference types.
   * @see #isAssignableFrom(GeneralType)
   * @see ParameterizedType#isSubtypeOf(GeneralType)
   *
   * @param otherType  the possible supertype
   * @return true if this type is a subtype of the given type, false otherwise
   */
  public boolean isSubtypeOf(GeneralType otherType) throws RandoopTypeException {
    if (otherType == null) {
      throw new IllegalArgumentException("type may not be null");
    }

    // Object is *the* supertype
    if (otherType.isObject()) {
      return true;
    }

    // minimally, underlying Class should be assignable
    Class<?> otherRuntimeType = otherType.getRuntimeClass();
    Class<?> thisRuntimeType = this.getRuntimeClass();
    if (!otherRuntimeType.isAssignableFrom(thisRuntimeType)) {
      return false;
    }

    // if other type is an interface, check interfaces first
    if (otherRuntimeType.isInterface()) {
      Type[] interfaces = thisRuntimeType.getGenericInterfaces();
      for (Type t : interfaces) {
        if (otherType.equals(GeneralType.forType(t))) {
          return true; // found the type
        }
      }
    }

    // otherwise, get superclass
    Type superclass = thisRuntimeType.getGenericSuperclass();
    if (superclass != null) {
      GeneralType superType = GeneralType.forType(superclass);
      if (otherType.equals(superType)) { // found the type
        return true;
      }

      // no match yet, so check for transitive chain
      return superType.isSubtypeOf(otherType);
    }

    return false;
  }

  /**
   * Indicate whether this type is void.
   *
   * @return true if this type is void, false otherwise
   */
  public boolean isVoid() {
    return false;
  }

  /**
   * Returns a concrete type for this general type created by instantiating
   * the type parameters with a list of concrete type arguments.
   *
   * @param substitution  the type substitution
   * @return a {@code ConcreteType} constructed by substituting for type
   * parameters in this generic type
   */
  public abstract GeneralType apply(Substitution substitution);

  /**
   * Returns the package of this types runtime class.
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

  public PrimitiveType toPrimitive() {
    throw new IllegalArgumentException("Type must be boxed primitive");
  }

  public ClassOrInterfaceType toBoxedPrimitive() {
    throw new IllegalArgumentException("type must be primitive");
  }

  /**
   * Indicates whether the given object represents a value that is assignable to this type.
   * If the reference is null, then returns true only if this type is not primitive.
   *
   * @param e  the element to check
   * @return true if the type of {@code e} is assignable to this type, false otherwise
   */
  public <T> boolean isInstance(T e) {
    if (e == null) {
      return !this.isPrimitive();
    }
    GeneralType type = GeneralType.forClass(e.getClass());
    return this.isAssignableFrom(type);
  }

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
   * Uses reflection to find the correspond type.
   *
   * @param typeName  the name of a type
   * @return the type object for the type with the name, null if none is found
   * @throws ClassNotFoundException if name is not a recognized type
   */
  public static GeneralType forName(String typeName) throws ClassNotFoundException, RandoopTypeException {
    Class<?> c = PrimitiveTypes.getClassForName(typeName);
    if (c == null) {
      c = Class.forName(typeName);
    }
    return GeneralType.forClass(c);
  }

  /**
   * Returns a type constructed from an object hiding behind a
   * {@code java.lang.reflect.Type} reference.
   * If the object is a {@code Class} instance then returns the
   * corresponding {@code ConcreteType}. If the type is actually a
   * {@code java.lang.reflect.ParameterizedType}, then the type arguments are
   * inspected to decide whether to return a {@code ParameterizedType} or a
   * {@code GenericClassType}.
   * If the type is a {@code java.lang.reflect.GenericArrayType}, then the
   * corresponding {@code GenericArrayType} is returned.
   * <p>
   * Note that when the type corresponds to a generic class type, this method
   * returns the type variables from the
   * {@link java.lang.reflect.ParameterizedType#getActualTypeArguments() getActualTypeArguments()}
   * method to maintain the guarantees needed for
   * {@link ParameterizedType#isSubtypeOf(GeneralType)}.
   *
   * @param type  the type to interpret
   * @return a {@code randoop.types.Type} object corresponding to the given type
   * @throws IllegalArgumentException if the rawtype is not a Class instance
   */
  public static GeneralType forType(Type type) {

    if (type instanceof WildcardType) {
      throw new IllegalArgumentException("Cannot construct type for wildcard " + type);
    }

    return ReferenceType.forType(type);

  }


}
