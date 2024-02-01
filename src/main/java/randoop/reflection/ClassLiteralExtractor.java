package randoop.reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import randoop.generation.SequenceInfo;
import randoop.generation.test.ClassEnum;
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

  /**
   * Creates a visitor that adds discovered literals to the given map.
   *
   * @param literalMap the map from types to sequences
   */
  ClassLiteralExtractor(MultiMap<ClassOrInterfaceType, Sequence> literalMap) {
    this.literalMap = literalMap;
  }

  /**
   * Creates a visitor that adds discovered literals to the given map and sequence information to
   * the given maps. This is only used when constant mining is enabled.
   *
   * @param literalMap the map from types to sequences
   * @param sequenceInfoMap the map from sequences to sequence information
   * @param packageClassCount the map from packages to the number of classes visited in the package
   */
  ClassLiteralExtractor(
      MultiMap<ClassOrInterfaceType, Sequence> literalMap,
      Map<Sequence, SequenceInfo> sequenceInfoMap,
      Map<Package, Integer> packageClassCount) {
    this.literalMap = literalMap;
    this.sequenceInfoMap = sequenceInfoMap;
    this.packageClassCount = packageClassCount;
  }

  /**
   * {@inheritDoc}
   *
   * <p>For each class, this adds a sequence that creates a value of the class type to the literal
   * map.
   *
   * <p>If constant mining is enabled, this also records the sequence information(frequency,
   * occurrence).
   */
  @Override
  public void visitBefore(Class<?> c) {
    // Record the visited sequences if constant mining is enabled to avoid adding duplicate
    // sequences in the same class.
    HashSet<Sequence> occurredSequences = new HashSet<>();
    ClassOrInterfaceType constantType = ClassOrInterfaceType.forClass(c);
    ClassFileConstants.ConstantSet constantSet = ClassFileConstants.getConstants(c.getName());
    Set<NonreceiverTerm> nonreceiverTerms =
        ClassFileConstants.constantSetToNonreceiverTerms(constantSet);
    for (NonreceiverTerm term : nonreceiverTerms) {
      Sequence seq =
          new Sequence()
              .extend(
                  TypedOperation.createNonreceiverInitialization(term), new ArrayList<Variable>(0));
      literalMap.add(constantType, seq);
      // Remove if true
      if (GenInputsAbstract.constant_mining) {
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
    // Avoid adding unnecessary SequenceInfo objects such as self classes but never used.
    if (frequency == 0) {
      return;
    }
    Package pkg = type.getPackage();
    SequenceInfo si = sequenceInfoMap.computeIfAbsent(seq, __ -> new SequenceInfo());
    si.update(type, pkg, hasOccurred, frequency);
  }

  // TODO: delete this
  public static void main(String[] args) {
    MultiMap<ClassOrInterfaceType, Sequence> literalMap = new MultiMap<>();
    Map<Sequence, SequenceInfo> sequenceInfoMap = new HashMap<>();
    ClassLiteralExtractor cle =
        new ClassLiteralExtractor(literalMap, sequenceInfoMap, new HashMap<>());
    System.out.println("randoop.generation.test.ClassEnum");
    cle.visitBefore(ClassEnum.class);
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
    //    System.out.println("randoop.generation.test2.ClassOne");
    //    cle.visitBefore(randoop.generation.test2.ClassOne.class);
    //    System.out.println(literalMap);
    //    System.out.println(sequenceInfoMap);
  }
}
