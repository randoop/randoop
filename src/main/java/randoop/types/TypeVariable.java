package randoop.types;

/**
 * Created by bjkeller on 4/18/16.
 */
public class TypeVariable extends ReferenceType {

  private final java.lang.reflect.TypeVariable<?> variable;

  private final ParameterBound bound;

  public TypeVariable(java.lang.reflect.TypeVariable<?> variable, ParameterBound bound) {
    this.variable = variable;
    this.bound = bound;
  }

  @Override
  public GeneralType apply(Substitution substitution) throws RandoopTypeException {
    return substitution.get(variable);
  }

}
