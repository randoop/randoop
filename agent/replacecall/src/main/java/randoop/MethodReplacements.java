package randoop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class used to communicate values from the replacecall agent to Randoop.
 *
 * <p>Includes:
 *
 * <ol>
 *   <li>The signatures of methods replaced by the replacecall agent to Randoop so Randoop can omit
 *       direct calls to these methods. Any method that is being replaced shouldn't be directly
 *       called.
 *   <li>The absolute path to the agent jar, and the arguments used when the agent was run, which
 *       allow the agent to be run in the same way within Randoop.
 * </ol>
 *
 * <p>Uses synchronized access to a static list of the signature strings, because the replacecall
 * agent can run on multiple classes concurrently.
 *
 * <p>This list is set by {@link randoop.instrument.ReplaceCallAgent#premain(String,
 * Instrumentation)} before the {@link randoop.instrument.CallReplacementTransformer} is added to
 * the class loader, and the method {@link #setReplacedMethods(List)} should only be called at that
 * point. Randoop should add the result of {@link #getSignatureList()} to the {@code --omitmethods}
 * patterns before starting generation.
 */
public class MethodReplacements {
  /** The list of signature strings. */
  private static List<String> signatureList = new ArrayList<>();

  /** The string with the path to the replacecall agent. */
  private static String agentPath;

  /** The argument string for the call to the agent that set this variable. */
  private static String agentArgs;

  /**
   * Copies the given list of method signature strings to the list in this object, overwriting the
   * previous list.
   *
   * <p>Should only be called once.
   *
   * @param sigList the method signature list
   */
  public static synchronized void setReplacedMethods(List<String> sigList) {
    signatureList = new ArrayList<>(sigList);
  }

  /**
   * Returns a copy of the signature list in this class.
   *
   * @return the list of signature strings set by {@link #setReplacedMethods(List)}, or the empty
   *     list if that method hasn't been called
   */
  public static synchronized List<String> getSignatureList() {
    List<String> result = new ArrayList<>(signatureList);
    Collections.sort(result);
    return result;
  }

  /**
   * Set the path of the agent jar file.
   *
   * @param agentPath the agent jar file path
   */
  public static synchronized void setAgentPath(String agentPath) {
    MethodReplacements.agentPath = agentPath;
  }

  /**
   * Set the argument string of the agent.
   *
   * @param agentArgs the argument string for the agent
   */
  public static synchronized void setAgentArgs(String agentArgs) {
    MethodReplacements.agentArgs = agentArgs;
  }

  /**
   * Get the path to the agent jar file.
   *
   * @return the path to the agent jar file
   */
  public static synchronized String getAgentPath() {
    return agentPath;
  }

  /**
   * Get the argument string for running the agent.
   *
   * @return the argument string
   */
  public static synchronized String getAgentArgs() {
    return agentArgs;
  }
}
