package randoop.generation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
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
 * Stores and provides means to access the component sequences generated during a run of Randoop.
 * "Component sequences" are sequences that Randoop uses to create larger sequences. The collection
 * of sequences is also called Randoop's "pool".
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
 * <p>SEED SEQUENCES. Seed sequences are sequences that were not created during the generation
 * process but obtained via other means. They include (1) sequences passed via the constructor, (2)
 * class literals, and (3) package literals. The only different treatment of seed sequences is
 * during calls to the clearGeneratedSequences() method, which removes only general, non-seed
 * components from the collection.
 */
public class ComponentManager {

  /** The principal set of sequences used to create other, larger sequences by the generator. */
  // Is never null. Contains both general components and seed sequences.
  // "gral" probably stands for "general".
  private SequenceCollection gralComponents;

  /**
   * The subset of the sequences that were given pre-generation to the component manager (via its
   * constructor).
   */
  // Seeds are all contained in gralComponents. This list is kept to restore seeds if the user calls
  // clearGeneratedSequences().
  private final Collection<Sequence> gralSeeds;

  /**
   * A set of additional components representing literals that should only be used as input to
   * specific classes.
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
  private PackageLiterals packageLiterals = null;

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
   * Removes any components sequences added so far, except for seed sequences, which are preserved.
   */
  void clearGeneratedSequences() {
    gralComponents = new SequenceCollection(this.gralSeeds);
  }

  /** @return the set of generated sequences */
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
