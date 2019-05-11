package randoop.types;

/**
 * The {@code null} type is the type of the value {@code null}. As the subtype of all reference
 * types, it is the default lowerbound of a {@link CaptureTypeVariable}.
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
   *
   * <p>This method returns null since the {@link NullReferenceType} does not have a runtime
   * representation
   */
  @Override
  public Class<?> getRuntimeClass() {
    return null;
  }

  @Override
  public ReferenceType substitute(Substitution substitution) {
    return this;
  }

  @Override
  public String getName() {
    return "NullType";
  }

  @Override
  public String getSimpleName() {
    return this.getName();
  }

  @Override
  public String getCanonicalName() {
    return this.getName();
  }

  @Override
  public boolean hasWildcard() {
    return false;
  }

  @Override
  public boolean isSubtypeOf(Type otherType) {
    return !otherType.equals(JavaTypes.VOID_TYPE) && otherType.isReferenceType();
  }
}
