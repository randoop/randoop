package randoop.types;

/**
 * {@code SimpleType} represents an atomic concrete type: a primitive type,
 * a class, an enum, or the rawtype for a generic class. 
 * It is a wrapper for a {@link Class} object, which is a runtime representation
 * of a type. 
 */
public class SimpleType extends ConcreteType {
  
  /** The runtime type of this simple type. */
  private Class<?> runtimeType;
  
  /**
   * Create a {@code Type} object for the runtime class
   * 
   * @param runtimeType  the runtime class for the type
   */
  public SimpleType(Class<?> runtimeType) {
    this.runtimeType = runtimeType;
  }

  /**
   * {@inheritDoc}
   * @return true if the runtime types are the same, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof SimpleType)) {
      return false;
    }
    SimpleType t = (SimpleType)obj;
    return this.runtimeType.equals(t.runtimeType);
  }
  
  @Override
  public int hashCode() {
    return runtimeType.hashCode();
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
   * @return the fully qualified name of this type
   */
  @Override
  public String getName() {
    return runtimeType.getCanonicalName();
  }

  @Override
  public Class<?> getRuntimeClass() {
    return runtimeType;
  }

  @Override
  public boolean isEnum() {
    return runtimeType.isEnum();
  }
  
  @Override 
  public boolean isPrimitive() {
    return runtimeType.isPrimitive();
  }
  
  /**
   * {@inheritDoc}
   * @return true if the runtime type corresponds to a generic, false otherwise
   */
  @Override
  public boolean isRawtype() {
    return runtimeType.getTypeParameters().length > 0;
  }
  
  @Override
  public boolean isVoid() {
    return runtimeType.equals(void.class);
  }
  
  /**
   * {@inheritDoc}
   * Tests for assignability to a {@code SimpleType}.
   * Checks for identity, widening reference, widening primitive,
   * boxing, and unboxing conversions.
   * Does not consider void assignable from/to any type.
   * 
   * @param sourceType  the source type 
   * @return true if the source type can be assigned to this type by an 
   * assignment conversion, false otherwise
   * @throws IllegalArgumentException if source type is null
   */
  @Override
  public boolean isAssignableFrom(ConcreteType sourceType) {
    if (sourceType == null) {
      throw new IllegalArgumentException("Source type may not be null");
    }
    
    // cannot assign to/from void
    if (this.isVoid() || sourceType.isVoid()) {
      return false;
    }
    
    // object eats everything by reference widening
    if (this.isObject()) {
      return true;
    }

    // both rawtype and non-generic superclass eat
    // parameterized types by reference widening
    if (sourceType.isParameterized()) {
      return this.runtimeType.isAssignableFrom(sourceType.getRuntimeClass());
    }
    
    // other cases must be SimpleType to SimpleType
    if (sourceType instanceof SimpleType) {
      return isAssignableFrom((SimpleType)sourceType);
    }

    return false;
  }

  /**
   * Tests for assignability to a {@code SimpleType} from a {@code SimpleType}.
   * Checks for identity, widening reference, widening primitive, boxing, and
   * unboxing conversions.
   * 
   * @param sourceType  the source type
   * @return true if 
   */
  private boolean isAssignableFrom(SimpleType sourceType) {
    // test for identity and reference widening conversions
    if (this.runtimeType.isAssignableFrom(sourceType.runtimeType)) { 
      return true;
    }
    
    // test for primitive widening or unboxing conversion
    if (this.isPrimitive()) {
      if (sourceType.isPrimitive()) { // primitive widening conversion 
        return PrimitiveTypes.isAssignable(this.runtimeType, sourceType.runtimeType);
      } else { // unbox then widen conversion
        Class<?> tUnboxed = PrimitiveTypes.toUnboxedType(sourceType.runtimeType);
        return tUnboxed != null && PrimitiveTypes.isAssignable(this.runtimeType, tUnboxed);
      }
    } 
    
    // test for boxing conversion
    if (sourceType.isPrimitive()) {
      Class<?> tBoxed = PrimitiveTypes.getBoxedType(sourceType.runtimeType);
      return tBoxed != null && this.runtimeType.isAssignableFrom(tBoxed);
    }
    
    return false;
  }
}
