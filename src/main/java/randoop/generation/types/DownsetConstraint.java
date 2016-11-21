package randoop.generation.types;

import java.util.List;
import java.util.Map;

import randoop.types.ParameterBound;
import randoop.types.ReferenceBound;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeVariable;

/**
 * Created by bjkeller on 11/2/16.
 */
public class DownsetConstraint implements Propagator {

  private final TypeVariable variable;
  private final ParameterBound upperBound;
  private final List<TypeVariable> parameters;

  public DownsetConstraint(TypeVariable variable, ParameterBound upperBound) {
    this.variable = variable;
    this.upperBound = upperBound;
    this.parameters = upperBound.getTypeParameters();
    this.parameters.add(variable);
  }

  @Override
  public int arity() {
    return parameters.size();
  }

  @Override
  public boolean filter(TypeVariable variable, Map<TypeVariable, TypeDomain> domains) {
    if (variable.equals(this.variable)) {
      TypeDomain domain = domains.get(variable);
      if (arity() == 1) {
        domain = domain.restrictDown(((ReferenceBound) upperBound).getBoundType());
        domains.put(variable, domain);
        return true;
      }
      // check if there exists values in domains of upperBound.getTypeParameters()
      // if not
    } else if (parameters.contains(variable)) {
      //
    }
    return false;
  }

  @Override
  public Propagator apply(Substitution<ReferenceType> substitution) {
    return null;
  }

  @Override
  public List<TypeVariable> getParameters() {
    return parameters;
  }
}
