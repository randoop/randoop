package randoop.generation;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.SerializationUtils;
import randoop.*;
import randoop.generation.exhaustive.SequenceGenerator;
import randoop.main.GenAllTests;
import randoop.main.GenInputsAbstract;
import randoop.operation.NonreceiverTerm;
import randoop.operation.Operation;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.TypeInstantiator;
import randoop.reflection.TypeNames;
import randoop.sequence.*;
import randoop.test.DummyCheckGenerator;
import randoop.types.*;
import randoop.util.*;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Randoop's forward, component-based generator.
 */
public class ForwardExhaustiveGenerator extends AbstractGenerator {

  private final Set<TypedOperation> observers;

  private final TypeInstantiator instantiator;

  // The set of all primitive values seen during generation and execution
  // of sequences. This set is used to tell if a new primitive value has
  // been generated, to add the value to the components.
  private Set<Object> runtimePrimitivesSeen = new LinkedHashSet<>();

  /* Class used to generate all permutations of typed operations (not including constructors). */
  private SequenceGenerator<TypedOperation> sequenceGenerator;

  private Set<TypedOperation> constructors;

  // Sequence to be used to instantiate current class.
  private Sequence constructorPrefix;

  private ExecutableSequence executableConstructorPrefix;

  // Since this generator is intended to generate for just one class, stores the CUT in this variable.
  private Type classUnderTest;

  public ForwardExhaustiveGenerator(
      List<TypedOperation> operations,
      ComponentManager componentManager,
      RandoopListenerManager listenerManager) {
    this(
        operations,
        null,
        Long.MAX_VALUE,
        Integer.MAX_VALUE,
        Integer.MAX_VALUE,
        componentManager,
        null,
        listenerManager);
  }

  public ForwardExhaustiveGenerator(
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

  public ForwardExhaustiveGenerator(
      List<TypedOperation> operations,
      Set<TypedOperation> observers,
      long timeMillis,
      int maxGenSequences,
      int maxOutSequences,
      ComponentManager componentManager,
      IStopper stopper,
      RandoopListenerManager listenerManager) {

    super(
        operations.stream().filter(op -> !op.isConstructorCall()).collect(Collectors.toList()),
        timeMillis,
        maxGenSequences,
        maxOutSequences,
        componentManager,
        stopper,
        listenerManager);

    this.observers = observers;
    this.instantiator = componentManager.getTypeInstantiator();

    SequenceGenerator.SequenceIndex startingIndex = null;
    if (GenInputsAbstract.generation_index_file != null) {
      try {
        startingIndex =
            SequenceGenerator.SequenceIndex.deserializeFromFile(
                GenInputsAbstract.generation_index_file);
      } catch (IOException e) {
        System.out.printf("Error trying to deserializing sequence index file: %s", e);
      }
    }

    this.sequenceGenerator =
        new SequenceGenerator<>(this.operations, GenInputsAbstract.maxsize, startingIndex);
    initializeRuntimePrimitivesSeen();
    this.constructors =
        operations.stream().filter(o -> o.isConstructorCall()).collect(Collectors.toSet());
    this.constructorPrefix = selectConstructorPrefixSequence();
    if (constructorPrefix == null) {
      throw new RuntimeException(
          "Not possible to generate tests due to the impossibility of selecting constructors.");
    } else {
      this.executableConstructorPrefix = new ExecutableSequence(this.constructorPrefix);
      this.classUnderTest =
          executableConstructorPrefix
              .sequence
              .statements
              .get(executableConstructorPrefix.sequence.size() - 1)
              .getOutputType();
    }
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

    // it's been noted that during long executions, adding the generated sequence to component manager
    // causes the program to consume a large amount of the system's memory.
    boolean saveMemory = true;
    if (eSeq.sequence.hasActiveFlags() && !saveMemory) {
      componentManager.addGeneratedSequence(eSeq.sequence);
    }

    endTime = System.nanoTime();
    gentime += endTime - startTime;
    eSeq.gentime = gentime;

    return eSeq;
  }

  @Override
  public Set<Sequence> getAllSequences() {
    return new HashSet<>();
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

  /*
   Selects the new permutation in the set of possible permutations
  * */
  private List<TypedOperation> selectNextSequenceOfOperationsForNewUniqueSequence() {

    if (this.executableConstructorPrefix.hasNonExecutedStatements()) {
      this.prepareConstructorPrefix();
    }

    List<TypedOperation> nextPermutation = sequenceGenerator.next();

    List<String> operations =
        nextPermutation.stream().map(to -> to.getName()).collect(Collectors.toList());
    String sequence = String.join(",", operations);
    if (Log.isLoggingOn()) {

      Log.logLine("Sequence generator selected the following operations: " + sequence);
    }

    List<TypedOperation> nextSequence = Lists.newArrayList();

    for (TypedOperation op : nextPermutation) {
      if (op.isGeneric() || op.hasWildcardTypes()) {
        String opName = op.getName();
        op = instantiator.instantiate((TypedClassOperation) op);
        if (op == null) { //failed to instantiate generic
          Log.logLineIfOn(
              "Sequence generator discarded the sequence "
                  + sequence
                  + " because it could not instantiate operation "
                  + opName);
          return null;
        }
      }
      nextSequence.add(op);
    }

    return nextSequence;
  }

  private void prepareConstructorPrefix() {
    this.executableConstructorPrefix.execute(executionVisitor, checkGenerator);
    processSequence(executableConstructorPrefix);

    if (this.constructorPrefix.hasActiveFlags()) {
      componentManager.addGeneratedSequence(this.constructorPrefix);
    }
  }

  private List<TypedOperation> previousPermutation;

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

    List<TypedOperation> nextSequence = selectNextSequenceOfOperationsForNewUniqueSequence();

    if (nextSequence == null) {
      return null;
    }

    Sequence currentSequence = null;

    for (TypedOperation to : nextSequence) {
      // add flags here
      InputsAndSuccessFlag sequences = selectSimplestInputs(to, currentSequence);

      if (!sequences.success) {
        if (Log.isLoggingOn()) Log.logLine("Failed to find inputs for statement " + to.toString());
        return null;
      }

      Sequence concatSeq = Sequence.concatenate(sequences.sequences);

      // Figure out input variables.
      List<Variable> inputs = new ArrayList<>();
      for (Integer oneinput : sequences.indices) {
        // Assuming previous sequence has already had variables attributed to it:
        Variable v = concatSeq.getVariable(oneinput);
        inputs.add(v);
      }

      currentSequence = concatSeq.extend(to, inputs);

      // Discard if sequence is larger than size limit
      if (currentSequence.getNumberOfStatementsForCutFirstVariable() > GenInputsAbstract.maxsize) {
        if (Log.isLoggingOn()) {
          Log.logLine(
              "Sequence discarded because size "
                  + currentSequence.size()
                  + " exceeds maximum allowed size "
                  + GenInputsAbstract.maxsize);
        }
        return null;
      }
    }

    if (Log.isLoggingOn()) {
      Log.logLine(
          String.format(
              "Successfully created new unique sequence:%n%s%n", currentSequence.toString()));
    }

    previousPermutation = new LinkedList<>(nextSequence);
    return new ExecutableSequence(currentSequence);
  }

  // Select a constructor to be used as a prefix to the generated sequences of classes' methods.
  private Sequence selectConstructorPrefixSequence() {
    Sequence ctrSequence = null;

    // Try to find a parameterless constructor first:
    Optional<TypedOperation> parameterlessCtr =
        constructors
            .stream()
            .filter(
                c
                    -> c.getInputTypes().isEmpty()
                        && !c.getOutputType().getRuntimeClass().equals(Object.class))
            .findFirst();

    if (parameterlessCtr.isPresent()) {
      TypedOperation ctr = parameterlessCtr.get();
      ctrSequence = getSequenceWithInputsFromTypedOperation(ctr);

      if (ctrSequence != null) {
        return ctrSequence;
      }
    }

    // If not successful, returns the first constructor to be successfuly executed:
    for (TypedOperation cto : constructors) {
      ctrSequence = getSequenceWithInputsFromTypedOperation(cto);
      if (ctrSequence != null) {
        return ctrSequence;
      }
    }

    return null;
  }

  private Sequence getSequenceWithInputsFromTypedOperation(TypedOperation to) {

    if (to.isGeneric() || to.hasWildcardTypes()) {
      to = instantiator.instantiate((TypedClassOperation) to);
      if (to == null) {
        return null;
      }
    }

    InputsAndSuccessFlag sequences = selectSimplestInputs(to);
    Sequence seq = null;

    if (sequences.success) {
      Sequence concatSeq = Sequence.concatenate(sequences.sequences);
      // Figure out input variables.
      List<Variable> inputs = new ArrayList<>();
      for (Integer oneinput : sequences.indices) {
        Variable v = concatSeq.getVariable(oneinput);
        inputs.add(v);
      }
      seq = concatSeq.extend(to, inputs);
    }
    return seq;
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

  private InputsAndSuccessFlag selectSimplestInputs(TypedOperation operation, Sequence prefix) {
    InputsAndSuccessFlag result;

    if (constructorPrefix == null) {
      return new InputsAndSuccessFlag(true, new ArrayList<>(), new ArrayList<>());
    }

    TypeTuple inputTypes = operation.getInputTypes();

    List<Integer> variableIndices = new ArrayList<>(inputTypes.size());
    variableIndices.add(this.constructorPrefix.getLastVariable().index);

    List<Sequence> sequences = new ArrayList<>(inputTypes.size());
    int totStatements;

    if (prefix == null) {
      sequences.add(this.constructorPrefix);
      totStatements = this.constructorPrefix.size();
    } else {
      sequences.add(prefix);
      totStatements = prefix.size();
    }

    for (int i = 1; i < inputTypes.size(); i++) {
      Type inputType = inputTypes.get(i);
      SimpleList<Sequence> l = null;

      boolean inputTypeExistsInCurrentSequence =
          prefix != null && prefix.randomVariableForTypeLastStatement(inputType) != null;

      if (inputTypeExistsInCurrentSequence) {
        ArrayList<Sequence> l1 = new ArrayList<>();
        l1.add(prefix);
        l = new ArrayListSimpleList<>(l1);
      } else {
        switch (inputType.getCategory()) {
          case Array:
            l = getCandidateSequencesForArrayType(operation, i, inputType);
            break;
          case JDKCollectionSubtype:
            l =
                getCandidateSequencesForJDKCollectionSubtype(
                    operation, i, (InstantiatedType) inputType);
            break;
          case Other:
            Log.logLine("Will query component set for objects of type" + inputType);
            l = componentManager.getSequencesForType(operation, i);
            break;
        }
      }

      if (l.isEmpty()) {
        result = new InputsAndSuccessFlag(false, sequences, variableIndices);
        return result;
      }
      // Choose a sequence favoring small tests
      Sequence chosenSeq = Randomness.randomMemberWeighted(l);

      // Now, find values that satisfy the constraint set.
      Variable randomVariable = chosenSeq.randomVariableForTypeLastStatement(inputType);

      if (randomVariable == null) {
        throw new BugInRandoopException("type: " + inputType + ", sequence: " + chosenSeq);
      }

      if (inputTypeExistsInCurrentSequence) {
        variableIndices.add(randomVariable.index);
      } else {
        variableIndices.add(totStatements + randomVariable.index);
        totStatements += chosenSeq.size();
        sequences.add(chosenSeq);
      }
    }

    result = new InputsAndSuccessFlag(true, sequences, variableIndices);
    return result;
  }

  private InputsAndSuccessFlag selectSimplestInputs(TypedOperation operation) {
    return selectSimplestInputs(operation, null);
  }

  private SimpleList<Sequence> getCandidateSequencesForArrayType(
      TypedOperation operation, int i, Type inputType) {
    // 1. If T=inputTypes[i] is an array type, ask the component manager for
    // all sequences
    // of type T (list l1), but also try to directly build some sequences
    // that create arrays (list l2).

    SimpleList<Sequence> l;
    SimpleList<Sequence> l1 = componentManager.getSequencesForType(operation, i);
    if (Log.isLoggingOn()) {
      Log.logLine("Array creation heuristic: will create helper array of type " + inputType);
    }
    SimpleList<Sequence> l2 =
        HelperSequenceCreator.createArraySequence(componentManager, inputType);
    l = new ListOfLists<>(l1, l2);
    return l;
  }

  private SimpleList<Sequence> getCandidateSequencesForJDKCollectionSubtype(
      TypedOperation operation, int i, InstantiatedType inputType) {
    SimpleList<Sequence> l;
    InstantiatedType classType = inputType;

    SimpleList<Sequence> l1 = componentManager.getSequencesForType(operation, i);
    if (Log.isLoggingOn()) {
      Log.logLine("Collection creation heuristic: will create helper of type " + classType);
    }
    ArrayListSimpleList<Sequence> l2 = new ArrayListSimpleList<>();
    Sequence creationSequence = HelperSequenceCreator.createCollection(componentManager, classType);
    if (creationSequence != null) {
      l2.add(creationSequence);
    }

    if (l1.get(0).equals(constructorPrefix)) {
      l = new ListOfLists<>(l2);
    } else {
      l = new ListOfLists<>(l1, l2);
    }
    return l;
  }

  /**
   * Returns the set of sequences that are included in other sequences to
   * generate inputs (and, so, are subsumed by another sequence).
   */
  @Override
  public Set<Sequence> getSubsumedSequences() {
    return new HashSet<>();
  }

  @Override
  public BigInteger numGeneratedSequences() {
    return sequenceGenerator.getTotalSequencesIterated();
  }

  @Override
  protected boolean stop() {
    return !this.sequenceGenerator.hasNext() || super.stop();
  }

  @Override
  public void saveCurrentGenerationStep(File targetFile) {
    if (targetFile == null) {
      throw new IllegalArgumentException("targetFile cannot be null");
    }

    byte[] indexBytes = SerializationUtils.serialize(sequenceGenerator.getCurrentIndex());

    try {
      FileUtils.writeByteArrayToFile(targetFile, indexBytes);
    } catch (IOException e) {
      Log.logLineIfOn("Error saving current index to file: " + targetFile + " Error: " + e);
      e.printStackTrace();
    }
  }
}
