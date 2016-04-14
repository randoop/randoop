package randoop.types;

/**
 * Created by bjkeller on 4/11/16.
 */
public interface TypeOrdering {
  boolean isLessThanOrEqualTo(ConcreteType t1, ConcreteType t2);
}
