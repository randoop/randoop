package randoop.generation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import randoop.main.GenInputsAbstract;
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
import randoop.util.Randomness;
import randoop.util.SimpleList;

/**
 * Stores and provides means to access the component sequences generated during a run of Randoop.
 * "Component sequences" are sequences that Randoop uses to create larger sequences. Also stores and
 * provides means to access the frequency of extracted class literals.
 *
 * <p>This class manages different collections of component sequences:
 *
 * <ul>
 *   <li>General components that can be used as input to any method in any class.
 *   <li>Class literals: components representing literal values that apply only to a specific class
 *       and should not be used as inputs to other classes.
 * </ul>
 *
 * SEED SEQUENCES. Seed sequences are sequences that were not created during the generation process
 * but obtained via other means. They include (1) sequences passed via the constructor, (2) class
 * literals, and (3) package literals. The only different treatment of seed sequences is during
 * calls to the clearGeneratedSequences() method, which removes only general, non-seed components
 * from the collection.
 */
public class ComponentManager {

  /**
   * This frequency represents the number of times a class-level literal occurs in all classes under
   * test. Used for the static weighting scheme of extracted class-level literals, which is used in
   * the weighted sequence selection.
   */
  private Map<Sequence, Integer> literalFrequency;

  /** The principal set of sequences used to create other, larger sequences by the generator. */
  // Is never null. Contains both general components
  // and seed sequences.
  private SequenceCollection gralComponents;

  /**
   * The subset of the sequences that were given pre-generation to the component manager (via its
   * constructor).
   */
  // Seeds are all contained in gralComponents. This list
  // is kept to restore seeds if the user calls
  // clearGeneratedSequences().
  private final Collection<Sequence> gralSeeds;

  /**
   * A set of additional components representing literals that should only be used as input to
   * specific classes.
   */
  // May be null, which represents no class literals present.
  private ClassLiterals classLiterals = null;

  /**
   * A set of additional components representing literals that should only be used as input to
   * specific packages.
   */
  // May be null, which represents no package literals present.
  private PackageLiterals packageLiterals = null;

  private Set<Type> sequenceTypes;

  /** Create an empty component manager, with an empty seed sequence set. */
  public ComponentManager() {
    gralComponents = new SequenceCollection();
    gralSeeds = Collections.unmodifiableSet(Collections.<Sequence>emptySet());
  }

  /**
   * Create a component manager, initially populated with the given sequences (which are considered
   * seed sequences) and with a literalFrequency map to support sequences' static weighted-constants
   * weighting scheme.
   *
   * @param generalSeeds seed sequences. Can be null, in which case the seed sequences set is
   *     considered empty.
   */
  public ComponentManager(Collection<Sequence> generalSeeds) {
    Set<Sequence> seedSet = new LinkedHashSet<>(generalSeeds.size());
    seedSet.addAll(generalSeeds);
    this.gralSeeds = Collections.unmodifiableSet(seedSet);
    gralComponents = new SequenceCollection(seedSet);
    literalFrequency = new LinkedHashMap<>();
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
   * Add a component sequence, and update the sequence's frequency.
   *
   * @param sequence the sequence
   */
  public void addGeneratedSequence(Sequence sequence) {
    gralComponents.add(sequence);
    if (literalFrequency.containsKey(sequence)) {
      literalFrequency.put(sequence, literalFrequency.get(sequence) + 1);
    } else {
      literalFrequency.put(sequence, 1);
    }
  }

  /**
   * Removes any components sequences added so far, except for seed sequences, which are preserved.
   */
  void clearGeneratedSequences() {
    gralComponents = new SequenceCollection(this.gralSeeds);
  }

  /** @return the mapping of sequences to their frequency */
  public Map<Sequence, Integer> getLiteralFrequency() {
    return literalFrequency;
  }

  /*
   * @return the set of generated sequences
   */
  Set<Sequence> getAllGeneratedSequences() {
    return gralComponents.getAllSequences();
  }

  /**
   * Returns all the general component sequences that create values of the given class. If
   * exactMatch==true returns only sequences that declare values of the exact class specified; if
   * exactMatch==false returns sequences declaring values of cls or any other class that can be used
   * as a cls (i.e. a subclass of cls).
   *
   * @param cls the query type
   * @return the sequences that create values of the given type
   */
  SimpleList<Sequence> getSequencesForType(Type cls) {
    return gralComponents.getSequencesForType(cls, false);
  }

  /**
   * Returns component sequences that create values of the type required by the i-th input value of
   * the given statement. With probability <code>--p-const</code> (as given by the command-line
   * option), this only returns the subset of these component sequences that are extracted literals.
   * Otherwise, it returns all of these component sequences.
   *
   * @param operation the statement
   * @param i the input value index of statement
   * @return the sequences that create values of the given type
   */
  @SuppressWarnings("unchecked")
  SimpleList<Sequence> getSequencesForType(TypedOperation operation, int i) {

    //TODO: this is causing a lot of the issues with the tests
    Type neededType = operation.getInputTypes().get(i);

    SimpleList<Sequence> ret = gralComponents.getSequencesForType(neededType, false);
    if (operation instanceof TypedClassOperation) {
      if (Randomness.weightedCoinFlip(GenInputsAbstract.p_const)) {
        ClassOrInterfaceType declaringCls = ((TypedClassOperation) operation).getDeclaringType();
        if (declaringCls != null) {
          if (classLiterals != null) {
            SimpleList<Sequence> sl = classLiterals.getSequences(declaringCls, neededType);
            return sl;
          }
        }
      }
    }
    return ret;

    //    Type neededType = operation.getInputTypes().get(i);
    //
    //    SimpleList<Sequence> ret = gralComponents.getSequencesForType(neededType, false);
    //    if (operation instanceof TypedClassOperation) {
    //      if (classLiterals != null || packageLiterals != null) {
    //
    //        ClassOrInterfaceType declaringCls = ((TypedClassOperation) operation).getDeclaringType();
    //        if (declaringCls != null) {
    //          if (classLiterals != null) {
    //            SimpleList<Sequence> sl = classLiterals.getSequences(declaringCls, neededType);
    //            if (!sl.isEmpty()) {
    //              ret = new ListOfLists<>(ret, sl);
    //            }
    //          }
    //
    //          if (packageLiterals != null) {
    //            Package pkg = declaringCls.getPackage();
    //            if (pkg != null) {
    //              SimpleList<Sequence> sl = packageLiterals.getSequences(pkg, neededType);
    //              if (!sl.isEmpty()) {
    //                ret = new ListOfLists<>(ret, sl);
    //              }
    //            }
    //          }
    //        }
    //      }
    //    }
    //    return ret;
  }

  /**
   * Returns all sequences that represent primitive values (e.g. sequences like "Foo var0 = null" or
   * "int var0 = 1"), including general components, class literals and package literals.
   *
   * @return the sequences for primitive values
   */
  Set<Sequence> getAllPrimitiveSequences() {

    Set<Sequence> ret = new LinkedHashSet<>();
    if (classLiterals != null) {
      ret.addAll(classLiterals.getAllSequences());
    }
    if (packageLiterals != null) {
      ret.addAll(packageLiterals.getAllSequences());
    }
    for (PrimitiveType type : JavaTypes.getPrimitiveTypes()) {
      ret.addAll(gralComponents.getSequencesForType(type, true).toJDKList());
    }
    ret.addAll(gralComponents.getSequencesForType(JavaTypes.STRING_TYPE, true).toJDKList());
    return ret;
  }

  TypeInstantiator getTypeInstantiator() {
    return gralComponents.getTypeInstantiator();
  }
}
