package randoop.types;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code NonParameterizedType} represents a non-parameterized class, interface,
 * enum, or the rawtype of a generic class.
 * It is a wrapper for a {@link Class} object, which is a runtime representation
 * of a type.
 */
public class NonParameterizedType extends ClassOrInterfaceType {

  /** The runtime class of this simple type. */
  private final Class<?> runtimeClass;

  /**
   * Create a {@link NonParameterizedType} object for the runtime class
   *
   * @param runtimeType  the runtime class for the type
   */
  public NonParameterizedType(Class<?> runtimeType) {
    assert !runtimeType.isPrimitive() : "must be reference type, got " + runtimeType.getName();
    this.runtimeClass = runtimeType;
  }

  /**
   * {@inheritDoc}
   * @return true if the runtime types are the same, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NonParameterizedType)) {
      return false;
    }
    NonParameterizedType t = (NonParameterizedType) obj;
    return this.runtimeClass.equals(t.runtimeClass);
  }

  @Override
  public int hashCode() {
    return runtimeClass.hashCode();
  }

  /**
   * {@inheritDoc}
   * @see #getName()
   *
   * @return the name of this type
   */
  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public NonParameterizedType apply(Substitution<ReferenceType> substitution) {
    return this;
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
    for (java.lang.reflect.Type type : runtimeClass.getGenericInterfaces()) {
      interfaces.add(ClassOrInterfaceType.forType(type));
    }
    return interfaces;
  }

  /**
   * {@inheritDoc}
   * Returns the fully-qualified name of this type in the {@code Class.getCanonicalName()} format
   */
  @Override
  public String getName() {
    return runtimeClass.getCanonicalName();
  }

  /**
   * Returns the list of rawtypes for the direct interfaces for this type.
   *
   * @return the list of rawtypes for the direct interfaces of this type
   */
  private List<ClassOrInterfaceType> getRawTypeInterfaces() {
    List<ClassOrInterfaceType> interfaces = new ArrayList<>();
    for (Class<?> c : runtimeClass.getInterfaces()) {
      interfaces.add(new NonParameterizedType(c));
    }
    return interfaces;
  }

  @Override
  public Class<?> getRuntimeClass() {
    return runtimeClass;
  }

  @Override
  public ClassOrInterfaceType getSuperclass() {
    if (this.isObject()) {
      return this;
    }
    if (this.isRawtype()) {
      Class<?> superclass = this.runtimeClass.getSuperclass();
      if (superclass != null) {
        return new NonParameterizedType(superclass);
      }
    } else {
      java.lang.reflect.Type supertype = this.runtimeClass.getGenericSuperclass();
      if (supertype != null) {
        return ClassOrInterfaceType.forType(supertype);
      }
    }
    return null;
  }

  @Override
  public boolean isAbstract() {
    return Modifier.isAbstract(Modifier.classModifiers() & runtimeClass.getModifiers());
  }

  /**
   * {@inheritDoc}
   * Specifically checks for
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.7">boxing conversion (section 5.1.7)</a>
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
    return runtimeClass.isEnum();
  }

  @Override
  public boolean isInterface() {
    return runtimeClass.isInterface();
  }

  @Override
  public boolean isMemberClass() {
    return runtimeClass.isMemberClass();
  }

  @Override
  public boolean isRawtype() {
    return runtimeClass.getTypeParameters().length > 0;
  }

  @Override
  public boolean isStatic() {
    return Modifier.isStatic(runtimeClass.getModifiers() & Modifier.classModifiers());
  }

  @Override
  public boolean isSubtypeOf(Type otherType) {
    if (super.isSubtypeOf(otherType)) {
      return true;
    }

    if (otherType.isRawtype()) {
      if (otherType.isInterface()) {
        for (Class<?> c : runtimeClass.getInterfaces()) {
          if (otherType.hasRuntimeClass(c)) {
            return true;
          }
          NonParameterizedType superType = new NonParameterizedType(c);
          if (superType.isSubtypeOf(otherType)) {
            return true;
          }
        }
        return false;
      }

      ClassOrInterfaceType superType = this.getSuperclass();
      if (superType != null && !superType.equals(JavaTypes.OBJECT_TYPE)) {
        return otherType.equals(superType) || superType.isSubtypeOf(otherType);
      }
    }

    return false;
  }

  /**
   * If this type is a boxed primitive, unboxes this type and returns the primitive type.
   *
   * @return the primitive type if this is a boxed primitive
   * @throws IllegalArgumentException if this is not a boxed primitive type
   */
  public PrimitiveType toPrimitive() {
    if (this.isBoxedPrimitive()) {
      return new PrimitiveType(PrimitiveTypes.toUnboxedType(this.getRuntimeClass()));
    }
    throw new IllegalArgumentException("Type must be boxed primitive");
  }
}
