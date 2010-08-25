package randoop.plugin.internal.core.runtime;

import randoop.runtime.IMessage;

/**
 * Provides a simple message handler method that is used to process
 * <code>IMessage</code> objects received from Randoop during its execution.
 * 
 * @author Peter Kalauskas
 */
public interface IMessageListener {

  /**
   * Handles a message from Randoop
   * 
   * @param m
   */
  public void handleMessage(IMessage m);

  /**
   * Handles an unexpected termination
   */
  public void handleTermination();

}
