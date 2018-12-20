package randoop.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import randoop.Globals;
import randoop.main.RandoopBug;

public class TestCoverageInfo {

  public final int[] branchTrue;
  public final int[] branchFalse;
  public final Map<String, Set<Integer>> methodToIndices;

  // A pair of: branches covered, total branches in method
  private static class BranchCov {
    final int covered;
    final int inMethod;

    BranchCov(int covered, int inMethod) {
      this.covered = covered;
      this.inMethod = inMethod;
    }
  }

  public TestCoverageInfo(int totalBranches, Map<String, Set<Integer>> map) {
    if (totalBranches < 0) throw new IllegalArgumentException();
    branchTrue = new int[totalBranches];
    branchFalse = new int[totalBranches];
    methodToIndices = Collections.unmodifiableMap(map);
  }

  private String getCoverageInfo() {
    StringBuilder b = new StringBuilder();
    int totalBranchesCovered = 0;
    int totalBranches = 0;
    for (Map.Entry<String, Set<Integer>> entry : methodToIndices.entrySet()) {
      String methodSignature = entry.getKey();
      BranchCov covAndTot = getCoverageInfo(methodSignature);
      int branchesCovered = covAndTot.covered;
      int branchesInMethod = covAndTot.inMethod;
      totalBranchesCovered += branchesCovered;
      totalBranches += branchesInMethod;
      double percentCovered = ((double) branchesCovered) / ((double) branchesInMethod);
      b.append(
          (methodSignature == null ? "other" : methodSignature)
              + ": "
              + branchesCovered
              + "/"
              + branchesInMethod
              + " ("
              + percentCovered
              + "%)"
              + Globals.lineSep);
    }
    double totalPercent = ((double) totalBranchesCovered) / ((double) totalBranches);
    b.append(
        "TOTAL :"
            + totalBranchesCovered
            + "/"
            + totalBranches
            + " ("
            + totalPercent
            + "%)"
            + Globals.lineSep);
    return b.toString();
  }

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

  public static String getCoverageInfo(Class<?> clazz) {

    if (!isInstrumented(clazz)) {
      return "Class<?> not instrumented for branch coverage: " + clazz.getName();
    }

    try {
      return getCoverageInfoObject(clazz).getCoverageInfo();
    } catch (Exception e) {
      throw new RandoopBug(e);
    }
  }

  private static TestCoverageInfo getCoverageInfoObject(Class<?> clazz)
      throws IllegalArgumentException, SecurityException, IllegalAccessException,
          NoSuchFieldException {
    Field f = clazz.getDeclaredField("randoopCoverageInfo");
    f.setAccessible(true);
    return (TestCoverageInfo) f.get(null);
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
