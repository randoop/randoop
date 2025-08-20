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
 * Fuzzer that applies a single side‐effecting operation to a variable within a test sequence to
 * explore the stateful behavior (impurity) of that object.
 *
 * <p>Specifically, this fuzzer:
 *
 * <ol>
 *   <li>Randomly picks one impure method whose signature includes the target’s type.
 *   <li>Randomly chooses which parameter slot to supply the target into.
 *   <li>Fills the other slots by pulling sequences from the ComponentManager’s sequence collection.
 *   <li>Appends the new call to the sequence.
 * </ol>
 */
public final class GrtObjectFuzzer extends GrtFuzzer {
  /** Singleton instance. */
  private static final GrtObjectFuzzer INSTANCE = new GrtObjectFuzzer();

  /**
   * Cache mapping types to lists of operations that can be applied to mutate values of that type.
   * None of the operations in the map are annotated with
   * {@code @org.checkerframework.dataflow.qual.Pure} or
   * {@code @org.checkerframework.dataflow.qual.SideEffectFree}.
   */
  private final Map<Type, List<TypedOperation>> operationsByType = new HashMap<>();

  /** Component manager to get sequences for types. */
  private @MonotonicNonNull ComponentManager componentManager;

  /** Whether this fuzzer has been initialized. */
  private boolean initialized = false;

  /** Cache of resolved unions so we don't recompute ancestor walks. */
  private final Map<Type, List<TypedOperation>> unionCache = new HashMap<>();

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
   * This is based on the expectation that each Randoop run has already collected all the
   * side-effecting operations and the component manager should not change during the run.
   *
   * @param sideEffectOps a set of side-effecting operations to add to the fuzzer (not annotated
   *     with Checker Framework's {@code @Pure} or {@code @SideEffectFree})
   * @param cm the component manager to use for getting sequences for types (should not be null)
   */
  public void initializeIfNeeded(Set<TypedOperation> sideEffectOps, ComponentManager cm) {
    if (initialized) {
      return; // Already initialized, no need to do it again.
    }
    addOperations(sideEffectOps);
    this.componentManager = cm;
    initialized = true;
  }

  @Override
  public boolean canFuzz(Type type) {
    return !type.isNonreceiverType();
  }

  /**
   * Indexes side-effecting operations by input type for the fuzzer.
   *
   * <p>TODO: Currently, this method index each operation only under its exact formal types. When
   * fuzzing a subtype, lookups should also union operations from the subtype’s
   * supertypes/interfaces to find more applicable operations, especially in code paths known to be
   * annotated.
   *
   * @param operations a set of operations to index, all containing side effects (not annotated with
   *     Checker Framework's {@code @Pure} or {@code @SideEffectFree})
   * @throws IllegalArgumentException if the operation set is null
   */
  private void addOperations(Set<TypedOperation> operations) {
    if (operations == null) {
      throw new IllegalArgumentException("Operations list cannot be null");
    }

    // Build the type-to-operations map, for quick access later.
    for (TypedOperation op : operations) {
      TypeTuple inputTypes = op.getInputTypes();
      for (int i = 0; i < inputTypes.size(); i++) {
        Type type = inputTypes.get(i);
        type = erase(type);
        operationsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(op);
      }
    }
  }

  @Override
  public VarAndSeq fuzz(Sequence sequence, Variable variable) {
    checkPreconditions(sequence, variable);

    Type typeToFuzz = variable.getType();
    TypedOperation mutationOp = selectMutationOperation(typeToFuzz);
    if (mutationOp == null) {
      // No applicable operation for this type—return the original sequence unchanged.
      return new VarAndSeq(variable, sequence);
    }

    TypeTuple formals = mutationOp.getInputTypes();
    int fuzzParamPos = selectFuzzParameterPosition(formals, typeToFuzz, mutationOp);

    // Keep track of the sequences to concatenate and the index of the necessary variable in each.
    List<Sequence> sequencesToConcat = new ArrayList<>(formals.size());
    List<Integer> varIndicesInEachSeq = new ArrayList<>(formals.size());
    int targetParamPos = -1; // Initialize to an invalid position.

    // Collect input sequences for each formal parameter.
    for (int i = 0; i < formals.size(); i++) {
      Type formalType = formals.get(i);
      if (formalType.isAssignableFrom(typeToFuzz) && i == fuzzParamPos) {
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
          return new VarAndSeq(variable, sequence);
        }

        sequencesToConcat.add(candidateSeq);
        varIndicesInEachSeq.add(candidateVar.index);
      }
    }

    Sequence concatenated = Sequence.concatenate(sequencesToConcat);

    // Precompute offsets of each block within the concatenated sequence.
    int paramCount = formals.size();
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
      throw new RandoopBug(
          "Component manager is not set. Initialize the fuzzer with a component manager before fuzzing.");
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
   * @return a list of operations that can be applied to the type, or an empty list if no operations
   *     are applicable
   */
  private List<TypedOperation> getApplicableOps(Type t) {
    Type root = erase(t);
    return unionCache.computeIfAbsent(
        root,
        k -> {
          // Preserve insertion order & dedup
          java.util.LinkedHashSet<TypedOperation> set = new java.util.LinkedHashSet<>();
          for (Type a : ancestorsAndSelf(root)) {
            List<TypedOperation> list = operationsByType.get(a);
            if (list != null) set.addAll(list);
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
   * @param formals the formal parameter types of the mutation operation
   * @param typeToFuzz the type of the target variable to fuzz
   * @param mutationOp the operation being mutated (used for error reporting)
   * @return the index of the selected parameter position
   * @throws RandoopBug if no compatible parameter positions exist (indicating improper operation
   *     filtering)
   */
  private int selectFuzzParameterPosition(
      TypeTuple formals, Type typeToFuzz, TypedOperation mutationOp) {
    List<Integer> candidateParamPositions = new ArrayList<>();
    for (int i = 0; i < formals.size(); i++) {
      if (formals.get(i).isAssignableFrom(typeToFuzz)) {
        candidateParamPositions.add(i);
      }
    }

    // No candidate positions found for the type to fuzz.
    // This should not happen, as we have already checked that the operation is applicable to the
    // type to fuzz.
    if (candidateParamPositions.isEmpty()) {
      throw new RandoopBug(
          "No candidate positions found for the type "
              + typeToFuzz
              + " in the operation "
              + mutationOp
              + ". This should not happen.");
    }
    return Randomness.randomMember(candidateParamPositions);
  }

  /**
   * Returns a canonical/erased view of a type. For parameterized class/interface types, this
   * returns the raw type; otherwise it returns the type unchanged.
   *
   * @param t the type to erase (non-null)
   * @return the erased/canonical type
   */
  private Type erase(Type t) {
    if (t == null) {
      throw new IllegalArgumentException("type cannot be null");
    }
    Type raw = t.getRawtype(); // Randoop's API: raw type for generics
    return (raw != null) ? raw : t;
  }

  /**
   * Performs a breadth-first traversal over the given type and its ancestors (superclasses and
   * interfaces), returning the erased form of each in order.
   *
   * <p>Order: the starting type first, then superclass, then interfaces, then their ancestors.
   * Duplicates are removed while preserving order.
   *
   * @param t the starting type
   * @return a list of erased types including {@code t} and all ancestors
   */
  private List<Type> ancestorsAndSelf(Type t) {
    Type start = erase(t);

    // Non-class/interface types (primitives, arrays) have no ancestors.
    if (!(start instanceof ClassOrInterfaceType)) {
      List<Type> singleton = new ArrayList<>(1);
      singleton.add(start);
      return singleton;
    }

    java.util.Deque<Type> queue = new java.util.ArrayDeque<>();
    java.util.LinkedHashSet<Type> visited = new java.util.LinkedHashSet<>();

    queue.add(start);
    while (!queue.isEmpty()) {
      Type next = erase(queue.removeFirst());
      if (!visited.add(next)) {
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

    System.out.println("Ancestors of " + t + ": " + visited);

    return new ArrayList<>(visited);
  }
}
