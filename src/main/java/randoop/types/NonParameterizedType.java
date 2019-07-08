package randoop.types;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@code NonParameterizedType} represents a non-parameterized class, interface, enum, or the
 * rawtype of a generic class. It is a wrapper for a {@link Class} object, which is a runtime
 * representation of a type.
 */
public class NonParameterizedType extends ClassOrInterfaceType {

  /** The runtime class of this simple type. */
  private final Class<?> runtimeType;

  /**
   * Create a {@link NonParameterizedType} object for the runtime class.
   *
   * @param runtimeType the runtime class for the type
   */
  public NonParameterizedType(Class<?> runtimeType) {
    assert !runtimeType.isPrimitive() : "must be reference type, got " + runtimeType.getName();
    this.runtimeType = runtimeType;
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

  /**
   * {@inheritDoc}
   *
   * @return the name of this type
   * @see #getName()
   */
  @Override
  public String toString() {
    return this.getName();
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
    List<ClassOrInterfaceType> interfaces = new ArrayList<>();
    for (java.lang.reflect.Type type : runtimeType.getGenericInterfaces()) {
      interfaces.add(ClassOrInterfaceType.forType(type));
    }
    return interfaces;
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
    List<ClassOrInterfaceType> interfaces = new ArrayList<>();
    for (Class<?> c : runtimeType.getInterfaces()) {
      interfaces.add(new NonParameterizedType(c));
    }
    return interfaces;
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
        return new NonParameterizedType(superclass);
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
   * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.7">boxing conversion
   * (section 5.1.7)</a>
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
