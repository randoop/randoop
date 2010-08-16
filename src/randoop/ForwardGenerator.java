package randoop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.main.GenInputsAbstract;
import randoop.util.ArrayListSimpleList;
import randoop.util.ListOfLists;
import randoop.util.Log;
import randoop.util.MultiMap;
import randoop.util.PrimitiveTypes;
import randoop.util.Randomness;
import randoop.util.Reflection;
import randoop.util.SimpleList;
import randoop.util.Reflection.Match;

/**
 * Randoop's forward, component-based generator.
 */
public class ForwardGenerator extends AbstractGenerator {

  /**
   * The set of ALL sequences ever generated, including
   * sequences that were executed and then discarded.
   */
  public final Set<Sequence> allSequences;
  
  /** Sequences that are used in other sequences (and are thus redundant) **/
  public Set<Sequence> subsumed_sequences = new LinkedHashSet<Sequence>();

  // For testing purposes only. If Globals.randooptestrun==false then the array
  // is never populated or queried. This set contains the same set of
  // components as the set "allsequences" above, but stores them as
  // strings obtained via the toCodeString() method.
  private final List<String> allsequencesAsCode = new ArrayList<String>();

  // For testing purposes only.
  private final List<Sequence> allsequencesAsList = new ArrayList<Sequence>();

  // The set of all primitive values seen during generation and execution
  // of sequences. This set is used to tell if a new primitive value has
  // been generated, to add the value to the components.
  private Set<Object> runtimePrimitivesSeen = new LinkedHashSet<Object>();
  
  // Stores runtime objects created during generation. The set of objects
  // is used to determine if a new sequences creates objects different from
  // those created by earlier sequences.
  protected ObjectCache objectCache = new ObjectCache(new EqualsMethodMatcher());

  public void setObjectCache(ObjectCache newCache) {
    if (newCache == null) throw new IllegalArgumentException();
    this.objectCache = newCache;
  }

  public ForwardGenerator(List<StatementKind> statements,
      long timeMillis, int maxSequences,
      ComponentManager componentManager,
      IStopper stopper, RandoopListenerManager listenerManager, List<ITestFilter> fs) {

    super(statements, timeMillis, maxSequences, componentManager, stopper, listenerManager, fs);

    this.allSequences = new LinkedHashSet<Sequence>();

    initializeRuntimePrimitivesSeen();
    
  }

  /**
   * The runtimePrimitivesSeen set contains primitive values seen
   * during generation/execution and is used to determine new values
   * that should be added to the component set. The component set
   * initially contains a set of primitive sequences; this method
   * puts those primitives in this set.
   */
  private void initializeRuntimePrimitivesSeen() {
    for (Sequence s : componentManager.getAllPrimitiveSequences()) {
      ExecutableSequence es = new ExecutableSequence(s);
      es.execute(null);
      NormalExecution e = (NormalExecution)es.getResult(0);
      Object runtimeValue = e.getRuntimeValue();
      runtimePrimitivesSeen.add(runtimeValue);
    }
  }

  @Override
  public int numSequences() {
    return allSequences.size();
  }

  public ExecutableSequence step() {

    long startTime = System.nanoTime();

    if (componentManager.numGeneratedSequences() % GenInputsAbstract.clear == 0)
      componentManager.clearGeneratedSequences();

    ExecutableSequence eSeq = null;

    eSeq = createNewUniqueSequence();
    if (eSeq == null) {
      return null;
    }

    assert eSeq != null;

    if (GenInputsAbstract.dontexecute) {
      this.componentManager.addGeneratedSequence(eSeq.sequence);
      return null;
    }

    AbstractGenerator.currSeq = eSeq.sequence;

    long endTime = System.nanoTime();
    long gentime = endTime - startTime;
    startTime = endTime; // reset start time.

    eSeq.execute(executionVisitor);

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

  public Set<Sequence> allSequences() {
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
   */
  public void processSequence(ExecutableSequence seq) {

    if (GenInputsAbstract.offline) {
      if (Log.isLoggingOn()) {
        Log.logLine("Making all indices active (offline generation specified; sequences are not executed).");
      }
      seq.sequence.setAllActiveFlags();
      return;
    }

    if (seq.hasNonExecutedStatements()) {
      if (Log.isLoggingOn()) {
        Log.logLine("Making all indices inactive (sequence has non-executed statements, so judging it inadequate for further extension).");
      }
      seq.sequence.clearAllActiveFlags();
      return;
    }

    if (seq.hasFailure()) {
      if (Log.isLoggingOn()) {
        Log.logLine("Making all indices inactive (sequence reveals a failure, so judging it inadequate for further extension)");
      }
      seq.sequence.clearAllActiveFlags();
      return;
    }

    if (!seq.isNormalExecution()) {
      if (Log.isLoggingOn()) {
        Log.logLine("Making all indices inactive (exception thrown, or failure revealed during execution).");
      }
      seq.sequence.clearAllActiveFlags();
      return;
    }
    
    // If runtime value is a primitive value, clear active flag, and
    // if the value is new, add a sequence corresponding to that value.
    for (int i = 0; i < seq.sequence.size(); i++) {

      // type ensured by isNormalExecution clause ealier in this method.
      NormalExecution e = (NormalExecution)seq.getResult(i);
      Object runtimeValue = e.getRuntimeValue();
      if (runtimeValue == null) {
        if (Log.isLoggingOn()) {
          Log.logLine("Making index " + i + " inactive (value is null)");
        }
        seq.sequence.clearActiveFlag(i);
        continue;
      }
      
      Class<?> objectClass = runtimeValue.getClass();
      
      if (PrimitiveTypes.isBoxedOrPrimitiveOrStringType(objectClass)) {
        if (Log.isLoggingOn()) {
          Log.logLine("Making index " + i + " inactive (value is a primitive)");
        }
        seq.sequence.clearActiveFlag(i);
        
        boolean looksLikeObjToString = (runtimeValue instanceof String)
          && PrimitiveTypes.looksLikeObjectToString((String)runtimeValue);
        boolean tooLongString = (runtimeValue instanceof String)
          && !PrimitiveTypes.stringLengthOK((String)runtimeValue);
        if (!looksLikeObjToString && !tooLongString && runtimePrimitivesSeen.add(runtimeValue)) {
          // Have not seen this value before; add it to the component set.
          componentManager.addGeneratedSequence(PrimitiveOrStringOrNullDecl.sequenceForPrimitive(runtimeValue));
        }
      } else if (GenInputsAbstract.use_object_cache) {
        objectCache.setActiveFlags(seq, i);
      } else {
        if (Log.isLoggingOn()) {
          Log.logLine("Making index " + i + " active.");
        }

      }
    }

  }

  /**
   * Tries to create and execute a new sequence. If the sequence is new (not
   * already in the specified component manager), then it is executed and
   * added to the manager's sequences. If the sequence created is already in
   * the manager's sequences, this method has no effect, and returns null.
   */
  private ExecutableSequence createNewUniqueSequence() {

    if (Log.isLoggingOn()) Log.logLine("-------------------------------------------");

    StatementKind statement = null;

    if (this.statements.isEmpty())
      return null;

    // Select a StatementInfo
    statement = Randomness.randomMember(this.statements);
    if (Log.isLoggingOn()) Log.logLine("Selected statement: " + statement.toString());

    // jhp: add flags here
    InputsAndSuccessFlag  sequences = selectInputs(statement);

    if (!sequences.success) {
      if (Log.isLoggingOn()) Log.logLine("Failed to find inputs for statement.");
      return null;
    }

    Sequence concatSeq = Sequence.concatenate(sequences.sequences);

    // Figure out input variables.
    List<Variable> inputs = new ArrayList<Variable>();
    for (Integer oneinput : sequences.indices) {
      Variable v = concatSeq.getVariable(oneinput);
      inputs.add(v);
    }

    Sequence newSequence = concatSeq.extend(statement, inputs);

    // With .5 probability, do a primitive value heuristic.
    if (GenInputsAbstract.repeat_heuristic && Randomness.nextRandomInt(10) == 0) {
      int times = Randomness.nextRandomInt(100);
      newSequence = newSequence.repeatLast(times);
      if (Log.isLoggingOn()) Log.log(">>>" + times + newSequence.toCodeString());
    }

    // If parameterless statement, subsequence inputs
    // will all be redundant, so just remove it from list of statements.
    if (statement.getInputTypes().size() == 0) {
      statements.remove(statement);
    }

    // If sequence is larger than size limit, try again.
    if (newSequence.size() > GenInputsAbstract.maxsize) {
      if (Log.isLoggingOn()) Log.logLine("Sequence discarded because size " + newSequence.size() + " exceeds maximum allowed size " + GenInputsAbstract.maxsize);
      return null;
    }

    randoopConsistencyTests(newSequence);

    if (this.allSequences.contains(newSequence)) {
      if (Log.isLoggingOn()) Log.logLine("Sequence discarded because the same sequence was previously created.");
      return null;
    }

    this.allSequences.add(newSequence);

    for (Sequence s : sequences.sequences) {
      s.lastTimeUsed = java.lang.System.currentTimeMillis();
    }

    randoopConsistencyTest2(newSequence);

    if (Log.isLoggingOn()) {
      Log.logLine("Successfully created new unique sequence:" + newSequence.toString());
    }
    //System.out.println("###" + statement.toStringVerbose() + "###" + statement.getClass());

    // Keep track of any input sequences that are used in this sequence
    // Tests that contain only these sequences are probably redundant
    for (Sequence is : sequences.sequences) {
      subsumed_sequences.add (is);
    }

    return new ExecutableSequence(newSequence);
  }

  // Adds the string corresponding to the given newSequences to the
  // set allSequencesAsCode. The latter set is intended to mirror
  // the set allSequences, but stores strings instead of Sequences.
  protected void randoopConsistencyTest2(Sequence newSequence) {
    // Testing code.
    if (GenInputsAbstract.debug_checks) {
      this.allsequencesAsCode.add(newSequence.toCodeString());
      this.allsequencesAsList.add(newSequence);
    }
  }

  // Checks that the set allSequencesAsCode contains a set of strings
  // equivalent to the sequences in allSequences.
  protected void randoopConsistencyTests(Sequence newSequence) {
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
          Sequence  co = this.allsequencesAsList.get(index);
          co.equals(newSequence);
          b.append("new component:" + Globals.lineSep + "" + newSequence.toString()  + "" + Globals.lineSep + "as code:" + Globals.lineSep + "" + code + Globals.lineSep);
          b.append("existing component:" + Globals.lineSep + "" + this.allsequencesAsList.get(index).toString() + "" + Globals.lineSep + "as code:" + Globals.lineSep + ""
              + this.allsequencesAsList.get(index).toCodeString());
          throw new IllegalStateException(b.toString());
        }
      }
    }
  }

  // This method is responsible for doing two things:
  //
  // 1. Selecting at random a collection of sequences that can be used to
  //    create input values for the given statement, and
  //
  // 2. Selecting at random valid indices to the above sequence specifying
  //    the values to be used as input to the statement.
  //
  // The selected sequences and indices are wrapped in an InputsAndSuccessFlag
  // object and returned. If an appropriate collection of sequences and indices
  // was not found (e.g. because there are no sequences in the componentManager
  // that create values of some type required by the statement), the success flag
  // of the returned object is false.
  @SuppressWarnings("unchecked")
  private InputsAndSuccessFlag selectInputs(StatementKind statement) {

    // Variable inputTypes containsthe  values required as input to the
    // statement given as a parameter to the selectInputs method.

    List<Class<?>> inputTypes = statement.getInputTypes();


    // The rest of the code in this method will attempt to create
    // a sequence that creates at least one value of type T for
    // every type T in inputTypes, and thus can be used to create all the
    // inputs for the statement.
    // We denote this goal sequence as "S". We don't create S explicitly, but
    // define it as the concatenation of the following list of sequences.
    // In other words, S = sequences[0] + ... + sequences[sequences.size()-1].
    // (This representation choice is for efficiency: it is cheaper to perform
    //  a single concatenation of the subsequences in the end than repeatedly
    // extending S.)
    
    List<Sequence> sequences = new ArrayList<Sequence>();
    
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

    List<Integer> variables = new ArrayList<Integer>();

    // [Optimization]
    // The following two variables are used in the loop below only when
    // an alias ratio is present (GenInputsAbstract.alias_ratio != null).
    // Their purpose is purely to improve efficiency. For a given loop iteration
    // i, "types" contains the types of all variables in S, and  "typesToVars"
    // maps each type to all variable indices of the given type.
    SubTypeSet types = new SubTypeSet(false);
    MultiMap<Class<?>, Integer> typesToVars = new MultiMap<Class<?>, Integer>();

    for (int i = 0; i < inputTypes.size(); i++) {
      Class<?> t = inputTypes.get(i);

      // TODO Does this ever happen?
      if(!Reflection.isVisible(t)) return new InputsAndSuccessFlag (false, null, null);

      // true if statement st represents an instance method, and we are currently
      // selecting a value to act as the receiver for the method.
      boolean isReceiver = (i == 0 && (statement instanceof RMethod)
          && (!((RMethod) statement).isStatic()));

      // If alias ratio is given, attempt with some probability to use a variable already in S.
      if (GenInputsAbstract.alias_ratio != 0 &&
          Randomness.weighedCoinFlip(GenInputsAbstract.alias_ratio)) {

        // candidateVars will store the indices that can serve as input to the i-th input in st.
        List<SimpleList<Integer>> candidateVars = new ArrayList<SimpleList<Integer>>();
        
        // For each type T in S compatible with inputTypes[i], add all the indices in S of type T.
        for (Class<?> match : types.getMatches(t)) {
          // Sanity check: the domain of typesToVars contains all the types in variable types.
          assert typesToVars.keySet().contains(match);
          candidateVars.add(new ArrayListSimpleList<Integer>(new ArrayList<Integer>(typesToVars.getValues(match))));
        }
        
        // If any type-compatible variables found, pick one at random as the i-th input to st.
        SimpleList<Integer> candidateVars2 = new ListOfLists<Integer>(candidateVars);
        if (candidateVars2.size() > 0) {
          int randVarIdx = Randomness.nextRandomInt(candidateVars2.size());
          Integer randVar = candidateVars2.get(randVarIdx);
          variables.add(randVar);
          continue;
        }
      }

      // If we got here, it means we will not attempt to use a value already defined in S,
      // so we will have to augment S with new statements that yield a value of type inputTypes[i].
      // We will do this by assembling a list of candidate sequences n(stored in the list declared
      // immediately below) that create one or more values of the appropriate type, 
      // randomly selecting a single sequence from this list, and appending it to S.
      SimpleList<Sequence> l = null;
      
      // We use one of three ways to gather candidate sequences, but the third case below
      // is by far the most common.

      if (GenInputsAbstract.always_use_ints_as_objects && t.equals(Object.class)) {
        
        // 1. OBSCURE, applicable only for branch-directed generation project. Get all
        //    sequences that create one or more integer. Applicable only when inputTypes[i]
        //    is "Object" and always_use_ints_as_objects option is specified.
        if (Log.isLoggingOn()) Log.logLine("Integer-as-object heuristic: will use random Integer.");
        l = componentManager.getSequencesForType(int.class, false);
        
      } else if (t.isArray()) {
        
        // 2. If T=inputTypes[i] is an array type, ask the component manager for all sequences
        //    of type T (list l1), but also try to directly build some sequences that create arrays (list l2).
         SimpleList<Sequence> l1 = componentManager.getSequencesForType(statement, i);
         if (Log.isLoggingOn()) Log.logLine("Array creation heuristic: will create helper array of type " + t);
         SimpleList<Sequence> l2 = HelperSequenceCreator.createSequence(componentManager, t);
         l = new ListOfLists<Sequence>(l1, l2);
         
      } else {
        
        // 3. COMMON CASE: ask the component manager for all sequences that yield the required type.
        if (Log.isLoggingOn()) Log.logLine("Will query component set for objects of type" + t);
        l = componentManager.getSequencesForType(statement, i);
      }
      assert l != null;
      
      if (Log.isLoggingOn()) Log.logLine("components: " + l.size());
      
      // If we were not able to find (or create) any sequences of type inputTypes[i], and we are
      // allowed the use null values, use null. If we're not allowed, then return with failure.
      if (l.size() == 0) {
        if (isReceiver || GenInputsAbstract.forbid_null) {
          if (Log.isLoggingOn()) Log.logLine("forbid-null option is true. Failed to create new sequence.");
          return new InputsAndSuccessFlag (false, null, null);
        } else {
          if (Log.isLoggingOn()) Log.logLine("Will use null as " + i + "-th input");
          StatementKind st = PrimitiveOrStringOrNullDecl.nullOrZeroDecl(t);
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

      // At this point, we have one or more sequences that create non-null values of type inputTypes[i].
      // However, the user may have requested that we use null values as inputs with some given frequency.
      // If this is the case, then use null instead with some probability. 
      if (!isReceiver&& GenInputsAbstract.null_ratio != 0
          && Randomness.weighedCoinFlip(GenInputsAbstract.null_ratio)) {
        if (Log.isLoggingOn()) Log.logLine("null-ratio option given. Randomly decided to use null as input.");
        StatementKind st = PrimitiveOrStringOrNullDecl.nullOrZeroDecl(t);
        Sequence seq = new Sequence().extend(st, new ArrayList<Variable>());
        variables.add(totStatements);
        sequences.add(seq);
        assert seq.size() == 1;
        totStatements++;
        continue;
      }

      // At this point, we have a list of candidate sequences and need to select a
      // randomly-chosen sequence from the list.
      Sequence chosenSeq = null;
      if (GenInputsAbstract.small_tests) {
        chosenSeq = Randomness.randomMemberWeighted(l);
      } else {
        chosenSeq = Randomness.randomMember(l);
      }

      // Now, find values that satisfy the constraint set.
      Match m = Match.COMPATIBLE_TYPE;
      //if (i == 0 && statement.isInstanceMethod()) m = Match.EXACT_TYPE;
      Variable randomVariable = chosenSeq.randomVariableForTypeLastStatement(t, m);

      // We are not done yet: we have chosen a sequence that yields a value of the required
      // type inputTypes[i], but there may be more than one such value. Our last random
      // selection step is to select from among all possible values.
      //if (i == 0 && statement.isInstanceMethod()) m = Match.EXACT_TYPE;
      if (randomVariable == null) {
        throw new BugInRandoopException("type: " + t + ", sequence: " + chosenSeq);
      }

      // If we were unlucky and selected a null value as the receiver
      // for a method call, return with failure.
      if (i == 0
          && (statement instanceof RMethod)
          && (!((RMethod) statement).isStatic())
          && chosenSeq.getCreatingStatement(randomVariable) instanceof PrimitiveOrStringOrNullDecl)
        return new InputsAndSuccessFlag (false, null, null);

      // [Optimization.] Update optimization-related variables "types" and "typesToVars".
      if (GenInputsAbstract.alias_ratio != 0) {
        // Update types and typesToVars.
        for (int j = 0 ; j < chosenSeq.size() ; j++) {
          StatementKind stk = chosenSeq.getStatementKind(j);
          if (stk instanceof PrimitiveOrStringOrNullDecl)
            continue; // Prim decl not an interesting candidate for multiple uses.
          Class<?> outType = stk.getOutputType();
          types.add(outType);
          typesToVars.add(outType, totStatements + j);
        }
      }

      variables.add(totStatements + randomVariable.index);
      sequences.add(chosenSeq);
      totStatements += chosenSeq.size();
    }

    return new InputsAndSuccessFlag (true, sequences, variables);
  }

  /**
   * Returns the set of sequences that are used as inputs in other sequences
   * (and can thus be thought of as subsumed by another sequence).  
   */
  public Set<Sequence> subsumed_sequences() {
    return subsumed_sequences;
  }
}
