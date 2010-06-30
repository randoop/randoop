package randoop;

import randoop.runtime.IMessage;

public class ErrorRevealed implements IMessage {

  public final String testCode;
  public final String description;

  public ErrorRevealed(String testCode, String description) {
    this.testCode = testCode;
    this.description = description;
  }

  private static final long serialVersionUID = -9131735651851725022L;

  @Override
  public String toString() {
    return description;
  }
  
}
