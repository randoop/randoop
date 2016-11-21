package randoop.generation.types;

import java.util.List;
import java.util.Map;

import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeVariable;

/**
 * Created by bjkeller on 11/2/16.
 */
public interface Propagator {
  int arity();

  boolean filter(TypeVariable variable, Map<TypeVariable, TypeDomain> domains);

  Propagator apply(Substitution<ReferenceType> substitution);

  List<TypeVariable> getParameters();
}
