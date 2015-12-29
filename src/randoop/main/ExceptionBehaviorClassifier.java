package randoop.main;

import randoop.main.GenInputsAbstract.BehaviorType;
import randoop.sequence.ExecutableSequence;

public class ExceptionBehaviorClassifier {

  public static BehaviorType classify(Throwable t, ExecutableSequence s) {
    
    if (t instanceof RuntimeException || t instanceof Error) { 
      // check for specific unchecked exceptions
      
      // NPE on no-null input
      if (t instanceof NullPointerException && s.hasNullInput()) {
        return GenInputsAbstract.npe_on_null_input;
      }
      
      if (t instanceof OutOfMemoryError) {
        return GenInputsAbstract.oom_exception;
      }
      
      // default failure exceptions 
      if (t instanceof AssertionError || t instanceof StackOverflowError) {
        return BehaviorType.ERROR;
      }
      
      return GenInputsAbstract.unchecked_exception;
      
    } else {
      return GenInputsAbstract.checked_exception;
    }

  }
  
}
