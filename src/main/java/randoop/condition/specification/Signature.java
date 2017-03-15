package randoop.condition.specification;

import java.util.ArrayList;
import java.util.List;

/** Created by bjkeller on 3/14/17. */
public class Signature {
  private final List<Parameter> parameterList;

  public Signature() {
    this.parameterList = new ArrayList<>();
  }

  public Signature(List<Parameter> parameterList) {
    this.parameterList = parameterList;
  }

  public static Signature getSignature(Class<?>[] parameterTypes, List<String> parameterNames) {
    assert parameterNames.size() == parameterTypes.length
        : "number of parameter names and types must match";
    List<Parameter> parameterList = new ArrayList<>();
    for (int i = 0; i < parameterNames.size(); i++) {
      parameterList.add(new Parameter(parameterTypes[i].getCanonicalName(), parameterNames.get(i)));
    }
    return new Signature(parameterList);
  }
}
