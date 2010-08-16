package randoop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import randoop.main.GenInputsAbstract;
import randoop.util.ArrayListSimpleList;
import randoop.util.ListOfLists;
import randoop.util.OneMoreElementList;
import randoop.util.Randomness;
import randoop.util.RecordListReader;
import randoop.util.RecordProcessor;
import randoop.util.Reflection;
import randoop.util.SimpleList;
import randoop.util.WeightedElement;
import randoop.util.Reflection.Match;

/**
 * Immutable.
 * <p>
 * A sequence of statements. Each element in the sequence represents a
 * particular Statement, like a method call "Foo f = m(i1...iN)" or a
 * declaration "int x = 0".
 * <p>
 * This class represents only the structure of a well-formed sequence of
 * statements, and does not contain any information about the runtime behavior
 * of the sequence. The class randoop.ExecutableSequence adds
 * functionality that executes the sequence.
 */
public final class Sequence implements Serializable, WeightedElement {

  private static final long serialVersionUID = -4345602588310287644L;

  public double lastTimeUsed = java.lang.System.currentTimeMillis();

  // The list of statements.
  public final SimpleList<Statement> statements;

  // The values involved in the last statement (receiver, return
  // value, parameters). Should be final but cannot because of serialization.
  // This info is used by some generators.
  private transient/* final */List<Variable> lastStatementVariables;

  // The types of the values in lastStatementTypes.
  // Should be final but cannot because of serialization.
  // This info is used by some generators.
  private transient/* final */List<Class<?>> lastStatementTypes;
  
  /*
   * Weight is used by heuristic that favors smaller sequences
   * so it makes sense to define weight as the inverse of size.
   */
  public double getWeight() {
    return 1/(double)size();
  }

  /**
   * The statement for the statement at the given index.
   */

  public final StatementKind getStatementKind(int index) {
    return getStatementWithInputs(index).statement;
  }

  /**
   * The number of statements in the sequence.
   */
  public final int size() {
    return statements.size();
  }

  /** The value created by the ith statement. */
  public Variable getVariable(int i) {
    checkIndex(i);
    return new Variable(this, i);
  }

  /** The values associated with this sequence. */
  public List<Variable> getAllVariables() {
    List<Variable> retval = new ArrayList<Variable>();
    for (int i = 0 ; i < this.statements. size() ; i++) {
      retval.add(new Variable(this, i));
    }
    return retval;
  }

  /**
   * The variables involved in the last statement.
   * This includes the output variable.
   */
  public List<Variable> getLastStatementVariables() {
    return this.lastStatementVariables;
  }

  /**
   * The types of all the variables involved in the last statement.
   * This includes the output variable.
   * The types returned are not the types in the signature of the
   * StatementKind, but the types of the variables.
   */
  public List<Class<?>> getLastStatementTypes() {
    return this.lastStatementTypes;
  }


  /** The value created by the last statement in the sequence. */
  public Variable getLastVariable() {
    return new Variable(this, this.statements.size() - 1);
  }

  /** The last Statement in the sequence. */
  public StatementKind getLastStatement() {
    return this.getStatementKind(this.size() - 1);
  }

  /** The statement that created this value. */
  public StatementKind getCreatingStatement(Variable value) {
    if (value.sequence != this)
      throw new IllegalArgumentException("value.owner != this");
    return statements.get((value).index).statement;
  }

  /** The inputs for the ith statement. */
  public List<Variable> getInputs(int statementIndex) {
    List<Variable> inputsAsVariables = new ArrayList<Variable>();
    for (RelativeNegativeIndex relIndex : this.statements.get(statementIndex).inputs)
      inputsAsVariables.add(getVariableForInput(statementIndex, relIndex));
    return inputsAsVariables;
  }

  /** A Java source code representation of this sequence. */
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < size(); i++) {
      printStatement(b, i);
    }
    return b.toString();
  }

  /**
   * Equivalent to toParseableString().
   */
  public String toString() {
    return toParseableString();
  }
  
  // A set of bits, where there is one bit associated with each index.
  // Active flags are used during generation, to determine what values
  // in an existing sequence are useful to be used as inputs when
  // creating a new sequence out of the existing one.
  public BitSet activeFlags;

  public boolean hasActiveFlags() {
    return !activeFlags.isEmpty();
  }

  public boolean isActive(int i) {
    return activeFlags.get(i);
  }

  public void setAllActiveFlags() {
    activeFlags.set(0, this.size());
  }

  public void clearAllActiveFlags() {
    activeFlags.clear(0, this.size());
  }

  public void setActiveFlag(int i) {
    activeFlags.set(i);
  }

  public void clearActiveFlag(int i) {
    activeFlags.clear(i);
  }

  // Serialization.
  // Recovers transient fields (recomputes them).
  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    // customized deserialization code
    computeLastStatementInfo();
  }

  // Used internally (i.e. in package randoop.sequence) to represent inputs
  // to a statement.
  //
  // IMPLEMENTATION NOTE: Recall that a sequence is a sequence
  // of statements where the inputs to a statement are values created
  // be earlier statements. Instead of using a Variable to represent such
  // inputs, we use a RelativeNegativeIndex, which is just a wrapper
  // for an integer. The integer represents a negative offset from the
  // statement index in which this RelativeNegativeIndex lives, and
  // the offset points to the statement that created the values that is
  // used as an input. In other words, a RelativeNegativeIndex says
  // "I represent the value created by the N-th statement above me".
  //
  // For example, the sequence
  //
  // x = new Foo(); Bar b = x.m();
  //
  // is internally represented as follows:
  //
  // first element: Foo() applied to inputs []
  // second element: m() applied to inputs [-1]
  //
  // Here is a brief history of why we use this particular representation.
  //
  // The very first way we represented inputs to a statement was
  // using a list of StatementWithInput objects, i.e. an input was just
  // a reference to a previous statement that created the input value.
  // For example, a sequence might be represented as follows:
  //
  // StatementWithInputs@123: Foo() applied to inputs []
  // StatementWithInputs@124: m() applied to inputs [StatementWithInputs@123]
  //
  // We discovered that a big slowdown in the input generator was that we were
  // consuming lots of memory when creating sequences: for example, in memory,
  // when extending the sequence x = new Foo(); Bar b = x.m(); we cloned each
  // statement, created a new list, added the cloned statements, and finally
  // appended the new statement to the new list.
  //
  // Instead of cloning, we might imagine just using the original statements
  // in the new sequence. This does not work. For example, let's say that we
  // implement this scheme,
  // so that everywhere we need "new Foo()" we use the same statement (more
  // precisely, the same StatementWithInputs). Then, we cannot express
  // Foo f1 = new Foo(); Foo f2 = new Foo(); f1.equals(f2); because its
  // internal representation must be
  //
  // StatementWithInputs@123: Foo() applied to inputs []
  // StatementWithInputs@123: Foo() applied to inputs []
  // StatementWithInputs@125: Object.equals(Object) applied to inputs
  // [StatementWithInputs@123,
  // StatementWithInputs@123]
  //
  // It's clear that if we want to reuse statements, we cannot directly
  // use the statements as inputs.
  //
  // We can instead use indices: 0 represents the value created by the
  // first statement, 1 the second, etc. Now we can express the above
  // sequence:
  //
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [0, 1]
  //
  // This scheme makes it relatively expensive to concatenate sequences
  // (some generators do lots of concatenation, so concatenation is the
  // hotspot). Because indexing is absolute, some statements
  // will need to have their input indices updated. For example, let's say we
  // wanted to concatenate two copies of the
  // last sequence above. We cannot just concatenate the statements, because
  // then we have
  //
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [0, 1]
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [0, 1]
  //
  // While we really want
  //
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [0, 1]
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [3, 4]
  //
  // This means that we need to (1) adjust indices, which takes time, and
  // (2) create new statements that represent the adjusted indices, which
  // breaks the "reuse statements" idea.
  //
  // Relative indices are the current implementation. Instead of representing
  // inputs as indices that start from the beginning of the sequence, a
  // statement's
  // indices are represented by a relative, negative offsets:
  //
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [-2, -1]
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [-2, -1]
  //
  // Now concatenation is easier: to concatenate two sequences, concatenate
  // their statements. Also, we do not need to create any new
  // statements.
  protected static final class RelativeNegativeIndex implements Serializable {
    private static final long serialVersionUID = -79358572221311408L;

    public final int index;

    RelativeNegativeIndex(int index) {
      if (index >= 0)
        throw new IllegalArgumentException("invalid index (expecting non-positive): " + index);
      this.index = index;
    }

    @Override
    public String toString() {
      return Integer.toString(index);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof RelativeNegativeIndex))
        return false;
      return this.index == ((RelativeNegativeIndex) o).index;
    }

    @Override
    public int hashCode() {
      return this.index;
    }
  }

  /**
   * Returns the relative negative index that would result if we use the given
   * value as an input to the statement at position statementPosition.
   */
  public static RelativeNegativeIndex getRelativeIndexForVariable(int statementPosition, Variable v) {
    if (v.index >= statementPosition)
      throw new IllegalArgumentException();
    return new RelativeNegativeIndex(-(statementPosition - v.index));
  }

  /**
   * Returns the Variable corresponding to the given input, which is an input to
   * the statement at position statementPosition.
   */
  public Variable getVariableForInput(int statementPosition, RelativeNegativeIndex input) {
    int absoluteIndex = statementPosition + input.index;
    if (absoluteIndex < 0)
      throw new IllegalArgumentException("invalid index (expeciting non-negative): " + absoluteIndex);
    return new Variable(this, absoluteIndex);
  }

  /** Create a new, empty sequence. */
  public Sequence() {
    this(new ArrayListSimpleList<Statement>(), 0, 0);
  }

  /**
   * Returns a sequence that consists of a single primitive declaration
   * statement (e.g. int i = 1;)
   */
  public static Sequence create(PrimitiveOrStringOrNullDecl info) {
    return new Sequence().extend(info, new ArrayList<Variable>());
  }

  /**
   * Returns a sequence that is of the form "Foo f = null;" where Foo is the
   * given class.
   */
  public static Sequence zero(Class<?> c) {
    return create(PrimitiveOrStringOrNullDecl.nullOrZeroDecl(c));
  }


  // Create a sequence with the given statements.
  /*package*/ public Sequence(SimpleList<Statement> statements) {
    this(statements, computeHashcode(statements), computeNetSize(statements));
  }

  // The hashcode of a sequence is the sum of each statement's hashcode.
  // This seems good enough, and it makes computing hashCode of a
  // concatenation of sequences faster (it's just the addition of each sequence's'
  // hashCode). Otherwise, hashCode computation used to be a hotspot.
  private static int computeHashcode(SimpleList<Statement> statements) {
    int hashCode = 0;
    for (int i = 0; i < statements.size(); i++) {
      Statement s = statements.get(i);
      hashCode += s.hashCode();
    }
    return hashCode;
  }

  // The number of statements that are not primitive declarations, i.e. "int var7 = 0".
  private static int computeNetSize(SimpleList<Statement> statements) {
    int netSize = 0;
    for (int i = 0; i < statements.size(); i++) {
      StatementKind s = statements.get(i).statement;
      if (s instanceof PrimitiveOrStringOrNullDecl)
        continue;
       netSize++;
    }
    return netSize;
  }

  // Create a sequence that has the given statements and hashCode (hashCode is
  // for optimization).
  //
  // See comment at computeHashCode method for notes on hashCode.
  private Sequence(SimpleList<Statement> statements, int hashCode, int netSize) {
    if (statements == null)
      throw new IllegalArgumentException("`statements' argument cannot be null");
    this.statements = statements;
    this.savedHashCode = hashCode;
    this.savedNetSize = netSize;
    computeLastStatementInfo();
    this.activeFlags = new BitSet(this.size());
    setAllActiveFlags();
    checkRep();
  }

  // Set lastStatementVariables and lastStatementTypes to their appropriate
  // values. See documentation for these fields for more info.
  private void computeLastStatementInfo() {
    this.lastStatementTypes = new ArrayList<Class<?>>();
    this.lastStatementVariables = new ArrayList<Variable>();

    if (this.statements.size() > 0) {
      Statement si = this.statements.get(this.statements.size() - 1);

      // Process return value
      if (si.statement.getOutputType().equals(void.class)) {
        lastStatementTypes.add(void.class); // used for void methods and Dummy statements
      } else {
        lastStatementTypes.add(si.statement.getOutputType());
      }
      lastStatementVariables.add(new Variable(this, this.statements.size() - 1));

      // Process input arguments.
      if (si.inputs.size() != si.statement.getInputTypes().size())
        throw new RuntimeException(si.inputs + ", " + si.statement.getInputTypes() + ", " + si.statement.toString());

      List<Variable> v = this.getInputs(this.statements.size() - 1);
      if (v.size() != si.statement.getInputTypes().size()) throw new RuntimeException();

      for (int i = 0; i < v.size(); i++) {
        Variable value = v.get(i);
        assert Reflection.canBeUsedAs(value.getType(), si.statement.getInputTypes().get(i));
        lastStatementTypes.add(value.getType());
        Variable idx = getVariableForInput(this.size() - 1, si.inputs.get(i)); // XXX bogus. Isn't this just recomputing v from above?
        lastStatementVariables.add(idx);
      }
    }
  }

  /**
   * Representation invariant check.
   */
  private void checkRep() {

    if (!GenInputsAbstract.debug_checks)
      return;

    if (statements == null)
      throw new RuntimeException("statements == null");

    for (int si = 0; si < this.statements.size(); si++) {

      Statement statementWithInputs = this.statements.get(si);

      // No nulls.
      if (statementWithInputs == null)
        throw new IllegalStateException("Null statement in sequence:" + Globals.lineSep + this.toString());
      if (statementWithInputs.statement == null || statementWithInputs.inputs == null)
        throw new IllegalArgumentException("parameters cannot be null.");

      // The inputs to the statement are valid: there's the right number
      // of them,
      // and they refer to appropriate input values.
      if (statementWithInputs.statement.getInputTypes().size() != statementWithInputs.inputs.size())
        throw new IllegalArgumentException("statement.getInputConstraints().size()="
            + statementWithInputs.statement.getInputTypes().size() + " is different from inputIndices.length="
            + statementWithInputs.inputs.size() + ", sequence: " + this.toString());
      for (int i = 0; i < statementWithInputs.inputs.size(); i++) {
        int index = statementWithInputs.inputs.get(i).index;
        if (index >= 0)
          throw new IllegalStateException();
        Class<?> newRefConstraint = statements.get(si + statementWithInputs.inputs.get(i).index).statement.getOutputType();
        if (newRefConstraint == null)
          throw new IllegalStateException();
        if (!Reflection.canBeUsedAs(newRefConstraint, statementWithInputs.statement.getInputTypes().get(i)))
          throw new IllegalArgumentException(i + "th input constraint " + newRefConstraint + " does not imply " + "statement's " + i
              + "th input constraint " + statementWithInputs.statement.getInputTypes().get(i) + Globals.lineSep + ".Sequence:" + Globals.lineSep
              + this.toString());
      }
    }
  }

  /**
   * Two sequences are equal if their statements(+inputs) are element-wise
   * equal.
   */
  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Sequence))
      return false;
    if (o == this)
      return true;
    Sequence other = (Sequence) o;
    if (this.getStatementsWithInputs().size() != other.getStatementsWithInputs().size())
      return GenInputsAbstract.debug_checks ? verifyFalse("size", other) : false;
      for (int i = 0; i < this.statements.size(); i++) {
        Statement thisS = null;
        Statement otherS = null;
        thisS = this.statements.get(i);
        otherS = other.statements.get(i);
        if (GenInputsAbstract.debug_checks) {
          assert this.statements.get(i) == thisS;
          assert other.statements.get(i) == otherS;
        }
        if (!thisS.statement.equals(otherS.statement))
          return GenInputsAbstract.debug_checks ? verifyFalse("statement index " + i, other) : false;
          if (thisS.inputs.size() != otherS.inputs.size())
            return GenInputsAbstract.debug_checks ? verifyFalse("statement index " + i + " inputs size ", other) : false;
            for (int j = 0; j < thisS.inputs.size(); j++) {
              if (!thisS.inputs.get(j).equals(otherS.inputs.get(j)))
                return GenInputsAbstract.debug_checks ? verifyFalse("statement index " + i + " input " + j, other) : false;
            }
      }
      return true;
  }

  // Debugging helper for equals method.
  private boolean verifyFalse(String message, Sequence other) {
    if (this.toString().equals(other.toString()))
      throw new IllegalStateException(message + " : " + this.toString());
    return false;
  }

  // A saved copy of this sequence's hashcode to avoid recomputation.
  protected final int savedHashCode;

  // A saved copy of this sequence's net size to avoid recomputation.
  private final int savedNetSize;

  public final int getNetSize() {
    return savedNetSize;
  }

  // See comment at computeHashCode method for notes on hashCode.
  @Override
  public final int hashCode() {
    return savedHashCode;
  }

  /**
   * True iff this sequence contains a statement at the given index.
   */
  private boolean isValidIndex(int index) {
    if (index < 0)
      return false;
    if (index > this.size() - 1)
      return false;
    return true;
  }

  /**
   * An unmodifiable list of all the statements in this sequence.
   */
  final SimpleList<Statement> getStatementsWithInputs() {
    // The list is constructed unmodifiable so we can just return it.
    return this.statements;
  }

  /** The statement(+inputs) at the given index.
   */
  protected final Statement getStatementWithInputs(int index) {
    if (!isValidIndex(index))
      throw new IllegalArgumentException("Index " + index + " not valid for sequece " + this);
    return this.getStatementsWithInputs().get(index);
  }


  public Variable randomVariableForTypeLastStatement(Class<?> t, Match match) {
    return randomVariableOfTypeLastStatement(t, match);
  }

  public Variable randomVariableForType(Class<?> t, Match match) {
    List<Variable> possibleVariables = getVariablesOfType(t, match);
    if (possibleVariables.size() == 0)
      return null;
    return Randomness.randomMember(possibleVariables);
  }

  /**
   * A value declared in this sequence whose type matches the given
   * class. Returns null if there are no matches.
   */
  public final Variable randomVariableOfTypeLastStatement(Class<?> clazz, Reflection.Match match) {
    List<Variable> possibleVariables = valuesAppearingInLastStatement(clazz, match);
    if (possibleVariables.isEmpty())
      return null;
    return Randomness.randomMember(possibleVariables);
  }

  /** All the values declared in this sequences whose type matches the
   * given class. Returns an empty list if there are no matches.
   */
  public final List<Variable> valuesAppearingInLastStatement(Class<?> clazz, Reflection.Match match) {
    if (clazz == null || match == null)
      throw new IllegalArgumentException("parameters cannot be null.");
    List<Variable> possibleIndices = new ArrayList<Variable>(this.lastStatementVariables.size());
    for (int ithOutputIndex = 0; ithOutputIndex < this.lastStatementVariables.size(); ithOutputIndex++) {
      Variable i = this.lastStatementVariables.get(ithOutputIndex);
      Statement s = statements.get(i.index);
      if (!s.isVoidMethodCall() && varTypeMatches(s.statement.getOutputType(), clazz, match)) {
        possibleIndices.add(i);
      }
    }
    return possibleIndices;
  }

  /** All the variables declared in this sequences whose type matches the
   * given class. Returns an empty list if there are no matches.
   */
  public List<Variable> getVariablesOfType(Class<?> clazz, Reflection.Match match) {
    if (clazz == null || match == null)
      throw new IllegalArgumentException("parameters cannot be null.");
    List<Variable> possibleIndices = new ArrayList<Variable>(this.lastStatementVariables.size());
    for (int i = 0 ; i < this.size() ; i++) {
      Statement s = statements.get(i);
      if (!s.isVoidMethodCall()
          && varTypeMatches(s.statement.getOutputType(), clazz, match)) {
        possibleIndices.add(getVariable(i));
      }
    }
    return possibleIndices;
  }

  private boolean varTypeMatches(Class<?> t, Class<?> clazz, Match match) {
    switch (match) {
    case COMPATIBLE_TYPE:
      return Reflection.canBeUsedAs(t, clazz);
    case EXACT_TYPE:
      return t.equals(clazz);
    default:
      return false;
    }
  }

  protected void checkIndex(int i) {
    if (i < 0 || i > size() - 1)
      throw new IllegalArgumentException();
  }

  /**
   * Returns a new sequence that is equivalent to this sequence plus the given
   * statement appended at the end.
   */
  public final Sequence extend(StatementKind statement, List<Variable> inputVariables) {
    checkInputs(statement, inputVariables);
    List<RelativeNegativeIndex> indexList = new ArrayList<RelativeNegativeIndex>(1);
    for (Variable v : inputVariables) {
      indexList.add(getRelativeIndexForVariable(size(), v));
    }
    Statement newStatement = new Statement(statement, indexList);
    int newNetSize = (statement instanceof PrimitiveOrStringOrNullDecl) ? this.savedNetSize : this.savedNetSize + 1;
    return new Sequence(new OneMoreElementList<Statement>(this.statements, newStatement), this.savedHashCode
        + newStatement.hashCode(), newNetSize);
  }

  /**
   * Returns a new sequence that is equivalent to this sequence plus the given
   * statement appended at the end.
   */
  public final Sequence extend(StatementKind statement, Variable... inputs) {
    return extend(statement, Arrays.asList(inputs));
  }

  // Argument checker for extend method.
  // These checks should be caught by checkRep() too.
  private void checkInputs(StatementKind statement, List<Variable> inputVariables) {
    if (statement.getInputTypes().size() != inputVariables.size()) {
      String msg = "statement.getInputTypes().size():"
        + statement.getInputTypes().size()
        + " inputVariables.size():"
        + inputVariables.size()
        +" statement:"
        + statement;
      throw new IllegalArgumentException(msg);
    }
    for (int i = 0; i < inputVariables.size(); i++) {
      if (inputVariables.get(i).sequence != this) {
        String msg = "inputVariables.get("
          + i
          + ").owner != this for"
          + Globals.lineSep
          + "sequence: "
          + toString()
          + Globals.lineSep
          + "statement:"
          + statement
          + Globals.lineSep
          + "inputVariables:"
          + inputVariables;
        throw new IllegalArgumentException(msg);
      }
      Class<?> newRefConstraint =
        statements.get(inputVariables.get(i).index).statement.getOutputType();
      if (newRefConstraint == null) {
        String msg = "newRefConstraint == null for"
          + Globals.lineSep
          + "sequence: "
          + toString()
          + Globals.lineSep
          + "statement:"
          + statement
          + Globals.lineSep
          + "inputVariables:"
          + inputVariables;
        throw new IllegalArgumentException(msg);
      }
      if (!Reflection.canBeUsedAs(newRefConstraint, statement.getInputTypes().get(i))) {
        String msg = i
          + "th input constraint "
          + newRefConstraint
          + " does not imply "
          + "statement's "
          + i
          + "th input constraint "
          + statement.getInputTypes().get(i)
          + Globals.lineSep
          + ".Sequence:"
          + Globals.lineSep
          + ""
          + this.toString()
          + Globals.lineSep
          + "statement:"
          + statement
          + Globals.lineSep
          + "inputVariables:"
          + inputVariables;
        throw new IllegalArgumentException(msg);
      }
    }
  }

  /** Create a new sequence that is the concatenation of the given sequences. */
  

  
  public static Sequence concatenate(List<Sequence> sequences) {
    List<SimpleList<Statement>> statements1 = new ArrayList<SimpleList<Statement>>();
    int newHashCode = 0;
    int newNetSize = 0;
    for (Sequence c : sequences) {
      newHashCode += c.savedHashCode;
      newNetSize += c.savedNetSize;
      statements1.add(c.statements);
    }
    return new Sequence(new ListOfLists<Statement>(statements1), newHashCode, newNetSize);
  }

  // TODO inline and remove; used only in one place and confusing.
  public Variable getFirstVariableFromLastStatementVariables() {
    if (lastStatementVariables.size() == 0)
      throw new IllegalStateException();
    return lastStatementVariables.get(0);
  }

  /** The inputs for the ith statement, as indices. An index equal to x
   * means that the input is the value created by the x-th statement in
   * the sequence.
   */
  public List<Integer> getInputsAsAbsoluteIndices(int i) {
    List<Integer> inputsAsVariables = new ArrayList<Integer>();
    for (RelativeNegativeIndex relIndex : this.statements.get(i).inputs)
      inputsAsVariables.add(getVariableForInput(i, relIndex).index);
    return inputsAsVariables;
  }

  public void printStatement(StringBuilder b, int index) {
    // Get strings representing the inputs to this statement.
    // Example: { "var2", "(int)3" }
    getStatementKind(index).appendCode(getVariable(index), getInputs(index), b);
  }

  public Sequence repeatLast(int times) {
    Sequence retval = new Sequence(this.statements);
    StatementKind statementToRepeat = retval.getLastStatement();
    for (int i = 0 ; i < times ; i++) {
      List<Integer> vil = new ArrayList<Integer>();
      for (Variable v : retval.getInputs(retval.size()-1)) {
        if (v.getType().equals(int.class)) {
          int randint = Randomness.nextRandomInt(100);
          retval = retval.extend(new PrimitiveOrStringOrNullDecl(int.class, randint));
          vil.add(retval.size() - 1);
        } else {
          vil.add(v.getDeclIndex());
        }
      }
      List<Variable> vl = new ArrayList<Variable>();
      for (Integer vi : vil) {
        vl.add(retval.getVariable(vi));
      }
      retval = retval.extend(statementToRepeat,  vl);
    }
    return retval;
  }

  public MSequence toModifiableSequence() {
    MSequence slowSeq = new MSequence();
    List<MVariable> values = new ArrayList<MVariable>();
    for (int i = 0 ; i < size() ; i++) {
      values.add(new MVariable(slowSeq, getVariable(i).getName()));
    }
    List<MStatement> statements = new ArrayList<MStatement>();
    for (int i = 0 ; i < size() ; i++) {
      Statement sti = this.statements.get(i);
      StatementKind st = sti.statement;
      List<MVariable> inputs = new ArrayList<MVariable>();
      for (Variable v : getInputs(i)) {
        inputs.add(values.get(v.index));
      }
      MStatement slowSti =
        new MStatement(st, inputs, values.get(i));
      statements.add(slowSti);
    }
    slowSeq.statements = statements;
    return slowSeq;
  }

  public boolean isOwnerOf(Variable v) {
    return this == v.sequence;
  }

  /**
   * Returns a string representing this sequence. The string can be parsed back
   * into a sequence using the method Sequence.parse(String). In particular, the
   * following invariant holds:
   *  <pre>st.equals(parse(st.toParseableCode()))</pre>
   *  See the {@link randoop.Sequence#parse(List) parse} for the 
   *  required format of a String representing a Sequence.
   */
  public String toParseableString() {
    return toParseableString(Globals.lineSep);
  }
  
  /**
   * Like toParseableString, but the client can specify a string that
   * will be used a separator between statements.
   */
  public String toParseableString(String statementSep) {
    assert statementSep != null;
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < size(); i++) {
      b.append("var" + i);
      b.append(" =  ");
      b.append(StatementKinds.getId(getStatementKind(i)));
      b.append(" : ");
      b.append(getStatementKind(i).toParseableString());
      b.append(" : ");
      for (Variable v : getInputs(i)) {
        b.append(v.toString());
        b.append(" ");
      }
      b.append(statementSep);
    }
    return b.toString();  
  }

  /**
   * Parse a sequence encoded as a list of strings, each string corresponding to
   * one statement. This method is similar to parse(String), but expects the
   * individual statements already as separate strings. Each statement is
   * expected to be of the form:
   *
   * VAR = STATEMENT_KIND : VAR ... VAR
   *
   * Where the VAR are strings representing a variable name, and STATEMENT_KIND
   * is a string representing a StatementKind. For more on STATEMENT_KIND, see
   * the documentation for StatementKinds.parse(String).
   *
   * The first VAR token represents the "output variable" that is the result of
   * the statement call. The VAR tokens appearing after STATEMENT_KIND represent
   * the "input variables" to the statement call. At the i-th line, the input
   * variable tokens should appear as an output variable for some previous j-th
   * line, j < i.
   *
   * Here is an example of a list of lines representing a sequence.
   *
   * var0 = cons : java.util.HashMap.<init>() :
   * var1 = prim : double:-1.0 :
   * var2 = prim : java.lang.String:"hi!" :
   * var3 = method : java.util.HashMap.put(java.lang.Object,java.lang.Object) : var0 var1 var2
   *
   * The above sequence corresponds to the following java code (with package
   * names omitted for brevity):
   *
   * HashMap var0 = new HashMap(); double var1 = -1.0; String var2 = "hi!";
   * Object var3 = var0.put(var1, var2);
   *
   * When writing/reading sequences out to file: you have two options: serialize
   * the sequences using java's serialization mechanism, or write them out as
   * parseable text. Serialization is faster, and text is human-readable.
   */
  public static Sequence parse(List<String> statements) throws SequenceParseException {

    Map<String, Integer> valueMap = new LinkedHashMap<String, Integer>();
    Sequence sequence = new Sequence();
    int statementCount = 0;
    try {
      for (String statement : statements) {
        

        // Remove surrounding whitespace.
        statement = statement.trim();

        // Extract elements:
        //   var    =    <statementkind>    :    var ... var
        //    |                |                     |
        //    |                |                     |
        // newVar           stKind                inVars
        int equalsInd = statement.indexOf('=');
        int colonInd = statement.lastIndexOf(':');
        
        if (equalsInd == -1) {
            String msg = "A statement must be of the form "
                + "varname = <statementkind> : varname ... varname"
                + " but the " + statementCount + "-th (1-based) is missing"
                + " an \"=\" symbol.";
            throw new SequenceParseException(msg, statements, statementCount);
        }

        if (colonInd == -1) {
            String msg = "A statement must be of the form "
                + "varname = <statementkind> : varname ... varname"
                + " but the " + statementCount + "-th (1-based) is missing"
                + " a \":\" symbol.";
            throw new SequenceParseException(msg, statements, statementCount);
        }
        
        String newVar = statement.substring(0, equalsInd).trim();
        String stKind = statement.substring(equalsInd + 1, colonInd).trim();
        String inVarsStr = statement.substring(colonInd + 1).trim();

        if (valueMap.containsKey(newVar)) {
          String msg = "(Statement "
            + statementCount + ") result variable name " + newVar +
            " was already declared in a previous statement.";
          throw new SequenceParseException(msg, statements, statementCount);
        }

        // Parse statement kind.
        StatementKind st;
        try {
          st = StatementKinds.parse(stKind);
        } catch (StatementKindParseException e) {
          throw new SequenceParseException(e.getMessage(), statements, statementCount);
        }

        // Find input variables from their names.
        String[] inVars = new String[0];
        if (inVarsStr.trim().length() > 0) {
          // One or more input vars.
          inVars = inVarsStr.split("\\s");
        }

        if (inVars.length != st.getInputTypes().size()) {
          String msg = "Number of input variables given (" + inVarsStr + ") does not match expected (expected " + st.getInputTypes().size() + ")";
          throw new SequenceParseException(msg, statements, statementCount);
        }

        List<Variable> inputs = new ArrayList<Variable>();
        for (String inVar : inVars) {
          Integer index = valueMap.get(inVar);
          if (index == null) {
            String msg = "(Statement "
              + statementCount + ") input variable name " + newVar +
              " is not declared by a previous statement.";
            throw new IllegalArgumentException(msg);
          }
          inputs.add(sequence.getVariable(index));
        }

        sequence = sequence.extend(st, inputs);
        valueMap.put(newVar, sequence.getLastVariable().getDeclIndex());
        statementCount++;
      }
    } catch (RuntimeException e) {
      // Saw some other exception that is not a parse error.
      // Throw an error, giving information on the problem.
      StringBuilder b = new StringBuilder();
      b.append("Error while parsing the following list of strings as a sequence (error was at index " + statementCount + "):\n\n");
      for (String s : statements) {
        b.append(s + "\n");
      }
      b.append("\n\n");
      b.append("Error: " + e.toString() + "\n");
      b.append("Stack trace:\n");
      for (StackTraceElement s : e.getStackTrace()) {
        b.append(s.toString());
      }
      throw new Error(e);
    }
    return sequence;
  }

  /**
   * Parse a sequence encoded as a strings. Convience method for parse(List<String>),
   * which parses a sequence of strings, each representing a Statement. See
   * that method for more documentation on the string representation of a sequence.
   *
   * This method breaks up the given string into statements assuming that each
   * statement is separated by a line separator character.
   *
   * The following invariant holds:
   *
   *     st.equals(parse(st.toParseableCode()))
   *
   * When writing/reading sequences out to file: you have two options: serialize
   * the sequences using java's serialization mechanism, or write them out as
   * parseable text. Serialization is faster, and text is human-readable.
   * @throws SequenceParseException 
   */
  public static Sequence parse(String string) throws SequenceParseException {
    return parse(Arrays.asList(string.split(Globals.lineSep)));
  }

  /**
   * Reads a file containing a collection of sequences in textual
   * representation.
   *
   * The file should be made up of a list of records, each as follows:
   *
   * START SEQUENCE
   * <parseable-sequence-string>
   * END SEQUENCE
   */
  public static void readTextSequences(String file, final Collection<Sequence> collection) {
    // Parse the file using a RecordListReader.
    RecordProcessor processor = new RecordProcessor() {
      public void processRecord(List<String> record) {
        try {
          collection.add(Sequence.parse(record));
        } catch (SequenceParseException e) {
          throw new Error(e);
        }
      }
    };
    RecordListReader reader = new RecordListReader("SEQUENCE", processor);
    reader.parse(file);
  }
  

  public int lastUseBefore(int idx, Variable var) {
    if (var.sequence != this)
      throw new IllegalArgumentException("variable does not belong to sequence.");
    for (int i = idx - 1 ; i >= 0 ; i--) {
      if (getVariable(i).equals(var)) {
        return i;
      }
      for (Variable input : getInputs(i)) {
        if (input.equals(var)) {
          return i;
        }
      }
    }
    throw new IllegalStateException("Bug in Randoop.");
  }

  /**
   * A sequence representing a single primitive values, like
   * "Foo var0 = null" or "int var0 = 1".
   */
  public boolean isPrimitive() {
    if (size() != 1) {
      return false;
    }
    if (!(getStatementKind(0) instanceof PrimitiveOrStringOrNullDecl)) {
      return false;
    }
    return true;
  }
  
}
