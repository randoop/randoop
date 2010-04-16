package randoop.util;

import java.util.LinkedHashSet;
import java.util.Set;

import randoop.StateMatcher;
import randoop.util.HeapLinearizer.LinearizationKind;




public class HeapShapeMatcher implements StateMatcher {

  Set<String> cache = new LinkedHashSet<String>();

  public boolean add(Object object) {
    return cache.add(HeapLinearizer.linearize(object, LinearizationKind.SHAPE, false).toString());
  }

  public int size() {
    return cache.size();
  }

}
