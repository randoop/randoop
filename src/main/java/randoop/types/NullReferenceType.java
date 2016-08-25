package randoop.types;

/**
 * The <code>null</code> type is the type of the value <code>null</code>.
 * As the subtype of all reference types, it is the default lowerbound of
 * a {@link CaptureTypeVariable}.
 */
class NullReferenceType extends ReferenceType {

  private static final NullReferenceType value = new NullReferenceType();

  private NullReferenceType() {}

  /**
   * Returns the null type.
   *
   * @return the null type object
   */
  static NullReferenceType getNullType() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof NullReferenceType) && obj == value;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return this.getName();
  }

  /**
   * {@inheritDoc}
   * This method returns null since the {@link NullReferenceType} does not have a runtime
   * representation
   */
  @Override
  public Class<?> getRuntimeClass() {
    return null;
  }

  @Override
  public ReferenceType apply(Substitution<ReferenceType> substitution) {
    return this;
  }

  @Override
  public String getName() {
    return "NullType";
  }

  /**
   * Indicate whether this type has a wildcard either as or in a type argument.
   *
   * @return true if this type has a wildcard, and false otherwise
   */
  public boolean hasWildcard() {
    return false;
  }

  @Override
  public boolean isSubtypeOf(Type otherType) {
    return !otherType.equals(ConcreteTypes.VOID_TYPE) && otherType.isReferenceType();
  }
}
