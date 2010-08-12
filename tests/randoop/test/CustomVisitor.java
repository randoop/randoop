package randoop.test;

import java.lang.reflect.Method;

import randoop.ExecutableSequence;
import randoop.ExecutionOutcome;
import randoop.ExecutionVisitor;
import randoop.NormalExecution;

/**
 * A visitor that inserts calls "r = a.a1(A)" to every instance a of A found in
 * a sequence, and captures the return value of a.i after the call.
 */
public class CustomVisitor implements ExecutionVisitor {
  
  private static Method a1;
  static {
    try {
      a1 = A.class.getMethod("a1", A.class);
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  @Override
  public void initialize(ExecutableSequence executableSequence) {
    // nothing to initialize.
  }

  @Override
  public void visitAfter(ExecutableSequence esequence, int i) {
    
    // If sequence has not executed to completion, do nothing.
    if (i < esequence.sequence.size() - 1) {
      return;
    }
    
    // If we reach here, sequence has executed to completion. Look
    // for all objects of type randoop.test.A and for each such object,
    // call method a1(A), create some assertions, and add an appropriate
    // Check to the sequence with the given assertions.
    
    for (int j = 0 ; j < esequence.sequence.size() ; j++) {
      
      // Get the runtime object at index j.
      Object ret = null;
      ExecutionOutcome result = esequence.getResult(j);
      if (result instanceof NormalExecution) {
        ret = ((NormalExecution)result).getRuntimeValue();
      }

      // If runtime object is of type A, call method a1 on it.
      if (ret instanceof A) {
        
        A a = (A)ret;
        
        // Call a.a1(a), store result in aret.
        try {
          
          a1.invoke(a, a);
          
          // Create a Check for the value of a.i we just observed,
          // and tell Randoop that the check passed in this execution
          // (which obviously did).
          esequence.addCheck(i, new CustomCheck(esequence.sequence.getVariable(j), a.i), true);
          
        } catch (Exception e) {
          // Something went wrong with the call. In this
          // example, we ignore any objects for which this
          // happens.
          continue; // Move on to next object.
        }
      }
    }
    return;
  }

  @Override
  public void visitBefore(ExecutableSequence esequence, int i) {
    // Nothing to do before a given statement is executed.
    
  }
  
  
  
}

