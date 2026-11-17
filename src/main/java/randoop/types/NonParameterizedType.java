package randoop.types;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.plumelib.util.CollectionsPlume;

/**
 * {@code NonParameterizedType} represents a non-parameterized class, interface, enum, or the
 * rawtype of a generic class. It is a wrapper for a {@link Class} object, which is a runtime
 * representation of a type.
 */
public class NonParameterizedType extends ClassOrInterfaceType {

  /** The runtime class of this simple type. */
  private final Class<?> runtimeType;

  /** A cache of all NonParameterizedTypes that have been created. */
  private static final Map<Class<?>, NonParameterizedType> cache = new HashMap<>();

  /**
   * Create a {@link NonParameterizedType} object for the runtime class.
   *
   * @param runtimeType the runtime class for the type
   * @return a NonParameterizedType for the argument
   */
  public static NonParameterizedType forClass(Class<?> runtimeType) {
    // This cannot be
    //   return cache.computeIfAbsent(runtimeType, NonParameterizedType::new);
    // because NonParameterizedType::new side-effects `cache`.  It does so by calling
    // ClassOrInterfaceType.forClass which may call back into NonParameterizedType.

    NonParameterizedType cached = cache.get(runtimeType);
    if (cached == null) {
      cached = new NonParameterizedType(runtimeType);
      cache.put(runtimeType, cached);
    }
    return cached;
  }

  /**
   * Create a {@link NonParameterizedType} object for the runtime class.
   *
   * @param runtimeType the runtime class for the type
   */
  public NonParameterizedType(Class<?> runtimeType) {
    super();
    assert !runtimeType.isPrimitive() : "must be reference type, got " + runtimeType.getName();
    this.runtimeType = runtimeType;
    Class<?> enclosingClass = runtimeType.getEnclosingClass();
    if (enclosingClass != null) {
      this.enclosingType = ClassOrInterfaceType.forClass(enclosingClass);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return true if the runtime types are the same, false otherwise
   */
  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NonParameterizedType)) {
      return false;
    }
    NonParameterizedType other = (NonParameterizedType) obj;
    return super.equals(obj) && this.runtimeType.equals(other.runtimeType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(runtimeType);
  }

  @SuppressWarnings("ReferenceEquality") // detects any change; see Type#substitute
  @Override
  public NonParameterizedType substitute(Substitution substitution) {
    ClassOrInterfaceType enclosing = substituteEnclosingType(substitution);
    if (enclosing == enclosingType) {
      return this;
    }
    NonParameterizedType result = new NonParameterizedType(this.runtimeType);
    if (isMemberClass()) {
      result.enclosingType = enclosing;
    }
    return result;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Does not side-effect this type, which can be shared: for example, {@link #forClass} caches
   * it, and {@link #substitute} can return it. If capture conversion changes the enclosing type of
   * this member class, then the result is a new type.
   */
  @SuppressWarnings("ReferenceEquality") // detects any change made by capture conversion
  @Override
  public NonParameterizedType applyCaptureConversion() {
    if (!isMemberClass()) {
      return this;
    }
    ClassOrInterfaceType convertedEnclosingType = enclosingType.applyCaptureConversion();
    if (convertedEnclosingType == enclosingType) {
      return this;
    }
    NonParameterizedType result = new NonParameterizedType(this.runtimeType);
    result.enclosingType = convertedEnclosingType;
    return result;
  }

  @Override
  public List<ClassOrInterfaceType> getInterfaces() {
    if (this.isRawtype()) {
      return this.getRawTypeInterfaces();
    }
    return getGenericInterfaces();
  }

  /**
   * Returns the list of direct interfaces for this class.
   *
   * @return the list of direct interfaces for this class or interface type
   */
  private List<ClassOrInterfaceType> getGenericInterfaces() {
    Substitution substitution = getEnclosingTypeSubstitution();
    return CollectionsPlume.mapList(
        (java.lang.reflect.Type t) -> substituteIfNonNull(forType(t), substitution),
        runtimeType.getGenericInterfaces());
  }

  /**
   * Returns the substitution of the type arguments of the enclosing types of this member class for
   * the type parameters of the enclosing classes. For example, for {@code Outer<String>.Inner}, the
   * result maps the type parameter of {@code Outer} to {@code String}. Applying it to a supertype in
   * the declaration of this class, such as {@code ArrayList<T>}, yields the supertype of this type,
   * such as {@code ArrayList<String>}.
   *
   * @return the substitution for the type parameters of the enclosing classes, or null if there is
   *     none or if a type argument of an enclosing type is a wildcard
   */
  private @Nullable Substitution getEnclosingTypeSubstitution() {
    Substitution result = null;
    ClassOrInterfaceType type = this;
    while (type.isMemberClass()) {
      ClassOrInterfaceType enclosing = type.enclosingType;
      if (enclosing instanceof InstantiatedType) {
        Substitution substitution = ((InstantiatedType) enclosing).getTypeSubstitution();
        if (substitution == null) {
          return null;
        }
        result = result == null ? substitution : result.extend(substitution);
      }
      type = enclosing;
    }
    return result;
  }

  /**
   * Returns the given type with the given substitution applied, or the type itself if the
   * substitution is null.
   *
   * @param type a type
   * @param substitution a substitution, or null
   * @return {@code type} with {@code substitution} applied, if it is non-null
   */
  private static ClassOrInterfaceType substituteIfNonNull(
      ClassOrInterfaceType type, @Nullable Substitution substitution) {
    return substitution == null ? type : type.substitute(substitution);
  }

  @Override
  public NonParameterizedType getRawtype() {
    if (this.isMemberClass()) {
      // Special case: non-parameterized member classes can still carry type variables from
      // their enclosing type. Rawifying only the member class is not enough.
      NonParameterizedType rawEnclosingType = enclosingType.getRawtype();
      if (!enclosingType.equals(rawEnclosingType)) {
        NonParameterizedType rawType = new NonParameterizedType(runtimeType);
        rawType.enclosingType = rawEnclosingType;
        return rawType;
      }
    }
    return this;
  }

  /**
   * Returns the list of rawtypes for the direct interfaces for this type.
   *
   * @return the list of rawtypes for the direct interfaces of this type
   */
  private List<ClassOrInterfaceType> getRawTypeInterfaces() {
    return CollectionsPlume.mapList(NonParameterizedType::forClass, runtimeType.getInterfaces());
  }

  @Pure
  @Override
  public Class<?> getRuntimeClass() {
    return runtimeType;
  }

  @Override
  public ClassOrInterfaceType getSuperclass() {
    if (this.isRawtype()) {
      Class<?> superclass = this.runtimeType.getSuperclass();
      return superclass == null ? null : forClass(superclass);
    } else {
      java.lang.reflect.Type supertype = this.runtimeType.getGenericSuperclass();
      return supertype == null
          ? null
          : substituteIfNonNull(forType(supertype), getEnclosingTypeSubstitution());
    }
  }

  @Override
  public boolean isAbstract() {
    return Modifier.isAbstract(Modifier.classModifiers() & runtimeType.getModifiers());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Specifically checks for <a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.7">boxing
   * conversion (section 5.1.7)</a>
   */
  @Override
  public boolean isAssignableFrom(Type sourceType) {
    // check identity and reference widening
    if (super.isAssignableFrom(sourceType)) {
      return true;
    }

    // otherwise, check for boxing conversion
    return sourceType.isPrimitive()
        && !sourceType.isVoid() // JLS doesn't say so, void is primitive
        && this.isAssignableFrom(((PrimitiveType) sourceType).toBoxedPrimitive());
  }

  @Override
  public boolean isBoxedPrimitive() {
    return PrimitiveTypes.isBoxedPrimitive(this.getRuntimeClass());
  }

  @Override
  public boolean isEnum() {
    // Return true if the run-time type is an enum type or is an enum constant.
    if (runtimeType.isEnum()) {
      return true;
    }
    // An enum constant is represented (by the javac compiler) as a subclass of an enum type.
    Class<?> superClass = getRuntimeClass().getSuperclass();
    return superClass != null && superClass.isEnum();
  }

  /**
   * {@inheritDoc}
   *
   * <p>For a {@link NonParameterizedType}, if this type instantiates the {@code otherType}, which
   * is a {@link NonParameterizedType} by {@link
   * ClassOrInterfaceType#isInstantiationOf(ReferenceType)} also checks that runtime classes are
   * equal. This allows for proper matching of member classes that are of {@link
   * NonParameterizedType}.
   */
  @Override
  public boolean isInstantiationOf(ReferenceType otherType) {
    boolean instantiationOf = super.isInstantiationOf(otherType);
    if ((otherType instanceof NonParameterizedType)) {
      return instantiationOf && this.runtimeClassIs(otherType.getRuntimeClass());
    }
    return instantiationOf;
  }

  @Override
  public boolean isGenericForm() {
    return !isRawtype() && super.isGenericForm();
  }

  @Override
  public boolean isInterface() {
    return runtimeType.isInterface();
  }

  @Override
  public boolean isRawtype() {
    return runtimeType.getTypeParameters().length > 0;
  }

  @Override
  public boolean isStatic() {
    return Modifier.isStatic(runtimeType.getModifiers() & Modifier.classModifiers());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Also returns true if {@code otherType} is the generic form of this type's class (see {@link
   * ClassOrInterfaceType#isGenericForm()}). For example, raw {@code List} is a subtype of {@code
   * List<E>}, and {@code Outer<String>.Inner} is a subtype of {@code Outer<T>.Inner}. This is not a
   * JLS subtyping rule. Raw {@code List} is not a subtype of any other parameterized type, such as
   * {@code List<T>} for a type variable {@code T}; it is assignable to such a parameterized type
   * only by unchecked conversion.
   */
  @Override
  public boolean isSubtypeOfOrEqualTo(Type otherType) {
    if (super.isSubtypeOfOrEqualTo(otherType)) {
      return true;
    }
    return otherType instanceof ClassOrInterfaceType
        && otherType.runtimeClassIs(runtimeType)
        && ((ClassOrInterfaceType) otherType).isGenericForm();
  }

  /**
   * If this type is a boxed primitive, unboxes this type and returns the primitive type.
   *
   * @return the primitive type if this is a boxed primitive
   * @throws IllegalArgumentException if this is not a boxed primitive type
   */
  public PrimitiveType toPrimitive() {
    if (this.isBoxedPrimitive()) {
      Class<?> primitiveClass = PrimitiveTypes.toUnboxedType(this.getRuntimeClass());
      return PrimitiveType.forClass(primitiveClass);
    }
    throw new IllegalArgumentException("Type must be boxed primitive");
  }
}
