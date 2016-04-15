package randoop.types;

import java.util.List;
import java.util.Objects;

import plume.UtilMDE;

/**
 * Created by bjkeller on 4/13/16.
 */
class GenericParameterizedType extends GenericType {
  private final GenericClassType instantiatedType;
  private final Substitution substitution;
  private final List<TypeArgument> argumentList;

  public GenericParameterizedType(GenericClassType instantiatedType, Substitution substitution, List<TypeArgument> argumentList) {
    this.instantiatedType = instantiatedType;
    this.substitution = substitution;
    this.argumentList = argumentList;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof GenericParameterizedType)) {
      return false;
    }
    GenericParameterizedType t = (GenericParameterizedType)obj;
    if (! instantiatedType.equals(t.instantiatedType)) {
      return false;
    }
    if (argumentList.size() != t.argumentList.size()) {
      return false;
    }
    for (int i = 0; i < argumentList.size(); i++) {
      if (! argumentList.get(i).equals(t.argumentList.get(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(instantiatedType, substitution, argumentList);
  }

  public String toString() {
    return this.getName();
  }

  @Override
  public String getName() {
    return getRuntimeClass().getCanonicalName()
            + "<"
            + UtilMDE.join(argumentList, ",")
            + ">";
  }

  @Override
  public Class<?> getRuntimeClass() { return instantiatedType.getRuntimeClass(); }
}
