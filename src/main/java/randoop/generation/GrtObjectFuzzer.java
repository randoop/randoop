package randoop.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.plumelib.util.SIList;
import randoop.main.RandoopBug;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
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
   * All operations in the map are annotated with
   * {@code @org.checkerframework.dataflow.qual.Impure}.
   */
  private final Map<Type, List<TypedOperation>> operationsByType = new HashMap<>();

  /** Component manager to get sequences for types. */
  private @MonotonicNonNull ComponentManager componentManager;

  /** Variable that we are going to fuzz. */
  private @MonotonicNonNull Variable targetVariable;

  /** Whether this fuzzer has been initialized. */
  private boolean initialized = false;

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
   * @param sideEffectOps a set of side-effecting operations to add to the fuzzer (annotated with
   *     Checker Framework's {@code @Impure})
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

  /**
   * Set the variable to fuzz.
   *
   * @param targetVariable the variable to fuzz, not null
   */
  public void setTargetVariable(Variable targetVariable) {
    this.targetVariable = targetVariable;
  }

  @Override
  public boolean canFuzz(Type type) {
    return !type.isNonreceiverType();
  }

  /**
   * Indexes side-effecting operations by input type for the fuzzer.
   *
   * @param operations a set of operations to index, all containing side effects (annotated with
   *     Checker Framework's {@code @Impure})
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
        operationsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(op);
      }
    }
  }

  @Override
  public Sequence fuzz(Sequence sequence) {
    checkPreconditions(sequence);
    Type typeToFuzz = targetVariable.getType();
    TypedOperation mutationOp = selectMutationOperation(typeToFuzz);
    if (mutationOp == null) {
      // No applicable operation found for the type to fuzz. Return the original sequence.
      return sequence;
    }

    // Input variables for the mutation operation.
    List<Variable> inputVarsForMutOp = new ArrayList<>();

    // Formal parameters of the mutation operation.
    TypeTuple formals = mutationOp.getInputTypes();

    int fuzzParamPos = selectFuzzParameterPosition(formals, mutationOp);

    // Start from a copy of the original sequence and append the new sequence to it.
    // This allows us to conveniently know the index of the variable to fuzz in the new sequence
    // without calculating the index offset of the variable to be fuzzed in the new sequence.
    Sequence newSequence = Sequence.concatenate(sequence, new Sequence());

    // Find all input sequences needed to satisfy the formals of the mutation operation.
    for (int i = 0; i < formals.size(); i++) {
      Type formalT = formals.get(i);
      if (formalT.equals(typeToFuzz) && i == fuzzParamPos) {
        // Add variable to fuzz to the input variables for the mutation operation.
        inputVarsForMutOp.add(targetVariable);
      } else {
        // Find a sequence from the sequence collection that produces a value of this formal type.
        SIList<Sequence> fuzzTypeSequences =
            componentManager.getSequencesForType(mutationOp, i, false);

        if (fuzzTypeSequences.isEmpty()) {
          return sequence; // no sequences found for this type -> abort mutation
        }

        // TODO: We could use Randoop's input selection strategy here instead of
        //  always selecting a random sequence uniformly.
        Sequence inputSequence = Randomness.randomMember(fuzzTypeSequences);
        Variable randomVariable = inputSequence.randomVariableForTypeLastStatement(formalT, false);
        int prevSize = newSequence.size();
        newSequence = Sequence.concatenate(newSequence, inputSequence);
        inputVarsForMutOp.add(newSequence.getVariable(prevSize + randomVariable.index));
      }
    }

    remapOwners(inputVarsForMutOp, newSequence);

    return newSequence.extend(mutationOp, inputVarsForMutOp);
  }

  /**
   * Check preconditions for fuzzing a sequence. This method is called before fuzzing to ensure the
   * sequence and variable to fuzz are valid.
   *
   * @param seq the sequence to fuzz
   */
  @EnsuresNonNull({"targetVariable", "componentManager"})
  @SuppressWarnings("ReferenceEquality")
  private void checkPreconditions(Sequence seq) {
    if (seq == null) {
      throw new IllegalArgumentException("Sequence cannot be null");
    }
    if (seq.size() == 0) {
      throw new IllegalArgumentException("Cannot fuzz an empty Sequence");
    }
    if (componentManager == null) {
      throw new RandoopBug(
          "Component manager is not set. "
              + "This should not happen, as the fuzzer should be initialized with a component manager.");
    }
    if (targetVariable == null) {
      throw new RandoopBug(
          "Target variable to fuzz is not set. "
              + "This should not happen, as the fuzzer should have a target variable set before fuzzing.");
    }
    if (targetVariable.sequence == null) {
      throw new RandoopBug(
          "Variable to fuzz has no sequence set. "
              + "This should not happen, as the variable should be part of a sequence.");
    }
    if (targetVariable.sequence != seq) {
      throw new RandoopBug(
          "Variable to fuzz is not part of the sequence to fuzz. "
              + "Variable sequence: "
              + targetVariable.sequence
              + ", sequence to fuzz: "
              + seq);
    }
  }

  /**
   * Select a mutation operation that can be applied to the type to fuzz.
   *
   * @param typeToFuzz the type of the variable to fuzz
   * @return a randomly selected mutation operation that can be applied to the type to fuzz, or null
   *     if no applicable operation is found
   */
  private @Nullable TypedOperation selectMutationOperation(Type typeToFuzz) {
    List<TypedOperation> applicableOperations = operationsByType.get(typeToFuzz);

    // No applicable operations for this type. Return the original sequence.
    if (applicableOperations == null || applicableOperations.isEmpty()) {
      return null;
    }

    return Randomness.randomMember(applicableOperations);
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
   * Without this selection, we might generate mutations that don't actually exercise the target
   * variable.
   *
   * @param formals the formal parameter types of the mutation operation
   * @param mutationOp the operation being mutated (used for error reporting)
   * @return the index of the selected parameter position
   * @throws RandoopBug if no compatible parameter positions exist (indicating improper operation
   *     filtering)
   */
  @RequiresNonNull("targetVariable")
  private int selectFuzzParameterPosition(TypeTuple formals, TypedOperation mutationOp) {
    List<Integer> candidateParamPositions = new ArrayList<>();
    for (int i = 0; i < formals.size(); i++) {
      if (formals.get(i).equals(targetVariable.getType())) {
        candidateParamPositions.add(i);
      }
    }

    // No candidate positions found for the type to fuzz.
    // This should not happen, as we have already checked that the operation is applicable to the
    // type to fuzz.
    if (candidateParamPositions.isEmpty()) {
      throw new RandoopBug(
          "No candidate positions found for the type "
              + targetVariable.getType()
              + " in the operation "
              + mutationOp
              + ". This should not happen.");
    }
    return Randomness.randomMember(candidateParamPositions);
  }

  /**
   * Remap the owners of the variables in {@code inputVarsForMutOp} to the new sequence.
   *
   * @param inputVarsForMutOp the list of input variables for the mutation operation
   * @param newSequence the new sequence that will contain the mutation operation
   */
  @SuppressWarnings("ReferenceEquality")
  private void remapOwners(List<Variable> inputVarsForMutOp, Sequence newSequence) {
    for (int i = 0; i < inputVarsForMutOp.size(); i++) {
      Variable v = inputVarsForMutOp.get(i);
      if (v.sequence == null) {
        throw new RandoopBug(
            "Variable "
                + v
                + " has no sequence set. This should not happen, as the variable should be part of a sequence.");
      }
      if (v.sequence != newSequence) {
        inputVarsForMutOp.set(i, newSequence.getVariable(v.index));
      }
    }
  }
}
