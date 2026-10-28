package randoop.runtime;

/** A message indicating that the stream of messages from Randoop is being closed. */
public class ClosingStream implements IMessage {

  /** Creates a new ClosingStream. */
  public ClosingStream() {}

  /** ID for serializing this class. */
  private static final long serialVersionUID = 638560248651002139L;
}
