package randoop.generation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.main.RandoopBug;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.TypeInstantiator;
import randoop.sequence.ClassLiterals;
import randoop.sequence.PackageLiterals;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceCollection;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.PrimitiveType;
import randoop.types.Type;
import randoop.util.ListOfLists;
import randoop.util.Log;
import randoop.util.SimpleList;

/**
 * Stores the component sequences generated during a run of Randoop. "Component sequences" are
 * sequences that Randoop uses to create larger sequences. The collection of sequences is also
 * called Randoop's "pool".
 *
 * <p>This class manages different collections of component sequences:
 *
 * <ul>
 *   <li>General components that can be used as input to any method in any class.
 *   <li>Class literals: components representing literal values that apply only to a specific class
 *       and should not be used as inputs to other classes.
 *   <li>Package literals: analogous to class literals but at the package level.
 * </ul>
 *
 * <p>SEED SEQUENCES. Seed sequences are the initial sequences provided to the generation process.
 * They include (1) sequences passed via the constructor, (2) class literals, and (3) package
 * literals. The only different treatment of seed sequences is during calls to the
 * clearGeneratedSequences() method, which removes only general, non-seed components from the
 * collection.
 */
public class ComponentManager {

  /**
   * The principal set of sequences used to create other, larger sequences by the generator. Is
   * never null. Contains both general components and seed sequences. Can be reset by calling {@link
   * #clearGeneratedSequences}.
   */
  // "gral" probably stands for "general".
  private SequenceCollection gralComponents;

  /**
   * The sequences that were given pre-generation to the component manager (via its constructor).
   * (Does not include literals, I think?)
   *
   * <p>Seeds are all contained in {@link #gralComponents}. This list is kept to restore seeds if
   * the user calls {@link #clearGeneratedSequences}.
   */
  private final Collection<Sequence> gralSeeds;

  // TODO: add comment
  private Map<Sequence, Integer> constantFrequencyMap;

  // TODO: add comment
  private Map<Sequence, Integer> constantOccurrenceMap;

  private int classCount;

  /**
   * Components representing literals that should only be used as input to specific classes.
   *
   * <p>Null if class literals are not used or none were found. At most one of classLiterals and
   * packageliterals is non-null.
   */
  private ClassLiterals classLiterals = null;

  /**
   * A set of additional components representing literals that should only be used as input to
   * specific packages.
   *
   * <p>Null if package literals are not used or none were found. At most one of classLiterals and
   * packageliterals is non-null.
   */
  private @Nullable PackageLiterals packageLiterals = null;

  /** Create an empty component manager, with an empty seed sequence set. */
  public ComponentManager() {
    gralComponents = new SequenceCollection();
    gralSeeds = Collections.unmodifiableSet(Collections.<Sequence>emptySet());
  }

  /**
   * Create a component manager, initially populated with the given sequences, which are considered
   * seed sequences.
   *
   * @param generalSeeds seed sequences. Can be null, in which case the seed sequences set is
   *     considered empty.
   */
  public ComponentManager(Collection<Sequence> generalSeeds) {
    Set<Sequence> seedSet = new LinkedHashSet<>(generalSeeds.size());
    seedSet.addAll(generalSeeds);
    this.gralSeeds = Collections.unmodifiableSet(seedSet);
    gralComponents = new SequenceCollection(seedSet);
  }

  /**
   * Returns the number of (non-seed) sequences stored by the manager.
   *
   * @return count of generated sequences in this {@link ComponentManager}
   */
  // FIXME subtract size of seeds!
  public int numGeneratedSequences() {
    return gralComponents.size();
  }

  public int getClassCount() {
    return classCount;
  }

  public void setClassCount(int classCount) {
    this.classCount = classCount;
  }

  /**
   * Add a sequence representing a literal value that can be used when testing members of the given
   * class.
   *
   * @param type the class literal to add for the sequence
   * @param seq the sequence
   */
  public void addClassLevelLiteral(ClassOrInterfaceType type, Sequence seq) {
    if (classLiterals == null) {
      classLiterals = new ClassLiterals();
    }
    classLiterals.addSequence(type, seq);
  }

  // TODO: add comment
  public void addClassLevelLiteralInfo(ClassOrInterfaceType type, Sequence seq, int frequency) {
    assert classLiterals != null;
    classLiterals.addSequenceFrequency(type, seq, frequency);
  }

  /**
   * Add a sequence representing a literal value that can be used when testing classes in the given
   * package.
   *
   * @param pkg the package to add for the sequence
   * @param seq the sequence
   */
  public void addPackageLevelLiteral(Package pkg, Sequence seq) {
    if (packageLiterals == null) {
      packageLiterals = new PackageLiterals();
    }
    packageLiterals.addSequence(pkg, seq);
  }

  // TODO: add comment
  public void addPackageLevelLiteralInfo(
      Package pkg, Sequence seq, int frequency, int occurrences, int classCount) {
    assert packageLiterals != null;
    packageLiterals.addSequenceFrequency(pkg, seq, frequency);
    packageLiterals.addSequenceOccurrence(pkg, seq, occurrences);
    packageLiterals.putPackageClassCount(pkg, classCount);
  }

  /**
   * Add a component sequence.
   *
   * @param sequence the sequence
   */
  public void addGeneratedSequence(Sequence sequence) {
    gralComponents.add(sequence);
  }

  // TODO: add comment
  public void addGeneratedSequenceInfo(Sequence sequence, int frequency, int occurrences) {
    if (constantFrequencyMap == null) {
      constantFrequencyMap = new HashMap<>();
    }
    constantFrequencyMap.put(sequence, frequency);

    if (constantOccurrenceMap == null) {
      constantOccurrenceMap = new HashMap<>();
    }
    constantOccurrenceMap.put(sequence, occurrences);
  }

  public Map<Sequence, Integer> getSequenceFrequencyMap() {
    return constantFrequencyMap;
  }

  public Map<Sequence, Integer> getSequenceOccurrenceMap() {
    return constantOccurrenceMap;
  }

  // TODO: Remove this method
  public void test() {
    // ALL
    //    // print global frequencymap and occurrencemap
    //    System.out.println("Global Frequency Map");
    //    for (Map.Entry<Sequence, Integer> entry : constantFrequencyMap.entrySet()) {
    //      System.out.println(entry.getKey() + " : " + entry.getValue());
    //    }
    //    System.out.println("Global Occurrence Map");
    //    for (Map.Entry<Sequence, Integer> entry : constantOccurrenceMap.entrySet()) {
    //      System.out.println(entry.getKey() + " : " + entry.getValue());
    //    }

    // CLASS
    //    System.out.println("Class Frequency Map");
    //    for (Map.Entry<ClassOrInterfaceType, Map<Sequence, Integer>> entry :
    // classLiterals.getSequenceFrequencyMap().entrySet()) {
    //      System.out.println(entry.getKey());
    //      for (Map.Entry<Sequence, Integer> entry2 : entry.getValue().entrySet()) {
    //        System.out.println(entry2.getKey() + " : " + entry2.getValue());
    //      }
    //    }

    // PACKAGE
    System.out.println("Package Frequency Map");
    for (Map.Entry<Package, Map<Sequence, Integer>> entry :
        packageLiterals.getSequenceFrequencyMap().entrySet()) {
      System.out.println(entry.getKey());
      for (Map.Entry<Sequence, Integer> entry2 : entry.getValue().entrySet()) {
        System.out.println(entry2.getKey() + " : " + entry2.getValue());
      }
    }

    System.out.println("Package Occurrence Map");
    for (Map.Entry<Package, Map<Sequence, Integer>> entry :
        packageLiterals.getSequenceOccurrenceMap().entrySet()) {
      System.out.println(entry.getKey());
      for (Map.Entry<Sequence, Integer> entry2 : entry.getValue().entrySet()) {
        System.out.println(entry2.getKey() + " : " + entry2.getValue());
      }
    }
  }

  /**
   * Removes any components sequences added so far, except for seed sequences, which are preserved.
   */
  void clearGeneratedSequences() {
    gralComponents = new SequenceCollection(this.gralSeeds);
  }

  /**
   * Returns the set of all generated sequences.
   *
   * @return the set of all generated sequences
   */
  Set<Sequence> getAllGeneratedSequences() {
    return gralComponents.getAllSequences();
  }

  /**
   * Returns all the general component sequences that create values of the given class.
   *
   * @param cls the query type
   * @return the sequences that create values of the given type
   */
  SimpleList<Sequence> getSequencesForType(Type cls) {
    return gralComponents.getSequencesForType(cls, false, false);
  }

  /**
   * Returns component sequences that create values of the type required by the i-th input value of
   * a statement that invokes the given operation. Also includes any applicable class- or
   * package-level literals.
   *
   * @param operation the statement
   * @param i the input value index of statement
   * @param onlyReceivers if true, only return sequences that are appropriate to use as a method
   *     call receiver
   * @return the sequences that create values of the given type
   */
  @SuppressWarnings("unchecked")
  // This method is oddly named, since it does not take as input a type.  However, the method
  // extensively uses the operation, so refactoring the method to take a type instead would take
  // some work.
  SimpleList<Sequence> getSequencesForType(TypedOperation operation, int i, boolean onlyReceivers) {

    Type neededType = operation.getInputTypes().get(i);

    if (onlyReceivers && neededType.isNonreceiverType()) {
      throw new RandoopBug(
          String.format(
              "getSequencesForType(%s, %s, %s) neededType=%s",
              operation, i, onlyReceivers, neededType));
    }

    // This method appends two lists:
    //  * determines sequences from the pool (gralComponents)
    //  * determines literals

    SimpleList<Sequence> result =
        gralComponents.getSequencesForType(neededType, false, onlyReceivers);

    // Compute relevant literals.
    SimpleList<Sequence> literals = null;
    if (operation instanceof TypedClassOperation
        // Don't add literals for the receiver
        && !onlyReceivers) {
      // The operation is a method call, where the method is defined in class C.  Augment the
      // returned list with literals that appear in class C or in its package.  At most one of
      // classLiterals and packageLiterals is non-null.

      ClassOrInterfaceType declaringCls = ((TypedClassOperation) operation).getDeclaringType();
      assert declaringCls != null;

      if (classLiterals != null) {
        SimpleList<Sequence> sl = classLiterals.getSequences(declaringCls, neededType);
        if (!sl.isEmpty()) {
          literals = sl;
        }
      }

      if (packageLiterals != null) {
        Package pkg = declaringCls.getPackage();
        if (pkg != null) {
          SimpleList<Sequence> sl = packageLiterals.getSequences(pkg, neededType);
          if (!sl.isEmpty()) {
            literals = (literals == null) ? sl : new ListOfLists<>(literals, sl);
          }
        }
      }
    }

    // Append literals to result.
    if (literals != null) {
      if (result == null) {
        result = literals;
      } else if (literals == null) {
        // nothing to do
      } else {
        result = new ListOfLists<>(result, literals);
      }
    }
    return result;
  }

  // TODO: Reconstruct the following two methods to improve reusability
  // TODO: Check the correctness
  SimpleList<Sequence> getClassLevelSequences(
      TypedOperation operation, int i, boolean onlyReceivers) {
    Type neededType = operation.getInputTypes().get(i);

    if (onlyReceivers && neededType.isNonreceiverType()) {
      throw new RandoopBug(
          String.format(
              "getSequencesForType(%s, %s, %s) neededType=%s",
              operation, i, onlyReceivers, neededType));
    }

    if (operation instanceof TypedClassOperation
        // Don't add literals for the receiver
        && !onlyReceivers) {
      // The operation is a method call, where the method is defined in class C.  Augment the
      // returned list with literals that appear in class C or in its package.  At most one of
      // classLiterals and packageLiterals is non-null.

      ClassOrInterfaceType declaringCls = ((TypedClassOperation) operation).getDeclaringType();
      assert declaringCls != null;

      return classLiterals.getSequences(declaringCls, neededType);
    }

    return null;
  }

  SimpleList<Sequence> getPackageLevelSequences(
      TypedOperation operation, int i, boolean onlyReceivers) {
    Type neededType = operation.getInputTypes().get(i);

    if (onlyReceivers && neededType.isNonreceiverType()) {
      throw new RandoopBug(
          String.format(
              "getSequencesForType(%s, %s, %s) neededType=%s",
              operation, i, onlyReceivers, neededType));
    }

    if (operation instanceof TypedClassOperation
        // Don't add literals for the receiver
        && !onlyReceivers) {
      // The operation is a method call, where the method is defined in class C.  Augment the
      // returned list with literals that appear in class C or in its package.  At most one of
      // classLiterals and packageLiterals is non-null.

      ClassOrInterfaceType declaringCls = ((TypedClassOperation) operation).getDeclaringType();
      assert declaringCls != null;

      Package pkg = declaringCls.getPackage();
      assert packageLiterals != null;

      return packageLiterals.getSequences(pkg, neededType);
    }

    return null;
  }

  /**
   * Returns all sequences that represent primitive values (e.g. sequences like "Foo var0 = null" or
   * "int var0 = 1"), including general components, class literals and package literals.
   *
   * @return the sequences for primitive values
   */
  Set<Sequence> getAllPrimitiveSequences() {

    Set<Sequence> result = new LinkedHashSet<>();
    if (classLiterals != null) {
      result.addAll(classLiterals.getAllSequences());
    }
    if (packageLiterals != null) {
      result.addAll(packageLiterals.getAllSequences());
    }
    for (PrimitiveType type : JavaTypes.getPrimitiveTypes()) {
      result.addAll(gralComponents.getSequencesForType(type, true, false).toJDKList());
    }
    result.addAll(
        gralComponents.getSequencesForType(JavaTypes.STRING_TYPE, true, false).toJDKList());
    return result;
  }

  TypeInstantiator getTypeInstantiator() {
    return gralComponents.getTypeInstantiator();
  }

  public void log() {
    if (!Log.isLoggingOn()) {
      return;
    }
    gralComponents.log();
  }
}
