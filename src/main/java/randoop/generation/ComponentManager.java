package randoop.generation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.plumelib.util.CollectionsPlume;
import org.plumelib.util.SIList;
import randoop.generation.literaltfidf.ScopeToLiteralStatistics;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
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
 * candidates depends on the method being called.
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

  /** For each scope in the SUT, statistics about its literals. */
  public ScopeToLiteralStatistics scopeToLiteralStatistics = new ScopeToLiteralStatistics();

  /** Create an empty component manager, with an empty seed sequence set. */
  public ComponentManager() {
    gralComponents = new SequenceCollection();
    gralSeeds = Collections.<Sequence>emptySet();
  }

  /**
   * Create a component manager, initially populated with the given sequences, which are considered
   * seed sequences.
   *
   * @param generalSeeds seed sequences. Can be null, in which case the seed sequences set is
   *     considered empty.
   */
  public ComponentManager(Collection<Sequence> generalSeeds) {
    Set<Sequence> seedSet = new LinkedHashSet<>(generalSeeds);
    this.gralSeeds = Collections.unmodifiableSet(seedSet);
    gralComponents = new SequenceCollection(seedSet);
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
  public ScopeToLiteralStatistics getScopeToLiteralStatistics() {
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
    //  * determines literals, which depend on the scope of `declaringCls`

    SIList<Sequence> result = gralComponents.getSequencesForType(neededType, false, onlyReceivers);

    // Compute relevant literals.
    SIList<Sequence> literals = SIList.empty();
    if (operation instanceof TypedClassOperation
        // Don't add literals for the receiver
        && !onlyReceivers
        // Avoid duplication
        && GenInputsAbstract.literals_level != GenInputsAbstract.ClassLiteralsMode.ALL) {
      // The operation is a method call, where the method is defined in class C.
      assert declaringCls != null;
      literals = getLiteralSequences(neededType, declaringCls);
    }

    return SIList.concat(result, literals);
  }

  /**
   * Returns literal sequences of the type {@code neededType} from the current {@code declaringType}
   * as well as its superclasses.
   *
   * @param neededType the type of literals
   * @param declaringType the type whose scope to use for literal selection
   * @return the sequences extracted by literal that create values of the given type
   */
  SIList<Sequence> getLiteralSequences(Type neededType, ClassOrInterfaceType declaringType) {
    return scopeToLiteralStatistics.getSequencesIncludingSupertypes(declaringType, neededType);
  }

  /**
   * Returns all sequences that represent primitive values (e.g. sequences like "Foo var0 = null" or
   * "int var0 = 1"), including general components and literals.
   *
   * @return the sequences for primitive values
   */
  Set<Sequence> getAllPrimitiveSequences() {

    Set<Sequence> result = new LinkedHashSet<>();
    result.addAll(scopeToLiteralStatistics.getAllSequences());

    // Add primitive sequences from general components.
    // This code uses `CollectionsPlume.addAll`, whose second argument is an `Iterable`.
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
