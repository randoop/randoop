package randoop.generation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.CollectionsPlume;
import org.plumelib.util.SIList;
import randoop.generation.literaltfidf.ScopeToLiteralStatistics;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.AccessibilityPredicate;
import randoop.reflection.TypeInstantiator;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceCollection;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.PrimitiveType;
import randoop.types.Type;
import randoop.util.Log;

/**
 * Manages the pool of component sequences used by Randoop during generation.
 *
 * <p>A "component sequence" is a previously-constructed {@link randoop.sequence.Sequence} that can
 * be reused as a building block to create larger sequences. The pool contains:
 *
 * <ul>
 *   <li>Seed sequences supplied at construction time, which are preserved across calls to {@link
 *       #clearGeneratedSequences()}, and
 *   <li>Sequences generated during the current run.
 * </ul>
 *
 * <p>This class also maintains per-scope literal information via {@link #scopeToLiteralStatistics}.
 * Literals are not stored in the general pool; instead, they are consulted on demand (for example,
 * by {@link #getSequencesForParam(randoop.operation.TypedOperation,int,boolean)}) and combined with
 * pool sequences when returning candidates for a parameter. The reason is that which literals are
 * candidates depends on the method being called. (More precisely, on the class and package in which
 * the method is defined.)
 *
 * <p>Calling {@link #clearGeneratedSequences()} removes all non-seed sequences, restoring the pool
 * to the original seeds.
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

  /** For each scope in the SUT, statistics about its literals (if available). */
  public @Nullable ScopeToLiteralStatistics scopeToLiteralStatistics = null;

  /**
   * Decides which constructors/methods are callable from the generated test code. This predicate
   * matches the visibility rules chosen for the overall test package. This field exists so that if
   * the user calls {@link #clearGeneratedSequences}, we can create a new {@link
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
   *
   * <p>This variable is used only by {@link #clearGeneratedSequences}.
   */
  private final Set<Type> sutParameterOnlyTypes = new LinkedHashSet<>();

  /**
   * Create an empty component manager, with an immutable empty seed sequence set.
   *
   * @param accessibility decides which constructors/methods are callable from the generated test
   *     code. This predicate matches the visibility rules chosen for the overall test package.
   */
  public ComponentManager(AccessibilityPredicate accessibility) {
    this(Collections.emptySet(), accessibility);
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
    if (accessibility == null) {
      throw new IllegalArgumentException("accessibility must be non-null");
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
   * Returns the number of sequences stored by the manager.
   *
   * @return count of generated sequences in this {@link ComponentManager}
   */
  // FIXME subtract size of seeds!
  public int numGeneratedSequences() {
    return gralComponents.size();
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
    if (types == null || types.isEmpty()) {
      return;
    }
    gralComponents.addSutParameterOnlyTypes(types);
    this.sutParameterOnlyTypes.addAll(types);
  }

  /**
   * Return the {@link DemandDrivenInputCreator} that creates sequences for types that are
   * SUT-parameters but not SUT-returned.
   *
   * @return the {@link DemandDrivenInputCreator} that creates sequences for types that are
   *     SUT-parameters but not SUT-returned
   * @throws IllegalStateException if demand-driven input generation is not enabled (i.e., {@code
   *     GenInputsAbstract.demand_driven} is false)
   */
  public DemandDrivenInputCreator getDemandDrivenInputCreator() {
    if (GenInputsAbstract.demand_driven == false) {
      throw new IllegalStateException(
          "getDemandDrivenInputCreator() called when demand-driven input generation is disabled. "
              + "Enable it with --demand-driven=true.");
    }
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
   * Returns the literal statistics map.
   *
   * @return the literal statistics map
   */
  public @Nullable ScopeToLiteralStatistics getScopeToLiteralStatistics() {
    return scopeToLiteralStatistics;
  }

  /**
   * Sets the literal statistics map.
   *
   * @param scopeToLiteralStatistics the literal statistics map
   */
  // This is called in OperationModel.addClassLiterals().
  public void setScopeToLiteralStatistics(ScopeToLiteralStatistics scopeToLiteralStatistics) {
    this.scopeToLiteralStatistics = scopeToLiteralStatistics;
  }

  /**
   * Removes any component sequences added so far, except for seed sequences, which are preserved.
   */
  void clearGeneratedSequences() {
    gralComponents = new SequenceCollection(this.gralSeeds);
    initDemandDrivenIfEnabled();
    if (!sutParameterOnlyTypes.isEmpty()) {
      gralComponents.addSutParameterOnlyTypes(sutParameterOnlyTypes);
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
   * Returns candidate sequences for the {@code i}-th input of {@code operation}: pool sequences
   * that produce the required type, followed by literal sequences from the appropriate scope.
   *
   * <p>Literals are used only if {@link GenInputsAbstract#literals_level} != {@code NONE} and are
   * skipped for receiver positions.
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

    if (onlyReceivers && neededType.isNonreceiverType()) {
      throw new RandoopBug(
          String.format(
              "getSequencesForParam(%s, %s, %s) neededType=%s",
              operation, i, onlyReceivers, neededType));
    }

    // This method appends two lists:
    //  * sequences from the pool (gralComponents)
    //  * literals, which depend on `declaringCls`

    SIList<Sequence> result = gralComponents.getSequencesForType(neededType, false, onlyReceivers);

    // If literals are disabled, don't attempt to add any.
    if (GenInputsAbstract.literals_level == GenInputsAbstract.ClassLiteralsMode.NONE) {
      return result;
    }

    // Compute relevant literals.
    SIList<Sequence> literals = SIList.empty();
    if (operation instanceof TypedClassOperation
        // Don't add literals for the receiver
        && !onlyReceivers) {
      // The operation is a method call, where the method is defined in class C.
      ClassOrInterfaceType declaringCls = ((TypedClassOperation) operation).getDeclaringType();
      assert declaringCls != null;
      // The scope is determined from the class `declaringCls`.
      literals = getLiteralSequences(neededType, declaringCls);
    }

    return SIList.concat(result, literals);
  }

  /**
   * Returns literal sequences that produce values assignable to {@code neededType}, using a
   * selection strategy determined by the current {@code literals_level} configuration.
   *
   * <p>Note: the selection *strategy* (how a sequence is chosen) depends on flags such as {@code
   * --literal-tfidf}. The *set* of candidate sequences from which the strategy chooses is
   * determined by the {@code literals_level} configuration: CLASS uses literals from only the
   * declaring class (not supertypes), PACKAGE uses package-level statistics, and ALL uses the
   * global scope.
   *
   * @param neededType the returned sequences produce values assignable to this type
   * @param declaringType the class containing the operation being tested
   * @return sequences from the appropriate scope that create values of the needed type
   */
  SIList<Sequence> getLiteralSequences(Type neededType, ClassOrInterfaceType declaringType) {
    if (scopeToLiteralStatistics == null) {
      return SIList.empty();
    }
    switch (GenInputsAbstract.literals_level) {
      case NONE:
        return SIList.empty();
      case CLASS:
      case PACKAGE:
      case ALL:
        // For all levels, we call getLiteralStatistics(declaringType) which internally uses
        // getScope() to resolve the appropriate scope based on literals_level:
        //  - CLASS: getScope() returns the declaringType itself
        //  - PACKAGE: getScope() returns the package of declaringType (all classes in the package
        //    share the same LiteralStatistics instance)
        //  - ALL: getScope() returns the shared ALL_SCOPE key (all types map to global statistics)
        return scopeToLiteralStatistics
            .getLiteralStatistics(declaringType)
            .getSequencesForType(neededType);
      default:
        throw new RandoopBug("Unexpected literals level: " + GenInputsAbstract.literals_level);
    }
  }

  /**
   * Returns all sequences that represent primitive values (e.g. sequences like "Foo var0 = null" or
   * "int var0 = 1"), including general components and literals.
   *
   * @return the sequences for primitive values
   */
  Set<Sequence> getAllPrimitiveSequences() {

    Set<Sequence> result = new LinkedHashSet<>();
    // Include literal-derived primitive sequences unless disabled.
    if (GenInputsAbstract.literals_level != GenInputsAbstract.ClassLiteralsMode.NONE
        && scopeToLiteralStatistics != null) {
      result.addAll(scopeToLiteralStatistics.getAllSequences());
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
