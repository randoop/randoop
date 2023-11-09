package randoop.reflection;

import java.util.ArrayList;
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
  private int classCount = 0;

  ClassLiteralExtractor(MultiMap<ClassOrInterfaceType, Sequence> literalMap) {
    this.literalMap = literalMap;
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
    Package pkg = type.getPackage();
    SequenceInfo si = sequenceInfoMap.getOrDefault(seq, new SequenceInfo());
    si.update(seq, type, pkg, hasOccurred, frequency);
    sequenceInfoMap.put(seq, si);
  }

  static class SequenceInfo {
    /**
     * The number of times this sequence occurs, in any class. Only used when the literal level is
     * CLASS.
     */
    public int globalFrequency;

    /**
     * The number of classes in which this sequence occurs. Only used when the literal level is
     * CLASS.
     */
    public int globalOccurrence;

    /* How many times the sequence occurs in the class. */
    public Map<ClassOrInterfaceType, Integer> classFrequency;

    /* How many times the sequence occurs in the package. */
    public Map<Package, Integer> packageFrequency;

    /* How many classes the sequence occurs in the package. */
    public Map<Package, Integer> packageOccurrence;

    public SequenceInfo() {
      globalFrequency = 0;
      globalOccurrence = 0;
      classFrequency = new HashMap<>();
      packageFrequency = new HashMap<>();
      packageOccurrence = new HashMap<>();
    }

    /**
     * Update data structures to account for the fact that {@code seq} has been observed {@code
     * frequency} times in class {@code type}.
     *
     * @param hasOccurredInClass true if this is the second or subsequent occurrence of {@code seq}
     *     in the current class
     */
    public void update(
        Sequence seq,
        ClassOrInterfaceType type,
        Package pkg,
        boolean hasOccurredInClass,
        int frequency) {
      globalFrequency += frequency;
      classFrequency.put(type, classFrequency.getOrDefault(type, 0) + frequency);
      packageFrequency.put(pkg, packageFrequency.getOrDefault(pkg, 0) + frequency);
      if (!hasOccurredInClass) {
        globalOccurrence++;
        packageOccurrence.put(pkg, packageOccurrence.getOrDefault(pkg, 0) + 1);
      }
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("SequenceInfo: ");
      sb.append("globalFrequency: ").append(globalFrequency).append(System.lineSeparator());
      sb.append("globalOccurrence: ").append(globalOccurrence).append(System.lineSeparator());
      sb.append("classFrequency: ").append(classFrequency).append(System.lineSeparator());
      sb.append("packageFrequency: ").append(packageFrequency).append(System.lineSeparator());
      sb.append("packageOccurrence: ").append(packageOccurrence).append(System.lineSeparator());
      return sb.toString();
    }
  }
}
