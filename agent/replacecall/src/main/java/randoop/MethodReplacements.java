package randoop;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to communicate the signatures of methods replaced by the replacecall agent to Randoop
 * so Randoop can omit direct calls to these methods.
 *
 * <p>Uses synchronized access to a static list of the signature strings, because the replacecall
 * agent can run on multiple classes concurrently.
 *
 * <p>This list is set by {@link randoop.instrument.ReplaceCallAgent#premain(String,
 * Instrumentation)} before the {@link randoop.instrument.CallReplacementTransformer} is added to
 * the class loader, and the method {@link #addReplacedMethods(List)} should only be called at that
 * point. Randoop should add the result of {@link #getSignatureList()} to the {@code --omitmethods}
 * patterns before starting generation, because any method that is being replaced shouldn't be
 * directly called.
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
   *     list if that method hasn't been called
   */
  public static synchronized List<String> getSignatureList() {
    return new ArrayList<>(signatureList);
  }
}
