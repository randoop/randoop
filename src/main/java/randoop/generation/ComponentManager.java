package randoop.generation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.CollectionsPlume;
import org.plumelib.util.SIList;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.AccessibilityPredicate;
import randoop.reflection.TypeInstantiator;
import randoop.sequence.ClassLiterals;
import randoop.sequence.PackageLiterals;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceCollection;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.PrimitiveType;
import randoop.types.Type;
import randoop.util.Log;

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
   * The principal set of sequences used to create other, larger sequences by the generator.
   * Contains both general components and seed sequences. Can be reset by calling {@link
   * #clearGeneratedSequences}.
   */
  // "gral" probably stands for "general".
  private SequenceCollection gralComponents;

  /**
   * The sequences that were given pre-generation to the component manager (via its constructor).
   * (Does not include literals, I think?)
   *
   * <p>Seeds are all contained in {@link #gralComponents}. This list is kept to restore seeds if
   * the client calls {@link #clearGeneratedSequences}.
   */
  private final Collection<Sequence> gralSeeds;

  /**
   * Components representing literals that should only be used as input to specific classes.
   *
   * <p>Null if class literals are not used or none were found. At most one of classLiterals and
   * packageliterals is non-null.
   */
  private @Nullable ClassLiterals classLiterals = null;

  /**
   * A set of additional components representing literals that should only be used as input to
   * specific packages.
   *
   * <p>Null if package literals are not used or none were found. At most one of classLiterals and
   * packageliterals is non-null.
   */
  private @Nullable PackageLiterals packageLiterals = null;

  /**
   * Decides which constructors/methods are callable from the generated test code. This predicate
   * matches the visibility rules chosen for the overall test package. This is kept so that if the
   * user calls {@link #clearGeneratedSequences}, we can create a new {@link
   * DemandDrivenInputCreator} with the same accessibility rules.
   */
  private final AccessibilityPredicate accessibility;

  /**
   * Types that are SUT-parameters but not SUT-returned.
   *
   * <p>{@link randoop.generation.DemandDrivenInputCreator} will create sequences for these types
   * when no existing instances are available. This set is kept so that if the user calls {@link
   * #clearGeneratedSequences}, we can re-add these types to the {@link DemandDrivenInputCreator}
   * associated with {@link #gralComponents}.
   */
  private final Set<Type> sutParameterOnlyTypes = new LinkedHashSet<>();

  /**
   * Create an empty component manager, with an immutable empty seed sequence set.
   *
   * @param accessibility decides which constructors/methods are callable from the generated test
   *     code. This predicate matches the visibility rules chosen for the overall test package.
   */
  public ComponentManager(AccessibilityPredicate accessibility) {
    gralComponents = new SequenceCollection();
    gralSeeds = Collections.unmodifiableSet(Collections.<Sequence>emptySet());
    this.accessibility = accessibility;
    initDemandDrivenIfEnabled();
  }

  /**
   * Create a component manager, initially populated with the given sequences, which are considered
   * seed sequences.
   *
   * @param generalSeeds seed sequences. Can be null, in which case the seed sequences set is
   *     considered empty.
   * @param accessibility decides which constructors/methods are callable from the generated test
   *     code. This predicate matches the visibility rules chosen for the overall test package.
   */
  public ComponentManager(Collection<Sequence> generalSeeds, AccessibilityPredicate accessibility) {
    if (generalSeeds == null) {
      generalSeeds = Collections.emptySet();
    }
    Set<Sequence> seedSet = new LinkedHashSet<>(generalSeeds.size());
    seedSet.addAll(generalSeeds);
    this.gralSeeds = Collections.unmodifiableSet(seedSet);
    gralComponents = new SequenceCollection(seedSet);
    this.accessibility = accessibility;
    initDemandDrivenIfEnabled();
  }

  /**
   * If demand-driven input generation is enabled, set up the demand-driven input creator for the
   * component manager.
   */
  private void initDemandDrivenIfEnabled() {
    if (GenInputsAbstract.demand_driven) {
      DemandDrivenInputCreator ddic =
          new DemandDrivenInputCreator(
              gralComponents, gralComponents.getTypeInstantiator(), accessibility);
      gralComponents.setDemandDrivenInputCreator(ddic);
    }
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
  public void addPackageLevelLiteral(@Nullable Package pkg, Sequence seq) {
    if (packageLiterals == null) {
      packageLiterals = new PackageLiterals();
    }
    packageLiterals.addSequence(pkg, seq);
  }

  /**
   * Register types that are SUT-parameters but not SUT-returned.
   *
   * <p>{@link randoop.generation.DemandDrivenInputCreator} will create sequences for these types
   * when no existing instances are available.
   *
   * @param types a set of types that are SUT-parameters but not SUT-returned
   */
  public void addSutParameterOnlyTypes(Set<Type> types) {
    gralComponents.addSutParameterOnlyTypes(types);
    this.sutParameterOnlyTypes.addAll(types);
  }

  /**
   * Return the {@link DemandDrivenInputCreator} that creates sequences for types that are
   * SUT-parameters but not SUT-returned.
   *
   * @return the {@link DemandDrivenInputCreator} that creates sequences for types that are
   *     SUT-parameters but not SUT-returned
   */
  public DemandDrivenInputCreator getDemandDrivenInputCreator() {
    return gralComponents.getDemandDrivenInputCreator();
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
    if (GenInputsAbstract.demand_driven) {
      DemandDrivenInputCreator ddic =
          new DemandDrivenInputCreator(
              gralComponents, gralComponents.getTypeInstantiator(), accessibility);
      gralComponents.setDemandDrivenInputCreator(ddic);
      if (!sutParameterOnlyTypes.isEmpty()) {
        gralComponents.addSutParameterOnlyTypes(sutParameterOnlyTypes);
      }
    }
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
  SIList<Sequence> getSequencesForType(Type cls) {
    return gralComponents.getSequencesForType(cls, false, false);
  }

  /**
   * Returns component sequences that create values of the type required by the i-th input value of
   * a statement that invokes the given operation. Also includes any applicable class- or
   * package-level literals.
   *
   * @param operation the operation whose {@code i}th parameter to find values for
   * @param i an input value index for {@code operation}
   * @param onlyReceivers if true, the client of this method only returns sequences that are
   *     appropriate to use as a method call receiver
   * @return the sequences that create values of the given type
   */
  @SuppressWarnings("unchecked")
  SIList<Sequence> getSequencesForParam(TypedOperation operation, int i, boolean onlyReceivers) {

    Type neededType = operation.getInputTypes().get(i);
    ClassOrInterfaceType declaringCls = ((TypedClassOperation) operation).getDeclaringType();

    if (onlyReceivers && neededType.isNonreceiverType()) {
      throw new RandoopBug(
          String.format(
              "getSequencesForParam(%s, %s, %s) neededType=%s",
              operation, i, onlyReceivers, neededType));
    }

    // This method appends two lists:
    //  * determines sequences from the pool (gralComponents)
    //  * determines literals, which depend on `declaringCls`

    SIList<Sequence> result = gralComponents.getSequencesForType(neededType, false, onlyReceivers);

    // Compute relevant literals.
    SIList<Sequence> literals = SIList.empty();
    if (operation instanceof TypedClassOperation
        // Don't add literals for the receiver
        && !onlyReceivers) {
      // The operation is a method call, where the method is defined in class C.
      // Augment the returned list with literals that appear in class C or in its package.  At most
      // one of classLiterals and packageLiterals is non-null.

      assert declaringCls != null;

      if (classLiterals != null) {
        SIList<Sequence> sl = classLiterals.getSequences(declaringCls, neededType);
        if (!sl.isEmpty()) {
          literals = sl;
        }
      }

      if (packageLiterals != null) {
        Package pkg = declaringCls.getPackage();
        if (pkg != null) {
          @SuppressWarnings("nullness:dereference.of.nullable") // tested above, no side effects
          SIList<Sequence> sl = packageLiterals.getSequences(pkg, neededType);
          literals = SIList.concat(literals, sl);
        }
      }
    }

    return SIList.concat(result, literals);
  }

  /**
   * Returns all sequences that represent primitive values (e.g. sequences like "Foo var0 = null" or
   * "int var0 = 1"), including general components and literals.
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

    // Add primitive sequences from general components.
    for (PrimitiveType type : JavaTypes.getPrimitiveTypes()) {
      CollectionsPlume.addAll(result, gralComponents.getSequencesForType(type, true, false));
    }
    CollectionsPlume.addAll(
        result, gralComponents.getSequencesForType(JavaTypes.STRING_TYPE, true, false));
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
