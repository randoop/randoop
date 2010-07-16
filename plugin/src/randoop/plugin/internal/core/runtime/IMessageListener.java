package randoop.plugin.internal.core.runtime;

import randoop.runtime.IMessage;

/**
 * Provides a simple <code>handleMessage()</code> method that is used to process
 * <code>Message</code> objects received from Randoop during its execution.
 */
public interface IMessageListener {

  public void handleMessage(IMessage m);

  /**
   * Handles an unexpected termination
   */
  public void handleTermination();
}
