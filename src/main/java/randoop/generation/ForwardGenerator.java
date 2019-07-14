package randoop.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import randoop.DummyVisitor;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.SubTypeSet;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.operation.NonreceiverTerm;
import randoop.operation.Operation;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.RandoopInstantiationError;
import randoop.reflection.TypeInstantiator;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceExceptionError;
import randoop.sequence.Statement;
import randoop.sequence.Value;
import randoop.sequence.Variable;
import randoop.test.DummyCheckGenerator;
import randoop.types.ClassOrInterfaceType;
import randoop.types.InstantiatedType;
import randoop.types.JDKTypes;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.ListOfLists;
import randoop.util.Log;
import randoop.util.MultiMap;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;

/** Randoop's forward, component-based generator. */
public class ForwardGenerator extends AbstractGenerator {

  /**
   * The set of ALL sequences ever generated, including sequences that were executed and then
   * discarded.
   *
   * <p>This must be ordered by insertion to allow for flaky test history collection in {@link
   * randoop.main.GenTests#printSequenceExceptionError(AbstractGenerator, SequenceExceptionError)}.
   */
  private final LinkedHashSet<Sequence> allSequences;

  /** The side-effect-free methods. */
  private final Set<TypedOperation> sideEffectFreeMethods;

  /** Sequences that are used in other sequences (and are thus redundant) */
  private Set<Sequence> subsumed_sequences = new LinkedHashSet<>();

  /**
   * Set and used only if {@link GenInputsAbstract#debug_checks}==true. This set contains the same
   * set of components as the set "allsequences" above, but stores them as strings obtained via the
   * toCodeString() method.
   */
  private final List<String> allsequencesAsCode = new ArrayList<>();

  /** Set and used only if {@link GenInputsAbstract#debug_checks}==true. */
  private final List<Sequence> allsequencesAsList = new ArrayList<>();

  private final TypeInstantiator instantiator;

  /** How to select sequences as input for creating new sequences. */
  private final InputSequenceSelector inputSequenceSelector;

  /** How to select the method to use for creating a new sequence. */
  private final TypedOperationSelector operationSelector;

  /**
   * The set of all primitive values seen during generation and execution of sequences. This set is
   * used to tell if a new primitive value has been generated, to add the value to the components.
   */
  private Set<Object> runtimePrimitivesSeen = new LinkedHashSet<>();

  /**
   * Create a forward generator.
   *
   * @param operations list of operations under test
   * @param sideEffectFreeMethods side-effect-free methods
   * @param limits limits for generation, after which the generator will stop
   * @param componentManager stores previously-generated sequences
   * @param listenerManager manages notifications for listeners
   * @param classesUnderTest set of classes under test
   */
  public ForwardGenerator(
      List<TypedOperation> operations,
      Set<TypedOperation> sideEffectFreeMethods,
      GenInputsAbstract.Limits limits,
      ComponentManager componentManager,
      RandoopListenerManager listenerManager,
      Set<ClassOrInterfaceType> classesUnderTest) {
    this(
        operations,
        sideEffectFreeMethods,
        limits,
        componentManager,
        null,
        listenerManager,
        classesUnderTest);
  }

  /**
   * Create a forward generator.
   *
   * @param operations list of operations under test
   * @param sideEffectFreeMethods side-effect-free methods
   * @param limits limits for generation, after which the generator will stop
   * @param componentManager stores previously-generated sequences
   * @param stopper optional, additional stopping criterion for the generator. Can be null.
   * @param listenerManager manages notifications for listeners
   * @param classesUnderTest set of classes under test
   */
  public ForwardGenerator(
      List<TypedOperation> operations,
      Set<TypedOperation> sideEffectFreeMethods,
      GenInputsAbstract.Limits limits,
      ComponentManager componentManager,
      IStopper stopper,
      RandoopListenerManager listenerManager,
      Set<ClassOrInterfaceType> classesUnderTest) {
    super(operations, limits, componentManager, stopper, listenerManager);

    this.sideEffectFreeMethods = sideEffectFreeMethods;
    this.allSequences = new LinkedHashSet<>();
    this.instantiator = componentManager.getTypeInstantiator();

    initializeRuntimePrimitivesSeen();

    switch (GenInputsAbstract.method_selection) {
      case UNIFORM:
        this.operationSelector = new UniformRandomMethodSelection(operations);
        break;
      case BLOODHOUND:
        this.operationSelector = new Bloodhound(operations, classesUnderTest);
        break;
      default:
        throw new Error("This can't happen");
    }

    switch (GenInputsAbstract.input_selection) {
      case SMALL_TESTS:
        inputSequenceSelector = new SmallTestsSequenceSelection();
        break;
      case UNIFORM:
        inputSequenceSelector = new UniformRandomSequenceSelection();
        break;
      default:
        throw new Error(
            "Case statement does not handle all InputSelectionModes: "
                + GenInputsAbstract.input_selection);
    }
  }

  /**
   * Take action based on the given {@link Sequence} that was classified as a regression test.
   *
   * @param sequence the new sequence that was classified as a regression test
   */
  @Override
  public void newRegressionTestHook(Sequence sequence) {
    operationSelector.newRegressionTestHook(sequence);
  }

  /**
   * The runtimePrimitivesSeen set contains primitive values seen during generation/execution and is
   * used to determine new values that should be added to the component set. The component set
   * initially contains a set of primitive sequences; this method puts those primitives in this set.
   */
  // XXX this is goofy - these values are available in other ways
  private void initializeRuntimePrimitivesSeen() {
    for (Sequence s : componentManager.getAllPrimitiveSequences()) {
      ExecutableSequence es = new ExecutableSequence(s);
      es.execute(new DummyVisitor(), new DummyCheckGenerator());
      NormalExecution e = (NormalExecution) es.getResult(0);
      Object runtimeValue = e.getRuntimeValue();
      runtimePrimitivesSeen.add(runtimeValue);
    }
  }

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

    long gentime1 = System.nanoTime() - startTime;

    eSeq.execute(executionVisitor, checkGenerator);

    startTime = System.nanoTime(); // reset start time.

    determineActiveIndices(eSeq);

    if (eSeq.sequence.hasActiveFlags()) {
      componentManager.addGeneratedSequence(eSeq.sequence);
    }

    long gentime2 = System.nanoTime() - startTime;

    eSeq.gentime = gentime1 + gentime2;

    return eSeq;
  }

  @Override
  public LinkedHashSet<Sequence> getAllSequences() {
    return this.allSequences;
  }

  /**
   * Determines what indices in the given sequence are active. An active index i means that the i-th
   * method call creates an interesting/useful value that can be used as an input to a larger
   * sequence; inactive indices are never used as inputs. The effect of setting active/inactive
   * indices is that the SequenceCollection to which the given sequences is added only considers the
   * active indices when deciding whether the sequence creates values of a given type.
   *
   * <p>In addition to determining active indices, this method determines if any primitive values
   * created during execution of the sequence are new values not encountered before. Such values are
   * added to the component manager so they can be used during subsequent generation attempts.
   *
   * @param seq the sequence
   */
  private void determineActiveIndices(ExecutableSequence seq) {

    if (seq.hasNonExecutedStatements()) {
      Log.logPrintf("Sequence has non-executed statements: excluding from extension pool.%n");
      Log.logPrintf(
          "Non-executed statement: %s%n", seq.statementToCodeString(seq.getNonExecutedIndex()));
      seq.sequence.clearAllActiveFlags();
      return;
    }

    if (seq.hasFailure()) {
      Log.logPrintf("Sequence has failure: excluding from extension pool.%n");
      Log.logPrintf("Failing sequence: %s%n", seq.toCodeString());
      seq.sequence.clearAllActiveFlags();
      return;
    }

    if (seq.hasInvalidBehavior()) {
      Log.logPrintf(
          "Sequence has invalid behavior (%s): discarding and excluding from extension pool.%n",
          seq.getChecks());
      Log.logPrintf("Invalid sequence: %s%n", seq.toCodeString());
      seq.sequence.clearAllActiveFlags();
      return;
    }

    if (!seq.isNormalExecution()) {
      int i = seq.getNonNormalExecutionIndex();
      Log.logPrintf(
          "Excluding from extension pool due to exception or failure in statement %s%n", i);
      Log.logPrintf("  Statement: %s%n", seq.statementToCodeString(i));
      Log.logPrintf("  Result: %s%n", seq.getResult(i));
      seq.sequence.clearAllActiveFlags();
      return;
    }

    // Clear the active flags of some statements
    for (int i = 0; i < seq.sequence.size(); i++) {

      // If there is no return value, clear its active flag.
      // Cast succeeds because of isNormalExecution clause earlier in this method.
      NormalExecution e = (NormalExecution) seq.getResult(i);
      Object runtimeValue = e.getRuntimeValue();
      if (runtimeValue == null) {
        Log.logPrintf("Making index " + i + " inactive (value is null)%n");
        seq.sequence.clearActiveFlag(i);
        continue;
      }

      // If it is a call to a side-effect-free method, clear the active flag of
      // its receiver and arguments. (This method doesn't side effect the receiver or
      // any argument, so Randoop should use some other shorter sequence
      // that produces the value.)
      Sequence stmts = seq.sequence;
      Statement stmt = stmts.statements.get(i);
      boolean isSideEffectFree =
          stmt.isMethodCall() && sideEffectFreeMethods.contains(stmt.getOperation());
      Log.logPrintf("isSideEffectFree => %s for %s%n", isSideEffectFree, stmt);
      if (isSideEffectFree) {
        List<Integer> inputVars = stmts.getInputsAsAbsoluteIndices(i);
        for (Integer inputIndex : inputVars) {
          seq.sequence.clearActiveFlag(inputIndex);
        }
      }

      // If its runtime value is a primitive value, clear its active flag,
      // and if the value is new, add a sequence corresponding to that value.
      // This yields shorter tests than using the full sequence that produced
      // the value.
      Class<?> objectClass = runtimeValue.getClass();
      if (NonreceiverTerm.isNonreceiverType(objectClass) && !objectClass.equals(Class.class)) {
        Log.logPrintf("Making index " + i + " inactive (value is a primitive)%n");
        seq.sequence.clearActiveFlag(i);

        boolean looksLikeObjToString =
            (runtimeValue instanceof String)
                && Value.looksLikeObjectToString((String) runtimeValue);
        boolean tooLongString =
            (runtimeValue instanceof String) && !Value.stringLengthOK((String) runtimeValue);
        if (runtimeValue instanceof Double && Double.isNaN((double) runtimeValue)) {
          runtimeValue = Double.NaN; // canonicalize NaN value
        }
        if (runtimeValue instanceof Float && Float.isNaN((float) runtimeValue)) {
          runtimeValue = Float.NaN; // canonicalize NaN value
        }
        if (!looksLikeObjToString && !tooLongString && runtimePrimitivesSeen.add(runtimeValue)) {
          // Have not seen this value before; add it to the component set.
          componentManager.addGeneratedSequence(Sequence.createSequenceForPrimitive(runtimeValue));
        }
      } else {
        Log.logPrintf("Making index " + i + " active.%n");
      }
    }
  }

  /**
   * Tries to create a new sequence. If the sequence is new (not already in the specified component
   * manager), then it is added to the manager's sequences.
   *
   * <p>This method returns null if:
   *
   * <ul>
   *   <li>it selects an operation but cannot generate inputs for the operation
   *   <li>it creates too large a method
   *   <li>it creates a duplicate sequence
   * </ul>
   *
   * This method modifies the list of operations that represent the set of methods under tests.
   * Specifically, if the selected operation used for creating a new and unique sequence is a
   * parameterless operation (a static constant method or no-argument constructor) it is removed
   * from the list of operations. Such a method will return the same thing every time it is invoked
   * (unless it's nondeterministic, but Randoop should not be run on nondeterministic methods). Once
   * invoked, its result is in the pool and there is no need to call the operation again and so we
   * will remove it from the list of operations.
   *
   * @return a new sequence, or null
   */
  private ExecutableSequence createNewUniqueSequence() {

    Log.logPrintf("-------------------------------------------%n");

    if (this.operations.isEmpty()) {
      return null;
    }

    // Select the next operation to use in constructing a new sequence.
    TypedOperation operation = operationSelector.selectOperation();
    Log.logPrintf("Selected operation: %s%n", operation);

    if (operation.isGeneric() || operation.hasWildcardTypes()) {
      try {
        operation = instantiator.instantiate((TypedClassOperation) operation);
      } catch (Throwable e) {
        if (GenInputsAbstract.fail_on_generation_error) {
          if (operation.isMethodCall() || operation.isConstructorCall()) {
            String opName = operation.getOperation().getReflectionObject().toString();
            throw new RandoopInstantiationError(opName, e);
          }
        } else {
          operationHistory.add(operation, OperationOutcome.SEQUENCE_DISCARDED);
          Log.logPrintf("Instantiation error for operation%n %s%n", operation);
          Log.logStackTrace(e);
          System.out.printf("Instantiation error for operation%n %s%n", operation);
          return null;
        }
      }
      if (operation == null) { // failed to instantiate generic
        Log.logPrintf("Failed to instantiate generic operation%n", operation);
        return null;
      }
    }

    // add flags here
    InputsAndSuccessFlag inputs;
    try {
      inputs = selectInputs(operation);
    } catch (Throwable e) {
      if (GenInputsAbstract.fail_on_generation_error) {
        throw new RandoopGenerationError(operation, e);
      } else {
        operationHistory.add(operation, OperationOutcome.SEQUENCE_DISCARDED);
        Log.logPrintf("Error selecting inputs for operation: %s%n", operation);
        Log.logStackTrace(e);
        System.out.println("Error selecting inputs for operation: " + operation);
        e.printStackTrace();
        return null;
      }
    }

    if (!inputs.success) {
      operationHistory.add(operation, OperationOutcome.NO_INPUTS_FOUND);
      Log.logPrintf("Failed to find inputs for operation: %s%n", operation);
      return null;
    }

    Sequence concatSeq = Sequence.concatenate(inputs.sequences);

    // Figure out input variables.
    List<Variable> inputVars = new ArrayList<>();
    for (Integer inputIndex : inputs.indices) {
      Variable v = concatSeq.getVariable(inputIndex);
      inputVars.add(v);
    }

    Sequence newSequence = concatSeq.extend(operation, inputVars);

    // With .1 probability, do a "repeat" heuristic.
    if (GenInputsAbstract.repeat_heuristic && Randomness.nextRandomInt(10) == 0) {
      int times = Randomness.nextRandomInt(100);
      newSequence = repeat(newSequence, operation, times);
      Log.logPrintf("repeat-heuristic>>> %s %s%n", times, newSequence.toCodeString());
    }

    // A parameterless operation (a static constant method or no-argument constructor) returns the
    // same thing every time it is invoked. Since we have just invoked it, its result will be in the
    // pool.
    // There is no need to call this operation again, so remove it from the list of operations.
    if (operation.getInputTypes().isEmpty()) {
      operationHistory.add(operation, OperationOutcome.REMOVED);
      operations.remove(operation);
    }

    // Discard if sequence is larger than size limit
    if (newSequence.size() > GenInputsAbstract.maxsize) {
      operationHistory.add(operation, OperationOutcome.SEQUENCE_DISCARDED);
      Log.logPrintf(
          "Sequence discarded because size %d exceeds maximum allowed size %d%n",
          newSequence.size(), GenInputsAbstract.maxsize);
      return null;
    }

    randoopConsistencyTests(newSequence);

    // Discard if sequence is a duplicate.
    if (this.allSequences.contains(newSequence)) {
      operationHistory.add(operation, OperationOutcome.SEQUENCE_DISCARDED);
      Log.logPrintf("Sequence discarded because the same sequence was previously created.%n");
      return null;
    }

    this.allSequences.add(newSequence);

    randoopConsistencyTest2(newSequence);

    Log.logPrintf("Successfully created new unique sequence:%n%s%n", newSequence.toString());

    // Keep track of any input sequences that are used in this sequence.

    // A test that is a subsequence of the new one is redundant.
    subsumed_sequences.addAll(inputs.sequences);

    return new ExecutableSequence(newSequence);
  }

  /**
   * Adds the given operation to a new {@code Sequence} with the statements of this object as a
   * prefix, repeating the operation the given number of times. Used during generation.
   *
   * @param seq the sequence to extend
   * @param operation the {@link TypedOperation} to repeat
   * @param times the number of times to repeat the {@link Operation}
   * @return a new {@code Sequence}
   */
  private Sequence repeat(Sequence seq, TypedOperation operation, int times) {
    Sequence retval = new Sequence(seq.statements);
    for (int i = 0; i < times; i++) {
      List<Integer> vil = new ArrayList<>();
      for (Variable v : retval.getInputs(retval.size() - 1)) {
        if (v.getType().equals(JavaTypes.INT_TYPE)) {
          int randint = Randomness.nextRandomInt(100);
          retval =
              retval.extend(
                  TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, randint));
          vil.add(retval.size() - 1);
        } else {
          vil.add(v.getDeclIndex());
        }
      }
      List<Variable> vl = new ArrayList<>();
      for (Integer vi : vil) {
        vl.add(retval.getVariable(vi));
      }
      retval = retval.extend(operation, vl);
    }
    return retval;
  }

  // If debugging is enabled,
  // adds the string corresponding to the given newSequences to the
  // set allSequencesAsCode. The latter set is intended to mirror
  // the set allSequences, but stores strings instead of Sequences.
  private void randoopConsistencyTest2(Sequence newSequence) {
    // Testing code.
    if (GenInputsAbstract.debug_checks) {
      this.allsequencesAsCode.add(newSequence.toCodeString());
      this.allsequencesAsList.add(newSequence);
    }
  }

  // Checks that the set allSequencesAsCode contains a set of strings
  // equivalent to the sequences in allSequences.
  private void randoopConsistencyTests(Sequence newSequence) {
    // Testing code.
    if (GenInputsAbstract.debug_checks) {
      String code = newSequence.toCodeString();
      if (this.allSequences.contains(newSequence)) {
        if (!this.allsequencesAsCode.contains(code)) {
          throw new IllegalStateException(code);
        }
      } else {
        if (this.allsequencesAsCode.contains(code)) {
          int index = this.allsequencesAsCode.indexOf(code);
          StringBuilder b = new StringBuilder();
          Sequence co = this.allsequencesAsList.get(index);
          assert co.equals(newSequence); // XXX this was a floating call to equals
          b.append("new component:")
              .append(Globals.lineSep)
              .append("")
              .append(newSequence.toString())
              .append("")
              .append(Globals.lineSep)
              .append("as code:")
              .append(Globals.lineSep)
              .append("")
              .append(code)
              .append(Globals.lineSep);
          b.append("existing component:")
              .append(Globals.lineSep)
              .append("")
              .append(this.allsequencesAsList.get(index).toString())
              .append("")
              .append(Globals.lineSep)
              .append("as code:")
              .append(Globals.lineSep)
              .append("")
              .append(this.allsequencesAsList.get(index).toCodeString());
          throw new IllegalStateException(b.toString());
        }
      }
    }
  }

  /**
   * This method is responsible for doing two things:
   *
   * <ol>
   *   <li>Selecting at random a collection of sequences that can be used to create input values for
   *       the given statement, and
   *   <li>Selecting at random valid indices to the above sequence specifying the values to be used
   *       as input to the statement.
   * </ol>
   *
   * <p>The selected sequences and indices are wrapped in an InputsAndSuccessFlag object and
   * returned. If an appropriate collection of sequences and indices was not found (e.g. because
   * there are no sequences in the componentManager that create values of some type required by the
   * statement), the success flag of the returned object is false.
   *
   * @param operation the statement to analyze
   * @return the selected sequences and indices
   */
  @SuppressWarnings("unchecked")
  private InputsAndSuccessFlag selectInputs(TypedOperation operation) {

    // Variable inputTypes contains the values required as input to the
    // statement given as a parameter to the selectInputs method.

    TypeTuple inputTypes = operation.getInputTypes();
    Log.logPrintf("selectInputs:  inputTypes=%s%n", inputTypes);

    // The rest of the code in this method will attempt to create
    // a sequence that creates at least one value of type T for
    // every type T in inputTypes, and thus can be used to create all the
    // inputs for the statement.
    // We denote this goal sequence as "S". We don't create S explicitly, but
    // define it as the concatenation of the following list of sequences.
    // In other words, S = sequences[0] + ... + sequences[sequences.size()-1].
    // (This representation choice is for efficiency: it is cheaper to perform
    // a single concatenation of the subsequences in the end than to repeatedly
    // extend S.)

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
      // currently selecting a value to act as the receiver for the method.
      boolean isReceiver = (i == 0 && operation.isMessage() && !operation.isStatic());

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
          candidateVars.add(new SimpleArrayList<Integer>(typesToVars.getValues(match)));
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

      // The user may have requested that we use null values as inputs with some given frequency.
      // If this is the case, then use null instead with some probability.
      if (!isReceiver
          && GenInputsAbstract.null_ratio != 0
          && Randomness.weightedCoinFlip(GenInputsAbstract.null_ratio)) {
        Log.logPrintf("null-ratio option given. Randomly decided to use null as input.%n");
        TypedOperation st = TypedOperation.createNullOrZeroInitializationForType(inputType);
        Sequence seq = new Sequence().extend(st, Collections.emptyList());
        variables.add(totStatements);
        sequences.add(seq);
        assert seq.size() == 1;
        totStatements++;
        continue;
      }

      // If we got here, it means we will not attempt to use null or a value already defined in S,
      // so we will have to augment S with new statements that yield a value of type inputTypes[i].
      // We will do this by assembling a list of candidate sequences (stored in the list declared
      // immediately below) that create one or more values of the appropriate type,
      // randomly selecting a single sequence from this list, and appending it to S.
      SimpleList<Sequence> candidates;

      // We use one of two ways to gather candidate sequences, but the second
      // case below is by far the most common.

      if (inputType.isArray()) {

        // 1. If T=inputTypes[i] is an array type, ask the component manager for all sequences
        // of type T (list l1), but also try to directly build some sequences
        // that create arrays (list l2).
        Log.logPrintf("Array creation heuristic: will create helper array of type %s%n", inputType);
        SimpleList<Sequence> l1 = componentManager.getSequencesForType(operation, i, isReceiver);
        SimpleList<Sequence> l2 =
            HelperSequenceCreator.createArraySequence(componentManager, inputType);
        candidates = new ListOfLists<>(l1, l2);
        Log.logPrintf("Array creation heuristic: " + candidates.size() + " candidates%n");

      } else if (inputType.isParameterized()
          && ((InstantiatedType) inputType)
              .getGenericClassType()
              .isSubtypeOf(JDKTypes.COLLECTION_TYPE)) {
        InstantiatedType classType = (InstantiatedType) inputType;

        SimpleList<Sequence> l1 = componentManager.getSequencesForType(operation, i, isReceiver);
        Log.logPrintf("Collection creation heuristic: will create helper of type %s%n", classType);
        SimpleArrayList<Sequence> l2 = new SimpleArrayList<>();
        Sequence creationSequence =
            HelperSequenceCreator.createCollection(componentManager, classType);
        if (creationSequence != null) {
          l2.add(creationSequence);
        }
        candidates = new ListOfLists<>(l1, l2);

      } else {

        // 2. COMMON CASE: ask the component manager for all sequences that
        // yield the required type.
        Log.logPrintf("Will query component set for objects of type %s%n", inputType);
        candidates = componentManager.getSequencesForType(operation, i, isReceiver);
      }
      assert candidates != null;
      Log.logPrintf("number of candidate components: %s%n", candidates.size());

      if (candidates.isEmpty()) {
        // We were not able to find (or create) any sequences of type inputTypes[i].
        // Try to use null if allowed.
        if (isReceiver) {
          Log.logPrintf("No sequences of receiver type.%n");
          return new InputsAndSuccessFlag(false, null, null);
        } else if (GenInputsAbstract.forbid_null) {
          Log.logPrintf(
              "No sequences of type, and forbid-null option is true. Failed to create new sequence.%n");
          return new InputsAndSuccessFlag(false, null, null);
        } else {
          Log.logPrintf(
              "Found no sequences of required type; will use null as " + i + "-th input%n");
          TypedOperation st = TypedOperation.createNullOrZeroInitializationForType(inputType);
          Sequence seq = new Sequence().extend(st, Collections.emptyList());
          variables.add(totStatements);
          sequences.add(seq);
          assert seq.size() == 1;
          totStatements++;
          // Null is not an interesting value to add to the set of
          // possible values to reuse, so we don't update typesToVars or types.
          continue;
        }
      }

      // At this point, we have a list of candidate sequences and need to select a
      // randomly-chosen sequence from the list.
      VarAndSeq varAndSeq = randomVariable(candidates, inputType, isReceiver);
      Variable randomVariable = varAndSeq.var;
      Sequence chosenSeq = varAndSeq.seq;

      // [Optimization.] Update optimization-related variables "types" and "typesToVars".
      if (GenInputsAbstract.alias_ratio != 0) {
        // Update types and typesToVars.
        for (int j = 0; j < chosenSeq.size(); j++) {
          Statement stk = chosenSeq.getStatement(j);
          if (stk.isNonreceivingInitialization()) {
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

  // A pair of a variable and a sequence
  private static class VarAndSeq {
    final Variable var;
    final Sequence seq;

    VarAndSeq(Variable var, Sequence seq) {
      this.var = var;
      this.seq = seq;
    }
  }

  /**
   * Return a variable of the given type.
   *
   * @param candidates sequences, each of which produces the given type; that is, each would be a
   *     legal return value
   * @param inputType the type of the chosen variable/sequence
   * @param isReceiver whether the value will be used as a receiver
   * @return a random variable of the given type, chosen from the candidates
   */
  VarAndSeq randomVariable(SimpleList<Sequence> candidates, Type inputType, boolean isReceiver) {
    // Log.logPrintf("entering randomVariable(%s)%n", inputType);
    for (int i = 0; i < 10; i++) { // can return null.  Try several times to get a non-null value.

      // if (Log.isLoggingOn()) {
      //   Log.logPrintf("randomVariable: %d candidates%n", candidates.size());
      //   for (int j = 0; j < candidates.size(); j++) {
      //     String candIndented
      //         = candidates.get(j).toString().trim().replace("\n", "\n            ");
      //     Log.logPrintf("  cand #%d: %s%n", j, candIndented);
      //   }
      // }

      Sequence chosenSeq = inputSequenceSelector.selectInputSequence(candidates);
      Log.logPrintf("chosenSeq: %s%n", chosenSeq);

      // TODO: the last statement might not be active -- it might not create a usable variable of
      // such a type.  An example is a void method that is called with only null arguments.
      // More generally, paying attention to only the last statement here seems like a reasonable
      // design choice, but it is inconsistent with how Randoop behaves in general, and all parts
      // of Randoop should be made consistent.  Alternative to the below (but this is a hack, and it
      // would be better to make the design cleaner):
      // Variable randomVariable = chosenSeq.randomVariableForType(inputType, isReceiver);

      // We are not done yet: we have chosen a sequence that yields a value of the required
      // type inputTypes[i], but it may produce more than one such value. Our last random
      // selection step is to select from among all possible values produced by the sequence.
      Variable randomVariable = chosenSeq.randomVariableForTypeLastStatement(inputType, isReceiver);

      if (randomVariable == null) {
        continue;
      }
      if (isReceiver
          && (chosenSeq.getCreatingStatement(randomVariable).isNonreceivingInitialization()
              || randomVariable.getType().isPrimitive())) {
        System.out.println();
        System.out.println("Selected null or a primitive as the receiver for a method call.");
        // System.out.printf("  operation = %s%n", operation);
        System.out.printf("  isReceiver = %s%n", isReceiver);
        System.out.printf("  randomVariable = %s%n", randomVariable);
        System.out.printf("    getType() = %s%n", randomVariable.getType());
        System.out.printf("    isPrimitive = %s%n", randomVariable.getType().isPrimitive());
        System.out.printf("  chosenSeq = {%n%s}%n", chosenSeq);
        System.out.printf(
            "    getCreatingStatement = %s%n", chosenSeq.getCreatingStatement(randomVariable));
        System.out.printf(
            "    isNonreceivingInitialization = %s%n",
            chosenSeq.getCreatingStatement(randomVariable).isNonreceivingInitialization());
        continue;
        // throw new RandoopBug(
        //     "Selected null or primitive value as the receiver for a method call");
      }

      return new VarAndSeq(randomVariable, chosenSeq);
    }
    // Can't get here unless isReceiver is true.  TODO: fix design so this cannot happen.
    assert isReceiver;
    // Try every element of the list, in order.
    List<VarAndSeq> validResults = new ArrayList<>();
    for (int i = 0; i < candidates.size(); i++) {
      Sequence s = candidates.get(i);
      Variable randomVariable = s.randomVariableForTypeLastStatement(inputType, isReceiver);
      validResults.add(new VarAndSeq(randomVariable, s));
    }
    if (validResults.size() == 0) {
      throw new RandoopBug(
          String.format(
              "Failed to select %svariable with input type %s",
              (isReceiver ? "receiver " : ""), inputType));
    }
    return Randomness.randomMember(validResults);
  }

  /**
   * Returns the set of sequences that are included in other sequences to generate inputs (and, so,
   * are subsumed by another sequence).
   */
  @Override
  public Set<Sequence> getSubsumedSequences() {
    return subsumed_sequences;
  }

  @Override
  public int numGeneratedSequences() {
    return allSequences.size();
  }

  @Override
  public String toString() {
    return "ForwardGenerator("
        + ("allSequences:" + allSequences.size())
        + ", "
        + ("sideEffectFreeMethods:" + sideEffectFreeMethods.size())
        + ", "
        + ("subsumed_sequences:" + subsumed_sequences.size())
        + ", "
        + ("runtimePrimitivesSeen:" + runtimePrimitivesSeen.size())
        + ")";
  }
}
