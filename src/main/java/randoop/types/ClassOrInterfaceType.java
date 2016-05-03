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
  public abstract ClassOrInterfaceType apply(Substitution<ReferenceType> substitution);

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
  @Override
  public boolean isSubtypeOf(GeneralType otherType) {

    if (super.isSubtypeOf(otherType)) {
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
      return false;
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
