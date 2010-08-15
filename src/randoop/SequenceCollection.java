package randoop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.main.GenInputsAbstract;
import randoop.util.ArrayListSimpleList;
import randoop.util.ListOfLists;
import randoop.util.Log;
import randoop.util.Reflection;
import randoop.util.SimpleList;
import randoop.util.Reflection.Match;

/**
 * A collection of sequences that makes its efficient to ask for
 * all the sequences that create a value of a given type.
 *
 * <p>
 * RANDOOP IMPLEMENTATION NOTE.
 * <p>
 * 
 * When creating new sequences, Randoop often needs to search for all
 * the previously-generated sequences that create one or more values
 * of a given type. Since this set can contain thousands of sequences,
 * finding these sequences can can be time-consuming and a bottleneck
 * in generation (as we discovered during profiling).
 * 
 * <p>
 *
 * This class makes the above search faster by maintanining two data structures:
 *
 * <ul>
 * <li> A map from types to the sets of all sequences that create one
 *      or more values of exactly the given type.
 *
 * <li> A set of all the types that can be created with the existing
 *      set of sequences.  The set is maintained as a {@link
 *      SubTypeSet} that allows for quick queries about can-be-used-as
 *      relationships among the types in the set.
 * </ul>
 *
 * To find all the sequences that create values of a given type,
 * Randoop first uses the <code>SubTypeSet</code> to find the set
 * <code>S</code> of feasible subtypes in set of sequences, and
 * returns the range of <code>S</cod> in the sequence map.
 */
public class SequenceCollection {

  // We make it a list to make it easier to pick out an element at random.
  private Map<Class<?>, ArrayListSimpleList<Sequence>> activeSequences = new LinkedHashMap<Class<?>, ArrayListSimpleList<Sequence>>();

  private SubTypeSet typesWithSequencesMap = new SubTypeSet(false);

  public int numActivesequences = 0;

  private void checkRep() {
    if (!GenInputsAbstract.debug_checks)
      return;
    if (activeSequences.size() != typesWithSequencesMap.size()) {
      StringBuilder b = new StringBuilder();
      b.append("activesequences types=" + Globals.lineSep + activeSequences.keySet());
      b.append(", typesWithsequencesMap types=" + Globals.lineSep);
      b.append(typesWithSequencesMap.typesWithsequences);
      throw new IllegalStateException(b.toString());
    }
  }

  public int numTypes() {
    return typesWithSequencesMap.size();
  }

  public int size() {
    return numActivesequences;
  }

  /**
   * Removes all sequences from this collection.
   */
  public void clear() {
    if (Log.isLoggingOn()) Log.logLine("Clearing sequence collection.");
    this.activeSequences = new LinkedHashMap<Class<?>, ArrayListSimpleList<Sequence >>();
    this.typesWithSequencesMap = new SubTypeSet(false);
    numActivesequences = 0;
    checkRep();
  }

  /**
   * Create a new, empty collection. 
   */
  public SequenceCollection() {
    this(new ArrayList<Sequence>());
  }
 
  /**
   * Create a new collection and adds the given initial sequences.
   */
  public SequenceCollection(Collection<Sequence> initialSequences) {
    if (initialSequences == null)
      throw new IllegalArgumentException("initialSequences is null.");
    this.activeSequences = new LinkedHashMap<Class<?>, ArrayListSimpleList<Sequence >>();
    this.typesWithSequencesMap = new SubTypeSet(false);
    numActivesequences = 0;
    addAll(initialSequences);
    checkRep();
  }

  public void addAll(Collection<Sequence> col) {
    if (col == null) {
      throw new IllegalArgumentException("col is null");
    }
    for (Sequence  c : col) {
      add(c);
    }
  }

  public void addAll(SequenceCollection components) {
    for(ArrayListSimpleList<Sequence> s:components.activeSequences.values()) {
      for(Sequence seq:s.theList) {
        add(seq);
      }
    }       
  }

  /**
   * Add a sequence to this collection. This method takes into account the
   * active indices in the sequence. If sequence[i] creates a values of type T,
   * and sequence[i].isActive==true, then the sequence is seen as creating a
   * useful value at index i. More precisely, the method/constructor at that
   * index is said to produce a useful value (and if the user later queries for
   * all sequences that create a T, the sequence will be in the collection
   * returned by the query). How a value is deemed useful or not is left up to
   * the client.
   */
  public void add(Sequence sequence) {
    List<Class<?>> classes = new ArrayList<Class<?>>();
    List<Class<?>> constraints = sequence.getLastStatementTypes();
    List<Variable> values = sequence.getLastStatementVariables();
    assert constraints.size() == values.size();
    for (int i = 0 ; i < constraints.size() ; i++) {
      Variable v = values.get(i);
      assert Reflection.canBeUsedAs(v.getType(), constraints.get(i));
      if (sequence.isActive(v.getDeclIndex()))
        classes.add(constraints.get(i));
    }
    updateCompatibleClassMap(classes);
    updateCompatibleMap(sequence, classes);
    checkRep();
  }

  private void updateCompatibleClassMap(List<Class<?>> classes) {
    for (Class<?> c : classes) {
      typesWithSequencesMap.add(c);
    }
  }

  private void updateCompatibleMap(Sequence newsequence, List<Class<?>> classes) {
    for (int i = 0; i < classes.size(); i++) {
      Class<?> t = classes.get(i);
      ArrayListSimpleList<Sequence > set = this.activeSequences.get(t);
      if (set == null) {
        set = new ArrayListSimpleList<Sequence >();
        this.activeSequences.put(t, set);
      }
      if (Log.isLoggingOn())
        Log.logLine("Adding sequence to active sequences of type " + t);
      boolean added = set.add(newsequence); numActivesequences++;
      assert added == true;
    }
  }


  /**
   * Searches through the set of active sequences to find all sequences whose types
   * match with the parameter type.
   *
   * @param clazz -
   *            the type desired for the sequences being sought
   * @return list of sequence objects that are of typp 'type' and abide by the
   *         constraints defined by nullOk.
   */
  public SimpleList<Sequence> getSequencesForType(Class<?> clazz, boolean exactMatch) {

    if (clazz == null)
      throw new IllegalArgumentException("clazz cannot be null.");

    if (Log.isLoggingOn()) {
      Log.logLine("getActivesequencesThatYield: entering method, clazz=" + clazz .toString());
      // Log.logLine(activesequences.toString());
    }

    List<SimpleList<Sequence>> ret = new ArrayList<SimpleList<Sequence>>();


    if (exactMatch) {
      SimpleList<Sequence > l = this.activeSequences.get(clazz);
      if (l != null) {
        ret.add(l);
      }
    } else {
      for (Class<?> compatibleClass : typesWithSequencesMap.getMatches(clazz)) {
        ret.add(this.activeSequences.get(compatibleClass));
      }
    }

    if (ret.isEmpty()) {
      if (Log.isLoggingOn())
        Log.logLine("getActivesequencesThatYield: found no sequences matching class " + clazz);
    }
    SimpleList<Sequence> selector = new ListOfLists<Sequence>(ret);
    if (Log.isLoggingOn())
      Log.logLine("getActivesequencesThatYield: returning " + selector.size() + " sequences.");
    return selector;
  }

  public Set<Class<?>> getTypesThatHaveSequences() {
    return Collections.unmodifiableSet(this.typesWithSequencesMap.getElements());
  }

  public boolean hasSequences(Class<?> targetClass, Match match) {
    return typesWithSequencesMap.containsAssignableType(targetClass, match);
  }

  public Set<Sequence> getAllSequences() {
    Set<Sequence> result = new LinkedHashSet<Sequence>();
    for(ArrayListSimpleList<Sequence> a: activeSequences.values()) {
      result.addAll(a.theList);
    }
    return result;

  }

  public Set<StatementKind> getAllStatements() {
    Set<StatementKind> result = new LinkedHashSet<StatementKind>();
    for(Sequence s: getAllSequences()) {
      for (Statement stmtWithInputs : s.getStatementsWithInputs().toJDKList()) {
        result.add(stmtWithInputs.statement);
      }
    }
    return result;

  }
}
