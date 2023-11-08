package randoop.reflection;

import java.util.*;
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
 *
 * @see OperationModel
 */
class ClassLiteralExtractor extends DefaultClassVisitor {

  private MultiMap<ClassOrInterfaceType, Sequence> literalMap;

  private static Map<Sequence, Integer> sequenceFrequency = new HashMap<>();

  private int classCount;

  ClassLiteralExtractor(MultiMap<ClassOrInterfaceType, Sequence> literalMap) {
    this.literalMap = literalMap;
    classCount = 0;
  }

  @Override
  public void visitBefore(Class<?> c) {
    MultiMap<Class<?>, NonreceiverTerm> constantMap = new MultiMap<>();
    ClassFileConstants.ConstantSet constantSet = ClassFileConstants.getConstants(c.getName());
    ClassFileConstants.buildConstantMap(constantSet, constantMap);
    for (Class<?> constantClass : constantMap.keySet()) {
      ClassOrInterfaceType constantType = ClassOrInterfaceType.forClass(constantClass);
      classCount++;
      System.out.print(classCount);
      for (NonreceiverTerm term : constantMap.getValues(constantClass)) {
        Sequence seq =
            new Sequence()
                .extend(
                    TypedOperation.createNonreceiverInitialization(term),
                    new ArrayList<Variable>(0));
        literalMap.add(constantType, seq);
        // Update the global frequency for this sequence
        sequenceFrequency.put(
            seq,
            sequenceFrequency.getOrDefault(seq, 0)
                + constantSet.constantFrequency.get(term.getValue()));
      }
    }
  }
}
