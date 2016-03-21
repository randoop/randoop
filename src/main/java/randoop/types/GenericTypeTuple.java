package randoop.types;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code GenericTypeTuple} represents a tuple of generic types,
 * primarily as the input to a {@link randoop.operation.GenericOperation GenericOperation}.
 */
public class GenericTypeTuple implements GeneralTypeTuple {

  /** The ordered list of {@link randoop.types.GenericType GenericType} objects */
  private final ArrayList<GenericType> list;

  /**
   * Create a tuple of {@link GenericType} objects.
   *
   * @param list  the list of {@link GenericType} objects
   */
  public GenericTypeTuple(List<GenericType> list) {
    this.list = new ArrayList<>(list);
  }

  /**
   * Create an empty tuple.
   */
  public GenericTypeTuple() {
    this.list = new ArrayList<>();
  }

  /**
   * Return the number of components in this tuple.
   *
   * @return the number of components in this tuple.
   */
  @Override
  public int size() {
    return list.size();
  }

  /**
   * Indicate whether the tuple has any components.
   *
   * @return true if the tuple has no components, false otherwise
   */
  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  /**
   * Return the ith component of this tuple.
   *
   * @param i  the component index
   * @return the component at the ith index
   */
  @Override
  public GenericType get(int i) {
    assert 0 <= i && i < list.size();
    return list.get(i);
  }

  /**
   * Instantiates this {@code GenericTypeTuple} to a {@link ConcreteTypeTuple}
   * using the given type substitution.
   *
   * @param substitution  the type substitution
   * @return the concrete type tuple formed by applying the substitution componentwise to this tuple
   */
  public ConcreteTypeTuple instantiate(Substitution substitution) {
    List<ConcreteType> concreteTypes = new ArrayList<>();
    for (GenericType genericType : list) {
      concreteTypes.add(genericType.instantiate(substitution));
    }
    return new ConcreteTypeTuple(concreteTypes);
  }
}
