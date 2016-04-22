package randoop.types;

import java.lang.reflect.Type;

/**
 * A reference type in JLS (Section 4.3) is defined as one of Class/Interface type,
 * Array type, or a type variable.
 */
public abstract class ReferenceType extends GeneralType{

  /**
   * {@inheritDoc}
   * Provides the default for reference type objects, which is the {@code Object}
   * type.
   *
   * @return the {@code Object} class type
   */
  @Override
  public ReferenceType getSuperclass() {
    return ConcreteTypes.OBJECT_TYPE;
  }

  /**
   * Returns a {@code ReferenceType} object for the given {@code Class} object.
   * Only checks for arrays, classes and interfaces since type variables are only represented
   * by {@code Type} references.
   * For arrays, delegates to {@link ArrayType#forClass(Class)}.
   * For class/interface types, delegates to {@link ClassOrInterfaceType#forClass(Class)}.
   *
   * @param classType  the {@code Class} object representing the type
   * @return the {@code ReferenceType} object for the given type
   */
  public static ReferenceType forClass(Class<?> classType) {
    if (classType.isPrimitive()) {
      throw new IllegalArgumentException("type must be a reference type");
    }

    if (classType.isArray()) {
      return ArrayType.forClass(classType);
    }

    return ClassOrInterfaceType.forClass(classType);
  }

  /**
   * Creates a {@code ReferenceType} for the given {@code java.lang.reflect.Type}.
   * First, checks that the reference is not one of the direct subtypes of
   * {@code Type} representing reference types ({@code GenericArrayType} or
   * {@code TypeVariable}).
   * Then checks that the reference is not a {@code Class} object representing
   * an array.
   * If the reference is none of those, returns the result of converting the
   * type to a class or interface type.
   *
   * @param type  the type reference
   * @return the {@code ReferenceType} for the given {@code Type}
   */
  public static ReferenceType forType(Type type) {
    if (type instanceof java.lang.reflect.GenericArrayType) {
      return ArrayType.forType(type);
    }

    if (type instanceof java.lang.reflect.TypeVariable) {
      return TypeVariable.forType(type);
    }

    if ((type instanceof Class<?>) && ((Class<?>)type).isArray()) {
      return ArrayType.forType(type);
    }

    return ClassOrInterfaceType.forType(type);

  }
}
