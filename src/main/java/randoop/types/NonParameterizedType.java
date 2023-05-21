package randoop.types;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    assert !runtimeType.isPrimitive() : "must be reference type, got " + runtimeType.getName();
    this.runtimeType = runtimeType;
    Class<?> enclosingClass = runtimeType.getEnclosingClass();
    if (enclosingClass != null) {
      this.setEnclosingType(ClassOrInterfaceType.forClass(enclosingClass));
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return true if the runtime types are the same, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
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
    return Objects.hash(runtimeType);
  }

  @Override
  public NonParameterizedType substitute(Substitution substitution) {
    return (NonParameterizedType)
        substitute(substitution, new NonParameterizedType(this.runtimeType));
  }

  @Override
  public NonParameterizedType applyCaptureConversion() {
    return (NonParameterizedType) applyCaptureConversion(this);
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
    return CollectionsPlume.mapList(
        ClassOrInterfaceType::forType, runtimeType.getGenericInterfaces());
  }

  @Override
  public NonParameterizedType getRawtype() {
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

  @Override
  public Class<?> getRuntimeClass() {
    return runtimeType;
  }

  @Override
  public ClassOrInterfaceType getSuperclass() {
    if (this.isObject()) {
      return this;
    }
    if (this.isRawtype()) {
      Class<?> superclass = this.runtimeType.getSuperclass();
      if (superclass != null) {
        return NonParameterizedType.forClass(superclass);
      }
    } else {
      java.lang.reflect.Type supertype = this.runtimeType.getGenericSuperclass();
      if (supertype != null) {
        return ClassOrInterfaceType.forType(supertype);
      }
    }
    return JavaTypes.OBJECT_TYPE;
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
    return runtimeType.isEnum();
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
