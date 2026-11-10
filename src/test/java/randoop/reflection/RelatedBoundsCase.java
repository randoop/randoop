package randoop.reflection;

import java.util.Collection;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Methods whose type variables have bounds that mention other type variables of the method. */
public class RelatedBoundsCase {

  /**
   * Returns the given value.
   *
   * @param <K> the result type
   * @param <V> the argument type, a subtype of {@code K}
   * @param value a value
   * @return {@code value}
   */
  public static <K, V extends K> K widen(V value) {
    return value;
  }

  /**
   * Returns the first argument.
   *
   * @param <A> the result type
   * @param <B> the type of the first argument, a subtype of {@code A}
   * @param <C> the type of the second argument, a subtype of {@code B}
   * @param b a value
   * @param c a value
   * @return {@code b}
   */
  public static <A, B extends A, C extends B> A chain(B b, C c) {
    return b;
  }

  /**
   * Returns null. The parameter type is parameterized, so {@code M} is instantiated by matching an
   * input type, and the bound of {@code M} mentions {@code K} and {@code V}, which remain to be
   * instantiated.
   *
   * @param <K> the result type
   * @param <V> the value type of the maps
   * @param <M> the type of the maps
   * @param maps a collection of maps
   * @return null
   */
  public static <K, V, M extends Map<K, V>> @Nullable K someKey(Collection<M> maps) {
    return null;
  }
}
