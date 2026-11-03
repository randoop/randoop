package randoop.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import randoop.Globals;
import randoop.main.RandoopBug;

/**
 * Branch coverage information for a class that has been instrumented for branch coverage. An
 * instrumented class stores an instance of this class in its static field {@code
 * randoopCoverageInfo}.
 */
public class TestCoverageInfo {

  /** For each branch condition index, the number of times the condition evaluated to true. */
  public final int[] branchTrue;

  /** For each branch condition index, the number of times the condition evaluated to false. */
  public final int[] branchFalse;

  /** Maps a method signature to the indices of the branch conditions in that method. */
  public final Map<String, Set<Integer>> methodToIndices;

  /**
   * The number of branches covered in a method, and the total number of branches in it. Each branch
   * condition has two branches: one for each outcome.
   */
  private static class BranchCov {
    /** The number of branches covered in the method. */
    /*package*/ final int covered;

    /** The total number of branches in the method. */
    /*package*/ final int inMethod;

    /**
     * Creates a BranchCov.
     *
     * @param covered the number of branches covered
     * @param inMethod the total number of branches in the method
     */
    /*package*/ BranchCov(int covered, int inMethod) {
      this.covered = covered;
      this.inMethod = inMethod;
    }
  }

  /**
   * Creates a {@code TestCoverageInfo} in which no branch has been covered.
   *
   * @param totalConditions the number of branch conditions; must be non-negative
   * @param map maps a method signature to the indices of the branch conditions in that method
   */
  public TestCoverageInfo(int totalConditions, Map<String, Set<Integer>> map) {
    if (totalConditions < 0) {
      throw new IllegalArgumentException();
    }
    branchTrue = new int[totalConditions];
    branchFalse = new int[totalConditions];
    methodToIndices = Collections.unmodifiableMap(map);
  }

  private String getCoverageInfo() {
    StringBuilder b = new StringBuilder(64);
    int totalBranchesCovered = 0;
    int totalBranches = 0;
    for (Map.Entry<String, Set<Integer>> entry : methodToIndices.entrySet()) {
      String methodSignature = entry.getKey();
      BranchCov covAndTot = getCoverageInfo(methodSignature);
      int branchesCovered = covAndTot.covered;
      int branchesInMethod = covAndTot.inMethod;
      totalBranchesCovered += branchesCovered;
      totalBranches += branchesInMethod;
      b.append((methodSignature == null ? "other" : methodSignature));
      b.append(": ");
      b.append(coverageString(branchesCovered, branchesInMethod));
      b.append(Globals.lineSep);
    }
    b.append("TOTAL :");
    b.append(coverageString(totalBranchesCovered, totalBranches));
    b.append(Globals.lineSep);
    return b.toString();
  }

  /**
   * Returns a string such as "3/4 (75%)" that reports how many of the given branches are covered.
   * The percentage is rounded down, so it is 100% only if every branch is covered.
   *
   * @param covered the number of branches covered
   * @param total the total number of branches
   * @return a string reporting the number and percentage of branches covered
   */
  private static String coverageString(int covered, int total) {
    if (total == 0) {
      return "0/0 (n/a)";
    }
    return String.format("%d/%d (%d%%)", covered, total, 100L * covered / total);
  }

  /**
   * Returns how many branches the given method has, and how many of them are covered. Each branch
   * point has two branches: its true branch and its false branch.
   *
   * @param methodSignature the signature of a method, or null for the branches that are not in any
   *     method
   * @return the number of covered branches and the total number of branches in the method
   */
  private BranchCov getCoverageInfo(String methodSignature) {
    Set<Integer> indices = methodToIndices.get(methodSignature);
    int totalBranches = indices.size() * 2;
    int branchesCovered = 0;
    for (Integer i : indices) {
      if (branchTrue[i] > 0) {
        branchesCovered++;
      }
      if (branchFalse[i] > 0) {
        branchesCovered++;
      }
    }
    assert branchesCovered <= totalBranches;
    return new BranchCov(branchesCovered, totalBranches);
  }

  /**
   * Returns a human-readable report of the branch coverage of the given class: the number and
   * fraction of branches covered in each method and in total.
   *
   * @param clazz the class whose coverage to report
   * @return a report of the branch coverage of the class, or a message stating that the class is
   *     not instrumented for branch coverage
   */
  public static String getCoverageInfo(Class<?> clazz) {

    if (!isInstrumented(clazz)) {
      return "Class<?> not instrumented for branch coverage: " + clazz.getName();
    }

    try {
      return getCoverageInfoObject(clazz).getCoverageInfo();
    } catch (RandoopBug e) {
      throw e;
    } catch (Exception e) {
      throw new RandoopBug(e);
    }
  }

  /**
   * Returns the test coverage information that is stored in static field {@code
   * randoopCoverageInfo} of the given class.
   *
   * @param clazz the class whose coverage information to obtain
   * @return the test coverage information for the class
   */
  private static TestCoverageInfo getCoverageInfoObject(Class<?> clazz) {
    try {
      Field f = clazz.getDeclaredField("randoopCoverageInfo");
      f.setAccessible(true);
      TestCoverageInfo result = (TestCoverageInfo) f.get(null);
      if (result == null) {
        throw new RandoopBug("randoopCoverageInfo field is null in class " + clazz);
      }
      return result;
    } catch (NoSuchFieldException e) {
      throw new RandoopBug("Class " + clazz + " lacks the field randoopCoverageInfo", e);
    } catch (IllegalAccessException e) {
      throw new RandoopBug("this can't happen", e);
    }
  }

  private static boolean isInstrumented(Class<?> clazz) {
    for (Field f : clazz.getDeclaredFields()) {
      if (f.getName().equals("randoopCoverageInfo")) {
        return true;
      }
    }
    return false;
  }
}
