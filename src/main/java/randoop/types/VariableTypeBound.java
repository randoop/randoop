package randoop.types;

import java.lang.reflect.TypeVariable;
import java.util.Objects;

/**
 * Created by bjkeller on 4/7/16.
 */
public class VariableTypeBound extends TypeBound {
  private final TypeVariable<?> typeVariable;

  public VariableTypeBound(TypeVariable<?> typeVariable) {
    this.typeVariable = typeVariable;
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
    ConcreteTypeBound b = new ConcreteTypeBound(type);
    return b.isSatisfiedBy(argType, substitution);
  }

  @Override
  public boolean isVariableBound() {
    return true;
  }

  public TypeVariable<?> getTypeVariable() {
    return typeVariable;
  }
}
