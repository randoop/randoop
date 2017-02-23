package randoop.reflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

  private Map<Sequence, Integer> tfFrequency;

  ClassLiteralExtractor(
      MultiMap<ClassOrInterfaceType, Sequence> literalMap, Map<Sequence, Integer> tfFrequency) {
    this.literalMap = literalMap;
    this.tfFrequency = tfFrequency;
  }

  // So I have no idea if we will have sequence equality here, leading to capturing
  // TODO This may guarantee that constants are not repeated through class file constants,
  // fix if needed
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
        if (GenInputsAbstract.constant_mining) {
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
