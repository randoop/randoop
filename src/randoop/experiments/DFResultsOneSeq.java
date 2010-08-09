package randoop.experiments;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import randoop.Globals;
import randoop.Sequence;
import randoop.SequenceParseException;
import randoop.Variable;

import cov.Branch;
import cov.CoverageAtom;

/**
 * Encapsulates the results of the branch analysis for a given
 * sequence and its frontier branch.
 */
public class DFResultsOneSeq implements Serializable {

  private static final long serialVersionUID = -7983260094515559715L;

  /**
   * Class that contains information about the values in the frontier
   */
  public static class VariableInfo implements Serializable {
    private static final long serialVersionUID = 20080625L;
    public Variable value;
    // Use TreeSet to ensure same iteration order.
    // (For regression testing, since order may vary on other
    // set implementations on Linux vs. OS X.)
    public Set<String> branch_compares = new TreeSet<String>();
    public VariableInfo (Variable value) {
      this.value = value;
    }
    public void add_branch_compare (String compared_to) {
      branch_compares.add (compared_to);
    }
    public String toString() {
      return value + ":" + branch_compares;
    }
  }

  public Sequence sequence;
  public CoverageAtom frontierBranch;
  public Set<VariableInfo> values;

  public DFResultsOneSeq(Sequence sequence,
      CoverageAtom frontierBranch,
      Set<VariableInfo> values) {
    this.sequence = sequence;
    this.frontierBranch = frontierBranch;
    this.values = values;
  }

  /**
   * Same as toParseableString().
   */
  @Override
  public String toString() {
    return toParseableString();
  }
  
  /**
   * Outputs this object as a String that can be parsed
   * by method DFResultsOneSeq.parse(List<String>).
   * 
   * See documentation for DataFlowOutput.parse(String filename).
   */
  public String toParseableString() {
        
    StringBuilder out = new StringBuilder();
    out.append(frontierBranch.toString());
    out.append(Globals.lineSep);
    out.append(sequence.toParseableString());
    out.append("VARS " + values.size());
    out.append(Globals.lineSep);
    
    // We want to ensure output is always the same across multiple
    // runs (on different operating systems), for regression testing.
    // So we sort the variables based on their string representation.
    Comparator<VariableInfo> comp = new Comparator<VariableInfo>() {
      public int compare(VariableInfo o1, VariableInfo o2) {
        return o1.toString().compareTo(o2.toString());
      }
    };
    Set<VariableInfo> sortedValues = new TreeSet<VariableInfo>(comp);
    sortedValues.addAll(values);

    for (VariableInfo vi : sortedValues) {
      out.append(vi.value.getName());
      for (String s : vi.branch_compares) {
        out.append(" " + s);
      }
      out.append(Globals.lineSep);
    }
    return out.toString();
  }
  
  /**
   * Creates a DFResultsOneSeq from a list of lines (presumably generated via
   * the toParseableString() method).
   */
  public static DFResultsOneSeq parse(List<String> lines) {
    if (lines.size() < 3)
      throw new IllegalArgumentException("Invalid record: " + print(lines));
    
    // First line is branch description.
    Branch b = Branch.parse(lines.get(0));

    // Find end of sequence.
    int varIndex = -1;
    for (int i = 1 ; i < lines.size() ; i++) {
      if (lines.get(i).startsWith("VARS")) {
        varIndex = i;
        break;
      }
    }
    if (varIndex == -1)
      throw new IllegalArgumentException("Invalid record: " + print(lines));

    // Lines 1--(varIndex-1) is the sequence.
    Sequence sequence;
    try {
      sequence = Sequence.parse(lines.subList(1, varIndex));
    } catch (SequenceParseException e) {
      throw new Error(e);
    }

    // Find out how many vars there are.
    Integer numVars = Integer.parseInt(lines.get(varIndex).substring("VARS".length()).trim());
    if (varIndex + numVars != lines.size() - 1)
      throw new IllegalArgumentException("Invalid record: " + print(lines));

    // Create a map of variable names to actual sequence variables.
    // in the record is a variable in the sequence.
    Map<String, Variable> allvarnames = new LinkedHashMap<String, Variable>();
    for (Variable v : sequence.getAllVariables())
      allvarnames.put(v.getName(), v);

    // Read the VariableInfos.
    Set<VariableInfo> varinfos = new LinkedHashSet<VariableInfo>();
    for (int i = varIndex + 1; i < lines.size() ; i++) {
      String[] split = lines.get(i).trim().split("\\s");
      
      // Check that there is at least one token.
      // If line is all whitespace, array will have length 1, so need second check.      
      if (split.length <= 1 && split[0].length() == 0)
        throw new IllegalArgumentException("Invalid record: " + print(lines));
      String varname = split[0];
      if (!allvarnames.containsKey(varname))
        throw new IllegalArgumentException("Invalid record: " + print(lines));

      VariableInfo vi = new VariableInfo(allvarnames.get(varname));

      // Add compared values to vi.
      for (int j = 1 ; j < split.length ; j++) {
        vi.add_branch_compare(split[j]);
      }
      
      // Finally, add variable to list of variables.
      varinfos.add(vi);
    }
    return new DFResultsOneSeq(sequence, b, varinfos);
  }
  
  // Print each line separately.
  private static String print(List<String> record) {
    StringBuilder b = new StringBuilder();
    for (String line : record) {
      b.append(line);
      b.append(Globals.lineSep);
    }
    return b.toString();
  }

}
