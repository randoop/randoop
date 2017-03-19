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
 * of each class visited, adding a sequenceTermFrequency for each to a map associating a
 * sequenceTermFrequency with a type.
 */
class ClassLiteralExtractor extends DefaultClassVisitor {

  private MultiMap<ClassOrInterfaceType, Sequence> literalMap;

  /**
   * The map of sequences to their term frequency: tf(t,d), where t is a sequence and d is all
   * classes under test. Note that this is the raw frequency, just the number of times they occur
   * within all classes under test.
   */
  private Map<Sequence, Integer> sequenceTermFrequency;

  ClassLiteralExtractor(
      MultiMap<ClassOrInterfaceType, Sequence> literalMap,
      Map<Sequence, Integer> sequenceTermFrequency) {
    this.literalMap = literalMap;
    this.sequenceTermFrequency = sequenceTermFrequency;
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
          if (sequenceTermFrequency.containsKey(seq)) {
            sequenceTermFrequency.put(seq, sequenceTermFrequency.get(seq) + 1);
          } else {
            sequenceTermFrequency.put(seq, 1);
          }
        }
      }
    }
  }
}
