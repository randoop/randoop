package randoop;

import java.util.Set;

import randoop.util.ListOfLists;
import randoop.util.SimpleList;

public class ComponentManager {

  /**
   *  The set of sequences used to create other, larger sequences
   *  by the generator. 
   */
  public SequenceCollection components = null;

  /**
   * A set of additional sequences representing literals extracted
   * from the class files under test.
   */
  private ClassLiterals classLiterals = null;
  
  /**
   * A set of additional sequences representing literals extracted
   * from the class files under test.
   */
  private PackageLiterals packageLiterals = null;

  public ComponentManager() {
    components = new SequenceCollection();
  }
  
  public ComponentManager(SequenceCollection seeds) {
    components = new SequenceCollection(seeds);
  }

  // NON-SEED CLEAR
  public void clear() {
    components.clear();
  }

  // NON-SEED SIZE
  public int size() {
    return components.size();
  }

  public void add(Sequence sequence) {
    components.add(sequence);
  }

  public Set<Sequence> getAllSequences() {
    return components.getAllSequences();
  }

  public SimpleList<Sequence> getSequencesForType(Class<?> cls, boolean b) {
    return components.getSequencesForType(cls, b);
  }

  @SuppressWarnings("unchecked")
  public SimpleList<Sequence> getSequencesForType(StatementKind statement, int i) {
    
    Class<?> neededType = statement.getInputTypes().get(i);
    
    SimpleList<Sequence> ret = components.getSequencesForType(neededType, false);
    
    if (classLiterals != null || packageLiterals != null) {
      
      
      Class<?> declaringCls = null;
      if (statement instanceof RMethod) {
        declaringCls = ((RMethod)statement).getMethod().getDeclaringClass();
      } else if (statement instanceof RConstructor) {
        declaringCls = ((RConstructor)statement).getConstructor().getDeclaringClass();
      }
    
      if (classLiterals != null) {
        if (declaringCls != null) { 
          SimpleList<Sequence> sl = classLiterals.getLiterals(neededType, declaringCls);
          System.out.println(">>>" + sl.size() + "," + neededType + "," + declaringCls);
          if (!sl.isEmpty()) {
            ret = new ListOfLists<Sequence>(ret, sl);
          }
        }
      }
    
      if (packageLiterals != null) {
        Package pkg = declaringCls.getPackage(); // XXX can it be null?
        SimpleList<Sequence> sl = packageLiterals.getLiterals(neededType, pkg);
        if (!sl.isEmpty()) {
          ret = new ListOfLists<Sequence>(ret, sl);
        }
      }
    }
    return ret;
  }
  

  public void addClassLevelLiteral(Class<?> cls, Sequence seq) {
    System.out.println("@@@");
    if (classLiterals == null) {
      classLiterals = new ClassLiterals();
    }
    classLiterals.addSequence(seq, cls);
  }

  public void addPackageLevelLiteral(Package pkg, Sequence seq) {
    if (packageLiterals == null) {
      packageLiterals = new PackageLiterals();
    }
    packageLiterals.addSequence(seq, pkg);
  }
}
