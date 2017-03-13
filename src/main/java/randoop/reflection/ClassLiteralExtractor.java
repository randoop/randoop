package randoop.reflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import randoop.main.GenInputsAbstract;
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.util.ClassFileConstants;
import randoop.util.MultiMap;

/**
 * {@code ClassLiteralExtractor} is a {@link ClassVisitor} that extracts literals from the bytecode
 * of each class visited, adding a sequence for each to a map associating a sequence with a type.
 */
class ClassLiteralExtractor extends DefaultClassVisitor {

  private MultiMap<ClassOrInterfaceType, Sequence> literalMap;

  /**
   * The term frequency mapping is used for the weighted constants option. This will keep track of
   * how often a constant appears across all constants.
   */
  private Map<Sequence, Integer> tfFrequency;

  ClassLiteralExtractor(
      MultiMap<ClassOrInterfaceType, Sequence> literalMap, Map<Sequence, Integer> tfFrequency) {
    this.literalMap = literalMap;
    this.tfFrequency = tfFrequency;
  }

  @Override
  public void visitBefore(Class<?> c) {
    Collection<ClassFileConstants.ConstantSet> constList = new ArrayList<>();
    constList.add(ClassFileConstants.getConstants(c.getName()));
    MultiMap<Class<?>, NonreceiverTerm> constantMap = ClassFileConstants.toMap(constList);
    for (Class<?> constantClass : constantMap.keySet()) {
      ClassOrInterfaceType constantType = ClassOrInterfaceType.forClass(constantClass);
      for (NonreceiverTerm term : constantMap.getValues(constantClass)) {
        Sequence seq =
            new Sequence()
                .extend(
                    TypedOperation.createNonreceiverInitialization(term),
                    new ArrayList<Variable>());
        literalMap.add(constantType, seq);
        if (GenInputsAbstract.weighted_constants) {
          if (tfFrequency.containsKey(seq)) {
            tfFrequency.put(seq, tfFrequency.get(seq) + 1);
          } else {
            tfFrequency.put(seq, 1);
          }
        }
      }
    }
  }
}
