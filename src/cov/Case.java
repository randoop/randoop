package cov;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * 
 * Immutable. Contains line number, case number, etc. for a single
 * switch case. NOTE: We haven't incorporated switch statements into
 * the tool yet.  So this class is dead code at the moment.
 *
 * A randoop.Sequence object stores the set of CoverageAtom objects
 * corresponding to the branch directions and switch cases covered by
 * the sequence.
 *
 * The cov package implements a basic branch coverage instrumenter
 * that we use for the branch-directed test generation research.
 *
 * This tool is prototype-quality, not for production use. In
 * particular, it is missing a number of features including tracking
 * coverage for switch statements, and lack of support for
 * generics.
 */
public class Case implements CoverageAtom, Serializable {

  private static final long serialVersionUID = 3241913234342455590L;
  public final String className;
  public final int lineNumber;
  public final int switchNumber; // Switch number recorded by coverage tracking tool.
  public final int caseNumber; // The case that this atom represents.

  private static Map<Case,Case> savedBranchInfos =
    new LinkedHashMap<Case,Case>();

  public static Case getSwitchInfo(String className, int lineNumber, int switchNumber, int caseNumber) {
    Case o = new Case(className, lineNumber, switchNumber, caseNumber);
    Case saved = savedBranchInfos.get(o);
    if (saved == null) {
      savedBranchInfos.put(o, o);
      saved = o;
    }
    return saved;
  }

  public Case(String className, int lineNumber, int switchNumber, int caseNumber) {
    if (className == null) throw new IllegalArgumentException("className cannot be null.");
    this.className = className;
    this.lineNumber = lineNumber;
    this.switchNumber = switchNumber;
    this.caseNumber = caseNumber;
  }

  @Override
  public String toString() {
    return "swtich:name=" + className + "line=" + lineNumber + ",num=" + switchNumber + ",case=" + caseNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (!(o instanceof Case)) return false;
    Case other = (Case)o;
    return className.equals(other.className)
    && lineNumber == other.lineNumber
    && switchNumber == other.switchNumber
    && caseNumber == other.caseNumber;
  }

  @Override
  public int hashCode() {
    int ret = 13;
    ret = 31*ret + className.hashCode();
    ret = 31*ret + new Integer(lineNumber).hashCode();
    ret = 31*ret + new Integer(switchNumber).hashCode();
    ret = 31*ret + new Integer(caseNumber).hashCode();
    return ret;
  }

  public String getClassName() {
    return this.className;
  }

  public int getLineNumber() {
    return this.lineNumber;
  }
}
