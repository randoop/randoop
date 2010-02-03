package randoop;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyUtils {
  
  
  public static Set<Integer> predecessors(Sequence s, int idx) {
    Set<Integer> ret = new LinkedHashSet<Integer>();
    Set<Variable> depvars = new LinkedHashSet<Variable>(s.getInputs(idx));
    for (int i = idx - 1 ; i >= 0 ; i--) {
      boolean toadd = false;
      if (depvars.contains(s.getVariable(i))) {
        toadd = true;
      } else {
        for (Variable v : s.getInputs(i)) {
          if (depvars.contains(v)) {
            toadd = true;
            break;
          }
        }
      }
      if (toadd) {
        ret.add(i);
        depvars.addAll(s.getInputs(i));
      }
    }
    return ret;
  }

  public static Sequence predecessorSequence(Sequence s, int idx) {
    
    List<Integer> indices = new ArrayList<Integer>(predecessors(s, idx));
    indices.add(idx); // Also add the index corresponding to the requested statement index.
    Collections.sort(indices);
    Map<Integer,Integer> newIdx = new LinkedHashMap<Integer,Integer>();
    for (int j = 0 ; j < indices.size() ; j++) {
      newIdx.put(indices.get(j), j);
    }
    Sequence news = new Sequence();
    for (int i = 0 ; i < indices.size() ; i++) {
      int oldidx = indices.get(i);
      List<Variable> oldins = s.getInputs(oldidx);
      List<Variable> newins = new ArrayList<Variable>(oldins.size());
      for (Variable oldv : oldins) {
        newins.add(news.getVariable(newIdx.get(oldv.index)));
      }
      news = news.extend(s.getStatementKind(oldidx), newins);
    }
    return news;
  }
  
  private static BitSet[] sets;
  private static int[] lastuse;
  private static int maxlength = 300; // TODO paramterize.
  static {
    sets = new BitSet[maxlength];
    for (int i = 0 ; i < maxlength ; i++) {
      sets[i] = new BitSet(i+1);
    }
    lastuse = new int[maxlength];
  }
  
  /**
   * The longest dep set of the sub-sequence s[0..idx].
   */
  public static BitSet longestDepSet(Sequence s, int idx) {
    if (s.size() == 0) throw new IllegalArgumentException("size must be greater than 0.");
    if (idx < 0 || idx >= s.size()) throw new IllegalArgumentException();
    assert s.size() <= maxlength;
    int max = -1;
    int maxidx = -1;
    for (int i = 0 ; i <= idx ; i++) {
      BitSet set = sets[i];
      set.clear();
      set.set(i);
      lastuse[i] = i;
      for (Variable invar : s.getInputs(i)) {
        set.or(sets[lastuse[invar.index]]);
        lastuse[invar.index] = i;
      }
      int size = set.cardinality();
      if (size > max) {
        max = size;
        maxidx = i;
      }
    }
    for (int i = 0 ; i < s.size() ; i++) {
      //System.out.println("@ " + sets[i]);
    }
    return sets[maxidx];
  }
  
  public static BitSet longestDepSet(Sequence s) {
    return longestDepSet(s, s.size() - 1);
  }
  
  public static Sequence getLongestDepSetSubSequence(Sequence s) {
    BitSet indices = longestDepSet(s, s.size() - 1);
    int length = indices.length();
    assert indices.get(length - 1);

    Map<Integer,Integer> newIdx = new LinkedHashMap<Integer,Integer>();
    int count = 0;
    for (int i = 0 ; i < length ; i++) {
      if (!indices.get(i))
        continue;
      newIdx.put(i, count++);
    }
    
    Sequence news = new Sequence();
    for (int i = 0 ; i < length ; i++) {
      if (!indices.get(i))
        continue;
      List<Variable> oldins = s.getInputs(i);
      List<Variable> newins = new ArrayList<Variable>(oldins.size());
      for (Variable oldv : oldins) {
        newins.add(news.getVariable(newIdx.get(oldv.index)));
      }
      news = news.extend(s.getStatementKind(i), newins);
    }
    return news;
  }
  
}
