package randoop.experiments;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plume.Option;
import plume.Unpublicized;
import randoop.ExecutableSequence;
import randoop.IEventListener;
import randoop.RConstructor;
import randoop.RMethod;
import randoop.Sequence;
import randoop.StatementKind;
import cov.Branch;
import cov.Coverage;
import cov.CoverageAtom;


public class CodeCoverageTracker implements IEventListener {

  @Unpublicized
  @Option("When branch coverage fails to increase for the given number of seconds (>0), stop generation; -1 to disable")
  public static int stop_when_plateau = -1;

  // TODO use?
  @Unpublicized
  @Option("Output coverage stats during generation")
  public static boolean stats_coverage = false;
  
  public final LinkedList<Class<?>> covClasses;
  public final Map<CoverageAtom, Set<Sequence>> branchesToCoveringSeqs;
  public final Set<Branch> branchesCovered;
  private int branchtot;
  private int branchcov;
  Map<StatementKind,Integer> membersToBranchTot;

  /**
   * Creates a coverage tracker that tracks the classes specified
   * in the given list. The list can be null, which is equivalent to
   * an empty list.
   */
  public CodeCoverageTracker(List<Class<?>> coverageInstrumentedClasses) {
    if (coverageInstrumentedClasses == null) {
      this.covClasses = new LinkedList<Class<?>>();
    } else {
      this.covClasses = new LinkedList<Class<?>>(coverageInstrumentedClasses);
    }
    branchesToCoveringSeqs = new LinkedHashMap<CoverageAtom, Set<Sequence>>();
    branchesCovered = new LinkedHashSet<Branch>();
    branchtot = 0;
    branchcov = 0;
    membersToBranchTot = new LinkedHashMap<StatementKind, Integer>();
    
    // Setup STAT_BRANCHTOT for the coverage classes.
    for (Class<?> cls : coverageInstrumentedClasses) {
      Set<CoverageAtom> atoms = Coverage.getBranches(cls);
      assert atoms != null : cls.toString();
      for (CoverageAtom ca : atoms) {
        Member member = Coverage.getMemberContaining(ca);
        if (member == null) {
          // Atom does not belong to method or constructor.
          // Add only to global stats.
          branchtot++;
          continue;
        }

        if (member instanceof Method) {
          // Atom belongs to a method.
          // Add to method stats (and implicitly, global stats).
          Method method = (Method)member;
          addToCount(RMethod.getRMethod(method), 1);
          continue;
        }

        // Atom belongs to a constructor.
        // Add to constructor stats (and implicitly, global stats).
        assert member instanceof Constructor<?> : member.toString();
        Constructor<?> cons = (Constructor<?>)member;
        addToCount(RConstructor.getRConstructor(cons), 1);
      }
    }

  }
  
  private void addToCount(StatementKind member, int i) {
    Integer count = membersToBranchTot.get(member);
    if (count == null) {
      count = 0;
    }
    membersToBranchTot.put(member, count + 1);
  }

  @Override
  public void generationStepPre() {
    Coverage.clearCoverage(covClasses);
  }

  @Override
  public void generationStepPost(ExecutableSequence es) {
    
    Set<Branch> cov = new LinkedHashSet<Branch>();
    for (CoverageAtom ca : Coverage.getCoveredAtoms(covClasses)) {
      cov.add((Branch)ca);
      Set<Sequence> seqs = branchesToCoveringSeqs.get(ca);
      if (seqs == null) {
        seqs = new LinkedHashSet<Sequence>();
        branchesToCoveringSeqs.put(ca, seqs);
      }
      if (es != null && seqs.isEmpty()) {
        seqs.add(es.sequence);
      }
    }
    
    if (es != null) {
      
      Set<Branch> coveredBranches = cov;
      
      // Update coverage information.
      for (Branch ca : coveredBranches) {

        // This branch was already counted.
        if (branchesCovered.contains(ca))
          continue;

        branchesCovered.add(ca);

        Member member = Coverage.getMemberContaining(ca);
        if (member == null) {
          // Atom does not belong to method or constructor.
          // Add only to global stats.
          branchcov++;
          continue;
        }

        if (member instanceof Method) {
          // Atom belongs to a method.
          // Add to method stats (and implicitly, global stats).
          Method method = (Method) member;
          addToCount(RMethod.getRMethod(method), 1);
          continue;
        }

        // Atom belongs to a constructor.
        // Add to constructor stats (and implicitly, global stats).
        assert member instanceof Constructor<?> : member.toString();
        Constructor<?> cons = (Constructor<?>) member;
        addToCount(RConstructor.getRConstructor(cons), 1);
      }

    }
  }
  
  public long lastCovIncrease = System.currentTimeMillis();
  private int lastNumBranches = 0;


  @Override
  public void progressThreadUpdate() {
    
    if (branchesCovered.size() > lastNumBranches) {
      lastCovIncrease = System.currentTimeMillis();
      lastNumBranches = branchesCovered.size();
    }
  }

  @Override
  public boolean stopGeneration() {
    
    if (stop_when_plateau == -1) {
      return false;
    }
    if (branchtot == 0) {
      return false;
    }
    long now = System.currentTimeMillis();
    return now - lastCovIncrease > stop_when_plateau * 1000;
  }

  @Override
  public void explorationEnd() {
    // Nothing do to here.
  }

  @Override
  public void explorationStart() {
    // Nothing do to here.
  }

}
