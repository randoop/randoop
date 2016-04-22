package randoop.types;

import java.lang.reflect.Type;

/**
 * An abstract class representing Java class or interface types as defined in the JLS.
 * (See JLS, section 4.3.)
 *
 * @see SimpleClassOrInterfaceType
 * @see ParameterizedType
 */
public abstract class ClassOrInterfaceType extends ReferenceType {

  @Override
  public ClassOrInterfaceType getSuperclass() {
    return ConcreteTypes.OBJECT_TYPE;
  }

  @Override
  public abstract ClassOrInterfaceType apply(Substitution substitution);

  /**
   * Translates a {@code Class} object that represents a class or interface into a
   * {@code ClassOrInterfaceType} object.
   * If the object has parameters, then delegates to {@link ParameterizedType#forClass(Class)}.
   * Otherwise, creates a {@link SimpleClassOrInterfaceType} object from the given object.
   *
   * @param classType  the class type to translate
   * @return the {@code ClassOrInterfaceType} object created from the given class type
   */
  public static ClassOrInterfaceType forClass(Class<?> classType) {
    if (classType.isArray() || classType.isPrimitive()) {
      throw new IllegalArgumentException("type must be a class or interface");
    }

    if (classType.getTypeParameters().length > 0) {
      return ParameterizedType.forClass(classType);
    }

    return new SimpleClassOrInterfaceType(classType);
  }

  /**
   * Creates a {@code ClassOrInterfaceType} object for a given
   * {@code java.lang.reflect.Type} reference.
   * If type is a {@code java.lang.reflect.ParameterizedType}, then calls
   * {@link ParameterizedType#forType(Type)}.
   * Otherwise, if type is a {@code Class} object, calls {@link #forClass(Class)}.
   *
   * @param type  the type reference
   * @return the {@code ClassOrInterfaceType} object for the given type
   */
  public static ClassOrInterfaceType forType(Type type) {

    if (type instanceof java.lang.reflect.ParameterizedType) {
      java.lang.reflect.ParameterizedType t = (java.lang.reflect.ParameterizedType) type;
      return ParameterizedType.forType(t);
    }

    if (type instanceof Class<?>) {
      Class<?> classType = (Class<?>)type;
      return ClassOrInterfaceType.forClass(classType);
    }

    throw new IllegalArgumentException("Unable to create class type from type " + type);
  }
}
