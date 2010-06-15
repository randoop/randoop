package randoop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  private Collection<Sequence> seeds;

  private void checkRep() {
    if (Globals.nochecks)
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
   * Restores the collection to its initial state, meaning: the collection
   * contains only the seeds given as input argument to the constructor.
   * In other words, this method removes all the sequences
   * from the collection, and them adds back the seeds
   * given in the constructor call.
   */
  public void clear() {
    if (Log.isLoggingOn()) Log.logLine("Clearing activesequences.");
    init();
  }

  /**
   * Create a new, empty collection with no seeds. 
   */
  public SequenceCollection() {
    this(new ArrayList<Sequence>());
  }

  /**
   * Creates a new one that is a copy of the old one.
   */
  public SequenceCollection(SequenceCollection c) {
    this(new ArrayList<Sequence>(c.size()));
    addAll(c);
  }
  /** Create a new collection that uses the given seeds.
   * Note: The clear() method removes all the sequences
   * from the collection, and them adds back the seeds
   * given in the constructor call.
   */
  public SequenceCollection(Collection<Sequence> seeds) {
    if (seeds == null)
      throw new IllegalArgumentException("seeds cannot be null.");
    this.seeds = Collections.unmodifiableCollection(seeds);
    init();
  }

  private void init() {
    this.activeSequences = new LinkedHashMap<Class<?>, ArrayListSimpleList<Sequence >>();
    this.typesWithSequencesMap = new SubTypeSet(false);
    numActivesequences = 0;
    addAll(this.seeds);
    checkRep();
  }

  public void addAll(Collection<Sequence> col) {
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
