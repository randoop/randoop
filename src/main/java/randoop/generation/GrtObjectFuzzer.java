package randoop.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.SIList;
import randoop.main.RandoopBug;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.VarAndSeq;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.Randomness;

/**
 * Fuzzer that applies a single side-effecting operation to a variable within a test sequence to
 * explore the stateful behavior (impurity) of that object.
 *
 * <p>Specifically, this fuzzer:
 *
 * <ol>
 *   <li>Randomly picks one side-effecting method whose signature includes the target's type.
 *   <li>Randomly chooses which parameter slot to supply the target into (if there are multiple
 *       possibilities).
 *   <li>Fills the other slots by pulling sequences from the ComponentManager's sequence collection.
 *   <li>Appends the new call to the sequence.
 * </ol>
 */
public final class GrtObjectFuzzer extends GrtFuzzer {
  /** Singleton instance. */
  private static final GrtObjectFuzzer INSTANCE = new GrtObjectFuzzer();

  /** Maps a type to operations that mutate values of that type. */
  private final Map<Type, List<TypedOperation>> mutatorsByType = new HashMap<>();

  /** Component manager to get sequences for types. */
  private @MonotonicNonNull ComponentManager componentManager;

  /** Whether this fuzzer has been initialized. */
  private boolean initialized = false;

  /** Resolved unions to avoid recomputing ancestor walks. */
  private final Map<Type, List<TypedOperation>> typeToUnion = new HashMap<>();

  /**
   * Get the singleton instance of {@link GrtObjectFuzzer}.
   *
   * @return the singleton instance
   */
  public static GrtObjectFuzzer getInstance() {
    return INSTANCE;
  }

  /** Private constructor to enforce singleton. */
  private GrtObjectFuzzer() {
    /* no-op */
  }

  /**
   * Initialize once with side-effecting operations and component manager. Later calls are no-ops.
   * This is based on the expectation that Randoop has already collected all the side-effecting
   * operations and the component manager should not change during the run.
   *
   * @param sideEffectOps a set of side-effecting operations to add to the fuzzer
   * @param cm the component manager to use for getting sequences for types (should not be null)
   */
  public void initializeIfNeeded(Set<TypedOperation> sideEffectOps, ComponentManager cm) {
    if (initialized) {
      return;
    }
    mutatorsByType.clear();
    typeToUnion.clear();
    addOperations(sideEffectOps);
    this.componentManager = cm;
    initialized = true;
  }

  @Override
  public boolean canFuzz(Type type) {
    return !type.isNonreceiverType();
  }

  /**
   * Adds side-effecting operations to this fuzzer.
   *
   * @param mutators a set of side-effecting operations to index
   */
  private void addOperations(Set<TypedOperation> mutators) {
    // Build the type-to-mutators map, for quick access later.
    for (TypedOperation op : mutators) {
      TypeTuple inputTypes = op.getInputTypes();
      for (int i = 0; i < inputTypes.size(); i++) {
        Type type = erase(inputTypes.get(i));
        mutatorsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(op);
      }
    }
  }

  @Override
  public VarAndSeq fuzz(Sequence sequence, Variable variable) {
    checkPreconditions(sequence, variable);

    Type typeToFuzz = variable.getType();
    TypedOperation mutationOp = selectMutationOperation(typeToFuzz);
    if (mutationOp == null) {
      // No applicable operation for this type -- return the original sequence unchanged.
      return new VarAndSeq(variable, sequence);
    }

    TypeTuple formalTypes = mutationOp.getInputTypes();
    int fuzzParam = selectFuzzParameter(formalTypes, typeToFuzz, mutationOp);

    // Keep track of the sequences to concatenate and the index of the necessary variable in each.
    List<Sequence> sequencesToConcat = new ArrayList<>(formalTypes.size());
    List<Integer> varIndicesInEachSeq = new ArrayList<>(formalTypes.size());
    int targetParamPos = -1; // Initialize to an invalid position.

    // Collect input sequences for each formal parameter.
    for (int i = 0; i < formalTypes.size(); i++) {
      Type formalType = formalTypes.get(i);
      if (formalType.isAssignableFrom(typeToFuzz) && i == fuzzParam) {
        sequencesToConcat.add(sequence);
        varIndicesInEachSeq.add(variable.index);
        targetParamPos = i; // Remember where the target variable goes.
      } else {
        SIList<Sequence> candidates = componentManager.getSequencesForType(mutationOp, i, false);

        if (candidates.isEmpty()) {
          // No sequence can satisfy this parameter - abort mutation.
          return new VarAndSeq(variable, sequence);
        }

        // TODO: Use Randoop's input selection strategy instead of uniform random.
        Sequence candidateSeq = Randomness.randomMember(candidates);
        Variable candidateVar = candidateSeq.randomVariableForTypeLastStatement(formalType, false);
        if (candidateVar == null) {
          // No variable of the required type in the candidate sequence.
          return new VarAndSeq(variable, sequence);
        }

        Type candType = candidateVar.getType();
        if (!formalType.isAssignableFrom(candType)) {
          // The candidate variable's type does not match the formal type.
          return new VarAndSeq(variable, sequence);
        }

        sequencesToConcat.add(candidateSeq);
        varIndicesInEachSeq.add(candidateVar.index);
      }
    }

    Sequence concatenated = Sequence.concatenate(sequencesToConcat);

    // Precompute offsets of each block within the concatenated sequence.
    int paramCount = formalTypes.size();
    int[] offsets = new int[paramCount];
    int acc = 0;
    for (int i = 0; i < paramCount; i++) {
      offsets[i] = acc;
      acc += sequencesToConcat.get(i).size();
    }

    // Map indices from individual sequences to the concatenated one.
    List<Variable> inputsForMutation = new ArrayList<>(paramCount);
    Variable updatedVariable = null;
    for (int i = 0; i < paramCount; i++) {
      int localIndex = varIndicesInEachSeq.get(i);
      int globalIndex = offsets[i] + localIndex;
      Variable v = concatenated.getVariable(globalIndex);
      inputsForMutation.add(v);
      if (i == targetParamPos) {
        updatedVariable = v;
      }
    }

    if (updatedVariable == null) {
      throw new RandoopBug(
          "Target variable was not found in the concatenated sequence. This should not happen.");
    }

    Sequence mutationSeq = concatenated.extend(mutationOp, inputsForMutation);
    return new VarAndSeq(updatedVariable, mutationSeq);
  }

  /**
   * Check preconditions for fuzzing a sequence. This method is called before fuzzing to ensure the
   * sequence and variable to fuzz are valid.
   *
   * @param sequence the sequence to fuzz
   * @param variable the variable to fuzz
   * @throws IllegalArgumentException if the sequence is null or empty
   * @throws RandoopBug if the component manager or target variable is not set, or if the target
   *     variable is not part of the sequence to fuzz
   */
  @EnsuresNonNull({"componentManager"})
  @SuppressWarnings("ReferenceEquality")
  private void checkPreconditions(Sequence sequence, Variable variable) {
    if (sequence == null) {
      throw new IllegalArgumentException("Sequence cannot be null");
    }
    if (sequence.size() == 0) {
      throw new IllegalArgumentException("Cannot fuzz an empty Sequence");
    }
    if (componentManager == null) {
      throw new RandoopBug("Component manager is not set. Initialize the fuzzer before fuzzing.");
    }
    if (variable == null) {
      throw new RandoopBug("Variable to fuzz is null.");
    }
    if (variable.sequence == null) {
      throw new RandoopBug("Variable to fuzz has no sequence set.");
    }
    if (variable.sequence != sequence) {
      throw new RandoopBug(
          "Variable to fuzz is not part of the sequence to fuzz. "
              + "Variable sequence: "
              + variable.sequence
              + ", sequence to fuzz: "
              + sequence);
    }
  }

  /**
   * Returns a list of operations that can be applied to the given type.
   *
   * @param t the type to check for applicable operations
   * @return a list of operations that can be applied to the type; may be an empty list
   */
  private List<TypedOperation> getApplicableOps(Type t) {
    Type root = erase(t);
    return typeToUnion.computeIfAbsent(
        root,
        k -> {
          // Preserve insertion order & dedup
          java.util.LinkedHashSet<TypedOperation> set = new java.util.LinkedHashSet<>();
          for (Type a : supertypes(root)) {
            List<TypedOperation> list = mutatorsByType.get(a);
            if (list != null) {
              set.addAll(list);
            }
          }
          return new ArrayList<>(set);
        });
  }

  /**
   * Select a mutation operation that can be applied to the type to fuzz.
   *
   * @param typeToFuzz the type of the variable to fuzz
   * @return a randomly selected mutation operation that can be applied to the type to fuzz, or null
   *     if no applicable operation is found
   */
  private @Nullable TypedOperation selectMutationOperation(Type typeToFuzz) {
    List<TypedOperation> applicable = getApplicableOps(typeToFuzz);
    return applicable.isEmpty() ? null : Randomness.randomMember(applicable);
  }

  /**
   * Selects a parameter position of the given type in the mutation operation where the target
   * variable will be used.
   *
   * <p>This selection ensures the target variable is actually used in the mutation by guaranteeing:
   *
   * <ol>
   *   <li>At least one parameter position matches the target variable's type
   *   <li>The target variable's sequence is incorporated as input for the mutation
   * </ol>
   *
   * Without this selection, we might generate mutations that don't exercise the target variable.
   *
   * @param formalTypes the formal parameter types of the mutation operation
   * @param typeToFuzz the type of the target variable to fuzz
   * @param mutationOp the operation being mutated (used for error reporting)
   * @return the index of the selected parameter position
   */
  private int selectFuzzParameter(
      TypeTuple formalTypes, Type typeToFuzz, TypedOperation mutationOp) {
    List<Integer> candidateParamPositions = new ArrayList<>(2);
    for (int i = 0; i < formalTypes.size(); i++) {
      if (formalTypes.get(i).isAssignableFrom(typeToFuzz)) {
        candidateParamPositions.add(i);
      }
    }

    if (candidateParamPositions.isEmpty()) {
      throw new RandoopBug("No candidate positions found for " + typeToFuzz + " in " + mutationOp);
    }

    return Randomness.randomMember(candidateParamPositions);
  }

  /**
   * Returns an erased (raw) type. Returns the type unchanged if it is not a parameterized
   * class/interface.
   *
   * @param t the type to erase
   * @return the erased type
   */
  private Type erase(Type t) {
    Type raw = t.getRawtype(); // Randoop's API: raw type for generics
    return (raw != null) ? raw : t;
  }

  /**
   * Performs a breadth-first traversal over the given type and its supertypes. Returns the erased
   * form of each in this order: first the erasure of the argument `t`, then its immediate
   * superclass and interfaces, then their supertypes. Duplicates are removed while preserving
   * order.
   *
   * @param t the starting type
   * @return a list of erased types including {@code t} and all ancestors
   */
  private List<Type> supertypes(Type t) {
    Type start = erase(t);

    // Non-class/interface types (primitives, arrays) have no ancestors.
    if (!(start instanceof ClassOrInterfaceType)) {
      List<Type> singleton = new ArrayList<>(1);
      singleton.add(start);
      return singleton;
    }

    java.util.Deque<Type> queue = new java.util.ArrayDeque<>();
    java.util.LinkedHashSet<Type> result = new java.util.LinkedHashSet<>();

    queue.add(start);
    while (!queue.isEmpty()) {
      Type next = erase(queue.removeFirst());
      if (!result.add(next)) {
        continue; // already processed
      }
      if (next instanceof ClassOrInterfaceType) {
        ClassOrInterfaceType ci = (ClassOrInterfaceType) next;
        ClassOrInterfaceType sup = ci.getSuperclass();
        if (sup != null) {
          queue.addLast(erase(sup));
        }
        for (Type itf : ci.getInterfaces()) {
          if (itf != null) {
            queue.addLast(erase(itf));
          }
        }
      }
    }
    return new ArrayList<>(result);
  }
}
