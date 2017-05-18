package randoop.generation.types;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import randoop.types.JavaTypes;
import randoop.types.ReferenceType;
import randoop.types.Substitution;

/** Created by bjkeller on 12/14/16. */
public class DomainTestUtilities {
  static ReferenceType makeComparableType(ReferenceType type) {
    Substitution<ReferenceType> substitution =
        Substitution.forArgs(JavaTypes.COMPARABLE_TYPE.getTypeParameters(), type);
    return JavaTypes.COMPARABLE_TYPE.apply(substitution);
  }

  static Set<ReferenceType> makeSet(ReferenceType... types) {
    Set<ReferenceType> set = new HashSet<>();
    Collections.addAll(set, types);
    return set;
  }
}
