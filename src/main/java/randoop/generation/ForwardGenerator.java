package randoop.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.BugInRandoopException;
import randoop.DummyVisitor;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.SubTypeSet;
import randoop.main.GenInputsAbstract;
import randoop.operation.NonreceiverTerm;
import randoop.operation.Operation;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.TypeInstantiator;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Value;
import randoop.sequence.Variable;
import randoop.test.DummyCheckGenerator;
import randoop.types.JavaTypes;
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

/**
 * Randoop's forward, component-based generator.
 */
public class ForwardGenerator extends AbstractGenerator {

  /**
   * The set of ALL sequences ever generated, including sequences that were
   * executed and then discarded.
   */
  private final Set<Sequence> allSequences;
  private final Set<TypedOperation> observers;

  /** Sequences that are used in other sequences (and are thus redundant) */
  private Set<Sequence> subsumed_sequences = new LinkedHashSet<>();

  // For testing purposes only. If Globals.randooptestrun==false then the array
  // is never populated or queried. This set contains the same set of
  // components as the set "allsequences" above, but stores them as
  // strings obtained via the toCodeString() method.
  private final List<String> allsequencesAsCode = new ArrayList<>();

  // For testing purposes only.
  private final List<Sequence> allsequencesAsList = new ArrayList<>();

  private final TypeInstantiator instantiator;

  // The set of all primitive values seen during generation and execution
  // of sequences. This set is used to tell if a new primitive value has
  // been generated, to add the value to the components.
  private Set<Object> runtimePrimitivesSeen = new LinkedHashSet<>();

  public ForwardGenerator(
      List<TypedOperation> operations,
      Set<TypedOperation> observers,
      long timeMillis,
      int maxGenSequences,
      int maxOutSequences,
      ComponentManager componentManager,
      RandoopListenerManager listenerManager) {
    this(
        operations,
        observers,
        timeMillis,
        maxGenSequences,
        maxOutSequences,
        componentManager,
        null,
        listenerManager);
  }

  public ForwardGenerator(
      List<TypedOperation> operations,
      Set<TypedOperation> observers,
      long timeMillis,
      int maxGenSequences,
      int maxOutSequences,
      ComponentManager componentManager,
      IStopper stopper,
      RandoopListenerManager listenerManager) {

    super(
        operations,
        timeMillis,
        maxGenSequences,
        maxOutSequences,
        componentManager,
        stopper,
        listenerManager);

    this.observers = observers;
    this.allSequences = new LinkedHashSet<>();
    this.instantiator = componentManager.getTypeInstantiator();

    initializeRuntimePrimitivesSeen();
  }

  /**
   * The runtimePrimitivesSeen set contains primitive values seen during
   * generation/execution and is used to determine new values that should be
   * added to the component set. The component set initially contains a set of
   * primitive sequences; this method puts those primitives in this set.
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

    long endTime = System.nanoTime();
    long gentime = endTime - startTime;
    startTime = endTime; // reset start time.

    eSeq.execute(executionVisitor, checkGenerator);

    endTime = System.nanoTime();

    eSeq.exectime = endTime - startTime;
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

  @Override
  public Set<Sequence> getAllSequences() {
    return Collections.unmodifiableSet(this.allSequences);
  }

  /**
   * Determines what indices in the given sequence are active. An active index i
   * means that the i-th method call creates an interesting/useful value that
   * can be used as an input to a larger sequence; inactive indices are never
   * used as inputs. The effect of setting active/inactive indices is that the
   * SequenceCollection to which the given sequences is added only considers the
   * active indices when deciding whether the sequence creates values of a given
   * type.
   * <p>
   * In addition to determining active indices, this method determines if any
   * primitive values created during execution of the sequence are new values
   * not encountered before. Such values are added to the component manager so
   * they can be used during subsequent generation attempts.
   *
   * @param seq  the sequence
   */
  private void processSequence(ExecutableSequence seq) {

    if (seq.hasNonExecutedStatements()) {
      if (Log.isLoggingOn()) {
        Log.logLine(
            "Making all indices inactive (sequence has non-executed statements, so judging it inadequate for further extension).");
        Log.logLine(
            "Non-executed statement: " + seq.statementToCodeString(seq.getNonExecutedIndex()));
      }
      seq.sequence.clearAllActiveFlags();
      return;
    }

    if (seq.hasFailure()) {
      if (Log.isLoggingOn()) {
        Log.logLine(
            "Making all indices inactive (sequence reveals a failure, so judging it inadequate for further extension)");
        Log.logLine("Failing sequence: " + seq.toCodeString());
      }
      seq.sequence.clearAllActiveFlags();
      return;
    }

    if (seq.hasInvalidBehavior()) {
      if (Log.isLoggingOn()) {
        Log.logLine("Making all indices inactive (sequence has invalid behavior)");
        Log.logLine("Invalid sequence: " + seq.toCodeString());
      }
      seq.sequence.clearAllActiveFlags();
      return;
    }

    if (!seq.isNormalExecution()) {
      if (Log.isLoggingOn()) {
        Log.logLine(
            "Making all indices inactive (exception thrown, or failure revealed during execution).");
        Log.logLine(
            "Statement with non-normal execution: "
                + seq.statementToCodeString(seq.getNonNormalExecutionIndex()));
      }
      seq.sequence.clearAllActiveFlags();
      return;
    }

    // Clear the active flags of some statements
    for (int i = 0; i < seq.sequence.size(); i++) {

      // If there is no return value, clear its active flag
      // Cast succeeds because of isNormalExecution clause earlier in this
      // method.
      NormalExecution e = (NormalExecution) seq.getResult(i);
      Object runtimeValue = e.getRuntimeValue();
      if (runtimeValue == null) {
        if (Log.isLoggingOn()) {
          Log.logLine("Making index " + i + " inactive (value is null)");
        }
        seq.sequence.clearActiveFlag(i);
        continue;
      }

      // If it is a call to an observer method, clear the active flag of
      // its receiver. (This method doesn't side effect the receiver, so
      // Randoop should use the other shorter sequence that produces the
      // receiver.)
      Sequence stmts = seq.sequence;
      Statement stmt = stmts.statements.get(i);
      if (stmt.isMethodCall() && observers.contains(stmt.getOperation())) {
        List<Integer> inputVars = stmts.getInputsAsAbsoluteIndices(i);
        int receiver = inputVars.get(0);
        seq.sequence.clearActiveFlag(receiver);
      }

      // If its runtime value is a primitive value, clear its active flag,
      // and if the value is new, add a sequence corresponding to that value.
      Class<?> objectClass = runtimeValue.getClass();
      if (NonreceiverTerm.isNonreceiverType(objectClass) && !objectClass.equals(Class.class)) {
        if (Log.isLoggingOn()) {
          Log.logLine("Making index " + i + " inactive (value is a primitive)");
        }
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
        if (Log.isLoggingOn()) {
          Log.logLine("Making index " + i + " active.");
        }
      }
    }
  }

  /**
   * Tries to create and execute a new sequence. If the sequence is new (not
   * already in the specified component manager), then it is executed and added
   * to the manager's sequences. If the sequence created is already in the
   * manager's sequences, this method has no effect, and returns null.
   *
   * @return a new sequence, or null
   */
  private ExecutableSequence createNewUniqueSequence() {

    if (Log.isLoggingOn()) {
      Log.logLine("-------------------------------------------");
    }

    if (this.operations.isEmpty()) {
      return null;
    }

    // Select a StatementInfo
    TypedOperation operation = Randomness.randomMember(this.operations);
    if (Log.isLoggingOn()) {
      Log.logLine("Selected operation: " + operation.toString());
    }

    if (operation.isGeneric() || operation.hasWildcardTypes()) {
      operation = instantiator.instantiate((TypedClassOperation) operation);
      if (operation == null) { //failed to instantiate generic
        return null;
      }
    }

    // add flags here
    InputsAndSuccessFlag sequences = selectInputs(operation);

    if (!sequences.success) {
      if (Log.isLoggingOn()) Log.logLine("Failed to find inputs for statement.");
      return null;
    }

    Sequence concatSeq = Sequence.concatenate(sequences.sequences);

    // Figure out input variables.
    List<Variable> inputs = new ArrayList<>();
    for (Integer oneinput : sequences.indices) {
      Variable v = concatSeq.getVariable(oneinput);
      inputs.add(v);
    }

    Sequence newSequence = concatSeq.extend(operation, inputs);

    // With .5 probability, do a primitive value heuristic.
    if (GenInputsAbstract.repeat_heuristic && Randomness.nextRandomInt(10) == 0) {
      int times = Randomness.nextRandomInt(100);
      newSequence = repeat(newSequence, operation, times);
      if (Log.isLoggingOn()) Log.log("repeat-heuristic>>>" + times + newSequence.toCodeString());
    }

    // If parameterless statement, subsequence inputs
    // will all be redundant, so just remove it from list of statements.
    // XXX does this make sense? especially in presence of side-effects
    if (operation.getInputTypes().isEmpty()) {
      operations.remove(operation);
    }

    // Discard if sequence is larger than size limit
    if (newSequence.size() > GenInputsAbstract.maxsize) {
      if (Log.isLoggingOn()) {
        Log.logLine(
            "Sequence discarded because size "
                + newSequence.size()
                + " exceeds maximum allowed size "
                + GenInputsAbstract.maxsize);
      }
      return null;
    }

    randoopConsistencyTests(newSequence);

    if (this.allSequences.contains(newSequence)) {
      if (Log.isLoggingOn()) {
        Log.logLine("Sequence discarded because the same sequence was previously created.");
      }
      return null;
    }

    this.allSequences.add(newSequence);

    for (Sequence s : sequences.sequences) {
      s.lastTimeUsed = java.lang.System.currentTimeMillis();
    }

    randoopConsistencyTest2(newSequence);

    if (Log.isLoggingOn()) {
      Log.logLine(
          String.format("Successfully created new unique sequence:%n%s%n", newSequence.toString()));
    }
    // System.out.println("###" + statement.toStringVerbose() + "###" +
    // statement.getClass());

    // Keep track of any input sequences that are used in this sequence.
    // Tests that contain only these sequences are probably redundant.
    for (Sequence is : sequences.sequences) {
      subsumed_sequences.add(is);
    }

    return new ExecutableSequence(newSequence);
  }

  /**
   * Adds the given operation to a new {@code Sequence} with the statements of
   * this object as a prefix, repeating the operation the given number of times.
   * Used during generation.
   *
   * @param seq  the sequence to extend
   * @param operation
   *          the {@link TypedOperation} to repeat.
   * @param times
   *          the number of times to repeat the {@link Operation}.
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

  // Adds the string corresponding to the given newSequences to the
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
  private InputsAndSuccessFlag selectInputs(TypedOperation operation) {

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
      // a
      // randomly-chosen sequence from the list.
      Sequence chosenSeq;
      if (GenInputsAbstract.small_tests) {
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

  /**
   * Returns the set of sequences that are included in other sequences to
   * generate inputs (and, so, are subsumed by another sequence).
   */
  @Override
  public Set<Sequence> getSubsumedSequences() {
    return subsumed_sequences;
  }

  @Override
  public int numGeneratedSequences() {
    return allSequences.size();
  }
}
