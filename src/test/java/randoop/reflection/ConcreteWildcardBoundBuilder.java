package randoop.reflection;

/** An implementation of {@link WildcardBoundBuilder}. */
class ConcreteWildcardBoundBuilder implements WildcardBoundBuilder<ConcreteWildcardBoundBuilder> {
  @Override
  public WildcardBoundBuilder<ConcreteWildcardBoundBuilder> create() {
    return new ConcreteWildcardBoundBuilder();
  }
}
