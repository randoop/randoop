package randoop.types;

/**
 * The <code>null</code> type is the type of the value <code>null</code>.
 * As the subtype of all reference types, it is the default lowerbound of
 * a {@link CaptureTypeVariable}.
 */
class NullReferenceType extends ReferenceType {

  NullReferenceType() {}

  @Override
  public boolean equals(Object obj) {
    return obj instanceof NullReferenceType;
  }

  @Override
  public String getName() {
    return "NullType";
  }

  @Override
  public ReferenceType apply(Substitution<ReferenceType> substitution) {
    return this;
  }

  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    return otherType.isReferenceType();
  }

}
