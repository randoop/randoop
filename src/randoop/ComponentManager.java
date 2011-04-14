package randoop;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import randoop.util.ListOfLists;
import randoop.util.PrimitiveTypes;
import randoop.util.SimpleList;

/**
 * Stores and provides means to access the component sequences generated during
 * a run of Randoop. "Component sequences" are sequences that Randoop uses to
 * create larger sequences.
 * 
 * This class manages different collections of component sequences:
 * 
 * <ul>
 * <li>General components that can be used as input to any method in any class.
 * <li>Class literals: components representing literal values that apply only to
 * a specific class and should not be used as inputs to other classes.
 * <li>Package literals: analogous to class literals but at the package level.
 * </ul>
 * 
 * SEED SEQUENCES. Seed sequences are sequences that were not created during the
 * generation process but obtained via other means. They include (1) sequences
 * passed via the constructor, (2) class literals, and (3) package literals. The
 * only different treatment of seed sequences is during calls to the
 * clearGeneratedSequences() method, which removes only general, non-seed
 * components from the collection.
 */
public class ComponentManager {

  /**
   * The principal set of sequences used to create other, larger sequences by
   * the generator.
   */
  // Is never null. Contains both general components
  // and seed sequences.
  private SequenceCollection gralComponents;

  /**
   * The subset of the sequences that were given pre-generation
   * to the component manager (via its constructor).
   */
  // Seeds are all contained in gralComponents. This list
  // is kept to restore seeds if the user calls
  // clearGeneratedSequences().
  private final Collection<Sequence> gralSeeds;
  
  /**
   * A set of additional components representing literals that
   * should only be used as input to specific classes.
   */
  // May be null, which represents no class literals present.
  private ClassLiterals classLiterals = null;
  
  /**
   * A set of additional components representing literals that
   * should only be used as input to specific packages.
   */
  // May be null, which represents no package literals present.
  private PackageLiterals packageLiterals = null;


  /**
   * Create an empty component manager, with an empty seed sequence set.
   */
  public ComponentManager() {
    gralComponents = new SequenceCollection();
    gralSeeds = Collections.unmodifiableSet(Collections.<Sequence>emptySet());
  }
  
  /**
   * Create a component manager, initially populated with the
   * given sequences, which are considered seed sequences.
   * 
   * @param generalSeeds seed sequences. Can be null, in which case
   * the seed sequences set is considered empty.
   */
  public ComponentManager(Collection<Sequence> generalSeeds) {
    Set<Sequence> seedSet = new LinkedHashSet<Sequence>(generalSeeds.size());
    if (generalSeeds != null) {
      seedSet.addAll(generalSeeds);
    }
    this.gralSeeds = Collections.unmodifiableSet(seedSet);
    gralComponents = new SequenceCollection(seedSet);
  }

  /**
   * Returns the number of (non-seed) sequences stored by the manager.
   */
  // FIXME subtract size of seeds!
  public int numGeneratedSequences() {
    return gralComponents.size();
  }

  /**
   * Add a sequence representing a literal value that can be used
   * when testing members of the given class.
   */
  public void addClassLevelLiteral(Class<?> cls, Sequence seq) {
    if (classLiterals == null) {
      classLiterals = new ClassLiterals();
    }
    classLiterals.addSequence(cls, seq);
  }

  /**
   * Add a sequence representing a literal value that can be used
   * when testing classes in the given package.
   */
  public void addPackageLevelLiteral(Package pkg, Sequence seq) {
    if (packageLiterals == null) {
      packageLiterals = new PackageLiterals();
    }
    packageLiterals.addSequence(pkg, seq);
  }
  
  /**
   * Add a component sequence.
   */
  public void addGeneratedSequence(Sequence sequence) {
    gralComponents.add(sequence);
  }

  /**
   * Removes any components sequences added so far, except for seed sequences,
   * which are preserved.
   */
  public void clearGeneratedSequences() {
    gralComponents.clear();
    gralComponents.addAll(this.gralSeeds);
  }

  public Set<Sequence> getAllGeneratedSequences() {
    return gralComponents.getAllSequences();
  }

  /**
   * Returns all the general component sequences that create values of the given
   * class. If exactMatch==true returns only sequences that declare values of
   * the exact class specified; if exactMatch==false returns sequences declaring
   * values of cls or any other class that can be used as a cls (i.e. a subclass
   * of cls).
   */
  public SimpleList<Sequence> getSequencesForType(Class<?> cls, boolean exactMatch) {
    return gralComponents.getSequencesForType(cls, exactMatch);
  }

  /**
   * Returns component sequences that create values of the type required by the
   * i-th input value of the given statement. Any applicable class- or
   * package-level literals, those are added to the collection as well.
   */
  @SuppressWarnings("unchecked")
  public SimpleList<Sequence> getSequencesForType(StatementKind statement, int i) {

    Class<?> neededType = statement.getInputTypes().get(i);

    SimpleList<Sequence> ret = gralComponents.getSequencesForType(neededType, false);

    if (classLiterals != null || packageLiterals != null) {

      Class<?> declaringCls = null;
      if (statement instanceof RMethod) {
        declaringCls = ((RMethod) statement).getMethod().getDeclaringClass();
      } else if (statement instanceof RConstructor) {
        declaringCls = ((RConstructor) statement).getConstructor().getDeclaringClass();
      }

      if (classLiterals != null) {
        if (declaringCls != null) {
          SimpleList<Sequence> sl = classLiterals.getSequences(declaringCls, neededType);
          if (!sl.isEmpty()) {
            ret = new ListOfLists<Sequence>(ret, sl);
          }
        }
      }

      if (packageLiterals != null) {
        Package pkg = declaringCls.getPackage();
        if (pkg != null) {
          SimpleList<Sequence> sl = packageLiterals.getSequences(pkg, neededType);
          if (!sl.isEmpty()) {
            ret = new ListOfLists<Sequence>(ret, sl);
          }
        }
      }
    }

    return ret;
  }

  /**
   * Returns all sequences that represent primitive values
   * (e.g. sequences like "Foo var0 = null" or "int var0 = 1"),
   * including general components, class literals and package
   * literals.
   */
  public Set<Sequence> getAllPrimitiveSequences() {
    
    Set<Sequence> ret = new LinkedHashSet<Sequence>();
    if (classLiterals != null) {
      for (SequenceCollection c : classLiterals.map.values()) {
        ret.addAll(c.getAllSequences());
      }
    }
    if (packageLiterals != null) {
      for (SequenceCollection c : packageLiterals.map.values()) {
        ret.addAll(c.getAllSequences());
      }
    }
    for (Class<?> c : PrimitiveTypes.getPrimitiveTypesAndString()) {
      ret.addAll(gralComponents.getSequencesForType(c, true).toJDKList());
    }
    return ret;
  }
}
