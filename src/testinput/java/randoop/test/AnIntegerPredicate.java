package randoop.test;

import randoop.util.predicate.Predicate;

/** Input test for parameterized types. */
public class AnIntegerPredicate implements Predicate<Integer> {

  @Override
  public boolean test(Integer integer) {
    return false;
  }

  @Override
  public Predicate<Integer> or(Predicate<Integer> p) {
    return null;
  }

  @Override
  public Predicate<Integer> and(Predicate<Integer> q) {
    return null;
  }
}
