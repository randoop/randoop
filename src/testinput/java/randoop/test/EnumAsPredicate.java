package randoop.test;

import java.util.function.Predicate;

/**
 * Class for enums with parameterized types.
 *
 * <p>inspired by Google javascript code in Defects4J Closure 123f
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
  public Predicate<Integer> or(Predicate<? super Integer> p) {
    return this;
  }

  @Override
  public Predicate<Integer> and(Predicate<? super Integer> q) {
    return this;
  }
}
