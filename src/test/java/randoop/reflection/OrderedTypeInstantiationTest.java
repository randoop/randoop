package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.JavaTypes;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeArgument;
import randoop.types.TypeVariable;

/**
 * Tests that {@link TypeInstantiator} instantiates the ordered type parameter of a class such as
 * {@code TreeSet} with a type that has an ordering.
 */
public class OrderedTypeInstantiationTest {

  /** A comparator interface that is not {@code java.util.Comparator}. */
  public interface MyComparator<T> {
    int compare(T o1, T o2);
  }

  /** A comparator for strings. */
  public static class MyStringComparator implements MyComparator<String> {
    @Override
    public int compare(String o1, String o2) {
      return o1.compareTo(o2);
    }
  }

  /** A class whose first type parameter is unordered and whose second is ordered. */
  public static class MyOrderedMap<V, K> {
    public MyOrderedMap() {}

    public MyOrderedMap(MyComparator<? super K> comparator) {}
  }

  /** A subclass of an ordered class, with no comparator constructor of its own. */
  public static class MyTreeSet<E> extends TreeSet<E> {
    private static final long serialVersionUID = 20261107L;

    public MyTreeSet() {}
  }

  /** A sorted type whose first type parameter is unordered and whose second is ordered. */
  public static class MySortedMap<V, K> {
    public MySortedMap(MyComparator<? super K> comparator) {}

    public MySortedMap(MySortedMap<?, K> other) {}

    public MyComparator<? super K> comparator() {
      return null;
    }
  }

  /** An instantiation of {@link MySortedMap} whose unordered type argument is not comparable. */
  public static class ThreadStringMap extends MySortedMap<Thread, String> {
    public ThreadStringMap() {
      super((MyComparator<? super String>) null);
    }
  }

  /** A comparator for threads, which are not comparable. */
  public static class MyThreadComparator implements MyComparator<Thread> {
    @Override
    public int compare(Thread o1, Thread o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }

  /**
   * An ordered class with a constructor whose comparator orders the unordered type parameter, not
   * the ordered one.
   */
  public static class MismatchedComparatorSet<K, V> extends TreeSet<K> {
    private static final long serialVersionUID = 20261117L;

    public MismatchedComparatorSet(MyComparator<? super V> comparator, int n) {}
  }

  /** An ordered class whose ordered type parameter is bounded by its other type parameter. */
  public static class BoundedTreeSet<K extends V, V> extends TreeSet<K> {
    private static final long serialVersionUID = 20261117L;

    public BoundedTreeSet() {}
  }

  /** A sorted map whose ordered type argument, {@code Thread}, is not comparable. */
  public static class StringThreadMap extends MySortedMap<String, Thread> {
    public StringThreadMap() {
      super((MyComparator<? super Thread>) null);
    }
  }

  /** A sorted map with a constructor that copies another sorted map. */
  public static class CopyingSortedMap<V, K> extends MySortedMap<V, K> {
    public CopyingSortedMap(MySortedMap<V, K> other) {
      super(other);
    }
  }

  /**
   * A comparator interface whose type parameter has a bound, so that its {@code compare} method
   * erases to {@code compare(Number, Number)}.
   */
  public interface NumberOrder<T extends Number> {
    int compare(T o1, T o2);
  }

  /** An order on {@code AtomicInteger}, which is a {@code Number} that is not comparable. */
  public static class AtomicIntegerOrder implements NumberOrder<AtomicInteger> {
    @Override
    public int compare(AtomicInteger o1, AtomicInteger o2) {
      return Integer.compare(o1.get(), o2.get());
    }
  }

  /** An ordered set of numbers whose constructor takes a {@link NumberOrder}. */
  public static class NumberTreeSet<E extends Number> extends TreeSet<E> {
    private static final long serialVersionUID = 20261119L;

    public NumberTreeSet(NumberOrder<E> order) {
      super(order::compare);
    }
  }

  /** A set of threads, which are not comparable. */
  public static class ThreadTreeSet extends TreeSet<Thread> {
    private static final long serialVersionUID = 20261119L;

    public ThreadTreeSet() {
      super(new MyThreadOrder());
    }
  }

  /** A {@code java.util.Comparator} for threads. */
  public static class MyThreadOrder implements java.util.Comparator<Thread> {
    @Override
    public int compare(Thread o1, Thread o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }

  /**
   * An ordered set whose constructor takes a {@code NavigableSet}, which inherits its {@code
   * comparator()} method from {@code SortedSet}.
   */
  public static class CopyingNavigableSet<E> extends TreeSet<E> {
    private static final long serialVersionUID = 20261119L;

    public CopyingNavigableSet(NavigableSet<E> other) {
      // Calls TreeSet(SortedSet), which uses the comparator of `other`.
      super(other);
    }
  }

  /** How many times to instantiate each constructor, since instantiation is random. */
  private static final int TRIALS = 50;

  /**
   * Returns input types that contain some types that implement Comparable and some that do not.
   *
   * @return a set of input types, closed under supertypes
   */
  private static Set<Type> inputTypes() {
    Set<Type> result = new LinkedHashSet<>();
    for (Class<?> c :
        new Class<?>[] {
          String.class, Thread.class, BitSet.class, MyStringComparator.class, ThreadStringMap.class
        }) {
      result.addAll(ClassOrInterfaceType.forClass(c).getSupertypesInclusive());
    }
    return result;
  }

  /**
   * Instantiates the given constructor many times, and checks the type argument at the given index
   * each time.
   *
   * @param constructor a constructor of a generic class
   * @param index the index of the ordered type parameter
   * @param expected the only type that may instantiate the ordered type parameter
   */
  private static void checkOrderedTypeArgument(
      TypedClassOperation constructor, int index, Type expected) {
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes());
    for (int i = 0; i < TRIALS; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(constructor);
      assertNotNull("instantiation of " + constructor, instantiated);
      InstantiatedType outputType = (InstantiatedType) instantiated.getOutputType();
      ReferenceArgument typeArgument = (ReferenceArgument) outputType.getTypeArguments().get(index);
      assertEquals(outputType.toString(), expected, typeArgument.getReferenceType());
    }
  }

  @Test
  public void testTreeSet() throws NoSuchMethodException {
    checkOrderedTypeArgument(
        TypedOperation.forConstructor(TreeSet.class.getConstructor()), 0, JavaTypes.STRING_TYPE);
  }

  @Test
  public void testTreeMap() throws NoSuchMethodException {
    checkOrderedTypeArgument(
        TypedOperation.forConstructor(TreeMap.class.getConstructor()), 0, JavaTypes.STRING_TYPE);
  }

  @Test
  public void testUserDefinedComparator() throws NoSuchMethodException {
    checkOrderedTypeArgument(
        TypedOperation.forConstructor(MyOrderedMap.class.getConstructor()),
        1,
        JavaTypes.STRING_TYPE);
    checkOrderedTypeArgument(
        TypedOperation.forConstructor(MyOrderedMap.class.getConstructor(MyComparator.class)),
        1,
        JavaTypes.STRING_TYPE);
  }

  @Test
  public void testSubclassOfOrderedClass() throws NoSuchMethodException {
    checkOrderedTypeArgument(
        TypedOperation.forConstructor(MyTreeSet.class.getConstructor()), 0, JavaTypes.STRING_TYPE);
  }

  @Test
  public void testSortedTypeOrdersSecondParameter() throws NoSuchMethodException {
    checkOrderedTypeArgument(
        TypedOperation.forConstructor(MySortedMap.class.getConstructor(MySortedMap.class)),
        1,
        JavaTypes.STRING_TYPE);
  }

  /**
   * A comparator argument that orders a type parameter other than the ordered one does not
   * determine the ordered type argument, so the ordered type argument is comparable.
   */
  @Test
  public void testComparatorForOtherTypeParameter() throws NoSuchMethodException {
    Set<Type> inputTypes = inputTypes();
    inputTypes.addAll(
        ClassOrInterfaceType.forClass(MyThreadComparator.class).getSupertypesInclusive());
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation constructor =
        TypedOperation.forConstructor(
            MismatchedComparatorSet.class.getConstructor(MyComparator.class, int.class));
    // Instantiation can fail, as when there is no comparator input type for the type chosen for V.
    boolean someInstantiation = false;
    for (int i = 0; i < TRIALS; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(constructor);
      if (instantiated == null) {
        continue;
      }
      someInstantiation = true;
      InstantiatedType outputType = (InstantiatedType) instantiated.getOutputType();
      ReferenceArgument typeArgument = (ReferenceArgument) outputType.getTypeArguments().get(0);
      assertEquals(outputType.toString(), JavaTypes.STRING_TYPE, typeArgument.getReferenceType());
    }
    assertTrue("no instantiation of " + constructor, someInstantiation);
  }

  /**
   * The instantiation of an ordered class satisfies the bound of its ordered type parameter that
   * mentions another type parameter.
   */
  @Test
  public void testOrderedParameterBoundedByOtherParameter() throws NoSuchMethodException {
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes());
    TypedClassOperation constructor =
        TypedOperation.forConstructor(BoundedTreeSet.class.getConstructor());
    boolean someInstantiation = false;
    for (int i = 0; i < TRIALS; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(constructor);
      if (instantiated == null) {
        continue;
      }
      someInstantiation = true;
      InstantiatedType outputType = (InstantiatedType) instantiated.getOutputType();
      Type k = ((ReferenceArgument) outputType.getTypeArguments().get(0)).getReferenceType();
      Type v = ((ReferenceArgument) outputType.getTypeArguments().get(1)).getReferenceType();
      assertTrue(outputType.toString(), k.isSubtypeOfOrEqualTo(v));
    }
    assertTrue("no instantiation of " + constructor, someInstantiation);
  }

  /**
   * Instantiates the given constructor many times with the given input types, and returns the type
   * arguments of the output type of each successful instantiation.
   *
   * @param constructor a constructor of a generic class
   * @param inputTypes the input types
   * @return the type arguments of the output type of each successful instantiation
   */
  private static List<List<Type>> instantiatedTypeArguments(
      TypedClassOperation constructor, Set<Type> inputTypes) {
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    List<List<Type>> result = new ArrayList<>();
    for (int i = 0; i < TRIALS; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(constructor);
      if (instantiated == null) {
        continue;
      }
      List<Type> typeArguments = new ArrayList<>();
      for (TypeArgument argument :
          ((InstantiatedType) instantiated.getOutputType()).getTypeArguments()) {
        typeArguments.add(((ReferenceArgument) argument).getReferenceType());
      }
      result.add(typeArguments);
    }
    return result;
  }

  /**
   * When an input type for a copy constructor's sorted-map parameter supplies the ordered type
   * argument, it also supplies the other type argument, so that the input type can be passed to the
   * constructor.
   */
  @Test
  public void testCopyConstructorKeepsOtherTypeArgument() throws NoSuchMethodException {
    Set<Type> inputTypes = inputTypes();
    inputTypes.addAll(
        ClassOrInterfaceType.forClass(StringThreadMap.class).getSupertypesInclusive());
    TypedClassOperation constructor =
        TypedOperation.forConstructor(CopyingSortedMap.class.getConstructor(MySortedMap.class));
    ReferenceType threadType = ReferenceType.forClass(Thread.class);
    boolean threadKey = false;
    for (List<Type> typeArguments : instantiatedTypeArguments(constructor, inputTypes)) {
      // Thread is not comparable, so it can be the key type only if StringThreadMap supplied it.
      if (typeArguments.get(1).equals(threadType)) {
        threadKey = true;
        assertEquals(typeArguments.toString(), JavaTypes.STRING_TYPE, typeArguments.get(0));
      }
    }
    assertTrue("no instantiation of " + constructor + " with key type Thread", threadKey);
  }

  /** A comparator interface whose type parameter has a bound orders a type parameter. */
  @Test
  public void testBoundedComparatorInterface() throws NoSuchMethodException {
    Set<Type> inputTypes = inputTypes();
    inputTypes.addAll(
        ClassOrInterfaceType.forClass(AtomicIntegerOrder.class).getSupertypesInclusive());
    TypedClassOperation constructor =
        TypedOperation.forConstructor(NumberTreeSet.class.getConstructor(NumberOrder.class));
    // No input type is a comparable Number, so only the comparator can supply the type argument.
    List<List<Type>> instantiations = instantiatedTypeArguments(constructor, inputTypes);
    assertTrue("no instantiation of " + constructor, !instantiations.isEmpty());
    for (List<Type> typeArguments : instantiations) {
      assertEquals(ReferenceType.forClass(AtomicInteger.class), typeArguments.get(0));
    }
  }

  /**
   * A {@code NavigableSet} parameter orders a type parameter, though {@code NavigableSet} inherits
   * its {@code comparator()} method.
   */
  @Test
  public void testInheritedComparatorMethod() throws NoSuchMethodException {
    Set<Type> inputTypes = inputTypes();
    inputTypes.addAll(ClassOrInterfaceType.forClass(ThreadTreeSet.class).getSupertypesInclusive());
    TypedClassOperation constructor =
        TypedOperation.forConstructor(CopyingNavigableSet.class.getConstructor(NavigableSet.class));
    ReferenceType threadType = ReferenceType.forClass(Thread.class);
    boolean threadElement = false;
    for (List<Type> typeArguments : instantiatedTypeArguments(constructor, inputTypes)) {
      // Thread is not comparable, so it can be the element type only if ThreadTreeSet supplied it.
      if (typeArguments.get(0).equals(threadType)) {
        threadElement = true;
      }
    }
    assertTrue("no instantiation of " + constructor + " with element type Thread", threadElement);
  }

  /** A constructor whose unordered type parameter is already instantiated can be instantiated. */
  @Test
  public void testPartiallyInstantiatedConstructor() throws NoSuchMethodException {
    TypeVariable valueParameter =
        GenericClassType.forClass(TreeMap.class).getTypeParameters().get(1);
    TypedClassOperation constructor =
        TypedOperation.forConstructor(TreeMap.class.getConstructor())
            .substitute(new Substitution(valueParameter, JavaTypes.STRING_TYPE));
    checkOrderedTypeArgument(constructor, 0, JavaTypes.STRING_TYPE);
  }
}
