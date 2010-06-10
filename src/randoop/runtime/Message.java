package randoop.runtime;

import java.io.Serializable;

public class Message implements Serializable {
  private static final long serialVersionUID = -5479780441853668578L;

  public enum Type {
    START, WORK, DONE;
  }

  private Type fType;
  private long fTime;
  private long fSequences;

  public Message(Type type, long time, long sequences) {
    fType = type;
    fTime = time;
    fSequences = sequences;
  }

  /**
   * Returns the type of this message.
   */
  public Type getType() {
    return fType;
  }

  /**
   * Returns the number of seconds that have passed since the generation process
   * began.
   */
  public long getTime() {
    return fTime;
  }

  /**
   * Returns the number of inputs that have been generated.
   */
  public long getInputs() {
    return fSequences;
  }

  /**
   * Returns the percent of the operation complete relative to another
   * <code>WorkingStatus</code> representing 100% of the work complete.
   * 
   * @param reference
   * @return percent, where <code>1.0</code> represents 100%
   */
  public double getPercentDone(Message reference) {
    double p1 = (double) getTime() / reference.getTime();
    double p2 = (double) getInputs() / reference.getInputs();

    return Math.max(p1, p2);
  }
}
