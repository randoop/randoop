package randoop.test;

import java.util.function.Predicate;

/** Input test for parameterized types. */
public class AnIntegerPredicate implements Predicate<Integer> {

  @Override
  public boolean test(Integer integer) {
    return false;
  }

  @Override
  public Predicate<Integer> or(Predicate<? super Integer> p) {
    return null;
  }

  @Override
  public Predicate<Integer> and(Predicate<? super Integer> q) {
    return null;
  }
}
