package randoop.types;

import java.lang.reflect.TypeVariable;
import java.util.Objects;

/**
 * Created by bjkeller on 4/7/16.
 */
public class VariableTypeBound extends TypeBound {
  private final TypeVariable<?> typeVariable;
  private final TypeOrdering typeOrdering;

  public VariableTypeBound(TypeVariable<?> typeVariable, TypeOrdering typeOrdering) {
    this.typeVariable = typeVariable;
    this.typeOrdering = typeOrdering;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof VariableTypeBound)) {
      return false;
    }
    VariableTypeBound var = (VariableTypeBound)obj;
    return this.typeVariable.equals(var.typeVariable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeVariable);
  }

  @Override
  public String toString() {
    return typeVariable.toString();
  }

  @Override
  public boolean isSatisfiedBy(ConcreteType argType, Substitution substitution) throws RandoopTypeException {
    ConcreteType type = substitution.get(typeVariable);
    if (type == null) {
      throw new RandoopTypeException("unable to instantiate bound " + typeVariable);
    }
    ConcreteTypeBound b = new ConcreteTypeBound(type, typeOrdering);
    return b.isSatisfiedBy(argType, substitution);
  }

  public TypeBound apply(Substitution substitution) {
    ConcreteType type = substitution.get(typeVariable);
    if (type == null) {
      return this;
    }
    return new ConcreteTypeBound(type, typeOrdering);
  }
}
