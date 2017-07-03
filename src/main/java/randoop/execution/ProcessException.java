package randoop.execution;

/** Created by bjkeller on 6/30/17. */
public class ProcessException extends Throwable {
  private static final long serialVersionUID = 736230736083495268L;

  public ProcessException(String s, Throwable e) {
    super(s, e);
  }
}
