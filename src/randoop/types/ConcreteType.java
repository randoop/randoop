package randoop.types;

import java.lang.reflect.Type;

/**
 * {@code ConcreteType} represents a concrete type, a primitive type, 
 * a non-generic class, an enum, or a parameterized type.
 * @see randoop.types.ConcreteSimpleType
 * @see randoop.types.ConcreteArrayType
 * @see randoop.types.ParameterizedType
 */
public abstract class ConcreteType extends GeneralType {

  /**
   * Indicates whether a {@code ConcreteType} can be assigned to this type.
   * <p>
   * Based on the definition of assignment conversion from
   * section 5.2 of JDK 7 Java Language Specification a type is assignable
   * to another if it can be converted to the second by
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
   * The method {@link Class#isAssignableFrom(Class)} checks identity and
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
   * Indicate whether this type is a parameterized type.
   * (A parameterized type is a generic class with concrete type arguments.)
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
    return this.isAssignableFrom(ConcreteType.forClass(e.getClass()));
  }
  
  /**
   * Returns a {@code ConcreteType} object for the given class object.
   * Returned type may represent a primitive type, an enum, a non-generic class,
   * an array, a parameterized type, or the rawtype of a generic class.
   * <p>
   * For a primitive, enum, or non-generic class, the arguments should be empty.
   * If the type class is a generic class, then a parameterized type is created
   * as long as the number of actual type arguments agrees with the number of
   * type parameters.
   * If a generic class is given without arguments, then the type is treated
   * as a rawtype. 
   *
   * @param typeClass  the {@code Class} object
   * @param arguments  the actual type arguments for parameterized type
   * @return a {@code Type} object wrapping the {@code Class} object.
   * @throws IllegalArgumentException if passed null, or type arguments do not
   * match type parameters.
   */
  public static ConcreteType forClass(Class<?> typeClass, ConcreteType... arguments) {
    if (typeClass == null) {
      throw new IllegalArgumentException("Must have Class object to create type");
    }
    
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
        GenericClassType genericClass = (GenericClassType)genericType;
        return genericClass.instantiate(arguments);
      }
      // if no arguments, fall through to return as rawtype
    }
    
    return new ConcreteSimpleType(typeClass);
  }
  
  /**
   * Returns a {@code ConcreteType} object for a {@code ConcreteType} object 
   * representing a concrete type.
   * @see randoop.types.GeneralType#forType(Type)
   * 
   * @param type  the type to convert
   * @return a {@code ConcreteType} object corresponding to the concrete type
   * @throws IllegalArgumentException if the type is not concrete
   */
  public static ConcreteType forType(Type type) {
    GeneralType t = GeneralType.forType(type);
    if (! t.isGeneric()) {
      return (ConcreteType)t;
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

}
