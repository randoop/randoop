package randoop.test;

import randoop.util.predicate.Predicate;

/**
 * Class for enums with parameterized types.
 *
 * inspired by Google javascript code in Defects4J Closure 123f
 */
public enum EnumAsPredicate implements Predicate<Integer> {
  ONE {
    @Override
    public boolean test(Integer integer) {
      return false;
    }
  },
  TWO {
    @Override
    public boolean test(Integer integer) {
      return true;
    }
  };

  @Override
  public Predicate<Integer> or(Predicate<Integer> p) {
    return this;
  }

  @Override
  public Predicate<Integer> and(Predicate<Integer> q) {
    return this;
  }
}
