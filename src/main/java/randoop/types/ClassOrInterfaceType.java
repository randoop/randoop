package randoop.types;

import java.util.List;

/**
 * Represents a class or interface type as defined in JLS Section 4.3.
 * <p>
 * This abstract class corresponds to the grammar in the JLS:
 * <pre>
 *   ClassOrInterfaceType:
 *     ClassType
 *     InterfaceType
 * </pre>
 * Since InterfaceType is syntactically the same as ClassType, the subclasses of this type
 * distinguish between types with parameters ({@link ParameterizedType}), and types without
 * ({@link NonParameterizedType}).
 */
public abstract class ClassOrInterfaceType extends ReferenceType {

  /**
   * Translates a {@code Class} object that represents a class or interface into a
   * {@code ClassOrInterfaceType} object.
   * If the object has parameters, then delegates to {@link ParameterizedType#forClass(Class)}.
   * Otherwise, creates a {@link NonParameterizedType} object from the given object.
   *
   * @param classType  the class type to translate
   * @return the {@code ClassOrInterfaceType} object created from the given class type
   */
  public static ClassOrInterfaceType forClass(Class<?> classType) {
    if (classType.isArray() || classType.isPrimitive()) {
      throw new IllegalArgumentException("type must be a class or interface, got " + classType);
    }

    if (classType.getTypeParameters().length > 0) {
      return ParameterizedType.forClass(classType);
    }

    return new NonParameterizedType(classType);
  }

  /**
   * Creates a {@code ClassOrInterfaceType} object for a given
   * {@code java.lang.reflect.Type} reference.
   * If type is a {@code java.lang.reflect.ParameterizedType}, then calls
   * {@link ParameterizedType#forType(java.lang.reflect.Type)}.
   * Otherwise, if type is a {@code Class} object, calls {@link #forClass(Class)}.
   *
   * @param type  the type reference
   * @return the {@code ClassOrInterfaceType} object for the given type
   */
  public static ClassOrInterfaceType forType(java.lang.reflect.Type type) {

    if (type instanceof java.lang.reflect.ParameterizedType) {
      java.lang.reflect.ParameterizedType t = (java.lang.reflect.ParameterizedType) type;
      return ParameterizedType.forType(t);
    }

    if (type instanceof Class<?>) {
      Class<?> classType = (Class<?>) type;
      return ClassOrInterfaceType.forClass(classType);
    }

    throw new IllegalArgumentException("Unable to create class type from type " + type);
  }

  /**
   * {@inheritDoc}
   * This abstract method allows substitutions to be applied to {@link ClassOrInterfaceType} objects
   * without casting.
   */
  @Override
  public abstract ClassOrInterfaceType apply(Substitution<ReferenceType> substitution);

  public String getClassName() {
    return getRuntimeClass().getSimpleName();
  }

  /**
   * Returns the interface types directly implemented or extended by this class or interface type.
   * Preserves the order in the reflection method {@link Class#getGenericInterfaces()}.
   * If no interfaces are implemented/extended, then returns the empty list.
   *
   * @return the list of interfaces implemented or extended by this type
   */
  public abstract List<ClassOrInterfaceType> getInterfaces();

  /**
   * Returns the package of the runtime class of this type.
   *
   * @return the package of the runtime class of this type, or null if there is none
   */
  public Package getPackage() {
    Class<?> c = getRuntimeClass();
    if (c == null) {
      throw new IllegalArgumentException("Class " + this.toString() + " has no runtime class");
    }
    return c.getPackage();
  }

  /**
   * Finds the parameterized type that is a supertype of this class that also matches the given
   * generic class.
   * For example, if {@code class C<T> implements Comparator<T>} and
   * {@code class A extends C<String>}, then when applied to {@code A}, this method would return
   * {@code C<String>} when given {@code C<T>}, and {@code Comparator<String>} when given
   * {@code Comparator<E>}.
   * Returns null if there is no such type.
   * <p>
   * Performs a depth-first search of the supertype relation for this type.
   * If the goal type is an interface, then searches the interfaces of this type first.
   *
   * @param goalType  the generic class type
   * @return the instantiated type matching the goal type, or null
   */
  public InstantiatedType getMatchingSupertype(GenericClassType goalType) {
    if (this.isInstantiationOf(goalType)) {
      return (InstantiatedType) this;
    }

    if (goalType.isInterface()) {
      List<ClassOrInterfaceType> interfaces = this.getInterfaces();
      for (ClassOrInterfaceType interfaceType : interfaces) {
        if (goalType.getRuntimeClass().isAssignableFrom(interfaceType.getRuntimeClass())) {
          if (interfaceType.isParameterized()) {
            InstantiatedType type = (InstantiatedType) interfaceType;
            if (type.isInstantiationOf(goalType)) {
              return (InstantiatedType) interfaceType;
            }
            InstantiatedType result = type.getMatchingSupertype(goalType);
            if (result != null) {
              return result;
            }
          } else {
            return interfaceType.getMatchingSupertype(goalType);
          }
        }
      }
    }

    ClassOrInterfaceType superclass = this.getSuperclass();
    if (superclass != null
        && !superclass.isObject()
        && goalType.getRuntimeClass().isAssignableFrom(superclass.getRuntimeClass())) {

      if (superclass.isInstantiationOf(goalType)) {
        return (InstantiatedType) superclass;
      }

      return superclass.getMatchingSupertype(goalType);
    }

    return null;
  }

  /**
   * Return the type for the superclass for this class.
   *
   * @return superclass of this type, or the {@code Object} type if this type has no superclass
   */
  public ClassOrInterfaceType getSuperclass() {
    // Default implementation, overridden in subclasses
    return JavaTypes.OBJECT_TYPE;
  }

  /**
   * Indicate whether this class is abstract.
   *
   * @return true if this class is abstract, false otherwise
   */
  public abstract boolean isAbstract();

  /**
   * Indicate whether this class is a member of another class.
   * (see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.5">JLS section 8.5</a>)
   *
   * @return true if this class is a member class, false otherwise
   */
  public abstract boolean isMemberClass();

  /**
   * Indicates whether this class is static.
   *
   * @return true if this class is static, false otherwise
   */
  public abstract boolean isStatic();

  /**
   * Test whether this type is a subtype of the given type according to
   * transitive closure of definition of the <i>direct supertype</i> relation in
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.10.2">
   * section 4.10.2 of JLS for JavaSE 8</a>.
   * @see #isAssignableFrom(Type)
   * @see ParameterizedType#isSubtypeOf(Type)
   *
   * @param otherType  the possible supertype
   * @return true if this type is a subtype of the given type, false otherwise
   */
  @Override
  public boolean isSubtypeOf(Type otherType) {
    if (super.isSubtypeOf(otherType)) {
      return true;
    }

    if (!otherType.isReferenceType()) {
      return false;
    }

    // if otherType is an interface, first check interfaces
    if (otherType.isInterface()) {
      List<ClassOrInterfaceType> interfaces = this.getInterfaces();
      for (ClassOrInterfaceType type : interfaces) {

        if (type.equals(otherType)) {
          return true;
        }
        if (type.isSubtypeOf(otherType)) {
          return true;
        }
      }
    }
    // otherwise, if otherType is an interface, may be interface of a superclass

    // Stop if this type is an interface, because does not have superclass
    if (this.isInterface()) {
      return false;
    }

    ClassOrInterfaceType superClassType = this.getSuperclass();

    // If superclass is Object, then search has failed. So, stop.
    // Otherwise, check whether superclass is a subtype
    return !superClassType.isObject() && superClassType.isSubtypeOf(otherType);
  }
}
