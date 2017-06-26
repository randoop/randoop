package randoop;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to communicate the signatures of methods replaced by the mapcalls agent to Randoop for
 * use in omitting direct calls to these methods. Uses synchronized access to a static list of the
 * signature strings. This list is set by {@link randoop.instrument.MapCallsAgent#premain(String,
 * Instrumentation)} before the {@link randoop.instrument.CallReplacementTransformer} is added to
 * the class loader, and the method {@link #addReplacedMethods(List)} should only be called at that
 * point. Randoop should use {@link #getSignatureList()} to add to the {@code --omitmethods}
 * patterns before starting generation.
 */
public class MethodReplacements {
  /** The list of signature strings */
  private static List<String> signatureList = new ArrayList<>();

  /**
   * Copies the given list of method signature strings to the list in this object.
   *
   * @param sigList the method signature list
   */
  public static synchronized void addReplacedMethods(List<String> sigList) {
    signatureList = new ArrayList<>(sigList);
  }

  /**
   * Returns a copy of the signature list in this class.
   *
   * @return the list of signature strings set by {@link #addReplacedMethods(List)}, or the empty
   *     list if that method hasn't been called.
   */
  public static synchronized List<String> getSignatureList() {
    return new ArrayList<>(signatureList);
  }
}
