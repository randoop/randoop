package randoop.operation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import randoop.sequence.Variable;

public class FinalInstanceField extends PublicField {

  private static final long serialVersionUID = 6214863094040724681L;

  public FinalInstanceField(Field field) {
    super(field);
  }

  @Override
  public List<Class<?>> getSetTypes() {
    return new ArrayList<>();
  }

  @Override
  public List<Class<?>> getAccessTypes() {
    List<Class<?>> types = new ArrayList<>();
    types.add(getDeclaringClass());
    return types;
  }

  @Override
  public String toCode(List<Variable> inputVars) {
    return inputVars.get(0) + "." + getName();
  }

}
