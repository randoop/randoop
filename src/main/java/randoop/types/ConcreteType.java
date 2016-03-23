package randoop.types;

import java.lang.reflect.Type;

/**
 * {@code ConcreteType} represents any type that does not have a type variable:
 * a primitive type, a non-generic class, an enum, a parameterized type, or a
 * rawtype.
 * @see randoop.types.ConcreteSimpleType
 * @see randoop.types.ConcreteArrayType
 * @see randoop.types.ParameterizedType
 */
public abstract class ConcreteType extends GeneralType {

  public static final ConcreteType VOID_TYPE = ConcreteType.forClass(void.class);
  public static final ConcreteType STRING_TYPE = ConcreteType.forClass(String.class);
  public static final ConcreteType OBJECT_TYPE = ConcreteType.forClass(Object.class);

  /**
   * Indicates whether a value of a {@code ConcreteType} can be assigned to a
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
  public boolean isAssignableFrom(ConcreteType sourceType) {
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
   * Indicates whether this is a primitive type.
   *
   * @return true if this type is primitive, false otherwise
   */
  public boolean isPrimitive() {
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
   * Indicate whether this type is a rawtype of a generic class. The rawtype is
   * the runtime type of the class with type parameters erased.
   *
   * @return true if this type is a rawtype of a generic class, false otherwise
   */
  public boolean isRawtype() {
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
   * Indicates whether the given object represents an instance of this type.
   *
   * @param e  the element to check
   * @return true if the type of {@code e} is assignable to this type, false otherwise
   */
  public <T> boolean isInstance(T e) {
    return this.isAssignableFrom(ConcreteType.forClass(e.getClass(), new ConcreteType[0]));
  }

  /**
   * Returns a {@code ConcreteType} object for the given class object.
   * <p>
   * For a primitive, enum, or non-generic class, the arguments should be empty.
   * If {@code typeClass} is a generic class, then a {@link ParameterizedType}
   * is created as long as the number of actual type arguments agrees with the
   * number of type parameters.
   * Otherwise, if {@code typeClass} is a generic class given without arguments,
   * then the type is a rawtype, and a {@link ConcreteSimpleType} is created.
   *
   * @param typeClass  the {@code Class} object
   * @param arguments  the actual type arguments to create a parameterized type
   * @return a {@code Type} object wrapping the {@code Class} object.
   */
  public static ConcreteType forClass(Class<?> typeClass, ConcreteType... arguments) {

    if (typeClass.isPrimitive()) {
      if (arguments.length > 0) {
        String msg = "There should be no type arguments for a primitive type";
        throw new IllegalArgumentException(msg);
      }
      return new ConcreteSimpleType(typeClass);
    }
    if (typeClass.isEnum()) {
      if (arguments.length > 0) {
        String msg = "There should be no type arguments for an enum type";
        throw new IllegalArgumentException(msg);
      }
      return new ConcreteSimpleType(typeClass);
    }
    if (typeClass.isArray()) {
      return new ConcreteArrayType(typeClass);
    }

    if (typeClass.getTypeParameters().length > 0) { // is generic
      if (arguments.length > 0) {
        GenericType genericType = GenericType.forClass(typeClass);
        GenericClassType genericClass = (GenericClassType) genericType;
        return genericClass.instantiate(arguments);
      }
      // if no arguments, fall through to return as rawtype
    }
    assert arguments.length == 0;
    return new ConcreteSimpleType(typeClass);
  }

  /**
   * Returns a {@code ConcreteType} object for an object with type
   * {@code java.lang.reflect.Type} that represents a concrete type.
   * <p>
   * (The interface {@link java.lang.reflect.Type} is the type returned by
   * reflection methods that provide type parameter information for generic or
   * parameterized types.)
   * @see randoop.types.GeneralType#forType(Type)
   *
   * @param type  the type to convert
   * @return a {@code ConcreteType} object corresponding to the concrete type
   */
  public static ConcreteType forType(Type type) {
    GeneralType t = GeneralType.forType(type);
    if (!t.isGeneric()) {
      return (ConcreteType) t;
    }
    String msg = "unable to create concrete type from type " + type.toString();
    throw new IllegalArgumentException(msg);
  }

  /**
   * Returns the {@code ConcreteType} of an array for the given element type.
   *
   * @param elementType  the element type for the array type
   * @return a type object representing the array type with the given element type
   */
  public static ConcreteType forArrayOf(ConcreteType elementType) {
    return new ConcreteArrayType(elementType);
  }

  /**
   * {@inheritDoc}
   * @return this concrete type
   */
  @Override
  public ConcreteType apply(Substitution substitution) {
    return this;
  }
}
