package randoop.generation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.plumelib.util.CollectionsPlume;
import org.plumelib.util.SIList;
import randoop.generation.constanttfidf.ScopeToConstantStatistics;
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

  /** For each scope in the SUT, statistics about its constants. */
  public ScopeToConstantStatistics scopeToConstantStatistics = new ScopeToConstantStatistics();

  /**
   * Cache for constant sequences filtered by scope and type. Key format: scopeKey + ":" +
   * neededType
   */
  private final Map<String, SIList<Sequence>> constantSequenceCache = new HashMap<>();

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
   * Add a component sequence.
   *
   * @param sequence the sequence
   */
  public void addGeneratedSequence(Sequence sequence) {
    gralComponents.add(sequence);
  }

  /**
   * Returns the constant statistics.
   *
   * @return an object that contains the constant information
   */
  public ScopeToConstantStatistics getScopeToConstantStatistics() {
    return scopeToConstantStatistics;
  }

  /**
   * Sets the constant statistics.
   *
   * @param scopeToConstantStatistics the constant statistics
   */
  // This is called in OperationModel.addClassLiterals().
  public void setScopeToConstantStatistics(ScopeToConstantStatistics scopeToConstantStatistics) {
    this.scopeToConstantStatistics = scopeToConstantStatistics;
    constantSequenceCache.clear();
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
   * package-level literals, and constants.
   *
   * @param operation the operation whose {@code i}th parameter to find values for
   * @param i an input value index for {@code operation}
   * @param onlyReceivers if true, the client of this method only returns sequences that are
   *     appropriate to use as a method call receiver
   * @return the sequences that create values of the given type
   */
  @SuppressWarnings("unchecked")
  // This method is oddly named, since it does not take as input a type.  However, the method
  // extensively uses the operation, so refactoring the method to take a type instead would take
  // some work.
  SIList<Sequence> getSequencesForType(TypedOperation operation, int i, boolean onlyReceivers) {

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

    SIList<Sequence> result = gralComponents.getSequencesForType(neededType, false, onlyReceivers);

    // Compute relevant literals.
    SIList<Sequence> literals = SIList.empty();
    if (operation instanceof TypedClassOperation
        // Don't add literals for the receiver
        && !onlyReceivers) {
      // The operation is a method call, where the method is defined in class C.
      ClassOrInterfaceType declaringCls = ((TypedClassOperation) operation).getDeclaringType();
      assert declaringCls != null;

      SIList<Sequence> constantCandidates = getConstantSequences(neededType, declaringCls);
      literals = SIList.concat(literals, constantCandidates);
    }

    return SIList.concat(result, literals);
  }

  /**
   * Returns constant sequences of the type {@code neededType} from the current {@code
   * declaringType} as well as its superclasses.
   *
   * @param neededType the type of constants
   * @param declaringType the type whose scope to use for constant selection
   * @return the sequences extracted by constant that create values of the given type
   */
  SIList<Sequence> getConstantSequences(Type neededType, ClassOrInterfaceType declaringType) {
    Object scopeKey = scopeToConstantStatistics.getScope(declaringType);
    String cacheKey = scopeKey + ":" + neededType;
    SIList<Sequence> result = constantSequenceCache.get(cacheKey);
    if (result == null) {
      result =
          scopeToConstantStatistics.getSequencesIncludingSuperclasses(declaringType, neededType);
      constantSequenceCache.put(cacheKey, result);
    }

    return result;
  }

  /**
   * Returns all sequences that represent primitive values (e.g. sequences like "Foo var0 = null" or
   * "int var0 = 1"), including general components and constant literals.
   *
   * @return the sequences for primitive values
   */
  Set<Sequence> getAllPrimitiveSequences() {

    Set<Sequence> result = new LinkedHashSet<>();
    if (scopeToConstantStatistics != null) {
      result.addAll(scopeToConstantStatistics.getAllSequences());
    }

    // Add primitive sequences from general components
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
