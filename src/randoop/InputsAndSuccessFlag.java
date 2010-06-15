/**
 * 
 */
package randoop;

import java.util.List;

/**
 * Return type of an InputSelector's method selectsequencesAndMap(..).
 * It encapsulates a list of sequences and an inputMapping on those sequences.
 *
 */
public class InputsAndSuccessFlag   {

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
