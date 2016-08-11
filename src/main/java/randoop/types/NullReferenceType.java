package randoop.types;

/**
 * The <code>null</code> type is the type of the value <code>null</code>.
 * As the subtype of all reference types, it is the default lowerbound of
 * a {@link CaptureTypeVariable}.
 */
class NullReferenceType extends ReferenceType {

  private static final NullReferenceType value = new NullReferenceType();

  private NullReferenceType() {}

  @Override
  public boolean equals(Object obj) {
    return obj instanceof NullReferenceType;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public ReferenceType apply(Substitution<ReferenceType> substitution) {
    return this;
  }

  @Override
  public String getName() {
    return "NullType";
  }

  static NullReferenceType getNullType() {
    return value;
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
