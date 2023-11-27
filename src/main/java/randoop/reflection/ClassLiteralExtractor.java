package randoop.reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import randoop.generation.SequenceInfo;
import randoop.generation.test.ClassOne;
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
 *
 * @see OperationModel
 */
class ClassLiteralExtractor extends DefaultClassVisitor {
  /** Maps a type to a sequence. */
  private MultiMap<ClassOrInterfaceType, Sequence> literalMap;

  /** Maps a sequence to information about the sequence. */
  private Map<Sequence, SequenceInfo> sequenceInfoMap;

  /** Record how many classes in a package have been visited. */
  private Map<Package, Integer> packageClassCount;

  /** The number of classes visited. */
  private Integer classCount; // TODO: Deprecated unless we can wrap it for passing it as reference

  /**
   * Creates a visitor that adds discovered literals to the given map.
   *
   * @param literalMap the map from types to sequences
   */
  ClassLiteralExtractor(MultiMap<ClassOrInterfaceType, Sequence> literalMap) {
    this.literalMap = literalMap;
    classCount = 0;
  }

  /**
   * Creates a visitor that adds discovered literals to the given map and sequence information to
   * the given maps. This is only used when constant mining is enabled.
   *
   * @param literalMap the map from types to sequences
   * @param sequenceInfoMap the map from sequences to sequence information
   * @param packageClassCount the map from packages to the number of classes visited in the package
   * @param classCount the number of classes visited
   */
  ClassLiteralExtractor(
      MultiMap<ClassOrInterfaceType, Sequence> literalMap,
      Map<Sequence, SequenceInfo> sequenceInfoMap,
      Map<Package, Integer> packageClassCount,
      Integer classCount) {
    this.literalMap = literalMap;
    this.sequenceInfoMap = sequenceInfoMap;
    this.packageClassCount = packageClassCount;
    this.classCount = classCount;
  }

  /**
   * {@inheritDoc}
   *
   * <p>For each class, this adds a sequence that creates a value of the class type to the literal
   * map.
   *
   * <p>If constant mining is enabled, this also records the sequence information.
   */
  @Override
  public void visitBefore(Class<?> c) {
    classCount++;
    // Record the visited sequences if constant mining is enabled.
    HashSet<Sequence> occurredSequences = new HashSet<>();
    ClassOrInterfaceType constantType = ClassOrInterfaceType.forClass(c);
    ClassFileConstants.ConstantSet constantSet = ClassFileConstants.getConstants(c.getName());
    Set<NonreceiverTerm> nonreceiverTerms = ClassFileConstants.toNonreceiverTerms(constantSet);
    for (NonreceiverTerm term : nonreceiverTerms) {
      Sequence seq =
          new Sequence()
              .extend(
                  TypedOperation.createNonreceiverInitialization(term), new ArrayList<Variable>(0));
      literalMap.add(constantType, seq);
      if (GenInputsAbstract.constant_mining) {
        // Record the sequence information.
        updateSequenceInfo(
            seq,
            constantType,
            occurredSequences.contains(seq),
            constantSet.getConstantFrequency(term.getValue()));
        Package pkg = constantType.getPackage();
        packageClassCount.put(pkg, packageClassCount.getOrDefault(pkg, 0) + 1);
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
    si.update(type, pkg, hasOccurred, frequency);
    sequenceInfoMap.put(seq, si);
  }

  // TODO: delete this
  public static void main(String[] args) {
    MultiMap<ClassOrInterfaceType, Sequence> literalMap = new MultiMap<>();
    Map<Sequence, SequenceInfo> sequenceInfoMap = new HashMap<>();
    ClassLiteralExtractor cle =
        new ClassLiteralExtractor(literalMap, sequenceInfoMap, new HashMap<>(), 0);
    System.out.println("randoop.generation.test.ClassOne");
    cle.visitBefore(ClassOne.class);
    System.out.println(literalMap);
    System.out.println(sequenceInfoMap);
    //    literalMap.clear();
    //    sequenceInfoMap.clear();
    //    System.out.println("randoop.generation.test.ClassThree");
    //    cle.visitBefore(ClassThree.class);
    //    System.out.println(literalMap);
    //    System.out.println(sequenceInfoMap);
    //    literalMap.clear();
    //    sequenceInfoMap.clear();
    System.out.println("randoop.generation.test2.ClassOne");
    cle.visitBefore(randoop.generation.test2.ClassOne.class);
    System.out.println(literalMap);
    System.out.println(sequenceInfoMap);
    System.out.println(cle.classCount);
  }
}
