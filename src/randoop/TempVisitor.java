package randoop;

import java.util.*;

public final class TempVisitor implements ExecutionVisitor {

  public void visitBefore(ExecutableSequence sequence, int idx) {
    // do nothing.
  }

  public boolean visitAfter(ExecutableSequence sequence, int idx) {

//     if (idx == 0) {
//       for (int i = 0 ; i < sequence.sequence.size() ; i++) {
//         String st = sequence.sequence.getStatementKind(i).toString();
//         if (st.contains("Flat3") && st.contains("put")) {
//           System.out.println(sequence.sequence);
//           break;
//         }
//       }
//     }

    for (int i = 0 ; i <= idx ; i++) {
      ExecutionOutcome o = sequence.getResult(i);
      if (!(o instanceof NormalExecution)) {
        return true;
      }
      NormalExecution n = (NormalExecution)o;
      Object val = n.getRuntimeValue();
      if (val == null) continue;
      if (val.getClass().getName().contains("Flat3")) {
        String state = val.toString();
        if (states.add(state)) {
          System.out.println("@@@" + states.size());
        }
      }

    }
    return true;
  }

  private static Set<String> states = new LinkedHashSet<String>();

}
