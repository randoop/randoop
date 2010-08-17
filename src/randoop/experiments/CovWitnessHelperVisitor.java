package randoop.experiments;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plume.Triple;
import randoop.ExecutableSequence;
import randoop.ExecutionVisitor;
import randoop.Sequence;

import cov.Branch;
import cov.Coverage;
import cov.CoverageAtom;

/**
 * This class is only used by the branch-directed generation research project.
 *
 * Records the branches covered after executing all-but-last method
 * call in a sequence.
 * 
 * ASSUMPTIONS:
 * 
 * 1. Coverage has been cleared before executing the sequence.
 * 
 * 2. The statements in a sequence are all executed and visited
 * from beginning to end. This visitor will not work properly with the
 * naive generator that backtracks during execution.
 * 
 */
public class CovWitnessHelperVisitor implements ExecutionVisitor {
  
  private final List<Class<?>> covClasses;

  private final Map<CoverageAtom, Set<Sequence>> covWitnessMap;

  private Map<Class<?>, int[]> trues;
  
  private Map<Class<?>, int[]> falses;


  /**
   * @param tracker the classes that are coverage-instrumented.
   */
  public CovWitnessHelperVisitor(CodeCoverageTracker tracker) {
    if (tracker == null) {
      throw new IllegalArgumentException("tracker is null");
    }
    this.covClasses = tracker.covClasses;
    this.covWitnessMap = tracker.branchesToCoveringSeqs;
    this.trues = null;
    this.falses = null;
  }

  @Override
  public void initialize(ExecutableSequence executableSequence) {
    // Nothing to do for initialization.
  }

  public void visitAfter(ExecutableSequence sequence, int idx) {
    
    assert sequence.sequence.size() > 0;
    
    if (sequence.sequence.size() == 1) {
      addWitnessesLength1Seq(sequence.sequence);
      return;
    }
    
    assert sequence.sequence.size() > 1;
    
    // We've executed all but last statements.
    if (idx == sequence.sequence.size() - 2) {
      
      trues = new LinkedHashMap<Class<?>, int[]>();
      falses = new LinkedHashMap<Class<?>, int[]>();
      
      // Store covered branches.
      for (Class<?> cls : covClasses) {
        
        Coverage.initCoverage(cls);
        
        int[] trueBranches = Coverage.getTrueBranches(cls);
        int[] savedtrues = new int[trueBranches.length];
        System.arraycopy(trueBranches, 0, savedtrues, 0, trueBranches.length);
        trues.put(cls, savedtrues);

        int[] falseBranches = Coverage.getFalseBranches(cls);
        int[]  savedfalses = new int[falseBranches.length];
        System.arraycopy(falseBranches, 0, savedfalses, 0, falseBranches.length);
        falses.put(cls, savedfalses);
      }
      return;
    }
    
    if (idx == sequence.sequence.size() - 1) {
      
      Set<Branch> newcov = new LinkedHashSet<Branch>();
      
      // Store covered branches.
      for (Class<?> cls : covClasses) {
        String clsname = cls.getName();
        Coverage.initCoverage(cls);
        
        int[] oldtrues = trues.get(cls);
        assert oldtrues != null;
        
        int[] oldfalses = falses.get(cls);
        assert oldfalses != null;
        
        int[] newtrues = Coverage.getTrueBranches(cls);
        assert oldtrues.length == newtrues.length;
        
        int[] newfalses = Coverage.getFalseBranches(cls);
        assert oldfalses.length == newfalses.length;
        
        assert oldtrues.length == oldfalses.length;
        assert oldfalses.length == newfalses.length;  

        for (int i = 0 ; i < newtrues.length ; i++) {
          if (newtrues[i] > oldtrues[i]) {
            newcov.add((Branch) Coverage.atomInfoToAtoms.get(new Triple<String, Integer, Boolean>(clsname, i, true)));
          }
          if (newfalses[i] > oldfalses[i]) {
            newcov.add((Branch) Coverage.atomInfoToAtoms.get(new Triple<String, Integer, Boolean>(clsname, i, false)));
          }
        }
      }
      
      // For each branch covered in the last statement, add the sequence to the
      // coverage witness map.
      for (Branch br :newcov) {
        Set<Sequence> seqs = covWitnessMap.get(br);
        if (seqs == null) {
          seqs = new LinkedHashSet<Sequence>();
          covWitnessMap.put(br, seqs);
        }
        seqs.add(sequence.sequence);
      }
      
    }
  }

  private void addWitnessesLength1Seq(Sequence sequence) {
    for (CoverageAtom ca : Coverage.getCoveredAtoms(covClasses)) {
      Set<Sequence> seqs = covWitnessMap.get(ca);
      if (seqs == null) {
        seqs = new LinkedHashSet<Sequence>();
        covWitnessMap.put(ca, seqs);
      }
      seqs.add(sequence);
    }
  }

  public void visitBefore(ExecutableSequence sequence, int i) {
    return;
  }
}
