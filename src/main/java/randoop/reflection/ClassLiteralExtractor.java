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

  /* Maps a sequence to information about the sequence. */
  private static Map<Sequence, SequenceInfo> sequenceInfoMap = new HashMap<>();

  /* The number of classes visited. */
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
    HashSet<Sequence> occurredSequences = new HashSet<>();
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
        updateSequenceInfo(
            seq,
            constantType,
            occurredSequences.contains(seq),
            constantSet.constantFrequency.get(term.getValue()));
        occurredSequences.add(seq);
      }
    }
  }

  private void updateSequenceInfo(
      Sequence seq, ClassOrInterfaceType type, Boolean hasOccurred, int frequency) {
    Package pkg = type.getPackage();
    SequenceInfo si = sequenceInfoMap.getOrDefault(seq, new SequenceInfo());
    si.update(seq, type, pkg, hasOccurred, frequency);
    sequenceInfoMap.put(seq, si);
  }

  static class SequenceInfo {
    public int globalFrequency;
    public int globalOccurrence;

    /* How many times the sequence occur by the class */
    public Map<ClassOrInterfaceType, Integer> classFrequency;

    /* How many times the sequence occur by the package */
    public Map<Package, Integer> packageFrequency;

    /* How many classes the sequence occur by the package */
    public Map<Package, Integer> packageOccurrence;

    public SequenceInfo() {
      globalFrequency = 0;
      globalOccurrence = 0;
      classFrequency = new HashMap<>();
      packageFrequency = new HashMap<>();
      packageOccurrence = new HashMap<>();
    }

    public void update(
        Sequence seq, ClassOrInterfaceType type, Package pkg, boolean hasOccurred, int frequency) {
      globalFrequency += frequency;
      classFrequency.put(type, classFrequency.getOrDefault(type, 0) + frequency);
      packageFrequency.put(pkg, packageFrequency.getOrDefault(pkg, 0) + frequency);
      if (!hasOccurred) {
        globalOccurrence++;
        packageOccurrence.put(pkg, packageOccurrence.getOrDefault(pkg, 0) + 1);
      }
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("globalFrequency: ").append(globalFrequency).append("\n");
      sb.append("globalOccurrence: ").append(globalOccurrence).append("\n");
      sb.append("classFrequency: ").append(classFrequency).append("\n");
      sb.append("packageFrequency: ").append(packageFrequency).append("\n");
      sb.append("packageOccurrence: ").append(packageOccurrence).append("\n");
      return sb.toString();
    }
  }
}
