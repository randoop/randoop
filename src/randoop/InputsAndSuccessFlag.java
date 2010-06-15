/**
 * 
 */
package randoop;

import java.util.List;

/**
 * Return type for ForwardGenerator's selectInputs method, which is
 * responsible for selecting a set of component sequences to be
 * concatenated into a longer sequence.
 * 
 */
class InputsAndSuccessFlag {

  /**
   * True if method selectInputs(StatementKind statement) was able to
   * find component sequences for all the input types required by
   * the given statement. 
   */
  public boolean success;
  
  public List<Sequence> sequences;
  
  // Each pair represents a variable.
  // The first element is the sequence that the variable comes from.
  // The second element is the index of the variable in the sequence.
  public List<Integer> variables;
  
  public InputsAndSuccessFlag(boolean success, List<Sequence> sequences,
      List<Integer> vars) {
    this.success = success;
    this.sequences = sequences;
    this.variables = vars;
  }

}
