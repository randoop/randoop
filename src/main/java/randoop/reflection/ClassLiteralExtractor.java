package randoop.reflection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import randoop.generation.SequenceInfo;
import randoop.generation.test.ClassOne;
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

  /* Maps a sequence to information about the sequence. */
  private Map<Sequence, SequenceInfo> sequenceInfoMap;

  /* The number of classes visited. */
  private int classCount = 0;

  ClassLiteralExtractor(MultiMap<ClassOrInterfaceType, Sequence> literalMap) {
    this.literalMap = literalMap;
    classCount = 0;
  }

  ClassLiteralExtractor(MultiMap<ClassOrInterfaceType, Sequence> literalMap, Map<Sequence, SequenceInfo> sequenceInfoMap) {
    this.literalMap = literalMap;
    this.sequenceInfoMap = sequenceInfoMap;
    classCount = 0;
  }

  @Override
  public void visitBefore(Class<?> c) {
    MultiMap<Class<?>, NonreceiverTerm> constantMap = new MultiMap<>();
    ClassFileConstants.ConstantSet constantSet = ClassFileConstants.getConstants(c.getName());
    ClassFileConstants.addToConstantMap(constantSet, constantMap);
    HashSet<Sequence> occurredSequences = new HashSet<>();
    for (Class<?> constantClass : constantMap.keySet()) {
      ClassOrInterfaceType constantType = ClassOrInterfaceType.forClass(constantClass);
      classCount++;
      // TODO: delete this
      System.out.print(classCount);
      for (NonreceiverTerm term : constantMap.getValues(constantClass)) {
        Sequence seq =
            new Sequence()
                .extend(
                    TypedOperation.createNonreceiverInitialization(term),
                    new ArrayList<Variable>(0));
        literalMap.add(constantType, seq);
        updateSequenceInfo(
            seq,
            constantType,
            occurredSequences.contains(seq),
            constantSet.constantFrequency.get(term.getValue()));
        occurredSequences.add(seq);
      }
    }
  }

  /**
   * If there is an existing SequenceInfo in the map, this side-effects it. Otherwise, this installs
   * a new SequenceInfo into the map.
   */
  private void updateSequenceInfo(
      Sequence seq, ClassOrInterfaceType type, Boolean hasOccurred, int frequency) {
    System.out.println("updateSequenceInfo: " + seq + " " + type + " " + hasOccurred + " " + frequency);
    Package pkg = type.getPackage();
    System.out.println("pkg: " + pkg);
    SequenceInfo si = sequenceInfoMap.getOrDefault(seq, new SequenceInfo());
    System.out.printf("si: %s\n", si);
    si.update(type, pkg, hasOccurred, frequency);
    sequenceInfoMap.put(seq, si);
  }

  public static void main(String[] args) {
    MultiMap<ClassOrInterfaceType, Sequence> literalMap = new MultiMap<>();
    Map<Sequence, SequenceInfo> sequenceInfoMap = new HashMap<>();
    ClassLiteralExtractor cle = new ClassLiteralExtractor(literalMap, sequenceInfoMap);
    cle.visitBefore(ClassOne.class);
    System.out.println(literalMap);
    System.out.println(sequenceInfoMap);
  }
}
