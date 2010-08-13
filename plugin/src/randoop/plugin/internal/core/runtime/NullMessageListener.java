package randoop.plugin.internal.core.runtime;

import randoop.runtime.IMessage;

public class NullMessageListener implements IMessageListener{

  public void handleMessage(IMessage m) {
    // do nothing
  }

  public void handleTermination() {
    // do nothing
  }
  
}
