package randoop.plugin.internal.core.runtime;

import randoop.runtime.IMessage;

public class NullMessageListener implements IMessageListener{

  @Override
  public void handleMessage(IMessage m) {
    // do nothing
  }

  @Override
  public void handleTermination() {
    // do nothing
  }
  
}
