package randoop.instrument;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/** Created by bjkeller on 6/21/17. */
public class MethodReplacements {
  private static List<String> signatureList = new ArrayList<>();

  static synchronized void addReplacedMethods(
      ConcurrentHashMap<MethodDef, MethodDef> replacementMap) {
    for (MethodDef def : replacementMap.keySet()) {
      signatureList.add(def.toString());
    }
  }

  public static synchronized List<String> getSignatureList() {
    return new ArrayList<>(signatureList);
  }
}
