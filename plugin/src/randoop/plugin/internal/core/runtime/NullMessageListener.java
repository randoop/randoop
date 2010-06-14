package randoop.plugin.internal.core.runtime;

import randoop.runtime.Message;

public class NullMessageListener implements IMessageListener{

  @Override
  public void handleMessage(Message m) {
    // do nothing
  }
}
