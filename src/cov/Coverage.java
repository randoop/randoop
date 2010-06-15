package cov;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.util.Reflection;
import plume.Triple;

/**
 * This class contains utility methods to access the coverage
 * information in classes that were instrumented using the coverage
 * instrumenter.
 *
 * The cov package implements a basic branch coverage instrumenter
 * that we use for the branch-directed test generation research.
 *
 * This tool is prototype-quality, not for production use. In
 * particular, it is missing a number of features including tracking
 * coverage for switch statements, and lack of support for
 * generics.
 */
public class Coverage {

  private static final String lineSep = System.getProperty("line.separator");
  
  public static Map<Class<?>,Boolean> isInstrumentedCached =
    new LinkedHashMap<Class<?>, Boolean>();
  
  /**
   * Same as c.isPrimitive() but faster if this test is done very 
   * frequently (as it is in Randoop).
   */
  public static boolean isInstrumented(Class<?> c) {
    if (c == null)
      throw new IllegalArgumentException("c cannot be null.");
    Boolean b = isInstrumentedCached.get(c);
    if (b == null) {
      try {
        c.getField(Constants.isInstrumentedField);
        b = true;
      } catch (NoSuchFieldException e) {
        b = false;
      }
      assert b != null;
      isInstrumentedCached.put(c, b);
    }
    return b;
  }

  public static boolean isInstrumented(Member member) {
    if (member==null) throw new IllegalArgumentException("member cannot be null.");
    return isInstrumented(member.getDeclaringClass());
  }

  /**
   * If line belongs to a mehod that does not have an id (i.e. inner-class
   * method) returns null.
   */
  public static String getMethodIdForLine(Class<?> cls, int lineNumber) {
    if (cls==null) throw new IllegalArgumentException("cls cannot be null.");
    if (!isInstrumented(cls)) throw new IllegalArgumentException("cls is not coverage-instrumented: " + cls.getName());

    Map<String, int[]> startEndLines = getMethodStartEndLines(cls);
    for (Map.Entry<String, int[]> e : startEndLines.entrySet()) {
      if (inInterval(lineNumber, e.getValue())) {
        return e.getKey();
      }
    }
    return null;
  }

  // Inclusive.
  private static boolean inInterval(int lineNumber, int[] value) {
    assert lineNumber > 0;
    assert value != null && value.length == 2;
    assert value[0] <= value[1];
    return lineNumber >= value[0] && lineNumber <= value[1];
  }


  /**
   *
   * @param cls
   *          The class whose source lines are of interest.
   * @param startLine
   *          The first line of interest. Must be >= 1.
   * @param endLine
   *          The last line of interest. Must be >= startLine. If endLine is
   *          greater than the number of lines in the source file, the method
   *          returns lines up to the last line in the file.
   * @return Returns null if lineNumber is greater than lines in source file.
   */
  public static List<String> getSourceLines(Class<?> cls, int startLine, int endLine) {

    // Check inputs.
    if (cls==null) throw new IllegalArgumentException("cls cannot be null.");
    if (!isInstrumented(cls)) throw new IllegalArgumentException("cls is not coverage-instrumented: " + cls.getName());
    if (startLine <= 0) throw new IllegalArgumentException("startLine must be >0: " + startLine);
    if (endLine <= 0) throw new IllegalArgumentException("endLine must be >0: " + endLine);
    if (startLine > endLine) throw new IllegalArgumentException("startLine must be <= endLine.");

    String sourceFileName = getSourceFileName(cls) + ".orig";
    InputStream fileStream = cls.getResourceAsStream(sourceFileName);
    List<String> lines = new ArrayList<String>();
    try {
      LineNumberReader reader = new LineNumberReader(new InputStreamReader(fileStream));
      String line = reader.readLine();
      while (line != null && reader.getLineNumber() <= endLine) {
        int lineNumber = reader.getLineNumber();
        if (lineNumber >= startLine)
          lines.add(line);
        line = reader.readLine();
      }
    } catch (Exception e) {
      // XXX add e.getMessage to other statements as well.
      throw new RuntimeException("Error in coverage instrumenter: " + e.getMessage()); // Should
      // never
      // get
      // there.
    }
    return lines;
  }

  /**
   *
   * @param cls
   *          The class whose annotated source you're interested int.
   * @return The lines of source in cls, if it is instrumented for coverage.
   *         Each line corresponding to a branch statements is preceded with a
   *         "T" if the true branch was followed, and/or also with an "F" if the
   *         false branch was followed.
   */
  public static List<String> getCoverageAnnotatedSource(Class<?> cls) {

    List<String> lines = getSourceLines(cls, 1, Integer.MAX_VALUE);
    Set<Integer> branchLines = new LinkedHashSet<Integer>();
    int[] branchLinesArray = getBranchLineNumbers(cls);
    for (int l : branchLinesArray) branchLines.add(l);
    int[] trues = getTrueBranches(cls);
    int[] falses = getFalseBranches(cls);

    List<String> ret = new ArrayList<String>();

    for (int line = 1 ; line <= lines.size() ; line++) {
      StringBuilder b = new StringBuilder();
      if (branchLines.contains(line)) {

        // Find branch number. Iterate through branchLines and when we find
        // that branchLines[x]=i, then x is the branch number.
        int branchNumber = -1;
        for (int j = 0 ; j < branchLinesArray.length ; j++) {
          if (branchLinesArray[j]==line) {
            branchNumber = j;
            break;
          }
        }
        assert branchNumber >= 0;
        if (trues[branchNumber] > 0) {
          b.append("T");
        } else {
          b.append("_");
        }
        if (falses[branchNumber] > 0) {
          b.append("F");
        } else {
          b.append("_");
        }
      } else {
        b.append("  ");
      }
      b.append(lines.get(line - 1));
      ret.add(b.toString());
    }

    return ret;
  }

  /**
   *
   * @return Returns null if lineNumber is greater than lines in source file.
   */
  public static String getMethodSource(Class<?> cls, int lineNumber, String prefix) {
    if (cls==null) throw new IllegalArgumentException("cls cannot be null.");
    if (prefix==null) throw new IllegalArgumentException("prefix cannot be null.");
    if (!isInstrumented(cls)) throw new IllegalArgumentException("cls is not coverage-instrumented: " + cls.getName());
    if (lineNumber <= 0) throw new IllegalArgumentException("lineNumber must be >0.");

    String methodId = getMethodIdForLine(cls, lineNumber);
    int startLine = -1;
    int endLine = -1;
    if (methodId == null) {
      startLine = lineNumber;
      endLine = lineNumber;
    } else {
      int[] startEndLines = getMethodStartEndLines(cls).get(methodId);
      startLine = startEndLines[0];
      endLine = startEndLines[1];
    }
    List<String> lines = getSourceLines(cls, startLine, endLine);
    int relativeLineNumber = lineNumber - startLine; // zero-based.
    assert relativeLineNumber < lines.size();
    StringBuilder b = new StringBuilder();
    for (int i = 0 ; i < lines.size() ; i++) {
      if (i == relativeLineNumber) {
        b.append(prefix);
      } else {
        for (int j = 0 ; j < prefix.length() ; j++)
          b.append(' ');
      }
      b.append(lines.get(i));
      b.append(lineSep);
    }
    return b.toString();
  }

  /**
   * @return Returns null if the method belongs to a class that is not
   *         instrumented.
   */
  public static Set<CoverageAtom> getBranches(Member m) {
    if (m == null) throw new IllegalArgumentException("m cannot be null.");
    if (!((m instanceof Method) || (m instanceof Constructor<?>)))
      throw new IllegalArgumentException("m must be a method or constructor.");
    initCoverage(m.getDeclaringClass());
    return membersToAtoms.get(m);
  }

  /**
   *
   * @param cls
   * @return Returns null if the class is not instrumented.
   */
  public static Set<CoverageAtom> getBranches(Class<?> cls) {
    if (cls == null) throw new IllegalArgumentException("cls cannot be null.");
    if (!isInstrumented(cls)) throw new IllegalArgumentException("cls is not coverage-instrumented.");
    initCoverage(cls);
    return classesToAtoms.get(cls);
  }

  /**
   * Returns null if method has no id.
   */
  public static String getMethodId(Member m) {
    Annotation[] annos = null;
    if (m instanceof Method) {
      Method method = (Method)m;
      annos = method.getDeclaredAnnotations();
    } else if (m instanceof Constructor<?>) {
      Constructor<?> cons = (Constructor<?>)m;
      annos = cons.getDeclaredAnnotations();
    } else {
      throw new IllegalArgumentException("m must be a Method or Constructor.");
    }
    Annotation methodIdAnno = null;
    for (Annotation anno : annos) {
      if (anno.annotationType().getName().endsWith(Constants.MethodIdAnnotation)) {
        methodIdAnno = anno;
        break;
      }
    }
    if (methodIdAnno == null)
      return null;
    try {
      Method valueMethod = methodIdAnno.annotationType().getMethod("value");
      return (String)valueMethod.invoke(methodIdAnno);
    } catch (Exception e) {
      // Should never get here. XXX COULD HAPPEN IF PRIVATE CLASS?
      throw new RuntimeException("Error in coverage instrumenter.");
    }
  }

  public static void clearCoverage(Collection<Class<?>> classes) {
    for (Class<?> cls : classes) {
      Arrays.fill(getTrueBranches(cls), 0);
      Arrays.fill(getFalseBranches(cls), 0);
    }
  }

  public static int[] getTrueBranches(Class<?> cls) {
    if (cls==null) throw new IllegalArgumentException("cls cannot be null.");
    if (!isInstrumented(cls)) throw new IllegalArgumentException("cls is not coverage-instrumented: " + cls.getName());
    try {
      Field f = cls.getField(Constants.trueBranches);
      makeAccessible(f);
      return (int[]) f.get(null);
    } catch (Exception e) {
      throw new RuntimeException("Error in coverage instrumenter."); // Should
      // never
      // get
      // here.
    }
  }

  public static int[] getFalseBranches(Class<?> cls) {
    if (cls==null) throw new IllegalArgumentException("cls cannot be null.");
    if (!isInstrumented(cls)) throw new IllegalArgumentException("cls is not coverage-instrumented: " + cls.getName());
    try {
      Field f = cls.getField(Constants.falseBranches);
      makeAccessible(f);
      return (int[]) f.get(null);
    } catch (Exception e) {
      throw new RuntimeException("Error in coverage instrumenter."); // Should
      // never
      // get
      // here.
    }
  }

  public static int[] getBranchLineNumbers(Class<?> cls) {
    if (cls==null) throw new IllegalArgumentException("cls cannot be null.");
    if (!isInstrumented(cls)) throw new IllegalArgumentException("cls is not coverage-instrumented: " + cls.getName());
    try {
      Field f = cls.getField(Constants.branchLines);
      makeAccessible(f);
      return (int[]) f.get(null);
    } catch (Exception e) {
      throw new RuntimeException("Error in coverage instrumenter."); // Should
      // never
      // get
      // here.
    }
  }

  @SuppressWarnings("unchecked")
  public static Map<String,int[]> getMethodIdToBranches(Class<?> cls) {
    if (cls==null) throw new IllegalArgumentException("cls cannot be null.");
    if (!isInstrumented(cls)) throw new IllegalArgumentException("cls is not coverage-instrumented: " + cls.getName());
    try {
      Field f = cls.getField(Constants.methodIdToBranches);
      makeAccessible(f);
      return (Map<String,int[]>) f.get(null);
    } catch (Exception e) {
      throw new RuntimeException("Error in coverage instrumenter: " + e); // Should
      // never
      // get
      // here.
    }
  }

  @SuppressWarnings("unchecked")
  public static Map<String,int[]> getMethodStartEndLines(Class<?> cls) {
    if (cls==null) throw new IllegalArgumentException("cls cannot be null.");
    if (!isInstrumented(cls)) throw new IllegalArgumentException("cls is not coverage-instrumented: " + cls.getName());
    try {
      Field f = cls.getField(Constants.methodLineSpansField);
      makeAccessible(f);
      return (Map<String,int[]>) f.get(null);
    } catch (Exception e) {
      throw new RuntimeException("Error in coverage instrumenter."); // Should
      // never
      // get
      // here.
    }
  }



  public static String getSourceFileName(Class<?> cls) {
    if (cls==null) throw new IllegalArgumentException("cls cannot be null.");
    if (!isInstrumented(cls)) throw new IllegalArgumentException("cls is not coverage-instrumented: " + cls.getName());
    try {
      Field f = cls.getField(Constants.sourceFileNameField);
      makeAccessible(f);
      return (String) f.get(null);
    } catch (Exception e) {
      throw new RuntimeException("Error in coverage instrumenter."); // Should
      // never
      // get
      // here.
    }
  }

  private static void makeAccessible(Field f) {
    assert f != null;
    if (!f.isAccessible())
      f.setAccessible(true);
  }

  public static Set<CoverageAtom> getCoveredAtoms(Class<?> cls) {
    Set<CoverageAtom> atoms = new LinkedHashSet<CoverageAtom>();
    initCoverage(cls);
    String className = cls.getName();
    int[] trueBranches = getTrueBranches(cls);
    int[] falseBranches = getFalseBranches(cls);
    for (int i = 0 ; i < trueBranches.length ; i++) {
      if (trueBranches[i] > 0) atoms.add(atomInfoToAtoms.get(new Triple<String, Integer, Boolean>(className, i, true)));
      if (falseBranches[i] > 0) atoms.add(atomInfoToAtoms.get(new Triple<String, Integer, Boolean>(className, i, false)));
    }
    return atoms;
  }

  // TODO replace impl with use of getCoveredAtoms(Class).
  public static Set<CoverageAtom> getCoveredAtoms(Collection<Class<?>> classes) {
    Set<CoverageAtom> atoms = new LinkedHashSet<CoverageAtom>();
    for (Class<?> cls : classes) {
      initCoverage(cls);
      String className = cls.getName();
      int[] trueBranches = getTrueBranches(cls);
      int[] falseBranches = getFalseBranches(cls);
      for (int i = 0 ; i < trueBranches.length ; i++) {
        if (trueBranches[i] > 0) atoms.add(atomInfoToAtoms.get(new Triple<String, Integer, Boolean>(className, i, true)));
        if (falseBranches[i] > 0) atoms.add(atomInfoToAtoms.get(new Triple<String, Integer, Boolean>(className, i, false)));
      }
    }
    return atoms;
  }

  private static Map<CoverageAtom,Member> atomsToMembers = new LinkedHashMap<CoverageAtom, Member>();
  private static Map<Member,Set<CoverageAtom>> membersToAtoms = new LinkedHashMap<Member,Set<CoverageAtom>>();
  private static Map<Class<?>, Set<CoverageAtom>> classesToAtoms = new LinkedHashMap<Class<?>, Set<CoverageAtom>>();
  // Given class name, branch number, and boolean value, returns the corresponding coverage atom.
  public static Map<Triple<String,Integer,Boolean>,CoverageAtom> atomInfoToAtoms =
    new LinkedHashMap<Triple<String,Integer,Boolean>, CoverageAtom>();

  public static void initCoverage(Class<?> cls) {
    assert cls != null;
    if (classesToAtoms.containsKey(cls))
      return;
    if (!isInstrumented(cls))
      throw new IllegalArgumentException("cls is not coverage-instrumented: " + cls.getName());

    int[] branchLineNumbers = getBranchLineNumbers(cls);

    Set<Integer> branchNumbers = new LinkedHashSet<Integer>();
    for (int i = 0 ; i < branchLineNumbers.length ; i++)
      branchNumbers.add(i);

    Map<String,int[]> methodIdToBranches = getMethodIdToBranches(cls);

    Set<Member> allDeclaredMembers = new LinkedHashSet<Member>();
    allDeclaredMembers.addAll(Arrays.asList(cls.getDeclaredMethods()));
    allDeclaredMembers.addAll(Arrays.asList(cls.getDeclaredConstructors()));

    classesToAtoms.put(cls, new LinkedHashSet<CoverageAtom>());

    for (Member method : allDeclaredMembers) {

      membersToAtoms.put(method, new LinkedHashSet<CoverageAtom>());

      // Get the id for this method (added as an annotation in the instrumented sources).
      String methodId = getMethodId(method);
      // If the method has no id, it means we do not keep track of its
      // individual coverage, so it will have no entries in the maps.
      if (methodId == null)
        continue;
      // Get the branch numbers for this method.
      int[] methodBranchNumbers = methodIdToBranches.get(methodId);
      //   Every method with an id has a non-null value in the above map.
      assert methodBranchNumbers != null : cls.toString();

      for (int branchNumber : methodBranchNumbers) {
        // Line numbers are indexed by branch numbers.
        assert branchNumber < branchLineNumbers.length : cls.toString();
        // Update maps.
        int lineNumber = branchLineNumbers[branchNumber];
        String methodName = null;
        if (method instanceof Method)
          methodName = method.getName();
        else {
          assert method instanceof Constructor<?>;
          methodName = "<init>";
        }
        Branch t = Branch.getBranchInfo(cls.getName(), methodName, lineNumber, branchNumber, true);
        Branch f = Branch.getBranchInfo(cls.getName(), methodName, lineNumber, branchNumber, false);
        atomsToMembers.put(t, method);
        atomsToMembers.put(f, method);
        membersToAtoms.get(method).add(t);
        membersToAtoms.get(method).add(f);
        classesToAtoms.get(cls).add(t);
        classesToAtoms.get(cls).add(f);
        atomInfoToAtoms.put(new Triple<String, Integer, Boolean>(cls.getName(), branchNumber, true), t);
        atomInfoToAtoms.put(new Triple<String, Integer, Boolean>(cls.getName(), branchNumber, false), f);
        // Remove the branch number from the todo set.
        assert branchNumbers.contains(branchNumber);
        branchNumbers.remove(branchNumber);
      }
    }

    // The remaining branch numbers belong to no method or constructor.
    for (Iterator<Integer> it = branchNumbers.iterator() ; it.hasNext() ; ) {
      Integer branchNumber = it.next();
      int lineNumber = branchLineNumbers[branchNumber];
      Branch t = Branch.getBranchInfo(cls.getName(), null, lineNumber, branchNumber, true);
      Branch f = Branch.getBranchInfo(cls.getName(), null, lineNumber, branchNumber, false);
      classesToAtoms.get(cls).add(t);
      classesToAtoms.get(cls).add(f);
      atomInfoToAtoms.put(new Triple<String, Integer, Boolean>(cls.getName(), branchNumber, true), t);
      atomInfoToAtoms.put(new Triple<String, Integer, Boolean>(cls.getName(), branchNumber, false), f);
      // Remove the branch number from the todo set.
      assert branchNumbers.contains(branchNumber);
      it.remove();
    }

    assert classesToAtoms.get(cls).size() == 2 * branchLineNumbers.length
    : "classesToAtoms.size()=" + classesToAtoms.size() + ",branchLineNumbers.length=" + branchLineNumbers.length;
  }


  /**
   *
   * @param cov
   * @return Returns null if this coverage atom does not belong to a method or
   *         constructor.
   */
  public static Member getMemberContaining(CoverageAtom cov) {
    if (cov == null) throw new IllegalArgumentException("cov cannot be null.");
    Class<?> cls = Reflection.classForName(cov.getClassName());
    initCoverage(cls);
    return atomsToMembers.get(cov);
  }

  /**
   * Increments the count for the given branch by 1.
   */
  public static void touch(Branch br) {
    Class<?> cls = Reflection.classForName(br.getClassName());
    if (br.branch) {
      getTrueBranches(cls)[br.branchNumber]++;
    } else {
      getFalseBranches(cls)[br.branchNumber]++;
    }
  }

}
