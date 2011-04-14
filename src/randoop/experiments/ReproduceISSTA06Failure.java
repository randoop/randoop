package randoop.experiments;

public class ReproduceISSTA06Failure extends Exception {

  // This class is never serialized; this is just to
  // appease eclipse.
  private static final long serialVersionUID = 1L;

  public ReproduceISSTA06Failure(String message) {
    super(message);
  }
}
