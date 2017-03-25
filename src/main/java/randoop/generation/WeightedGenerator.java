package randoop.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import randoop.BugInRandoopException;
import randoop.SubTypeSet;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.InstantiatedType;
import randoop.types.JDKTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.ArrayListSimpleList;
import randoop.util.ListOfLists;
import randoop.util.Log;
import randoop.util.MultiMap;
import randoop.util.Randomness;
import randoop.util.SimpleList;
import randoop.util.WeightedElement;

/**
 * WeightedGenerator extends the functionality of forward generator by storing additional
 * information necessary for the weighted random selection of input sequences. This has sequence and
 * constant weighting schemes, which are used through the command-line flags <code>
 * --weighted-sequences</code> and <code>--weighted-constants</code>. Note that the <code>
 * --small-flags</code> uses Sequence's static weighting scheme, but that the use of <code>
 * --weighted-sequences</code> and/or <code>--weighted-constants</code> overrides <code>
 * --small-flags</code>.
 */
public class WeightedGenerator extends ForwardGenerator {

  /** Map of sequences to their weights. */
  private final Map<WeightedElement, Double> weightMap = new HashMap<>();

  /** Map of sequences to the number of times they've been executed. */
  private final Map<WeightedElement, Integer> sequenceExecutionNumber = new HashMap<>();

  /**
   * Map of sequence/constants to their constant weights. Note that these weights are never changed
   * once initialized.
   */
  private final Map<Sequence, Double> constantWeights = new HashMap<>();

  /** Set of all executed sequences */
  private final Set<Sequence> executedSequences = new HashSet<>();

  public WeightedGenerator(
      List<TypedOperation> operations,
      Set<TypedOperation> observers,
      long timeMillis,
      int maxGenSequences,
      int maxOutSequences,
      ComponentManager componentManager,
      RandoopListenerManager listenerManager,
      int numClasses,
      Map<Sequence, Integer> tfFrequency) {
    this(
        operations,
        observers,
        timeMillis,
        maxGenSequences,
        maxOutSequences,
        componentManager,
        null,
        listenerManager,
        numClasses,
        tfFrequency);
  }

  public WeightedGenerator(
      List<TypedOperation> operations,
      Set<TypedOperation> observers,
      long timeMillis,
      int maxGenSequences,
      int maxOutSequences,
      ComponentManager componentManager,
      IStopper stopper,
      RandoopListenerManager listenerManager,
      int numClasses,
      Map<Sequence, Integer> sequenceTermFrequencies) {

    super(
        operations,
        observers,
        timeMillis,
        maxGenSequences,
        maxOutSequences,
        componentManager,
        stopper,
        listenerManager);

    // --weighted-constants weighting scheme for initial mined sequences, based on the TF-IDF formula
    if (GenInputsAbstract.weighted_constants) {

      int totalNumConstants = 0;
      for (Sequence s : sequenceTermFrequencies.keySet()) {
        totalNumConstants += sequenceTermFrequencies.get(s);
      }
      assert componentManager instanceof WeightedComponentManager;
      for (Map.Entry<Sequence, Integer> m :
          ((WeightedComponentManager) componentManager).getSequenceFrequency().entrySet()) {

        // note that this is adjusting the tf(t,d) by normalizing it across the sum of all sequences' tf(t,d)
        // TODO: explore performance with unnormalized tf(t,d), as well as inter-weight tuning
        double weight =
            ((double) sequenceTermFrequencies.get(m.getKey()) / totalNumConstants)
                * Math.log((double) (numClasses + 1) / ((numClasses + 1) - m.getValue()));
        constantWeights.put(m.getKey(), weight);
      }
    }
  }

  /**
   * Should only be called once we're done with executing tests.
   *
   * @return a set of all executed sequences
   */
  public Set<Sequence> getExecutedSequences() {
    return executedSequences;
  }

  /**
   * The runtimePrimitivesSeen set contains primitive values seen during generation/execution and is
   * used to determine new values that should be added to the component set. The component set
   * initially contains a set of primitive sequences; this method puts those primitives in this set.
   * Also introduces weighted selection based on the runtime of the sequence if the weighted flags
   * are enabled.
   */
  @Override
  public ExecutableSequence step() {

    long startTime = System.nanoTime();

    if (componentManager.numGeneratedSequences() % GenInputsAbstract.clear == 0) {
      componentManager.clearGeneratedSequences();
    }

    ExecutableSequence eSeq = createNewUniqueSequence();

    if (eSeq == null) {
      return null;
    }

    if (GenInputsAbstract.dontexecute) {
      this.componentManager.addGeneratedSequence(eSeq.sequence);
      return null;
    }

    setCurrentSequence(eSeq.sequence);

    long endTime = System.nanoTime();
    long gentime = endTime - startTime;
    startTime = endTime; // reset start time.

    eSeq.execute(executionVisitor, checkGenerator);

    endTime = System.nanoTime();

    eSeq.exectime = endTime - startTime;

    double initialWeight =
        eSeq.sequence.getWeight(); // use a sequence's default weight as the initial value
    double weightedSequencesWeight;
    double weightedConstantsWeight;
    double weight = initialWeight; // final weight used in the actual weighted random selection

    // TODO: explore fine-tuning of weight interactions. Specifically, magnitudes between weightedConstant and
    // TODO: weightedSequences are drastic.  This affects the selection in the global pool, where there can be some
    // TODO: sequences with only weightedConstant weights, which are extremely small.

    // --weighted-sequences weighting scheme
    if (GenInputsAbstract.weighted_sequences) {

      // track # times a sequence has been executed
      if (sequenceExecutionNumber.containsKey(eSeq.sequence)) {
        sequenceExecutionNumber.put(eSeq.sequence, sequenceExecutionNumber.get(eSeq.sequence) + 1);
      } else {
        sequenceExecutionNumber.put(eSeq.sequence, 1);
      }

      // --weighted-sequences weight formula
      weightedSequencesWeight =
          1.0
              / (eSeq.exectime
                  * sequenceExecutionNumber.get(eSeq.sequence)
                  * Math.sqrt(eSeq.sequence.size()));
      // layer it on top
      weight *= weightedSequencesWeight;
    }

    // applying --weighted-constants weighting scheme
    if (GenInputsAbstract.weighted_constants) {
      // apply only if it exists. since not all sequences can be initial weighted constants, we often ignore them
      if (constantWeights.containsKey(eSeq.sequence)) {
        weightedConstantsWeight = constantWeights.get(eSeq.sequence);
        // layer it on top
        weight *= weightedConstantsWeight;
      }
    }

    assert weight >= 0;
    assert eSeq.sequence != null;

    weightMap.put(eSeq.sequence, weight); // add a weight no matter what

    if (GenInputsAbstract.output_sequence_info) {
      // add it to our growing set of executed sequences if not already added
      if (!executedSequences.contains(eSeq.sequence)) {
        executedSequences.add(eSeq.sequence);
      }
    }

    startTime = endTime; // reset start time.
    processSequence(eSeq);

    if (eSeq.sequence.hasActiveFlags()) {
      componentManager.addGeneratedSequence(eSeq.sequence);
    }

    endTime = System.nanoTime();
    gentime += endTime - startTime;
    eSeq.gentime = gentime;

    return eSeq;
  }

  // This method is responsible for doing two things:
  //
  // 1. Selecting at random a collection of sequences that can be used to
  // create input values for the given statement, and
  //
  // 2. Selecting at random valid indices to the above sequence specifying
  // the values to be used as input to the statement.
  //
  // The selected sequences and indices are wrapped in an InputsAndSuccessFlag
  // object and returned. If an appropriate collection of sequences and indices
  // was not found (e.g. because there are no sequences in the componentManager
  // that create values of some type required by the statement), the success
  // flag
  // of the returned object is false.
  @SuppressWarnings("unchecked")
  protected InputsAndSuccessFlag selectInputs(TypedOperation operation) {

    // Variable inputTypes contains the values required as input to the
    // statement given as a parameter to the selectInputs method.

    TypeTuple inputTypes = operation.getInputTypes();

    // The rest of the code in this method will attempt to create
    // a sequence that creates at least one value of type T for
    // every type T in inputTypes, and thus can be used to create all the
    // inputs for the statement.
    // We denote this goal sequence as "S". We don't create S explicitly, but
    // define it as the concatenation of the following list of sequences.
    // In other words, S = sequences[0] + ... + sequences[sequences.size()-1].
    // (This representation choice is for efficiency: it is cheaper to perform
    // a single concatenation of the subsequences in the end than repeatedly
    // extending S.)

    List<Sequence> sequences = new ArrayList<>();

    // We store the total size of S in the following variable.

    int totStatements = 0;

    // The method also returns a list of randomly-selected variables to
    // be used as inputs to the statement, represented as indices into S.
    // For example, given as statement a method M(T1)/T2 that takes as input
    // a value of type T1 and returns a value of type T2, this method might
    // return, for example, the sequence
    //
    // T0 var0 = new T0(); T1 var1 = var0.getT1()"
    //
    // and the singleton list [0] that represents variable var1. The variable
    // indices are stored in the following list. Upon successful completion
    // of this method, variables will contain inputTypes.size() variables.
    // Note additionally that for every i in variables, 0 <= i < |S|.

    List<Integer> variables = new ArrayList<>();

    // [Optimization]
    // The following two variables are used in the loop below only when
    // an alias ratio is present (GenInputsAbstract.alias_ratio != null).
    // Their purpose is purely to improve efficiency. For a given loop iteration
    // i, "types" contains the types of all variables in S, and "typesToVars"
    // maps each type to all variable indices of the given type.
    SubTypeSet types = new SubTypeSet(false);
    MultiMap<Type, Integer> typesToVars = new MultiMap<>();

    for (int i = 0; i < inputTypes.size(); i++) {
      Type inputType = inputTypes.get(i);

      // true if statement st represents an instance method, and we are
      // currently
      // selecting a value to act as the receiver for the method.
      boolean isReceiver = (i == 0 && (operation.isMessage()) && (!operation.isStatic()));

      // If alias ratio is given, attempt with some probability to use a
      // variable already in S.
      if (GenInputsAbstract.alias_ratio != 0
          && Randomness.weightedCoinFlip(GenInputsAbstract.alias_ratio)) {

        // candidateVars will store the indices that can serve as input to the
        // i-th input in st.
        List<SimpleList<Integer>> candidateVars = new ArrayList<>();

        // For each type T in S compatible with inputTypes[i], add all the
        // indices in S of type T.
        for (Type match : types.getMatches(inputType)) {
          // Sanity check: the domain of typesToVars contains all the types in
          // variable types.
          assert typesToVars.keySet().contains(match);
          candidateVars.add(
              new ArrayListSimpleList<>(new ArrayList<>(typesToVars.getValues(match))));
        }

        // If any type-compatible variables found, pick one at random as the
        // i-th input to st.
        SimpleList<Integer> candidateVars2 = new ListOfLists<>(candidateVars);
        if (!candidateVars2.isEmpty()) {
          int randVarIdx = Randomness.nextRandomInt(candidateVars2.size());
          Integer randVar = candidateVars2.get(randVarIdx);
          variables.add(randVar);
          continue;
        }
      }

      // If we got here, it means we will not attempt to use a value already
      // defined in S,
      // so we will have to augment S with new statements that yield a value of
      // type inputTypes[i].
      // We will do this by assembling a list of candidate sequences n(stored in
      // the list declared
      // immediately below) that create one or more values of the appropriate
      // type,
      // randomly selecting a single sequence from this list, and appending it
      // to S.
      SimpleList<Sequence> l;

      // We use one of two ways to gather candidate sequences, but the second
      // case below
      // is by far the most common.

      if (inputType.isArray()) {

        // 1. If T=inputTypes[i] is an array type, ask the component manager for
        // all sequences
        // of type T (list l1), but also try to directly build some sequences
        // that create arrays (list l2).
        SimpleList<Sequence> l1 = componentManager.getSequencesForType(operation, i);
        if (Log.isLoggingOn()) {
          Log.logLine("Array creation heuristic: will create helper array of type " + inputType);
        }
        SimpleList<Sequence> l2 =
            HelperSequenceCreator.createArraySequence(componentManager, inputType);
        l = new ListOfLists<>(l1, l2);

      } else if (inputType.isParameterized()
          && ((InstantiatedType) inputType)
              .getGenericClassType()
              .isSubtypeOf(JDKTypes.COLLECTION_TYPE)) {
        InstantiatedType classType = (InstantiatedType) inputType;

        SimpleList<Sequence> l1 = componentManager.getSequencesForType(operation, i);
        if (Log.isLoggingOn()) {
          Log.logLine("Collection creation heuristic: will create helper of type " + classType);
        }
        ArrayListSimpleList<Sequence> l2 = new ArrayListSimpleList<>();
        Sequence creationSequence =
            HelperSequenceCreator.createCollection(componentManager, classType);
        if (creationSequence != null) {
          l2.add(creationSequence);
        }
        l = new ListOfLists<>(l1, l2);

      } else {

        // 2. COMMON CASE: ask the component manager for all sequences that
        // yield the required type.
        if (Log.isLoggingOn()) {
          Log.logLine("Will query component set for objects of type" + inputType);
        }
        l = componentManager.getSequencesForType(operation, i);
      }
      assert l != null;

      if (Log.isLoggingOn()) {
        Log.logLine("components: " + l.size());
      }

      // If we were not able to find (or create) any sequences of type
      // inputTypes[i], and we are
      // allowed the use null values, use null. If we're not allowed, then
      // return with failure.
      if (l.isEmpty()) {
        if (isReceiver || GenInputsAbstract.forbid_null) {
          if (Log.isLoggingOn()) {
            Log.logLine("forbid-null option is true. Failed to create new sequence.");
          }
          return new InputsAndSuccessFlag(false, null, null);
        } else {
          if (Log.isLoggingOn()) Log.logLine("Will use null as " + i + "-th input");
          TypedOperation st = TypedOperation.createNullOrZeroInitializationForType(inputType);
          Sequence seq = new Sequence().extend(st, new ArrayList<Variable>());
          variables.add(totStatements);
          sequences.add(seq);
          assert seq.size() == 1;
          totStatements++;
          // Null is not an interesting value to add to the set of
          // possible values to reuse, so we don't update typesToVars or types.
          continue;
        }
      }

      // At this point, we have one or more sequences that create non-null
      // values of type inputTypes[i].
      // However, the user may have requested that we use null values as inputs
      // with some given frequency.
      // If this is the case, then use null instead with some probability.
      if (!isReceiver
          && GenInputsAbstract.null_ratio != 0
          && Randomness.weightedCoinFlip(GenInputsAbstract.null_ratio)) {
        if (Log.isLoggingOn()) {
          Log.logLine("null-ratio option given. Randomly decided to use null as input.");
        }
        TypedOperation st = TypedOperation.createNullOrZeroInitializationForType(inputType);
        Sequence seq = new Sequence().extend(st, new ArrayList<Variable>());
        variables.add(totStatements);
        sequences.add(seq);
        assert seq.size() == 1;
        totStatements++;
        continue;
      }

      // At this point, we have a list of candidate sequences and need to select
      // a random element from it. Based on the enabled flags, we will use one of three
      // options for this. weighted_sequences and constants use weights generated from
      // previous operations done in the generator (Weighted), small_tests use the default weight of
      // sequences, and selecting a random member.
      Sequence chosenSeq;

      if (GenInputsAbstract.weighted_sequences || GenInputsAbstract.weighted_constants) {
        // Orienteering and Constant mining Stuff
        chosenSeq = Randomness.randomMemberWeighted(l, weightMap);
      } else if (GenInputsAbstract.small_tests) {
        chosenSeq = Randomness.randomMemberWeighted(l);
      } else {
        chosenSeq = Randomness.randomMember(l);
      }

      // Now, find values that satisfy the constraint set.
      Variable randomVariable = chosenSeq.randomVariableForTypeLastStatement(inputType);

      // We are not done yet: we have chosen a sequence that yields a value of
      // the required
      // type inputTypes[i], but there may be more than one such value. Our last
      // random
      // selection step is to select from among all possible values.
      // if (i == 0 && statement.isInstanceMethod()) m = Match.EXACT_TYPE;
      if (randomVariable == null) {
        throw new BugInRandoopException("type: " + inputType + ", sequence: " + chosenSeq);
      }

      // Fail, if we were unlucky and selected a null or primitive value as the
      // receiver for a method call.
      if (i == 0
          && operation.isMessage()
          && !(operation.isStatic())
          && (chosenSeq.getCreatingStatement(randomVariable).isPrimitiveInitialization()
              || randomVariable.getType().isPrimitive())) {

        return new InputsAndSuccessFlag(false, null, null);
      }

      // [Optimization.] Update optimization-related variables "types" and
      // "typesToVars".
      if (GenInputsAbstract.alias_ratio != 0) {
        // Update types and typesToVars.
        for (int j = 0; j < chosenSeq.size(); j++) {
          Statement stk = chosenSeq.getStatement(j);
          if (stk.isPrimitiveInitialization()) {
            continue; // Prim decl not an interesting candidate for multiple
          }
          // uses.
          Type outType = stk.getOutputType();
          types.add(outType);
          typesToVars.add(outType, totStatements + j);
        }
      }

      variables.add(totStatements + randomVariable.index);
      sequences.add(chosenSeq);
      totStatements += chosenSeq.size();
    }

    return new InputsAndSuccessFlag(true, sequences, variables);
  }
}
