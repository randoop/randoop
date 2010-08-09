package randoop.experiments;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.MSequence;
import randoop.MStatement;
import randoop.MVariable;
import randoop.util.Reflection;

public class BranchDirMutator {

  /**
   * Replaces all uses of oldv with newv.
   * Rearranges statements if necessary, e.g. if the first use of oldv comes
   * before the declaration of newv.
   */
  public static String replace(MSequence s, MVariable oldv, MVariable newv) {
    if (oldv == newv) throw new IllegalArgumentException();
    if (oldv.owner != s || newv.owner != s)
      throw new IllegalArgumentException();
    checkTypesOk(s, oldv, newv);

    if (firstUse(oldv) == -1)
      return null;
    if (oldv.getDeclIndex() > newv.getDeclIndex()) {
      return simpleReplace(s, oldv, newv);
    } else {
      return complexReplace(s, oldv, newv);
    }
  }

  // Checks that everywhere that oldv is used, newv can be substituted.
  private static void checkTypesOk(MSequence s, MVariable oldv, MVariable newv) {
    for (int i = 0 ; i < s.size() ; i++) {
      MStatement st = s.statements.get(i);
      assert st.inputs.size() == st.statementKind.getInputTypes().size();
      for (int j = 0 ; j < st.inputs.size() ; j++) {
        if (st.inputs.get(j).equals(oldv)) {
          assert Reflection.canBeUsedAs(newv.getType(), st.statementKind.getInputTypes().get(j));
        }
      }
    }
  }

  // Checks that everywhere that oldv is used, newv can be substituted.
  private static int firstUse(MVariable oldv) {
    MSequence s = oldv.owner;
    for (int i = 0 ; i < s.size() ; i++) {
      MStatement st = s.statements.get(i);
      assert st.inputs.size() == st.statementKind.getInputTypes().size();
      for (int j = 0 ; j < st.inputs.size() ; j++) {
        if (st.inputs.get(j).equals(oldv)) {
          return i;
        }
      }
    }
    return -1;
  }

  private static String complexReplace(MSequence s, MVariable oldv, MVariable newv) {
    assert oldv.getDeclIndex() < newv.getDeclIndex();

    // Create a new sequence where oldv comes after newv, not before.
    Set<MVariable> newvPreds = getPredecessors(newv);
    if (newvPreds.contains(oldv))
      return "Replacement failed: newv depends on oldv."; 
    List<MVariable> valuesAfter = valuesAfter(oldv);
    valuesAfter.retainAll(newvPreds);
    // newvPreds has the values that newv depends on that come after v.
    // Those (and newv itself) are the values that we have to move to before oldv.
    List<MVariable> valuesAfter2 = valuesAfter(oldv);
    valuesAfter2.removeAll(newvPreds);

    List<MStatement> newStatements = new ArrayList<MStatement>();
    for (int i = 0 ; i < oldv.getDeclIndex() ; i++) {
      newStatements.add(s.statements.get(i));
    }
    // At this point, add the values from newv.
    for (MVariable v : valuesAfter) {
      newStatements.add(v.getCreatingStatementWithInputs());
    }
    newStatements.add(oldv.getCreatingStatementWithInputs());
    for (MVariable v : valuesAfter2) {
      newStatements.add(v.getCreatingStatementWithInputs());
    }

    s.statements = newStatements;
    return simpleReplace(s, oldv, newv);
  }

  private static List<MVariable> valuesAfter(MVariable oldv) {
    List<MVariable> valuesAfter = new ArrayList<MVariable>();
    for (int i = oldv.getDeclIndex() + 1 ; i < oldv.owner.size() ; i++) {
      valuesAfter.add(oldv.owner.getVariable(i));
    }
    return valuesAfter;
  }

  private static String simpleReplace(MSequence s, MVariable oldv, MVariable newv) {
    assert oldv.getDeclIndex() > newv.getDeclIndex();
    for (int i = 0 ; i < s.size() ; i++) {
      MStatement st = s.statements.get(i);
      for (int j = 0 ; j < st.inputs.size() ; j++) {
        MVariable v = st.inputs.get(j);
        if (v==oldv) {
          assert i > newv.getDeclIndex();
          st.inputs.set(j, newv);
        }
      }
    }
    return null;
  }

  private static Set<MVariable> getPredecessors(MVariable v) {
    if (v==null) throw new IllegalArgumentException();
    List<MVariable> inputs = v.getCreatingStatementWithInputs().inputs;
    Set<MVariable> inputsSet = new LinkedHashSet<MVariable>(inputs);
    inputsSet.add(v);
    if (inputs.isEmpty())
      return inputsSet;
    for (MVariable input : inputs) {
      inputsSet.addAll(getPredecessors(input));
    }
    // Sanity check: all values belong to same sequence.
    MSequence owner = v.owner;
    for (MVariable input : inputsSet) assert input.owner == owner;
    return inputsSet;
  }

}
