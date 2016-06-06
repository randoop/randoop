package randoop.types;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code SimpleClassOrInterfaceType} represents a non-generic class, interface, enum, or the
 * rawtype of a generic class.
 * It is a wrapper for a {@link Class} object, which is a runtime representation
 * of a type.
 */
public class SimpleClassOrInterfaceType extends ClassOrInterfaceType {

  /** The runtime type of this simple type. */
  private final Class<?> runtimeClass;

  /**
   * Create a {@code ConcreteSimpleType} object for the runtime class
   *
   * @param runtimeType  the runtime class for the type
   */
  public SimpleClassOrInterfaceType(Class<?> runtimeType) {
    assert ! runtimeType.isPrimitive() : "must be reference type";
    this.runtimeClass = runtimeType;
  }

  /**
   * {@inheritDoc}
   * @return true if the runtime types are the same, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SimpleClassOrInterfaceType)) {
      return false;
    }
    SimpleClassOrInterfaceType t = (SimpleClassOrInterfaceType) obj;
    return this.runtimeClass.equals(t.runtimeClass);
  }

  @Override
  public int hashCode() {
    return runtimeClass.hashCode();
  }

  /**
   * {@inheritDoc}
   * @return the name of this type
   */
  @Override
  public String toString() {
    return this.getName();
  }

  /**
   * {@inheritDoc}
   * @return the fully-qualified name of this type
   */
  @Override
  public String getName() {
    return runtimeClass.getCanonicalName();
  }

  /**
   * {@inheritDoc}
   * @return the {@code Class} object for this concrete simple type
   */
  @Override
  public Class<?> getRuntimeClass() {
    return runtimeClass;
  }

  @Override
  public boolean isAbstract() {
    return Modifier.isAbstract(Modifier.classModifiers() & runtimeClass.getModifiers());
  }

  @Override
  public boolean isMemberClass() {
    return runtimeClass.isMemberClass();
  }

  @Override
  public boolean isEnum() {
    return runtimeClass.isEnum();
  }

  @Override
  public boolean isInterface() { return runtimeClass.isInterface(); }

  @Override
  public boolean isBoxedPrimitive() {
    return PrimitiveTypes.isBoxedPrimitiveTypeOrString(runtimeClass)
            && ! this.equals(ConcreteTypes.STRING_TYPE);
  }

  @Override
  public boolean isString() {
    return this.equals(ConcreteTypes.STRING_TYPE);
  }

  /**
   * {@inheritDoc}
   * @return true if the runtime type corresponds to a generic, false otherwise
   */
  @Override
  public boolean isRawtype() {
    return runtimeClass.getTypeParameters().length > 0;
  }

  @Override
  public boolean isVoid() {
    return runtimeClass.equals(void.class);
  }

  @Override
  public SimpleClassOrInterfaceType apply(Substitution<ReferenceType> substitution) {
    return this;
  }

  /**
   * {@inheritDoc}
   * Specifically checks for
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.7">boxing conversion (section 5.1.7)</a>
   */
  @Override
  public boolean isAssignableFrom(GeneralType sourceType) {
    // check identity and reference widening
    if (super.isAssignableFrom(sourceType)) {
      return true;
    }

    // otherwise, check for boxing conversion
    return sourceType.isPrimitive()
        && ! sourceType.isVoid()
        && this.isAssignableFrom(sourceType.toBoxedPrimitive());
  }

  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    if (super.isSubtypeOf(otherType)) {
      return true;
    }

    if (otherType.isRawtype()) {
      if (otherType.isInterface()) {
        for (Class<?> c : runtimeClass.getInterfaces()) {
          if (otherType.hasRuntimeClass(c)) {
            return true;
          }
          SimpleClassOrInterfaceType superType = new SimpleClassOrInterfaceType(c);
          if (superType.isSubtypeOf(otherType)) {
            return true;
          }
        }
        return false;
      }

      ClassOrInterfaceType superType = this.getSuperclass();
      if (superType != null && ! superType.equals(ConcreteTypes.OBJECT_TYPE)) {
        return otherType.equals(superType)
                || superType.isSubtypeOf(otherType);
      }
    }

    return false;
  }

  @Override
  public ClassOrInterfaceType getSuperclass() {
    if (this.equals(ConcreteTypes.OBJECT_TYPE)) {
      return this;
    }
    if (this.isRawtype()) {
      Class<?> superclass = this.runtimeClass.getSuperclass();
      if (superclass != null) {
        return new SimpleClassOrInterfaceType(superclass);
      }
    } else {
      Type supertype = this.runtimeClass.getGenericSuperclass();
      if (supertype != null) {
        return ClassOrInterfaceType.forType(supertype);
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   * If this is a rawtype then returns interfaces as rawtypes.
   */
  @Override
  public List<ClassOrInterfaceType> getInterfaces() {
    if (this.isRawtype()) {
      return this.getRawTypeInterfaces();
    }
    return getGenericInterfaces();
  }

  /**
   * {@inheritDoc}
   * @return false, since a simple class is not an instantiation of any generic class
   */
  @Override
  public boolean isInstantiationOf(GenericClassType genericClassType) {
    return false;
  }

  @Override
  public boolean isStatic() {
    return Modifier.isStatic(runtimeClass.getModifiers() & Modifier.classModifiers());
  }

  /**
   * Returns the list of interfaces for this class.
   *
   * @return the list of interfaces for this class or interface type
   */
  private List<ClassOrInterfaceType> getGenericInterfaces() {
    List<ClassOrInterfaceType> interfaces = new ArrayList<>();
    for (Type type : runtimeClass.getGenericInterfaces()) {
      interfaces.add(ClassOrInterfaceType.forType(type));
    }
    return interfaces;
  }

  /**
   * Returns the list of rawtype interfaces for this type.
   *
   * @return the list of rawtypes for the interfaces of this type
   */
  private List<ClassOrInterfaceType> getRawTypeInterfaces() {
    List<ClassOrInterfaceType> interfaces = new ArrayList<>();
    for (Class<?> c : runtimeClass.getInterfaces()) {
      interfaces.add(new SimpleClassOrInterfaceType(c));
    }
    return interfaces;
  }

  @Override
  public PrimitiveType toPrimitive() {
    if (this.isBoxedPrimitive()) {
      return new PrimitiveType(PrimitiveTypes.toUnboxedType(this.getRuntimeClass()));
    }
    throw new IllegalArgumentException("Type must be boxed primitive");
  }

  @Override
  public ClassOrInterfaceType toBoxedPrimitive() {
    if (this.isBoxedPrimitive()) {
      return this;
    }
    throw new IllegalArgumentException("Type must be primitive");
  }
}
