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
   * Initialize this fuzzer with side-effecting operations and a component manager.
   *
   * @param mutators side-effecting operations used as mutators
   * @param cm the component manager used to obtain sequences for required types
   */
  public void initialize(Set<TypedOperation> mutators, ComponentManager cm) {
    // Build the type-to-mutators map, for quick access later.
    for (TypedOperation op : mutators) {
      TypeTuple inputTypes = op.getInputTypes();
      for (int i = 0; i < inputTypes.size(); i++) {
        Type type = inputTypes.get(i).getRawtype();
        mutatorsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(op);
      }
    }
    this.componentManager = cm;
  }

  @Override
  public boolean canFuzz(Type type) {
    return !type.isNonreceiverType();
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

    // Collect input sequences for each formal parameter.
    for (int i = 0; i < formalTypes.size(); i++) {
      Type formalType = formalTypes.get(i);
      if (i == fuzzParam) {
        // Use the current sequence's variable for the selected fuzz parameter.
        sequencesToConcat.add(sequence);
        varIndicesInEachSeq.add(variable.index);
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
      if (i == fuzzParam) {
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
   * Returns all side-effecting operations applicable to {@code t}.
   *
   * @param t the (possibly generic) type to check
   * @return a deduplicated list of applicable operations; may be empty
   */
  private List<TypedOperation> getApplicableOps(Type t) {
    Type erased = t.getRawtype();
    return typeToUnion.computeIfAbsent(
        erased,
        k -> {
          // Deduplicate while preserving insertion order (stable, reproducible order).
          java.util.LinkedHashSet<TypedOperation> union = new java.util.LinkedHashSet<>();
          if (t instanceof ClassOrInterfaceType) {
            // Include the type itself and all supertypes (each erased before lookup).
            for (ClassOrInterfaceType anc :
                ((ClassOrInterfaceType) t).getAllSupertypesInclusive()) {
              List<TypedOperation> ops = mutatorsByType.get(anc.getRawtype());
              if (ops != null) {
                union.addAll(ops);
              }
            }
          } else {
            // Array/primitive guard: only consider the erased type itself.
            List<TypedOperation> ops = mutatorsByType.get(erased);
            if (ops != null) {
              union.addAll(ops);
            }
          }
          return new ArrayList<>(union);
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
   * Chooses the index of a parameter in {@code mutationOp} whose type can accept the target
   * variable (of {@code typeToFuzz}). A random compatible index is returned if multiple exist.
   *
   * @param formalTypes the formal parameter types of {@code mutationOp}
   * @param typeToFuzz the type of the target variable to pass to the operation
   * @param mutationOp the operation whose parameter is being selected (for diagnostics)
   * @return the index of a compatible parameter position
   * @throws RandoopBug if no parameter type is compatible with {@code typeToFuzz}
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
}
