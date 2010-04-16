package randoop.util;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.StateMatcher;
import randoop.util.HeapLinearizer.LinearizationKind;

public class HeapMatcher implements StateMatcher, Serializable {

  private static final long serialVersionUID = 0;

  Set<String> cache = new LinkedHashSet<String>();

  public boolean add(Object object) {
    List<Object> l = HeapLinearizer.linearize(object, LinearizationKind.FULL, false);
    String s = l.toString();
    boolean retval = cache.add(s);
    return retval;
  }

  public int size() {
    return cache.size();
  }

}
