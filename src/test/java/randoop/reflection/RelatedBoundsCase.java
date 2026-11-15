package randoop.reflection;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
   * @param <K> the key type of the maps
   * @param <V> the value type of the maps
   * @param <M> the type of the maps
   * @param maps a collection of maps
   * @return null
   */
  public static <K, V, M extends Map<K, V>> @Nullable Map<K, V> someMap(Collection<M> maps) {
    return null;
  }

  /**
   * Returns null. {@code V} is instantiated by matching an input type, and {@code K} occurs only in
   * the bound of {@code V}.
   *
   * @param <K> the result type
   * @param <V> the type of the nodes
   * @param nodes a list of nodes
   * @return null
   */
  public static <K, V extends Map<K, V>> @Nullable K selfMapKey(List<V> nodes) {
    return null;
  }

  /** A key of a {@link Node}. It is not an input type in the tests. */
  public static class Key {}

  /** A map from keys to nodes. */
  @SuppressWarnings("serial")
  public static class Node extends HashMap<Key, Node> {}

  /**
   * Returns null. {@code V} is instantiated by matching an input type, and {@code T} must then be a
   * proper supertype of the type of {@code V}.
   *
   * @param <T> the result type
   * @param <V> the type of the elements
   * @param elements a list of elements
   * @return null
   */
  public static <T extends Comparable<T>, V extends T> @Nullable T comparableSupertype(
      List<V> elements) {
    return null;
  }

  /** A class that is comparable to itself. */
  public static class Base implements Comparable<Base> {
    @Override
    public int compareTo(Base other) {
      return 0;
    }
  }

  /** A subclass of a comparable class. It is comparable only to {@link Base}, not to itself. */
  public static class Derived extends Base {}

  /**
   * Returns null. The parameter type is a member class of a generic class.
   *
   * @param <E> the type argument of the enclosing class
   * @param inner a member of an {@link Outer}
   * @return null
   */
  public static <E> @Nullable E enclosed(Outer<E>.Inner inner) {
    return null;
  }

  /**
   * A generic class with a member class.
   *
   * @param <E> a type parameter
   */
  public static class Outer<E> {
    /** A member class. */
    public class Inner {}
  }
}
