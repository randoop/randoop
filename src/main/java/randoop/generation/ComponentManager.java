package randoop.generation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.main.GenInputsAbstract;
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

  /**
   * Wrapper for the constant mining storage. It contains the constant mining storage for each
   * literal level.
   */
  private ConstantMiningWrapper constantMiningWrapper = new ConstantMiningWrapper();

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

  /**
   * Add a component sequence.
   *
   * @param sequence the sequence
   */
  public void addGeneratedSequence(Sequence sequence) {
    gralComponents.add(sequence);
  }

  /**
   * Get the constant mining wrapper.
   *
   * @return the constant mining wrapper that contains the constant mining information for each
   *     literal level
   */
  public ConstantMiningWrapper getConstantMiningWrapper() {
    return constantMiningWrapper;
  }

  /**
   * Set the constant mining wrapper.
   *
   * @param constantMiningWrapper the constant mining wrapper
   */
  public void setConstantMiningWrapper(ConstantMiningWrapper constantMiningWrapper) {
    this.constantMiningWrapper = constantMiningWrapper;
  }

  /**
   * Get the constant frequency information for the given scope based on the literals level.
   *
   * @param scope the desired scope, could be any package, class, or null
   * @return the frequency information for the given scope
   */
  public Map<Sequence, Integer> getConstantFrequencyInfoForType(Object scope) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        return constantMiningWrapper
            .getClassLevel()
            .getFrequencyInfoForType((ClassOrInterfaceType) scope);
      case PACKAGE:
        return constantMiningWrapper.getPackageLevel().getFrequencyInfoForType((Package) scope);
      case ALL:
        return constantMiningWrapper.getAllLevel().getFrequencyInfo().get(null);
      default:
        throw new RandoopBug("Unexpected literals level: " + GenInputsAbstract.literals_level);
    }
  }

  /**
   * Get the classes with constant information for the given scope based on the literals level.
   *
   * @param scope the desired scope, could be any package, class, or null
   * @return the classes with constant information for the given scope
   */
  public Map<Sequence, Integer> getClassesWithConstantInfoForType(Object scope) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        throw new RandoopBug("Should not get classesWithConstant in CLASS level");
      case PACKAGE:
        return constantMiningWrapper
            .getPackageLevel()
            .getClassesWithConstantInfoForType((Package) scope);
      case ALL:
        return constantMiningWrapper.getAllLevel().getClassesWithConstantInfo().get(null);
      default:
        throw new RandoopBug("Unexpected literals level: " + GenInputsAbstract.literals_level);
    }
  }

  /**
   * Get the number of total classes for the given scope based on the literals level.
   *
   * @param scope the desired scope, could be any package or null
   * @return the total classes for the given scope
   */
  public Integer getTotalClassesForType(Object scope) {
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        throw new RandoopBug("Should not get totalClasses in CLASS level");
      case PACKAGE:
        return constantMiningWrapper.getPackageLevel().getTotalClassesForType((Package) scope);
      case ALL:
        return constantMiningWrapper.getAllLevel().getTotalClassesForType(null);
      default:
        throw new RandoopBug("Unexpected literals level: " + GenInputsAbstract.literals_level);
    }
  }

  // TODO: Convert it to toString
  // Only for testing constant mining. Delete this after tests are done.
  public void test() {
    // ALL
    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        System.out.println("Class Level");
        System.out.println("Class Frequency Map");
        for (Map.Entry<ClassOrInterfaceType, Map<Sequence, Integer>> entry :
            constantMiningWrapper.getClassLevel().getFrequencyInfo().entrySet()) {
          System.out.println(entry.getKey());
          for (Map.Entry<Sequence, Integer> entry2 : entry.getValue().entrySet()) {
            System.out.println(entry2.getKey() + " : " + entry2.getValue());
          }
        }
        break;
      case PACKAGE:
        System.out.println("Package Level");
        System.out.println("Package Frequency Map");
        for (Map.Entry<Package, Map<Sequence, Integer>> entry :
            constantMiningWrapper.getPackageLevel().getFrequencyInfo().entrySet()) {
          System.out.println(entry.getKey());
          for (Map.Entry<Sequence, Integer> entry2 : entry.getValue().entrySet()) {
            System.out.println(entry2.getKey() + " : " + entry2.getValue());
          }
        }
        System.out.println("Package classWithConstant Map");
        for (Map.Entry<Package, Map<Sequence, Integer>> entry :
            constantMiningWrapper.getPackageLevel().getClassesWithConstantInfo().entrySet()) {
          System.out.println(entry.getKey());
          for (Map.Entry<Sequence, Integer> entry2 : entry.getValue().entrySet()) {
            System.out.println(entry2.getKey() + " : " + entry2.getValue());
          }
        }
        break;
      case ALL:
        System.out.println("All Level");
        System.out.println("Global Frequency Map");
        for (Map.Entry<Sequence, Integer> entry :
            constantMiningWrapper.getAllLevel().getFrequencyInfo().get(null).entrySet()) {
          System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        System.out.println("Global classesWithConstants Map");
        for (Map.Entry<Sequence, Integer> entry :
            constantMiningWrapper.getAllLevel().getClassesWithConstantInfo().get(null).entrySet()) {
          System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        break;
      default:
        throw new RandoopBug("Unexpected literals level: " + GenInputsAbstract.literals_level);
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

  // Validates if the onlyReceiver flag is consistent with the neededType. Throw an exception if the
  // flag is inconsistent with the neededType.
  private void validateReceiver(TypedOperation operation, Type neededType, boolean onlyReceivers) {
    if (onlyReceivers && neededType.isNonreceiverType()) {
      throw new RandoopBug(
          String.format(
              "getSequencesForType(%s, %s, %s) neededType=%s",
              operation, neededType, onlyReceivers, neededType));
    }
  }

  /**
   * Returns component sequences extracted by constant mining that create values of the type
   * required by the i-th input value of a statement that invokes the given operation for its
   * corresponding class for the current literal level. Only used when constant mining is enabled.
   *
   * @param operation the statement
   * @param i the input value index of statement
   * @param onlyReceivers if true, only return sequences that are appropriate to use as a method
   *     call receiver
   * @return the sequences extracted by constant mining that create values of the given type
   */
  SimpleList<Sequence> getConstantMiningSequences(
      TypedOperation operation, int i, boolean onlyReceivers) {
    Type neededType = operation.getInputTypes().get(i);
    validateReceiver(operation, neededType, onlyReceivers);

    SequenceCollection sc = new SequenceCollection();

    switch (GenInputsAbstract.literals_level) {
      case CLASS:
        if (operation instanceof TypedClassOperation
            // Don't add literals for the receiver
            && !onlyReceivers) {
          // The operation is a method call, where the method is defined in class C.  Augment the
          // returned list with literals that appear in class C or in its package.  At most one of
          // classLiterals and packageLiterals is non-null.

          ClassOrInterfaceType declaringCls = ((TypedClassOperation) operation).getDeclaringType();
          assert declaringCls != null;
          // Add all sequences from the constant mining storage
          sc.addAll(constantMiningWrapper.getClassLevel().getSequencesForScope(declaringCls));
          return sc.getSequencesForType(neededType, false, onlyReceivers);
        }
        break;
      case PACKAGE:
        if (operation instanceof TypedClassOperation
            // Don't add literals for the receiver
            && !onlyReceivers) {

          // The operation is a method call, where the method is defined in class C.  Augment the
          // returned list with literals that appear in class C or in its package.  At most one of
          // classLiterals and packageLiterals is non-null.

          ClassOrInterfaceType declaringCls = ((TypedClassOperation) operation).getDeclaringType();
          assert declaringCls != null;

          Package pkg = declaringCls.getPackage();
          // Add all sequences from the constant mining storage
          sc.addAll(constantMiningWrapper.getPackageLevel().getSequencesForScope(pkg));
          return sc.getSequencesForType(neededType, false, onlyReceivers);
        }
        break;
      case ALL:
        sc.addAll(constantMiningWrapper.getAllLevel().getSequencesForScope(null));
        return sc.getSequencesForType(neededType, false, onlyReceivers);
      default:
        throw new RandoopBug("Unexpected literals level: " + GenInputsAbstract.literals_level);
    }

    // TODO: Check why it is possible to reach here. Is it supposed to be unreachable?
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
