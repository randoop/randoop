package randoop.sequence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import org.junit.Test;
import randoop.condition.RandoopSpecificationError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.TypeInstantiator;

/**
 * Tests that the type instantiator of a {@link SequenceCollection} tracks the collection's types
 * after {@link SequenceCollection#clear}.
 */
public class SequenceCollectionClearTest {

  /** A class that bounds the type variable of {@link #apple}. */
  public static class Apple {
    /** Creates an Apple. */
    public Apple() {}
  }

  /** A class, unrelated to {@link Apple}, that bounds the type variable of {@link #banana}. */
  public static class Banana {
    /** Creates a Banana. */
    public Banana() {}
  }

  /**
   * Returns its argument.
   *
   * @param <T> the type of the argument
   * @param t a value
   * @return {@code t}
   */
  public static <T extends Apple> T apple(T t) {
    return t;
  }

  /**
   * Returns its argument.
   *
   * @param <T> the type of the argument
   * @param t a value
   * @return {@code t}
   */
  public static <T extends Banana> T banana(T t) {
    return t;
  }

  /**
   * Returns a sequence that calls the no-argument constructor of the given class.
   *
   * @param c a class with a public no-argument constructor
   * @return a sequence that constructs an instance of {@code c}
   */
  private static Sequence constructorSequence(Class<?> c)
      throws NoSuchMethodException, RandoopSpecificationError {
    TypedClassOperation constructor = TypedOperation.forConstructor(c.getConstructor());
    return new Sequence().extend(constructor, new ArrayList<Variable>());
  }

  @Test
  public void testInstantiatorAfterClear() throws Exception {
    TypedClassOperation appleOp =
        TypedOperation.forMethod(
            SequenceCollectionClearTest.class.getMethod("apple", Apple.class));
    TypedClassOperation bananaOp =
        TypedOperation.forMethod(
            SequenceCollectionClearTest.class.getMethod("banana", Banana.class));

    SequenceCollection collection = new SequenceCollection();
    collection.add(constructorSequence(Apple.class));
    TypeInstantiator instantiator = collection.getTypeInstantiator();
    assertNotNull(instantiator.instantiate(appleOp));
    assertNull(instantiator.instantiate(bananaOp));

    collection.clear();
    collection.add(constructorSequence(Banana.class));

    // The instantiator obtained before clearing is the collection's current instantiator.
    assertSame(instantiator, collection.getTypeInstantiator());
    assertNull(instantiator.instantiate(appleOp));
    assertNotNull(instantiator.instantiate(bananaOp));
  }
}
