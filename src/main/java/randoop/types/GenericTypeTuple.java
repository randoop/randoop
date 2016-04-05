package randoop.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import randoop.BugInRandoopException;
import randoop.operation.GenericOperation;

/**
 * {@code GenericTypeTuple} represents a tuple of generic types,
 * primarily as the input to a {@link GenericOperation GenericOperation}.
 */
public class GenericTypeTuple implements GeneralTypeTuple {

  /** The ordered typeList of {@link randoop.types.GenericType GenericType} objects */
  private final ArrayList<GeneralType> typeList;

  /**
   * Create a tuple of {@link GenericType} objects.
   *
   * @param typeList  the list of {@link GenericType} objects
   */
  public GenericTypeTuple(List<GeneralType> typeList) {
    this.typeList = new ArrayList<>(typeList);
  }

  /**
   * Create an empty tuple.
   */
  public GenericTypeTuple() {
    this.typeList = new ArrayList<>();
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof GenericTypeTuple)) {
      return false;
    }
    GenericTypeTuple tuple = (GenericTypeTuple)obj;
    return typeList.equals(tuple.typeList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeList);
  }

  /**
   * Return the number of components in this tuple.
   *
   *
   * @return the number of components in this tuple.
   */
  @Override
  public int size() {
    return typeList.size();
  }

  /**
   * Indicate whether the tuple has any components.
   *
   * @return true if the tuple has no components, false otherwise
   */
  @Override
  public boolean isEmpty() {
    return typeList.isEmpty();
  }

  @Override
  public boolean isGeneric() {
    for (GeneralType type : typeList) {
      if (type.isGeneric()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public GeneralTypeTuple apply(Substitution substitution) {
    List<GeneralType> generalTypes = new ArrayList<>();
    for (GeneralType generalType : typeList) {
      generalTypes.add(generalType.apply(substitution));
    }
    return new GenericTypeTuple(generalTypes);
  }

  /**
   * Return the ith component of this tuple.
   *
   * @param i  the component index
   * @return the component at the ith index
   */
  @Override
  public GeneralType get(int i) {
    assert 0 <= i && i < typeList.size();
    return typeList.get(i);
  }

  /**
   * Instantiates this {@code GenericTypeTuple} to a {@link ConcreteTypeTuple}
   * using the given type substitution.
   *
   * @param substitution  the type substitution
   * @return the concrete type tuple formed by applying the substitution componentwise to this tuple
   */
  public ConcreteTypeTuple instantiate(Substitution substitution) {
    return ((GenericTypeTuple)this.apply(substitution)).makeConcrete();
  }

  public ConcreteTypeTuple makeConcrete() {
    List<ConcreteType> concreteTypes = new ArrayList<>();
    for (GeneralType generalType : typeList) {
      if (generalType.isGeneric()) {
        String msg = "attempt to force generic type to concrete type: " + generalType;
        throw new BugInRandoopException(msg);
      }
      concreteTypes.add((ConcreteType)generalType);
    }
    return new ConcreteTypeTuple(concreteTypes);
  }
}
