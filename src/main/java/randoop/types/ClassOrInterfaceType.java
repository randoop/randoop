package randoop.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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

  /** The enclosing type: non-null if this is a member class. */
  private ClassOrInterfaceType enclosingType = null;

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

    ClassOrInterfaceType type;
    if (classType.getTypeParameters().length > 0) {
      type = ParameterizedType.forClass(classType);
    } else {
      type = new NonParameterizedType(classType);
    }
    if (classType.isMemberClass()) {
      type.setEnclosingType(ClassOrInterfaceType.forClass(classType.getEnclosingClass()));
    }
    return type;
  }

  /**
   * Creates a {@link ClassOrInterfaceType} object for a given {@code java.lang.reflect.Type}
   * reference when there is no generic declaration.
   * This occurs when supertypes are found for a type.
   *
   * @param type  the type reference
   * @return the {@link ClassOrInterfaceType} for the given {@code type}
   */
  public static ClassOrInterfaceType forType(java.lang.reflect.Type type) {
    if (type instanceof java.lang.reflect.ParameterizedType) {
      java.lang.reflect.ParameterizedType t = (java.lang.reflect.ParameterizedType) type;
      if (((Class<?>) t.getRawType()).getTypeParameters().length > 0) {
        return ParameterizedType.forType(t);
      }
    }
    return forType(ParameterTable.emptyTable(), type);
  }

  /**
   * Creates a {@code ClassOrInterfaceType} object for a given
   * {@code java.lang.reflect.Type} reference.
   * If type is a {@code java.lang.reflect.ParameterizedType}, then calls
   * {@link ParameterizedType#forType(ParameterTable,java.lang.reflect.Type)}.
   * Otherwise, if type is a {@code Class} object, calls {@link #forClass(Class)}.
   *
   * @param type  the type reference
   * @return the {@code ClassOrInterfaceType} object for the given type
   */
  public static ClassOrInterfaceType forType(
      ParameterTable parameterTable, java.lang.reflect.Type type) {

    if (type instanceof java.lang.reflect.ParameterizedType) {
      java.lang.reflect.ParameterizedType t = (java.lang.reflect.ParameterizedType) type;
      // non-generic member classes of a generic class show up as ParameterizedType
      // treat these as Class<?>
      Class<?> rawType = (Class<?>) t.getRawType();
      if (rawType.getTypeParameters().length == 0) {
        return ClassOrInterfaceType.forClass(rawType);
      }
      return ParameterizedType.forType(parameterTable, t);
    }

    if (type instanceof Class<?>) {
      Class<?> classType = (Class<?>) type;
      // if the type is generic, forcing the type to be a parameterized type can result in errors
      // because type parameters will be from the class declaration rather than the context of
      // the type in the code.  In this case, it is possible to have two distinct
      // java.lang.reflect.TypeVariables that represent the same type parameter.
      //
      return new NonParameterizedType(classType);
    }

    throw new IllegalArgumentException("Unable to create class type from type " + type);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ClassOrInterfaceType)) {
      return false;
    }
    ClassOrInterfaceType otherType = (ClassOrInterfaceType) obj;
    return !(this.isMemberClass() && otherType.isMemberClass())
        || this.enclosingType.equals(otherType.enclosingType);
  }

  /**
   * {@inheritDoc}
   * This abstract method allows substitutions to be applied to {@link ClassOrInterfaceType} objects
   * without casting.
   */
  @Override
  public abstract ClassOrInterfaceType apply(Substitution<ReferenceType> substitution);

  /**
   * Applies the substitution to the enclosing type of this type and adds the result as the
   * enclosing class of the given type.
   *
   * @param substitution  the substitution to apply to the enclosing type
   * @param type  the type to which resulting enclosing type is to be added
   * @return the type with enclosing type added if needed
   */
  final ClassOrInterfaceType apply(
      Substitution<ReferenceType> substitution, ClassOrInterfaceType type) {
    if (this.isMemberClass() && !this.isStatic()) {
      type.setEnclosingType(enclosingType.apply(substitution));
    }
    return type;
  }

  @Override
  public abstract ClassOrInterfaceType applyCaptureConversion();

  /**
   * Applies capture conversion to the enclosing type of this type and adds the result as the
   * enclosing class of the given type.
   *
   * @param type  this type with capture conversion applied
   * @return  the type with converted enclosing type
   */
  final ClassOrInterfaceType applyCaptureConversion(ClassOrInterfaceType type) {
    if (this.isMemberClass() && !this.isStatic()) {
      type.setEnclosingType(enclosingType.applyCaptureConversion());
    }
    return type;
  }

  /**
   * Returns the name of this class type.
   * Does not include package, enclosing classes, or type arguments.
   *
   * @return the name of this class
   */
  public String getSimpleName() {
    return getRuntimeClass().getSimpleName();
  }

  @Override
  public String getCanonicalName() {
    return getRuntimeClass().getCanonicalName();
  }

  @Override
  public String getName() {
    if (this.isMemberClass()) {
      if (this.isStatic()) {
        return enclosingType.getCanonicalName() + "." + this.getSimpleName();
      }
      return enclosingType.getName() + "." + this.getSimpleName();
    }
    return this.getCanonicalName();
  }

  @Override
  public String getUnqualifiedName() {
    String prefix = "";
    if (this.isMemberClass()) {
      prefix = enclosingType.getUnqualifiedName() + ".";
    }
    return prefix + this.getSimpleName();
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
   * Returns the non-parameterized form of this class type.
   *
   * @return  the non-parameterized form of this class type
   */
  public abstract NonParameterizedType getRawtype();

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
   * Computes a substitution that can be applied to the type variables of the generic goal type to
   * instantiate operations of this type, possibly inherited from from the goal type.
   * The substitution will unify this type or a supertype of this type with the given goal type.
   *
   * If there is no unifying substitution, returns {@code null}.
   *
   * @param goalType  the generic type for which a substitution is needed
   * @return a substitution unifying this type or a supertype of this type with the goal type
   */
  public Substitution<ReferenceType> getInstantiatingSubstitution(ClassOrInterfaceType goalType) {
    assert goalType.isGeneric() : "goal type must be generic";

    Substitution<ReferenceType> substitution = new Substitution<>();
    if (this.isMemberClass() && !this.isStatic()) {
      substitution = enclosingType.getInstantiatingSubstitution(goalType);
      if (substitution == null) {
        return null;
      }
    }

    if (goalType instanceof GenericClassType) {
      InstantiatedType supertype = this.getMatchingSupertype((GenericClassType) goalType);
      if (supertype != null) {
        Substitution<ReferenceType> supertypeSubstitution = supertype.getTypeSubstitution();
        if (supertypeSubstitution == null) {
          return null;
        }
        substitution = substitution.extend(supertypeSubstitution);
      }
    }
    return substitution;
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

  public Collection<ClassOrInterfaceType> getSuperTypes() {
    Collection<ClassOrInterfaceType> supertypes = new HashSet<>();
    if (this.isObject()) {
      return supertypes;
    }
    ClassOrInterfaceType superclass = this.getSuperclass();
    if (superclass != null) {
      supertypes.add(superclass);
      supertypes.addAll(superclass.getSuperTypes());
    }
    List<ClassOrInterfaceType> interfaces = this.getInterfaces();
    for (ClassOrInterfaceType interfaceType : interfaces) {
      if (interfaceType != null) {
        supertypes.add(interfaceType);
        supertypes.addAll(interfaceType.getSuperTypes());
      }
    }
    return supertypes;
  }

  /**
   * Indicate whether this class is abstract.
   *
   * @return true if this class is abstract, false otherwise
   */
  public abstract boolean isAbstract();

  @Override
  public boolean isGeneric() {
    return this.isMemberClass() && !this.isStatic() && enclosingType.isGeneric();
  }

  /**
   * {@inheritDoc}
   * For a {@link ClassOrInterfaceType} that is a member class, if
   * {@code otherType} is also a member class, then the enclosing type of
   * this type must instantiate the enclosing type of {@code otherType}.
   */
  @Override
  public boolean isInstantiationOf(ReferenceType otherType) {
    if (super.isInstantiationOf(otherType)) {
      return true;
    }
    if (this.isMemberClass() && (otherType instanceof ClassOrInterfaceType)) {
      ClassOrInterfaceType otherClassType = (ClassOrInterfaceType) otherType;
      return otherClassType.isMemberClass()
          && this.enclosingType.isInstantiationOf(otherClassType.enclosingType);
    }
    return false;
  }

  /**
   * Indicate whether this class is a member of another class.
   * (see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.5">JLS section 8.5</a>)
   *
   * @return true if this class is a member class, false otherwise
   */
  public final boolean isMemberClass() {
    return enclosingType != null;
  }

  @Override
  public boolean isParameterized() {
    return this.isMemberClass() && !this.isStatic() && enclosingType.isParameterized();
  }

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

  /**
   * Indicate whether this type has a wildcard either as or in a type argument.
   *
   * @return true if this type has a wildcard, and false otherwise
   */
  public boolean hasWildcard() {
    return false;
  }

  /**
   * Sets the enclosing type for this class type.
   *
   * @param enclosingType  the type for the class enclosing the declaration of this type
   */
  private void setEnclosingType(ClassOrInterfaceType enclosingType) {
    this.enclosingType = enclosingType;
  }

  /**
   * Returns the type arguments for this type.
   *
   * @return the list of type arguments
   */
  public List<TypeArgument> getTypeArguments() {
    return new ArrayList<>();
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    if (this.isMemberClass() && !this.isStatic()) {
      return enclosingType.getTypeParameters();
    }
    return new ArrayList<>();
  }

  public boolean isClassType() {
    return true;
  }

  public ParameterTable getParameterTable() {
    return ParameterTable.emptyTable();
  }
}
