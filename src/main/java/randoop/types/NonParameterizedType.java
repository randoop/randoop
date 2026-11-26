package randoop.types;

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;
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

  @Override
  public NonParameterizedType substitute(Substitution substitution) {
    ClassOrInterfaceType enclosing = substituteEnclosingType(substitution);
    if (sameType(enclosing, enclosingType)) {
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
  @Override
  public NonParameterizedType applyCaptureConversion() {
    if (!isMemberClass()) {
      return this;
    }
    ClassOrInterfaceType convertedEnclosingType = enclosingType.applyCaptureConversion();
    if (sameType(convertedEnclosingType, enclosingType)) {
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
    if (!isMemberClass()) {
      return CollectionsPlume.mapList(
          ClassOrInterfaceType::forType, runtimeType.getGenericInterfaces());
    }
    // For a member class such as Outer<String>.Inner, substitute the type arguments of the
    // enclosing types.
    Substitution substitution = getSupertypeSubstitution();
    return CollectionsPlume.mapList(
        (java.lang.reflect.Type type) -> forType(type).substitute(substitution),
        runtimeType.getGenericInterfaces());
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
   * Returns the list of rawtypes for the direct interfaces for this type. These are the erasures of
   * the direct interfaces (JLS section 4.8): a member interface of a generic class is a member of
   * the raw type of that class.
   *
   * @return the list of rawtypes for the direct interfaces of this type
   */
  private List<ClassOrInterfaceType> getRawTypeInterfaces() {
    return CollectionsPlume.mapList(
        (Class<?> c) -> NonParameterizedType.forClass(c).getRawtype(), runtimeType.getInterfaces());
  }

  @Pure
  @Override
  public Class<?> getRuntimeClass() {
    return runtimeType;
  }

  @Override
  public @Nullable ClassOrInterfaceType getSuperclass() {
    if (this.isRawtype()) {
      // The superclass of a raw type is the erasure of its declared superclass (JLS section 4.8).
      // If that is a member class of a generic class, such as Outer<String>.Inner, then the erasure
      // is a member of the raw type Outer, whose supertypes are erased too.
      Class<?> superclass = this.runtimeType.getSuperclass();
      return superclass == null ? null : forClass(superclass).getRawtype();
    } else {
      java.lang.reflect.Type supertype = this.runtimeType.getGenericSuperclass();
      // For a member class such as Outer<String>.Sub, whose superclass is Outer<T>.Base,
      // substitute the type arguments of the enclosing types.
      if (supertype == null) {
        return null;
      }
      ClassOrInterfaceType result = forType(supertype);
      return isMemberClass() ? result.substitute(getSupertypeSubstitution()) : result;
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

  /**
   * {@inheritDoc}
   *
   * <p>A class that declares type parameters is raw. So is a member class (not a static nested
   * class) of a raw type, such as {@code Inner} in raw {@code Outer}, which {@link #getRawtype()}
   * returns for {@code Outer<T>.Inner} (JLS section 4.8). The supertypes of a raw type are erased.
   */
  @Override
  public boolean isRawtype() {
    return runtimeType.getTypeParameters().length > 0
        || (isMemberClass() && enclosingType.isRawtype());
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
   *
   * <p>A member class that declares no type parameters is a subtype of the same member class whose
   * enclosing type is a supertype of its enclosing type. For example, {@code Outer<String>.Inner}
   * is a subtype of {@code Outer<?>.Inner}, but not of {@code Outer<Long>.Inner}.
   */
  @Override
  public boolean isSubtypeOfOrEqualTo(Type otherType) {
    if (super.isSubtypeOfOrEqualTo(otherType)) {
      return true;
    }
    if (!(otherType instanceof ClassOrInterfaceType)) {
      return false;
    }
    ClassOrInterfaceType otherClassType = (ClassOrInterfaceType) otherType;
    if (!otherType.runtimeClassIs(runtimeType)) {
      return hasMemberClassSupertype(otherClassType);
    }
    if (otherClassType.isGenericForm()) {
      return true;
    }
    // A member class that declares no type parameters, such as Outer<String>.Inner, is a subtype
    // of the same member class of a supertype of its enclosing type, such as Outer<?>.Inner.
    return !this.isRawtype()
        && otherClassType instanceof NonParameterizedType
        && this.isMemberClass()
        && otherClassType.isMemberClass()
        && this.enclosingType.isSubtypeOfOrEqualTo(otherClassType.enclosingType);
  }

  /**
   * Returns true if some proper supertype of this type has the same class as {@code memberType}, a
   * member class that declares no type parameters, and is a subtype of {@code memberType} by {@link
   * #isSubtypeOfOrEqualTo(Type)}. For example, for {@code class Child extends Outer<String>.Inner},
   * returns true for {@code Outer<?>.Inner} but false for {@code Outer<Long>.Inner}. The superclass
   * check in {@link ClassOrInterfaceType#isSubclassOf} compares types for equality, so it does not
   * find such a supertype.
   *
   * @param memberType a class or interface type, whose class differs from this type's class
   * @return true if a supertype of this type with the class of {@code memberType} is a subtype of
   *     {@code memberType}
   */
  private boolean hasMemberClassSupertype(ClassOrInterfaceType memberType) {
    if (this.isRawtype()
        || !(memberType instanceof NonParameterizedType)
        || !memberType.isMemberClass()) {
      return false;
    }
    Class<?> target = memberType.getRuntimeClass();
    if (!target.isAssignableFrom(runtimeType)) {
      return false;
    }
    Deque<ClassOrInterfaceType> worklist = new ArrayDeque<>();
    addDirectSupertypes(this, worklist);
    while (!worklist.isEmpty()) {
      ClassOrInterfaceType supertype = worklist.pop();
      if (!target.isAssignableFrom(supertype.getRuntimeClass())) {
        continue;
      }
      if (supertype.runtimeClassIs(target)) {
        if (supertype.isSubtypeOfOrEqualTo(memberType)) {
          return true;
        }
      } else {
        addDirectSupertypes(supertype, worklist);
      }
    }
    return false;
  }

  /**
   * Adds the direct superclass and the direct interfaces of the given type to the worklist.
   *
   * @param type a class or interface type
   * @param worklist the worklist; modified by this method
   */
  private static void addDirectSupertypes(
      ClassOrInterfaceType type, Deque<ClassOrInterfaceType> worklist) {
    ClassOrInterfaceType superclass = type.getSuperclass();
    if (superclass != null) {
      worklist.push(superclass);
    }
    for (ClassOrInterfaceType iface : type.getInterfaces()) {
      worklist.push(iface);
    }
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
