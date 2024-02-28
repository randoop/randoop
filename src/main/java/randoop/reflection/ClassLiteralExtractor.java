package randoop.reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import randoop.generation.SequenceInfo;
import randoop.generation.test.ClassEnum;
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
  /** Map a literal sequences corresponding to each class under test. */
  private MultiMap<ClassOrInterfaceType, Sequence> literalMap;

  /**
   * Maps a literal sequence to information about the sequence, including frequency and number of
   * occurrence for each literal level. // TODO: This should be changed after reconstruction
   */
  private Map<Sequence, SequenceInfo> sequenceInfoMap;

  /** Record the number of classes in a package have been visited. */
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
      System.out.println("literalMap: " + literalMap);
      if (GenInputsAbstract.constant_mining) {
        updateSequenceInfo(
            seq,
            constantType,
            occurredSequences.contains(seq),
            constantSet.getConstantFrequency(term.getValue()));
        occurredSequences.add(seq);
      }
    }
    if (GenInputsAbstract.constant_mining) {
      // Record the class count for each package.
      Package pkg = constantType.getPackage();
      packageClassCount.put(pkg, packageClassCount.getOrDefault(pkg, 0) + 1);
    }
  }

  /**
   * If there is an existing SequenceInfo in the map, this side-effects it. Otherwise, this installs
   * a new SequenceInfo into the map.
   */
  private void updateSequenceInfo(
      Sequence seq, ClassOrInterfaceType type, Boolean hasOccurred, int frequency) {
    // Avoid adding unnecessary SequenceInfo objects when the extractor is visiting the current
    // class and
    // this class is never used in the software under test.
    if (frequency == 0) {
      return;
    }
    Package pkg = type.getPackage();
    SequenceInfo si = sequenceInfoMap.computeIfAbsent(seq, __ -> new SequenceInfo());
    si.update(type, pkg, hasOccurred, frequency);
  }

  // TODO: delete this or change it to toString()
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
