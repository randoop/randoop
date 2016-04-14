package randoop.types;

import java.lang.reflect.Type;

/**
 * Created by bjkeller on 4/12/16.
 */
public class TypeParameter implements TypeArgument {
  private final Type parameter;
  private final TypeBound typeBound;

  public TypeParameter(Type parameter, TypeBound typeBound) {
    this.parameter = parameter;
    this.typeBound = typeBound;
  }

  public Type getParameter() {
    return parameter;
  }

  @Override
  public TypeBound getBound() {
    return typeBound;
  }

  @Override
  public boolean isGeneric() { return true; }

  @Override
  public String toString() {
    return parameter.toString();
  }
}
