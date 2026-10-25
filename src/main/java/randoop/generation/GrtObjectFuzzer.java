package randoop.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
 * <p>Specifically, this fuzzer takes a sequence and a variable within that sequence, and then:
 *
 * <ol>
 *   <li>Randomly picks one side-effecting method whose signature includes the variable type.
 *   <li>Randomly chooses which parameter slot to supply the variable into (if there are multiple
 *       possibilities).
 *   <li>Fills the other slots from the ComponentManager's sequence collection.
 *   <li>Appends the new call to the sequence.
 * </ol>
 */
public final class GrtObjectFuzzer extends GrtFuzzer {
  /** The singleton instance. */
  private static final GrtObjectFuzzer INSTANCE = new GrtObjectFuzzer();

  /** Maps RAW type to mutating operations that have a parameter of that type. */
  private final Map<Type, List<TypedOperation>> rawTypeToSideEffectingOps = new HashMap<>();

  /** Component manager to get sequences for types. */
  private @MonotonicNonNull ComponentManager componentManager;

  /**
   * Cache of candidate mutator operations keyed by raw type.
   *
   * <p>Each entry is a coarse superset of mutators collected from the raw-type index (includes
   * operations declared on the type or any of its supertypes). This avoids re-walking the
   * supertypes for repeated queries; full type/generic compatibility is checked later when an
   * operation is selected.
   */
  private final Map<Type, List<TypedOperation>> typeToApplicableOps = new HashMap<>();

  /** How to select sequences as inputs for creating new sequences. */
  private @MonotonicNonNull InputSequenceSelector inputSequenceSelector;

  /**
   * Get the singleton instance of {@link GrtObjectFuzzer}.
   *
   * @return the singleton instance
   */
  public static GrtObjectFuzzer getInstance() {
    return INSTANCE;
  }

  /** Creates a GrtObjectFuzzer. The client must call {@link #initialize}. */
  // This constructor is private to to enforce the singleton pattern.
  private GrtObjectFuzzer() {
    // nothing to do; `initialize()` does the initialization.
  }

  /**
   * Initialize this fuzzer with side-effecting operations and a component manager.
   *
   * @param mutators side-effecting operations used as mutators
   * @param cm the component manager used to obtain sequences for required types
   * @param selector strategy for choosing input sequences for the parameters that are not being
   *     fuzzed
   */
  public void initialize(
      Set<TypedOperation> mutators, ComponentManager cm, InputSequenceSelector selector) {
    if (this.componentManager != null) {
      throw new RandoopBug("Do not call initialize multiple times.");
    }
    // Build the type-to-mutators map, for quick access later.
    for (TypedOperation op : mutators) {
      TypeTuple inputTypes = op.getInputTypes();
      for (int i = 0; i < inputTypes.size(); i++) {
        Type type = inputTypes.get(i).getRawtype();
        rawTypeToSideEffectingOps.computeIfAbsent(type, k -> new ArrayList<>()).add(op);
      }
    }
    this.componentManager = cm;
    this.inputSequenceSelector = selector;
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

    TypeTuple paramTypes = mutationOp.getInputTypes();
    int paramCount = paramTypes.size();
    int fuzzParam = selectFuzzParameter(paramTypes, typeToFuzz);

    // Each sequence provides one argument to `mutationOp`.  One of them (the one for `fuzzParam`)
    // is the original sequence.
    List<Sequence> sequencesToConcat = new ArrayList<>(paramCount);
    List<Integer> varIndicesInEachSeq = new ArrayList<>(paramCount);

    // Collect input sequences for each formal parameter.
    for (int i = 0; i < paramCount; i++) {
      Type paramType = paramTypes.get(i);
      if (i == fuzzParam) {
        // Use the current sequence's variable for the selected fuzz parameter.
        sequencesToConcat.add(sequence);
        varIndicesInEachSeq.add(variable.index);
      } else {
        // Choose a sequence from the component pool for the ith parameter.
        SIList<Sequence> candidates = componentManager.getSequencesForParam(mutationOp, i, false);

        if (candidates.isEmpty()) {
          // No existing sequence can satisfy this parameter -- abort mutation.
          return new VarAndSeq(variable, sequence);
        }

        Sequence candidateSeq = inputSequenceSelector.selectInputSequence(candidates);
        Variable candidateVar = candidateSeq.randomVariableForTypeLastStatement(paramType, false);
        if (candidateVar == null) {
          // The candidate sequence has no variable of the required type.
          return new VarAndSeq(variable, sequence);
        }

        sequencesToConcat.add(candidateSeq);
        varIndicesInEachSeq.add(candidateVar.index);
      }
    }

    Sequence concatenated = Sequence.concatenate(sequencesToConcat);

    // Map indices from individual sequences to the concatenated one.
    List<Variable> inputsForMutation = new ArrayList<>(paramCount);
    int offset = 0;
    Variable updatedVariable = null;
    for (int i = 0; i < paramCount; i++) {
      int globalIndex = offset + varIndicesInEachSeq.get(i);
      offset += sequencesToConcat.get(i).size();
      Variable v = concatenated.getVariable(globalIndex);
      inputsForMutation.add(v);
      if (i == fuzzParam) {
        updatedVariable = v;
      }
    }

    if (updatedVariable == null) {
      throw new RandoopBug("Target variable was not found in the concatenated sequence.");
    }

    Sequence mutationSeq = concatenated.extend(mutationOp, inputsForMutation);
    return new VarAndSeq(updatedVariable, mutationSeq);
  }

  /**
   * Check preconditions for fuzzing a sequence. Throw an exception if they are violated.
   *
   * @param sequence the sequence to fuzz
   * @param variable the variable to fuzz
   * @throws IllegalArgumentException if the sequence is null or empty
   * @throws RandoopBug if the component manager or target variable is not set, or if the target
   *     variable is not part of the sequence to fuzz
   */
  @EnsuresNonNull({"componentManager", "inputSequenceSelector"})
  @SuppressWarnings("ReferenceEquality")
  private void checkPreconditions(Sequence sequence, Variable variable) {
    if (sequence == null) {
      throw new IllegalArgumentException("Sequence cannot be null");
    }
    if (sequence.size() == 0) {
      throw new IllegalArgumentException("Cannot fuzz an empty Sequence");
    }

    if (variable == null) {
      throw new RandoopBug("Variable to fuzz is null");
    }
    if (variable.sequence == null) {
      throw new RandoopBug("Variable to fuzz (" + variable + ") has no sequence");
    }
    if (variable.sequence != sequence) {
      throw new RandoopBug(
          "Variable to fuzz is not part of the sequence to fuzz. "
              + "Variable sequence: "
              + variable.sequence
              + ", sequence to fuzz: "
              + sequence);
    }

    if (componentManager == null || inputSequenceSelector == null) {
      throw new RandoopBug("Fuzzer is not initialized");
    }
  }

  /**
   * Select a side-effecting operation whose signature contains the target's type. For
   * class/interface types, it collects mutators whose parameter type matches the target type or any
   * of its supertypes. Then one operation is chosen uniformly at random.
   *
   * @param typeToFuzz the type of the variable to fuzz
   * @return a randomly selected mutation operation that can be applied to the type to fuzz, or null
   *     if no applicable operation is found
   */
  private @Nullable TypedOperation selectMutationOperation(Type typeToFuzz) {
    if (typeToFuzz.isNonreceiverType()) {
      return null;
    }

    List<TypedOperation> applicableOps = computeApplicableOperations(typeToFuzz);
    if (applicableOps.isEmpty()) {
      return null;
    }

    // Refine raw-type superset: keep ops with at least one parameter where typeToFuzz is a
    // (generic) subtype.
    List<TypedOperation> compatibleOps = new ArrayList<>(applicableOps.size());
    for (TypedOperation op : applicableOps) {
      TypeTuple inputs = op.getInputTypes();
      for (int i = 0; i < inputs.size(); i++) {
        if (inputs.get(i).isAssignableFrom(typeToFuzz)) {
          compatibleOps.add(op);
          break;
        }
      }
    }

    return compatibleOps.isEmpty() ? null : Randomness.randomMember(compatibleOps);
  }

  /**
   * Chooses the index of a parameter in {@code mutationOp} for which {@code typeToFuzz} is a
   * subtype of the parameter type. If multiple exist, one is chosen at random.
   *
   * @param paramTypes the formal parameter types of {@code mutationOp}
   * @param typeToFuzz the type of the target variable to pass to the operation
   * @return the index of a parameter whose type is a supertype of {@code typeToFuzz}
   */
  private int selectFuzzParameter(TypeTuple paramTypes, Type typeToFuzz) {
    List<Integer> candidateParamPositions = new ArrayList<>(2);
    for (int i = 0; i < paramTypes.size(); i++) {
      if (paramTypes.get(i).isAssignableFrom(typeToFuzz)) {
        candidateParamPositions.add(i);
      }
    }
    return Randomness.randomMember(candidateParamPositions);
  }

  /**
   * Computes and caches the list of operations applicable to a given type. For class/interface
   * types, this includes operations that accept the type or any of its supertypes. For array types,
   * only operations accepting the exact raw type are included.
   *
   * @param typeToFuzz the type being fuzzed
   * @return a list of operations applicable to the given type
   */
  private List<TypedOperation> computeApplicableOperations(Type typeToFuzz) {
    Type rawType = typeToFuzz.getRawtype();
    List<TypedOperation> applicableOps = typeToApplicableOps.get(rawType);
    if (applicableOps == null) {
      // Deduplicate while preserving insertion order.
      LinkedHashSet<TypedOperation> opsSet = new LinkedHashSet<>();
      if (typeToFuzz instanceof ClassOrInterfaceType) {
        // Include the type itself and all supertypes.
        for (ClassOrInterfaceType ancestor :
            ((ClassOrInterfaceType) typeToFuzz).getAllSupertypesInclusive()) {
          List<TypedOperation> ops = rawTypeToSideEffectingOps.get(ancestor.getRawtype());
          if (ops != null) {
            opsSet.addAll(ops);
          }
        }
      } else {
        // Not a ClassOrInterfaceType, so it as an array: only consider the raw type itself.
        List<TypedOperation> ops = rawTypeToSideEffectingOps.get(rawType);
        if (ops != null) {
          opsSet.addAll(ops);
        }
      }
      applicableOps = new ArrayList<>(opsSet);
      typeToApplicableOps.put(rawType, applicableOps);
    }
    return applicableOps;
  }
}
