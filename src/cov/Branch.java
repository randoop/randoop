package cov;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import randoop.util.Files;

import plume.Pair;

/**
 * Immutable. Contains the information for a single branch, including:
 *
 * <ul>
 * <li>The containing class.
 * <li>The containing method, if any.
 * <li>The line number.
 * <li>The branch number. Each branch in a class has a unique identifying number.
 * <li>The branch direction (true or false).
 * </ul>
 *
 * The cov package implements a basic branch coverage instrumenter
 * that we use for the branch-directed test generation research.
 *
 * This tool is prototype-quality, not for production use. In
 * particular, it is missing a number of features including tracking
 * coverage for switch statements, and lack of support for
 * generics.
 */
public class Branch implements CoverageAtom, Serializable {

  private static final long serialVersionUID = 471914492716268803L;
  public final String className;
  public final String methodName;
  public final int lineNumber;
  public final int branchNumber; // Branch number recorded by coverage tracking tool.
  public final boolean branch;

  private static Map<Branch,Branch> savedBranchInfos =
    new LinkedHashMap<Branch,Branch>();

  /**
   * Creates a new branch object.
   * 
   * @param className
   *          the name of the class containing this branch.
   * @param methodName
   *          the name of the method containing this branch. If this branch is
   *          part of a constructor, the string must be "<init>". If this
   *          branch is not part of a method or constructor, the string must be
   *          null.
   * @param lineNumber
   *          the source line number where this branch appears.
   * @param branchNumber
   *          the branch number id.
   * @param branch
   *          the direction of this branch.
   */
  // TODO verify that if another branch with the same className and line number exists,
  // it has the same method name and branch number as well.
  protected static Branch getBranchInfo(String className,
      String methodName, int lineNumber, int branchNumber, boolean branch) {
    Branch o = new Branch(className, methodName, lineNumber, branchNumber, branch);
    Branch saved = savedBranchInfos.get(o);
    if (saved == null) {
      savedBranchInfos.put(o, o);
      saved = o;
    }
    return saved;
  }

  private Branch(String className, String methodName, int lineNumber,
      int branchNumber, boolean branch) {
    if (className == null) throw new IllegalArgumentException("className cannot be null.");
    this.className = className;
    this.methodName = methodName;
    this.lineNumber = lineNumber;
    this.branchNumber = branchNumber;
    this.branch = branch;
  }

  @Override
  // If you change the result of this method, make sure you udpate the parse method.
  public String toString() {
    return "classname=" + className + ",methodname=" + methodName + ",line="
    + lineNumber + ",id=" + branchNumber + ",direction=" + branch;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (!(o instanceof Branch)) return false;
    Branch other = (Branch)o;
    return className.equals(other.className)
    && methodName == null ? other.methodName == null : methodName.equals(other.methodName)
        && lineNumber == other.lineNumber
        && branchNumber == other.branchNumber
        && branch == other.branch;
  }

  @Override
  public int hashCode() {
    int ret = 13;
    ret = 31*ret + className.hashCode();
    if (methodName != null)
      ret = 31*ret + methodName.hashCode();
    ret = 31*ret + new Integer(lineNumber).hashCode();
    ret = 31*ret + new Integer(branchNumber).hashCode();
    ret = 31*ret + new Boolean(branch).hashCode();
    return ret;
  }

  public Branch getOppositeBranch() {
    return Branch.getBranchInfo(this.className, this.methodName, this.lineNumber, this.branchNumber, !this.branch);
  }

  public String getClassName() {
    return this.className;
  }

  public String getMethodName() {
    return this.methodName;
  }

  public int getLineNumber() {
    return this.lineNumber;
  }

  /**
   * Creates a branch object from a String. The string is in
   * the same format as the one used in toString().
   * @param str a string representation of a branch.
   */
  public static Branch parse(String str) {
    if (str == null) throw new IllegalArgumentException("str cannot be null.");
    String[] pairs = str.split(",");
    if (pairs.length != 5) throw new IllegalArgumentException("invalid string: " + str);

    List<Pair<String, String>> pairs2 = readKeyValuePairs(pairs, new String[] {
        "classname", "methodname", "line", "id", "direction" });

    String classname = pairs2.get(0).b;
    String methodname = pairs2.get(1).b;
    if (methodname.equals("null"))
      methodname = null;

    int line = Integer.parseInt(pairs2.get(2).b);
    int id = Integer.parseInt(pairs2.get(3).b);
    boolean dir = Boolean.parseBoolean(pairs2.get(4).b);

    return new Branch(classname, methodname, line, id, dir);
  }

  // Parses key-value pairs, where each pair is of the form <key>=<value>, 
  // the i-th key is as specified in expectedKeys, and the values are not empty.
  private static List<Pair<String, String>> readKeyValuePairs(String[] pairs, String[] expectedKeys) {
    assert pairs.length == expectedKeys.length;
    List<Pair<String,String>> ret = new ArrayList<Pair<String,String>>();
    for (int i = 0 ; i < pairs.length ; i++) {
      String s = pairs[i];
      String[] classnamePair = s.split("=");
      if (classnamePair.length != 2)
        throw new IllegalArgumentException("invalid key-value pair: " + s);
      String key = classnamePair[0];
      if (!key.equals(expectedKeys[i]))
        throw new IllegalArgumentException("invalid key value (expected " + expectedKeys[i] + "): " + s);
      String value = classnamePair[1];
      if (key.length() == 0 || value.length() == 0)
        throw new IllegalArgumentException("invalid string (empty key or value): " + s);
      ret.add(new Pair<String, String>(key, value));
    }
    return ret;
  }

  /**
   * Print the given set of branches out to the given file, as text.
   * If (sorted == true) will sort the branches lexicographically before printing.
   */
  public static void writeToFile(Set<Branch> branches, String filename, boolean sorted) {
    Set<Branch> sortedMaybe = null;
    if (sorted) {
      Comparator<Branch> comp = new Comparator<Branch>() {
        public int compare(Branch o1, Branch o2) {
          return o1.toString().compareTo(o2.toString());
        }
      };
      sortedMaybe = new TreeSet<Branch>(comp);
      sortedMaybe.addAll(branches);
    } else {
      sortedMaybe = branches;
    }
    List<String> lines = new ArrayList<String>();
    for (Branch b : sortedMaybe) {
      lines.add(b.toString());
    }
    try {
      Files.writeToFile(lines, filename);
    } catch (IOException e) {
      throw new Error(e);
    }
  }
  
  /**
   * Read the given file assuming each line describes a branch. Returns the
   * collections of branches described in the file.
   * 
   * Allows whitespace or comment lines (a comment line starts with "#").
   */
  public static Set<Branch> readFromFile(String filename) {
    List<String> lines = null;
    try {
      lines = Files.readWhole(filename);
    } catch (IOException e) {
      throw new Error(e);
    }
    Set<Branch> branches = new LinkedHashSet<Branch>();
    for (String l : lines) {
      l = l.trim();
      if (l.length() == 0 || l.startsWith("#"))
        continue;
      branches.add(Branch.parse(l));
    }
    return branches;
  }

}
